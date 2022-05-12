package com.raftgroup.pupping.store.api

import android.content.Context
import android.graphics.Bitmap
import com.lib.util.toFile
import com.lib.util.toFormatString
import com.raftgroup.pupping.BuildConfig
import com.raftgroup.pupping.store.api.rest.*
import com.raftgroup.pupping.store.provider.model.ModifyPetProfileData
import com.raftgroup.pupping.store.provider.model.ModifyUserProfileData
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.module.network.NetworkFactory
import com.skeleton.sns.SnsUser
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiBridge(
    private val context: Context,
    private val networkFactory: NetworkFactory,
    private val interceptor: ApiInterceptor
) {
    val auth: AuthApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        AuthApi::class.java)
    val user: UserApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        UserApi::class.java)

    private val pet: PetApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        PetApi::class.java)
    private val misc: MiscApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        MiscApi::class.java)

    private val mission: MissionApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        MissionApi::class.java)

    private val album: AlbumApi = networkFactory.getRetrofit(BuildConfig.APP_REST_ADDRESS, listOf( interceptor ) ).create(
        AlbumApi::class.java)


    @Suppress("UNCHECKED_CAST")
    fun getUpdateUserProfile(apiQ: ApiQ, snsUser:SnsUser?) = runBlocking {
        return@runBlocking when(apiQ.type){
            ApiType.AuthLogin -> auth.post(apiQ.body as Map<String, String>)
            ApiType.AuthReflash -> auth.reflash(apiQ.body as Map<String, String>)
            ApiType.GetUser -> user.get((apiQ.requestData as? SnsUser)?.snsID ?: "")
            ApiType.UpdateUser -> getUpdateUserProfile( snsUser?.snsID,  apiQ.requestData as? ModifyUserProfileData )
            ApiType.GetWeather -> misc.getWeather(apiQ.query?.get(ApiField.lat), apiQ.query?.get(ApiField.lng))
            ApiType.GetMission -> mission.getMissions(
                apiQ.query?.get(ApiField.userId), apiQ.query?.get(ApiField.petId), apiQ.query?.get(ApiField.missionCategory),
                apiQ.query?.get(ApiField.page) ?: "0", apiQ.query?.get(ApiField.size) ?: ApiValue.PAGE_SIZE.toString()
            )
            ApiType.SearchMission -> mission.getSearch(
                apiQ.query?.get(ApiField.searchType), apiQ.query?.get(ApiField.distance),
                apiQ.query?.get(ApiField.lat), apiQ.query?.get(ApiField.lng), apiQ.query?.get(ApiField.missionCategory),
                apiQ.query?.get(ApiField.page) ?: "0", apiQ.query?.get(ApiField.size) ?: ApiValue.PAGE_SIZE.toString()
            )
            ApiType.CompleteMission -> mission.post(apiQ.body as Map<String, Any>)
            ApiType.CompleteWalk -> mission.post(apiQ.body as Map<String, Any>)
            ApiType.GetMissionSummary -> mission.getSummary(apiQ.contentID)
            ApiType.GetPet -> pet.get(apiQ.contentID)
            ApiType.GetPets -> pet.getUserPets((apiQ.requestData as? SnsUser)?.snsID ?: "")
            ApiType.RegistPet -> getRegistPetProfile(snsUser?.snsID, apiQ.requestData as? PetProfile)
            ApiType.UpdatePetImage -> getUpdatePetProfile(apiQ.contentID, img = apiQ.requestData as? Bitmap)
            ApiType.UpdatePet -> getUpdatePetProfile(apiQ.contentID, profile = apiQ.requestData as? ModifyPetProfileData)
            ApiType.DeletePet -> pet.delete(apiQ.contentID)
            ApiType.GetAlbumPictures -> album.get(
                apiQ.contentID,
                apiQ.query?.get(ApiField.pictureType),
                apiQ.query?.get(ApiField.page) ?: "0",
                apiQ.query?.get(ApiField.size) ?: ApiValue.PAGE_SIZE.toString()
            )
            ApiType.RegistAlbumPicture -> getRegistAlbumPicture(apiQ.contentID, apiQ.requestData as? AlbumData)
            ApiType.UpdateAlbumPictures -> getUpdateLikeAlbumPicture(apiQ.contentID, apiQ.requestData as? Boolean)
            ApiType.DeleteAlbumPictures -> album.delete(apiQ.contentID)
        }
    }

    private fun getUpdateUserProfile(userId:String?, model:ModifyUserProfileData?) = runBlocking {
        var image: MultipartBody.Part? = null
        model?.image?.let {
            val file = it.toFile(context, "profileImage.jpg")
            val imgBody = RequestBody.create(MediaType.parse("image/jpeg"),file )
            image = MultipartBody.Part.createFormData("contents", file.name, imgBody)
        }
        val name: RequestBody? = getRequestBody(model?.nickName)
        user.put(userId ?: "", name, image)
    }

    private fun getUpdatePetProfile(petId:String, img: Bitmap?) = runBlocking {
        var image: MultipartBody.Part? = null
        img?.let {
            val file = it.toFile(context)
            val imgBody: RequestBody? = RequestBody.create(MediaType.parse("image/jpeg"),file )
            image = MultipartBody.Part.createFormData("contents", "profileImage.jpg" , imgBody)
        }
        pet.put(petId, contents = image)
    }

    private fun getUpdatePetProfile(petId:String, profile: ModifyPetProfileData?) = runBlocking {
        val name: RequestBody? = getRequestBody(profile?.nickName)
        val breed: RequestBody? = getRequestBody(profile?.species)
        val birthdate: RequestBody? = getRequestBody(profile?.birth?.toFormatString()?.substring(0, 19))
        val sex: RequestBody? = getRequestBody(profile?.gender?.apiDataKey())
        val regNumber: RequestBody? = getRequestBody(profile?.microfin)
        var status: RequestBody? = null
        PetProfile.getStatusValue(profile)?.let {
            status = if (it.isEmpty()) getRequestBody("") else getRequestBody(it.reduce { acc, s -> "$acc,$s" })
        }
        val weight: RequestBody? = getRequestBody(profile?.weight?.toString())
        val size: RequestBody? = getRequestBody(profile?.size?.toString())
        pet.put(petId, name, breed, birthdate, sex, regNumber, status=status, weight = weight, size = size)
    }

    private fun getRegistPetProfile(userId:String?, profile: PetProfile?) = runBlocking {
        val name: RequestBody? = getRequestBody(profile?.nickName?.value)
        val breed: RequestBody? = getRequestBody(profile?.species?.value)
        val birthdate: RequestBody? = getRequestBody(profile?.birth?.value?.toFormatString()?.substring(0, 19))
        val sex: RequestBody? = getRequestBody(profile?.gender?.value?.apiDataKey())
        val regNumber: RequestBody? = getRequestBody(profile?.microfin?.value)
        val level: RequestBody? = getRequestBody("1")
        var status: RequestBody? = null
        PetProfile.getStatusValue(profile)?.let {
            status = if (it.isEmpty()) getRequestBody("") else getRequestBody(it.reduce { acc, s -> "$acc,$s" })
        }

        var image: MultipartBody.Part? = null
        profile?.image?.value?.let {
            val file = it.toFile(context)
            val imgBody: RequestBody? = RequestBody.create(MediaType.parse("image/jpeg"),file )
            image = MultipartBody.Part.createFormData("contents", "profileImage.jpg" , imgBody)
        }
        pet.post(userId,name, breed, birthdate, sex, regNumber, level, status, image)
    }

    private fun getRegistAlbumPicture(ownerId:String?, albumData: AlbumData?) = runBlocking {
        val owner: RequestBody? = getRequestBody(ownerId)
        val type: RequestBody? = getRequestBody(albumData?.type?.getApiCode())
        var image: MultipartBody.Part? = null
        albumData?.image?.let {
            val file = it.toFile(context)
            val imgBody: RequestBody? = RequestBody.create(MediaType.parse("image/jpeg"),file )
            image = MultipartBody.Part.createFormData("contents", "albumImage.jpg" , imgBody)
        }

        var thumb: MultipartBody.Part? = null
        albumData?.thumb?.let {
            val file = it.toFile(context)
            val imgBody: RequestBody? = RequestBody.create(MediaType.parse("image/jpeg"),file )
            thumb = MultipartBody.Part.createFormData("smallContents", "thumbAlbumImage.jpg" , imgBody)
        }
        album.post(owner, type, thumb, image)
    }

    private fun getUpdateLikeAlbumPicture(id:String, isLike:Boolean?) = runBlocking {
        val param = java.util.HashMap<String, Any>()
        param["id"] = id.toIntOrNull() ?: 0
        param["isChecked"] = isLike ?: true
        val params = java.util.HashMap<String, Any>()
        params["items"] = arrayOf(param)
        album.put(params)
    }

    private  fun getRequestBody(value:String?):RequestBody?{
        value ?: return null
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }
}