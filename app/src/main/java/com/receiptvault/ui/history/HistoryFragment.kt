package com.receiptvault.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.receiptvault.databinding.FragmentHistoryBinding
import com.receiptvault.utils.OcrParser
import com.receiptvault.utils.gone
import com.receiptvault.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var receiptAdapter: ReceiptAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupCategoryFilter()
        setupObservers()
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter { receipt ->
            val action = HistoryFragmentDirections.actionHistoryFragmentToDetailFragment(receipt.id)
            findNavController().navigate(action)
        }
        binding.rvReceipts.adapter = receiptAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupCategoryFilter() {
        val categories = listOf("All") + OcrParser.CATEGORIES.drop(1)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = categories[position]
                viewModel.setCategoryFilter(if (selected == "All") null else selected)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        binding.btnClearFilters.setOnClickListener {
            viewModel.clearFilters()
            binding.searchView.setQuery("", false)
            binding.spinnerCategory.setSelection(0)
        }

        binding.btnDateFilter.setOnClickListener {
            val sheet = DateRangeBottomSheet()
            sheet.onDatesSelected = { start, end ->
                viewModel.setDateRange(start, end)
            }
            sheet.show(parentFragmentManager, DateRangeBottomSheet.TAG)
        }
    }

    private fun setupObservers() {
        viewModel.receipts.observe(viewLifecycleOwner) { receipts ->
            receiptAdapter.submitList(receipts)
            binding.tvEmpty.visibility = if (receipts.isEmpty()) View.VISIBLE else View.GONE
            binding.tvReceiptCount.text = "${receipts.size} receipt(s)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
