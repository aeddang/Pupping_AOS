package com.raftgroup.pupping.scene.page.walk.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.page.PageViewModel
import com.lib.util.secToMinString
import com.lib.util.toDecimal
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPlayInfoBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkEventType
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkModel
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkStatus
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkType
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
        super.onPageViewModel(vm)
        (vm as? PlayWalkModel)?.let {  playWalkModel ->
            when (playWalkModel.type){
                PlayWalkType.Walk -> setupWalk(playWalkModel)
                PlayWalkType.Mission -> setupMission(playWalkModel)
            }
        }
        return this
    }
    private fun setupWalk(playWalkModel:PlayWalkModel){
        val owner = lifecycleOwner ?: return
        binding.locationBox.visibility = View.GONE
        binding.graphArea.visibility = View.GONE
        binding.btnA.setOnClickListener {
            playWalkModel.toggleWalk()
        }
        binding.btnB.text = context.getString(R.string.btnStop)
        binding.btnB.setOnClickListener {
            playWalkModel.complete()
        }
        playWalkModel.status.observe(owner){ status->
            updatePlayStatus(status)
        }
        playWalkModel.status.value?.let {
            updatePlayStatus(it)
        }
        playWalkModel.event.observe(owner){

        }
        val m = context.getString(R.string.m)
        playWalkModel.playDistence.observe(owner) { des ->
            binding.textDistence.text = "${des.toDecimal(f=1)}$m"
        }
        playWalkModel.playTime.observe(owner) { t ->
            binding.textTime.text = t.toDouble().secToMinString()
        }
    }
    private fun updatePlayStatus(status:PlayWalkStatus){
        when (status){
            PlayWalkStatus.Play -> {
                binding.btnA.defaultImageRes = R.drawable.ic_pause
                binding.btnA.text = context.getString(R.string.btnPause)
                binding.btnA.selected = false
            }
            else ->{
                binding.btnA.defaultImageRes = R.drawable.ic_resume
                binding.btnA.text = context.getString(R.string.btnResume)
                binding.btnA.selected = false
            }
        }
    }

    private fun setupMission(playWalkModel:PlayWalkModel){
        val owner = lifecycleOwner ?: return
        binding.btnA.text = context.getString(R.string.btnNext)
        binding.btnA.setOnClickListener {
            playWalkModel.next()
        }
        binding.btnB.text = context.getString(R.string.btnComplete)
        binding.btnB.setOnClickListener {
            playWalkModel.complete()
        }
        playWalkModel.status.observe(owner){ status->
            when (status){
                PlayWalkStatus.Play -> {}
                else ->{}
            }
        }
        playWalkModel.event.observe(owner){ evt->
            when (evt?.type){
                PlayWalkEventType.Start -> onStart()
                PlayWalkEventType.Next -> onNext(playWalkModel)
                else ->{

                }
            }
        }
        val m = context.getString(R.string.m)
        playWalkModel.playDistence.observe(owner) { des ->
            binding.textDistence.text = "${des.toDecimal(f=1)}$m"
        }
        playWalkModel.playTime.observe(owner) { t ->
            binding.textTime.text = t.toDouble().secToMinString()
        }
        playWalkModel.currentDistenceFromDestination.observe(owner) { des ->
            des ?: return@observe
            binding.textLocationDistence.text = "${des.toDecimal(f=1)}$m"
        }
        playWalkModel.currentProgress.observe(owner) { progress ->
           graphBuilder?.show(progress)
        }

    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
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
    private fun onNext(playWalkModel:PlayWalkModel){
        playWalkModel.currentDestination?.let {
            binding.textLocation.text = it.place.name
        }


    }
}