package com.skeleton.component.progressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import com.lib.util.Log
import com.lib.view.animate.AnimateDrawView
import com.lib.view.animate.AnimationUtil

class ImageSpinner: AnimateDrawView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    private var centerX = 0.0f
    private var centerY = 0.0f
    private var radius = 0.0f
    private var currentValue = 0.0f
    private var drawRersource:Bitmap? = null
    var repeatTime = 2000.0
    @DrawableRes var resource = 0
    set(value) {
        if(field == value) return
        field = value
        drawRersource = BitmapFactory.decodeResource(context.resources, resource)
    }

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
        if(isRunning) return
        isReverse = false
        visibility = View.VISIBLE
        startAnimation(0)
    }
    private fun loaded(){
        if(!isRunning) return
        visibility = View.GONE
        stopAnimation()
        isDrawing = false
    }


    private var isReady = false
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isReady = true
        if(isLoading) loading()
    }

    override fun onStart() {
        val r = width.toFloat()/2.0f
        radius =  r
        centerX = r
        centerY = r
        isDrawing = true
        frm = 1
    }

    private var prevValue = 0.0f
    private var isReverse = false
    override fun onCompute(f: Int) {
        var v = if(isReverse) AnimationUtil.easeOutSine(currentTime.toDouble(), 0.0, 360.0 , repeatTime)
        else AnimationUtil.easeInSine(currentTime.toDouble(), 0.0, 360.0  , repeatTime)
        if(v >= (359.0)) {
            v = 0.0
            frm = 0
        }
        currentValue = v.toFloat()
        if(currentValue < prevValue) isReverse = !isReverse
        prevValue = currentValue

        Log.i(appTag, "onCompute $currentValue")
    }

    override fun onDrawAnimation(canvas: Canvas?) {
        Log.i(appTag, "onDrawAnimation $drawRersource $radius")
        drawRersource ?: return
        canvas ?: return
        if(radius <= 20.0f) {
            onStart()
            return
        }
        canvas.save()
        val mat = Matrix()
        val sc = radius * 2.0f / drawRersource!!.width
        Log.i(appTag, "onDrawAnimation $sc  $radius")
        mat.preRotate(currentValue, centerX, centerY)
        mat.preScale(sc, sc)
        mat.postTranslate(centerX - radius, centerY - radius)
        canvas.drawBitmap(drawRersource!!, mat, null)
        canvas.restore()
    }
}