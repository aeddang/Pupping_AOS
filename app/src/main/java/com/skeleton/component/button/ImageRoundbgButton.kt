package com.skeleton.component.button

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.lib.page.PageUI
import com.raftgroup.pupping.databinding.UiImageRoundedButtonBinding

open class ImageRoundedButton : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: UiImageRoundedButtonBinding
    @SuppressLint("CustomViewStyleable", "Recycle")
    override fun onInit() {
        super.onInit()
        binding = UiImageRoundedButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    @DrawableRes
    var defaultImageRes:Int? = null
    @DrawableRes
    var activeImageRes:Int? = null


    override var selected:Boolean? = null
        set(value) {
            field = value
            val context = context ?: return
            setBgColor(field)
            setOutline(field)

            val drawable:Drawable? = if (defaultImageRes != null) AppCompatResources.getDrawable(context,defaultImageRes!!) else defaultImage
            val drawableAc:Drawable? = if (activeImageRes != null) AppCompatResources.getDrawable(context,activeImageRes!!) else activeImage
            if ( drawable==null && drawableAc==null ) return
            if ( field == false) {
                binding.imageIcon.setImageDrawable(drawable ?: drawableAc!!)
            }
            else {
                binding.imageIcon.setImageDrawable(drawableAc ?: drawable!!)
            }
        }
}