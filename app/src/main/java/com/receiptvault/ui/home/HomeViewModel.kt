package com.receiptvault.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.repository.ReceiptRepository
import com.receiptvault.utils.currentMonthPrefix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _monthPrefix = MutableLiveData(currentMonthPrefix())

    val recentReceipts: LiveData<List<Receipt>> =
        repository.getReceiptsByMonth(currentMonthPrefix()).asLiveData()

    val monthlyTotal: LiveData<Double?> =
        repository.getMonthlyTotal(currentMonthPrefix()).asLiveData()

    fun syncToCloud() {
        viewModelScope.launch {
            runCatching { repository.syncAllToCloud() }
        }
    }
}
