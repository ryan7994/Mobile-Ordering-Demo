package com.ryanjames.swabergersmobilepos.feature.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuBinding
import com.ryanjames.swabergersmobilepos.domain.Category
import com.ryanjames.swabergersmobilepos.domain.Resource
import javax.inject.Inject

class MenuFragment : BaseFragment<FragmentMenuBinding>(R.layout.fragment_menu) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MenuFragmentViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addSubscriptions()
        viewModel.retrieveMenu()
    }

    private fun addSubscriptions() {
        viewModel.menuObservable.observe(viewLifecycleOwner, Observer { menuResource ->
            when (menuResource) {
                is Resource.Success -> {
                    if (menuResource.data.categories.isEmpty()) {
                        return@Observer
                    }
                    setupViewPager(menuResource.data.categories)
                }
            }
        })
    }


    private fun setupViewPager(categories: List<Category>) {
        activity?.let {
            binding.tabLayout.setupWithViewPager(binding.viewPager)
            binding.viewPager.adapter = ProgramDetailPagerAdapter(childFragmentManager, categories)
            binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    viewModel.selectedCategoryPosition = position
                }
            })
            binding.viewPager.currentItem = viewModel.selectedCategoryPosition
        }
    }

    private class ProgramDetailPagerAdapter(fm: FragmentManager, private val tabs: List<Category>) :
        FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return MenuPagerFragment.newInstance(tabs[position].categoryId)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabs[position].categoryName
        }

        override fun getCount(): Int = tabs.size

    }

    interface MenuFragmentCallback {
        fun onAddLineItem()
    }

}
