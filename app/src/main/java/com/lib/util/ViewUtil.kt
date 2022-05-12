package com.lib.util

import android.app.Activity
import android.text.InputFilter
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.raftgroup.pupping.R

fun EditText.limitLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun Toast.showCustomToast(@StringRes messageRes: Int, activity: Activity)
{
    this.showCustomToast(activity.getString(messageRes), activity)
}
fun Toast.showCustomToast(message: String, activity: Activity)
{
    val layout = activity.layoutInflater.inflate (
        R.layout.ui_toast,
        activity.findViewById(R.id.toast_container)
    )

    // set the text of the TextView of the message
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message
    this.apply {
        setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM , 0, 0)
        duration = Toast.LENGTH_SHORT
        view = layout
        setMargin(0f,0f)
        show()
    }
}