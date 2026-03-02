package project.taxsignal.util

import project.taxsignal.model.SalaryResult

object TaxCalculator {
    fun calculate(monthlySalary: Long): SalaryResult { //Salary화면 세금 계산

        // 4대보험
        // 국민연금 상한선 265,500원 설정
        val nationalPension = (monthlySalary * 0.045).toLong().coerceAtMost(265500)
        val healthInsurance = (monthlySalary * 0.03545).toLong()
        val longTermCare = (healthInsurance * 0.1295).toLong()
        val employmentInsurance = (monthlySalary * 0.009).toLong()

        // 소득세 간략 계산 (연봉 환산 -> 누진세율 -> 다시 월로 나눔)
        // 실제로는 간이세액표를 쓰지만, MVP에서는 누진세율로 근사치 계산
        val yearlySalary = monthlySalary * 12
        val yearlyIncomeTax = when {
            yearlySalary <= 14_000_000 -> yearlySalary * 0.06
            yearlySalary <= 50_000_000 -> (yearlySalary - 14_000_000) * 0.15 + 840_000
            else -> (yearlySalary - 50_000_000) * 0.24 + 6_240_000 // 예시 구간
        }
        val monthlyIncomeTax = (yearlyIncomeTax / 12).toLong()
        val localIncomeTax = (monthlyIncomeTax * 0.1).toLong()

        // 실수령액 계산
        // 총공제액
        val totalDeductions = nationalPension + healthInsurance + longTermCare +
                employmentInsurance + monthlyIncomeTax + localIncomeTax
        val actualPay = monthlySalary - totalDeductions

        return SalaryResult(
            monthlySalary, nationalPension, healthInsurance,
            longTermCare, employmentInsurance, monthlyIncomeTax,
            localIncomeTax, actualPay
        )
    }
}