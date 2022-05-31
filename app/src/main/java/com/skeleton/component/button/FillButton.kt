package com.skeleton.component.button
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.UiFillButtonBinding
enum class PageButtonType{
    Normal, Stroke, Small, UnderLine, Select;
    companion object {
        fun getType(resIdx:Int):PageButtonType{
            return when(resIdx){
                1 -> PageButtonType.Stroke
                2 -> PageButtonType.Small
                3 -> PageButtonType.UnderLine
                4 -> PageButtonType.Select
                else -> PageButtonType.Normal
            }
        }
    }
}

open class FillButton : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: UiFillButtonBinding
    private var btnType:PageButtonType = PageButtonType.Normal

    override fun onInit() {
        super.onInit()
        binding = UiFillButtonBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            val btnStyle = context.obtainStyledAttributes(it, R.styleable.PageButtonStyle)
            var type = PageButtonType.Normal
            if (btnStyle.hasValue(R.styleable.PageButtonStyle_buttonType)) {
                val resIdx = btnStyle.getInt(R.styleable.PageButtonStyle_buttonType, 0)
                type = PageButtonType.getType(resIdx)
            }
            setupButton(type)
            super.initialize(context, attrs)
        }
    }
    fun setupButton(type:PageButtonType, isSelected:Boolean = false):FillButton{
        setupButton(type)
        this.selected = isSelected
        return this
    }
    private fun setupButton(type:PageButtonType){
        this.btnType = type
        this.radius = context.resources.getDimension(R.dimen.radius_lightExtra)
        when(type){
            PageButtonType.Stroke -> {
                this.defaultTextSize = context.resources.getDimension(R.dimen.font_regularExtra) / context.resources.displayMetrics.density
                this.defaultTextColor = context.getColor( R.color.app_greyDeep )
                this.activeTextColor = context.getColor( R.color.app_white )
                this.defaultBgColor = context.getColor( R.color.app_white )
                this.activeBgColor = context.getColor( R.color.app_greyDeep )
                this.strokeColor = context.getColor( R.color.app_greyDeep )
                this.stroke = context.resources.getDimension(R.dimen.stroke_light)
            }
            PageButtonType.Small -> {
                this.defaultTextSize = context.resources.getDimension(R.dimen.font_regularExtra) / context.resources.displayMetrics.density
                this.defaultTextColor = context.getColor( R.color.app_white )
                this.defaultBgColor = context.getColor( R.color.app_greyLight )
                this.activeBgColor = context.getColor( R.color.brand_primary )
            }
            PageButtonType.UnderLine -> {
                this.radius = 0f
                this.defaultTextSize = context.resources.getDimension(R.dimen.font_regularExtra) / context.resources.displayMetrics.density
                this.defaultTextColor = context.getColor( R.color.brand_fourth )
                this.activeTextColor = context.getColor( R.color.brand_thirdly )
                this.defaultBgColor = context.getColor( R.color.app_white )
                this.activeBgColor = context.getColor( R.color.app_white )
                this.useLine = true
            }
            PageButtonType.Select -> {
                this.radius = context.resources.getDimension(R.dimen.radius_medium)
                this.defaultTextSize = context.resources.getDimension(R.dimen.font_lightExtra) / context.resources.displayMetrics.density
                this.defaultTextColor = context.getColor( R.color.app_greyExtra )
                this.activeTextColor = context.getColor( R.color.brand_primary )
                this.defaultBgColor = context.getColor( R.color.transparent )
                this.activeBgColor = context.getColor( R.color.app_white )
                this.defaultStroke = context.resources.getDimension(R.dimen.stroke_light)
                this.defaultStrokeColor = context.getColor( R.color.transparent )
                this.activeStrokeColor = context.getColor( R.color.brand_primary )
            }
            else -> {
                this.defaultTextSize = context.resources.getDimension(R.dimen.font_thin) / context.resources.displayMetrics.density
                this.defaultTextColor = context.getColor( R.color.app_white )
                this.defaultBgColor = context.getColor( R.color.app_greyLight )
                this.activeBgColor = context.getColor( R.color.brand_primary )
            }
        }
    }

    override fun onBinding() {
        super.onBinding()
        when(this.btnType){
            PageButtonType.UnderLine -> {
                binding.body.gravity = Gravity.LEFT
            }
            else ->{
                binding.body.gravity = Gravity.CENTER
            }
        }
    }
    fun getButton():Button{
        return binding.btn
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.btn.setOnClickListener(l)
    }

    @DrawableRes
    var defaultImageRes:Int? = null

    @DrawableRes
    var activeImageRes:Int? = null

    var useLine:Boolean? = null
        set(value) {
            field = value
            if (field == null) {
                when(this.btnType){
                    PageButtonType.UnderLine -> {
                        binding.line.visibility = View.VISIBLE
                    }
                    else ->{
                        binding.line.visibility = View.GONE
                    }
                }
            }
            else {
                binding.line.visibility = if (field==true) View.VISIBLE else View.GONE
            }
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
            setBgColor(field)
            setOutline(field)
            defaultTextColor?.let { color ->
                if ( field == false) binding.text.setTextColor( color  )
                else binding.text.setTextColor( activeTextColor ?: color )
            }
            defaultTextSize?.let { size ->
                if ( field == false) binding.text.textSize = size
                else  binding.text.textSize = activeTextSize ?: size
            }
            val drawable: Drawable? = if (defaultImageRes != null) AppCompatResources.getDrawable(context,defaultImageRes!!) else defaultImage
            val drawableAc: Drawable? = if (activeImageRes != null) AppCompatResources.getDrawable(context,activeImageRes!!) else activeImage
            if ( drawable==null && drawableAc==null ) {
                binding.imageIcon.visibility = View.GONE
                return
            }
            binding.imageIcon.visibility = View.VISIBLE
            if ( field == false) {
                binding.imageIcon.setImageDrawable(drawable ?: drawableAc!!)
            }
            else {
                binding.imageIcon.setImageDrawable(drawableAc ?: drawable!!)
            }
        }
}