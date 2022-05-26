package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetProfileDescriptionBinding
import com.raftgroup.pupping.databinding.CpProfileImageBinding
import com.raftgroup.pupping.databinding.CpSpeechBubbleButtonBinding
import com.raftgroup.pupping.store.api.rest.PetData
import com.raftgroup.pupping.store.provider.model.Gender
import com.raftgroup.pupping.store.provider.model.PetProfile


class ProfileImage : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpProfileImageBinding
    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {
           super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpProfileImageBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setData(path:String?,
                @DimenRes radius:Int? = null,
                @ColorRes strokeColor:Int? = null,
                @DimenRes strokeWidth:Int? = null){
        radius?.let {
            this.radius = context.resources.getDimension(it)
        }
        strokeWidth?.let {
            this.defaultStroke = context.resources.getDimension(it)
        }
        strokeColor?.let {
            this.defaultStrokeColor = context.getColor(it)
        }

        Glide.with(context)
            .load(path)
            .error(defaultImage ?:  context.getDrawable(R.drawable.img_empty_dog_profile))
            .into(binding.img)

        selected = false
    }

    override var selected:Boolean? = null
        set(value) {
            field = value
            val context = context ?: return
            setBgColor(field)
            setOutline(field)
        }

}
