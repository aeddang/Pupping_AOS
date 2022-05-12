package com.raftgroup.pupping.scene.page.intro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager

import com.lib.page.*

import com.lib.view.adapter.BaseViewPagerAdapter
import com.raftgroup.pupping.databinding.*
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.scene.page.viewmodel.PageID
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class PageIntro : PageFragment(){
    private val appTag = javaClass.simpleName
    @Inject lateinit var pagePresenter: PagePresenter
    @Inject lateinit var pageProvider: FragmentProvider
    @Inject lateinit var ctx: Context
    private lateinit var binding: PageIntroBinding
    override fun onViewBinding(): View {
        binding = PageIntroBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    private val adapter = PagerAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = adapter
        adapter.setData(arrayOf(1,2,3))
        binding.viewPager.currentItem = 0
    }

    override fun onCoroutineScope() {
        super.onCoroutineScope()

        val ctx = context
        ctx ?: return

        binding.viewPager.addOnPageChangeListener( object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                when (position){
                    2 ->{}
                    else -> {}
                }
            }
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })

    }

    inner class PagerAdapter : BaseViewPagerAdapter<PageUI, Int>(){
        override fun getPageView(container: ViewGroup, position: Int): PageUI {
            val item =  when (position){
                0 -> Item1(ctx)
                1 -> Item2(ctx)
                2 -> Item3(ctx)
                else -> Item1(ctx)
            }
            return item
        }
    }

    inner class Item1(context: Context) : PageUI(context) {
        private lateinit var itemBinding: ItemIntro1Binding
        override fun onBinding() {
            itemBinding = ItemIntro1Binding.inflate(LayoutInflater.from(this.context), this, true)
            itemBinding.btnNext.setOnClickListener {
                binding.viewPager.currentItem += 1
            }
        }
    }
    inner class Item2(context: Context) : PageUI(context) {
        private lateinit var itemBinding: ItemIntro2Binding
        override fun onBinding() {
            itemBinding = ItemIntro2Binding.inflate(LayoutInflater.from(this.context), this, true)
            itemBinding.btnNext.setOnClickListener {
                binding.viewPager.currentItem += 1
            }
        }
    }
    inner class Item3(context: Context) : PageUI(context) {
        private lateinit var itemBinding: ItemIntro3Binding
        override fun onBinding() {
            itemBinding = ItemIntro3Binding.inflate(LayoutInflater.from(this.context), this, true)
            itemBinding.btnComplete.setOnClickListener {
                pagePresenter.changePage(pageProvider.getPageObject(PageID.Login))
            }
        }
    }
}