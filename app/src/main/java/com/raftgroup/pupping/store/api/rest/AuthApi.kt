package com.raftgroup.pupping.store.api.rest

import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface AuthApi {
    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST(Api.Auth.Login)
    suspend fun post(
        @Body params: Map<String, String>
    ): ApiResponse<UserAuth>?


    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST(Api.Auth.Login)
    suspend fun reflash(
        @Body params: Map<String, String>
    ): ApiResponse<UserAuth>?


    /*
    @Headers("Content-Type: application/json")
    @POST(Api.Authentication.ACCOUNT)
    suspend fun postAccount(
        @Body params:Map<String, String>
    ): ApiResponse<Any?>?

    @DELETE(Api.Authentication.ACCOUNT)
    suspend fun deleteAccount(
    ): ApiResponse<Any?>?

    @Headers("Content-Type: application/json")
    @POST(Api.Authentication.SNS_LOGIN)
    suspend fun snsLogin(
        @Body params:Map<String, String>? = null
    ): ApiResponse<Account>?

    @Headers("Content-Type: application/json")
    @POST(Api.Authentication.LOGIN)
    suspend fun login(
        @Path(Api.ACTION) action: String,
        @Body params:Map<String, String>? = null
    ): ApiResponse<Account>?

    @JvmSuppressWildcards
    @Headers("Content-Type: application/json")
    @POST(Api.Authentication.REGISTER_PUSH_TOKEN)
    suspend fun postToken(
        @Body params:Map<String, Any>? = null
    ): ApiResponse<Any?>?

    @Multipart
    @PUT(Api.Authentication.ACCOUNT)
    suspend fun putProfile(
        @Part profilepicture: MultipartBody.Part?
    ): ApiResponse<Any?>?

    @Multipart
    @PUT(Api.Authentication.ACCOUNT)
    suspend fun putAccount(
        @Part("password") password: RequestBody?,
        @Part("nickname") nickname: RequestBody? = null,
        @Part("emailAddress") emailAddress: RequestBody? = null,
        @Part("profilepictureurl") profilepictureurl: RequestBody? = null,
        @Part("sex") sex: RequestBody? = null,
        @Part("skintype") skintype: RequestBody? = null,
        @Part("phone") phone: RequestBody? = null,
        @Part("address") address: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("isterms") isterms: RequestBody? = null,
        @Part("ismarketing") ismarketing: RequestBody? = null,
        @Part("ispersonalinfo") ispersonalinfo: RequestBody? = null,
        @Part("isover14") isover14: RequestBody? = null
    ): ApiResponse<Any?>?

     */
}


data class UserAuth(
    @SerializedName("token") var token: String? = null
)
