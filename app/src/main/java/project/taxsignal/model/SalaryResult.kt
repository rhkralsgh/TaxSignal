package project.taxsignal.model

data class SalaryResult(
    val baseSalary: Long,        // 세전 월급
    val nationalPension: Long,   // 국민연금
    val healthInsurance: Long,   // 건강보험
    val longTermCare: Long,     // 장기요양
    val employmentInsurance: Long, // 고용보험
    val incomeTax: Long,         // 소득세 (간이세액표 대신 간략화된 누진세율 적용)
    val localIncomeTax: Long,    // 지방소득세
    val actualPay: Long,    // 실수령액
)
