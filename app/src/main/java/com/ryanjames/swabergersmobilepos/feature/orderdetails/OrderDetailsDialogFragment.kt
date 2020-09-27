package com.ryanjames.swabergersmobilepos.feature.orderdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentOrderDetailsBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagItemAdapter
import kotlinx.android.synthetic.main.fragment_order_details.*
import javax.inject.Inject

private const val EXTRA_ORDER_ID = "extra.order.id"

class OrderDetailsDialogFragment : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentOrderDetailsBinding

    private lateinit var viewModel: OrderDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_details, container, false)
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(OrderDetailsViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = getString(R.string.order_details)
        toolbar.setNavigationOnClickListener { dismiss() }
        subscribe()
        arguments?.getString(EXTRA_ORDER_ID)?.let { orderId ->
            viewModel.retrieveOrder(orderId)
        }
    }

    private fun subscribe() {
        viewModel.getOrderSummary.observe(viewLifecycleOwner, Observer { bagSummary ->
            setupRecyclerView(bagSummary.lineItems)
        })
    }

    private fun setupRecyclerView(orderItems: List<BagLineItem>) {
        val adapter = BagItemAdapter(orderItems, object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: BagLineItem) {

            }
        })
        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        }
    }

    companion object {

        private const val FULL_SCREEN_DIALOG_TAG = "Full Screen Dialog"

        fun display(supportFragmentManager: FragmentManager, orderId: String): OrderDetailsDialogFragment {

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val previous = supportFragmentManager.findFragmentByTag(FULL_SCREEN_DIALOG_TAG)
            if (previous != null) {
                fragmentTransaction.remove(previous)
            }
            fragmentTransaction.addToBackStack(null)

            val fullScreenDialog = OrderDetailsDialogFragment()
            fullScreenDialog.arguments = Bundle().apply {
                putString(EXTRA_ORDER_ID, orderId)
            }
            fullScreenDialog.show(supportFragmentManager, FULL_SCREEN_DIALOG_TAG)
            return fullScreenDialog
        }

    }

}