package com.raftgroup.pupping.scene.page.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.UiSettings
import com.lib.page.*
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.PageRepository
import com.skeleton.sns.SnsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PageExplore : PageFragment(), PageRequestPermission{
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    @Inject lateinit var snsManager: SnsManager
    @Inject lateinit var repository: PageRepository
    private lateinit var binding: PageExploreBinding
    private var currentMap:MapView? = null
    override fun onViewBinding(): View {
        binding = PageExploreBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializaMap(view, savedInstanceState)
    }

    private fun initializaMap(rootView: View, savedInstanceState: Bundle?) {
        MapsInitializer.initialize(ctx)
        val mapView = binding.mapView
        currentMap = mapView
        mapView.onCreate(savedInstanceState)
        binding.mapView.alpha = 0f
    }

    override fun onPause() {
        currentMap?.onPause()
        super.onPause()
    }
    override fun onResume() {
        currentMap?.onResume()
        super.onResume()
    }
    override fun onDestroy() {
        currentMap?.onDestroy()
        currentMap = null
        super.onDestroy()
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        binding.btnTest.setOnClickListener {
            snsManager.requestAllLogOut()
            repository.clearLogin()
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

        pagePresenter.requestPermission(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), this)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
        super.onRequestPermissionResult(resultAll, permissions)
        if (!resultAll) return
        val mapView = binding.mapView

        mapView.getMapAsync { gm ->
            gm.mapType = GoogleMap.MAP_TYPE_NORMAL
            val mUiSettings: UiSettings = gm.uiSettings
            gm.isMyLocationEnabled = true
            gm.animateCamera(CameraUpdateFactory.zoomTo(7.0f))
            mUiSettings.isCompassEnabled = true
            mUiSettings.isMyLocationButtonEnabled = false
            mUiSettings.setAllGesturesEnabled(true)
        }
        binding.mapView.alpha = 1f
    }
}