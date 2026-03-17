package project.taxsignal.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import project.taxsignal.model.DeductionItem

private val Context.dataStore by preferencesDataStore(name = "tax_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val SALARY_KEY = stringPreferencesKey("input_salary")
        val CARD_SPENDING_KEY = stringPreferencesKey(("card_spending"))
    }
    // 월급 저장
    suspend fun saveSalary(salary: String) {
        context.dataStore.edit {
            it[SALARY_KEY] = salary
        }
    }
    // 월급 읽기
    val salaryFlow: Flow<String> = context.dataStore.data.map {
            it[SALARY_KEY] ?: ""
        }
    // 카드 사용액 저장
    suspend fun saveCardSpending(amount: String) {
        context.dataStore.edit {
        it[CARD_SPENDING_KEY] = amount
        }
    }
    //카드 사용액 읽기
    val cardSpendingFlow: Flow<String> = context.dataStore.data.map {
        it[CARD_SPENDING_KEY] ?:""
    }
}