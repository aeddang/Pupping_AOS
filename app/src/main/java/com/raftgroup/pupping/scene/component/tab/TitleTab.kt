package com.raftgroup.pupping.scene.component.tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageUI
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpTitleTabBinding


enum class TitleType{
    Normal, Add, Modify, More;
    companion object {
        fun getType(resIdx:Int):TitleType{
            return when(resIdx){
                1 -> TitleType.Add
                2 -> TitleType.Modify
                3 -> TitleType.More
                else -> TitleType.Normal
            }
        }
    }
}

class TitleTab: PageUI {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initialize(context, attrs)}
    private lateinit var binding: CpTitleTabBinding
    private var titleType: TitleType = TitleType.Normal

    override fun onInit() {
        super.onInit()
        binding = CpTitleTabBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val style = context.obtainStyledAttributes(it, R.styleable.PageUIStyle)
            if (style.hasValue(R.styleable.PageUIStyle_android_title)) {
                this.text = style.getString(R.styleable.PageUIStyle_android_title)
            }
            val titleStyle = context.obtainStyledAttributes(it, R.styleable.PageTitleStyle)
            var type = TitleType.Normal
            if (titleStyle.hasValue(R.styleable.PageTitleStyle_titleType)) {
                val resIdx = titleStyle.getInt(R.styleable.PageTitleStyle_titleType, 0)
                type = TitleType.getType(resIdx)
            }
            titleType = type
            super.initialize(context, attrs)
        }
    }

    override fun onBinding() {
        super.onBinding()
        binding.btnAdd.visibility = View.GONE
        binding.btnModify.visibility = View.GONE
        binding.textMore.visibility = View.GONE
        binding.btnMore.visibility = View.GONE
        setupButton(titleType)
    }

    private fun setupButton(type:TitleType){
        this.titleType = type
        this.radius = context.resources.getDimension(R.dimen.radius_lightExtra)
        when(type){
            TitleType.Add -> {
                binding.btnAdd.visibility = View.VISIBLE
            }
            TitleType.Modify -> {
                binding.btnModify.visibility = View.VISIBLE
            }
            TitleType.More -> {
                binding.textMore.visibility = View.VISIBLE
                binding.btnMore.visibility = View.VISIBLE
            }
            else -> {

            }
        }
    }
    fun setup(title:String? = null, l: OnClickListener? = null){
        l?.let {
            when(titleType){
                TitleType.Add -> {
                    binding.btnAdd.setOnClickListener(it)
                }
                TitleType.Modify -> {
                    binding.btnModify.setOnClickListener(it)
                }
                TitleType.More -> {
                    binding.textMore.setOnClickListener(it)
                    binding.btnMore.setOnClickListener(it)
                }
                else -> {}
            }
        }
        title?.let { this.text = it }
    }
    var text:String? = null
        set(value) {
            field = value
            if (field == null) binding.textTitle.visibility = View.GONE
            else {
                binding.textTitle.visibility = View.VISIBLE
                binding.textTitle.text = field
            }
        }

    var useButton:Boolean = true
        set(value) {
            field = value
            if (field) setupButton(this.titleType)
            else {
                binding.btnAdd.visibility = View.GONE
                binding.btnModify.visibility = View.GONE
                binding.textMore.visibility = View.GONE
                binding.btnMore.visibility = View.GONE
            }
        }
}