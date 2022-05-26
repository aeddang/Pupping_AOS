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
import com.raftgroup.pupping.databinding.CpUserProfileDescriptionBinding
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
class UserProfileDescription : PageComponent, ListItem<UserProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpUserProfileDescriptionBinding

    private var profileData:UserProfile? = null
    override fun init(context: Context) {
        binding = CpUserProfileDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
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

    fun setMyProfile(){

        binding.btnRegistNickName.visibility = View.VISIBLE

        binding.btnRegistNickName.setOnClickListener {
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.ProfileRegist)
                    .addParam(PageParam.type, PageProfileRegist.ProfileType.User)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setData(data: UserProfile, idx:Int){
        PageLog.d(data.type.value ?: "type null", appTag)
        PageLog.d(data.email.value ?: "email null", appTag)
        profileData = data
        lifecycleOwner?.let {owner ->

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

    private fun updateName(name: String){
        dataProvider.requestData(ApiQ(
            appTag, ApiType.UpdateUser, requestData = ModifyUserProfileData(nickName = name)
        ))
    }


}