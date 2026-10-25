package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val searchQuery: String = "",
    val currentRecord: IpoAuctionRecord? = null,
    val decisionResult: IpoDecisionResult? = null,
    val allRecords: List<IpoAuctionRecord> = emptyList(),
    val selectedLots: Int = 1,
    val selectedFeeDiscount: Double = 1.0, // 1.0 原價, 0.6 6折, 0.28 2.8折
    val isSearching: Boolean = false,
    val userMessage: String? = null
) {
    /**
     * 嚴格只回傳進行中／未過期（抽籤或掛牌蜜月期尚未結束）的 IPO 標案
     */
    val displayRecords: List<IpoAuctionRecord>
        get() = allRecords.filter { it.isActive() }
}

class IpoDecisionViewModel(
    private val repository: IpoAuctionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IpoUiState())
    val uiState: StateFlow<IpoUiState> = _uiState.asStateFlow()

    init {
        observeLocalData()
        syncTwseData()
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                val activeList = records.filter { it.isActive() }
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
     * 連線證券交易所同步最新競拍開標與抽籤時程公告
     */
    fun syncTwseData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.fetchTwseAuctions(startDate = "20250101", endDate = "20261231")
            result.onSuccess { activeList ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        userMessage = if (activeList.isNotEmpty()) {
                            "已成功同步證交所最新 ${activeList.size} 筆進行中初上市櫃（IPO）標案！"
                        } else {
                            "已同步完成，目前無進行中之 IPO 競拍標案"
                        }
                    )
                }
                if (activeList.isNotEmpty() && _uiState.value.currentRecord == null) {
                    selectRecord(activeList.first())
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
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

        // 1. 先從進行中清單查詢
        val activeRecords = _uiState.value.displayRecords
        val localMatch = activeRecords.find {
            it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
        }

        if (localMatch != null) {
            selectRecord(localMatch)
            return
        }

        // 2. 本地查無，連線 TWSE 官方 API 檢索
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.fetchTwseAuctions(startDate = "20240101", endDate = "20261231")
            result.onSuccess { fetched ->
                val match = fetched.find {
                    it.stockCode.equals(query, ignoreCase = true) || it.stockName.contains(query, ignoreCase = true)
                }
                if (match != null) {
                    selectRecord(match)
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            userMessage = "已從證交所公告取得 [${match.stockCode} ${match.stockName}] IPO 案！"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            userMessage = "證交所目前查無 [$query] 之初上市櫃競拍開標紀錄（請確認代號）"
                        )
                    }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        userMessage = "連線查詢失敗，請檢查網路連線"
                    )
                }
            }
        }
    }

    fun selectRecord(record: IpoAuctionRecord) {
        _uiState.update {
            it.copy(
                currentRecord = record,
                searchQuery = record.stockCode,
                decisionResult = IpoDecisionEngine.evaluate(
                    record = record,
                    lots = it.selectedLots,
                    feeDiscount = it.selectedFeeDiscount
                ),
                userMessage = null
            )
        }
    }

    fun updateSelectedLots(lots: Int) {
        val safeLots = lots.coerceAtLeast(1)
        val current = _uiState.value.currentRecord ?: return
        _uiState.update {
            it.copy(
                selectedLots = safeLots,
                decisionResult = IpoDecisionEngine.evaluate(
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
                decisionResult = IpoDecisionEngine.evaluate(
                    record = current,
                    lots = it.selectedLots,
                    feeDiscount = discount
                )
            )
        }
    }

    fun updateEmergingPrice(price: Double?) {
        val current = _uiState.value.currentRecord ?: return
        val updated = current.copy(emergingMarketPrice = price)
        _uiState.update {
            it.copy(
                currentRecord = updated,
                decisionResult = IpoDecisionEngine.evaluate(
                    record = updated,
                    lots = it.selectedLots,
                    feeDiscount = it.selectedFeeDiscount
                )
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun updateCurrentRecordInternal(record: IpoAuctionRecord, all: List<IpoAuctionRecord>) {
        _uiState.update {
            it.copy(
                currentRecord = record,
                allRecords = all,
                searchQuery = record.stockCode,
                decisionResult = IpoDecisionEngine.evaluate(
                    record = record,
                    lots = it.selectedLots,
                    feeDiscount = it.selectedFeeDiscount
                )
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
