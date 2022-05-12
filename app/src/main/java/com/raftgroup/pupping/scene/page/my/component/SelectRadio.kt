package com.raftgroup.pupping.scene.page.my.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.allViews
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.databinding.CpSelectRadioBinding
import com.raftgroup.pupping.databinding.CpSelectRadioItemBinding
import com.raftgroup.pupping.scene.page.my.model.InputData
import com.raftgroup.pupping.scene.page.my.model.RadioData


class SelectRadio : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private lateinit var binding: CpSelectRadioBinding
    override fun init(context: Context) {
        binding = CpSelectRadioBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    fun setup(data: InputData, prefix:String = "", completionHandler:(Int) -> Unit){
        binding.textTitle.text = "$prefix${data.title}"
        binding.body.removeAllViews()
        binding.btnCheckAll.setOnClickListener {
            binding.body.allViews.forEach { v->
                (v as? SelectRadioItem)?.let{ item->
                    item.selected = true
                    data.checks?.forEach { it.isCheck = true }
                    completionHandler(999)
                }
            }
        }
        data.checks?.let{ checks ->
            checks.forEachIndexed { index, d ->
                val item = SelectRadioItem(context)
                item.setData(d)
                item.setOnClickListener {
                    d.isCheck = !d.isCheck
                    item.selected = d.isCheck
                    completionHandler(index)
                }
                binding.body.addView(item)
            }
        }
    }

    inner class SelectRadioItem : PageComponent, ListItem<RadioData> {
        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        private val appTag = javaClass.simpleName

        private lateinit var binding: CpSelectRadioItemBinding

        override fun init(context: Context) {
            binding = CpSelectRadioItemBinding.inflate(LayoutInflater.from(context), this, true)
            super.init(context)
        }

        override fun setData(data: RadioData, idx: Int) {
            data.text?.let { binding.btnRadio.text = context.getString(it) }
            binding.btnRadio.selected = data.isCheck
        }
        override fun setOnClickListener(l: OnClickListener?) {
            binding.btnRadio.setOnClickListener(l)
        }
        var selected:Boolean? = null
            set(value) {
                field = value
                binding.body.selected = value
                binding.btnRadio.selected = value
            }

    }
}

