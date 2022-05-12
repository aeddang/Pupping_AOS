package com.raftgroup.pupping.scene.page.popup
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.lib.page.PageFragment
import com.lib.page.PagePresenter
import com.lib.page.PageView
import com.lib.util.DataLog
import com.lib.util.PageLog
import com.lib.view.adapter.BaseAdapter
import com.raftgroup.pupping.R
import com.raftgroup.pupping.databinding.PageHistoryBinding
import com.raftgroup.pupping.scene.component.list.History
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageParam
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.api.ApiField
import com.raftgroup.pupping.store.api.ApiQ
import com.raftgroup.pupping.store.api.ApiSuccess
import com.raftgroup.pupping.store.api.ApiType
import com.raftgroup.pupping.store.api.rest.MissionCategory
import com.raftgroup.pupping.store.api.rest.MissionData
import com.raftgroup.pupping.store.provider.DataProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class PageHistory : PageFragment() {

    private val appTag = javaClass.simpleName
    @Inject
    lateinit var pagePresenter: PagePresenter
    @Inject
    lateinit var pageProvider: FragmentProvider
    @Inject
    lateinit var ctx: Context
    @Inject
    lateinit var repository: PageRepository
    @Inject
    lateinit var dataProvider: DataProvider
    private lateinit var binding: PageHistoryBinding
    override val pageChileren:ArrayList<PageView>?
        get(){
            return arrayListOf(binding.historyList)
        }

    override fun onViewBinding(): View {
        binding = PageHistoryBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }
    override fun onDestroyView() {
        PageLog.d("onDestroyView", appTag)
        super.onDestroyView()
        repository.disposeLifecycleOwner(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pageTab.setup(if (type == MissionCategory.Walk) R.string.profileWalkHistory else R.string.profileMissionHistory, isBack = true)
        binding.historyList.setup( object : BaseAdapter.Delegate {
            override fun onBottom(page: Int, size: Int) {
                super.onBottom(page, size)
                currentPage = page
                load()
            }

            override fun onReflash() {
                super.onReflash()
                reload()
            }
        })
        binding.historyList.setSelected { data, v->

        }
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()
        val ctx = context
        ctx ?: return
        dataProvider.result.observe(this){ res: ApiSuccess<ApiType>? ->
            res?.let {
                DataLog.d(res.contentID, appTag+"contentID")
                if(res.id == appTag) {
                    when(res.type){
                        ApiType.GetMission -> {
                            (res.data as? List<MissionData>)?.let{ datas->
                                loaded(datas)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    private var userId:String = ""
    private var petId:Int? = null
    private var requestCode:Int? = null
    private var currentPage:Int = 0
    private var type: MissionCategory = MissionCategory.Walk
    private val historyDatas:MutableList<History> = arrayListOf()
    override fun onPageParams(params: Map<String, Any?>): PageView {

        (params[PageParam.type] as? MissionCategory)?.let { type->
            this.type = type
        }
        (params[PageParam.id] as? String)?.let { id->
            this.userId = id
        }
        (params[PageParam.subId] as? Int)?.let { id->
            this.petId = id
        }
        return super.onPageParams(params)
    }

    override fun onTransactionCompleted() {
        super.onTransactionCompleted()
        reload()
    }

    private fun reload(){
        historyDatas.clear()
        binding.historyList.resetDatas()
        currentPage = 0
        load()
    }

    private fun load(){
        val query = HashMap<String,String>()
        //query[ApiField.userId] = null
        query[ApiField.petId] = petId.toString()
        query[ApiField.missionCategory] = type.getApiCode()
        query[ApiField.page] = currentPage.toString()
        dataProvider.requestData(ApiQ(appTag, ApiType.GetMission, query = query))
    }

    private fun loaded(datas:List<MissionData>){
        val added = datas.map { History().setData(it) }
        historyDatas.addAll(added)
        DataLog.d("added ${added.count()}", appTag)
        binding.historyList.addDatas(added)
    }
}