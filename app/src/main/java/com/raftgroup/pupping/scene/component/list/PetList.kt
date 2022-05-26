package com.raftgroup.pupping.scene.component.list

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.allViews
import com.lib.page.PageComponent
import com.lib.page.PagePresenter
import com.raftgroup.pupping.databinding.CpPetListBinding
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.api.rest.AlbumCategory
import com.raftgroup.pupping.store.provider.model.PetProfile
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PetList : PageComponent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider

    private lateinit var binding: CpPetListBinding
    private var userId:String? = null
    override fun init(context: Context) {
        binding = CpPetListBinding.inflate(LayoutInflater.from(context), this, true)
        super.init(context)
    }
    fun setup(@StringRes titleRes:Int , userId:String? = null, l: OnClickListener? = null){
        this.userId = userId
        binding.titleTab.visibility = View.VISIBLE
        binding.titleTab.setup(context.getString(titleRes) , l)
    }
    fun setup(title:String? = null, userId:String? = null, l: OnClickListener? = null){
        this.userId = userId
        binding.titleTab.visibility = View.VISIBLE
        binding.titleTab.setup(title, l)
    }

    fun setDatas(datas:List<PetProfile>){
        binding.listView.body.removeAllViews()
        datas.forEach {
            val item = PetListItem(context)
            item.lifecycleOwner = lifecycleOwner
            binding.listView.body.addView(item, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            item.setData(it)
            item.setSelected{profile, v ->
                pagePresenter.openPopup(
                    pageProvider.getPageObject(PageID.Profile)
                        .addParam(PageParam.data, profile)
                        .addParam(PageParam.id, this.userId)

                )
            }
        }
        binding.emptyView.visibility = if (datas.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.listView.body.allViews.forEach { v ->
            (v as? PetListItem)?.let{
               it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}

