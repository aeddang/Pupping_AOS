package com.raftgroup.pupping.scene.component.button
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpSortButtonBinding
import com.raftgroup.pupping.databinding.UiImageBoxButtonBinding



open class SortButton : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpSortButtonBinding
    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {

            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_defaultTextColor)) {
                this.defaultTextColor = context.getColor(R.color.app_grey)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_activeTextColor)) {
                this.activeTextColor =  context.getColor(R.color.brand_primary)
            }
            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpSortButtonBinding.inflate(LayoutInflater.from(context), this, true)
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
            binding.text.text = field
        }

    fun setup(@StringRes stringRes:Int, @DrawableRes iconRes:Int, @ColorRes colorRes:Int ){
         defaultImageRes = iconRes
         activeTextColor = context.getColor(colorRes)
         text = context.resources.getString(stringRes)
         selected = false
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
            val drawable:Drawable? = if (defaultImageRes != null) AppCompatResources.getDrawable(context,defaultImageRes!!) else defaultImage
            val drawableAc:Drawable? = if (activeImageRes != null) AppCompatResources.getDrawable(context,activeImageRes!!) else activeImage
            if ( drawable!=null || drawableAc!=null ) {
                if (field == false) {
                    binding.imageIcon.setImageDrawable(drawable ?: drawableAc!!)
                } else {
                    binding.imageIcon.setImageDrawable(drawableAc ?: drawable!!)
                }
            }

            dfColor?.let { color ->
                if ( field == true) {
                    binding.imageIcon.setColorFilter(acColor ?: color)
                    binding.imageOpen.setColorFilter(acColor ?: color)
                }
                else {
                    binding.imageIcon.setColorFilter(color)
                    binding.imageOpen.setColorFilter(color)
                }
            }
            binding.body.selected = field

        }
}