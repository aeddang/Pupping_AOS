package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetAboutBinding
import com.raftgroup.pupping.scene.component.button.CheckButton
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PetAbout : PageComponent, ListItem<PetProfile> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPetAboutBinding
    private var profileData:PetProfile? = null
    override fun init(context: Context) {
        binding = CpPetAboutBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.btnRecord.setOnClickListener {
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.HealthModify)
                    .addParam(PageParam.data, profileData)
            )
        }
        binding.titleRecord.setup{
            pagePresenter.openPopup(
                pageProvider.getPageObject(PageID.HealthModify)
                    .addParam(PageParam.data, profileData)
            )
        }
    }

    override fun setData(data: PetProfile, idx: Int) {
        profileData = data
        if (data.isMypet){
            if( data.size.value == null && data.weight.value == null ){
                binding.btnRecord.visibility = View.VISIBLE
                binding.recordBody.visibility = View.GONE
                binding.titleRecord.useButton = false
            } else {
                binding.recordWeight.value = "${(data.weight.value ?: 0)}${context.getString(R.string.kg)}"
                binding.recordSize.value = "${(data.size.value ?: 0)}${context.getString(R.string.m)}"
                binding.btnRecord.visibility = View.GONE
                binding.recordBody.visibility = View.VISIBLE
                binding.titleRecord.useButton = true
            }
        } else {
            binding.titleRecord.useButton = false
            binding.btnRecord.visibility = View.GONE
            if( data.size.value == null && data.weight.value == null ){
                binding.recordBody.visibility = View.GONE
            } else {
                binding.recordWeight.value = "${(data.weight.value ?: 0)}${context.getString(R.string.kg)}"
                binding.recordSize.value = "${(data.size.value ?: 0)}${context.getString(R.string.m)}"
                binding.recordBody.visibility = View.VISIBLE
            }
        }

        var hasVaccinate = false
        binding.vaccinateBody.removeAllViews()
        if ( data.neutralization.value == true ) {
            hasVaccinate = true
            addVaccinated(R.string.profileRegistParovirusVaccinated)
        }
        if ( data.distemper.value == true ) {
            hasVaccinate = true
            addVaccinated(R.string.profileRegistDistemperVaccinated)
        }
        if ( data.hepatitis.value == true ) {
            hasVaccinate = true
            addVaccinated(R.string.profileRegistHepatitisVaccinated)
        }
        if ( data.parovirus.value == true ) {
            hasVaccinate = true
            addVaccinated(R.string.profileRegistParovirusVaccinated)
        }
        if ( data.rabies.value == true ) {
            hasVaccinate = true
            addVaccinated(R.string.profileRegistRabiesVaccinated)
        }
        binding.vaccinateBody.visibility = if(hasVaccinate) View.VISIBLE else View.GONE
    }

    private fun addVaccinated(@StringRes str:Int){
        val check = CheckButton(context)
        binding.vaccinateBody.addView(check, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        check.text = context.getString(str)
        check.selected = true
    }




}