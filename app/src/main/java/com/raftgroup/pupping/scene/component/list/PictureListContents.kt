package com.raftgroup.pupping.scene.component.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.lib.util.*
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPictureListContentsBinding
import com.raftgroup.pupping.scene.page.popup.PagePicture
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.api.rest.AlbumData
import com.raftgroup.pupping.store.api.rest.PictureData
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.dialog.Select
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PictureListContents : PageComponent, ListItem<Picture> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    companion object {
        const val width:Int = 162
        const val height:Int = 198
    }
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPictureListContentsBinding
    private var pictureData: Picture? = null

    override fun init(context: Context) {
        binding = CpPictureListContentsBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner?.let {
            pictureData?.removeObservers(it)
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        binding.favoriteBtn.setOnClickListener {
            val data = this.pictureData ?: return@setOnClickListener
            val isFavorite = data.isLike.value != true
            dataProvider.requestData(
                ApiQ(appTag, ApiType.UpdateAlbumPictures,
                    contentID = data.pictureId.toString(),
                    requestData = isFavorite
                ))
        }
    }
    fun setSelected(completionHandler:(Picture, View) -> Unit) {
        binding.btn.setOnClickListener {
            val data = this.pictureData ?: return@setOnClickListener
            if ( data.isEmpty ) addPicture()
            else {
                completionHandler(data, binding.img)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    override fun setData(data: Picture, idx:Int){
        pictureData = data
        if (data.isEmpty){
            binding.img.setImageResource(R.drawable.img_picture_empty)
            binding.strokeBorder.visibility = View.VISIBLE
            binding.favoriteBtn.visibility = View.GONE
            return
        }
        binding.strokeBorder.visibility = View.GONE
        binding.favoriteBtn.visibility = View.VISIBLE
        setLike(data)
        setImage(data)
        setLikeValue(data)
        lifecycleOwner?.let {owner ->
            pictureData?.image?.observe(owner){
                pictureData?.let { setImage(it) }
            }
            pictureData?.isLike?.observe(owner){
                pictureData?.let { setLike(it)}
            }
            pictureData?.likeValue?.observe(owner){
                pictureData?.let { setLikeValue(it)}
            }
        }
    }

    private  fun setLike(data: Picture){
        data.isLike.value?.let{
            binding.favoriteBtn.selected = it
        }
    }

    private  fun setLikeValue(data: Picture){
        data.likeValue.value?.let{
            binding.favoriteBtn.text = "${it.toThousandUnit()}"
        }
    }

    private fun setImage(data: Picture){
        if (data.image.value != null) {
            binding.img.setImageBitmap(data.image.value)
        } else if (data.imagePath != null ) {
            Glide.with(context)
                .load(data.imagePath)
                .error(R.drawable.img_empty_dog_profile)
                .into(binding.img)
        }
    }

    private fun addPicture(){
        Select.Builder(context)
            .setResButtons(arrayOf(
                R.string.btnAlbum,
                R.string.btnCamera,
                R.string.cancel
            ))
            .setSelected(2)
            .onSelected { selectIdx->
                when(selectIdx) {
                    0 -> {
                        AppUtil.openIntentImagePick(pagePresenter.activity, id=pictureData?.requestCode)
                    }
                    1 -> {
                        pagePresenter.requestPermission(
                            arrayOf(Manifest.permission.CAMERA),
                            ( object : PageRequestPermission {
                                override fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){
                                    if(!resultAll) return
                                    AppUtil.openIntentImagePick(pagePresenter.activity, true, id=pictureData?.requestCode)
                                }
                            })
                        )
                    }
                    else -> {}
                }
            }
            .show()
    }
}