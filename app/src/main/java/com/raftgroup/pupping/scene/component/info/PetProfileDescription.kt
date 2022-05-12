package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageUI
import com.raftgroup.pupping.databinding.CpPetProfileDescriptionBinding
import com.raftgroup.pupping.store.api.rest.PetData
import com.raftgroup.pupping.store.provider.model.Gender
import com.raftgroup.pupping.store.provider.model.PetProfile


class PetProfileDescription : PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpPetProfileDescriptionBinding

    override fun onInit() {
        super.onInit()
        binding = CpPetProfileDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setData(gender:Gender?, age:String?, species:String?){
        if(gender != null) {
            binding.textGender.text = gender.getSimpleTitle()
            binding.textGender.visibility = View.VISIBLE
        } else {
            binding.textGender.visibility = View.GONE
        }
        if(age != null) {
            binding.textAge.text = age
            binding.textAge.visibility = View.VISIBLE
            binding.dotAge.visibility = View.VISIBLE
        } else {
            binding.textAge.visibility = View.GONE
            binding.dotAge.visibility = View.GONE
        }
        if(species != null) {
            binding.textSpecies.text = species
            binding.textSpecies.visibility = View.VISIBLE
            binding.dotSpecies.visibility = View.VISIBLE
        } else {
            binding.textSpecies.visibility = View.GONE
            binding.dotSpecies.visibility = View.GONE
        }
    }

    fun setSpecies(species:String?){
        if(species != null) {
            binding.textSpecies.text = species
            binding.textSpecies.visibility = View.VISIBLE
            binding.dotSpecies.visibility = View.VISIBLE
        } else {
            binding.textSpecies.visibility = View.GONE
            binding.dotSpecies.visibility = View.GONE
        }
    }
}
