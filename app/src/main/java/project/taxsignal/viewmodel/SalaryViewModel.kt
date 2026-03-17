package project.taxsignal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.taxsignal.data.DataStoreManager
import project.taxsignal.model.DeductionItem
import project.taxsignal.model.SalaryResult
import project.taxsignal.util.TaxCalculator

class SalaryViewModel(application: Application): AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)

    //사용자 입력값
    private val _inputSalary = MutableStateFlow("")
    val inputSalary: StateFlow<String> = _inputSalary.asStateFlow()

    //계산 결과
    val salaryResult: StateFlow<SalaryResult?> =
        _inputSalary.map { salary ->
            val amount = salary.toLongOrNull()
            if (amount != null) {
                TaxCalculator.calculate(amount)
            } else {
                null
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue =  null
        )

    init {
        viewModelScope.launch {
            launch {
                dataStoreManager.salaryFlow.collect {
                _inputSalary.value = it
                }
            }
            launch {
                dataStoreManager.cardSpendingFlow.collect {
                    _monthlyCardSpending.value = it
                }
            }
        }
    }

    //월급 변경 시 호출
    fun onSalaryChanged(newValue: String) {
        _inputSalary.value = newValue

        viewModelScope.launch {
            dataStoreManager.saveSalary(newValue)
        }
    }

    //소득공제 기준액 25% 계산
    val taxThreshold: StateFlow<Long> = salaryResult.map { result ->
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
        viewModelScope.launch {
            dataStoreManager.saveCardSpending(newValue)
        }
    }

}