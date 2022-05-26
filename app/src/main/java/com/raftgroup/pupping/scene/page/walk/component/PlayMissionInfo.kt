package com.raftgroup.pupping.scene.page.walk.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpMissionInfoBinding
import com.raftgroup.pupping.databinding.CpPlayMissionInfoBinding
import com.raftgroup.pupping.scene.component.info.WaypointInfo
import com.raftgroup.pupping.store.mission.Mission


class PlayMissionInfo: PageComponent, ListItem<Mission> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpPlayMissionInfoBinding
    override fun init(context: Context) {
        binding = CpPlayMissionInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    override fun setData(data:Mission, idx:Int){
        binding.title.text = data.type.info()
        binding.pointInfo.text = data.lv.point().toInt().toString()
        binding.lvInfo.setInfo(data.lv.icon(), data.lv.info(), data.lv.color())
        binding.timeInfo.value = data.viewDuration(context)
        binding.speedInfo.value = data.viewSpeed(context)
        binding.distenceInfo.value = data.viewDistence(context)
        binding.textSummry.text = data.summary
        binding.wayPointArea.removeAllViews()
        data.start?.let {
            binding.wayPointArea.addView(WaypointInfo(context).setStart(it.name, R.color.app_white))
        }
        data.waypoints.forEach {
            binding.wayPointArea.addView(WaypointInfo(context).setPoint(it.name, R.color.app_white))
        }
        data.destination?.let {
            binding.wayPointArea.addView(WaypointInfo(context).setDestination(it.name, R.color.app_white))
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

}