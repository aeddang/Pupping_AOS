package com.lib.view.animate

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

object AnimationUtil{
    const val ANIMATION_DURATION_SHORT = 500L
    const val ANIMATION_DURATION = 1000L
    const val ANIMATION_DURATION_LONG = 1500L

    fun linear(t:Double, b:Double, c:Double, d:Double):Double {
        return (c * t / d) + b
    }

    fun easeInSine(t:Double, b:Double, c:Double, d:Double):Double {
        return -c * cos(t/d * (Math.PI/2)) + c + b
    }

    fun easeOutSine(t:Double, b:Double, c:Double, d:Double):Double {
        return c * sin(t/d * (Math.PI/2)) + b
    }

    fun easeInElastic(t:Double, b:Double, c:Double, d:Double):Double {
        var s = 0.0
        var p = 0.0
        var a = c
        if (t == 0.0) return b
        var tt = t/d
        if ( tt == 1.0) return b+c
        if ( p == 0.0 ) p = d * 0.3
        if ( a < abs(c)) {
            a=c
            s=p/4.0
        }
        else s = p/(2.0*Math.PI) * asin (c/a)
        tt -= 1.0
        return -(a*Math.pow(2.0,10.0*tt) * sin( (tt*d-s)*(2*Math.PI)/p )) + b
    }

    fun easeOutElastic(t:Double, b:Double, c:Double, d:Double):Double {
        var s = 0.0
        var p = 0.0
        var a = c
        if (t == 0.0) return b
        val tt = t/d
        if (tt == 1.0) return b+c
        if (p == 0.0) p = d * 0.3
        if (a < abs(c)) {
            a= c
            s= p/4.0
        }
        else s = p/(2*Math.PI) * asin (c/a)
        return a*Math.pow(2.0,-10.0*tt) * sin( (tt*d-s)*(2.0*Math.PI)/p ) + c + b
    }


}