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
import com.raftgroup.pupping.databinding.CpCheckButtonBinding


open class CheckButton : PageUI {
    constructor(context: Context) : super(context) { initialize(context, null)}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpCheckButtonBinding
    override fun initialize(context: Context, attrs: AttributeSet?) {
        this.defaultTextColor = context.getColor(R.color.app_grey)
        this.activeTextColor =  context.getColor(R.color.brand_secondary)
        attrs?.let {

            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpCheckButtonBinding.inflate(LayoutInflater.from(context), this, true)
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

    fun setup(@StringRes stringRes:Int, @DrawableRes iconRes:Int, @ColorRes colorRes:Int , selected:Boolean = false){
        defaultImageRes = iconRes
        activeTextColor = context.getColor(colorRes)
        text = context.resources.getString(stringRes)
        this.selected = selected
    }

    override var selected:Boolean? = null
        set(value) {
            field = value
            val context = context ?: return

            val drawable: Drawable? = if (defaultImageRes != null) AppCompatResources.getDrawable(context,defaultImageRes!!) else defaultImage
            val drawableAc: Drawable? = if (activeImageRes != null) AppCompatResources.getDrawable(context,activeImageRes!!) else activeImage
            drawable?.let {
                if ( field == false) {
                    binding.imageIcon.setImageDrawable(it)
                }
                else {
                    binding.imageIcon.setImageDrawable(drawableAc ?: it)
                }
            }

            var dfColor:Int? = null
            var acColor:Int? = null
            defaultTextColor?.let { color ->
                dfColor = color
                activeTextColor?.let {  acColor = it }
            }
            dfColor?.let { color ->
                if ( field == false) {
                    binding.text.setTextColor( color  )
                    binding.imageIcon.setColorFilter(color)
                }
                else {
                    binding.text.setTextColor( acColor ?: color )
                    binding.imageIcon.setColorFilter(acColor ?: color)
                }
            }
        }
}