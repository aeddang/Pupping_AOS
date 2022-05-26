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
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetProfileImageBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PetProfileImage : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPetProfileImageBinding
    private var isMypet:Boolean = false
    private var profileData:PetProfile? = null
    override fun init(context: Context) {
        binding = CpPetProfileImageBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleOwner?.let {owner ->
            profileData?.image?.observe(owner){
                profileData?.let { setImage(it) }
            }
        }
        binding.btnBack.setOnClickListener {
            pagePresenter.goBack()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner?.let {
            profileData?.removeObservers(it)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setData(data: PetProfile){
        profileData = data
        isMypet = data.isMypet
        setImage(data)
        lifecycleOwner?.let {owner ->
            profileData?.image?.observe(owner){
                profileData?.let { setImage(it) }
            }
        }
        if (isMypet){
            binding.btnModify.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnModify.setOnClickListener {
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.ProfileModify)
                        .addParam(PageParam.data, data)
                )

            }
            binding.btnDelete.setOnClickListener {
                dataProvider.requestData(ApiQ(appTag, ApiType.DeletePet, contentID = data.petId.toString()))
            }
        } else {
            binding.btnModify.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
        }
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

}