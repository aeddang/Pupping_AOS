package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetAboutBinding
import com.raftgroup.pupping.databinding.CpPetHistoryBinding
import com.raftgroup.pupping.scene.component.button.CheckButton
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class PetHistory : PageComponent, ListItem<PetProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPetHistoryBinding
    private var profileData:PetProfile? = null
    override fun init(context: Context) {
        binding = CpPetHistoryBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    fun setup(completionHandler:(MissionCategory?) -> Unit){
        binding.btnWalk.setOnClickListener {
            completionHandler(MissionCategory.Walk)
        }
        binding.btnMission.setOnClickListener {
            completionHandler(MissionCategory.Mission)
        }
        binding.btnRecord.setOnClickListener {
            completionHandler(null)
        }
    }

    override fun setData(data: PetProfile, idx: Int) {
        profileData = data
        data.totalWalkCount?.let { count ->
            binding.btnWalk.text = context.getString(R.string.profileHistoryTotalWalk).replace("%s", count.toString())
        }
        data.totalMissionCount?.let { count ->
            binding.btnMission.text = context.getString(R.string.profileHistoryTotalMission).replace("%s", count.toString())
        }

        if (data.totalExerciseDistance != null && data.totalExerciseDuration != null){
           val record = "${Mission.viewDistence(context, data.totalExerciseDistance!!)}" + " / " +
                   "${Mission.viewDuration(context, data.totalExerciseDuration!!)}"
            binding.btnRecord.text = context.getString(R.string.profileHistoryTotalRecord).replace("%s", record)
        }else{
            data.totalExerciseDistance?.let { dis ->
                binding.btnRecord.text = context.getString(R.string.profileHistoryTotalRecord).replace("%s",
                    Mission.viewDistence(context, dis))
            }
            data.totalExerciseDuration?.let { dur ->
                binding.btnRecord.text = context.getString(R.string.profileHistoryTotalRecord).replace("%s",
                    Mission.viewDuration(context, dur))
            }
        }
    }
}