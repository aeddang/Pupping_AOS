package com.raftgroup.pupping.scene.page.my
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.page.*
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.component.list.PictureList
import com.raftgroup.pupping.scene.page.popup.PagePicture
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.*
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.api.rest.AlbumData
import com.raftgroup.pupping.store.api.rest.PictureData
import com.raftgroup.pupping.store.api.rest.WeatherData
import com.raftgroup.pupping.store.provider.DataProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


@AndroidEntryPoint
class PageMy : PageFragment(), PageRequestPermission{
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider

    private lateinit var binding: PageMyBinding
    private var userId:String = ""
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.myProfile, binding.petList, binding.pictureList)
        }

    override fun onViewBinding(): View {
        binding = PageMyBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTab.setup(R.string.titleMy)
        binding.myProfile.setData(dataProvider.user.currentProfile)

    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        dataProvider.user.snsUser?.snsID?.let { userId ->
            this.userId = userId
        }
        binding.petList.setup(userId=this.userId){
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.ProfileRegist)
                    .addParam(PageParam.type, PageProfileRegist.ProfileType.Pet)
            )
        }
        binding.pictureList.setup{
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.PictureList)
                    .addParam(PageParam.requestCode, this.requestCode)
                    .addParam(PageParam.type, AlbumCategory.User)
                    .addParam(PageParam.id, this.userId)

            )
        }
        binding.pictureList.setSelected { data, v->
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.Picture)
                    .addParam(PageParam.type, AlbumCategory.User)
                    .addParam(PageParam.id, this.userId)
                    .addParam(PageParam.data, data)
                    .addParam(PageParam.datas,binding.pictureList.getDatas().filter { !it.isEmpty }),
                v, PagePicture.transitionName
            )
        }

        dataProvider.user.pets.observe(this){
            PageLog.d(it, appTag)
            binding.petList.setDatas(it)
        }
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                DataLog.d(userId, appTag+"userId")
                if(res.contentID == userId){
                    when(res.type){
                        ApiType.RegistAlbumPicture -> {
                            (res.data as? PictureData)?.let { data ->
                                binding.pictureList.addData(Picture().setData(data), 1)
                            }
                        }
                        ApiType.GetAlbumPictures -> {
                            if(res.id != appTag) return@observe
                            (res.data as? List<PictureData>)?.let{ datas->
                                val empty =  Picture().setEmptyData(requestCode)
                                val pictures = datas.map { Picture().setData(it) }.toMutableList()
                                pictures.add(0, empty)
                                binding.pictureList.setDatas(pictures)
                            }
                        }
                        else -> {}
                    }
                } else {
                    when(res.type){
                        ApiType.DeleteAlbumPictures -> {
                            binding.pictureList.deleteData(res.contentID.toInt())
                        }
                        ApiType.UpdateAlbumPictures -> {
                            (res.requestData as? Boolean)?.let { isFavorite ->
                                binding.pictureList.updateData(res.contentID.toInt(), isFavorite)
                            }
                        }
                        else -> {}
                    }
                }


            }
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        binding.myProfile.setMyProfile()
        dataProvider.user.pets.value?.let {
            binding.petList.setDatas(it)
        }
        dataProvider.user.snsUser?.snsID?.let { userId ->
            val query = HashMap<String,String>()
            query[ApiField.pictureType] = AlbumCategory.User.getApiCode()
            dataProvider.requestData(ApiQ(appTag, ApiType.GetAlbumPictures, contentID = userId, query = query))
        }
        repository.updateMyData(true)
    }

    private val requestCode = UUID.randomUUID().hashCode()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if ( this.requestCode != requestCode ){ return }
        if (resultCode != Activity.RESULT_OK) return
        onResultData(data)
    }

    private fun onResultData(data: Intent){
        ComponentLog.d(data, appTag)
        val imageBitmap = data.extras?.get("data") as? Bitmap
        imageBitmap?.let{
            ComponentLog.d(imageBitmap, appTag)
            registImage(imageBitmap)
            return
        }
        data.data?.let { galleryImgUri ->
            try {
                val path = galleryImgUri.getAbsuratePathFromUri(ctx)
                Glide.with(this)
                    .asBitmap()
                    .load(path)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            //profileData?.update(resource)
                            registImage(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onLoadFailed(errorDrawable: Drawable?) {}
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun registImage(resource: Bitmap){
        val data = AlbumData(
            AlbumCategory.User,
            resource.copy(resource.config, false).size(PictureList.pictureWidth, PictureList.pictureHeight),
            resource
        )
        dataProvider.user.snsUser?.snsID?.let { userId->
            dataProvider.requestData(
                ApiQ(
                    appTag, ApiType.RegistAlbumPicture, contentID = userId,
                    requestData = data )
            )
        }
    }
}