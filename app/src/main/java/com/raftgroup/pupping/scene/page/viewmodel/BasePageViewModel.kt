package com.raftgroup.pupping.scene.page.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lib.page.*
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.PageRepository
import com.skeleton.module.Repository
/*
enum class ViewModelEventType{
}
data class ViewModelEvent(val type: ViewModelEventType, val id: String = "", var data:Any? = null)
*/
open class BasePageViewModel(val repo: PageRepository) : ViewModel(), PageViewModel {
    override val repository: Repository get() = repo
    override val observable: PageAppViewModel get() = repo.pagePresenter.observable
    override val presenter:PagePresenter get() = repo.pagePresenter
    val pageProvider :FragmentProvider get() = repo.pageProvider
    val dataProvider : DataProvider get() = repo.dataProvider

    var owner: LifecycleOwner? = null; protected set

    @CallSuper
    override fun onCreateView(owner: LifecycleOwner, pageObject: PageObject?) {
        this.owner = owner
        repo.clearEvent()
    }

    @CallSuper
    override fun onDestroyView(owner: LifecycleOwner , pageObject: PageObject?) {
        if(this.owner != owner) return
        repository.disposeLifecycleOwner(owner)
        this.owner = null
        onDestroyOwner(owner, pageObject)
    }

    protected open fun onDestroyOwner(owner: LifecycleOwner , pageObject: PageObject?) {}

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        this.owner = null
    }


}