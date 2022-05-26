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
import com.raftgroup.pupping.databinding.PageUserBinding
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.component.list.PictureList
import com.raftgroup.pupping.scene.page.my.PageProfileRegist
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.*
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.raftgroup.pupping.store.provider.model.User
import com.raftgroup.pupping.store.provider.model.UserProfile
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class PageUser : PageFragment() {

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
    private lateinit var binding: PageUserBinding

    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.imgProfile, binding.profile, binding.pictureList)
        }

    override fun onViewBinding(): View {
        binding = PageUserBinding.inflate(LayoutInflater.from(context))
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
                if(res.contentID == userId) {
                    when(res.type){

                        ApiType.GetAlbumPictures -> {
                            if(res.id != appTag) return@observe
                            (res.data as? List<PictureData>)?.let{ datas->
                                if (datas.isEmpty()) return@observe
                                binding.pictureList.visibility = View.VISIBLE
                                val pictures = datas.map { Picture().setData(it) }.toMutableList()
                                binding.pictureList.setDatas(pictures)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    private var userId:String = ""
    private var profile:UserProfile? = null
    private var pets:List<PetProfile>? = null
    override fun onPageParams(params: Map<String, Any?>): PageView {

        (params[PageParam.data] as? User)?.let { data->
            this.profile = data.currentProfile
            this.userId = data.snsUser?.snsID ?: ""
            this.pets = data.pets.value
        }
        return super.onPageParams(params)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.profile?.let { data->
            binding.imgProfile.setData(data)
            binding.profile.setData(data)
        }
        this.pets?.let {
            binding.petList.setDatas(it)
        }

        binding.pictureList.setup{
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.PictureList)
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
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        load()
    }

    private fun load(){
        val query = HashMap<String,String>()
        query[ApiField.pictureType] = AlbumCategory.User.getApiCode()
        query[ApiField.page] = "0"
        dataProvider.requestData(ApiQ(appTag, ApiType.GetAlbumPictures, contentID = userId, query = query))
    }

}