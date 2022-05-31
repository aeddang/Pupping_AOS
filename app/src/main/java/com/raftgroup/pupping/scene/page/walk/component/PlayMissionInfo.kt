package com.raftgroup.pupping.scene.page.walk.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageComponent
import com.lib.page.PageView
import com.lib.page.PageViewModel
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpMissionInfoBinding
import com.raftgroup.pupping.databinding.CpPlayMissionInfoBinding
import com.raftgroup.pupping.scene.component.info.WaypointInfo
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkEventType
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkModel
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType
import com.raftgroup.pupping.store.mission.Mission


class PlayMissionInfo: PageComponent, ListItem<Mission> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpPlayMissionInfoBinding
    private var startPoint:WaypointInfo? = null
    private var wayPoints:ArrayList<WaypointInfo> = arrayListOf()
    override fun init(context: Context) {
        binding = CpPlayMissionInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    override fun onPageViewModel(vm: PageViewModel): PageView {
        super.onPageViewModel(vm)
        val owner = lifecycleOwner ?: return this
        (vm as? PlayWalkModel)?.let { playWalkModel ->
            playWalkModel.event.observe(owner){ evt->
                when (evt?.type){
                    PlayWalkEventType.Start -> {

                        onActive(false)
                    }
                    PlayWalkEventType.CompleteStep -> onCompleted(evt.data as? Int ?: 0)
                    else ->{

                    }
                }
            }
        }
        return this
    }
    override fun setData(data:Mission, idx:Int){
        binding.title.text = data.type.info()
        binding.pointInfo.text = data.lv.point().toInt().toString()
        binding.lvInfo.setInfo(data.lv.icon(), data.lv.info(), R.color.app_white)
        binding.timeInfo.value = null
        binding.speedInfo.value = null
        binding.distenceInfo.value = null
        binding.timeInfo.text = data.viewDuration(context)
        binding.speedInfo.text = data.viewSpeed(context)
        binding.distenceInfo.text = data.viewDistence(context)
        binding.textSummry.text = data.summary
        binding.textSummry.visibility = View.GONE
        binding.wayPointArea.removeAllViews()

        data.start?.let {
            val point =WaypointInfo(context).setStart(it.name, R.color.app_white)
            startPoint = point
            binding.wayPointArea.addView(point)
        }

        wayPoints.clear()
        data.waypoints.forEach {
            val point = WaypointInfo(context).setPoint(it.name, R.color.app_white)
            wayPoints.add(point)
            binding.wayPointArea.addView(point)
        }
        data.destination?.let {
            val point = WaypointInfo(context).setDestination(it.name, R.color.app_white)
            wayPoints.add(point)
            binding.wayPointArea.addView(point)
        }
    }

    private var isActive:Boolean = true
    fun toggleActive(){
        if (isActive) onActive(false) else onActive(true)
    }
    fun onActive(isAc:Boolean){
        isActive = isAc
        if(isAc){
            binding.wayPointArea.visibility = View.VISIBLE
            binding.textSummry.visibility = View.GONE
            binding.valueInfoBox.visibility = View.VISIBLE
        } else {
            binding.wayPointArea.visibility = View.GONE
            binding.textSummry.visibility = View.VISIBLE
            binding.valueInfoBox.visibility = View.GONE
        }

    }
    private fun onCompleted(step:Int){
        val find = step-1
        if (find < 0) {
            startPoint?.let {
                it.setStart(it.text, R.color.brand_secondary, true)
            }
            return
        }
        if (find >= wayPoints.size) return
        val current = wayPoints[find]
        if (find == wayPoints.size-1) current.setDestination(current.text, R.color.brand_secondary, true)
        else current.setPoint(current.text, R.color.brand_secondary, true)
    }
}