package com.raftgroup.pupping.scene.page.report
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.*
import com.lib.view.adapter.BaseAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PageHistoryBinding
import com.raftgroup.pupping.databinding.PageReportBinding
import com.raftgroup.pupping.scene.component.info.ArcGraphData
import com.raftgroup.pupping.scene.component.info.CompareData
import com.raftgroup.pupping.scene.component.info.CompareGraphData
import com.raftgroup.pupping.scene.component.list.History
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.api.rest.MissionData
import com.raftgroup.pupping.store.api.rest.MissionReport
import com.raftgroup.pupping.store.api.rest.MissionSummary
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class PageReport : PageFragment() {
    enum class ReportType{
        Weekly, Monthly
    }
    private val appTag = javaClass.simpleName
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    @Inject
    lateinit var ctx: Context
    @Inject
    lateinit var repository: PageRepository
    @Inject
    lateinit var dataProvider: DataProvider
    private lateinit var binding: PageReportBinding
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf()
        }

    override fun onViewBinding(): View {
        binding = PageReportBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTab.setup(null, isBack = true)
        binding.menuTab.setup(arrayListOf(R.string.reportWalkSummaryWeekly, R.string.reportWalkSummaryMonthly), 0){ idx->
            binding.menuTab.selectedIdx = idx
            type = when(idx){
                1 -> ReportType.Monthly
                else -> ReportType.Weekly
            }
            load()
        }

        val ctx = context ?: return
        petProfile?.let { profile->
            binding.textNickname.text = "${profile.nickName.value}${ctx.getString(R.string.owner)}"
            binding.duration.value = Mission.viewDuration(ctx, profile.totalExerciseDuration ?: 0.0)
            binding.distance.value = Mission.viewDistence(ctx, profile.totalExerciseDistance ?: 0.0)
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                if(res.contentID == petProfile?.petId.toString()) {
                    when(res.type){
                        ApiType.GetMissionSummary-> {
                            (res.data as? MissionSummary)?.let{ data->
                                loaded(data)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    private var userId:String = ""
    private var petProfile:PetProfile? = null
    private var cachedData:MissionSummary? = null
    private var type:ReportType = ReportType.Weekly
    override fun onPageParams(params: Map<String, Any?>): PageView {

        (params[PageParam.data] as? PetProfile)?.let { data->
            this.petProfile = data
        }
        (params[PageParam.id] as? String)?.let { id->
            this.userId = id
        }
        return super.onPageParams(params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        load()
    }

    private fun load(){
        cachedData?.let {
            loaded(it)
            return
        }
        dataProvider.requestData(ApiQ(appTag, ApiType.GetMissionSummary, contentID = petProfile?.petId.toString()))
    }

    private fun loaded(data:MissionSummary){
        when(type){
            ReportType.Weekly ->setWeeklyData(data)
            ReportType.Monthly ->setMonthlyData(data)
        }
    }

    private fun setWeeklyData(data:MissionSummary){
        data.weeklyReport?.let {
            val currentDaysWalkTimeIdx = setReport(it)
        }
        //setupData()

    }
    private fun setMonthlyData(data:MissionSummary){
        data.monthlyReport?.let {
            val currentDaysWalkTimeIdx = setReport(it)
        }
        //setupData()
    }

    private fun setReport(data:MissionReport): Int{
        var ctx = context ?: return -1
        var todayIdx:Int = -1
        val max =(data.missionTimes?.size ?: 7).toDouble()
        val myCount:Int = data.totalMissionCount?.toInt() ?: 0
        /*
        self.daysWalkCompareData
        = [
            CompareGraphData(value:myCount, max:max , color:Color.brand.primary, title:String.pageText.reportWalkDayCompareMe),
            CompareGraphData(value:Float(data.avgMissionCount ?? 0), max:max, color:Color.app.grey, title:String.pageText.reportWalkDayCompareOthers)
        ]
        */
        data.missionTimes?.let {missionTimes->
            val count = missionTimes.size
            val today = LocalDate.now().toFormatString("yyyyMMdd")
            val values:List<Double> = missionTimes.map{ time ->
                 min(50.0, time.v ?: 0.0) / 50.0
            }
            val lines:List<String> = missionTimes.mapIndexed { idx, time ->
                if (time.d == today) { todayIdx = idx }
                val date = time.d?.toDate("yyyyMMdd") ?: LocalDate.now()
                val mm = date.toFormatString("MM")?.toInt().toString()
                val dd = date.toFormatString("dd")?.toInt().toString()
                "$mm/$dd"
            }
            //self.daysWalkTimeData = LineGraphData(values: values, lines: lines)
            val unit = ctx.getString(R.string.reportWalkDayUnit)
            val unitType = if(type == ReportType.Weekly) ctx.getString(R.string.reportWalkDayTextWeek) else ctx.getString(R.string.reportWalkDayTextMonth)
            val endStr = "$myCount/$count$unit"
            val end = SpannableString(endStr)
            end.setSpan(
                ForegroundColorSpan(ctx.getColor(R.color.brand_primary)),
                0,
                myCount.toString().length,
                0
            )

            val titleLeading = "${ctx.getString(R.string.reportWalkDayText)} "
            val titleStr = "$titleLeading$myCount$unit\n$unitType"
            val title = SpannableString(titleStr)
            title.setSpan(
                ForegroundColorSpan(ctx.getColor(R.color.brand_primary)),
                titleLeading.length,
                titleLeading.length + myCount.toString().length + unit.length,
                0
            )
            val arcData = ArcGraphData(
                value = myCount.toDouble(),
                max = count.toDouble(),
                end = end,
                title = title
            )
            binding.walkGraph.setData(arcData)

            val avg = data.avgMissionCount?.toInt() ?: 0
            val compareLeading = "${ctx.getString(R.string.reportWalkDayCompareText1)} "
            val comparValue = when {
                avg == myCount -> {
                    ctx.getString(R.string.reportWalkDayCompareSame)
                }
                avg > myCount -> {
                    ctx.getString(R.string.reportWalkDayCompareLess)
                }
                else -> {
                    ctx.getString(R.string.reportWalkDayCompareMore)
                }
            }
            val compareStr = "$compareLeading$comparValue\n${ctx.getString(R.string.reportWalkDayCompareText2)}"
            val compare = SpannableString(compareStr)
            compare.setSpan(
                ForegroundColorSpan(ctx.getColor(R.color.brand_primary)),
                compareLeading.length,
                compareLeading.length + comparValue.length,
                0
            )
            val compareMe = CompareData(myCount.toDouble(), count.toDouble(), getCompareDesc(myCount.toDouble(), count))
            val compareOther = CompareData(avg.toDouble(), count.toDouble(), getCompareDesc(avg.toDouble(), count))
            val compareGraphData = CompareGraphData(compareMe, compareOther, compare)
            binding.walkCompare.setData(compareGraphData)
        }
        return todayIdx
    }
    private fun getCompareDesc(v:Double, m:Int):SpannableString{
        val value = v.toDecimal(f=2)
        val compareStr = "$value/$m"
        val compare = SpannableString(compareStr)
        compare.setSpan(
            ForegroundColorSpan(ctx.getColor(R.color.app_greyExtra)),
            0,
            value.length,
            0
        )
        return compare
    }

}