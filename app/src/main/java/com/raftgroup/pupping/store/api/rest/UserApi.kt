package com.raftgroup.pupping.store.api.rest

import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserApi {
    @GET(Api.User.User)
    suspend fun get(
        @Path(Api.CONTENT_ID) contentID: String
    ): ApiResponse<UserData>?

    @Multipart
    @PUT(Api.User.User)
    suspend fun put(
        @Path(Api.CONTENT_ID) contentID: String,
        @Part("name") name: RequestBody? = null,
        @Part contents: MultipartBody.Part?
    ): ApiResponse<Any?>?
}

data class UserData(
    @SerializedName("token") var token: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("password") var password: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("pictureUrl") var pictureUrl: String? = null,
    @SerializedName("providerType") var providerType: String? = null,
    @SerializedName("roleType") var roleType: String? = null,
    @SerializedName("exerciseDuration") var exerciseDuration: Double? = null,
    @SerializedName("point") var point: Double? = null
)
