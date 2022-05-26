package com.raftgroup.pupping.scene.component.info

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.bumptech.glide.Glide

import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpUserProfileImageBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.UserProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileImage : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpUserProfileImageBinding
    private var profileData:UserProfile? = null
    override fun init(context: Context) {
        binding = CpUserProfileImageBinding.inflate(LayoutInflater.from(context), this, true)
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
    fun setData(data: UserProfile){
        profileData = data
        setImage(data)
        lifecycleOwner?.let {owner ->
            profileData?.image?.observe(owner){
                profileData?.let { setImage(it) }
            }
        }
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

}