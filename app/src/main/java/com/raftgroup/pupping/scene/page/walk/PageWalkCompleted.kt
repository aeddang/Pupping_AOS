package com.raftgroup.pupping.scene.page.walk

import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.lib.page.*
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.scene.page.walk.model.Walk
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.DetectData
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PageWalkCompleted() : PageFragment(), PageRequestPermission{
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

    private lateinit var binding: PageWalkCompletedBinding
    private var mission:Mission? = null
    private var walk:Walk? = null
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.redeemInfo)
        }
    override val hasBackPressAction: Boolean
        get(){
            return true
        }


    override fun onViewBinding(): View {
        binding = PageWalkCompletedBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context ?: return
        mission?.let {
            val title = ctx.getString(R.string.missionCompleted)
            val playTime = (it.playTime/60).toDecimal(f=1)
            val text = ctx.getString(R.string.missionCompletedText).replace("%s",playTime)
            val point = it.lv.point()
            binding.redeemInfo.setup(title, text, point)
        }

        walk?.let {
            val title = ctx.getString(R.string.walkCompleted)
            val playTime = (it.playTime/60).toDecimal(f=1)
            val text = ctx.getString(R.string.walkCompletedText).replace("%s",playTime)
            val point = it.point()
            binding.redeemInfo.setup(title, text, point)
        }

        binding.redeemInfo.onClose{
            onCompleted()
        }

        binding.redeemInfo.onPictureCompleted {resource->
            val crop = Size(resource.width, resource.height).getCropRatioSize(Size(240,240))
            val cropImg = resource.centerCrop(crop)
            val resizeImg = cropImg.size(240,240)
            //resource.recycle()
            cropImg.recycle()
            dataProvider.requestData(ApiQ(appTag,ApiType.CheckHumanWithDog, requestData =  resizeImg))
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
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                if(res.id != appTag) return@let
                when(res.type){
                    ApiType.CompleteWalk -> {
                        onCompleted()
                        Toast(context).showCustomToast(R.string.walkCompletedSaved, pagePresenter.activity)
                    }
                    ApiType.CompleteMission -> {
                        mission?.let {
                            dataProvider.user.missionCompleted(it)
                        }
                        onCompleted()
                        Toast(context).showCustomToast(R.string.missionCompletedSaved, pagePresenter.activity)
                    }
                    ApiType.CheckHumanWithDog -> {
                        val data = res.data as? DetectData
                        if (data == null) {
                            Toast(context).showCustomToast(R.string.alertCompletedNeedPictureError, pagePresenter.activity)
                            return@let
                        }
                        if (data.isDetected != true) {
                            Toast(context).showCustomToast(R.string.alertCompletedNeedPictureError, pagePresenter.activity)
                            return@let
                        }
                        sendResult(data.pictureUrl)
                    }
                    else -> {}
                }
            }
        }
    }
    private fun sendResult(picturePath:String?){
        var withPets:List<PetProfile> = arrayListOf()
        dataProvider.user.pets.value?.let{ pets->
            withPets = pets.filter { it.isWith }
        }
        mission?.let {
            it.pictureUrl = picturePath
            sendMission(it, withPets)
        }
        walk?.let {
            it.pictureUrl = picturePath
            sendWalk(it, withPets)
        }
    }
    private fun sendMission(mission:Mission, withPets:List<PetProfile> ){
        val params = java.util.HashMap<String, Any>()
        params["missionCategory"] = MissionCategory.Mission.getApiCode()
        params["missionType"] = mission.type.info()
        params["title"] = context?.getString(mission.playType.info()) ?: ""
        params["description"] = mission.description
        params["difficulty"] = mission.lv.apiDataKey()
        params["duration"] = mission.playTime
        params["distance"] = mission.playDistence
        mission.pictureUrl?.let {  params["pictureUrl"] = it }
        val point = mission.lv.point()
        params["point"] = point
        params["experience"] = point
        params["petIds"] = withPets.map{it.petId}
        val geos: List<java.util.HashMap<String, Any>> = mission.allPoint.map{
            val geo = java.util.HashMap<String, Any>()
            geo["lat"] = it.latLng?.latitude ?: 0
            geo["lng"] = it.latLng?.longitude ?: 0
            geo
        }
        params["geos"] = geos
        dataProvider.requestData(ApiQ(appTag, ApiType.CompleteMission, body = params))

    }
    private fun sendWalk(walk:Walk, withPets:List<PetProfile> ){
        val params = java.util.HashMap<String, Any>()
        params["missionCategory"] = MissionCategory.Walk.getApiCode()
        params["duration"] = walk.playTime
        params["distance"] = walk.playDistence
        walk.pictureUrl?.let{ params["pictureUrl"] = it }
        val point = walk.point()
        params["point"] = point
        params["experience"] = point
        params["petIds"] = withPets.map{it.petId}
        val geos: List<java.util.HashMap<String, Any>> = walk.locations.map{
            val geo = java.util.HashMap<String, Any>()
            geo["lat"] = it.latitude
            geo["lng"] = it.longitude
            geo
        }
        params["geos"] = geos
        dataProvider.requestData(ApiQ(appTag, ApiType.CompleteWalk, body = params))
    }
    private fun onCompleted(){
        mission?.let {
            missionManager.completedMission()
            missionManager.endMission()
        }
        pagePresenter.closeAllPopup()
    }

    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.data] as? Mission)?.let { data->
            mission = data
        }
        (params[PageParam.data] as? Walk)?.let { data->
            walk = data
        }
        return super.onPageParams(params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }
    
}