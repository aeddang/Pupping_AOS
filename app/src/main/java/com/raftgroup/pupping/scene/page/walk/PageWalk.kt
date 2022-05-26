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
    lateinit var ctx: Context
    @Inject
    lateinit var repository: PageRepository
    @Inject
    lateinit var dataProvider: DataProvider
    @Inject
    lateinit var missionManager: MissionManager
    private lateinit var binding: PageMissionBinding
    private var mission:Mission? = null
    private var gestureArea:RectF? = null
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.playMap, binding.playMissionInfo, binding.playInfo)
        }

    override fun onViewBinding(): View {
        binding = PageMissionBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        Alert.Builder(pagePresenter.activity)
            .setSelectButtons()
            .setText(type.getCloseMsg())
            .onSelected {
                if (it == 0){
                    pagePresenter.closePopup(pageObject?.key)
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
            /*
            bottomPosJob?.cancel()
            bottomPosJob = scope.launch(scope.coroutineContextIO) {
                delay(AnimationUtil.ANIMATION_DURATION_LONG)
                withContext(scope.coroutineContext) {

                }
            }*/
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
    }

    override fun onReturn(view: PageDragingView) {
        super.onReturn(view)
        onDefaultMode()
    }

    override fun onMove(view: PageDragingView, pct: Float) {
        super.onMove(view, pct)
        binding.playSummry.alpha = 1f-pct
    }

    override fun onAnimate(view: PageDragingView, pct: Float) {
        super.onAnimate(view, pct)
        binding.playSummry.alpha = 1f-pct
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

}