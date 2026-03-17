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
    private val gson = Gson()

    companion object {
        val SALARY_KEY = stringPreferencesKey("input_salary")
        val CARD_SPENDING_KEY = stringPreferencesKey(("card_spending"))
        val DEDUCTIONS_KEY = stringPreferencesKey("deduction_json")
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
    //추가 공제 리스트 저장(JSON 변환)
    suspend fun saveDeductions(list: List<DeductionItem>) {
        val json = gson.toJson(list)
        context.dataStore.edit {
            it[DEDUCTIONS_KEY] = json
        }
    }
    //추가 공제 리스트 읽기(JSON 복구)
    val deductionsFlow: Flow<List<DeductionItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[DEDUCTIONS_KEY] ?: ""
        if (json.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<DeductionItem>>() {}.type
            gson.fromJson(json, type)
        }
    }
}