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
import com.lib.util.size
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpGoogleMapBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.provider.model.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlayMap : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    private val appTag = javaClass.simpleName

    private lateinit var binding: CpGoogleMapBinding
    private var currentMap: GoogleMap? = null

    override fun init(context: Context) {
        binding = CpGoogleMapBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.mapView.onDestroy()
        currentMap = null
    }
    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }

    override fun onPageViewModel(vm: PageViewModel): PageView {
        return super.onPageViewModel(vm)
    }

    override fun onPagePause() {
        super.onPagePause()
        binding.mapView.onPause()
    }
    override fun onPageResume() {
        super.onPageResume()
        binding.mapView.onResume()
    }

    @SuppressLint("MissingPermission")
    fun onRequestPermissionResult(resultAll: Boolean, permissions: List<Boolean>?) {
        if (!resultAll) return
        binding.mapView.getMapAsync { gm ->
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
    fun initializaMap(rootView: View, savedInstanceState: Bundle?) {
        MapsInitializer.initialize(context)
        val mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        binding.mapView.alpha = 0f
        mapView.getMapAsync { map ->
            currentMap = map
        }
    }

    private var zoom:Float = 1F
    fun moveMe(zoom:Float? = null, location : Location? = null){
        zoom?.let {
            this.zoom = it
        }
        if (location == null) requestMyLocation() else moveMe(location)
    }

    @SuppressLint("MissingPermission")
    private fun requestMyLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location ?: return@addOnSuccessListener
            val update = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoom)
            moveMe(location)
        }
    }
    @SuppressLint("MissingPermission")
    private fun moveMe(location : Location){
        val update = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoom)
        currentMap?.moveCamera(update)
    }
    fun resetMarkers(){
        currentMap?.clear()
    }
    @SuppressLint("CheckResult")
    fun addMarkers(userDatas:List<User> ){

        val size = context.resources.getDimension(R.dimen.profile_light).roundToInt()
        val stroke = context.resources.getDimension(R.dimen.stroke_regular)
        val color = context.getColor(R.color.brand_primary)
        userDatas.forEach { user->
            val geo = user.finalGeo ?: return@forEach
            val loc = LatLng(geo.lat ?: 0.0, geo.lng ?: 0.0)
            var snippet = ""
            user.pets.value?.let {  pets->
                snippet = pets.filter { it.nickName.value != null }
                    .map { it.nickName.value!! }
                    .reduceIndexed { index, stack, current ->
                        if(index==0) current
                        else "$stack ,${current}"
                    }
            }
            val path = user.currentProfile.imagePath
            if (!path.isNullOrEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val iconimg = resource.size(size, size).cropCircle(stroke, color)
                            val icon = BitmapDescriptorFactory.fromBitmap(iconimg)
                            val mk = currentMap?.addMarker(
                                MarkerOptions()
                                    .position(loc)
                                    .title(user.currentProfile.nickName.value)
                                    .snippet(snippet)
                                    .icon(icon)
                                    .anchor(0.5f, 0.5f)
                                    .rotation(30.0f)

                            )
                            mk?.tag = user
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onLoadFailed(errorDrawable: Drawable?) {}
                    })
            }
        }
    }


}