package com.skeleton.component.dialog

import android.app.AlertDialog
import android.content.Context

object CustomDialog {
    fun makeDialog(context: Context, title: Int, message: Int): AlertDialog.Builder {
        return makeDialog(context, context.getString(title), context.getString(message))
    }

    fun makeDialog(context: Context, title: Int, message: String?): AlertDialog.Builder {
        return makeDialog(context, context.getString(title), message)
    }

    fun makeDialog(context: Context, title:String?, message: Int): AlertDialog.Builder {
        return makeDialog(context, title, context.getString(message))
    }

    fun makeDialog(context: Context, title:String?, message: String?): AlertDialog.Builder{
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        return builder
    }
}