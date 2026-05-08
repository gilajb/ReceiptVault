package com.receiptvault.ui.scanner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.receiptvault.databinding.FragmentConfirmationBinding
import com.receiptvault.utils.DatePickerHelper
import com.receiptvault.utils.OcrParser
import com.receiptvault.utils.gone
import com.receiptvault.utils.sanitizeInput
import com.receiptvault.utils.showToast
import com.receiptvault.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ConfirmationFragment : Fragment() {

    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by viewModels()
    private val args: ConfirmationFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setupObservers()
        setupClickListeners()

        // Load image and run OCR
        val imageFile = File(args.imagePath)
        Glide.with(this).load(imageFile).into(binding.ivReceipt)

        val imageUri = Uri.fromFile(imageFile)
        viewModel.processImage(imageUri, requireContext())
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            OcrParser.CATEGORIES.drop(1) // Remove "All"
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanState.Processing -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                }
                is ScanState.Success -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    populateFields(state.data)
                }
                is ScanState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    showToast("OCR error: ${state.message}. Please fill in details manually.")
                }
                else -> {
                    binding.progressBar.gone()
                }
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SaveState.Saving -> {
                    binding.progressBar.visible()
                    binding.btnSave.isEnabled = false
                }
                is SaveState.Success -> {
                    binding.progressBar.gone()
                    showToast("Receipt saved!")
                    findNavController().navigate(
                        ConfirmationFragmentDirections.actionConfirmationFragmentToHomeFragment()
                    )
                }
                is SaveState.Error -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                    showToast(state.message)
                    viewModel.resetSaveState()
                }
                else -> {
                    binding.progressBar.gone()
                    binding.btnSave.isEnabled = true
                }
            }
        }
    }

    private fun populateFields(data: ExtractedReceiptData) {
        binding.etMerchant.setText(data.merchant)
        binding.etDate.setText(data.date)
        binding.etAmount.setText(data.amount.toString())

        val categoryList = OcrParser.CATEGORIES.drop(1)
        val idx = categoryList.indexOf(data.category)
        if (idx >= 0) binding.spinnerCategory.setSelection(idx)
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            val merchant = sanitizeInput(binding.etMerchant.text.toString())
            val date = sanitizeInput(binding.etDate.text.toString())
            val amountStr = binding.etAmount.text.toString()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: "Other"
            val notes = sanitizeInput(binding.etNotes.text.toString())

            if (merchant.isBlank()) {
                showToast("Please enter a merchant name")
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull() ?: run {
                showToast("Please enter a valid amount")
                return@setOnClickListener
            }

            viewModel.saveReceipt(
                merchant = merchant,
                date = date.ifBlank { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) },
                amount = amount,
                category = category,
                imageUri = args.imagePath,
                notes = notes.takeIf { it.isNotBlank() }
            )
        }

        // Show date picker when date field is tapped
        binding.etDate.setOnClickListener {
            DatePickerHelper.showDatePicker(requireContext(), binding.etDate)
        }
        binding.etDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) DatePickerHelper.showDatePicker(requireContext(), binding.etDate)
        }

        binding.btnDiscard.setOnClickListener {
            findNavController().navigate(
                ConfirmationFragmentDirections.actionConfirmationFragmentToHomeFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
