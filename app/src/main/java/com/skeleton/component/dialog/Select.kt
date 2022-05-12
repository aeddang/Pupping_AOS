package com.skeleton.component.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import com.lib.util.Log
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpSelectBinding
import com.skeleton.component.button.FillButton
import com.skeleton.component.button.PageButtonType


class Select(context: Context) : Dialog(context), View.OnClickListener {
    private val appTag = javaClass.simpleName
    private var btns: List<FillButton> = listOf()
    var buttons:Array<String> = arrayOf()
    var selectIdx:Int = -1
    private var selectListener:SelectListener? = null
    private lateinit var binding: CpSelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val windowlp = window?.attributes
        windowlp?.gravity = Gravity.BOTTOM
        windowlp?.horizontalMargin = 0f
        window?.attributes = windowlp
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        binding = CpSelectBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)


    }

    override fun show() {
        super.show()
        window ?: return
        buttons.let{ btnDatas->
            btns = btnDatas.mapIndexed{ idx, text ->
                val btn = FillButton(window!!.context)
                binding.body.addView(btn, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                btn.text = text
                btn.setOnClickListener(this)
                btn.setupButton(PageButtonType.UnderLine, idx == selectIdx)

            }
            btns.last().useLine = false
        }
    }


    override fun onClick(v: View) {
        (v as? Button)?.let { select ->
            val find = btns.map{it.getButton()}.indexOf(select)
            if (find != -1) selectListener?.onSelected(this, find)
        }
        dismiss()
    }

    interface SelectListener{
        fun onSelected(view: Select, idx:Int)
    }
    class Builder(val context: Context?) {
        private val dialog:Select? = if (context!= null) Select(context) else null

        fun setButtons(buttons: Array<String>?): Builder {
            buttons?.let{ dialog?.buttons = it }
            return this
        }
        fun setResButtons(@StringRes buttons:Array<Int>?): Builder {
            context ?: return this
            buttons?.let{ lists ->
                dialog?.buttons = lists.map { context.getString(it) }.toTypedArray()
            }
            return this
        }

        fun setSelected(idx: Int): Builder {
            dialog?.selectIdx = idx
            return this
        }

        fun onSelected(select: (Int) -> Unit): Builder {
            dialog?.selectListener = object :SelectListener {
                override fun onSelected(view: Select, idx: Int) {
                    Log.d("build", "onSelected $idx")
                    select(idx)
                }
            }
            return this
        }

        fun show(){
            dialog?.show()
        }
    }
}