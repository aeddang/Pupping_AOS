package com.raftgroup.pupping.scene.page.walk.component

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
import com.raftgroup.pupping.databinding.CpPlayProfileInfoBinding
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.dialog.Select
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class PlayProfileInfo : PageComponent, ListItem<PetProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPlayProfileInfoBinding
    private var isMypet:Boolean = false
    private var profileData:PetProfile? = null
    override fun init(context: Context) {
        binding = CpPlayProfileInfoBinding.inflate(LayoutInflater.from(context), this, true)
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
            profileData?.exp?.observe(owner){
                profileData?.let { setExp(it)}
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
    }


    @SuppressLint("SetTextI18n")
    override fun setData(data: PetProfile, idx:Int){
        profileData = data
        isMypet = data.isMypet
        binding.textName.text = data.nickName.value
        setLv(data)
        setImage(data)
        setExp(data)
    }

    private  fun setLv(data: PetProfile){
        data.lv.value?.toString()?.let{
            binding.textLv.text = "Lv.$it"
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
    private fun setExp(data: PetProfile){
        data.lv.value?.toString()?.let{
            binding.textExp.text = it
        }
    }
}