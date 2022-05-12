package com.lib.module

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData


class SoftKeyboard(
    private val inputMethodManager: InputMethodManager?,
    private val view: ViewGroup? = null)
    : View.OnFocusChangeListener , TextWatcher {

    private val editTextList = ArrayList<EditText>()
    var isKeyboardShow:MutableLiveData<Boolean> = MutableLiveData( false );  private set
    var changedText:MutableLiveData<String?> = MutableLiveData( null );  private set
    private var focusView:View? = null
    private val appTag = javaClass.simpleName
    init {
        initEditTexts(view)
    }


    private fun initEditTexts(viewgroup: ViewGroup?) {
        viewgroup ?: return
        val childCount = viewgroup.childCount
        for (i in 0 until childCount) {
            val v: View = viewgroup.getChildAt(i)
            if (v is ViewGroup) {
                initEditTexts(v)
            }
            if (v is EditText) {
                v.onFocusChangeListener = this
                v.addTextChangedListener(this)
               // v.isCursorVisible = true
                editTextList.add(v)
            }
        }
    }

    fun destroy(owner:LifecycleOwner? = null){
        owner?.let{
            isKeyboardShow.removeObservers(it)
            changedText.removeObservers(it)
        }
        editTextList.forEach { v->
            v.onFocusChangeListener = null
            v.removeTextChangedListener(this)
        }
        editTextList.clear()
        focusView = null
        hideKeyBoard()
    }

    fun addEditTexts(list: ArrayList<EditText>){
        list.forEach { v->
            v.onFocusChangeListener = this
            v.isCursorVisible = false
            v.addTextChangedListener(this)
            editTextList.add(v)
        }
    }

    fun removeEditTexts(list: ArrayList<EditText>){
        list.forEach { v->
            v.onFocusChangeListener = null
            v.removeTextChangedListener(this)
            editTextList.remove(v)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {

        if (hasFocus && isKeyboardShow.value == false ) {
            focusView = v
            isKeyboardShow.value  = true
        }else if( !hasFocus && isKeyboardShow.value == true && focusView == view ){
            focusView = null
            isKeyboardShow.value  = false
        }
    }
    override fun beforeTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int){}
    override fun onTextChanged(var1: CharSequence?, var2: Int, var3: Int, var4: Int){}
    override fun afterTextChanged(var1: Editable?){
        changedText.value = var1?.toString()
    }

    fun showKeyBoard(){
        isKeyboardShow.value = true
        inputMethodManager?.showSoftInput(focusView, 0)
    }
    fun hideKeyBoard(){
        isKeyboardShow.value  = false
        inputMethodManager?.hideSoftInputFromWindow(focusView?.windowToken, 0)
        focusView = null
    }

    fun showKeyBoard(view: View, flag: Int = 0){
        view.requestFocus()
        isKeyboardShow.value  = true
        focusView = view
        inputMethodManager?.showSoftInput(view, flag)
    }
    fun hideKeyBoard(view: View, flag: Int = 0){
        isKeyboardShow.value  = false
        focusView = null
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, flag)
    }

}