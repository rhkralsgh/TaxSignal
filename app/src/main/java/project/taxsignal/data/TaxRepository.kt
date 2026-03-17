package project.taxsignal.data

import kotlinx.coroutines.flow.Flow
import project.taxsignal.model.DeductionItem

class TaxRepository(private val deductionDao: DeductionDao) {

    // 모든 항목 가져오기
    val allDeductions: Flow<List<DeductionItem>> = deductionDao.getAllDeductions()

    // 데이터 삽입
    suspend fun insert(item: DeductionItem) {
        deductionDao.insert(item)
    }

    // 데이터 삭제
    suspend fun delete(item: DeductionItem) {
        deductionDao.delete(item)
    }
}