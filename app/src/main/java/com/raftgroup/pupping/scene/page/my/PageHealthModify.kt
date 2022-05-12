package com.raftgroup.pupping.scene.page.my

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.DataLog
import com.lib.util.PageLog
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PageHealthModifyBinding
import com.raftgroup.pupping.databinding.PageProfileModifyBinding
import com.raftgroup.pupping.scene.page.my.component.*
import com.raftgroup.pupping.scene.page.my.model.InputData
import com.raftgroup.pupping.scene.page.my.model.InputDataType
import com.raftgroup.pupping.scene.page.my.model.RadioData
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.*
import com.skeleton.component.dialog.Alert
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PageHealthModify: PageFragment() {

    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding:PageHealthModifyBinding

    override fun onViewBinding(): View {
        binding = PageHealthModifyBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override val hasBackPressAction: Boolean
        get(){
            checkClose()
            return true
        }

    private fun checkClose(){
        Alert.Builder(pagePresenter.activity)
            .setSelectButtons()
            .setText(R.string.profileCancelConfirm)
            .onSelected {
                if (it == 0){
                    pagePresenter.closePopup(pageObject?.key)
                }
            }
            .show()
    }


    override fun onCoroutineScope() {
        super.onCoroutineScope()
        dataProvider.result.observe(this) { res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag + "contentID")
                if (res.contentID == currentProfile?.petId.toString()) {
                    when (res.type) {
                        ApiType.UpdatePet -> {
                            pagePresenter.closePopup(pageObject?.key)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    private var currentProfile:PetProfile? = null
    private var currentSize:Double = 0.0
    private var currentWeight:Double = 0.0
    private var currentName:String = ""
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf()
        }

    @Suppress("UNREACHABLE_CODE")
    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.data] as? PetProfile)?.let { profile->
            currentName = profile.nickName.value ?: ""
            currentWeight = profile.weight.value ?: 0.0
            currentSize = profile.size.value ?: 0.0
            currentProfile = profile
        }
        return super.onPageParams(params)
    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        val ctx = context ?: return
        binding.btnCancel.setOnClickListener { checkClose() }
        binding.btnModify.setOnClickListener {
            val modifyData = ModifyPetProfileData(
                weight = currentWeight,
                size = currentSize
            )
            dataProvider.requestData(ApiQ(appTag, ApiType.UpdatePet, contentID = currentProfile?.petId.toString(), requestData = modifyData))
        }


        binding.pageTab.setup(null, isBack = true){
            checkClose()
        }

        val prefix = "$currentName${ctx.getString(R.string.owner)} "
        binding.inputWeight.setup(
            InputData(
                InputDataType.Text,
                ctx.getString( R.string.profileRegistWeight ),
                placeHolder = ctx.getString( R.string.kg),
                keyboardType = InputType.TYPE_CLASS_NUMBER,
                inputValue = currentWeight.toString()
            ), prefix)
        { c, _ ->
            currentWeight = c.toDouble()
        }
        binding.inputSize.setup(
            InputData(
                InputDataType.Text,
                ctx.getString( R.string.profileRegistSize ),
                placeHolder = ctx.getString( R.string.m),
                keyboardType = InputType.TYPE_CLASS_NUMBER,
                inputValue = currentSize.toString()
            ), prefix)
        { c, _ ->
            currentSize = c.toDouble()
        }
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

    }
}