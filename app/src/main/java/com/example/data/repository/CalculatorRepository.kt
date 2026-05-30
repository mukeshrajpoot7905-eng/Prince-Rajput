package com.example.data.repository

import com.example.data.dao.CurrencyDao
import com.example.data.dao.HistoryDao
import com.example.data.entity.CurrencyRateEntity
import com.example.data.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(
    private val historyDao: HistoryDao,
    private val currencyDao: CurrencyDao
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<HistoryEntity>> = historyDao.getFavoriteHistory()
    val allCurrencyRates: Flow<List<CurrencyRateEntity>> = currencyDao.getAllRates()

    suspend fun insertHistory(history: HistoryEntity): Long {
        return historyDao.insertHistory(history)
    }

    suspend fun updateHistoryFavorite(id: Long, isFavorite: Boolean) {
        historyDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun deleteHistoryById(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearAllHistory() {
        historyDao.deleteAllHistory()
    }

    suspend fun cacheCurrencyRates(rates: List<CurrencyRateEntity>) {
        currencyDao.insertRates(rates)
    }

    suspend fun getRateByCode(code: String): CurrencyRateEntity? {
        return currencyDao.getRateByCode(code)
    }
}
