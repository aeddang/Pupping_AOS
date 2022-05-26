package com.raftgroup.pupping.scene.page.walk.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.page.PageViewModel
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPlayInfoBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.provider.DataProvider
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayInfo : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject
    lateinit var dataProvider: DataProvider
    private val appTag = javaClass.simpleName

    private lateinit var binding: CpPlayInfoBinding
    private var graphBuilder: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpPlayInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        graphBuilder = GraphBuilder(binding.graphArea, Graph.Type.HolizentalBar)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setColor(R.color.brand_primary)
            .setRange(1.0)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }
    override fun onPageViewModel(vm: PageViewModel): PageView {
        return super.onPageViewModel(vm)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        onStart()
    }

    private fun onStart(){
        binding.petList.removeAllViews()
        dataProvider.user.pets.value?.let { pets->
            pets.filter { it.isWith }.forEach {
                val pet = PlayProfileInfo(context)
                binding.petList.addView(pet, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
                pet.setData(it)
            }
        }
    }
}