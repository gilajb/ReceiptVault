package com.receiptvault.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

data class FilterState(
    val query: String = "",
    val category: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _filterState = MutableLiveData(FilterState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val receipts: LiveData<List<Receipt>> = _filterState.switchMap { filter ->
        repository.getFilteredReceipts(
            merchant = filter.query.takeIf { it.isNotBlank() },
            category = filter.category,
            startDate = filter.startDate,
            endDate = filter.endDate
        ).asLiveData()
    }

    val allCategories: LiveData<List<String>> = repository.getAllCategories().asLiveData()

    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value?.copy(query = query)
    }

    fun setCategoryFilter(category: String?) {
        _filterState.value = _filterState.value?.copy(category = category)
    }

    fun setDateRange(startDate: String?, endDate: String?) {
        _filterState.value = _filterState.value?.copy(startDate = startDate, endDate = endDate)
    }

    fun clearFilters() {
        _filterState.value = FilterState()
    }
}
