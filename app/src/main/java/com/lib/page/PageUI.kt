package com.lib.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import com.raftgroup.pupping.R


abstract class PageUI : FrameLayout, Page {
    constructor(context: Context): super(context) { init(context) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) { init(context) }
    override fun getLayoutResID(): Int? = null
    private fun init(context: Context) {
        getLayoutResID()?.let {  LayoutInflater.from(context).inflate(it, this, true) }
        this.onInit()

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.onBinding()
    }
    @SuppressLint("CustomViewStyleable", "Recycle")
    protected open fun initialize(context: Context, attrs: AttributeSet?) {
        var isSelected = false
        attrs?.let {
            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            val dpi = context.resources.displayMetrics.density

            if (style.hasValue(R.styleable.PageUIStyle_defaultImage)) {
                this.defaultImage = style.getDrawable(R.styleable.PageUIStyle_defaultImage)
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeImage)) {
                this.activeImage = style.getDrawable(R.styleable.PageUIStyle_activeImage)
            }
            if (style.hasValue(R.styleable.PageUIStyle_android_radius)) {
                this.radius = style.getDimension(R.styleable.PageUIStyle_android_radius, 0f)
            }
            if (style.hasValue(R.styleable.PageUIStyle_shadowX)) {
                this.shadowX = style.getDimension(R.styleable.PageUIStyle_shadowY,0f)
            }
            if (style.hasValue(R.styleable.PageUIStyle_shadowY)) {
                this.shadowY = style.getDimension(R.styleable.PageUIStyle_shadowY,0f)
            }
            if (style.hasValue(R.styleable.PageUIStyle_strokeColor)) {
                this.defaultStrokeColor = style.getColor(R.styleable.PageUIStyle_strokeColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_strokeWidth)) {
                this.defaultStroke = style.getDimension(R.styleable.PageUIStyle_strokeWidth, 0f)
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeStrokeColor)) {
                this.activeStrokeColor = style.getColor(R.styleable.PageUIStyle_activeStrokeColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeStrokeWidth)) {
                this.activeStroke = style.getDimension(R.styleable.PageUIStyle_activeStrokeWidth, 0f)
            }
            if (style.hasValue(R.styleable.PageUIStyle_defaultBgColor)) {
                this.defaultBgColor = style.getColor(R.styleable.PageUIStyle_defaultBgColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeBgColor)) {
                this.activeBgColor = style.getColor(R.styleable.PageUIStyle_activeBgColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_defaultTextColor)) {
                this.defaultTextColor = style.getColor(R.styleable.PageUIStyle_defaultTextColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeTextColor)) {
                this.activeTextColor = style.getColor(R.styleable.PageUIStyle_activeTextColor, 0)
            }
            if (style.hasValue(R.styleable.PageUIStyle_defaultTextSize)) {
                this.defaultTextSize = style.getDimension(R.styleable.PageUIStyle_defaultTextSize,10f) / dpi
            }
            if (style.hasValue(R.styleable.PageUIStyle_activeTextSize)) {
                this.activeTextSize = style.getDimension(R.styleable.PageUIStyle_activeTextSize, 10f) / dpi
            }
            if (style.hasValue(R.styleable.PageUIStyle_selected)) {
                isSelected = style.getBoolean(R.styleable.PageUIStyle_selected, false)
            }
        }
        this.selected = isSelected
    }
    protected open fun onBinding(){}
    protected open fun onInit(){}

    open var selected:Boolean? = null
    protected var defaultImage: Drawable? = null
    protected var activeImage: Drawable? = null

    protected var defaultTextColor:Int? = null
    protected var activeTextColor:Int? = null
    protected var defaultTextSize:Float?  = null
    protected var activeTextSize:Float?  = null

    protected var defaultBgColor:Int? = null
    protected var activeBgColor:Int? = null

    protected var defaultStroke:Float? = null
    protected var defaultStrokeColor:Int? = null
    protected var activeStroke:Float? = null
    protected var activeStrokeColor:Int? = null

    protected var radius:Float = 0f
    protected var shadowX:Float = 0f
    protected var shadowY:Float = 0f

    open var stroke:Float = 1f
        set(value) {
            field = value
            strokeColor?.let {
                this.invalidate()
            }
        }
    open var strokeColor:Int? = null
        set(value) {
            field = value
            strokeColor?.let {
                this.invalidate()
            }
        }
    private var path:Path? = null
    private var cornerRadius:Float = 0f
    override fun onSizeChanged(width:Int, height:Int, oldWidth:Int, oldHeight:Int) {
        if (strokeColor != null) {
            val path = Path();
            val s = stroke/2f - stroke/4f
            path.addRoundRect(
                RectF(s, s, width.toFloat()-s, height.toFloat()-s),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
            this.path = path
        }  else {
            this.path = null
        }
        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }
    override fun dispatchDraw(canvas:Canvas) {
        this.path?.let { p ->
            this.strokeColor?.let {
                val paint = Paint()
                paint.style = Paint.Style.STROKE
                paint.color = it
                paint.strokeWidth = stroke
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawPath(p, paint)
            }
        }
        super.dispatchDraw(canvas)
    }

    open fun setBgColor(isActive:Boolean? = null){
        defaultBgColor?.let {
            this.setBackgroundColor( if (isActive == true) activeBgColor ?: it else it)
        }
    }
    open fun setOutline(isActive:Boolean? = null){
        val stroke:Float? = if (isActive == true) activeStroke ?: defaultStroke  else defaultStroke
        val strokeColor:Int? = if (isActive == true) activeStrokeColor ?: defaultStrokeColor  else defaultStrokeColor
        this.setCorners(radius,
            stroke,strokeColor,shadowX, shadowY
        )
    }

    open fun setCorners(cornerRadius:Float, stroke:Float? = null, strokeColor:Int? = null, shadowOffsetX:Float = 0f, shadowOffsetY:Float = 0f,) {
        this.cornerRadius = cornerRadius
        val mOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val left = 0
                val top = 0;
                val right = view.width
                val bottom = view.height
                outline.setRoundRect(left, top, right, bottom, cornerRadius)
            }
        }
        this.apply {
            outlineProvider = mOutlineProvider
            clipToOutline = true
        }
        stroke?.let {
            this.stroke = it
        }
        strokeColor?.let {
            this.strokeColor = it
        }
    }
}