package com.raftgroup.pupping.store.api.rest

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.raftgroup.pupping.store.api.Api
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiResponse
import com.raftgroup.pupping.store.api.ApiValue
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

enum class AlbumCategory{
    Pet, User;
    fun getApiCode() : String {
        return when(this){
            AlbumCategory.Pet -> "Pet"
            AlbumCategory.User -> "User"
        }
    }
    companion object {
        fun getCategory(value :String?) : AlbumCategory?
        {
            return when(value){
                "Pet" -> AlbumCategory.Pet
                "User" -> AlbumCategory.User
                else -> null
            }
        }
    }
}

data class AlbumData(
    val type:AlbumCategory,
    val thumb:Bitmap?,
    val image:Bitmap?
)

interface AlbumApi {
    @GET(Api.Album.pictures)
    suspend fun get(
        @Query(ApiField.ownerId) ownerId: String,
        @Query(ApiField.pictureType) pictureType: String?,
        @Query(ApiField.page) page: String? = "0",
        @Query(ApiField.size) size: String? = ApiValue.PAGE_SIZE.toString()
    ): ApiResponse<PictureData>?

    @Multipart
    @POST(Api.Album.pictures)
    suspend fun post(
        @Part(ApiField.ownerId) name: RequestBody?,
        @Part(ApiField.pictureType) pictureType: RequestBody?,
        @Part smallContents: MultipartBody.Part?,
        @Part contents: MultipartBody.Part?
    ): ApiResponse<PictureData?>?

    @Headers("Content-Type: application/json;charset=UTF-8")
    @PUT(Api.Album.picturesThumbsup)
    @JvmSuppressWildcards
    suspend fun put(
        @Body params: Map<String, Any>
    ): ApiResponse<Any?>?

    @DELETE(Api.Album.pictures)
    suspend fun delete(
        @Query(ApiField.pictureIds) pictureIds: String,
    ): ApiResponse<Any?>?
}

data class PictureData (
    @SerializedName("pictureId") var pictureId: Int? = null,
    @SerializedName("pictureType") var pictureType: String? = null,
    @SerializedName("ownerId") var ownerId: String? = null,
    @SerializedName("pictureUrl") var pictureUrl: String? = null,
    @SerializedName("smallPictureUrl") var smallPictureUrl: String? = null,
    @SerializedName("thumbsupCount") var thumbsupCount: Double? = null,
    @SerializedName("isChecked") var isChecked: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null
)
