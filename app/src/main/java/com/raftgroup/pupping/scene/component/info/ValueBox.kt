package com.raftgroup.pupping.scene.component.info
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpUnitInfoBinding
import com.raftgroup.pupping.databinding.CpValueBoxBinding

class ValueBox : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpValueBoxBinding

    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            if (style.hasValue(R.styleable.PageUIStyle_android_gravity)) {
                binding.bodyLayout.gravity = style.getInt(R.styleable.PageUIStyle_android_gravity, Gravity.CENTER)
            }

            super.initialize(context, attrs)
        }
    }
    override fun onInit() {
        super.onInit()
        binding = CpValueBoxBinding.inflate(LayoutInflater.from(context), this, true)
    }

    var text:String? = null
        set(value) {
            field = value
            if (field?.isEmpty() != false) binding.textTitle.visibility = View.GONE
            else {
                binding.textTitle.visibility = View.VISIBLE
                binding.textTitle.text = field
            }
        }

    var value:String? = null
        set(value) {
            field = value
            binding.textValue.text = field
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

            val drawable: Drawable? = defaultImage
            val drawableAc: Drawable? = activeImage
            if ( drawable==null && drawableAc==null ){
                binding.icon.visibility = View.GONE
                return
            }
            binding.icon.visibility = View.VISIBLE
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