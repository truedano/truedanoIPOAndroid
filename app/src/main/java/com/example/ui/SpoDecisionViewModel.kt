package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PresetSpoCases
import com.example.domain.calculator.SpoDecisionEngine
import com.example.domain.model.SpoDecisionResult
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.repository.SpoLotteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpoUiState(
    val searchQuery: String = "2884",
    val currentRecord: SpoLotteryRecord = PresetSpoCases.ALL_PRESETS.first(),
    val decisionResult: SpoDecisionResult = SpoDecisionEngine.evaluate(PresetSpoCases.ALL_PRESETS.first()),
    val allRecords: List<SpoLotteryRecord> = PresetSpoCases.ALL_PRESETS,
    val selectedLots: Int = 1,
    val selectedFeeDiscount: Double = 1.0, // 1.0 原價, 0.6 6折, 0.28 2.8折
    val customMarketPriceText: String = "",
    val userMessage: String? = null,
    val showActiveOnly: Boolean = true,
    val isSyncing: Boolean = false
) {
    val displayRecords: List<SpoLotteryRecord>
        get() = if (showActiveOnly) allRecords.filter { it.isActive() } else allRecords
}

class SpoDecisionViewModel(
    private val repository: SpoLotteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpoUiState())
    val uiState: StateFlow<SpoUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        syncTwseData()
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

    /**
     * 從臺灣證券交易所官方端點同步最新公開申購抽籤日程表
     */
    fun syncTwseData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchTwseLotteries()
            result.onSuccess { newRecords ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = "已成功同步證交所最新 ${newRecords.size} 筆增資申購標案！"
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = "連線證交所同步失敗：${e.message}（已保留既有紀錄）"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleActiveFilter(activeOnly: Boolean) {
        _uiState.update { it.copy(showActiveOnly = activeOnly) }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        // 1. 先從本地資料庫或已同步資料查詢
        val localMatch = _uiState.value.allRecords.find {
            it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
        }

        if (localMatch != null) {
            selectRecord(localMatch)
            return
        }

        // 2. 本地查無，觸發線上重新抓取並搜尋
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchTwseLotteries()
            result.onSuccess { remoteList ->
                val match = remoteList.find {
                    it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
                }
                if (match != null) {
                    selectRecord(match)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            userMessage = "已從證交所最新公告取得 [${match.stockCode} ${match.stockName}] 現增案！"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            userMessage = "證交所近期無 [$query] 之公開增資申購公告，請確認代號"
                        )
                    }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = "查無現增標案 [$query]，請確認股票代號或名稱"
                    )
                }
            }
        }
    }

    fun selectRecord(record: SpoLotteryRecord) {
        _uiState.update {
            it.copy(
                currentRecord = record,
                searchQuery = record.stockCode,
                customMarketPriceText = String.format("%.2f", record.marketPrice),
                decisionResult = SpoDecisionEngine.evaluate(
                    record = record,
                    lots = it.selectedLots,
                    feeDiscount = it.selectedFeeDiscount
                ),
                userMessage = null
            )
        }
    }

    fun onMarketPriceChanged(newPriceText: String) {
        _uiState.update { it.copy(customMarketPriceText = newPriceText) }
        val parsed = newPriceText.toDoubleOrNull()
        if (parsed != null && parsed > 0) {
            val updatedRecord = _uiState.value.currentRecord.copy(marketPrice = parsed)
            _uiState.update {
                it.copy(
                    currentRecord = updatedRecord,
                    decisionResult = SpoDecisionEngine.evaluate(
                        record = updatedRecord,
                        lots = it.selectedLots,
                        feeDiscount = it.selectedFeeDiscount
                    )
                )
            }
        }
    }

    fun updateLots(lots: Int) {
        val safeLots = lots.coerceAtLeast(1)
        _uiState.update {
            it.copy(
                selectedLots = safeLots,
                decisionResult = SpoDecisionEngine.evaluate(
                    record = it.currentRecord,
                    lots = safeLots,
                    feeDiscount = it.selectedFeeDiscount
                )
            )
        }
    }

    fun updateFeeDiscount(discount: Double) {
        _uiState.update {
            it.copy(
                selectedFeeDiscount = discount,
                decisionResult = SpoDecisionEngine.evaluate(
                    record = it.currentRecord,
                    lots = it.selectedLots,
                    feeDiscount = discount
                )
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun updateCurrentRecordInternal(record: SpoLotteryRecord, all: List<SpoLotteryRecord>) {
        _uiState.update {
            it.copy(
                currentRecord = record,
                allRecords = all,
                searchQuery = record.stockCode,
                customMarketPriceText = String.format("%.2f", record.marketPrice),
                decisionResult = SpoDecisionEngine.evaluate(
                    record = record,
                    lots = it.selectedLots,
                    feeDiscount = it.selectedFeeDiscount
                )
            )
        }
    }

    class Factory(private val repository: SpoLotteryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SpoDecisionViewModel(repository) as T
        }
    }
}
