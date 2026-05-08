package com.receiptvault.ui.history

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.receiptvault.R
import com.receiptvault.utils.DatePickerHelper

class DateRangeBottomSheet : BottomSheetDialogFragment() {

    var onDatesSelected: ((startDate: String?, endDate: String?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_date_range, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etStart = view.findViewById<EditText>(R.id.et_start_date)
        val etEnd = view.findViewById<EditText>(R.id.et_end_date)
        val btnApply = view.findViewById<Button>(R.id.btn_apply)
        val btnClear = view.findViewById<Button>(R.id.btn_clear_dates)

        etStart.setOnClickListener {
            DatePickerHelper.showDatePicker(requireContext(), etStart)
        }
        etEnd.setOnClickListener {
            DatePickerHelper.showDatePicker(requireContext(), etEnd)
        }

        btnApply.setOnClickListener {
            onDatesSelected?.invoke(
                etStart.text.toString().takeIf { it.isNotBlank() },
                etEnd.text.toString().takeIf { it.isNotBlank() }
            )
            dismiss()
        }

        btnClear.setOnClickListener {
            onDatesSelected?.invoke(null, null)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DateRangeBottomSheet"
    }
}
