package project.taxsignal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deduction_items")
data class DeductionItem( //공제 항목들
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Long,
    val isCustom: Boolean, //사용자가 추가한 항목인지 여부
    val category: String // 공제 카테고리
)
