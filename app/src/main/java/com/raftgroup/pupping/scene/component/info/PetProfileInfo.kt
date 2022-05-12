package com.raftgroup.pupping.scene.component.info

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageRequestPermission
import com.lib.util.*
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetProfileInfoBinding
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.dialog.Select
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class PetProfileInfo : PageComponent, ListItem<PetProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPetProfileInfoBinding
    private var isMypet:Boolean = false
    private var profileData:PetProfile? = null
    override fun init(context: Context) {
        binding = CpPetProfileInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleOwner?.let {owner ->
            profileData?.image?.observe(owner){
                profileData?.let { setImage(it) }
            }
            profileData?.lv?.observe(owner){
                profileData?.let { setLv(it)}
            }
            profileData?.species?.observe(owner){
                profileData?.let { setSpecies(it)}
            }
        }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner?.let {
            profileData?.removeObservers(it)
        }
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)

        binding.btnAddProfile.setOnClickListener {
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
                            AppUtil.openIntentImagePick(pagePresenter.activity, id=profileData?.petId )
                        }
                        1 -> {
                            pagePresenter.requestPermission(
                                arrayOf(Manifest.permission.CAMERA),
                                ( object : PageRequestPermission {
                                    override fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){
                                        if(!resultAll) return
                                        AppUtil.openIntentImagePick(pagePresenter.activity, true, id=profileData?.petId)
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
        val currentData = profileData ?: return
        data ?: return
        if ( requestCode != currentData.petId ){ return }
        if (resultCode != Activity.RESULT_OK) return
        onResultData(data)
    }

    private fun onResultData(data: Intent){
        ComponentLog.d(data, appTag)
        val imageBitmap = data.extras?.get("data") as? Bitmap
        imageBitmap?.let{
            ComponentLog.d(imageBitmap, appTag)
            //profileData?.update(it)
            updateImage(imageBitmap)
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
                            //profileData?.update(resource)
                            updateImage(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onLoadFailed(errorDrawable: Drawable?) {}
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setData(data: PetProfile, idx:Int){
        profileData = data
        isMypet = data.isMypet
        binding.textName.text = data.nickName.value
        setLv(data)
        setImage(data)
        setDescription(data)
        if (isMypet){
            binding.btnAddProfile.visibility = View.VISIBLE
        }
    }

    private  fun setLv(data: PetProfile){
        data.lv.value?.toString()?.let{
            binding.textLv.text = "lv$it"
        }
    }

    private  fun setSpecies(data: PetProfile){
        binding.description.setSpecies(data.species.value)
    }

    private fun setImage(data: PetProfile){
        if (data.image.value != null) {
            binding.imgProfile.setImageBitmap(data.image.value)
        } else if (data.imagePath != null ) {
            Glide.with(context)
                .load(data.imagePath)
                .error(R.drawable.img_empty_dog_profile)
                .into(binding.imgProfile)
        }

    }
    private fun setDescription(data: PetProfile){
        data.birth.value?.year?.let { birthYY ->
            val now = LocalDate.now()
            val yy = now.year
            val age = (yy - birthYY + 1).toString() + "yrs"
            data.gender.value?.let {
                binding.description.setData(it, age, data.species.value)
            }

        }
    }
    private fun updateImage(resource: Bitmap){

        val crop = Size(resource.width, resource.height).getCropRatioSize(Size(240,240))
        val cropImg = resource.centerCrop(crop)
        val resizeImg = cropImg.size(240,240)
        resource.recycle()
        cropImg.recycle()

        dataProvider.requestData(ApiQ(
            appTag, ApiType.UpdatePetImage, contentID = profileData?.petId.toString(),
            requestData = resizeImg
        ))
    }

}