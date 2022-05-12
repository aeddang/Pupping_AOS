package com.raftgroup.pupping.scene.page.popup

import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageUI
import com.lib.page.PageView
import com.lib.util.DataLog
import com.lib.util.PageLog
import com.lib.util.animateAlpha
import com.lib.util.toThousandUnit
import com.lib.view.adapter.BaseViewPagerAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PagePictureBinding
import com.raftgroup.pupping.databinding.UiImageBinding
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.dialog.Alert
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PagePicture  : PageFragment() {
    companion object{
        const val transitionName: String = "PagePictureTransitionName"
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

    private lateinit var binding: PagePictureBinding
    private val adapter = PagerAdapter()

    override fun onViewBinding(): View {
        binding = PagePictureBinding.inflate(LayoutInflater.from(context))
        ViewCompat.setTransitionName(binding.viewPager, PagePicture.transitionName)
        return binding.root
    }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
        pagePresenter.isFullScreen = false
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        pagePresenter.isFullScreen = true
        uiHidden()
    }

    private var userId:String = ""
    private var petId:Int? = null
    private var type:AlbumCategory = AlbumCategory.User
    private var pictureDatas:MutableList<Picture> = arrayListOf()
    private var currentPicture:Picture? = null
        set (newData) {
            field?.removeObservers(this)
            newData?.likeValue?.observe(this){
                setLikeValue(newData)
            }
            newData?.isLike?.observe(this){
                setLike(newData)
            }
            newData?.let {
                setLike(it)
                setLikeValue(it)
            }
            field = newData
        }

    private var isMine:Boolean = false
    private var initIdx:Int = 0
    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.datas] as? List<Picture>)?.let { datas->
            this.pictureDatas = datas.map { it.copy() }.toMutableList()
        }
        (params[PageParam.type] as? AlbumCategory)?.let { type->
            this.type = type
        }
        (params[PageParam.id] as? String)?.let { id->
            this.userId = id

        }
        (params[PageParam.subId] as? Int)?.let { id->
            this.petId = id
        }
        (params[PageParam.idx] as? Int)?.let { idx->
            this.initIdx = idx
        }
        (params[PageParam.data] as? Picture)?.let { data->
            this.pictureDatas.find { it.pictureId == data.pictureId }?.let { find ->
                val idx = this.pictureDatas.indexOf(find)
                if (idx != -1){
                    this.initIdx = idx
                }
            }
        }
        return super.onPageParams(params)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataProvider.user.snsUser?.snsID?.let{
            this.isMine = it == userId
        }
        binding.viewPager.adapter = adapter
        binding.btnBack.setOnClickListener {
            pagePresenter.closePopup(pageObject?.key)
        }
        binding.favoriteBtn.setOnClickListener {
            val data = this.currentPicture ?: return@setOnClickListener
            val isFavorite = data.isLike.value != true
            dataProvider.requestData(
                ApiQ(appTag, ApiType.UpdateAlbumPictures,
                    contentID = data.pictureId.toString(),
                    requestData = isFavorite
                )
            )
        }
        if (this.isMine){
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                val data = this.currentPicture ?: return@setOnClickListener
                Alert.Builder(pagePresenter.activity)
                    .setSelectButtons()
                    .setText(R.string.alertDeleteProfile)
                    .onSelected {
                        if (it == 0){
                            dataProvider.requestData(
                                ApiQ(appTag, ApiType.DeleteAlbumPictures,
                                    contentID = data.pictureId.toString()
                                )
                            )
                        }
                    }
                    .show()
            }
        } else {
            binding.btnDelete.visibility = View.GONE
        }

        binding.viewPager.addOnPageChangeListener( object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                currentPicture = pictureDatas[position]
            }
            override fun onPageSelected(position: Int) {

            }
            override fun onPageScrollStateChanged(state: Int) {}
        })


    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        adapter.setData(pictureDatas.toTypedArray())
        binding.viewPager.currentItem = initIdx
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                DataLog.d(userId, appTag+"userId")
                val pictureId = res.contentID.toInt()
                if(res.contentID != pictureId.toString()) return@observe
                when(res.type){
                    ApiType.DeleteAlbumPictures -> {
                        pictureDatas.find { d ->
                            (d as? Picture)?.let {
                                return@find it.pictureId == pictureId
                            }
                            return@find false
                        }?.let { find ->
                            //adapter.resetData()
                            DataLog.d("pictureDatas before ${ pictureDatas.count() }", appTag)
                            pictureDatas.remove(find)
                            if (binding.viewPager.currentItem >= pictureDatas.count()){
                                binding.viewPager.currentItem = pictureDatas.count()-1
                            }
                            DataLog.d("pictureDatas after ${ pictureDatas.count() }", appTag)

                            adapter.setData(pictureDatas.toTypedArray())
                            binding.viewPager.currentItem = 0
                        }
                    }
                    ApiType.UpdateAlbumPictures -> {
                        (res.requestData as? Boolean)?.let { isFavorite ->
                            pictureDatas.find { d ->
                                (d as? Picture)?.let {
                                    return@find it.pictureId == pictureId
                                }
                                return@find false
                            }?.let { find ->
                               find.updata(isFavorite)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setLike(data: Picture){
        data.isLike.value?.let{
            binding.favoriteBtn.selected = it
        }
    }

    private fun setLikeValue(data: Picture){
        data.likeValue.value?.let{
            binding.favoriteBtn.text = "${it.toThousandUnit()}"
        }
    }

    private var isUiView:Boolean = true
    private fun toggleUiView(){
        if( isUiView ) uiHidden() else uiView()
    }
    private fun uiView(){
        isUiView = true
        binding.uiBox.animateAlpha(1f, false)
    }
    private fun uiHidden(){
        isUiView = false
        binding.uiBox.animateAlpha(0f, false)
    }

    inner class PagerAdapter : BaseViewPagerAdapter<Item, Picture>(){
        override fun getPageView(container: ViewGroup, position: Int): Item {
            val item = Item(context ?: ctx)
            item.setData(pictureDatas[position])
            return item
        }
    }

    inner class Item(context: Context) : PageUI(context) {
        private lateinit var binding: UiImageBinding
        private var data:Picture? = null
        private var hasImage:Boolean = false
        override fun onBinding() {
            binding = UiImageBinding.inflate(LayoutInflater.from(this.context), this, true)

        }
        fun setData(data: Picture?){
            this.data = data
            if(!hasImage) return
            Glide.with(context).clear(binding.img)
            setImage()
        }
        private fun setImage(){
            val picture = data ?: return
            hasImage = true
            if (picture.image.value != null) {
                binding.img.setImageBitmap(picture.image.value)
            } else if (picture.imagePath != null ) {
                Glide.with(context)
                    .load(picture.imagePath)
                    .error(R.drawable.img_empty_dog_profile)
                    .into(binding.img)
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            Glide.with(context).clear(binding.img)
        }
        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            setImage()
            this.setOnClickListener {
                toggleUiView()
            }
        }
    }
}