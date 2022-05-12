package com.raftgroup.pupping.scene.page.popup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.*
import com.lib.view.adapter.BaseAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PagePictureListBinding
import com.raftgroup.pupping.databinding.PageProfileBinding
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.component.list.PictureList
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.api.rest.AlbumData
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.api.rest.PictureData
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class PageProfile : PageFragment() {

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

    private lateinit var binding: PageProfileBinding


    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.imgProfile, binding.petProfile,
                binding.pictureList, binding.menuTab, binding.about, binding.history)
        }

    override fun onViewBinding(): View {
        binding = PageProfileBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }



    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return

        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                if(res.contentID == petId) {
                    when(res.type){
                        ApiType.DeletePet -> {
                            pagePresenter.closePopup(pageObject?.key)
                        }
                        ApiType.UpdatePet -> {
                            this.profile?.let { profile ->
                                binding.about.setData(profile)
                                binding.history.setData(profile)
                                setFin(profile)
                            }
                        }
                        ApiType.RegistAlbumPicture -> {
                            (res.data as? PictureData)?.let { data ->
                                binding.pictureList.addData(Picture().setData(data),  if (isMine) 1 else 0 )
                            }
                        }
                        ApiType.GetAlbumPictures -> {
                            if(res.id != appTag) return@observe
                            (res.data as? List<PictureData>)?.let{ datas->

                                val pictures = datas.map { Picture().setData(it) }.toMutableList()
                                if(isMine) {
                                    val empty = Picture().setEmptyData(requestCode)
                                    pictures.add(0, empty)
                                }
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
    private var userId:String = ""
    private var petId:String = ""
    private var profile:PetProfile? = null
    private var isMine:Boolean = false
    private val requestCode = UUID.randomUUID().hashCode()

    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.id] as? String)?.let { id->
            this.userId = id
        }
        (params[PageParam.data] as? PetProfile)?.let { data->
            this.profile = data
            this.petId = data.petId.toString()
        }
        return super.onPageParams(params)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataProvider.user.snsUser?.snsID?.let{
            this.isMine = it == userId
        }
        this.profile?.let { data->
            binding.imgProfile.setData(data)
            binding.petProfile.setData(data)
            binding.about.setData(data)
            binding.history.setData(data)
            binding.menuTab.setup(arrayListOf(R.string.profileAbout, R.string.profileHistory), 0){ idx->
                binding.menuTab.selectedIdx = idx
                setupMenu(idx)
            }
            setFin(data)
        }
        binding.history.setup { type ->
            if (type != null){
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.History)
                        .addParam(PageParam.type, type)
                        .addParam(PageParam.id, this.userId)
                        .addParam(PageParam.subId, this.petId.toInt())

                )
            } else {
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.Report)
                        .addParam(PageParam.data, profile)
                        .addParam(PageParam.id, this.userId)

                )
            }
        }

        binding.pictureList.setup{
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.PictureList)
                    .addParam(PageParam.requestCode, if(isMine) this.requestCode else null)
                    .addParam(PageParam.type, AlbumCategory.Pet)
                    .addParam(PageParam.id, this.userId)
                    .addParam(PageParam.subId, this.petId.toInt())

            )
        }
        binding.pictureList.setSelected { data, v->
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.Picture)
                    .addParam(PageParam.type, AlbumCategory.Pet)
                    .addParam(PageParam.id, this.userId)
                    .addParam(PageParam.subId, this.petId.toInt())
                    .addParam(PageParam.data, data)
                    .addParam(PageParam.datas,binding.pictureList.getDatas().filter { !it.isEmpty }),
                v, PagePicture.transitionName
            )
        }
        setupMenu()
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        load()
    }

    private fun load(){
        val query = HashMap<String,String>()
        query[ApiField.pictureType] = AlbumCategory.Pet.getApiCode()
        query[ApiField.page] = "0"
        dataProvider.requestData(ApiQ(appTag, ApiType.GetAlbumPictures, contentID = petId, query = query))
    }


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

    private  fun setFin(data: PetProfile){
        val fin = data.microfin.value
        if (fin == null){
            binding.finBox.visibility = View.GONE
        } else {
            binding.finBox.visibility = View.VISIBLE
            binding.textFin.text = fin
        }
    }

    private fun setupMenu(idx:Int = 0){
        when(idx){
            1-> {
                binding.pictureList.visibility = View.GONE
                binding.about.visibility = View.GONE
                binding.history.visibility = View.VISIBLE
            }
            else ->{
                binding.pictureList.visibility = View.VISIBLE
                binding.about.visibility = View.VISIBLE
                binding.history.visibility = View.GONE
            }
        }
    }

    private fun registImage(resource: Bitmap){
        val data = AlbumData(
            AlbumCategory.Pet,
            resource.copy(resource.config, false).size(PictureList.pictureWidth, PictureList.pictureHeight),
            resource
        )
        dataProvider.requestData(
            ApiQ(
                appTag, ApiType.RegistAlbumPicture, contentID = petId,
                requestData = data )
        )
    }
}