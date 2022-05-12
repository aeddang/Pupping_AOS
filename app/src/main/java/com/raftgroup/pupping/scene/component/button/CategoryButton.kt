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
import com.raftgroup.pupping.databinding.CpCategoryButtonBinding
import com.raftgroup.pupping.databinding.UiImageBoxButtonBinding


open class CategoryButton : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpCategoryButtonBinding
    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {

            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.title = style.getString(R.styleable.PageUIStyle_android_title)
            }
            if (style.hasValue(R.styleable.PageUIStyle_android_text)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_text)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_defaultTextColor)) {
                this.defaultTextColor = context.getColor(R.color.app_greyDeep)
            }
            if (!style.hasValue(R.styleable.PageUIStyle_activeTextColor)) {
                this.activeTextColor =  context.getColor(R.color.app_white)
            }
            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpCategoryButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }
    @DrawableRes
    var defaultImageRes:Int? = null
    @DrawableRes
    var activeImageRes:Int? = null
    var title:String? = null
        set(value) {
            field = value
            if (field == null) binding.textTitle.visibility = View.GONE
            else {
                binding.textTitle.visibility = View.VISIBLE
                binding.textTitle.text = field
            }
        }
    var text:String? = null
        set(value) {
            field = value
            if (field == null) binding.textSubTitle.visibility = View.GONE
            else {
                binding.textSubTitle.visibility = View.VISIBLE
                binding.textSubTitle.text = field
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