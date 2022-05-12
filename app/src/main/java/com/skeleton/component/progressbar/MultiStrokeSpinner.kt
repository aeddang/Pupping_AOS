package com.skeleton.component.progressbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

import com.lib.view.animate.AnimateDrawView
import com.lib.view.animate.AnimationUtil
import com.raftgroup.pupping.R
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class MultiStrokeSpinner: AnimateDrawView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    private var centerX = 0.0f
    private var centerY = 0.0f
    private var radius = 0.0f
    private var currentValue = 0
    var barNum = 12
        set(value) {
            field = value
            barDegree = 360/barNum
        }
    private var barDegree = 360/barNum
    var barSize = 4.0f * context.resources.displayMetrics.density
    var repeatTime = 800.0
    var strokeWidth = 2.0f * context.resources.displayMetrics.density
    var colors = arrayOf(context.getColor(R.color.app_grey), context.getColor(R.color.app_greyLight))
        set(value) {
            field = value
            val size = paints.size
            value.filterIndexed { index, _ ->  index < size }.forEachIndexed { index, c -> paints[index].color = c }
        }
    var paints = arrayOf( Paint(), Paint() )

    init {
        isDrawing = false
        visibility = View.GONE
    }

    var isLoading:Boolean = false
    set(value) {
        field = value
        if(!isReady) return
        if(field) loading() else loaded()
    }
    private fun loading(){
        isReverse = false
        visibility = View.VISIBLE
        startAnimation(0)
    }
    private fun loaded(){
        visibility = View.GONE
        stopAnimation()
        isDrawing = false
    }


    private var isReady = false
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        paints.forEachIndexed{ idx , p->
            p.color = colors[idx]
            p.style = Paint.Style.STROKE
            p.strokeWidth = strokeWidth
            p.strokeCap = Paint.Cap.ROUND
        }
        isReady = true
        if(isLoading) loading()
    }

    override fun onStart() {
        val r = width.toFloat()/2.0f
        radius =  r - strokeWidth
        centerX = r
        centerY = r
        isDrawing = true
        frm = 1
    }

    private var prevValue = 0
    private var isReverse = false
    override fun onCompute(f: Int) {
        var v = if(isReverse) AnimationUtil.easeOutSine(currentTime.toDouble(), 0.0, barNum.toDouble() , repeatTime)
        else AnimationUtil.easeInSine(currentTime.toDouble(), 0.0, barNum.toDouble() , repeatTime)
        if(v >= (barNum - 0.2)) {
            v = 0.0
            frm = 0
        }
        currentValue = round( v ).toInt()
        if(currentValue < prevValue) isReverse = !isReverse
        prevValue = currentValue
    }

    override fun onDrawAnimation(canvas: Canvas?) {
        if(radius <= 20.0f) {
            onStart()
            return
        }
        val inRadius = radius - barSize
        val defaultPaint = if(isReverse) paints[1] else paints[0]
        val progressPaint = if(isReverse) paints[0] else paints[1]
        val len = barNum -1
        for (i in 0..len){
            val d = i * barDegree - 90
            val r = (d * Math.PI/180.0).toFloat()
            val sx = centerX + (cos(r) *inRadius)
            val sy = centerY + (sin(r) *inRadius)
            val ex = centerX + (cos(r) *radius)
            val ey = centerY + (sin(r) *radius)
            val p = if(i>currentValue) defaultPaint else progressPaint
            canvas?.drawLine(sx,sy,ex,ey, p)
        }
    }
}