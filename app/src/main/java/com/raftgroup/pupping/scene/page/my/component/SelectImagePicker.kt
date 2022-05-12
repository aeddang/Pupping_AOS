package com.raftgroup.pupping.scene.page.my.component

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.lib.util.AppUtil
import com.lib.util.getAbsuratePathFromUri
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpSelectImagePickerBinding
import com.raftgroup.pupping.scene.page.my.model.InputData
import com.skeleton.component.dialog.Select
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectImagePicker: PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    private lateinit var binding: CpSelectImagePickerBinding
    private val requestId = UUID.randomUUID().hashCode()
    private var completionHandler:((Bitmap) -> Unit)? = null
    override fun init(context: Context) {
        binding = CpSelectImagePickerBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    fun setup(data: InputData, prefix:String = "", completionHandler:(Bitmap) -> Unit){
        this.completionHandler = completionHandler
        binding.textTitle.text = "$prefix${data.title}"
        data.selectedImage?.let {
            binding.imgProfile.setImageBitmap(it)
        }
        binding.imgProfileBox.setOnClickListener {
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
                            AppUtil.openIntentImagePick(pagePresenter.activity, id=requestId )
                        }
                        1 -> {
                            pagePresenter.requestPermission(
                                arrayOf(Manifest.permission.CAMERA),
                                ( object : PageRequestPermission {
                                    override fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){
                                        if(!resultAll) return
                                        AppUtil.openIntentImagePick(pagePresenter.activity, true, id=requestId)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if ( requestCode != requestId ){ return }
        if (resultCode != Activity.RESULT_OK) return
        onResultData(data)
    }

    private fun onResultData(data: Intent){

        val imageBitmap = data.extras?.get("data") as? Bitmap
        imageBitmap?.let{ resource->
            binding.imgProfile.setImageBitmap(resource)
            completionHandler?.let { it(resource) }
            return
        }
        data.data?.let { galleryImgUri ->
            try {
                val path = galleryImgUri.getAbsuratePathFromUri(context!!)
                Glide.with(this)
                    .asBitmap()
                    .load(path)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.imgProfile.setImageBitmap(resource)
                            completionHandler?.let { it(resource) }
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onLoadFailed(errorDrawable: Drawable?) {}
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}