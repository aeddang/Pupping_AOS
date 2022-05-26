package com.raftgroup.pupping.store.api.rest

import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.R
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiResponse
import com.raftgroup.pupping.store.api.ApiValue
import retrofit2.http.*

enum class MissionCategory {
    Walk, Mission, All;
    fun getApiCode():String {
        return when (this){
            MissionCategory.Walk -> "Walk"
            MissionCategory.Mission -> "Mission"
            MissionCategory.All -> "All"
        }
    }

    fun getView():Int? {
        return when (this){
            MissionCategory.Walk -> R.string.walk
            MissionCategory.Mission -> R.string.mission
            MissionCategory.All -> null
        }
    }
    companion object {
        fun getCategory(value:String?) : MissionCategory {
            return when (value){
                "Walk" -> MissionCategory.Walk
                "Mission" -> MissionCategory.Mission
                else -> MissionCategory.All
            }
        }
    }
}

enum class MissionSearchType{
    Distance, Time, Random, User;
    fun getApiCode():String {
        return when (this){
            MissionSearchType.Distance -> "Distance"
            MissionSearchType.Time -> "Time"
            MissionSearchType.Random -> "Random"
            MissionSearchType.User -> "User"
        }
    }
}

interface MissionApi {

    @GET(Api.Mission.missions)
    suspend fun getMissions(
        @Query(ApiField.userId) userId: String?,
        @Query(ApiField.petId) petId: String?,
        @Query(ApiField.missionCategory) missionCategory: String?,
        @Query(ApiField.page) page: String? = "0",
        @Query(ApiField.size) size: String? = ApiValue.PAGE_SIZE.toString()
    ): ApiResponse<MissionData>?

    @GET(Api.Mission.search)
    suspend fun getSearch(
        @Query(ApiField.searchType) searchType: String?,
        @Query(ApiField.distance) distance: String?,
        @Query(ApiField.lat) lat: String?,
        @Query(ApiField.lng) lng: String?,
        @Query(ApiField.missionCategory) missionCategory: String?,
        @Query(ApiField.page) page: String? = "0",
        @Query(ApiField.size) size: String? = ApiValue.PAGE_SIZE.toString()
    ): ApiResponse<MissionData>?



    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST(Api.Mission.missions)
    suspend fun post(
        @Body params: Map<String, Any>
    ): ApiResponse<MissionData>?

    @GET(Api.Mission.summary)
    suspend fun getSummary(
        @Query(ApiField.petId) petId: String?
    ): ApiResponse<MissionSummary>?
}

data class MissionData (
    @SerializedName("token") var token: String? = null,
    @SerializedName("missionId") var missionId: Int? = null,
    @SerializedName("missionCategory") var missionCategory: String? = null,
    @SerializedName("missionType") var missionType: String? = null,
    @SerializedName("difficulty") var difficulty: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("pictureUrl") var pictureUrl: String? = null,
    @SerializedName("duration") var duration: Double? = null,
    @SerializedName("distance") var distance: Double? = null,
    @SerializedName("point") var point: Double? = null,
    @SerializedName("user") var user: UserData? = null,
    @SerializedName("geos") var geos: ArrayList<GeoData>? = null,
    @SerializedName("pets") var pets: ArrayList<PetData>? = null
)

data class GeoData(
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("lng") var lng: Double? = null
)

data class MissionSummary (
    @SerializedName("totalDuration") var totalDuration: Double? = null,
    @SerializedName("totalDistance") var totalDistance: Double? = null,
    @SerializedName("weeklyReport") var weeklyReport: MissionReport? = null,
    @SerializedName("monthlyReport") var monthlyReport: MissionReport? = null
)

data class MissionReport (
    @SerializedName("totalMissionCount") var totalMissionCount: Double? = null,
    @SerializedName("avgMissionCount") var avgMissionCount: Double? = null,
    @SerializedName("missionTimes") var missionTimes: ArrayList<MissionTime>? = null
)

data class MissionTime (
    @SerializedName("d") var d: String? = null,
    @SerializedName("v") var v: Double? = null
)