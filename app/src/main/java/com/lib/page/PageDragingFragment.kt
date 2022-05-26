package com.lib.page

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.Range
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.lib.model.Gesture
import com.lib.util.AnimationDuration
import com.lib.util.Log
import com.lib.util.animateAlpha
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min


abstract class PageDragingFragment : PageFragment(), PageDragingView.Delegate{
    private lateinit var contentView: PageDragingView
    private lateinit var bodyView:View
    private lateinit var bgView:View
    protected open var useGesture:Boolean = true
    protected open var closePos:Int = -1
    set(value) {
        field = value
        if( value == -1 ) return
        contentView.closePos = closePos
    }
    protected open var dragArea:Range<Int>? = null
        set(value) {
            field = value
            contentView.dragArea = dragArea
        }
    abstract fun getContentView(): PageDragingView
    abstract fun getBodyView(): View
    abstract fun getBackgroundView(): View

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bodyView = getBodyView()
        bgView = getBackgroundView()
        contentView = getContentView()
        contentView.useGesture = useGesture
        contentView.contentsView = bodyView
        contentView.delegate = this
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onMove(view: PageDragingView, pct:Float) {
        if (bgView.visibility == View.GONE)  bgView.visibility = View.VISIBLE
        bgView.alpha = pct
    }

    override fun onAnimate(view: PageDragingView, pct:Float){
        if (bgView.visibility == View.GONE)  bgView.visibility = View.VISIBLE
        bgView.alpha = pct
    }

    override fun onClose(view: PageDragingView) {
        bgView.animateAlpha(0.0f)
        pageObject?.let { page ->
            page.isBottom = true
            delegate?.onBottomPage(page)
        }

    }

    override fun onReturn(view: PageDragingView) {
        pageObject?.isBottom = false
        pageObject?.let { page ->
            page.isBottom = false
            delegate?.onTopPage(page)
        }
    }

}

open class PageDragingView: FrameLayout, Gesture.Delegate {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs:AttributeSet) : super(context,attrs)

    private val appTag = javaClass.simpleName
    private val DURATION_DIV = 5
    var closeType = Gesture.Type.PanDown
    var returnType = Gesture.Type.PanUp
    var delegate: Delegate? = null
    lateinit var contentsView: View
    var isVertical = false; private set
    var isHorizontal = false; private set
    var useGesture = true
    var dragArea:Range<Int>? = null
    var closePos:Int = 0
        set(value) {
            if (field == value) return
            field = value
            if( value == -1 ) return
            if( isClosed ) onGestureClose(isClosure = false, isMove = false)
        }

    private var animation: ViewPropertyAnimator? = null

    private lateinit var gesture:Gesture
    private var startPosition = 0f
    private var finalGesture = Gesture.Type.None
    private var animationCloseRunnable: Runnable = Runnable { didCloseAnimation() }
    private var animationReturnRunnable: Runnable = Runnable { didReturnAnimation() }

    private var _contentSize = 0f
    val contentSize:Float
        get() {
            if (_contentSize != 0f) return _contentSize
            _contentSize = if (isVertical) contentsView.height.toFloat() else contentsView.width.toFloat()
            return _contentSize
        }



    open fun setGestureStart(startPos:Float) {
        if(isVertical) contentsView.translationY = startPos else contentsView.translationX = startPos
    }

    open fun setGestureClose() {
        val closePosX = getClosePos().first
        val closePosY = getClosePos().second
        contentsView.translationX = closePosX
        contentsView.translationY = closePosY
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isVertical = closeType == Gesture.Type.PanUp || closeType == Gesture.Type.PanDown
        isHorizontal = closeType == Gesture.Type.PanLeft || closeType == Gesture.Type.PanRight
        Log.d(appTag, "onAttachedToWindow $isVertical")
        gesture = Gesture(this,isVertical,isHorizontal)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animation?.cancel()
        animation = null
        delegate = null
        gesture.onDestroy()
    }

    private var prevTouchEvent: MotionEvent? = null
    private var trigger:Boolean = false
    private var isClosed = false
    private var isCloseMove = false
    var gestureArea:RectF? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureArea?.let{
            val px = ev.getX(0)
            val py = ev.getY(0)
            var hitX = true
            var hitY = true
            if (it.left != -1f){
                hitX = it.left <= px && it.right >= px
            }
            if (it.top != -1f){
                hitY = it.top <= py && it.bottom >= py
            }
            if(!hitX || !hitY) return false
        }

        if( ev.action ==  MotionEvent.ACTION_DOWN && !trigger) {
            prevTouchEvent = ev
            return false
        }
        if( ev.action ==  MotionEvent.ACTION_MOVE) {
            trigger = gesture.adjustEvent(ev)
            val pos = gesture.movePosA.first()
            val eventPos = if( isVertical ) pos.y else pos.x
            Log.d(appTag, "${isClosed} $eventPos")
            if (!isClosed ){
                dragArea?.let {
                    Log.d(appTag, "${it} $eventPos")
                    if ( !it.contains(eventPos) ) {
                        trigger = false
                        Log.d(appTag, "trigger off$trigger")
                        return false
                    }
                    Log.d(appTag, "trigger on $trigger")
                }
            }else if ( isCloseMove ){
                val range = if( isVertical ) contentsView.height else contentsView.width
                if ((range - closePos) > eventPos) {
                    trigger = false
                    return false
                }else{
                    isCloseMove = false
                }
            }
            parent.requestDisallowInterceptTouchEvent(trigger)
            if (prevTouchEvent != null && trigger) {
                onTouchEvent(prevTouchEvent!!)
                prevTouchEvent = null
            }
            return trigger
        }
        if( (ev.action ==  MotionEvent.ACTION_UP
                || ev.action ==  MotionEvent.ACTION_CANCEL
                ||ev.action ==  MotionEvent.ACTION_OUTSIDE)
            ) {
            gesture.adjustEvent(ev)
            prevTouchEvent = null
            gesture.cancelEvent()
            trigger = false
            isCloseMove = isClosed
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (trigger) {
            gesture.adjustEvent(ev)
            if( ev.action ==  MotionEvent.ACTION_UP
                || ev.action ==  MotionEvent.ACTION_CANCEL
                ||ev.action ==  MotionEvent.ACTION_OUTSIDE
            ) {
                parent.requestDisallowInterceptTouchEvent(false)
                prevTouchEvent = null
                gesture.cancelEvent()
                trigger = false
                isCloseMove = isClosed
            }
            true
        }else{
            super.onTouchEvent(ev)
        }
    }

    override fun stateChange(g: Gesture, e: Gesture.Type) {
        if (!trigger &&  e != Gesture.Type.Start) return
        when (e) {
            Gesture.Type.Start -> touchStart()
            Gesture.Type.MoveV -> if(isVertical) touchMove(g.deltaY)
            Gesture.Type.MoveH -> if(isHorizontal) touchMove(g.deltaX)
            Gesture.Type.End,Gesture.Type.Cancel -> touchEnd()
            else -> { }
        }
    }

    private fun touchStart() {
        Log.d(appTag, "touchStart")
        finalGesture = Gesture.Type.None
        delegate?.onMoveStart(this)
        contentsView.let {startPosition = if(isVertical) it.translationY else it.translationX }
    }

    private fun getMoveAmount(pos:Float) :Float {
        var p = pos
        var max = 0
        when(closeType) {
            Gesture.Type.PanDown -> {
                max = contentsView.height
                if (p > max) p = max.toFloat() else if (p < 0f) p = 0f
                contentsView.translationY = floor(p.toDouble()).toFloat()
            }
            Gesture.Type.PanUp -> {
                max = -contentsView.height
                if (p < max) p = max.toFloat() else if (p > 0f) p = 0f
                contentsView.translationY = floor(p.toDouble()).toFloat()
            }
            Gesture.Type.PanRight -> {
                max = contentsView.width
                if (p > max) p = max.toFloat() else if (p < 0f) p = 0f
                contentsView.translationX = floor(p.toDouble()).toFloat()
            }
            Gesture.Type.PanLeft -> {
                max = -contentsView.width
                if (p < max) p = max.toFloat() else if (p > 0f) p = 0f
                contentsView.translationX = floor(p.toDouble()).toFloat()
            }
            else -> { }
        }
        return (max - p) / max
    }

    private fun touchMove(delta:Int) {
        val p = delta + startPosition
        Log.d(appTag, "touchMove $p")
        delegate?.onMove(this,getMoveAmount(p))
    }

    private fun touchEnd() {
        Log.d(appTag, "touchEnd ${finalGesture.name}")
        if (isClosed)
            when (finalGesture) {
                returnType -> onGestureReturn()
                closeType -> {
                    closePos = 0
                    onGestureClose(true)
                }
                Gesture.Type.None -> onGestureReturn()
                else -> onGestureClose(false)
            }
        else
            if (finalGesture == closeType ) onGestureClose() else onGestureReturn(false)

    }

    override fun gestureComplete(g: Gesture, e: Gesture.Type) {
        this.finalGesture = e
        Log.d(appTag, "touch Complete ${finalGesture.name}")
    }

    private fun getClosePos():Pair<Float,Float> {
        var closePosX = 0f
        var closePosY = 0f
        when(closeType) {
            Gesture.Type.PanDown -> closePosY = contentsView.height.toFloat() - closePos
            Gesture.Type.PanUp -> closePosY = -contentsView.height.toFloat() - closePos
            Gesture.Type.PanRight -> closePosX = contentsView.width.toFloat() - closePos
            Gesture.Type.PanLeft -> closePosX = -contentsView.width.toFloat() - closePos
            else -> { }
        }
        return Pair(closePosX + 10,closePosY + 10)
    }

    open fun onGestureClose(isClosure:Boolean = true, isMove:Boolean = true):Long {
        if (isMove) delegate?.onMoveStart(this)
        isClosed = true
        isCloseMove = true
        val closePosX = getClosePos().first
        val closePosY = getClosePos().second

        val start = if (isVertical) contentsView.translationY else contentsView.translationX
        val end = if (isVertical) closePosY else closePosX
        var duration = if (isVertical) abs(closePosY - contentsView.translationY).toLong()
        else abs(closePosX - contentsView.translationX).toLong()
        duration /= DURATION_DIV

        animation?.cancel()
        animation = contentsView.animate()
            .translationX(closePosX)
            .translationY(closePosY)
            .setInterpolator(DecelerateInterpolator())
            .setUpdateListener {
                if (isMove) this.onUpdateAnimation(it, start, end) }
            .setDuration(min(duration, AnimationDuration.SHORT))

        if(isClosure) animation?.withEndAction(animationCloseRunnable)
        animation?.start()
        return duration
    }
    protected open fun didCloseAnimation() {
        delegate?.onClose(this)
    }

    open fun onGestureReturn(isClosure:Boolean = true):Long {
        isClosed = false
        isCloseMove = false
        val start = if (isVertical) contentsView.translationY else contentsView.translationX
        var duration = if (isVertical) abs(contentsView.translationY).toLong() else abs(contentsView.translationX).toLong()
        duration /= DURATION_DIV

        animation?.cancel()
        animation = contentsView.animate()
            .translationX(0f)
            .translationY(0f)
            .setInterpolator(AccelerateInterpolator())
            .setUpdateListener{this.onUpdateAnimation(it, start, 0f)}
            .setDuration(min(duration, AnimationDuration.SHORT))
        if (isClosure) animation?.withEndAction(animationReturnRunnable)
        animation?.start()
        return duration
    }

    protected open fun onUpdateAnimation(animation: ValueAnimator, start: Float, end: Float) {
        val dr = if (end > start) 1f else -1f
        val range = abs(end - start)
        val pct = animation.animatedValue as Float
        val pos = start + (dr*range*pct)

        delegate?.onAnimate(this, getMoveAmount(pos))
    }

    protected open fun didReturnAnimation() {
        delegate?.onReturn(this)
        delegate?.onMoveCompleted(this)
    }

    interface Delegate {
        fun onMoveStart(view: PageDragingView){}
        fun onMoveCompleted(view: PageDragingView){}
        fun onMove(view: PageDragingView, pct:Float){}
        fun onAnimate(view: PageDragingView, pct:Float){}
        fun onClose(view: PageDragingView){}
        fun onReturn(view: PageDragingView){}
    }
}