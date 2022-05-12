package com.raftgroup.pupping.scene.component.info

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.lib.page.PageComponent
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpLocationInfoBinding
import com.raftgroup.pupping.store.api.rest.WeatherData


class LocationInfo : PageComponent, ListItem<WeatherData> {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private lateinit var binding: CpLocationInfoBinding
    override fun init(context: Context) {
        binding =CpLocationInfoBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)
    }
    fun setAdress(adress:String?){
        binding.title.text = adress ?: context.getText(R.string.locationNotFound)
    }

    override fun setData(data: WeatherData, idx:Int){

        if (data.temp != null){
            binding.textTemperature.text = data.temp + "°"
            binding.textTemperature.visibility = View.VISIBLE
        } else {
            binding.textTemperature.visibility = View.GONE
        }
        if (data.desc != null){
            binding.textWeather.text = data.desc
            binding.textWeather.visibility = View.VISIBLE
            binding.dot.visibility = View.VISIBLE
        } else {
            binding.textWeather.visibility = View.GONE
            binding.dot.visibility = View.GONE
        }

        if(data.iconId != null){
            Glide.with(context)
                .load("http://openweathermap.org/img/wn/${data.iconId}@2x.png")
                .into(binding.iconWeather)
            binding.iconWeather.visibility = View.VISIBLE
        } else {
            binding.iconWeather.visibility = View.GONE
        }

    }

}