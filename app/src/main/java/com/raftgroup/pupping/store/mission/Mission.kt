package com.raftgroup.pupping.store.mission

import android.content.Context
import android.location.Location
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.raftgroup.pupping.R
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


enum class MissionType {
    Today, Event, Always;

    fun info(): String {
        return when (this) {
            Today -> "Today’s Mission"
            Event -> "Event!! Mission"
            Always -> "Any Time Mission"
        }
    }

    @ColorRes
    fun color(): Int {
        return when (this) {
            Today -> R.color.brand_primary
            Event -> R.color.brand_thirdly
            Always -> R.color.brand_secondary
        }
    }

    companion object {
        fun random(): MissionType {
            return MissionType.values().random()
        }
    }
}

enum class MissionPlayType {
    Nearby, Location, Speed, Endurance;
    @StringRes
    fun info() : Int {
        return when(this) {
            Nearby -> R.string.playAround
            Location-> R.string.playLocation
            Speed -> R.string.playSpeed
            Endurance-> R.string.playEndurance
        }
    }
    companion object {
        fun random() : MissionPlayType {
            return MissionPlayType.values().random()
        }
    }
}

enum class MissionLv{
    Lv1, Lv2, Lv3, Lv4;
    fun apiDataKey() : String {
        return when(this) {
            Lv1 -> "lv1"
            Lv2 -> "lv2"
            Lv3 -> "lv3"
            Lv4 -> "lv4"
        }
    }

    fun info() : String {
        return when(this) {
            Lv1 -> "Easy"
            Lv2 -> "Normal"
            Lv3 -> "Difficult"
            Lv4 -> "Very Difficult"
        }
    }
    @DrawableRes
    fun icon() : Int {
        return when(this) {
            Lv1 -> R.drawable.ic_easy
            Lv2 -> R.drawable.ic_normal
            Lv3 -> R.drawable.ic_hard
            Lv4 -> R.drawable.ic_hard
        }
    }

    @ColorRes
    fun color() : Int{
        return when(this) {
            Lv1 -> R.color.brand_secondary
            Lv2 -> R.color.brand_primary
            Lv3 -> R.color.brand_thirdly
            Lv4 -> R.color.brand_thirdly
        }
    }

    fun point() : Double{
        return when(this) {
            Lv1 -> 10.0
            Lv2 -> 20.0
            Lv3 -> 30.0
            Lv4 -> 50.0
        }
    }
    fun speed() : Double{ // meter per sec
        return when(this) {
            Lv1 -> 1000.0 / 3600.0
            Lv2 -> 1500.0 / 3600.0
            Lv3 -> 3000.0 / 3600.0
            Lv4 -> 5000.0 / 3600.0
        }
    }

    fun locationCount() : Int { // meter
        return when(this) {
            Lv1 -> 1
            Lv2 -> 2
            Lv3 -> 3
            Lv4 -> 4
        }
    }

    fun distance() : Double { // meter
        return when(this) {
            Lv1 -> 1000.0
            Lv2 -> 1500.0
            Lv3 -> 3000.0
            Lv4 -> 5000.0
        }
    }

    fun duration() : Double{ // sec
        return when(this) {
            Lv1 -> 30.0 * 60.0
            Lv2 -> 60.0 * 60.0
            Lv3 -> 90.0 * 60.0
            Lv4 -> 120.0 * 60.0
        }
    }

    companion object {
        fun getMissionLv(value :String?) : MissionLv?
        {
            return when(value) {
                "lv1" -> Lv1
                "lv2" -> Lv2
                "lv3" -> Lv3
                "lv4" -> Lv4
                else -> null
            }
        }
        fun random() : MissionLv
        {
            return MissionLv.values().random()
        }
    }
}


enum class MissionKeyword{
    Convenience, AnimalHospital, Mart;
    fun keyword() : String{
        return when(this){
            Convenience -> "편의점"
            AnimalHospital -> "동물병원"
            Mart -> "마트"
        }
    }
    companion object {
        fun random() : MissionKeyword
        {
            return MissionKeyword.values().random()
        }
    }
}


class Mission(
    val type:MissionType,
    val playType:MissionPlayType,
    val lv:MissionLv
){
    companion object {
        fun viewSpeed(ctx:Context, value :Double) : String
        {
            return String.format("%.1f", (value * 3600 / 1000)) + ctx.resources.getString(R.string.kmPerH)
        }
        fun viewDistence(ctx:Context, value :Double) : String
        {
            return String.format("%.1f", (value / 1000)) + ctx.resources.getString(R.string.km)
        }
        fun viewDuration(ctx:Context, value :Double) : String
        {
            return String.format("%.1f", (value / 60)) + ctx.resources.getString(R.string.min)
        }
    }

    val id:String = UUID.randomUUID().toString()
    // Use fields to define the data types to return.

    var description:String = ""; private set
    var summary:String = ""; private set
    var recommandPlaces:List<AutocompletePrediction> = arrayListOf(); private set
    var start:Place? = null; private set
    var destination:Place? = null; private set
    var waypoints:List<Place> = arrayListOf(); private set
    var startTime:Double = 0.0; private set
    var totalDistence:Double = 0.0; private set //miter
    var duration:Double = 0.0; private set //sec
    var speed:Double = 0.0; private set //meter per hour

    var isCompleted:Boolean = false; private set
    var playTime:Double = 0.0; private set
    var playDistence:Double = 0.0; private set
    var pictureUrl:String? = null


    fun completed(playTime:Double, playDistence:Double) {
        this.playTime = playTime
        this.playDistence = playDistence
        isCompleted = true
    }

    fun add(start:Place) : Mission {
        this.start = start
        return this
    }

    fun add(values:List<Place>) : Mission {
        val pickList = values.toMutableList()
        val pickedList:MutableList<Place> = arrayListOf()
        val len:Int = min(values.count(), lv.locationCount())
        for (i in 0 until len) {
            val pick = pickList.random()
            if (pickList.indexOf(pick) != -1) {
                pickList.remove(pick)
            }
            pickedList.add(pick)
        }
        pickedList.sortedBy {  it.latLng.latitude }
        this.destination = pickedList.last()
        this.waypoints = pickedList.dropLast(1)
        return this
    }

    fun addRecommandPlaces(values:List<AutocompletePrediction>) : Mission {
        this.recommandPlaces = values
        return this
    }

    fun viewSpeed(ctx:Context):String { return Mission.viewSpeed(ctx, speed) }
    fun viewDistence(ctx:Context):String { return Mission.viewDistence(ctx, totalDistence) }
    fun viewDuration(ctx:Context):String { return Mission.viewDuration(ctx, duration) }

    val allPoint:List<Place>; get(){
        val points:MutableList<Place> = arrayListOf()
        this.start?.let { points.add(it) }
        points.addAll(waypoints)
        this.destination?.let { points.add(it) }
        return points
    }

    fun build(ctx:Context) : Mission {
        start ?: return this
        val locale = Locale.getDefault().language
        if (this.destination != null) {
            var desc = ""
            var distence:Double = 0.0
            var prevLoc = start!!
            val way:String = " -> "
            waypoints.filter{it.name != null}.forEach{ waypoint ->
                val distance = FloatArray(1)
                Location.distanceBetween(
                    prevLoc.latLng?.latitude ?: 0.0, prevLoc.latLng?.longitude ?: 0.0,
                    waypoint.latLng?.latitude ?: 0.0, waypoint.latLng?.longitude ?: 0.0, distance
                )
                distence += distance[0]
                desc = "$desc${waypoint.name}$way"
                prevLoc = waypoint
            }
            val distance = FloatArray(1)
            Location.distanceBetween(
                prevLoc.latLng?.latitude ?: 0.0, prevLoc.latLng?.longitude ?: 0.0,
                destination!!.latLng?.latitude ?: 0.0, destination!!.latLng?.longitude ?: 0.0, distance
            )
            distence += distance[0]
            speed = lv.speed()
            totalDistence = distence
            duration = ceil(distence/speed)
            if ( desc.isNotEmpty() ) {
                desc.removeSuffix(way)
                val trailing = destination?.name ?: ""
                description = if (locale == "ko") {
                    "$desc\n를(을) 경유하여 ${viewDuration(ctx)} 안에\n${trailing}로(으로) 이동"
                } else {
                    "Move $trailing in ${viewDuration(ctx)} via\n$desc"
                }
            } else {
                description = if (locale == "ko") {
                    "${viewDuration(ctx)} 안에\n${destination!!.name}로(으로) 이동"
                } else {
                    "Move ${destination!!.name}\nwithin ${viewDuration(ctx)}"
                }
            }
            summary = if (locale == "ko") {
                "${viewDuration(ctx)} 안에 ${destination!!.name}로(으로) 이동"
            } else {
                "Move ${destination!!.name} within ${viewDuration(ctx)}"
            }
        } else {
            when(playType) {
                MissionPlayType.Endurance -> {
                    totalDistence = lv.distance()
                    duration = lv.duration ()
                    speed = ceil (totalDistence / duration)
                    description = if (locale == "ko") {
                        "${viewDuration(ctx)} 동안 ${viewDistence(ctx)} 이상 이동"
                    } else {
                        "Move for ${viewDuration(ctx)}, over ${viewDistence(ctx)}"
                    }
                }

                MissionPlayType.Speed -> {
                    totalDistence = MissionLv.Lv1.distance()
                    speed =lv.speed()
                    duration = ceil(totalDistence/speed)
                    description = if (locale == "ko") {
                        "${viewSpeed(ctx)} 이상 속도로 ${viewDistence(ctx)} 이동"
                    } else {
                        "${viewDistence(ctx)} moves at a speed of ${viewSpeed(ctx)}"
                    }
                }
                else -> {}
            }
            summary =  description
        }
        return this
    }
}



