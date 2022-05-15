package com.skeleton.component.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.lib.util.ComponentLog
import com.lib.view.animate.AnimationUtil
import dagger.Component
import kotlin.math.roundToInt


class GraphLine : Graph{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    var usePoint = true
    var activeIndex:Int = -1
    private var range:Float = -0.0f
    private var max = 0.0f
    var pointBgPaint:Paint? = null
    var graphMargin = 6.0f

    init {
        this.duration = AnimationUtil.ANIMATION_DURATION
        this.type = Type.Line
    }
    override fun getModifyValues(value: List<Double>): List<Double> {
        return value
    }

    override fun setRange(endValue: Double) {
        this.endValue = endValue
        camera = Camera()
        //camera.rotateY(-40.0f)
        //camera.rotateX(20.0f)
    }

    override fun setColor(colors: Array<Int>, bgColor:Int?) {
        paints = ArrayList()
        val dpi = context.resources.displayMetrics.density
        graphMargin *= dpi
        colors.forEach {
            val paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.SQUARE
            paint.strokeWidth =  dpi * 2.0f
            paint.color = it
            paints.add( paint )
        }
        bgColor?.let {
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = it
            pointBgPaint = paint
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onDrawAnimation(canvas: Canvas?) {
        if(kind == 0) return
        camera.save()
        canvas?.save()
        camera.applyToCanvas(canvas)
        val path = Path()
        val paint = paints[0]
        val activePaint = if(paints.size > 1) paints[1] else paint
        val positions = ArrayList<PointF>()
        values.forEachIndexed { idx , value ->
            val v =  max - ( value / endValue * currentValue * max )
            val position = PointF((idx.toFloat() * range) + graphMargin , (v.toFloat() + graphMargin) )
            positions.add(position)
            if(idx ==0 )path.moveTo( position.x, position.y)
            else path.lineTo(position.x, position.y)
        }
        canvas?.drawPath(path, paint)
        positions.forEachIndexed{ idx , position ->
            if(usePoint) {
                val r = graphMargin - paint.strokeWidth
                pointBgPaint?.let {
                    canvas?.drawCircle(position.x, position.y, r, it)
                }
                canvas?.drawOval(position.x-r, position.y -r, position.x+r, position.y+r,
                    if(activeIndex == idx) activePaint ?: paint else paint)
            }
        }


        canvas?.restore()
        camera.restore()
        if( currentValue != 1.0 ) return
        drawGraphListener?.let {
            val datas = ArrayList<Pair<Double, Point>>()
            values.forEachIndexed { idx , value ->
                val p = positions[idx]
                val point = Point(p.x.roundToInt(), p.y.roundToInt())
                datas.add(Pair(value, point))
            }
            it.onDrawCompleted(this, datas)
        }
    }

    override fun onStart() {
        super.onStart()
        if(values.isEmpty()) return
        max =  size.height.toFloat() - graphMargin
        range = (size.width - (graphMargin*2)) / (values.size-1).toFloat()
        currentValue = 0.0
        targetValue = 1.0
        startValue = 0.0
        ComponentLog.d("size : $size", appTag)
        ComponentLog.d("max : $max", appTag)
    }

    override fun setStroke(stroke: Float, style: Paint.Style, cap: Paint.Cap) {
        super.setStroke(stroke, style, cap)
    }


}