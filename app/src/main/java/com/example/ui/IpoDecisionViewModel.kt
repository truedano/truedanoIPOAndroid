package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PresetCases
import com.example.domain.calculator.IpoDecisionEngine
import com.example.domain.model.IpoAuctionRecord
import com.example.domain.model.IpoDecisionResult
import com.example.domain.repository.IpoAuctionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IpoUiState(
    val searchQuery: String = "6898",
    val currentRecord: IpoAuctionRecord = PresetCases.ALL_PRESETS.first(),
    val decisionResult: IpoDecisionResult = IpoDecisionEngine.evaluate(PresetCases.ALL_PRESETS.first()),
    val allRecords: List<IpoAuctionRecord> = PresetCases.ALL_PRESETS,
    val selectedLots: Int = 1,
    val selectedFeeDiscount: Double = 1.0, // 1.0 原價, 0.6 6折, 0.28 2.8折
    val isSearching: Boolean = false,
    val userMessage: String? = null
)

class IpoDecisionViewModel(
    private val repository: IpoAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IpoUiState())
    val uiState: StateFlow<IpoUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            repository.seedPresetRecordsIfEmpty()
            repository.getAllRecords().collect { records ->
                if (records.isNotEmpty()) {
                    val current = _uiState.value.currentRecord
                    val matching = records.find { it.stockCode == current.stockCode } ?: records.first()
                    updateCurrentRecordInternal(matching, records)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        // 1. 先從本地資料庫或經典預設查詢
        val localMatch = _uiState.value.allRecords.find {
            it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
        }

        if (localMatch != null) {
            updateCurrentRecordInternal(localMatch, _uiState.value.allRecords)
            _uiState.update { it.copy(userMessage = "已載入 ${localMatch.stockCode} ${localMatch.stockName}") }
            return
        }

        // 2. 本地查無，連線 TWSE 官方 API 檢索
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, userMessage = "正在連線臺灣證交所 API 檢索 $query...") }
            val result = repository.fetchTwseAuctions(startDate = "20240101", endDate = "20261231")
            result.onSuccess { fetched ->
                val apiMatch = fetched.find {
                    it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
                }

                if (apiMatch != null) {
                    repository.insertOrUpdate(apiMatch)
                    val updatedList = _uiState.value.allRecords + apiMatch
                    updateCurrentRecordInternal(apiMatch, updatedList)
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            userMessage = "成功自 TWSE 獲取 ${apiMatch.stockCode} ${apiMatch.stockName} 開標數據"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            userMessage = "查無代號「$query」之競拍開標紀錄（請確認代號或該標案是否已完成開標）"
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        userMessage = "TWSE 連線逾時（${err.localizedMessage ?: "網路問題"}），已切換至離線資料"
                    )
                }
            }
        }
    }

    fun selectRecord(record: IpoAuctionRecord) {
        _uiState.update { it.copy(searchQuery = record.stockCode) }
        updateCurrentRecordInternal(record, _uiState.value.allRecords)
    }

    fun setLots(lots: Int) {
        val validLots = lots.coerceIn(1, 100)
        _uiState.update { current ->
            val updatedResult = IpoDecisionEngine.evaluate(
                record = current.currentRecord,
                lots = validLots,
                feeDiscount = current.selectedFeeDiscount
            )
            current.copy(selectedLots = validLots, decisionResult = updatedResult)
        }
    }

    fun setFeeDiscount(discount: Double) {
        _uiState.update { current ->
            val updatedResult = IpoDecisionEngine.evaluate(
                record = current.currentRecord,
                lots = current.selectedLots,
                feeDiscount = discount
            )
            current.copy(selectedFeeDiscount = discount, decisionResult = updatedResult)
        }
    }

    fun deleteRecord(record: IpoAuctionRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record.stockCode)
            val updated = _uiState.value.allRecords.filter { it.stockCode != record.stockCode }
            _uiState.update { current ->
                val fallback = if (current.currentRecord.stockCode == record.stockCode) {
                    updated.firstOrNull() ?: PresetCases.ALL_PRESETS.first()
                } else current.currentRecord
                current.copy(allRecords = updated, currentRecord = fallback)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
            _uiState.update { it.copy(allRecords = emptyList(), userMessage = "已清空所有本地歷史紀錄") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun updateCurrentRecordInternal(record: IpoAuctionRecord, all: List<IpoAuctionRecord>) {
        val calculated = IpoDecisionEngine.evaluate(
            record = record,
            lots = _uiState.value.selectedLots,
            feeDiscount = _uiState.value.selectedFeeDiscount
        )
        _uiState.update {
            it.copy(
                currentRecord = record,
                decisionResult = calculated,
                allRecords = if (all.isNotEmpty()) all else it.allRecords
            )
        }
    }

    class Factory(private val repository: IpoAuctionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IpoDecisionViewModel(repository) as T
        }
    }
}
