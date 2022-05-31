package com.raftgroup.pupping.scene.page.walk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.lib.page.*
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.my.PageProfileRegist
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkEventType
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkModel
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType
import com.raftgroup.pupping.scene.page.walk.model.Walk
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.dialog.Alert
import com.skeleton.module.ViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@AndroidEntryPoint
class PageWalk(val type:PlayWalkType) : PageDragingFragment(), PageRequestPermission{
    override fun getContentView(): PageDragingView  = binding.content
    override fun getBodyView(): View = binding.body
    override fun getBackgroundView(): View = binding.bg
    private val appTag = javaClass.simpleName
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    @Inject
    lateinit var activityModel: ActivityModel
    @Inject
    lateinit var repository: PageRepository
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var missionManager: MissionManager
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    lateinit var viewModel: PlayWalkModel
    private lateinit var binding: PageWalkBinding
    private var mission:Mission? = null
    private var gestureArea:RectF? = null
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.playMap, binding.playMissionInfo, binding.playInfo, binding.playSummry)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PlayWalkModel::class.java)
    }
    override fun onViewBinding(): View {
        binding = PageWalkBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.type = if (mission == null) PlayWalkType.Walk  else PlayWalkType.Mission
        pageViewModel = viewModel
        val top = context?.resources?.getDimension(R.dimen.app_top) ?: 100f
        gestureArea = RectF(-1f, 0f, -1f, top)
        binding.content.gestureArea = gestureArea
        binding.playSummry.alpha = 0f
        binding.playMap.initializaMap(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            closeMission()
        }
        binding.btnDragOnOff.setOnClickListener {
            if (isBottomMode) binding.content.onGestureReturn() else binding.content.onGestureClose()
        }
        binding.playMissionInfo.setOnClickListener{
            binding.playMissionInfo.toggleActive()
        }
        mission?.let { mission->
            binding.playMissionInfo.setData(mission)
        }
        binding.btnStart.setOnClickListener {
            dataProvider.user.pets.value?.let { pets ->
                if (pets.size == 1){
                    start()
                } else {
                    pagePresenter.openPopup(
                        pageProvider.getPageObject(PageID.SelectProfile)
                    )
                }
            }

        }
        when (type){
            PlayWalkType.Walk -> {

                binding.playMissionInfo.visibility = View.GONE
            }
            PlayWalkType.Mission -> {

            }
        }
        binding.playInfo.visibility = View.GONE
        updateBottomPos()
    }

    var isInit:Boolean = true
    private fun updateBottomPos(){
        context?.let { ctx->
            val bt = if(activityModel.useBottomTab.value == true || isInit) ctx.resources.getDimension(R.dimen.app_bottom).roundToInt() else 0
            val floating = ctx.resources.getDimension(R.dimen.app_floating_bottom).roundToInt()
            closePos = bt+floating
            isInit = false
        }
    }

    private fun closeMission(){
        val ctx = context ?: return
        Alert.Builder(pagePresenter.activity)
            .setSelectButtons()
            .setText(type.getCloseMsg(ctx))
            .onSelected {
                if (it == 0){
                    when (type) {
                        PlayWalkType.Walk ->  onCompleted()
                        PlayWalkType.Mission ->  pagePresenter.closePopup(pageObject?.key)
                    }
                }
            }
            .show()
    }

    override val hasBackPressAction: Boolean
        get(){
            closeMission()
            return true
        }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        activityModel.useBottomTab.removeObservers(this)
        activityModel.isMovingLayer.value = false
        repository.disposeLifecycleOwner(this)
        if (mission?.isCompleted == true) {return}
        if (missionManager.currentMission.value?.id == mission?.id) {
            missionManager.endMission()
            activityModel.isPlaying.value = false
        }else if (mission == null && missionManager.currentMission.value == null) {
            activityModel.isPlaying.value = false
        }
        mission = null

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        activityModel.isPlaying.value = true
        activityModel.useBottomTab.observe(this){
            if(pagePresenter.currentTopPage == pageObject) return@observe
            updateBottomPos()
        }
        viewModel.event.observe(this){ evt->
            when (evt?.type){
                PlayWalkEventType.Start -> {
                    binding.playInfo.visibility = View.VISIBLE
                    binding.btnStart.visibility = View.GONE
                }
                PlayWalkEventType.Completed -> {
                    when (type) {
                        PlayWalkType.Walk -> closeMission()
                        PlayWalkType.Mission ->  onCompleted()
                    }
                }
            }
        }
        pagePresenter.observable.event.observe(this){ evt->
            when(evt?.eventType){
                PageSelectProfile.CONFIRM -> start()
                else ->{}
            }
        }

    }

    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.data] as? Mission)?.let { data->
            mission = data
        }
        return super.onPageParams(params)
    }
    override fun onClose(view: PageDragingView) {
        super.onClose(view)
        if (isBottomMode) pagePresenter.closePopup(pageObject?.key)
        else onBottomMode()
        activityModel.isMovingLayer.value = false
    }

    override fun onReturn(view: PageDragingView) {
        super.onReturn(view)
        onDefaultMode()
        activityModel.isMovingLayer.value = false
    }

    override fun onMove(view: PageDragingView, pct: Float) {
        super.onMove(view, pct)
        binding.playSummry.alpha = 1f-pct
        activityModel.isMovingLayer.value = true
    }

    override fun onAnimate(view: PageDragingView, pct: Float) {
        super.onAnimate(view, pct)
        binding.playSummry.alpha = 1f-pct
    }

    private fun start(){
        val ctx = context ?: return
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        mission?.let {
            viewModel.startMission(it, fusedLocationClient)
            return
        }
        viewModel.startWalk(fusedLocationClient)
    }

    private var isBottomMode = false
    private fun onBottomMode(){
        if (isBottomMode) return
        isBottomMode = true
        pageObject?.isLayer = true
        binding.content.gestureArea = null
        binding.btnDragOnOff.selected = true
        binding.playSummry.alpha = 1f
        binding.body.setRadius(R.dimen.radius_regular)

    }
    private fun onDefaultMode(){
        if (!isBottomMode) return
        isBottomMode = false
        pageObject?.isLayer = false
        binding.content.gestureArea = gestureArea
        binding.btnDragOnOff.selected = false
        binding.playSummry.alpha = 0f
        binding.body.setRadius(null)
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

    private fun onCompleted(){
        val playWalkModel = viewModel as? PlayWalkModel ?: return
        val playTime = playWalkModel.playTime.value?.toDouble() ?: 0.0
        val playDistence = playWalkModel.playDistence.value ?: 0.0
        when (type) {
            PlayWalkType.Walk ->  {
                if (playDistence <= PlayWalkModel.limitedDestance ){
                    pagePresenter.closePopup(pageObject?.key)
                    return
                }
                val walk = Walk()
                playWalkModel.currentLocation.value?.let {
                    playWalkModel.currentLocation.value?.let {
                        walk.locations = arrayListOf(it)
                    }
                    walk.playTime = playTime
                    walk.playDistence = playDistence

                }
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.WalkCompleted)
                        .addParam(PageParam.data, walk)
                )
            }
            PlayWalkType.Mission ->  {
                mission?.completed(
                    playTime,
                    playDistence
                )
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.WalkCompleted)
                        .addParam(PageParam.data, mission)
                )
            }
        }

    }

}