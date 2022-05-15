package com.skeleton.component.graph

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.Size
import android.view.ViewGroup
import androidx.annotation.ColorRes
import com.lib.util.ComponentLog

class GraphBuilder {

    private val appTag = "GraphBuilder"

    lateinit var graph:Graph; private set
    private var type:Graph.Type

    constructor(type:Graph.Type) {
        this.type = type
    }
    constructor(parent:ViewGroup, params:ViewGroup.LayoutParams, type:Graph.Type, isResizeObserver:Boolean = true) {
        this.type = type
        graph = makeGraph(parent.context)
        if (isResizeObserver) {
            graph.viewTreeObserver?.addOnGlobalLayoutListener {
                setSize(Size(params.width, params.height), isImmediately = false)
            }
        }
        parent.addView(graph, params)
    }
    constructor(parent:ViewGroup,  type:Graph.Type, isResizeObserver:Boolean = true) {
        this.type = type
        graph = makeGraph(parent.context)
        if (isResizeObserver) {
            graph.viewTreeObserver?.addOnGlobalLayoutListener {
                setSize(Size(parent.width, parent.height), isImmediately = false)
            }
        }
        parent.addView(graph, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    constructor(_graph: Graph) {
        graph = _graph
        this.type = graph.type
    }

    private fun makeGraph(context:Context):Graph {
        graph = when(type){
            Graph.Type.HolizentalBar -> GraphBar(context)
            Graph.Type.VerticalBar -> GraphBar(context)
            Graph.Type.Circle -> GraphCircle(context)
            Graph.Type.HalfCircle -> GraphHalfCircle(context)
            Graph.Type.HalfRing -> GraphHalfRing(context)
            Graph.Type.Ring -> GraphRing(context)
            Graph.Type.Polygon -> GraphPolygon(context)
            Graph.Type.PointCircle -> GraphPointCircle(context)
            Graph.Type.Line -> GraphLine(context)
        }
        when(type){
            Graph.Type.HolizentalBar -> (graph as? GraphBar)?.isVertical = false
            else  -> {}
        }
        return graph
    }

    fun setAnimationType(type:Graph.AnimationType):GraphBuilder {
        graph.aniType = type
        return this
    }

    fun setRange(endValue:Double):GraphBuilder {
        graph.setRange(endValue)
        return this
    }

    fun setColor(@ColorRes colors:Array<Int>, @ColorRes bgColor:Int? = null):GraphBuilder {
        val newColors = colors.mapNotNull { graph.context.getColor(it) }
        graph.setColor(newColors.toTypedArray(), if (bgColor==null) null else graph.context.getColor(bgColor))
        return this
    }

    fun setColor(colors:Array<String>):GraphBuilder {
        val newColors = colors.mapNotNull { Color.parseColor(it) }
        graph.setColor(newColors.toTypedArray())
        return this
    }

    fun setColor(@ColorRes color:Int, @ColorRes bgColor:Int? = null):GraphBuilder {
        graph.setColor(arrayOf(graph.context.getColor(color)), if (bgColor==null) null else graph.context.getColor(bgColor))
        return this
    }

    fun setColor(color:String):GraphBuilder {
        graph.setColor(arrayOf(Color.parseColor(color)))
        return this
    }

    fun setPaint(paints:ArrayList<Paint>):GraphBuilder {
        graph.paints = paints
        return this
    }

    fun setStroke(stroke:Float, style:Paint.Style = Paint.Style.STROKE, cap:Paint.Cap = Paint.Cap.ROUND):GraphBuilder {
        graph.setStroke(stroke, style, cap)
        return this
    }

    fun setFill(shader: Shader? = null):GraphBuilder {
        graph.setFill(shader)
        return this
    }

    fun setSize(size:Size, isImmediately:Boolean = true):GraphBuilder {
        graph.size = size
        if (isImmediately) graph.setImmediatelyValues(graph.values)
        else graph.values = graph.values.map { it }
        return this
    }

    fun setDuration(duration:Long):GraphBuilder {
        graph.setAnimationDuratiuon(duration)
        return this
    }

    fun set(value:Double):Graph {
        graph.setImmediatelyValues(arrayListOf(value))
        return graph
    }

    fun set(values:List<Double>):Graph {
        graph.setImmediatelyValues(values)
        return graph
    }

    fun show(value:Double, delay:Long = 0):Graph {
        graph.delay = delay
        graph.values = arrayListOf(value)
        return graph
    }

    fun show(values:List<Double>, delay:Long = 0):Graph {
        graph.delay = delay
        graph.values = values
        return graph
    }



}