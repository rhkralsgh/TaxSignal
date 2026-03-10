package project.taxsignal.model

data class DeductionItem( //공제 항목들, select 박스
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val amount: Long
)
