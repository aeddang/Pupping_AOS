package com.raftgroup.pupping.scene.component.list

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lib.page.PageComponent
import com.lib.util.PageLog
import com.lib.view.adapter.BaseAdapter
import com.lib.view.adapter.ListItem
import com.lib.view.adapter.SingleAdapter
import com.lib.view.layoutmanager.GridSpacingItemDecoration
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.UiSwipeListviewBinding
import com.raftgroup.pupping.store.api.ApiValue
import kotlin.math.roundToInt

class PictureGrid : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val appTag = javaClass.simpleName
    private val adapter = ListAdapter()
    private lateinit var binding: UiSwipeListviewBinding
    private var listSize:Size = Size(0,0)
    private var selectedHandler:((Picture, View) -> Unit)? = null
    override fun init(context: Context) {
        binding = UiSwipeListviewBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.refreshBody.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter.removeAllData()
        binding.recyclerView.adapter = null

    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        binding.recyclerView.layoutManager = PictureGridLayoutManager(context)
        val spacing = context.resources.getDimension(R.dimen.margin_thin).toInt()
        val padding = context.resources.getDimension(R.dimen.margin_light).toInt()
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(
            2, spacing, false))
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setPadding(padding,padding,padding,padding)

        val w = (binding.recyclerView.width - spacing - (padding*2)).toDouble() / 2.0
        val h = (w / PictureListContents.width.toDouble() * PictureListContents.height.toDouble()).roundToInt()
        this.listSize = Size(w.roundToInt(),h)

    }
    fun setSelected(completionHandler:(Picture, View) -> Unit) {
        selectedHandler = completionHandler
    }
    fun setup(l:BaseAdapter.Delegate){
        binding.refreshBody.setOnRefreshListener {
            l.onReflash()
        }
        adapter.setOnAdapterListener(l)
    }
    fun resetDatas(){
        adapter.reset()
        binding.refreshBody.isRefreshing = false
    }
    fun setDatas(datas:List<Picture>){
        adapter.setDataArray(datas.toTypedArray())
        binding.refreshBody.isRefreshing = false
        setEmpty()
    }
    fun addDatas(datas:List<Picture>){
        adapter.addDataArray(datas.toTypedArray())
        binding.refreshBody.isRefreshing = false
        setEmpty()
    }
    fun addData(data:Picture, idx:Int = 0){
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

    fun deleteData(pictureId:Int){
        adapter.paginationData.data.find { d ->
            (d as? Picture)?.let {
                return@find it.pictureId == pictureId
            }
            return@find false
        }?.let { find ->
            adapter.removeData(find)
        }
    }

    fun updateData(pictureId:Int, isFavorite:Boolean){
        adapter.paginationData.data.find { d ->
            (d as? Picture)?.let {
                return@find it.pictureId == pictureId
            }
            return@find false
        }?.let { find ->
            find.updata(isFavorite)
        }
    }

    inner class ListAdapter : SingleAdapter<Picture>(true, ApiValue.PAGE_SIZE) {
        override fun getListCell(parent: ViewGroup): ListItem<Picture> {
            val item = PictureListContents(context)
            item.lifecycleOwner = lifecycleOwner
            item.setSelected { data, v->
                selectedHandler?.let { it(data, v) }
            }
            return item
        }
    }

    inner class PictureGridLayoutManager(context: Context) : GridLayoutManager(context, 2) {
        override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
            return spanLayoutSize(super.generateDefaultLayoutParams())
        }
        override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
            return spanLayoutSize(super.generateLayoutParams(c, attrs))
        }
        override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
            return spanLayoutSize(super.generateLayoutParams(lp))
        }
        private fun spanLayoutSize(layoutParams: RecyclerView.LayoutParams): RecyclerView.LayoutParams {
            layoutParams.width = listSize.width
            layoutParams.height = listSize.height
            return layoutParams
        }

    }
}