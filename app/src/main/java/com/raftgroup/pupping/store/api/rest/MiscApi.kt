package com.raftgroup.pupping.store.api.rest

import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiResponse
import retrofit2.http.*

interface MiscApi {
    @GET(Api.Misc.weather)
    suspend fun getWeather(
        @Query(ApiField.lat) lat: String?,
        @Query(ApiField.lng) lng: String?
    ): ApiResponse<WeatherData>?
}

data class WeatherData(
    @SerializedName("cityName") var cityName: String? = null,
    @SerializedName("temp") var temp: String? = null,
    @SerializedName("desc") var desc: String? = null,
    @SerializedName("iconId") var iconId: String? = null
)
