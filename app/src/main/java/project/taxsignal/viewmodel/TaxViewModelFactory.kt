package project.taxsignal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import project.taxsignal.data.TaxRepository

class TaxViewModelFactory(private val repository: TaxRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaxViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}