package com.raftgroup.pupping.scene.page.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.lib.page.*
import com.lib.util.AppUtil
import com.lib.util.PageLog
import com.lib.util.showCustomToast
import com.lib.view.adapter.BaseViewPagerAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.component.info.MissionInfo
import com.raftgroup.pupping.scene.component.info.PetProfileInfo
import com.raftgroup.pupping.scene.page.my.PageProfileRegist
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.WeatherData
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.dialog.Alert
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PageHome : PageFragment(), PageRequestPermission{
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    @Inject lateinit var missionManager: MissionManager
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: PageHomeBinding
    private val adapter = PagerAdapter()
    override val pageChileren:ArrayList<PageView>?
        get(){
            val profile =  try { binding.viewPager.get(binding.viewPager.currentItem) } catch(e:Exception) {null}
            (profile as? PageView)?.let {
                return arrayListOf(binding.locationInfo, it)
            }
            return arrayListOf(binding.locationInfo)
        }

    override fun onViewBinding(): View {
        binding = PageHomeBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = adapter
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        missionManager.generator.isBusy.observe(this){ loading ->
            if (loading) pagePresenter.loading() else pagePresenter.loaded()
        }
        missionManager.isMissionsUpdated.observe(this){ update ->
            if (!update) return@observe
            PageLog.d(missionManager.missions, appTag)
            binding.refreshBody.isRefreshing = false
            binding.listBody.removeAllViews()
            missionManager.missions.forEach { mission ->
                val info = MissionInfo(ctx)
                binding.listBody.addView(info)
                info.setData(mission)
                info.setOnClickListener {
                    startMission(mission)
                }
            }
        }
        dataProvider.user.pets.observe(this){
            setupPets(it)
        }
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                when(res.type){
                    ApiType.GetWeather -> {
                        (res.data as? WeatherData)?.let{
                            binding.locationInfo.setData(it)
                        }
                    }
                    else -> {}
                }
            }
        }
        binding.emptyPet.setOnClickListener {
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.ProfileRegist)
                    .addParam(PageParam.type, PageProfileRegist.ProfileType.Pet)
            )
        }
    }

    private fun startMission(mission:Mission){
        if (dataProvider.user.pets.value?.isEmpty() == true) {
            Alert.Builder(pagePresenter.activity)
                .setSelectButtons()
                .setText(R.string.alertNeedProfileRegist)
                .onSelected {
                    if (it == 0) {
                        pagePresenter.openPopup(
                            pageProvider.getPageObject(PageID.ProfileRegist)
                                .addParam(PageParam.type, PageProfileRegist.ProfileType.Pet)
                        )
                    }
                }
                .show()
            return
        }
        if (missionManager.currentMission.value?.id == mission.id) {
            Toast(context).showCustomToast(
                R.string.alertCurrentPlayMission,
                pagePresenter.activity
            )
            return
        }
        if (pagePresenter.hasLayerPopup) {
            if (missionManager.currentMission.value == null) {
                Alert.Builder(pagePresenter.activity)
                    .setSelectButtons()
                    .setText(R.string.alertPrevPlayWalk)
                    .onSelected {
                        if (it == 0) {
                            pagePresenter.closePopupId(PageID.Walk.value)
                            startMission(mission)
                        }
                    }
                    .show()
            }
            return
        }
        if ( missionManager.currentMission.value != null) {
            Alert.Builder(pagePresenter.activity)
                .setSelectButtons()
                .setText(R.string.alertPrevPlayMission)
                .onSelected {
                    if (it == 0) {
                        missionManager.endMission()
                        missionManager.startMission(mission)
                        pagePresenter.openPopup(
                            pageProvider.getPageObject(PageID.Mission)
                                .addParam(PageParam.data, mission)
                        )
                    }
                }
                .show()

        } else {
            missionManager.startMission(mission)
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.Mission)
                    .addParam(PageParam.data, mission)
            )
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pagePresenter.requestPermission(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), this)

        dataProvider.user.pets.value?.let {
            setupPets(it)
        }
    }

    private fun setupPets(pets:List<PetProfile>){
        adapter.setData(pets.toTypedArray())
        binding.viewPager.currentItem = 0
        binding.emptyPet.visibility = if (pets.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
        super.onRequestPermissionResult(resultAll, permissions)
        if (!resultAll) return
        binding.refreshBody.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary)
        binding.refreshBody.setOnRefreshListener {
            if (!missionManager.isBusy) reset()
        }
        reloadLocation()
        if (missionManager.missions.isNotEmpty()) return
        reloadMission()
    }

    private fun reset(){
        reloadLocation()
        reloadMission()
    }

    @SuppressLint("MissingPermission")
    private fun reloadLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location ?: return@addOnSuccessListener
            binding.locationInfo.setAdress(AppUtil.getAddress(ctx, LatLng(location.latitude, location.longitude)))
            val query = HashMap<String,String>()
            query[ApiField.lat] = location.latitude.toString()
            query[ApiField.lng] = location.longitude.toString()
            val q = ApiQ(appTag, ApiType.GetWeather, query=query, isOptional = true)

            dataProvider.requestData(q)
        }
    }

    private fun reloadMission(){

        missionManager.generateMission()
    }

    override fun onPause() {
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onDestroy() {
        super.onDestroy()
    }
    inner class PagerAdapter : BaseViewPagerAdapter<PetProfileInfo, PetProfile>(){
        override fun getPageView(container: ViewGroup, position: Int): PetProfileInfo {
            val item = PetProfileInfo(context ?: ctx)
            item.lifecycleOwner = this@PageHome
            this.pages?.get(position)?.let {
                item.setData(it)
            }
            return item
        }
    }

}