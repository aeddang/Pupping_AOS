package com.raftgroup.pupping.scene.page.walk

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.lib.page.*
import com.lib.util.*
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider

import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PageSelectProfile() : PageFragment(), PageRequestPermission{
    companion object{
        const val CONFIRM:String = "PageSelectProfileConfirm"
    }
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


    private lateinit var binding: PageSelectProfileBinding

    override fun onViewBinding(): View {
        binding = PageSelectProfileBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = context ?: return
        dataProvider.user.pets.value?.let{ pets->
            pets.forEachIndexed { idx, pet->
                val item = ProfileItem(ctx)
                binding.listBody.addView(
                    item,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                item.setData(pet)
                item.setOnClickListener {
                    val isWith = !pet.isWith
                    pet.isWith = isWith
                    item.select = isWith
                    checkConfirm()
                }
            }
            checkConfirm()
        }
        binding.btnConfirm.setOnClickListener {
            val pets = dataProvider.user.pets.value ?: return@setOnClickListener
            pets.find { it.isWith }?.let {
                pagePresenter.observable.event.value = PageEvent(PageEventType.Event, eventType = PageSelectProfile.CONFIRM )
                pagePresenter.closePopup(pageObject?.key)
                return@setOnClickListener
            }
        }
    }

    private fun checkConfirm(){
        val pets = dataProvider.user.pets.value ?: return
        val find = pets.find { it.isWith }
        binding.btnConfirm.selected = find != null
    }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
    }


    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
    }

    inner class ProfileItem : PageComponent, ListItem<PetProfile> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpSelectProfileItemBinding
        override fun init(context: Context) {
            binding = CpSelectProfileItemBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }

        override fun onLifecycleOwner(owner: LifecycleOwner) {
            super.onLifecycleOwner(owner)
        }

        override fun setOnClickListener(l: OnClickListener?) {
            super.setOnClickListener(l)
            binding.btn.setOnClickListener(l)
        }

        @SuppressLint("SetTextI18n")
        override fun setData(data: PetProfile, idx:Int){
            if (data.image.value != null) {
                binding.imgProfile.setImageBitmap(data.image.value)
            } else if (data.imagePath != null ) {
                Glide.with(context)
                    .load(data.imagePath)
                    .error(R.drawable.img_empty_dog_profile)
                    .into(binding.imgProfile)
            }
            data.lv.value?.toString()?.let{
                binding.textLv.text = "lv$it"
            }
            data.nickName.value?.toString()?.let{
                binding.textName.text = it
            }
            select = data.isWith
        }
        var select:Boolean = false
        set(value) {
            field = value
            binding.imgProfileLine.selected = value
        }

    }

}