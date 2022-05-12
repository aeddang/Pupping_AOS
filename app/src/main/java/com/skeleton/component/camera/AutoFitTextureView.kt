package com.skeleton.component.camera

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.lib.util.Log

class AutoFitTextureView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        TextureView(context, attrs, defStyle) {
    private val appTag = "Camera"//javaClass.simpleName
    private var ratioWidth = 0
    private var ratioHeight = 0

    fun setAspectRatio(width: Int, height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        ratioWidth = width
        ratioHeight = height
        Log.d(appTag, "setAspectRatio $ratioWidth $ratioHeight")
        requestLayout()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (0 == ratioWidth || 0 == ratioHeight) return
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var vw = width
        var vh = height
        if (width < height * ratioWidth / ratioHeight) {
            vh = width * ratioHeight / ratioWidth
        } else {
            vw = height * ratioWidth / ratioHeight
        }
        setMeasuredDimension(vw, vh)
        Log.d(appTag, "setAspectRatio view $vw $vh")
        translationX = ((width - vw)/2).toFloat()
        translationY = ((height - vh)/2).toFloat()

    }
}