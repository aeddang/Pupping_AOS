package com.raftgroup.pupping.store.api.rest

import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface PetApi {
    @GET(Api.Pet.Pet)
    suspend fun get(
        @Path(Api.CONTENT_ID) contentID: String
    ): ApiResponse<PetData>?

    @GET(Api.Pet.Pets)
    suspend fun getUserPets(
        @Query(ApiField.userId) userId: String?
    ): ApiResponse<PetData>?

    @Multipart
    @POST(Api.Pet.Pets)
    suspend fun post(
        @Query(ApiField.userId) userId: String?,
        @Part(ApiField.name) name: RequestBody?,
        @Part(ApiField.breed) breed: RequestBody?,
        @Part(ApiField.birthdate) birthdate: RequestBody?,
        @Part(ApiField.sex) sex: RequestBody?,
        @Part(ApiField.regNumber) regNumber: RequestBody?,
        @Part(ApiField.level) level: RequestBody?,
        @Part(ApiField.status) status: RequestBody?,
        @Part contents: MultipartBody.Part?
    ): ApiResponse<PetData?>?

    @Multipart
    @PUT(Api.Pet.Pet)
    suspend fun put(
        @Path(Api.CONTENT_ID) contentID: String,
        @Part(ApiField.name) name: RequestBody? = null,
        @Part(ApiField.breed) breed: RequestBody? = null,
        @Part(ApiField.birthdate) birthdate: RequestBody? = null,
        @Part(ApiField.sex) sex: RequestBody? = null,
        @Part(ApiField.regNumber) regNumber: RequestBody? = null,
        @Part(ApiField.level) level: RequestBody? = null,
        @Part(ApiField.status) status: RequestBody? = null,
        @Part(ApiField.weight) weight: RequestBody? = null,
        @Part(ApiField.size) size: RequestBody? = null,
        @Part contents: MultipartBody.Part? = null
    ): ApiResponse<Any?>?

    @DELETE(Api.Pet.Pet)
    suspend fun delete(
        @Path(Api.CONTENT_ID) contentID: String
    ): ApiResponse<Any?>?
}
data class PetData (
    @SerializedName("petId") var petId: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("pictureUrl") var pictureUrl: String? = null,
    @SerializedName("breed") var breed: String? = null,
    @SerializedName("birthdate") var birthdate: String? = null,
    @SerializedName("sex") var sex: String? = null,
    @SerializedName("regNumber") var regNumber: String? = null,
    @SerializedName("level") var level: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("exerciseDistance") var exerciseDistance: Double? = null,
    @SerializedName("exerciseDuration") var exerciseDuration: Double? = null,
    @SerializedName("experience") var experience: Double? = null,
    @SerializedName("weight") var weight: Double? = null,
    @SerializedName("size") var size: Double? = null,
    @SerializedName("walkCompleteCnt") var walkCompleteCnt: Int? = null,
    @SerializedName("missionCompleteCnt") var missionCompleteCnt: Int? = null
)
