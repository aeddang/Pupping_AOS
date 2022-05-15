package com.raftgroup.pupping.scene.component.info
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.graphics.alpha
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.util.*
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.component.list.Picture
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import com.skeleton.component.graph.GraphLine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class LineGraphData(
    var values:List<Double> = arrayListOf(),
    var lines:List<String> = arrayListOf("1/1", "1/2", "1/3", "1/4"),
    var raws:List<Pair<String,Boolean>> = arrayListOf(
        Pair("", false),
        Pair("40", false),
        Pair("30", true), Pair("20", true),
        Pair("10", false), Pair("0", false)
    ),
    var rawsUnit:String = "(minutes)",
    var activeIndex:Int = -1,
    var title: SpannableString? = null
)

@AndroidEntryPoint
class LineGraph : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpLineGraphBinding
    private var graphBuilder: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpLineGraphBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        graphBuilder = GraphBuilder(binding.graphArea, Graph.Type.Line, false)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setColor(arrayOf(R.color.app_grey,R.color.brand_primary), R.color.app_white)
            .setRange(1.0)

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)

    }

    @SuppressLint("SetTextI18n")
    fun setData(data: LineGraphData){
        binding.textTitle.text = data.title
        binding.textRawUnit.text = data.rawsUnit
        binding.rawBody.removeAllViews()
        binding.lineBody.removeAllViews()
        val size = Size(binding.graphArea.width,
            (context.resources.getDimension(R.dimen.bar_medium) * (data.raws.size)).toInt())
        ComponentLog.d("max : ${size.height}", appTag)
        graphBuilder?.setSize(size)
        (graphBuilder?.graph as? GraphLine)?.let {
            it.activeIndex = data.activeIndex
        }
        data.raws.forEach { d->
            val raw = LineGraphBar(context)
            binding.rawBody.addView(raw)
            raw.setData(d, -1)
        }
        setProgress(data)
        if(data.lines.size <= 10){
            data.lines.forEachIndexed {idx, d->
                val line = LineGraphText(context)
                val layout = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f)
                binding.lineBody.addView(line, layout)
                line.setData(d, if(idx == data.activeIndex) 1 else -1)
            }
        }
    }

    private  fun setProgress(data: LineGraphData){
        graphBuilder ?: return
        graphBuilder?.show(data.values)
    }

    inner class LineGraphText : PageComponent, ListItem<String> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpLineGraphTextBinding
        override fun init(context: Context) {
            binding = CpLineGraphTextBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }
        @SuppressLint("SetTextI18n")
        override fun setData(data: String, idx:Int){
            binding.text.text = data
            if (idx != -1){
                binding.text.setTextColor(context.getColor(R.color.brand_primary))
            } else {
                binding.text.setTextColor(context.getColor(R.color.app_greyExtra))
            }
        }
    }

    inner class LineGraphBar : PageComponent, ListItem<Pair<String,Boolean>> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName
        private lateinit var binding: CpLineGraphBarBinding
        override fun init(context: Context) {
            binding = CpLineGraphBarBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }
        @SuppressLint("SetTextI18n")
        override fun setData(data: Pair<String,Boolean>, idx:Int){
            if (data.first.isEmpty()) {
                binding.lineDash.visibility = View.VISIBLE
                binding.line.visibility = View.GONE
                binding.textTitle.visibility = View.GONE
                binding.bg.visibility = View.GONE
            } else {
                binding.line.visibility = View.VISIBLE
                binding.textTitle.visibility = View.VISIBLE
                binding.lineDash.visibility = View.GONE
                binding.textTitle.text = data.first
                binding.bg.visibility = if(data.second) View.VISIBLE else View.GONE

            }
        }
    }
}