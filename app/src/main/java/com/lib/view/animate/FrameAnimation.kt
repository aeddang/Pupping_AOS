package com.lib.view.animate
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.annotation.DrawableRes


open class FrameAnimation: AnimateDrawView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    private var paint = Paint()
    private var low = 1
    private var column = 1
    private var totalFrame = 1
    private var frameWidth = 1
    private var frameHeight = 1

    private var src:Rect? = null
    private var dest:Rect? = null
    private var currentFrame:Int = -1
    private var move:Int  = 1
    private var isRefeat:Boolean = false

    var frame:Int = 0
        set(value) {
            field = value
            if( currentFrame == field ) return
            startAnimation(-1)
        }
    private var bitmap:Bitmap? = null

    fun initSet(@DrawableRes resId:Int, column:Int, low:Int, isRefeat:Boolean = false, fps:Long = 1000/60, totalFrame:Int = -1) {
        this.fps = fps
        this.low = low
        this. column = column
        this.isRefeat = isRefeat
        this.totalFrame = if( totalFrame != -1) totalFrame else low * column
        paint.style = Paint.Style.FILL
        bitmap = BitmapFactory.decodeResource(resources, resId)
        bitmap?.let {
            frameWidth = it.width / column
            frameHeight = it.height / low
        }
        this.frame = 0
    }

    override fun onDrawAnimation(canvas: Canvas?) {
        bitmap?.let { bm ->
            if(dest == null)  setDest()
            dest?.let { canvas?.drawBitmap( bm , src, it, paint) }
        }
    }

    override fun onStart() {
        setDest()
    }
    private fun setDest(){
        if( width != 0 && height != 0) dest = Rect(0, 0, width, height)
    }

    override fun onCompute(f: Int) {
        currentFrame += move
        if(isRefeat){
            if(currentFrame == totalFrame) {
                currentFrame = totalFrame-2
                move = -1
            } else if( currentFrame == -1 ) {
                currentFrame = 1
                move = 1
            }

        }else if(currentFrame == totalFrame){
            currentFrame = 0
        }

        val idxX = currentFrame % this.column
        val idxY = Math.floor((currentFrame / this.column).toDouble())
        val tx = idxX * frameWidth
        val ty = (idxY * frameHeight).toInt()
        src = Rect(tx, ty, tx + frameWidth, ty + frameHeight)
        if(currentFrame == frame) onCompleted(f)

    }
}