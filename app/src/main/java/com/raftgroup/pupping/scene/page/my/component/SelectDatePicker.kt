package com.raftgroup.pupping.scene.page.my.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.lib.page.PageComponent
import com.raftgroup.pupping.databinding.CpSelectDatePickerBinding
import com.raftgroup.pupping.scene.page.my.model.InputData
import java.time.LocalDate

class SelectDatePicker: PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpSelectDatePickerBinding
    override fun init(context: Context) {
        binding = CpSelectDatePickerBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    fun setup(data: InputData, prefix:String = "", completionHandler:(LocalDate) -> Unit){
        binding.textTitle.text = "$prefix${data.title}"
        data.selectedDate?.let {
            binding.picker.updateDate(it.year, it.monthValue-1, it.dayOfMonth)
        }
        binding.picker.setOnDateChangedListener { _, yy, MM, dd ->
            val select = LocalDate.of(yy, MM+1, dd)
            completionHandler(select)
        }
    }


}