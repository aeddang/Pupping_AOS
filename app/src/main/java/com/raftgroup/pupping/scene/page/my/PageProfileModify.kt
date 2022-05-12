package com.raftgroup.pupping.scene.page.my

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.DataLog
import com.lib.util.PageLog
import com.raftgroup.pupping.R
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
class PageProfileModify: PageFragment() {

    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var repository: PageRepository
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding:PageProfileModifyBinding

    override fun onViewBinding(): View {
        binding = PageProfileModifyBinding.inflate(LayoutInflater.from(context))
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
    private var currentName:String = ""
    private var currentSpecies:String = ""
    private var currentMicrofin:String = ""
    private lateinit var selectVaccinate:InputData

    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf()
        }

    @Suppress("UNREACHABLE_CODE")
    override fun onPageParams(params: Map<String, Any?>): PageView {
        (params[PageParam.data] as? PetProfile)?.let { profile->
            currentName = profile.nickName.value ?: ""
            currentSpecies = profile.species.value ?: ""
            currentMicrofin = profile.microfin.value ?: ""
            currentProfile = profile
        }
        return super.onPageParams(params)
    }

    override fun onGlobalLayout() {
        super.onGlobalLayout()
        val ctx = context ?: return
        binding.btnCancel.setOnClickListener { checkClose() }
        binding.btnModify.setOnClickListener {
            val checks = selectVaccinate.checks ?: return@setOnClickListener
            val modifyData = ModifyPetProfileData(
                nickName = currentName,
                species = currentSpecies,
                microfin = currentMicrofin,
                neutralization = checks[0].isCheck,
                distemper = checks[1].isCheck,
                hepatitis = checks[2].isCheck,
                parovirus = checks[3].isCheck,
                rabies = checks[4].isCheck
            )
            dataProvider.requestData(ApiQ(appTag, ApiType.UpdatePet, contentID = currentProfile?.petId.toString(), requestData = modifyData))
        }
        selectVaccinate = InputData(
            InputDataType.Radio,
            ctx.getString( R.string.profileRegistHealth ),
            checks = arrayListOf(
                RadioData(currentProfile?.neutralization?.value ?: false, R.string.profileRegistNeutralized),
                RadioData(currentProfile?.distemper?.value ?: false, R.string.profileRegistDistemperVaccinated),
                RadioData(currentProfile?.hepatitis?.value ?: false, R.string.profileRegistHepatitisVaccinated),
                RadioData(currentProfile?.parovirus?.value ?: false, R.string.profileRegistParovirusVaccinated),
                RadioData(currentProfile?.rabies?.value ?: false, R.string.profileRegistRabiesVaccinated)
            )
        )

        binding.pageTab.setup(null, isBack = true){
            checkClose()
        }
        binding.inputName.setup(
            InputData(
                InputDataType.Text,
                ctx.getString( R.string.profileRegistName ),
                ctx.getString( R.string.profileRegistNameTip ),
                placeHolder = ctx.getString( R.string.profileNamePlaceHolder),
                inputValue = currentName
            ))
        { c, _ ->
            currentName = c
            val fullTitle = "$c${ctx.getString(R.string.owner)} "
            binding.inputSpecies.title = "$fullTitle${ctx.getString( R.string.profileRegistSpecies )}"
            binding.inputMicrofin.title = "$fullTitle${ctx.getString( R.string.profileRegistMicroFin )}"

        }
        val prefix = "$currentName${ctx.getString(R.string.owner)} "
        binding.inputSpecies.setup(
            InputData(
                InputDataType.Text,
                ctx.getString( R.string.profileRegistSpecies ),
                ctx.getString( R.string.profileRegistNameTip ),
                placeHolder = ctx.getString( R.string.profileSpeciesPlaceHolder),
                inputValue = currentSpecies
            ), prefix)
        { c, _ ->
            currentSpecies = c
        }
        binding.inputMicrofin.setup(
            InputData(
                InputDataType.Text,
                ctx.getString( R.string.profileRegistMicroFin ),
                info=ctx.getString( R.string.profileRegistMicroFinInfo ),
                placeHolder = ctx.getString( R.string.profileMicroFinPlaceHolder),
                inputValue = currentMicrofin
            ), prefix)
        { c, _ ->
            currentMicrofin = c
        }
        binding.selectVaccinate.setup(selectVaccinate){

        }

    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()

    }
}