package com.raftgroup.pupping.store.provider.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lib.util.*
import com.raftgroup.pupping.R
import com.raftgroup.pupping.store.api.rest.PetData
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

data class ModifyPetProfileData (
    var image:Bitmap? = null,
    var nickName:String? = null,
    var species:String? = null,
    var gender:Gender? =null,
    var birth:LocalDate? = null,
    var microfin:String? = null,
    var neutralization:Boolean? = null,
    var distemper:Boolean? = null,
    var hepatitis:Boolean? = null,
    var parovirus:Boolean? = null,
    var rabies:Boolean? = null,
    var weight:Double? = null,
    var size:Double? = null
)

data class ModifyPlayData (
    val lv:Int,
    val expval:Double
)

class PetProfile {
    companion object{
        val expRange:Double = 100.0
        fun getStatusValue(profile:PetProfile?):List<String>? {
            profile ?: return null
            val status:ArrayList<String> = arrayListOf()
            if (profile.neutralization.value == true) {status.add("neutralization")}
            if (profile.distemper.value == true) {status.add("distemper")}
            if (profile.hepatitis.value == true) {status.add("hepatitis")}
            if (profile.parovirus.value == true) {status.add("parovirus")}
            if (profile.rabies.value == true) {status.add("rabies")}
            return status
        }

        fun getStatusValue(profile:ModifyPetProfileData?):List<String>?{
            profile ?: return null
            val status:ArrayList<String> = arrayListOf()
            if (profile.neutralization == true) {status.add("neutralization")}
            if (profile.distemper == true) {status.add("distemper")}
            if (profile.hepatitis == true) {status.add("hepatitis")}
            if (profile.parovirus == true) {status.add("parovirus")}
            if (profile.rabies == true) {status.add("rabies")}
            return status
        }
    }
    private val appTag = javaClass.simpleName
    var id:String = UUID.randomUUID().toString(); private set
    var petId:Int = 0; private set
    var imagePath:String? = null; private set
    val image:MutableLiveData<Bitmap?> = MutableLiveData<Bitmap?>(null)
    val nickName:MutableLiveData<String?> = MutableLiveData<String?>(null)
    val species:MutableLiveData<String?> = MutableLiveData<String?>(null)
    val gender:MutableLiveData<Gender?> = MutableLiveData<Gender?>(null)
    val birth:MutableLiveData<LocalDate?> = MutableLiveData<LocalDate?>(null)
    val exp:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    val lv:MutableLiveData<Int> = MutableLiveData<Int>(-1)
    val prevExp:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    val nextExp:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    val neutralization:MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val distemper:MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val hepatitis:MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val parovirus:MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val rabies:MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)

    val microfin: MutableLiveData<String?> = MutableLiveData<String?>(null)
    val weight:MutableLiveData<Double?> = MutableLiveData<Double?>(null)
    val size:MutableLiveData<Double?> = MutableLiveData<Double?>(null)

    var isEmpty:Boolean = false; private set
    var isMypet:Boolean = false; private set
    var totalExerciseDistance: Double? = null; private set
    var totalExerciseDuration: Double? = null; private set
    var totalMissionCount: Int? = null; private set
    var totalWalkCount: Int? = null; private set
    var isWith:Boolean = true

    override fun equals(other: Any?): Boolean {
        (other as? PetProfile)?.let {
            return this.id == it.id
        }
        return super.equals(other)
    }
    fun init(nickName:String?,species:String?, gender:Gender?, birth:LocalDate?) : PetProfile{
        this.nickName.value = nickName
        this.species.value = species
        this.gender.value = gender
        this.birth.value = birth
        this.isMypet = true
        return this
    }

    fun init(isMyPet:Boolean) : PetProfile{
        isMypet = isMyPet
        return this
    }
    fun init(data:PetData, isMyPet:Boolean): PetProfile{
        this.isMypet = isMyPet
        this.petId = data.petId ?: 0
        this.imagePath = data.pictureUrl
        this.nickName.value = data.name
        this.species.value = data.breed
        this.gender.value = Gender.getGender(data.sex)
        this.birth.value = data.birthdate?.toDate( "yyyy-MM-dd'T'HH:mm:ss")
        this.exp.value = (data.experience ?: 0.0).toDouble()
        this.microfin.value = data.regNumber
        this.weight.value = data.weight
        this.size.value = data.size
        this.neutralization.value = data.status?.contains("neutralization")
        this.distemper.value = data.status?.contains("distemper")
        this.hepatitis.value = data.status?.contains("hepatitis")
        this.parovirus.value = data.status?.contains("parovirus")
        this.rabies.value = data.status?.contains("rabies")
        this.totalExerciseDistance = data.exerciseDistance
        this.totalExerciseDuration = data.exerciseDuration
        this.totalWalkCount = data.walkCompleteCnt
        this.totalMissionCount = data.missionCompleteCnt
        this.updatedExp()
        return this
    }
    fun removeObservers(owner: LifecycleOwner){
        image.removeObservers(owner)
        nickName.removeObservers(owner)
        species.removeObservers(owner)
        gender.removeObservers(owner)
        birth.removeObservers(owner)
        exp.removeObservers(owner)
        lv.removeObservers(owner)
        prevExp.removeObservers(owner)
        nextExp.removeObservers(owner)
        neutralization.removeObservers(owner)
        distemper.removeObservers(owner)
        hepatitis.removeObservers(owner)
        parovirus.removeObservers(owner)
        rabies.removeObservers(owner)
        microfin.removeObservers(owner)
        weight.removeObservers(owner)
    }

    fun empty(context:Context) : PetProfile{
        this.isEmpty = true
        this.nickName.value = context.getString(R.string.alertNeedProfile)
        this.isMypet = true
        return this
    }

    fun update(data:ModifyPetProfileData) : PetProfile{
        data.image?.let { this.image.value = it }
        data.nickName?.let { this.nickName.value = it }
        data.species?.let { this.species.value = it }
        data.gender?.let { this.gender.value = it }
        data.microfin?.let { this.microfin.value = it }
        data.birth?.let { this.birth.value = it }
        data.neutralization?.let { this.neutralization.value = it }
        data.distemper?.let { this.distemper.value = it }
        data.hepatitis?.let { this.hepatitis.value = it }
        data.parovirus?.let { this.parovirus.value = it }
        data.rabies?.let { this.rabies.value = it }
        data.weight?.let { this.weight.value = it }
        data.size?.let { this.size.value = it }
        //ProfileCoreData().update(id: self.id, data: data)
        return this
    }

    fun update(image:Bitmap?) : PetProfile{
        this.image.value = image
        return this
    }

    fun update(exp:Double) : PetProfile{
        this.exp.value = this.exp.value?.plus(exp)
        updatedExp()
        return this
    }

    private fun updatedExp(){
        val willLv:Int = floor((exp.value ?: 0.0) / PetProfile.expRange).toInt() + 1
        if (willLv != this.lv.value) {
            this.lv.value = willLv
            updatedLv()
        }
    }
    private fun updatedLv(){
        lv.value?.let { lvValue ->
            this.prevExp.value = (lvValue-1) * expRange
            this.nextExp.value = lvValue * expRange
            DataLog.d("prevExp ${prevExp.value}" , appTag)
            DataLog.d("nextExp ${nextExp.value}" , appTag)
        }
        DataLog.d("lv  ${lv.value}" , appTag)
    }
}
