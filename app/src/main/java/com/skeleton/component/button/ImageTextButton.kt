package com.skeleton.component.button

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.UiImageTextButtonBinding


open class ImageTextButton : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: UiImageTextButtonBinding
    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {

            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_defaultTextColor)) {
                this.defaultTextColor = context.getColor(R.color.app_greyLight)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_activeTextColor)) {
                this.activeTextColor =  context.getColor(R.color.brand_primary)
            }
            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = UiImageTextButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    @DrawableRes
    var defaultImageRes:Int? = null
    @DrawableRes
    var activeImageRes:Int? = null

    var text:String? = null
        set(value) {
            field = value
            if (field == null) binding.text.visibility = View.GONE
            else {
                binding.text.visibility = View.VISIBLE
                binding.text.text = field
            }
        }

    override var selected:Boolean? = null
        set(value) {
            field = value
            val context = context ?: return
            var dfColor:Int? = null
            var acColor:Int? = null
            defaultTextColor?.let { color ->
                dfColor = color
                activeTextColor?.let {  acColor = it }
            }
            dfColor?.let { color ->
                if ( field == false) binding.text.setTextColor( color  )
                else binding.text.setTextColor( acColor ?: color )
            }

            val drawable:Drawable? = if (defaultImageRes != null) AppCompatResources.getDrawable(context,defaultImageRes!!) else defaultImage
            val drawableAc:Drawable? = if (activeImageRes != null) AppCompatResources.getDrawable(context,activeImageRes!!) else activeImage
            if ( drawable==null && drawableAc==null ) return
            if ( field == false) {
                binding.imageIcon.setImageDrawable(drawable ?: drawableAc!!)
            }
            else {
                binding.imageIcon.setImageDrawable(drawableAc ?: drawable!!)
            }

            dfColor?.let { color ->
                if ( field == false) {
                    binding.imageIcon.setColorFilter(color)
                }
                else {
                    binding.imageIcon.setColorFilter(acColor ?: color)
                }
            }
        }
}