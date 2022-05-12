package com.raftgroup.pupping.scene.component.info

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.lib.util.*
import com.lib.view.adapter.ListItem
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.CpPetProfileInfoTopBinding
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.provider.model.PetProfile
import com.skeleton.component.graph.Graph
import com.skeleton.component.graph.GraphBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class PetProfileInfoTop : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var dataProvider: DataProvider
    private lateinit var binding: CpPetProfileInfoTopBinding
    private var profileData:PetProfile? = null
    private var graphBuilder: GraphBuilder? = null
    override fun init(context: Context) {
        binding = CpPetProfileInfoTopBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        graphBuilder = GraphBuilder(binding.graphArea, Graph.Type.HalfRing)
            .setAnimationType(Graph.AnimationType.EaseInSine)
            .setStroke(context.resources.getDimension(R.dimen.stroke_medium))
            .setColor(R.color.brand_primary, R.color.app_greyLight)
            .setRange(180.0)

    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleOwner?.let {
            profileData?.removeObservers(it)
        }
    }

    override fun onLifecycleOwner(owner: LifecycleOwner) {
        super.onLifecycleOwner(owner)

    }

    @SuppressLint("SetTextI18n")
    fun setData(data: PetProfile){
        profileData = data
        setNickName(data)
        setLv(data)
        setDescription(data)
        setExp(data)
        setProgress(data)

        lifecycleOwner?.let {owner ->
            profileData?.nickName?.observe(owner){
                profileData?.let { setNickName(it) }
            }
            profileData?.lv?.observe(owner){
                profileData?.let { setLv(it)}
            }
            profileData?.species?.observe(owner){
                profileData?.let { setSpecies(it)}
            }
            profileData?.exp?.observe(owner){
                profileData?.let { setExp(it)}
            }
            profileData?.nextExp?.observe(owner){
                profileData?.let { setProgress(it)}
            }
        }
    }
    private  fun setNickName(data: PetProfile){
        binding.textName.text = data.nickName.value
    }
    private  fun setLv(data: PetProfile){
        data.lv.value?.toString()?.let{
            binding.textLv.text = "lv.$it"
        }
    }

    private  fun setExp(data: PetProfile){
        data.exp.value?.toString()?.let{
            binding.textExp.text = "${it.toDecimalFormat()}exp"
        }
    }

    private  fun setSpecies(data: PetProfile){
        binding.description.setSpecies(data.species.value)
    }

    private fun setDescription(data: PetProfile){
        data.birth.value?.year?.let { birthYY ->
            val now = LocalDate.now()
            val yy = now.year
            val age = (yy - birthYY + 1).toString() + "yrs"
            data.gender.value?.let {
                binding.description.setData(it, age, data.species.value)
            }

        }
    }

    private  var currentExp:Double = -1.0
    private  fun setProgress(data: PetProfile){
        graphBuilder ?: return
        val exp = data.exp.value
        if (currentExp == exp) return
        val prev = data.prevExp.value
        val next = data.nextExp.value

        exp ?: return
        prev ?: return
        next ?: return
        if (next <= 0.0) return
        currentExp = exp
        val progress = (exp - prev) / (next - prev)
        DataLog.d("progress$progress", appTag)
        graphBuilder?.show(180 * progress)
    }
}