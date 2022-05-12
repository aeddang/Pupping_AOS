package com.skeleton.component.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.lib.util.Log
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpAlertBinding
import com.skeleton.component.button.FillButton

class Alert(context: Context) : Dialog(context), View.OnClickListener {
    private val appTag = javaClass.simpleName
    private var title: String? = null
    private var text: String? = null
    private var subText: String? = null
    private var buttons: ArrayList<String>? = null
    lateinit var btns: Array<FillButton>
    private var selectListener:SelectListener? = null
    private lateinit var binding: CpAlertBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = CpAlertBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        btns = arrayOf(binding.btn1, binding.btn2)
        val windowlp = window?.attributes
        windowlp?.gravity = Gravity.CENTER
        windowlp?.horizontalMargin = 0f
        window?.attributes = windowlp
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        if (buttons == null) {
            buttons = arrayListOf(context.getString(R.string.confirm))
        }
    }

    override fun show() {
        super.show()
        binding.textTitle.visibility = View.GONE
        binding.textBody.visibility = View.VISIBLE
        binding.textSub.visibility = View.VISIBLE
        btns.forEach {
            it.setOnClickListener(this)
            it.selected = false
            it.visibility = View.GONE
        }
        title?.let {
            binding.textTitle.text = it
            binding.textTitle.visibility = View.VISIBLE
        }
        text?.let{
            binding.textBody.text = it
            binding.textBody.visibility = View.VISIBLE
        }
        subText?.let{
            binding.textSub.text = it
            binding.textSub.visibility = View.VISIBLE
        }
        buttons?.let{ btnDatas->
            btnDatas.forEachIndexed {idx, data ->
                if (idx == 0)  btns[idx].selected = true
                btns[idx].text = data
                btns[idx].visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        val find = btns.map { (it as FillButton).getButton() }.indexOf(v)
        Log.d(appTag, "onClick $find")
        selectListener?.onSelected(this, find)
        dismiss()
    }

    interface SelectListener{
        fun onSelected(view: Alert, idx:Int)
    }
    class Builder(val context: Context?){
        private val dialog:Alert? = if (context!= null) Alert(context) else null
        fun setTitle(text:String): Builder {
            dialog?.title = text
            return this
        }

        fun setTitle(@StringRes text:Int): Builder {
            dialog?.title = context?.getString(text)
            return this
        }
        fun setText(text:String): Builder {
            dialog?.text = text
            return this
        }
        fun setText(@StringRes text:Int): Builder {
            dialog?.text = context?.getString(text)
            return this
        }
        fun setSubText(text:String): Builder {
            dialog?.subText = text
            return this
        }
        fun setSubText(@StringRes text:Int): Builder {
            dialog?.subText = context?.getString(text)
            return this
        }
        fun setSelectButtons(): Builder {
            val ctx = dialog?.context ?: return this
            val confirm = ctx.getString(R.string.confirm)
            val cancel = ctx.getString(R.string.cancel)
            dialog.buttons = arrayListOf(confirm, cancel)
            return this
        }
        fun resetButtons(buttons: ArrayList<String>?): Builder {
            dialog?.buttons = buttons
            return this
        }
        fun addButton(button:String): Builder {
            dialog?.buttons?.add(button)
            return this
        }

        fun onSelected(select: (Int) -> Unit): Builder {
            dialog?.selectListener = object :SelectListener {
                override fun onSelected(view: Alert, idx: Int) {
                    Log.d("build", "onSelected $idx")
                    select(idx)
                }
            }
            return this
        }
        fun show() {
            dialog?.show()
        }
    }
}