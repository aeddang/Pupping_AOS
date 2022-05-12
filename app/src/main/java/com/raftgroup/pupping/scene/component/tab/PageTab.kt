package com.raftgroup.pupping.scene.component.tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.lib.page.*
import com.raftgroup.pupping.databinding.CpPageTabBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PageTab : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName

    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider

    private lateinit var binding: CpPageTabBinding
    override fun init(context: Context) {
        binding = CpPageTabBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }
    fun setup(@StringRes titleRes:Int? = null, isBack:Boolean = false, isClose:Boolean = false, isSetting:Boolean = false,
              completionHandler:(() -> Unit)? = null){
        val str = if(titleRes!=null) context.getString(titleRes) else ""
        setup(str, isBack, isClose, isSetting, completionHandler)
    }
    fun setup(title:String = "", isBack:Boolean = false, isClose:Boolean = false, isSetting:Boolean = false,
              completionHandler:(() -> Unit)? = null){
        binding.textTitle.text = title
        if (isBack){
            binding.btnBack.setOnClickListener {
                completionHandler ?: pagePresenter.goBack()
                completionHandler?.invoke()
            }
            binding.btnBack.visibility = View.VISIBLE
        } else {
            binding.btnBack.visibility = View.GONE
        }
        if (isClose){
            binding.btnClose.setOnClickListener {
                completionHandler ?: pagePresenter.goBack()
                completionHandler?.invoke()
            }
            binding.btnClose.visibility = View.VISIBLE
        } else {
            binding.btnClose.visibility = View.GONE
        }
        if (isSetting){
            binding.btnSetting.setOnClickListener {
                //pagePresenter.goBack()
            }
            binding.btnSetting.visibility = View.VISIBLE
        } else {
            binding.btnSetting.visibility = View.GONE
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        binding.btnBack.setOnClickListener {
            pagePresenter.goBack()
        }
        binding.btnClose.setOnClickListener {
            pagePresenter.goBack()
        }
    }
}