package com.receiptvault.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.receiptvault.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show the Date Picker Bottom Sheet when "DATES" is clicked
        binding.btnDateFilter.setOnClickListener {
            val bottomSheet = DateRangeBottomSheet()
            bottomSheet.onDatesSelected = { startDate, endDate ->
                // Handle the selected dates here (e.g., filter your list)
                filterReceiptsByDate(startDate, endDate)
            }
            bottomSheet.show(parentFragmentManager, DateRangeBottomSheet.TAG)
        }
    }

    private fun filterReceiptsByDate(start: String?, end: String?) {
        // Your logic to filter the RecyclerView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}