package com.raftgroup.pupping.store.provider.model


import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.lib.util.DataLog
import com.raftgroup.pupping.R
import com.raftgroup.pupping.scene.component.list.History
import com.raftgroup.pupping.store.api.rest.GeoData
import com.raftgroup.pupping.store.api.rest.MissionData
import com.raftgroup.pupping.store.api.rest.PetData
import com.raftgroup.pupping.store.api.rest.UserData
import com.skeleton.sns.SnsType
import com.skeleton.sns.SnsUser
import java.util.*
import kotlin.collections.ArrayList

enum class Gender {
    Male, Female;
    @DrawableRes
    fun getIcon() : Int {
        return when(this) {
            Male -> R.drawable.ic_audiotrack_dark
            Female -> R.drawable.ic_audiotrack_dark //"Asset.icon.female"
        }
    }
    @StringRes
    fun getTitle():Int {
        return when(this) {
            Male -> R.string.male
            Female -> R.string.female
        }
    }
    fun getSimpleTitle():String {
        return when(this) {
            Male -> "Male"
            Female -> "Female"
        }
    }

    fun coreDataKey() : Int {
        return when(this) {
            Male -> 1
            Female -> 2
        }
    }
    fun apiDataKey() : String {
        return when(this) {
            Male -> "Male"
            Female -> "Female"
        }
    }

    companion object{
        fun getGender(value:Int) : Gender?{
            return when(value) {
                1  -> Gender.Male
                2 -> Gender.Female
                else -> null
            }
        }
        fun getGender(value:String?) : Gender?{
            return when(value) {
                "Male"  -> Gender.Male
                "Female" -> Gender.Female
                else -> null
            }
        }
    }
}

data class ModifyUserData (
    val point:Double? = null,
    var mission:Double? = null,
    var coin:Double? = null
)

class User {
    private val appTag = javaClass.simpleName
    var id:String = UUID.randomUUID().toString(); private set
    val point:MutableLiveData<Double> = MutableLiveData<Double>()
    val coin:MutableLiveData<Double> = MutableLiveData<Double>()
    val mission:MutableLiveData<Double> = MutableLiveData<Double>()

    val pets:MutableLiveData<List<PetProfile>> = MutableLiveData(arrayListOf())
    var currentProfile:UserProfile = UserProfile(); private set
    var currentPet:PetProfile? = null; private set
    var snsUser:SnsUser? = null; private set
    var recentMission:History? = null; private set
    var finalGeo:GeoData? = null; private set

    fun registUser(user:SnsUser){
        snsUser = user
    }
    fun clearUser(){
        snsUser = null
    }
    fun registUser(id:String?, token:String?, code:String?){
        DataLog.d("id " + (id ?: ""),appTag)
        DataLog.d("token " + (token ?: ""),appTag)
        DataLog.d("code " + (code ?: ""),appTag)
        val sndId = id ?: return
        val sndToken = token ?: return
        if (sndToken.isEmpty()) return
        if (sndId.isEmpty()) return
        val type = SnsType.getType(code) ?: return
        DataLog.d("user init " + (code ?: ""), appTag)
        snsUser = SnsUser(type, sndId, sndToken)
    }

    fun setData(data:UserData){
        point.value = data.point ?: 0.0
        currentProfile.setData(data)
    }

    fun setData(data:MissionData) : User {
        recentMission = History().setData(data)
        data.user?.let{
            setData(it)
        }
        data.pets?.let{
            setData(it, false)
        }
        val type = SnsType.getType(data.user?.providerType)
        val userId = data.user?.userId
        if (type != null && userId != null){
            snsUser = SnsUser(type, userId, "")
        }
        finalGeo = data.geos?.first()
        return this
    }
    /*
    fun missionCompleted(_ mission:Mission) {
        let point =  mission.lv.point()
        self.point += point
        self.mission += 1
        self.pets.filter{$0.isWith}.forEach{
            $0.update(exp: point)
        }
        UserCoreData().update(
            data: ModifyUserData(
                    point: self.point,
        mission: self.mission)
        )
    }
    */


    fun setData(data:List<PetData>, isMyPet:Boolean = true){
        val profiles = data.map{ PetProfile().init(it, isMyPet) }
        this.pets.value = profiles.toMutableList()
    }

    fun deletePet(petId:Int) {
        this.pets.value?.first { it.petId == petId }?.let{
            val newList = this.pets.value?.toMutableList()
            newList?.remove(it)
            this.pets.value = newList
        }
    }

    fun updatedPet(petId:Int, data:ModifyPetProfileData) {
        this.pets.value?.first { it.petId == petId }?.let{
            it.update(data)
        }
    }


    fun registPetComplete(profile:PetProfile)  {
        if(this.currentPet == null ) currentPet = profile
        this.pets.value?.let{
            val newList = this.pets.value?.toMutableList()
            newList?.add(profile)
            this.pets.value = newList
        }
    }

    fun getPet(id :String) : PetProfile? {
        return this.pets.value?.first{it.id == id}
    }

    fun addPet(profile:PetProfile) {
        this.pets.value?.let{
            val newList = this.pets.value?.toMutableList()
            newList?.add(profile)
            this.pets.value = newList
        }
    }
}