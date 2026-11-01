package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.calculator.SpoDecisionEngine
import com.example.domain.model.SpoDecisionResult
import com.example.domain.model.SpoLotteryRecord
import com.example.domain.model.SpoStage
import com.example.domain.repository.SpoLotteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpoUiState(
    val searchQuery: String = "",
    val currentRecord: SpoLotteryRecord? = null,
    val decisionResult: SpoDecisionResult? = null,
    val allRecords: List<SpoLotteryRecord> = emptyList(),
    val selectedLots: Int = 1,
    val selectedFeeDiscount: Double = 1.0, // 1.0 原價, 0.6 6折, 0.28 2.8折
    val customMarketPriceText: String = "",
    val userMessage: String? = null,
    val isSyncing: Boolean = false
) {
    /**
     * 針對「輔助投資人抽籤決策」排序與過濾：
     * 1. 排除已過抽籤日者 (today > lotteryDate)
     * 2. 排序依序為：開放申購中 (OPEN) -> 即將開放 (UPCOMING) -> 待抽籤開獎 (BALLOTING)
     */
    val displayRecords: List<SpoLotteryRecord>
        get() = allRecords
            .filter { it.isLotteryActive() }
            .sortedWith(
                compareBy<SpoLotteryRecord> {
                    when (it.getStage()) {
                        SpoStage.OPEN -> 0
                        SpoStage.UPCOMING -> 1
                        SpoStage.BALLOTING -> 2
                        SpoStage.CLOSED -> 3
                    }
                }.thenBy { it.endDate }
            )
}

class SpoDecisionViewModel(
    private val repository: SpoLotteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpoUiState())
    val uiState: StateFlow<SpoUiState> = _uiState.asStateFlow()

    init {
        observeLocalData()
        syncTwseData()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                val activeList = records
                    .filter { it.isLotteryActive() }
                    .sortedWith(
                        compareBy<SpoLotteryRecord> {
                            when (it.getStage()) {
                                SpoStage.OPEN -> 0
                                SpoStage.UPCOMING -> 1
                                SpoStage.BALLOTING -> 2
                                SpoStage.CLOSED -> 3
                            }
                        }.thenBy { it.endDate }
                    )
                if (activeList.isNotEmpty()) {
                    val current = _uiState.value.currentRecord
                    val matching = activeList.find { it.stockCode == current?.stockCode } ?: activeList.first()
                    updateCurrentRecordInternal(matching, activeList)
                } else {
                    _uiState.update {
                        it.copy(
                            allRecords = emptyList(),
                            currentRecord = null,
                            decisionResult = null
                        )
                    }
                }
            }
        }
    }

    /**
     * 從臺灣證券交易所官方端點動態拉取最新公開申購抽籤日程表
     */
    fun syncTwseData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchTwseLotteries()
            result.onSuccess { newRecords ->
                val activeRecords = newRecords.filter { it.isLotteryActive() }
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = if (activeRecords.isNotEmpty()) {
                            "已成功同步證交所最新 ${activeRecords.size} 筆進行中現增標案！"
                        } else {
                            "已同步完成，目前無進行中之現增抽籤（前期案件皆已過抽籤日）"
                        }
                    )
                }
                if (activeRecords.isNotEmpty() && _uiState.value.currentRecord == null) {
                    selectRecord(activeRecords.first())
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = "連線證交所同步失敗：${e.message}"
                    )
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

        // 1. 先從已過濾的進行中清單比對
        val activeRecords = _uiState.value.displayRecords
        val localMatch = activeRecords.find {
            it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
        }

        if (localMatch != null) {
            selectRecord(localMatch)
            return
        }

        // 2. 若清單沒有，發起即時線上重新同步檢索
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.fetchTwseLotteries()
            result.onSuccess { remoteList ->
                val activeList = remoteList.filter { it.isLotteryActive() }
                val match = activeList.find {
                    it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
                }
                if (match != null) {
                    selectRecord(match)
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            userMessage = "已從證交所公告取得 [${match.stockCode} ${match.stockName}]！"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            userMessage = "證交所目前無 [$query] 之進行中現增案（可能尚未開放或已過抽籤日）"
                        )
                    }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        userMessage = "查無現增標案 [$query]，請確認股票代號"
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
        val current = _uiState.value.currentRecord
        if (parsed != null && parsed > 0 && current != null) {
            val updatedRecord = current.copy(marketPrice = parsed)
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
        val current = _uiState.value.currentRecord ?: return
        _uiState.update {
            it.copy(
                selectedLots = safeLots,
                decisionResult = SpoDecisionEngine.evaluate(
                    record = current,
                    lots = safeLots,
                    feeDiscount = it.selectedFeeDiscount
                )
            )
        }
    }

    fun updateFeeDiscount(discount: Double) {
        val current = _uiState.value.currentRecord ?: return
        _uiState.update {
            it.copy(
                selectedFeeDiscount = discount,
                decisionResult = SpoDecisionEngine.evaluate(
                    record = current,
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
