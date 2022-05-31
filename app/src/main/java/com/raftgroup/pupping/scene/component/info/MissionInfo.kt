package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpMissionInfoBinding
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.provider.model.PetProfile


class  MissionInfo: PageComponent, ListItem<Mission> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpMissionInfoBinding
    override fun init(context: Context) {
        binding = CpMissionInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    override fun setData(data:Mission, idx:Int){
        binding.title.text = data.type.info()
        binding.pointInfo.text = data.lv.point().toInt().toString()
        binding.lvInfo.setInfo(data.lv.icon(), data.lv.info(), data.lv.color())
        binding.timeInfo.setText(data.viewDuration(context))
        binding.speedInfo.setText(data.viewSpeed(context))
        binding.distenceInfo.setText(data.viewDistence(context))
        binding.wayPointArea.removeAllViews()

        data.waypoints.forEachIndexed { idx, p->
            val point = WaypointInfo(context).setPoint(p.name, isLine = idx != 0)
            binding.wayPointArea.addView(point)
        }
        data.destination?.let {
            val point = WaypointInfo(context).setDestination(it.name, isLine = data.waypoints.isNotEmpty())
            binding.wayPointArea.addView(point)
        }
    }

}