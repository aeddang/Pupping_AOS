package com.skeleton.component.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.lib.util.DataLog
import com.lib.view.animate.AnimationUtil

import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


open class GraphCircle: Graph {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    protected var startDegree:Float = -90.0f
    protected var totalDegree = 360.0
    protected var centerX = 0.0f
    protected var centerY = 0.0f
    protected var radius = 0.0f
    protected var rectF = RectF()
    var isRing = false

    init {
        this.duration = AnimationUtil.ANIMATION_DURATION
        this.type = Type.Circle
    }

    override fun getModifyValues(value: List<Double>): List<Double> {
        return value.map {
            return@map it / endValue * totalDegree
        }
    }

    override fun setRange(endValue: Double) {
        this.endValue = endValue
        camera = Camera()
        //camera.rotateY(-40.0f)
        //camera.rotateX(20.0f)
    }

    override fun setColor(colors: Array<Int>, bgColor:Int?) {
        bgColor?.let {
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.color = it
            paint.strokeWidth = strokeWidth
            paint.strokeCap = Paint.Cap.SQUARE
            bgPaint = paint
        }
        paints = ArrayList()
        colors.forEach {
            val paint = Paint()
            paint.color = it
            if(isRing){
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.strokeCap = Paint.Cap.SQUARE
            }
            paints.add( paint )
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDrawAnimation(canvas: Canvas?) {
        if(kind == 0) return
        var start = 0.0
        var sum = 0.0
        camera.save()
        canvas?.save()
        canvas?.translate(centerX, centerY)
        camera.applyToCanvas(canvas)
        canvas?.translate(-centerX, -centerY)
        canvas?.rotate(startDegree, centerX, centerY)
        bgPaint?.let {
            canvas?.drawArc(rectF,0.0f, totalDegree.toFloat(), !isRing, it)
        }
        values.forEachIndexed { idx , value ->
            var end = currentValue
            if(!isContinuous){
                val v = sum + value
                end = if ( v >  currentValue ) currentValue else v
                if( end <= start) return
            }
            val paint = paints[ idx % kind ]

            var e = if(type == Type.PointCircle) 1.0f else end.toFloat()
            val s = if(type == Type.PointCircle) end.toFloat() else start.toFloat()

            if(!isContinuous) e -= s
            //DataLog.d( "idx:$idx s: $s e: $e" , appTag)
            canvas?.drawArc(rectF,s, e, !isRing, paint)
            start = end
            sum += value
        }
        canvas?.restore()
        camera.restore()
        if( currentValue != targetValue ) return
        drawGraphListener?.let {
            val data = ArrayList<Pair<Double, Point>>()
            val l = rectF.width()/2.0f
            sum = startDegree.toDouble()
            values.forEach { value ->
                val v = sum + (value/2.0)
                val r = v * Math.PI/180
                val tx = centerX + (cos(r) *l)
                val ty = centerY + (sin(r) *l)
                val point = Point( tx.roundToInt() , ty.roundToInt() )
                data.add(Pair(value, point))
                sum += value
            }
            it.onDrawCompleted(this, data)
        }
    }

    override fun onStart() {
        super.onStart()
        val marginX = (width.toFloat() - size.width + strokeWidth)
        val marginY = (height.toFloat() - size.height + strokeWidth)
        val w = width.toFloat() - (marginX/2.0f)
        val h = height.toFloat() - (marginY/2.0f)
        centerX = width.toFloat()/2.0f
        centerY = height.toFloat()/2.0f

        rectF.set(marginX/2.0f, marginY/2.0f, w, h)

        if(isContinuous){
            targetValue = values[0]
            startValue = currentValue
        }else{
            currentValue = 0.0
            targetValue = totalDegree
            startValue = 0.0
        }

        if(targetValue.isNaN()) targetValue = 0.0

    }

    override fun onCompleted(f: Int) {
        super.onCompleted(f)
        //Log.i(appTag, "currentValue:$currentValue  targetValue: $targetValue")
    }


}