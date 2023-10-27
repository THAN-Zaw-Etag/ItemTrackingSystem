package com.tzh.itemTrackingSystem.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tzh.itemTrackingSystem.data.dao.CategoryDao
import com.tzh.itemTrackingSystem.data.dao.ItemDao
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.data.entity.Item
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(entities = [Item::class, Category::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getItemDao(): ItemDao
    abstract fun getCategoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(DelicateCoroutinesApi::class)
        fun getInstance(applicationContext: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    applicationContext, AppDatabase::class.java, "ItemTrackingSystemDataBase"
                ).fallbackToDestructiveMigration().addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        //check for null

                        val item = Item(
                            itemName = "Number 1",
                        )
                        val item2 = item.copy(
                            itemName = "Number 2"
                        )
                        val item3 = item.copy(
                            itemName = "Number 3"
                        )
                        GlobalScope.launch {
                            INSTANCE?.getItemDao()?.addItem(item)
                            INSTANCE?.getItemDao()?.addItem(item2)
                            INSTANCE?.getItemDao()?.addItem(item3)
                        }
                    }
                }).build()

                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}