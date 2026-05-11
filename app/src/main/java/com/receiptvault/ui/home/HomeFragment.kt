package com.receiptvault.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.receiptvault.R
import com.receiptvault.databinding.FragmentHomeBinding
import com.receiptvault.ui.history.ReceiptAdapter
import com.receiptvault.utils.currentMonthPrefix
import com.receiptvault.utils.monthDisplayName
import com.receiptvault.utils.toCurrencyString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recentAdapter: ReceiptAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        binding.tvMonthLabel.text = monthDisplayName(currentMonthPrefix())
        viewModel.syncToCloud()
    }

    private fun setupRecyclerView() {
        recentAdapter = ReceiptAdapter { receipt ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(receipt.id)
            findNavController().navigate(action)
        }
        binding.rvRecentReceipts.adapter = recentAdapter
    }

    private fun setupObservers() {
        viewModel.recentReceipts.observe(viewLifecycleOwner) { receipts ->
            recentAdapter.submitList(receipts.take(5))
            binding.tvNoReceipts.visibility =
                if (receipts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.monthlyTotal.observe(viewLifecycleOwner) { total ->
            binding.tvMonthlyTotal.text = (total ?: 0.0).toCurrencyString()
        }
    }

    private fun setupClickListeners() {
        binding.fabScan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scannerFragment)
        }

        binding.tvViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
