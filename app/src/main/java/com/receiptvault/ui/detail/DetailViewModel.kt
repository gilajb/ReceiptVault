package com.receiptvault.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DetailState {
    object Idle : DetailState()
    object Loading : DetailState()
    object Deleted : DetailState()
    object Saved : DetailState()
    data class Error(val message: String) : DetailState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _receipt = MutableLiveData<Receipt?>()
    val receipt: LiveData<Receipt?> = _receipt

    private val _state = MutableLiveData<DetailState>(DetailState.Idle)
    val state: LiveData<DetailState> = _state

    fun loadReceipt(id: Int) {
        viewModelScope.launch {
            _receipt.value = repository.getReceiptById(id)
        }
    }

    fun updateReceipt(receipt: Receipt) {
        _state.value = DetailState.Loading
        viewModelScope.launch {
            runCatching {
                repository.updateReceipt(receipt)
                _receipt.value = receipt
                _state.value = DetailState.Saved
            }.onFailure { e ->
                _state.value = DetailState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun deleteReceipt() {
        val r = _receipt.value ?: return
        _state.value = DetailState.Loading
        viewModelScope.launch {
            runCatching {
                repository.deleteReceipt(r)
                _state.value = DetailState.Deleted
            }.onFailure { e ->
                _state.value = DetailState.Error(e.message ?: "Delete failed")
            }
        }
    }

    fun resetState() { _state.value = DetailState.Idle }
}
