package com.raftgroup.pupping.scene.component.info
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpArcGraphBinding
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class ArcGraphData(
    var value:Double = 0.0,
    var max:Double = 7.0,
    var start:String = "Goal",
    var end:SpannableString? = null,
    var title: SpannableString? = null
)

@AndroidEntryPoint
class ArcGraph : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpArcGraphBinding
    private var graphBuilder: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpArcGraphBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        graphBuilder = GraphBuilder(binding.graphArea, Graph.Type.HalfRing)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setStroke(context.resources.getDimension(R.dimen.stroke_heavy))
            .setColor(R.color.brand_primary, R.color.app_greyLight)
            .setRange(180.0)

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)

    }

    @SuppressLint("SetTextI18n")
    fun setData(data: ArcGraphData){
        binding.textTitle.text = data.title
        binding.textStart.text = data.start
        binding.textEnd.text = data.end
        setProgress(data)
    }

    private  fun setProgress(data: ArcGraphData){
        graphBuilder ?: return
        val progress = data.value / data.max
        DataLog.d("progress$progress", appTag)
        graphBuilder?.show(180 * progress)
    }
}