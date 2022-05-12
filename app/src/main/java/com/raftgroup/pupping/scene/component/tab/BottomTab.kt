package com.raftgroup.pupping.scene.component.tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.lib.page.*
import com.lib.util.ComponentLog
import com.lib.util.Log
import com.lib.util.animateY
import com.raftgroup.pupping.databinding.CpBottomTapBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.skeleton.component.button.ImageTextButton
import dagger.hilt.android.AndroidEntryPoint


import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class BottomTab : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName

    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider

    private val pageIds = arrayOf(PageID.Home, PageID.Home,  PageID.Explore, PageID.My)
    private var btns:Array<ImageTextButton> = arrayOf()

    private lateinit var binding: CpBottomTapBinding
    override fun init(context: Context) {
        binding = CpBottomTapBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
        pagePresenter.observable.event.observe(owner, Observer{ evt:PageEvent? ->
            evt ?: return@Observer
            var delayShow:Job? = null
            when( evt.type ){
                PageEventType.WillChangePage ->{
                    val find = PageID.values().find { it.value == evt.id }
                    ComponentLog.d("find $find", appTag)
                    find ?: return@Observer
                    val page = evt.data as? PageObject
                    page?.isPopup?.let {isPop->
                        if (isPop) {
                            selectedCategory = -1
                            return@Observer
                        }
                    }
                    selectedCategory = floor(find.position.toDouble()/100.0).toInt() - 1
                }
                PageEventType.HideKeyboard -> {
                    delayShow?.cancel()
                    delayShow = scope.launch {
                        delay(100)
                        visibility = View.VISIBLE
                    }
                    ComponentLog.d("HIDE_KEY_BOARD", appTag)
                }
                PageEventType.ShowKeyboard -> {
                    delayShow?.cancel()
                    delayShow = null
                    visibility = View.GONE
                    ComponentLog.d("SHOW_KEY_BOARD", appTag)
                }
                else -> { }
            }
        })
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        btns = arrayOf(binding.btn0, binding.btn1, binding.btn2, binding.btn3)
        btns.forEachIndexed{idx,btn->
            btn.setOnClickListener {
                pagePresenter.changePage(pageProvider.getPageObject(pageIds[idx]))
            }
        }
        ComponentLog.d("onCoroutineScope ${btns.size}", appTag)
        val currentCategory = selectedCategory
        currentCategory ?: return
        if (currentCategory < 0) return
        if (currentCategory < btns.count()) {
            btns[currentCategory].selected = true
        }
    }

    private var selectedCategory:Int? = null
        set(value) {
            if( btns.isEmpty() ){
                ComponentLog.d("initate Category $value", appTag)
                field = value
                return
            }
            field?.let {
                if (it>=0 && it<btns.size) btns[it].selected = false
            }
            field = value
            ComponentLog.d("selectedCategory $field", appTag)
            value?.let {
                if (it>=0 && it<btns.size)  btns[it].selected = true
            }
        }



    private var isView = false
    fun viewTab() {
        if (isView) return
        isView = true
        Log.d(appTag, "viewTab")
        this.animateY(0, true).apply {
            interpolator = AccelerateInterpolator()
            startAnimation(this)
        }
    }
    fun hideTab() {
        if (! isView) return
        isView = false
        Log.d(appTag, "hideTab")
        this.animateY(- height, true).apply {
            interpolator = DecelerateInterpolator()
            startAnimation(this)
        }
    }

}