package com.raftgroup.pupping.scene.page.walk.component

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
import com.lib.page.*
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpRedeemInfoBinding
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.dialog.Alert
import com.skeleton.component.dialog.Select

import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RedeemInfo : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private val appTag = javaClass.simpleName

    private lateinit var binding: CpRedeemInfoBinding
    private var completionHandler:((Bitmap) -> Unit)? = null
    override fun init(context: Context) {
        binding = CpRedeemInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    private val requestCode = UUID.randomUUID().hashCode()
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.btnPicture.setOnClickListener {
            Alert.Builder(pagePresenter.activity)
                .setText(R.string.alertCompletedNeedPicture)
                .onSelected {
                    openPicture()
                }
                .show()
        }
    }

    private  fun openPicture(){
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
                        AppUtil.openIntentImagePick(pagePresenter.activity, id=requestCode )
                    }
                    1 -> {
                        pagePresenter.requestPermission(
                            arrayOf(Manifest.permission.CAMERA),
                            ( object : PageRequestPermission {
                                override fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){
                                    if(!resultAll) return
                                    AppUtil.openIntentImagePick(pagePresenter.activity, true, id=requestCode)
                                }
                            })
                        )
                    }
                    else -> {}
                }
            }
            .show()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun setup(title:String, text:String, point: Double){
        binding.textTitle.text = title
        binding.textDescription.text = text
        binding.textPoint.text = point.toDecimal()
    }
    fun onClose(l: OnClickListener){
        binding.btnClose.setOnClickListener{v->
            Alert.Builder(pagePresenter.activity)
                .setSelectButtons()
                .setText(R.string.alertCompletedExitConfirm)
                .onSelected { sel->
                    if (sel == 0){l.onClick(v)}
                }
                .show()
        }
    }
    fun onPictureCompleted(completionHandler:(Bitmap) -> Unit){
        this.completionHandler = completionHandler
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if ( requestCode != this.requestCode ){ return }
        if (resultCode != Activity.RESULT_OK) return
        onResultData(data)
    }

    private fun onResultData(data: Intent){
        ComponentLog.d(data, appTag)
        val imageBitmap = data.extras?.get("data") as? Bitmap
        imageBitmap?.let{resource->
            ComponentLog.d(imageBitmap, appTag)
            completionHandler?.let{ it(resource) }
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
                            completionHandler?.let{ it(resource) }
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