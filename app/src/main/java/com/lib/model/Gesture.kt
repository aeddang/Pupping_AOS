package com.lib.model

import android.graphics.Point
import android.view.MotionEvent
import com.lib.util.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.sqrt

class Gesture(var delegate: Delegate?, private val isVertical: Boolean, private val isHorizontal: Boolean) {
    private val appTag = javaClass.simpleName
    enum class Type {
        None,Start,End,Cancel,
        Move,MoveV,MoveH,
        LongTouch,Touch,
        Pan,PanRight,PanLeft,PanUp,PanDown,
        PinchMove,PinchRight,PinchLeft,PinchUp,PinchDown,PinchIn,PinchOut,PinchRotate
    }

    interface Delegate {
        fun stateChange(g: Gesture, e: Type){}
        fun rotateChange(g: Gesture, rotate: Float){}
        fun pinchChange(g: Gesture, dist: Float){}
        fun gestureComplete(g: Gesture, e: Type){}
    }

    private enum class MoveType {
        NONE,VERTICAL,HORIZONTAL
    }
    private val TAG = javaClass.simpleName
    var originPosA: ArrayList<Point> private set
    var startPosA: ArrayList<Point> private set
    var changePosA: ArrayList<Point> private set
    var movePosA: ArrayList<Point> private set

    var deltaX:Int = 0; private set
        get() { return originPosA[0].x - startPosA[0].x + changePosA[0].x }

    var deltaY:Int = 0; private set
        get() { return originPosA[0].y - startPosA[0].y + changePosA[0].y }

    private var moveType = MoveType.NONE
    private var isEventStart: Boolean = false
    private var startTime = 0L
    private var endTime = 0L
    private var startRotate = 0f
    private var startDistance = 0f
    private val changeRotate = 30f
    private val longTime = 2L
    private val moveMin = 3
    private val changeMin = 10
    private val changeMax = 70

    init {
        originPosA = ArrayList()
        startPosA = ArrayList()
        changePosA = ArrayList()
        movePosA = ArrayList()
    }

    fun onDestroy() {
        delegate = null
    }
    fun cancelEvent(){
        endEvent(false)
    }

    fun adjustEvent(event: MotionEvent): Boolean {
        val action = event.action
        val locations = ArrayList<Point>()
        var location: Point
        var pointerIndex: Int
        var mActivePointerId: Int

        for (i in 0 until event.pointerCount) {
            mActivePointerId = event.getPointerId(i)
            pointerIndex = event.findPointerIndex(mActivePointerId)
            location = Point(floor(event.getX(pointerIndex).toDouble()).toInt(), floor(event.getY(pointerIndex).toDouble()).toInt())
            locations.add(location)

        }
        var trigger:Boolean= true
        Log.d(appTag, "touch action $action")
        when (action) {
            MotionEvent.ACTION_DOWN -> startEvent(locations)
            MotionEvent.ACTION_MOVE -> trigger = moveEvent(locations)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE  -> endEvent(true)
        }

        return trigger
    }

    @Synchronized
    private fun startEvent(locations: ArrayList<Point>) {
        isEventStart = true
        moveType = MoveType.NONE
        startPosA = locations
        originPosA = locations
        changePosA = ArrayList()

        for (i in locations.indices) changePosA.add(Point(0, 0))

        val now = Date()
        startTime = now.time
        startDistance = 0f
        startRotate = 0f
        delegate?.stateChange(this, Type.Start)
    }

    @Synchronized
    private fun moveEvent(locations: ArrayList<Point>): Boolean {
        var trigger = false
        if (!isEventStart) startEvent(locations)
        movePosA = ArrayList()
        val len = locations.size
        var location: Point
        for (i in 0 until len) {
            location = locations[i]
            movePosA.add(Point(location.x, location.y))
        }
        var start: Point
        var change: Point

        checkEvent(false)
        if (len == startPosA.size) {
            for (i in 0 until len) {
                location = locations[i]
                movePosA.add(Point(location.x, location.y))
                start = startPosA[i]
                change = changePosA[i]
                change.x = location.x - start.x
                change.y = location.y - start.y
                //Log.d(appTag,"start.x "+start.x+" -  start.y "+start.y )
            }
            change = changePosA[0]

            //Log.d(appTag,"change.x "+change.x+" -  change.y "+change.y )
            if (abs(change.x) > abs(change.y)) {
                if (isHorizontal) trigger = abs(change.x) > moveMin
                if (moveType != MoveType.HORIZONTAL) {
                    moveType = MoveType.HORIZONTAL
                    //startPosA = locations
                }
                if (isHorizontal && len == 1) delegate?.stateChange(this,
                    Type.MoveH
                )
            } else if (abs(change.y) > abs(change.x)) {
                if (isVertical) trigger = abs(change.y) > moveMin
                if (moveType != MoveType.VERTICAL) {
                    moveType = MoveType.VERTICAL
                    //startPosA = locations
                }
                if (isVertical && len == 1) delegate?.stateChange(this,
                    Type.MoveV
                )
            }
            //if(trigger) startPosA = locations
            delegate?.stateChange(this, Type.Move)
        }
        else {
            delegate?.stateChange(this, Type.Cancel)
            endEvent(false)
        }

        Log.d(appTag,"moveEvent $trigger")
        return trigger
    }

    @Synchronized
    private fun endEvent(isComplete: Boolean) {
        if (!isEventStart) return
        val now = Date()
        endTime = now.time
        checkEvent(isComplete)
        delegate?.stateChange(this, Type.End)
        isEventStart = false
    }

    @Synchronized
    private fun checkEvent(isComplete: Boolean) {
        if (startPosA.size != movePosA.size && !isComplete) return
        val spdMD = 100f
        val moveMD: Float
        val start: Point
        val move: Point
        val change: Point

        var gestureTime = 0f
        if (isComplete) gestureTime = (endTime - startTime) / spdMD

        if (startPosA.size == 1) {
            change = changePosA[0]
            if (isComplete) {
                if (gestureTime >= longTime && abs(change.x) < changeMin && Math.abs(change.y) < changeMin) delegate?.gestureComplete(this,
                    Type.LongTouch
                )
                Log.d(appTag, "touch gestureTime $gestureTime")
                when(moveType) {
                    MoveType.HORIZONTAL -> {
                        moveMD = change.x / gestureTime
                        if (moveMD > changeMax) delegate?.gestureComplete(this,
                            Type.PanRight
                        )
                        else if (moveMD < -changeMax) delegate?.gestureComplete(this,
                            Type.PanLeft
                        )
                    }
                    MoveType.VERTICAL -> {
                        moveMD = change.y / gestureTime
                        if (moveMD > changeMax) delegate?.gestureComplete(this,
                            Type.PanDown
                        )
                        else if (moveMD < -changeMax) delegate?.gestureComplete(this,
                            Type.PanUp
                        )
                    }
                    else -> { }
                }
                if (abs(change.x) < changeMin && abs(change.y) < changeMin) delegate?.gestureComplete(this,
                    Type.Touch
                )
            }
            else {
                if (abs(change.x) > changeMin || abs(change.y) > changeMin) delegate?.stateChange(this,
                    Type.Pan
                )
            }
        }
        else if (startPosA.size == 2)
        {
            val start2: Point
            val move2: Point
            try {
                change = changePosA[0]
                start = startPosA[0]
                move = movePosA[0]
                start2 = startPosA[1]
                move2 = movePosA[1]
            }
            catch (e: IndexOutOfBoundsException){return}

            if (startDistance == 0f) startDistance = sqrt(((abs(start.x - start2.x) xor 2) + (abs(start.y - start2.y) xor 2)).toDouble()).toFloat()

            val startDist = startDistance
            val moveDist = sqrt(((abs(move.x - move2.x) xor 2) + (abs(move.y - move2.y) xor 2)).toDouble()).toFloat()
            val dist = moveDist - startDist

            val rotate:Float
            var w:Float
            var h:Float
            if (startRotate == 0f) {
                w = (start.x - start2.x).toFloat()
                h = (start.y - start2.y).toFloat()
                startRotate = (atan2(h.toDouble(), w.toDouble()) / Math.PI * 360).toFloat()
            }
            w = (move.x - move2.x).toFloat()
            h = (move.y - move2.y).toFloat()
            rotate = (atan2(h.toDouble(), w.toDouble()) / Math.PI * 360).toFloat()
            delegate?.rotateChange(this, rotate)

            if (isComplete && abs(startRotate - rotate) > changeRotate) delegate?.gestureComplete( this,
                Type.PinchRotate
            )
            if (isComplete) {
                if (abs(dist) > changeMin) {
                    if (dist > 0) delegate?.gestureComplete(this,
                        Type.PinchOut
                    )
                    else delegate?.gestureComplete(this,
                        Type.PinchIn
                    )
                }
                else {
                    when(moveType) {
                        MoveType.HORIZONTAL -> {
                            moveMD = change.x / gestureTime
                            if (moveMD > changeMax) delegate?.gestureComplete(this,
                                Type.PinchRight
                            )
                            else if (moveMD < -changeMax) delegate?.gestureComplete(this,
                                Type.PinchLeft
                            )
                        }
                        MoveType.VERTICAL -> {
                            moveMD = change.y / gestureTime
                            if (moveMD > changeMax) delegate?.gestureComplete(this,
                                Type.PinchDown
                            )
                            else if (moveMD < -changeMax) delegate?.gestureComplete(this,
                                Type.PinchUp
                            )
                        }
                        else -> { }
                    }
                }
            }
            else {
                if (abs(dist) > 1.0f) delegate?.pinchChange(this, dist)
                else delegate?.stateChange(this,
                    Type.PinchMove
                )
            }
        }
    }
}