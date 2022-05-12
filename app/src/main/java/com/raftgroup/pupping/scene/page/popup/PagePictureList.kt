package com.raftgroup.pupping.scene.page.popup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.*
import com.lib.view.adapter.BaseAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PagePictureListBinding
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.api.rest.PictureData
import com.raftgroup.pupping.store.provider.DataProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PagePictureList : PageFragment() {

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
    private lateinit var binding: PagePictureListBinding
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.pictureGrid)
        }

    override fun onViewBinding(): View {
        binding = PagePictureListBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTab.setup(R.string.titleAlbum, isBack = true)
        binding.pictureGrid.setup( object : BaseAdapter.Delegate {
            override fun onBottom(page: Int, size: Int) {
                super.onBottom(page, size)
                currentPage = page
                load()
            }

            override fun onReflash() {
                super.onReflash()
                reload()
            }
        })
        binding.pictureGrid.setSelected { data, v->
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.Picture)
                    .addParam(PageParam.type, type)
                    .addParam(PageParam.id, this.userId)
                    .addParam(PageParam.subId, this.petId)
                    .addParam(PageParam.data, data)
                    .addParam(PageParam.datas,pictureDatas.filter { !it.isEmpty }),
                v, PagePicture.transitionName
            )
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return

        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")

                if(res.contentID == contentId) {
                    when(res.type){
                        ApiType.RegistAlbumPicture -> {
                            (res.data as? PictureData)?.let { data ->
                                binding.pictureGrid.addData(Picture().setData(data),  if (isMine) 1 else 0)
                            }
                        }
                        ApiType.GetAlbumPictures -> {
                            (res.data as? List<PictureData>)?.let{ datas->
                                loaded(datas)
                            }
                        }
                        else -> {}
                    }
                } else {
                    when(res.type){
                        ApiType.DeleteAlbumPictures -> {
                            binding.pictureGrid.deleteData(res.contentID.toInt())
                        }
                        ApiType.UpdateAlbumPictures -> {
                            (res.requestData as? Boolean)?.let { isFavorite ->
                                binding.pictureGrid.updateData(res.contentID.toInt(), isFavorite)
                            }
                        }
                        else -> {}
                    }
                }

            }
        }
    }
    private var userId:String = ""
    private var petId:Int? = null
    private var contentId:String = ""
    private var requestCode:Int? = null
    private var currentPage:Int = 0
    private var type:AlbumCategory = AlbumCategory.User
    private val pictureDatas:MutableList<Picture> = arrayListOf()
    private var isMine:Boolean = false
    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.requestCode] as? Int)?.let { code->
            this.requestCode = code
            this.isMine = true
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
        this.contentId = if (type == AlbumCategory.User) userId  else (petId ?: 0).toString()

        /*
        (params[PageParam.datas] as? List<PictureData>)?.let { datas->
            pictureDatas.addAll(datas.map { Picture().setData(it) })
        }*/
        return super.onPageParams(params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        reload()
    }


    private fun reload(){
        pictureDatas.clear()
        binding.pictureGrid.resetDatas()
        currentPage = 0
        load()
    }
    private fun load(){
        val query = HashMap<String,String>()
        query[ApiField.pictureType] = type.getApiCode()
        query[ApiField.page] = currentPage.toString()
        //query[ApiField.size] = ApiValue.PAGE_SIZE.toString()
        dataProvider.requestData(ApiQ(appTag, ApiType.GetAlbumPictures, contentID = contentId, query = query))
    }
    private fun loaded(datas:List<PictureData>){
        if (pictureDatas.isEmpty()){
            DataLog.d("pictureDatas isEmpty", appTag)
            this.requestCode?.let {
                val empty =  Picture().setEmptyData(requestCode)
                pictureDatas.add(empty)
                binding.pictureGrid.addData(empty)
            }
        }
        val added = datas.map { Picture().setData(it) }
        pictureDatas.addAll(added)
        DataLog.d("added ${added.count()}", appTag)
        binding.pictureGrid.addDatas(added)
    }
}