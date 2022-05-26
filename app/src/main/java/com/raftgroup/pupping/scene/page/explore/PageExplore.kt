package com.raftgroup.pupping.scene.page.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.get
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

import com.lib.page.*
import com.lib.util.DataLog
import com.lib.util.showCustomToast
import com.lib.view.adapter.BaseAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.*
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.User
import com.skeleton.component.dialog.Alert
import com.skeleton.component.dialog.Select
import com.skeleton.sns.SnsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject




@AndroidEntryPoint
class PageExplore : PageFragment(), PageRequestPermission{
    companion object{
        val filters = arrayOf(
            "none",
            "500m",
            "1Km",
            "5km",
            "10Km"
        )
        val values = arrayOf( 0, 500, 1000, 5000, 10000)
        val zooms = arrayOf( 8.0f, 14.0f, 13.0f, 12.0f, 10.0f)
        private var filter:Int = 0
    }
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    @Inject lateinit var snsManager: SnsManager
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider
    @Inject lateinit var activityModel: ActivityModel
    private lateinit var binding: PageExploreBinding

    private var currentFilterIdx:Int = 0
    private var isMap:Boolean = true
    private var isMapViewAble = true
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.userMap, binding.userList)
        }


    override fun onViewBinding(): View {
        binding = PageExploreBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentFilterIdx = PageExplore.filter
        val isPlaying = activityModel.isPlaying.value ?: false
        isMap = !isPlaying
        isMapViewAble = !isPlaying
        binding.userMap.initializaMap(view, savedInstanceState)
        binding.pageTab.setup(R.string.titleExplore)
        binding.userList.setup( object : BaseAdapter.Delegate {
            override fun onBottom(page: Int, size: Int) {
                super.onBottom(page, size)
                currentPage = page
                load()
            }

            override fun onReflash() {
                super.onReflash()
                reload()
            }
        })

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        binding.btnList.setOnClickListener {
            setViewType(false)
        }
        binding.btnMap.setOnClickListener {
            if (!isMapViewAble) {
                Toast(context).showCustomToast(R.string.alertCurrentPlayingDisable, pagePresenter.activity)
                return@setOnClickListener
            }
            setViewType(true)
        }
        binding.btnSort.setOnClickListener {
            Select.Builder(context)
                .setButtons(PageExplore.filters)
                .setSelected(currentFilterIdx)
                .onSelected { selectIdx ->
                    setFilter(selectIdx)
                }
                .show()
        }
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                if(res.id != appTag) return@observe
                when(res.type){
                    ApiType.SearchMission -> {
                        (res.data as? List<MissionData>)?.let {
                            loaded(it)
                        }
                    }
                    else -> {}
                }
            }
        }
        activityModel.isPlaying.observe(this){
            isMapViewAble = !it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityModel.isPlaying.removeObservers(this)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pagePresenter.requestPermission(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), this)
        setViewType(isMap, true)
        setFilter(currentFilterIdx)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
        super.onRequestPermissionResult(resultAll, permissions)
        binding.userMap.onRequestPermissionResult(resultAll, permissions)
    }
    private fun setFilter(idx:Int){
        PageExplore.filter = idx
        currentFilterIdx = idx
        binding.btnSort.text = if(idx == 0) context?.getString(R.string.filter)
        else "${context?.getString(R.string.near)}${PageExplore.filters[idx]}"
        binding.btnSort.selected = idx != 0
        reload()
    }
    private fun setViewType(isMap:Boolean, isFirst:Boolean = false){
        this.isMap = isMap
        if (isMap){
            binding.btnMap.selected = true
            binding.btnList.selected = false
            currentLocation?.let {
                binding.userMap.moveMe(PageExplore.zooms[currentFilterIdx], it)
            }
            binding.userMap.visibility = View.VISIBLE
            binding.userList.visibility = View.GONE
        } else {
            binding.btnMap.selected = false
            binding.btnList.selected = true
            binding.userMap.visibility = View.GONE
            binding.userList.visibility = View.VISIBLE
        }
        if (isFirst) return
        if (userDatas.isEmpty()) {
            reload()
        } else {
            binding.userList.resetDatas()
            binding.userMap.resetMarkers()
            if (isMap) binding.userMap.addMarkers(userDatas) else binding.userList.setDatas(userDatas)
        }
    }

    private val userDatas:MutableList<User> = arrayListOf()
    private var currentPage:Int = 0
    private var currentLocation :Location? = null
    private fun reload(){
        userDatas.clear()
        binding.userList.resetDatas()
        binding.userMap.resetMarkers()
        currentPage = 0
        if(currentFilterIdx == 0) {
            load()
            if (isMap) {
                binding.userMap.moveMe(PageExplore.zooms[currentFilterIdx])
            }
        } else {
            loadMyLocation()
        }
    }
    @SuppressLint("MissingPermission")
    private fun loadMyLocation(){
        val ctx = context
        ctx  ?: return
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            currentLocation = location
            if(currentLocation == null) {
                Alert.Builder(ctx)
                    .setText(R.string.alertLocationDisable)
                    .show()
            } else {
                load()
                if (isMap) {
                    binding.userMap.moveMe(PageExplore.zooms[currentFilterIdx], location)
                }
            }
        }
    }

    private fun load(){
        val query = HashMap<String,String>()
        query[ApiField.missionCategory] = MissionCategory.All.getApiCode()
        if (currentFilterIdx == 0) {
            query[ApiField.searchType] = MissionSearchType.Random.getApiCode()
        } else {
            currentLocation?.let { loc ->
                query[ApiField.searchType] = MissionSearchType.User.getApiCode()
                query[ApiField.lng] = loc.longitude.toString()
                query[ApiField.lat] = loc.latitude.toString()
                query[ApiField.distance] = PageExplore.values[currentFilterIdx].toString()
            }
        }
        query[ApiField.page] = currentPage.toString()
        dataProvider.requestData(ApiQ(appTag, ApiType.SearchMission, query = query))
    }
    private fun loaded(datas:List<MissionData>){
        val added = datas.map { User().setData(it) }
        userDatas.addAll(added)
        if (isMap) binding.userMap.addMarkers(added) else binding.userList.addDatas(added)
    }
}