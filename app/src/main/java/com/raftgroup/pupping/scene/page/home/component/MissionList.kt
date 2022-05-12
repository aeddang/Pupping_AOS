package com.raftgroup.pupping.scene.page.home.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lib.page.PageComponent
import com.lib.util.PageLog

import com.lib.view.adapter.ListItem
import com.lib.view.adapter.SingleAdapter
import com.lib.view.layoutmanager.SpacingItemDecoration
import com.lib.view.layoutmanager.SpanningLinearLayoutManager
import com.raftgroup.pupping.R

import com.raftgroup.pupping.databinding.UiSwipeListviewBinding
import com.raftgroup.pupping.scene.component.info.MissionInfo
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MissionList : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject lateinit var missionManager: MissionManager
    private val appTag = javaClass.simpleName
    private val adapter = ListAdapter()

    private lateinit var binding: UiSwipeListviewBinding
    override fun init(context: Context) {
        binding = UiSwipeListviewBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.recyclerView.layoutManager = SpanningLinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        val spacing = context.resources.getDimension(R.dimen.margin_regular).toInt()
        binding.recyclerView.setPadding(
            0, context.resources.getDimension(R.dimen.margin_medium).toInt(),
            0, context.resources.getDimension(R.dimen.app_bottom).toInt())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(SpacingItemDecoration(spacing))
        binding.refreshBody.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary)
        binding.refreshBody.setOnRefreshListener {
            if (!missionManager.isBusy) missionManager.generateMission()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.recyclerView.adapter = null
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
        missionManager.isMissionsUpdated.observe(owner){ update ->
            if (!update) return@observe
            PageLog.d(missionManager.missions, appTag)
            binding.refreshBody.isRefreshing = false
            adapter.setDataArray(missionManager.missions.toTypedArray())
        }
    }

    inner class ListAdapter : SingleAdapter<Mission>(true) {
        override fun onBindData(item: View?, data: Mission) {
            super.onBindData(item, data)
        }

        override fun getListCell(parent: ViewGroup): ListItem<Mission> {
            val item = MissionInfo(context)
            PageLog.d(item, appTag)
            return item
        }
    }

}