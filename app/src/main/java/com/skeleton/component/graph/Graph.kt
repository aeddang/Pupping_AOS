package com.skeleton.component.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import androidx.annotation.CallSuper
import com.lib.util.ComponentLog
import com.lib.view.animate.AnimateDrawView
import com.lib.view.animate.AnimationUtil
import kotlin.math.min


abstract class Graph : AnimateDrawView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = "Graph"
    enum class Type {
        HolizentalBar,
        VerticalBar,
        Circle,
        Ring,
        HalfCircle,
        HalfRing,
        Polygon,
        PointCircle,
        Line
    }

    enum class AnimationType {
        EaseInSine,
        EaseOutSine,
        EaseInElastic,
        EaseOutElastic,
        Linear
    }

    var type:Type = Type.HolizentalBar; protected set
    var aniType:AnimationType = AnimationType.EaseOutSine
    protected var initValue:Double = 0.0
    protected var endValue:Double = 0.0
    protected var targetValue:Double = 1.0
    protected var currentValue:Double = 0.0
    protected var startValue:Double = 0.0
    protected var kind = 0
    protected var isContinuous = true
    protected var camera = Camera()
    protected var strokeWidth = 6f

    protected var drawGraphListener:DrawGraphListener? = null
    interface DrawGraphListener {
        fun onDrawCompleted(graph: Graph, datas:ArrayList<Pair<Double, Point>>){}
    }
    fun setOnDrawGraphListener(listener:DrawGraphListener? ) {
        drawGraphListener = listener
    }

    var size = Size(0,0)
    var bgPaint:Paint? = null
    var paints:ArrayList<Paint> = arrayListOf(Paint())
    internal var delay:Long = 0L

    var values:List<Double> = ArrayList()
        set(value) {
            if(!isContinuous) isDrawing = false
            if(value.isEmpty()) return
            field = getModifyValues(value)
            if(!isImmediately) startAnimation(duration, delay)
        }

    private var isImmediately = false
    open fun setImmediatelyValues(v:List<Double>){
        if(v.isEmpty()) return
        isImmediately = true
        values = v
        onStart()
        onCompleted(0)
        postInvalidate()
        isImmediately = false
    }


    abstract fun getModifyValues(value:List<Double>): List<Double>
    abstract fun setRange(endValue:Double)
    abstract fun setColor(colors:Array<Int>, bgColor:Int? = null)

    open fun setStroke(stroke:Float, style:Paint.Style = Paint.Style.STROKE, cap:Paint.Cap = Paint.Cap.ROUND) {
        strokeWidth = stroke
        paints.forEach {
            it.style = style
            it.strokeWidth = stroke
            it.strokeCap = cap
            it.isAntiAlias = true
        }
    }

    open fun setFill(shader:Shader? = null) {
        paints.forEach {p->
            p.style = Paint.Style.FILL
            shader?.let {  p.setShader(shader) }
        }
    }

    @CallSuper
    override fun onStart() {
        kind = paints.size
        isDrawing = true
        isContinuous = (values.size == 1)
        if(size.width == 0 && size.height == 0) size = Size(width, height)
        else if (size.width == 0) size = Size(size.width, height)
        else if (size.height == 0) size = Size(width, size.height)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(size.width == 0 && size.height == 0) size = Size(width, height)
        else if (size.width == 0) size = Size(size.width, height)
        else if (size.height == 0) size = Size(width, size.height)
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        drawGraphListener = null
    }

    fun setAnimationDuratiuon(d:Long) {
        duration = d
    }

    override fun onCompute(f: Int) {
        val delta = targetValue-startValue
        currentValue = min(targetValue, when(aniType){
            AnimationType.EaseInSine -> AnimationUtil.easeInSine(currentTime.toDouble(), startValue, delta, duration.toDouble())
            AnimationType.EaseOutSine -> AnimationUtil.easeOutSine(currentTime.toDouble(), startValue, delta, duration.toDouble())
            AnimationType.EaseInElastic -> AnimationUtil.easeOutElastic(currentTime.toDouble(), startValue, delta, duration.toDouble())
            AnimationType.EaseOutElastic -> AnimationUtil.easeInElastic(currentTime.toDouble(), startValue, delta, duration.toDouble())
            AnimationType.Linear -> AnimationUtil.linear(currentTime.toDouble(), startValue, delta, duration.toDouble())
        })

    }

    override fun onCompleted(f: Int) { currentValue = targetValue }
}