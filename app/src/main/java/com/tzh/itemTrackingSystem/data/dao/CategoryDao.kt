package com.tzh.itemTrackingSystem.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tzh.itemTrackingSystem.data.TableNameConstant
import com.tzh.itemTrackingSystem.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addCategory(category: Category): Long

    @Query("SELECT * FROM ${TableNameConstant.CATEGORY} order by `CategoryName`")
    abstract fun getAllCategory(): Flow<List<Category>>

    @Query("SELECT * FROM ${TableNameConstant.CATEGORY} where CategoryName = :name  Limit 1")
    abstract suspend fun getCategory(name: String): Category?

    @Query("SELECT * FROM ${TableNameConstant.CATEGORY} where CategoryId = :id  Limit 1")
    abstract suspend fun getCategory(id: Int): Category

    @Update()
    abstract suspend fun updateCategory(item: Category)
}