package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpMissionInfoBinding
import com.raftgroup.pupping.store.mission.Mission


class MissionInfo: PageComponent, ListItem<Mission> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpMissionInfoBinding
    override fun init(context: Context) {
        binding = CpMissionInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun setData(data:Mission, idx:Int){
        binding.title.text = data.type.info()
        binding.pointInfo.text = data.lv.point().toInt().toString()
        binding.lvInfo.setInfo(data.lv.icon(), data.lv.info(), data.lv.color())
        binding.timeInfo.setText(data.viewDuration(context))
        binding.speedInfo.setText(data.viewSpeed(context))
        binding.distenceInfo.setText(data.viewDistence(context))
        binding.wayPointArea.removeAllViews()
        data.start?.let {
            binding.wayPointArea.addView(WaypointInfo(context).setStart(it.name))
        }
        data.waypoints.forEach {
            binding.wayPointArea.addView(WaypointInfo(context).setPoint(it.name))
        }
        data.destination?.let {
            binding.wayPointArea.addView(WaypointInfo(context).setDestination(it.name))
        }
    }

}