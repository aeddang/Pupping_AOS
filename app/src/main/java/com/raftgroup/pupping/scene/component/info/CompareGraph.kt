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
import com.raftgroup.pupping.databinding.CpCompareGraphBinding
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class CompareGraphData(
    var me:CompareData?,
    var other:CompareData?,
    var title: SpannableString? = null
)

data class CompareData(
    var value:Double = 0.0,
    var max:Double = 7.0,
    var desc:SpannableString? = null,
)

@AndroidEntryPoint
class CompareGraph : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpCompareGraphBinding
    private var graphBuilderMe: GraphBuilder? = null
    private var graphBuilderOthers: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpCompareGraphBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        graphBuilderMe = GraphBuilder(binding.graphAreaMe, Graph.Type.HolizentalBar)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setColor(R.color.brand_primary, R.color.app_white)
            .setRange(1.0)
        graphBuilderOthers = GraphBuilder(binding.graphAreaOthers, Graph.Type.HolizentalBar)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setColor(R.color.app_grey, R.color.app_white)
            .setRange(1.0)

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)

    }

    @SuppressLint("SetTextI18n")
    fun setData(data: CompareGraphData){
        binding.textTitle.text = data.title
        data.me?.let { d->
            binding.textMe.text = d.desc
            setProgress(graphBuilderMe, d)
        }
        data.other?.let { d->
            binding.textOther.text = d.desc
            setProgress(graphBuilderOthers, d)
        }
    }

    private  fun setProgress( graphBuilder:GraphBuilder?, data: CompareData){
        graphBuilder ?: return
        val progress = data.value / data.max
        graphBuilder.show(progress)
    }
}