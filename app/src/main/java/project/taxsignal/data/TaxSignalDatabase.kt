package project.taxsignal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import project.taxsignal.model.DeductionItem

@Database(entities = [DeductionItem::class], version = 1)
abstract class TaxSignalDatabase : RoomDatabase() {
    abstract fun deductionDao(): DeductionDao

    companion object {
        @Volatile
        private var INSTANCE: TaxSignalDatabase? = null

        fun getDatabase(context: Context): TaxSignalDatabase {
            //생성된 인스턴스가 있따면 반환, 없으면 새로 만듬
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxSignalDatabase::class.java,
                    "tax_signal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}