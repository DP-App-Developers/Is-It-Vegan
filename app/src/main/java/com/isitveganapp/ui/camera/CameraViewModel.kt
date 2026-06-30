package com.isitveganapp.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isitveganapp.data.repository.IngredientRepository
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.domain.usecase.AnalyzeIngredientsUseCase
import com.isitveganapp.ocr.MlKitOcrEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val ocrEngine: MlKitOcrEngine,
    private val analyzeUseCase: AnalyzeIngredientsUseCase,
    private val repository: IngredientRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Processing : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded()
        }
    }

    fun onShutterPressed() {
        _uiState.value = UiState.Processing
    }

    fun processCapture(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        screenWidthPx: Int = 0,
        screenHeightPx: Int = 0,
        scanBoxLeft: Int = 0,
        scanBoxTop: Int = 0,
        scanBoxRight: Int = 0,
        scanBoxBottom: Int = 0,
        onResult: (AnalysisResult) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Processing
            ocrEngine.recognizeText(
                bitmap, rotationDegrees,
                screenWidthPx, screenHeightPx,
                scanBoxLeft, scanBoxTop, scanBoxRight, scanBoxBottom
            )
                .onSuccess { text ->
                    val result = analyzeUseCase.execute(text)
                    _uiState.value = UiState.Idle
                    onResult(result)
                }
                .onFailure { e ->
                    _uiState.value = UiState.Error(e.message ?: "Text recognition failed")
                }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Idle
    }
}
