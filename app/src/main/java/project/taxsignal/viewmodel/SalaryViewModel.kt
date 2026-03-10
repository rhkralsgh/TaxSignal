package project.taxsignal.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import project.taxsignal.model.DeductionItem
import project.taxsignal.model.SalaryResult
import project.taxsignal.util.TaxCalculator

class SalaryViewModel : ViewModel() {
    //사용자 입력값
    private val _inputSalary = MutableStateFlow("")
    val inputSalary: StateFlow<String> = _inputSalary.asStateFlow()

    //계산 결과
    private val _salaryResult = MutableStateFlow<SalaryResult?>(null)
    val salaryResult: StateFlow<SalaryResult?> = _salaryResult.asStateFlow()
    //월급 변경 시 호출
    fun onSalaryChanged(newValue: String) {
        _inputSalary.value = newValue
        val salaryAmount = newValue.toLongOrNull()
        if(salaryAmount != null) {
            _salaryResult.value = TaxCalculator.calculate(salaryAmount)
        } else {
            _salaryResult.value = null
        }
    }

    // 추가한 공제 항목 리스트
    private val _additionalDeductions = MutableStateFlow<List<DeductionItem>>(emptyList())
    val additionalDeductions = _additionalDeductions.asStateFlow()

    // 추가 항목 총합
    val totalAdditionalAmount = _additionalDeductions.map { list ->
        list.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        SharingStarted.WhileSubscribed(10000),
        0L
    )

    //항목 추가
    fun addDeduction(name:String, amount: Long) {
        _additionalDeductions.value += DeductionItem(name = name, amount = amount)
    }

    //항목 삭제
    fun removeDeduction(id: Long) {
        _additionalDeductions.value = _additionalDeductions.value.filter { it.id != id }
    }

    // 항목 리스트 중 '연금저축'항목 연간 총액 계산 - 절세팁 화면 연동
    val annualPensionSaving: StateFlow<Long> = _additionalDeductions.map {list ->
        list.filter { it.name == "연금저축" }.sumOf { it.amount } * 12
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(10000),
        initialValue = 0L
    )

    //소득공제 기준액 25% 계산
    val taxThreshold: StateFlow<Long> = _salaryResult.map { result ->
        result?.baseSalary?.let {
            TaxCalculator.calculateThreshold(it) } ?: 0L
    }.stateIn( //초기값 0, 10초 대기, 앱 종료 시 계산 중지
        viewModelScope,
        SharingStarted.WhileSubscribed(10000),
        0L
    )

    // 월간 목표 소비량
    val monthlyThreshold: StateFlow<Long> = taxThreshold.map { annualThreshold ->
        if (annualThreshold > 0) annualThreshold / 12 else 0L
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(10000),
        initialValue = 0L
    )

    //월간 카드 사용액
    private val _monthlyCardSpending = MutableStateFlow("") // 카드 사용액 입력값
    val monthlyCardSpending: StateFlow<String> = _monthlyCardSpending.asStateFlow()

    //카드 사용액 변경 시 호출
    fun onMonthlyCardChanged(newValue: String) {
        _monthlyCardSpending.value = newValue
    }

}