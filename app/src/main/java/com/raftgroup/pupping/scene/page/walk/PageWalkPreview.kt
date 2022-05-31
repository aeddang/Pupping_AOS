package com.raftgroup.pupping.scene.page.walk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lib.page.*
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.dialog.Alert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@AndroidEntryPoint
class PageWalkPreview(val type:PlayWalkType) : PageFragment(), PageRequestPermission{

    private val appTag = javaClass.simpleName
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    @Inject
    lateinit var activityModel: ActivityModel
    @Inject
    lateinit var ctx: Context
    @Inject
    lateinit var repository: PageRepository
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var missionManager: MissionManager

    private lateinit var binding: PageWalkPreviewBinding
    private var mission:Mission? = null
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.playMap, binding.playMissionInfo)
        }

    override fun onViewBinding(): View {
        binding = PageWalkPreviewBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playMap.initializaMap(view, savedInstanceState)
        binding.pageTab.setup(R.string.titleMissionInfo, isClose = true)
        mission?.let {
            binding.playMissionInfo.setData(it)
        }
    }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
        activityModel.isMovingLayer.removeObservers(this)
        mission = null
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        activityModel.isMovingLayer.observe(this){isMoving->
            if( isMoving ){
                binding.playMap.visibility = View.GONE
            }
        }
        pagePresenter.observable.event.observe(this){ evt->
            when(evt?.type){
                PageEventType.WillChangePage ->{
                    if (evt.id == pageObject?.pageID){
                        binding.playMap.visibility = View.VISIBLE
                    } else {
                        binding.playMap.visibility = View.GONE
                    }
                }
                else -> {}
            }

        }

    }

    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.data] as? Mission)?.let { data->
            mission = data
        }
        return super.onPageParams(params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pagePresenter.requestPermission(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), this)
    }
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
        super.onRequestPermissionResult(resultAll, permissions)
        binding.playMap.onRequestPermissionResult(resultAll, permissions)
        binding.playMap.moveMe(17.0f)
    }

}