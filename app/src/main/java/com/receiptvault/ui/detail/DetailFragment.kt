package com.receiptvault.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.receiptvault.databinding.FragmentDetailBinding
import com.receiptvault.utils.OcrParser
import com.receiptvault.utils.gone
import com.receiptvault.utils.sanitizeInput
import com.receiptvault.utils.showToast
import com.receiptvault.utils.toCurrencyString
import com.receiptvault.utils.toDisplayDate
import com.receiptvault.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    private var isEditMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupObservers()
        setupClickListeners()
        viewModel.loadReceipt(args.receiptId)
    }

    private fun setupCategorySpinner() {
        val categories = OcrParser.CATEGORIES.drop(1)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.receipt.observe(viewLifecycleOwner) { receipt ->
            receipt ?: return@observe

            binding.tvMerchant.text = receipt.merchant
            binding.tvDate.text = receipt.date.toDisplayDate()
            binding.tvAmount.text = receipt.amount.toCurrencyString()
            binding.tvCategory.text = receipt.category
            binding.tvNotes.text = receipt.notes ?: "No notes"

            binding.etMerchant.setText(receipt.merchant)
            binding.etDate.setText(receipt.date)
            binding.etAmount.setText(receipt.amount.toString())
            binding.etNotes.setText(receipt.notes ?: "")

            val categories = OcrParser.CATEGORIES.drop(1)
            val idx = categories.indexOf(receipt.category)
            if (idx >= 0) binding.spinnerCategory.setSelection(idx)

            val imageFile = File(receipt.imageUri)
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(binding.ivReceipt)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DetailState.Loading -> binding.progressBar.visible()
                is DetailState.Saved -> {
                    binding.progressBar.gone()
                    showToast("Receipt updated")
                    setEditMode(false)
                    viewModel.resetState()
                }
                is DetailState.Deleted -> {
                    binding.progressBar.gone()
                    showToast("Receipt deleted")
                    findNavController().navigateUp()
                }
                is DetailState.Error -> {
                    binding.progressBar.gone()
                    showToast(state.message)
                    viewModel.resetState()
                }
                else -> binding.progressBar.gone()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            setEditMode(!isEditMode)
        }

        binding.btnSave.setOnClickListener {
            val receipt = viewModel.receipt.value ?: return@setOnClickListener
            val merchant = sanitizeInput(binding.etMerchant.text.toString())
            val date = sanitizeInput(binding.etDate.text.toString())
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: run {
                showToast("Invalid amount")
                return@setOnClickListener
            }
            val category = binding.spinnerCategory.selectedItem?.toString() ?: receipt.category
            val notes = sanitizeInput(binding.etNotes.text.toString())

            viewModel.updateReceipt(
                receipt.copy(
                    merchant = merchant,
                    date = date,
                    amount = amount,
                    category = category,
                    notes = notes.takeIf { it.isNotBlank() }
                )
            )
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Receipt")
                .setMessage("Are you sure you want to delete this receipt?")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteReceipt() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnShare.setOnClickListener {
            val receipt = viewModel.receipt.value ?: return@setOnClickListener
            val shareText = """
                Receipt from ${receipt.merchant}
                Date: ${receipt.date}
                Amount: ${receipt.amount.toCurrencyString()}
                Category: ${receipt.category}
                ${receipt.notes?.let { "Notes: $it" } ?: ""}
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Receipt from ${receipt.merchant}")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
        }
    }

    private fun setEditMode(edit: Boolean) {
        isEditMode = edit
        binding.groupViewMode.visibility = if (edit) View.GONE else View.VISIBLE
        binding.groupEditMode.visibility = if (edit) View.VISIBLE else View.GONE
        binding.btnEdit.text = if (edit) "Cancel" else "Edit"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
