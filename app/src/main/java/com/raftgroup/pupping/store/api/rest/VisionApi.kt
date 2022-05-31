package com.raftgroup.pupping.store.api.rest

import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface VisionApi {
    @Multipart
    @POST(Api.Vision.detect)
    suspend fun post(
        @Part contents: MultipartBody.Part?
    ): ApiResponse<DetectData>?
}

data class DetectData(
    @SerializedName("isDetected") var isDetected: Boolean? = null,
    @SerializedName("pictureUrl") var pictureUrl: String? = null
)
