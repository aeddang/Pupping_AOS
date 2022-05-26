package com.raftgroup.pupping.scene.page.walk.model

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.lib.page.PageObject
import com.lib.util.DataLog
import com.raftgroup.pupping.R
import com.raftgroup.pupping.scene.page.viewmodel.BasePageViewModel
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.mission.Mission
import com.raftgroup.pupping.store.mission.MissionPlayType
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

enum class PlayWalkEventType {
    AccessDenied, Start, CompleteStep, Next ,Completed, Resume, Pause, ViewPoint
}
data class PlayWalkEvent(val type: PlayWalkEventType, val id: String = "", var data:Any? = null)

enum class PlayWalkStatus {
    Initate, Play, Stop, Complete
}
enum class PlayWalkType {
    Mission, Walk;
    fun getCloseMsg():Int {
        return when (this){
            PlayWalkType.Walk -> R.string.alertClosePlayWalk
            PlayWalkType.Mission -> R.string.alertClosePlayMission
        }
    }


}

data class PlayDestination (
    val place: Place,
    val location:LatLng,
    var isLast:Boolean = false
)

@SuppressLint("MissingPermission")
class PlayWalkModel(val type:PlayWalkType = PlayWalkType.Walk, repo: PageRepository): BasePageViewModel(repo){
    private val appTag = javaClass.simpleName
    private var locationObserver:FusedLocationProviderClient? = null
    var mission:Mission? = null; private set
    var startTime:Date = Date()
    val event = MutableLiveData<PlayWalkEvent?>()
    val status = MutableLiveData<PlayWalkStatus>(PlayWalkStatus.Initate)

    var playStep:Int = 0; private set
    var startLocation:LatLng? = null; private set
    var endLocation:LatLng ? = null; private set
    var destinations:MutableList<PlayDestination> = arrayListOf(); private set
    var destinationDistence:Double = 0.0; private set
    var currentDestination :PlayDestination? = null; private set

    val currentDistenceFromDestination = MutableLiveData<Double?>(null)
    val currentLocation = MutableLiveData<LatLng?>(null)
    val playTime = MutableLiveData<Long>(0L)
    val playDistence = MutableLiveData<Double>(0.0)
    val currentProgress = MutableLiveData<Double>(0.0)
    private val locationRequest =  LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onDestroyOwner(owner: LifecycleOwner, pageObject: PageObject?) {
        super.onDestroyOwner(owner, pageObject)
        event.removeObservers(owner)
        status.removeObservers(owner)
        currentDistenceFromDestination.removeObservers(owner)
        currentLocation.removeObservers(owner)
        playTime.removeObservers(owner)
        playDistence.removeObservers(owner)
        currentProgress.removeObservers(owner)
        locationObserver?.removeLocationUpdates(locationCallback)
        locationObserver = null
        mission = null
    }

    val locationCallback:LocationCallback = object : LocationCallback() {
        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
        }
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            p0?.lastLocation?.let { loc->
                if (event.value?.type == PlayWalkEventType.Start) event.value = PlayWalkEvent(PlayWalkEventType.Resume)
                currentLocation.value?.let { prev->
                    val results = FloatArray(0)
                    Location.distanceBetween(prev.latitude, prev.longitude, loc.latitude, loc.longitude, results)
                    playDistence.value = playDistence.value?.plus(results[0])
                }
                playTime.value = Date().time - startTime.time
                currentLocation.value = LatLng(loc.latitude, loc.longitude)
                currentProgress.value = progress
            }
        }
    }

    fun startMission(mission:Mission, locationObserver:FusedLocationProviderClient) {
        this.mission = mission
        mission.start?.let { start->
            mission.destination?.let {  end->

                val startLocation = start.latLng
                val endLocation = end.latLng
                val wayPointLocation = mission.waypoints.map{
                    PlayDestination(it,it.latLng)
                }
                destinations.add(PlayDestination(start,  startLocation))
                destinations.addAll(wayPointLocation)
                destinations.add(PlayDestination(end, endLocation, true ))
                this.startLocation = startLocation
                this.endLocation = endLocation

                DataLog.d("self.destinations ${this.destinations.size}" , appTag)
            }
        }
        this.start()
        this.locationObserver = locationObserver
        this.currentDistenceFromDestination.value = null
        locationObserver.requestLocationUpdates(this.locationRequest, locationCallback, Looper.getMainLooper())
    }


    fun startWalk(locationObserver:FusedLocationProviderClient) {
        this.start()
        this.locationObserver = locationObserver
        locationObserver.requestLocationUpdates(this.locationRequest, locationCallback, Looper.getMainLooper())
    }



    fun resumeWalk() {
        event.value = PlayWalkEvent(PlayWalkEventType.Resume)
        status.value = PlayWalkStatus.Play
        locationObserver?.requestLocationUpdates(this.locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun pauseWalk() {
        event.value = PlayWalkEvent(PlayWalkEventType.Pause)
        status.value = PlayWalkStatus.Stop
        locationObserver?.removeLocationUpdates(locationCallback)
    }

    fun toggleWalk() {
        if (status.value == PlayWalkStatus.Stop) {
            resumeWalk()
        } else {
            pauseWalk()
        }
    }

    private fun start(){
        startTime = Date()
        playStep = 0
        event.value = PlayWalkEvent(PlayWalkEventType.Start)
        status.value = PlayWalkStatus.Play
    }


    private val progress:Double
        get(){
            val mission = this.mission ?: return 0.0
            when (mission.playType) {
                MissionPlayType.Endurance, MissionPlayType.Speed -> {
                    val move = playDistence.value?.div(mission.totalDistence) ?: 0.0
                    if (move > 1.0) {
                        this.complete()
                    }
                    return move
                }
                else -> {
                    val destination = currentDestination?.location ?: return 0.0
                    val location = currentLocation.value ?: return 0.0
                    val results = FloatArray(0)
                    Location.distanceBetween(location.latitude, location.longitude, destination.latitude, destination.longitude, results)
                    val move = results[0].toDouble()
                    currentDistenceFromDestination.value = move
                    if (move < 5.0) {
                        next()
                    }
                    val pct = (destinationDistence - move)/destinationDistence
                    return min(pct, 1.0)
                }

            }
        }

    fun next(){
        if (destinations.isEmpty()) {return}
        event.value = PlayWalkEvent(PlayWalkEventType.CompleteStep, data = playStep)
        playStep += 1
        currentDistenceFromDestination.value = null
        DataLog.d("next $playStep", appTag)
        if (destinations.size <= playStep) {
            complete()
        } else {
            val start = currentDestination?.location ?: startLocation ?: return
            val current = destinations[playStep]
            currentDestination = current
            val results = FloatArray(0)
            Location.distanceBetween(current.location.latitude, current.location.longitude, start.latitude, start.longitude, results)
            destinationDistence = results[0].toDouble()
            event.value = PlayWalkEvent(PlayWalkEventType.Next, data = playStep)
        }
    }

    fun complete(){
        locationObserver?.removeLocationUpdates(locationCallback)
        status.value = PlayWalkStatus.Complete
        event.value = PlayWalkEvent(PlayWalkEventType.Completed)
    }


}

