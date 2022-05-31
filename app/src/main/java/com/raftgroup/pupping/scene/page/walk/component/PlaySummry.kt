package com.raftgroup.pupping.scene.page.walk.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap

import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.page.PageViewModel
import com.lib.util.cropCircle
import com.lib.util.secToMinString
import com.lib.util.size
import com.lib.util.toDecimal
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpGoogleMapBinding
import com.raftgroup.pupping.databinding.CpPlaySummryBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.walk.model.PlayWalkModel
import com.raftgroup.pupping.store.provider.model.User
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlaySummry : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    private val appTag = javaClass.simpleName

    private lateinit var binding: CpPlaySummryBinding
    private var graphBuilder: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpPlaySummryBinding.inflate(LayoutInflater.from(context), this, true)
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
        val owner = lifecycleOwner ?: return this
        val playWalkModel = vm as? PlayWalkModel ?: return this
        val m = context.getString(R.string.m)
        playWalkModel.playDistence.observe(owner) { des ->
            binding.textDistence.text = "${des.toDecimal(f=1)}$m"
        }
        playWalkModel.playTime.observe(owner) { t ->
            binding.textTime.text = t.toDouble().secToMinString()
        }
        playWalkModel.currentProgress.observe(owner) { progress ->
            graphBuilder?.show(progress)
        }
        return this
    }
}