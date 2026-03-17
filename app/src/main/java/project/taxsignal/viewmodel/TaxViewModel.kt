package project.taxsignal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.taxsignal.data.TaxRepository
import project.taxsignal.model.DeductionItem

class TaxViewModel(private val repository: TaxRepository) : ViewModel() {

    val allItems: StateFlow<List<DeductionItem>> = repository.allDeductions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addItem(name: String, amount: Long) {
        viewModelScope.launch {
            repository.insert(
                DeductionItem(
                    name = name,
                    amount = amount,
                    isCustom = true,
                    category = "기타"
                )
            )
        }
    }

    fun deleteItem(item: DeductionItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}