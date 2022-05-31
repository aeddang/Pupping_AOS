package com.raftgroup.pupping.scene.page.walk.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.floor

class Walk{
    var locations:List<LatLng> = arrayListOf()
    var playTime:Double = 0.0
    var playDistence:Double = 0.0
    var pictureUrl:String? = null
    fun point():Double {
        return 10.0 + floor(playDistence/1000f) *10.0
    }
}