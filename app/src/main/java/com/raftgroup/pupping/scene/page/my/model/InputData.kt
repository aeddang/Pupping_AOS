package com.raftgroup.pupping.scene.page.my.model

import android.graphics.Bitmap
import android.text.InputType
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.raftgroup.pupping.R
import java.time.LocalDate
import java.util.*

enum class InputDataType{
    Text, Select, Date, Image, Radio
}

class InputData(
    val type:InputDataType,
    val title: String,
    var tip: String? = null,
    var info: String? = null,
    var placeHolder:String = "",
    var keyboardType: Int = InputType.TYPE_CLASS_TEXT,
    var tabs:List<SelectData>? = null,
    var checks:List<RadioData>? = null,
    var isOption:Boolean = false,
    var inputValue:String = ""
){
    var selectedIdx:Int = -1
    var selectedDate:LocalDate? = LocalDate.now()
    var selectedImage:Bitmap? = null
    var inputMax:Int = 15
}
data class SelectData (
    var idx:Int = -1,
    @DrawableRes val image:Int,
    @StringRes val text:Int,
    @ColorRes val color:Int
){
    val id:String = UUID.randomUUID().toString()
}

data class RadioData(
    var isCheck:Boolean = false,
    @StringRes var text:Int? = null
){
    val id:String = UUID.randomUUID().toString()
}

