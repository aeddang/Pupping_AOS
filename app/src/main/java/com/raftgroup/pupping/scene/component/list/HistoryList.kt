package com.raftgroup.pupping.scene.component.list

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.lib.page.PageComponent
import com.lib.util.animateAlpha
import com.lib.util.animateSize
import com.lib.util.toDate
import com.lib.util.toFormatString
import com.lib.view.adapter.BaseAdapter
import com.lib.view.adapter.ListItem
import com.lib.view.adapter.SingleAdapter
import com.lib.view.layoutmanager.SpanningLinearLayoutManager
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpHistoryItemBinding
import com.raftgroup.pupping.databinding.UiSwipeListviewBinding
import com.raftgroup.pupping.store.api.ApiValue
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.api.rest.MissionData
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionLv
import java.util.*
import kotlin.math.roundToInt

class History{
    var missionId: Int? = null ; private set
    var category: String? = null ; private set
    var title: String? = null ; private set
    var imagePath: String? = null ; private set
    var description: String? = null ; private set
    var date: String? = null ; private set
    var duration: Double? = null ; private set
    var distance: Double? = null ; private set
    var point: Double? = null ; private set
    var lv:MissionLv? = null ; private set
    var missionCategory:MissionCategory? = null ; private set
    var index:Int = -1; private set
    var isExpanded:Boolean = false
    fun setData(data:MissionData) : History{
        missionCategory = MissionCategory.getCategory(data.missionCategory)
        missionId = data.missionId
        title = data.title
        imagePath = data.pictureUrl
        description = data.description
        lv = MissionLv.getMissionLv(data.difficulty)
        duration = data.duration
        distance = data.distance
        point = data.point ?: 0.0
        date = data.createdAt?.toDate("yyyy-MM-dd'T'HH:mm:ss")?.toFormatString("yy-MM-dd HH:mm")
        return this
    }
}

class HistoryList : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    private val adapter = ListAdapter()
    private lateinit var binding: UiSwipeListviewBinding
    private var selectedHandler:((Picture, View) -> Unit)? = null
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
        binding.recyclerView.adapter = adapter
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter.removeAllData()
        binding.recyclerView.adapter = null
    }

    fun setSelected(completionHandler:(Picture, View) -> Unit) {
        selectedHandler = completionHandler
    }

    fun setup(l: BaseAdapter.Delegate){
        binding.refreshBody.setOnRefreshListener {
            l.onReflash()
        }
        adapter.setOnAdapterListener(l)
    }
    fun resetDatas(){
        adapter.reset()
        binding.refreshBody.isRefreshing = false
    }

    fun setDatas(datas:List<History>){
        adapter.setDataArray(datas.toTypedArray())
        setEmpty()
    }
    fun addDatas(datas:List<History>){
        adapter.addDataArray(datas.toTypedArray())
        binding.refreshBody.isRefreshing = false
        setEmpty()
    }

    fun addData(data:History ,idx:Int = 0){
        adapter.insertData(data, idx)
        setEmpty()
    }

    private fun setEmpty(){
        if (adapter.getDatas().isEmpty()){
            binding.emptyInfo.visibility = View.VISIBLE
        } else {
            binding.emptyInfo.visibility = View.GONE
        }
    }
    fun getDatas() = adapter.getDatas()

    inner class ListAdapter : SingleAdapter<History>(true, ApiValue.PAGE_SIZE) {
        override fun getListCell(parent: ViewGroup): ListItem<History> {
            val item = HistoryListItem(context)
            return item
        }
    }

    inner class HistoryListItem : PageComponent, ListItem<History> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpHistoryItemBinding
        override fun init(context: Context) {
            binding = CpHistoryItemBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }

        override fun onLifecycleOwner(owner: LifecycleOwner) {
            super.onLifecycleOwner(owner)

        }
        @SuppressLint("SetTextI18n")
        override fun setData(data: History, idx:Int){
            binding.imgLv.visibility = View.GONE
            binding.textTitle.visibility = View.GONE
            binding.textDesc.visibility = View.GONE
            setExpand(data.isExpanded)
            Glide.with(context)
                .load(data.imagePath)
                .error(R.drawable.img_empty_user_profile)
                .into(binding.img)
            data.lv?.icon()?.let {
                binding.imgLv.visibility = View.VISIBLE
                binding.imgLv.setImageResource(it)
            }
            data.title?.let {
                binding.textTitle.visibility = View.VISIBLE
                binding.textTitle.text = it
            }
            data.description?.let {
                binding.textDesc.visibility = View.VISIBLE
                binding.textDesc.text = it
            }
            data.date?.let {
                binding.textDate.text = it
            }
            data.duration?.let {
                binding.duration.text = Mission.viewDuration(context, it)
            }
            data.distance?.let {
                binding.distance.text = Mission.viewDuration(context, it)
            }
            data.point?.let {
                binding.point.text = it.toString()
            }

            binding.btn.setOnClickListener {
                data.isExpanded = !data.isExpanded
                setExpand(data.isExpanded)
            }

        }
        private fun setExpand(isExpanded:Boolean){

            if (isExpanded){
                val size = context.resources.getDimension(R.dimen.profile_heavy).roundToInt()
                binding.imgBox.animateSize(Size(size, size)).start()
                binding.playBox.animateAlpha(1.0f, true)
                binding.textDesc.maxLines = Int.MAX_VALUE

            } else {
                val size = context.resources.getDimension(R.dimen.profile_regular).roundToInt()
                binding.imgBox.animateSize(Size(size, size)).start()
                binding.playBox.animateAlpha(0.0f, true, 10L)
                binding.textDesc.maxLines = 1
            }
        }
    }

}