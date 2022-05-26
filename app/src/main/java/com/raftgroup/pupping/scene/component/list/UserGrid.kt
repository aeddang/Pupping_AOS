package com.raftgroup.pupping.scene.component.list

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.util.PageLog
import com.lib.view.adapter.BaseAdapter
import com.lib.view.adapter.ListItem
import com.lib.view.adapter.SingleAdapter
import com.lib.view.layoutmanager.GridSpacingItemDecoration
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpUserListItemBinding
import com.raftgroup.pupping.databinding.UiSwipeListviewBinding
import com.raftgroup.pupping.scene.component.info.ProfileImage
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.api.ApiValue
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt
@AndroidEntryPoint
class UserGrid : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
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
        binding.recyclerView.layoutManager = UserGridLayoutManager(context)
        val dpi = context.resources.displayMetrics.density
        val spacing = 0 //context.resources.getDimension(R.dimen.margin_thin).toInt()
        val padding = context.resources.getDimension(R.dimen.margin_thinHalf).toInt() * 2
        val paddingBottom = context.resources.getDimension(R.dimen.app_bottom).toInt()
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(
            2, spacing, false))
        binding.recyclerView.adapter = adapter
        binding.recyclerView.setPadding(padding,padding,padding,padding + paddingBottom)
        val w = (binding.recyclerView.width - spacing - (padding*2)).toDouble() / 2.0
        this.listSize = Size(w.roundToInt(),(244 * dpi).roundToInt() )

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
    fun setDatas(datas:List<User>){
        adapter.setDataArray(datas.toTypedArray())
        binding.refreshBody.isRefreshing = false
        setEmpty()
    }
    fun addDatas(datas:List<User>){
        adapter.addDataArray(datas.toTypedArray())
        binding.refreshBody.isRefreshing = false
        setEmpty()
    }
    fun addData(data:User, idx:Int = 0){
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



    inner class ListAdapter : SingleAdapter<User>(true, ApiValue.PAGE_SIZE) {
        override fun getListCell(parent: ViewGroup): ListItem<User> {
            val item = UserListItem(context)
            item.lifecycleOwner = lifecycleOwner
            return item
        }
    }

    inner class UserListItem : PageComponent, ListItem<User> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpUserListItemBinding
        override fun init(context: Context) {
            binding = CpUserListItemBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }

        override fun onLifecycleOwner(owner: LifecycleOwner) {
            super.onLifecycleOwner(owner)
        }

        @SuppressLint("SetTextI18n")
        override fun setData(data: User, idx:Int){
            binding.profile.setData(data.currentProfile)
            binding.petList.removeAllViews()
            val size = context.resources.getDimension(R.dimen.profile_thin).roundToInt()
            val spacing = context.resources.getDimension(R.dimen.margin_tinyExtra).roundToInt()
            data.pets.value?.forEachIndexed{index, pet->
                val icon = ProfileImage(context)
                val lay = ViewGroup.MarginLayoutParams(size, size)
                if (index != 0) lay.marginStart = spacing
                binding.petList.addView(icon, lay)
                icon.setData(pet.imagePath, R.dimen.profile_thin_half)
            }
            binding.btn.setOnClickListener {
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.User)
                        .addParam(PageParam.data, data)
                )
            }
        }
    }


    inner class UserGridLayoutManager(context: Context) : GridLayoutManager(context, 2) {
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
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            return layoutParams
        }

    }
}