package com.raftgroup.pupping.scene.component.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.lib.view.adapter.SingleAdapter
import com.lib.view.layoutmanager.SpanningLinearLayoutManager
import com.raftgroup.pupping.databinding.CpPictureListBinding
import com.raftgroup.pupping.databinding.CpPictureListItemBinding
import com.raftgroup.pupping.store.api.ApiValue
import com.raftgroup.pupping.store.api.rest.PictureData
import java.util.*


class Picture{
    var index:Int = -1; private set
    var pictureId:Int = 0; private set
    var imagePath:String? = null; private set
    var originImagePath:String? = null; private set
    var ownerId:String = ""; private set
    val image: MutableLiveData<Bitmap?> = MutableLiveData<Bitmap?>(null)
    val isLike: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val likeValue: MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    var isEmpty:Boolean = true
    var requestCode = UUID.randomUUID().hashCode(); private set

    fun copy() : Picture{
        return Picture().setData(this)
    }

    fun setEmptyData(requestCode:Int? = null) : Picture{
        requestCode?.let { this.requestCode = it }
        isEmpty = true
        return this
    }
    fun setData(data:Picture) : Picture{
        requestCode = data.requestCode
        isEmpty = data.isEmpty
        index = data.index
        imagePath = data.imagePath
        originImagePath = data.originImagePath
        pictureId = data.pictureId ?: 0
        ownerId = data.ownerId ?: ""
        likeValue.value = data.likeValue.value ?: 0.0
        isLike.value = data.isLike.value ?: false
        return this
    }
    fun setData(data:PictureData, requestCode:Int? = null, index:Int = 0) : Picture{
        requestCode?.let { this.requestCode = it }
        isEmpty = false
        this.index = index
        imagePath = data.smallPictureUrl
        originImagePath = data.pictureUrl
        pictureId = data.pictureId ?: 0
        ownerId = data.ownerId ?: ""
        likeValue.value = data.thumbsupCount ?: 0.0
        isLike.value = data.isChecked ?: false
        return this
    }
    fun updata(isLike:Boolean) : Picture{
        if (isLike != this.isLike.value) {
            likeValue.value?.let { v->
                this.likeValue.value = if (isLike) v+1.0 else v-1.0
            }
            this.isLike.value = isLike
        }
        return this
    }
    fun removeObservers(owner: LifecycleOwner){
        image.removeObservers(owner)
        isLike.removeObservers(owner)
        likeValue.removeObservers(owner)
    }
}

class PictureList : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    companion object {
        const val width:Int = 180
        const val height:Int = 216
        const val pictureWidth:Int = PictureListContents.width * 2
        const val pictureHeight:Int = PictureListContents.height * 2
    }
    private val appTag = javaClass.simpleName
    private val adapter = ListAdapter()
    private lateinit var binding: CpPictureListBinding
    private var selectedHandler:((Picture, View) -> Unit)? = null
    override fun init(context: Context) {
        binding = CpPictureListBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.recyclerView.layoutManager = SpanningLinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
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
    fun setup(@StringRes titleRes:Int, l: OnClickListener? = null){
        binding.titleTab.setup(context.getString(titleRes) , l)
    }

    fun setup(title:String? = null, l: OnClickListener? = null){
        binding.titleTab.setup(title, l)
    }

    fun setDatas(datas:List<Picture>){
        adapter.setDataArray(datas.toTypedArray())
    }

    fun addData(data:Picture,idx:Int = 0){
        adapter.insertData(data, idx)
    }
    fun getDatas() = adapter.getDatas()

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
            val item = PictureListItem(context)
            item.lifecycleOwner = lifecycleOwner
            return item
        }
    }

    inner class PictureListItem : PageComponent, ListItem<Picture> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpPictureListItemBinding
        override fun init(context: Context) {
            binding = CpPictureListItemBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }

        override fun onLifecycleOwner(owner: LifecycleOwner) {
            super.onLifecycleOwner(owner)
            binding.contents.lifecycleOwner = owner
        }

        @SuppressLint("SetTextI18n")
        override fun setData(data: Picture, idx:Int){
            binding.contents.setData(data, idx)
            binding.contents.setSelected {picture, v ->
                selectedHandler?.let { it(picture, v) }
            }
        }
    }

}