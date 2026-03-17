package project.taxsignal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import project.taxsignal.model.DeductionItem

@Dao
interface DeductionDao {
    //데이터 저장
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DeductionItem)
    // 항목 삭제
    @Delete
    suspend fun delete(item: DeductionItem)
    //공제 항목을 실시간으로 가져옴
    @Query("SELECT * FROM deduction_items ORDER BY id DESC")
    fun getAllDeductions(): Flow<List<DeductionItem>>
}