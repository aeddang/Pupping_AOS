package com.raftgroup.pupping.scene.component.tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import com.lib.page.*
import com.raftgroup.pupping.databinding.CpMenuTabBinding
import com.skeleton.component.button.FillButton


class MenuTab : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName

    private var btns:Array<FillButton> = arrayOf()

    private lateinit var binding: CpMenuTabBinding
    override fun init(context: Context) {
        binding = CpMenuTabBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        btns = arrayOf(binding.btn0, binding.btn1, binding.btn2, binding.btn3)
        btns.forEach{btn->
            btn.visibility = View.GONE
        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
    }

    fun setup(@StringRes titleRes:List<Int>, selectedIdx:Int = 0 , completionHandler:(Int) -> Unit){
        val count = btns.count()
        titleRes.forEachIndexed { index, res ->
            if (index < count ) {
                val btn = btns[index]
                val str = context.getString(res)
                btn.text = str
                btn.visibility = View.VISIBLE
                btn.setOnClickListener {
                    completionHandler(index)
                }
            }
        }
        this.selectedIdx = selectedIdx
    }

    var selectedIdx:Int? = null
        set(value) {
            field?.let {
                if (it>=0 && it<btns.size) btns[it].selected = false
            }
            field = value
            value?.let {
                if (it>=0 && it<btns.size)  btns[it].selected = true
            }
        }

}