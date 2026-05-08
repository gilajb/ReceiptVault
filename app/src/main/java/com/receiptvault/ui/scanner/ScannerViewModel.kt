package com.receiptvault.ui.scanner

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.repository.ReceiptRepository
import com.receiptvault.utils.OcrParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractedReceiptData(
    val merchant: String,
    val date: String,
    val amount: Double,
    val category: String,
    val imageUri: String
)

sealed class ScanState {
    object Idle : ScanState()
    object Processing : ScanState()
    data class Success(val data: ExtractedReceiptData) : ScanState()
    data class Error(val message: String) : ScanState()
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _scanState = MutableLiveData<ScanState>(ScanState.Idle)
    val scanState: LiveData<ScanState> = _scanState

    private val _saveState = MutableLiveData<SaveState>(SaveState.Idle)
    val saveState: LiveData<SaveState> = _saveState

    fun processImage(imageUri: Uri, context: android.content.Context) {
        _scanState.value = ScanState.Processing

        val image = runCatching {
            InputImage.fromFilePath(context, imageUri)
        }.getOrElse {
            _scanState.value = ScanState.Error("Could not read image: ${it.message}")
            return
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val blocks = visionText.textBlocks.map { it.text }

                val merchant = OcrParser.extractMerchant(blocks)
                val date = OcrParser.extractDate(fullText)
                val amount = OcrParser.extractAmount(fullText)
                val category = OcrParser.guessCategory(merchant, fullText)

                _scanState.value = ScanState.Success(
                    ExtractedReceiptData(
                        merchant = merchant,
                        date = date,
                        amount = amount,
                        category = category,
                        imageUri = imageUri.toString()
                    )
                )
            }
            .addOnFailureListener { e ->
                _scanState.value = ScanState.Error(e.message ?: "OCR failed")
            }
    }

    fun saveReceipt(
        merchant: String,
        date: String,
        amount: Double,
        category: String,
        imageUri: String,
        notes: String?
    ) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            runCatching {
                val receipt = Receipt(
                    merchant = merchant,
                    amount = amount,
                    date = date,
                    category = category,
                    imageUri = imageUri,
                    notes = notes?.takeIf { it.isNotBlank() },
                    syncedToCloud = false
                )
                repository.insertReceipt(receipt)
                _saveState.value = SaveState.Success
            }.onFailure { e ->
                _saveState.value = SaveState.Error(e.message ?: "Failed to save receipt")
            }
        }
    }

    fun resetScanState() { _scanState.value = ScanState.Idle }
    fun resetSaveState() { _saveState.value = SaveState.Idle }
}
