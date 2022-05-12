package com.skeleton.component.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import androidx.annotation.ColorRes
import com.lib.view.animate.AnimationUtil
import kotlin.math.round

open class GraphBar : Graph {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)

    private val appTag = javaClass.simpleName

    var isVertical:Boolean = true
    set(value) {
        type = if(isVertical) Type.VerticalBar else Type.HolizentalBar
        field = value
    }

    private var center = 0.0f
    private var max = 0.0f
    private var strokeMargin = 0.0f
    init {
        this.duration = AnimationUtil.ANIMATION_DURATION
        bgPaint = Paint()
        bgPaint?.strokeCap = Paint.Cap.ROUND
        bgPaint?.color = Color.LTGRAY
    }

    override fun getModifyValues(value: List<Double>): List<Double> {
        return value
    }

    private var isDivision = false
    private var divisionValue = 0.0
    private var divisionPaint = Paint()
    private var zeroPaint:Paint? = null
    fun setDivision(v:Double, @ColorRes color:Int){
        isDivision = true
        divisionValue = v
        divisionPaint.color = context.getColor(color)

        zeroPaint = Paint()
        zeroPaint?.strokeCap = Paint.Cap.ROUND
        zeroPaint?.color = Color.LTGRAY
    }

    override fun setRange(endValue: Double) {
        this.endValue = endValue
    }

    override fun setColor(colors: Array<Int>, bgColor:Int?) {
        bgColor?.let {
            val paint = Paint()
            paint.color = it
            paint.strokeWidth = strokeWidth
            bgPaint = paint
        }
        paints = ArrayList()
        colors.forEach {
            val paint = Paint()
            paint.color = it
            paint.strokeWidth = strokeWidth
            paints.add( paint )
        }
    }


    @SuppressLint("DrawAllocation")
    override fun onDrawAnimation(canvas: Canvas?) {

        if(kind == 0) return
        var sum = 0.0
        var start = 0.0
        bgPaint?.let {
            canvas?.drawLine(0.0f,center,max,center,it)
        }
        values.forEachIndexed { idx , value ->
            var end = currentValue
            if(!isContinuous){
                val v = sum + value
                end = if ( v >  currentValue ) currentValue else v
            }
            val sPos = start.toFloat() / endValue.toFloat() * max
            var ePos = end.toFloat() / endValue.toFloat() * max
            //ComponentLog.d("onDrawAnimation $endValue $max" , appTag)
            if(zeroPaint != null && ePos == 0f) ePos = -( bgPaint!!.strokeWidth )

            if(ePos <= 0.0f){
                zeroPaint?.let {
                    if( isVertical ) canvas?.drawLine(center,max - sPos,center,max - ePos + strokeMargin,it)
                    else canvas?.drawLine(sPos,center,ePos,center,it)
                }
                return@forEachIndexed
            }
            if(isDivision){
                //ComponentLog.d("strokeWidth $strokeWidth strokeMargin $strokeMargin", appTag)
                if(end <= divisionValue){
                    divisionPaint.strokeCap = paints[0].strokeCap
                    if( isVertical ) canvas?.drawLine(center,max - sPos,center,max - ePos + strokeMargin,divisionPaint)
                    else canvas?.drawLine(sPos,center,ePos,center,divisionPaint)
                }else{
                    val dPos = divisionValue.toFloat() / endValue.toFloat() * max
                    // Log.i(appTag, "dPos $dPos max $max divisionValue $divisionValue sPos $sPos $endValue")
                    val paint = paints[ idx % kind]
                    if( isVertical ) canvas?.drawLine(center,max - sPos,center,max - ePos + strokeMargin,paint)
                    else canvas?.drawLine(sPos,center,ePos,center,paint)
                    val diff = ePos-dPos
                    divisionPaint.strokeCap = if(diff <= strokeMargin) paints[0].strokeCap else Paint.Cap.SQUARE
                    if( isVertical ) canvas?.drawLine(center, max - sPos + strokeMargin  ,center,max - dPos + strokeMargin ,divisionPaint)
                    else canvas?.drawLine(sPos,center,dPos,center,divisionPaint)
                }
            }else{
                //ComponentLog.d("strokeWidth $strokeWidth strokeMargin $strokeMargin", appTag)
                val paint = paints[ idx % kind]
                if( isVertical ) canvas?.drawLine(center,max - sPos,center,max - ePos + strokeMargin,paint)
                else canvas?.drawLine(sPos,center,ePos,center,paint)
            }
            start = end
            sum += value
        }

        drawGraphListener?.let {
            val data = ArrayList<Pair<Double, Point>>()
            val pos = round(currentValue/ endValue.toFloat() * max).toInt()
            val point =  if(isVertical) Point( 0 , pos) else Point( pos , 0 )
            data.add(Pair(currentValue, point))
            it.onDrawCompleted(this, data)
        }
    }

    override fun onStart() {
        super.onStart()
        strokeWidth = if(isVertical) size.width.toFloat() else size.height.toFloat()
        paints.forEach {
            it.strokeWidth = strokeWidth
        }
        divisionPaint.strokeWidth = strokeWidth
        bgPaint?.strokeWidth = strokeWidth
        center = if( isVertical) size.width.toFloat()/2.0f else size.height.toFloat()/2.0f
        max = if( isVertical) size.height.toFloat() else size.width.toFloat()
        targetValue = values.sum()
        if(isContinuous){
            startValue = currentValue
        }else{
            currentValue = 0.0
            startValue = 0.0
        }
        strokeMargin = strokeWidth/2.0f
    }


}