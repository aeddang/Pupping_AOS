package com.raftgroup.pupping.scene.component.info
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpUnitInfoBinding
class UnitInfo : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpUnitInfoBinding

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
                this.activeTextColor = context.getColor(R.color.brand_primary)
            }
            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpUnitInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setText(@StringRes text:Int, @ColorRes color:Int = R.color.app_grey){
        defaultTextColor = context.getColor(color)
        this.text = context.getString(text)
        this.selected = false
    }

    fun setText(text:String, @ColorRes color:Int = R.color.app_grey){
        defaultTextColor = context.getColor(color)
        this.text = text
        this.selected = false
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setInfo(@DrawableRes icon:Int, text:String, @ColorRes color:Int = R.color.app_grey){
        defaultTextColor = context.getColor(color)
        defaultImage = context.getDrawable(icon)
        this.selected = false
        this.text = text
    }

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
            val drawable: Drawable? = defaultImage
            val drawableAc: Drawable? = activeImage
            if ( drawable==null && drawableAc==null ) return
            if ( field == false) {
                binding.icon.setImageDrawable(drawable ?: drawableAc!!)
            }
            else {
                binding.icon.setImageDrawable(drawableAc ?: drawable!!)
            }
            dfColor?.let { color ->
                if ( field == false) binding.icon.setColorFilter(color)
                else binding.icon.setColorFilter( acColor ?: color )
            }
        }
}