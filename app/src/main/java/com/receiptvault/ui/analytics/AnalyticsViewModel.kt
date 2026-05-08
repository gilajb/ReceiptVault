package com.receiptvault.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.receiptvault.data.local.dao.CategoryTotal
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.repository.ReceiptRepository
import com.receiptvault.utils.currentMonthPrefix
import com.receiptvault.utils.nextMonth
import com.receiptvault.utils.previousMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _currentMonth = MutableLiveData(currentMonthPrefix())
    val currentMonth: LiveData<String> = _currentMonth

    val receipts: LiveData<List<Receipt>> = _currentMonth.switchMap { prefix ->
        repository.getReceiptsByMonth(prefix).asLiveData()
    }

    val monthlyTotal: LiveData<Double?> = _currentMonth.switchMap { prefix ->
        repository.getMonthlyTotal(prefix).asLiveData()
    }

    val categoryTotals: LiveData<List<CategoryTotal>> = _currentMonth.switchMap { prefix ->
        repository.getCategoryTotalsForMonth(prefix).asLiveData()
    }

    fun previousMonth() {
        _currentMonth.value = previousMonth(_currentMonth.value ?: currentMonthPrefix())
    }

    fun nextMonth() {
        _currentMonth.value = nextMonth(_currentMonth.value ?: currentMonthPrefix())
    }

    // Budget limits per category (could be user-configurable in future)
    val budgetLimits = mapOf(
        "Food & Dining" to 300.0,
        "Groceries" to 400.0,
        "Transport" to 150.0,
        "Shopping" to 200.0,
        "Healthcare" to 100.0,
        "Utilities" to 200.0,
        "Entertainment" to 100.0,
        "Travel" to 500.0,
        "Other" to 100.0
    )
}
