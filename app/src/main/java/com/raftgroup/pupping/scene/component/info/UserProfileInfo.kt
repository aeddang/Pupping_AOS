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
import com.raftgroup.pupping.databinding.CpUserProfileInfoBinding
import com.raftgroup.pupping.scene.page.my.PageProfileRegist
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.ModifyUserProfileData
import com.raftgroup.pupping.store.provider.model.UserProfile
import com.skeleton.component.dialog.Select
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class UserProfileInfo : PageComponent, ListItem<UserProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpUserProfileInfoBinding

    private var profileData:UserProfile? = null
    override fun init(context: Context) {
        binding = CpUserProfileInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.btnAddProfile.visibility = View.GONE
        binding.btnRegistNickName.visibility = View.GONE

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner?.let {
            profileData?.removeObservers(it)
        }
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentData = profileData ?: return
        data ?: return
        if ( requestCode != currentData.hashId ){ return }
        if (resultCode != Activity.RESULT_OK) return
        onResultData(data)
    }

    private fun onResultData(data: Intent){
        ComponentLog.d(data, appTag)
        val imageBitmap = data.extras?.get("data") as? Bitmap
        imageBitmap?.let{
            ComponentLog.d(imageBitmap, appTag)
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

    fun setMyProfile(){
        binding.btnAddProfile.visibility = View.VISIBLE
        binding.btnRegistNickName.visibility = View.VISIBLE
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
                            AppUtil.openIntentImagePick(pagePresenter.activity, id=profileData?.hashId)
                        }
                        1 -> {
                            pagePresenter.requestPermission(
                                arrayOf(Manifest.permission.CAMERA),
                                ( object : PageRequestPermission {
                                    override fun onRequestPermissionResult(resultAll:Boolean ,  permissions: List<Boolean>?){
                                        if(!resultAll) return
                                        AppUtil.openIntentImagePick(pagePresenter.activity, true, id=profileData?.hashId)
                                    }
                                })
                            )
                        }
                        else -> {}
                    }
                }
                .show()
        }
        binding.btnRegistNickName.setOnClickListener {
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.ProfileRegist)
                    .addParam(PageParam.type, PageProfileRegist.ProfileType.User)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setData(data: UserProfile, idx:Int){
        PageLog.d(data.imagePath ?: "image null", appTag)
        PageLog.d(data.type.value ?: "type null", appTag)
        PageLog.d(data.email.value ?: "email null", appTag)
        profileData = data
        lifecycleOwner?.let {owner ->
            data.image.observe(owner){
                setImage(data)
            }
            data.nickName.observe(owner){
                setNickName(data)
            }
            data.type.observe(owner){
                setSnsType(data)
            }
            data.email.observe(owner){
                setEmail(data)
            }
        }
        setNickName(data)
        setImage(data)
        setSnsType(data)
        setEmail(data)
    }
    private fun setNickName(data: UserProfile){
        binding.textName.text = data.nickName.value
    }

    private fun setSnsType(data: UserProfile){
        data.type.value?.logo()?.let {
            binding.imgLogo.setImageResource(it)
        }
    }

    private fun setEmail(data: UserProfile){
        binding.textEmail.text = data.email.value
    }

    private fun setImage(data: UserProfile){
        if (data.image.value != null) {
            binding.imgProfile.setImageBitmap(data.image.value)
        } else if (data.imagePath != null ) {
            Glide.with(context)
                .load(data.imagePath)
                .error(R.drawable.img_empty_user_profile)
                .into(binding.imgProfile)
        }

    }
    private fun updateName(name: String){
        dataProvider.requestData(ApiQ(
            appTag, ApiType.UpdateUser, requestData = ModifyUserProfileData(nickName = name)
        ))
    }
    private fun updateImage(resource: Bitmap){
        val crop = Size(resource.width, resource.height).getCropRatioSize(Size(240,240))
        val cropImg = resource.centerCrop(crop)
        val resizeImg = cropImg.size(240,240)
        resource.recycle()
        cropImg.recycle()

        dataProvider.requestData(ApiQ(
            appTag, ApiType.UpdateUser, requestData = ModifyUserProfileData(image = resizeImg)
        ))
    }

}