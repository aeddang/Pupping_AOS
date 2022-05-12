package com.raftgroup.pupping.scene.page.my.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.lib.page.PageComponent
import com.raftgroup.pupping.databinding.CpSelectTabBinding
import com.raftgroup.pupping.scene.page.my.model.InputData

class SelectTab : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private lateinit var binding: CpSelectTabBinding
    override fun init(context: Context) {
        binding = CpSelectTabBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    fun setup(data: InputData, prefix:String = "", completionHandler:(Int) -> Unit){
        binding.textTitle.text = "$prefix${data.title}"
        data.tabs?.let{ tabs ->
            if (tabs.size != 2) return
            val dataA = tabs[0]
            val dataB = tabs[1]
            binding.btnA.setup(dataA.text, dataA.image, dataA.color)
            binding.btnB.setup(dataB.text, dataB.image, dataB.color)

            binding.btnA.setOnClickListener {
                binding.btnA.selected = true
                binding.btnB.selected = false
                completionHandler(0)
            }
            binding.btnB.setOnClickListener {
                binding.btnB.selected = true
                binding.btnA.selected = false
                completionHandler(1)
            }

        }
        if (data.selectedIdx == 0){
            binding.btnA.selected = true
        }
        if (data.selectedIdx == 1){
            binding.btnB.selected = true
        }

    }


}