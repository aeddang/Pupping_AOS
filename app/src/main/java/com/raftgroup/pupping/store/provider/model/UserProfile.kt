package com.raftgroup.pupping.store.provider.model

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lib.util.PageLog
import com.raftgroup.pupping.store.api.rest.UserData
import com.skeleton.sns.SnsType
import com.skeleton.sns.SnsUser
import java.util.*


data class ModifyUserProfileData (
    var image:Bitmap? = null,
    var nickName:String? = null
)


class UserProfile {
    var hashId:Int = UUID.randomUUID().hashCode()
    var id:String = UUID.randomUUID().toString(); private set
    var imagePath:String? = null; private set
    var image: MutableLiveData<Bitmap?> = MutableLiveData<Bitmap?>()
    var nickName:MutableLiveData<String?> = MutableLiveData<String?>()

    var email:MutableLiveData<String?> = MutableLiveData<String?>()
    var type:MutableLiveData<SnsType?> = MutableLiveData<SnsType?>()


    fun setData(data:SnsUser) : UserProfile{
        type.value = data.snsType
        return this
    }

    fun setData(data:UserData){
        PageLog.d(data, "UserProfileInfoDATA")
        nickName.value = data.name
        email.value = data.email
        imagePath = data.pictureUrl
        type.value = SnsType.getType(data.providerType)
        image.value = null
    }

    fun update(data:ModifyUserProfileData) : UserProfile{
        data.image?.let {
            image.value = it
        }
        data.nickName?.let {
            nickName.value = it
        }
        return this
    }


    fun update(btm:Bitmap?) : UserProfile{
        image.value = btm
        return this
    }

    fun removeObservers(owner: LifecycleOwner){
        image.removeObservers(owner)
        nickName.removeObservers(owner)
        type.removeObservers(owner)
        email.removeObservers(owner)
    }
}
