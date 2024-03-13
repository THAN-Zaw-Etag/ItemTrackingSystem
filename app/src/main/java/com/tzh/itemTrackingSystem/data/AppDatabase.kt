package com.tzh.itemTrackingSystem.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tzh.itemTrackingSystem.data.dao.CategoryDao
import com.tzh.itemTrackingSystem.data.dao.ItemDao
import com.tzh.itemTrackingSystem.data.dao.PlanDao
import com.tzh.itemTrackingSystem.data.entity.Category
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.entity.ItemPlanEntity
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import kotlinx.coroutines.DelicateCoroutinesApi


@Database(
    entities = [ItemEntity::class, Category::class, PlanEntity::class, ItemPlanEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getItemDao(): ItemDao
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getPlanDao(): PlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(DelicateCoroutinesApi::class)
        fun getInstance(applicationContext: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    applicationContext, AppDatabase::class.java, "ItemTrackingSystemDataBase"
                ).fallbackToDestructiveMigration().addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {

                        }
                    },
                ).build()

                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}