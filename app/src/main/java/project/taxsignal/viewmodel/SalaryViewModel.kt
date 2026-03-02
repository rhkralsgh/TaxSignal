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
import project.taxsignal.model.SalaryResult
import project.taxsignal.util.TaxCalculator

class SalaryViewModel : ViewModel() {
    //사용자 입력값
    private val _inputSalary = MutableStateFlow("")
    val inputSalary: StateFlow<String> = _inputSalary.asStateFlow()

    //계산 결과
    private val _salaryResult = MutableStateFlow<SalaryResult?>(null)
    val salaryResult: StateFlow<SalaryResult?> = _salaryResult.asStateFlow()

    fun onSalaryChanged(newValue: String) {
        _inputSalary.value = newValue
        val salaryAmount = newValue.toLongOrNull()
        if(salaryAmount != null) {
            _salaryResult.value = TaxCalculator.calculate(salaryAmount)
        } else {
            _salaryResult.value = null
        }
    }
}