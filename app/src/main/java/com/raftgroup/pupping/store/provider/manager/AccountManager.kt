package com.raftgroup.pupping.store.provider.manager
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageLifecycleUser
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.PetData
import com.raftgroup.pupping.store.api.rest.UserData
import com.raftgroup.pupping.store.provider.model.ModifyPetProfileData
import com.raftgroup.pupping.store.provider.model.ModifyUserProfileData
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.raftgroup.pupping.store.provider.model.User
import com.skeleton.sns.SnsUser

class AccountManager(private val user: User) : PageLifecycleUser {

    private val appTag = javaClass.simpleName
    override fun setDefaultLifecycleOwner(owner: LifecycleOwner) {}
    override fun disposeDefaultLifecycleOwner(owner: LifecycleOwner) {}

    fun respondApi(res:ApiSuccess<ApiType>) :Boolean {
        when(res.type){
            ApiType.RegistPet ->{
                (res.data as? PetData)?.let{ user.registPetComplete(PetProfile().init(it, true))}
                return false
            }
            ApiType.DeletePet ->{
                user.deletePet(res.contentID.toInt())
                return false
            }
            ApiType.UpdatePet ->{
                (res.requestData as? ModifyPetProfileData )?.let{user.updatedPet(res.contentID.toInt(), it )}
                return false
            }
            ApiType.UpdatePetImage ->{
                (res.requestData as? Bitmap)?.let{ img ->
                    user.pets.value?.find { it.petId.toString() == res.contentID }?.let { pet->
                        pet.update(img)
                    }
                }
                return true
            }
            ApiType.UpdateUser ->{
                (res.requestData as? ModifyUserProfileData)?.let{ user.currentProfile.update(it) }
            }
            else ->{}
        }
        val requestUser = (res.requestData as? SnsUser) ?: return false
        if (requestUser.snsID != user.snsUser?.snsID) return false
        when(res.type){
            ApiType.GetUser ->{
                (res.data as? UserData)?.let{ user.setData(it) }
                return true
            }
            ApiType.GetPets ->{
                (res.data as? List<PetData>)?.let{ user.setData(it, isMyPet = true) }
                return true
            }
            else ->  return false
        }


    }
}