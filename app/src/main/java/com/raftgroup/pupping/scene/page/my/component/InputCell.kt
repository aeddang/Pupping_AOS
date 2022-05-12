package com.raftgroup.pupping.scene.page.my.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleOwner
import com.lib.module.SoftKeyboard
import com.lib.page.PageComponent
import com.lib.util.ComponentLog
import com.lib.util.limitLength
import com.raftgroup.pupping.databinding.CpInputCellBinding
import com.raftgroup.pupping.scene.page.my.model.InputData


class InputCell:PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    private lateinit var binding: CpInputCellBinding

    override fun init(context: Context) {
        binding = CpInputCellBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    var title:String? = null
        set(value) {
            field = value
            binding.textTitle.text = value
        }



    fun setup(data:InputData, prefix:String = "", completionHandler:(String, Boolean) -> Unit){
        binding.textTitle.text = "$prefix${data.title}"
        binding.textInput.hint = data.placeHolder
        binding.textInput.inputType = data.keyboardType
        binding.textInput.limitLength(data.inputMax)
        binding.textInput.setText(data.inputValue)
        modifyData(data)
        binding.textInput.setOnFocusChangeListener { _, b ->
            binding.input.selected = b
        }
        binding.textInput.addTextChangedListener( object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                ComponentLog.d(p0 ?: "", appTag)
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                completionHandler((p0 ?: "").toString(), false)
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.textInput.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER ) {
                completionHandler(binding.textInput.text.toString() , true)
            }
            return@setOnKeyListener false
        }


    }
    fun modifyData(data:InputData){
        binding.textInfo.text = data.info
        binding.textInfo.visibility = if (data.info == null || data.info == "") View.GONE else View.VISIBLE
        binding.textTip.text = data.tip

    }
    fun showTip(isShow:Boolean){
        binding.tip.visibility = if (isShow) View.GONE else View.VISIBLE
    }

}