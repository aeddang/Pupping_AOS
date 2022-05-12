package com.lib.view.animate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import com.lib.util.ComponentLog
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class AnimateDrawView : View, CoroutineScope{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context,attrs)
    private val appTag = javaClass.simpleName
    protected val pageJob: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main +  pageJob

    protected var animateDrawListener:AnimateDrawListener? = null
    interface AnimateDrawListener {
        fun onDrawCompleted(v: View){}
        fun onDrawStart(v: View){}
    }
    fun setOnAnimateDrawListener( listener:AnimateDrawListener? ) {
        animateDrawListener = listener
    }

    private var animationJob:Job? = null
    private var isCompleted = false
    protected var isRunning = false ; private set
    protected var fps: Long = 1000/60
    protected var frm:Int = 0
    protected var isDrawing = true
    protected var duration:Long = 0
    protected val currentTime:Long
        get() { return frm * fps}


    fun startAnimation( d:Long, delay:Long = 0 ) {
        stopAnimation()
        frm = 0
        duration = d
        isCompleted = false
        isRunning = true
        animateDrawListener?.onDrawStart( this@AnimateDrawView)
        animationJob = launch(Dispatchers.IO + pageJob) {
            delay(delay)
            while( isActive ) run()
            withContext(Dispatchers.Main) {
                if(isCompleted) onCompleted(frm)
                animateDrawListener?.onDrawCompleted(this@AnimateDrawView)
            }
        }
    }
    fun stopAnimation() {
        isRunning = false
        animationJob?.cancel()
    }

    protected open fun run( ) = runBlocking {
        frm++
        if (frm == 1) onStart()
        onCompute(frm)
        if (duration > 0) {
            if (duration <= currentTime) {
                isCompleted = true
                animationJob?.cancel()
            }
        }
        postInvalidate()
        delay(fps)
    }

    abstract fun onStart()
    abstract fun onCompute( f:Int )
    abstract fun onDrawAnimation(canvas: Canvas?)
    protected open fun onCompleted( f:Int ){}


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isDrawing) return
        onDrawAnimation(canvas)
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animateDrawListener = null
        pageJob.cancel()
        cancel()
    }

}