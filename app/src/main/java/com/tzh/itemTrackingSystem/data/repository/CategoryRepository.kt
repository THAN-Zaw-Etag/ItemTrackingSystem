package com.tzh.itemTrackingSystem.data.repository

import com.tzh.itemTrackingSystem.data.dao.CategoryDao
import com.tzh.itemTrackingSystem.data.entity.Category

class CategoryRepository(private val categoryDao: CategoryDao) {


    fun getCategoryList() = categoryDao.getAllCategory()

    suspend fun getCategory(name: String): Category {
        return categoryDao.getCategory(name)!!
    }

    suspend fun getCategory(id: Int): Category {
        return categoryDao.getCategory(id)
    }

    suspend fun addCategory(category: Category): Category {
        val isExistCategory = categoryDao.getCategory(category.categoryName)

        return if (isExistCategory == null) {
            categoryDao.addCategory(category = category)
            categoryDao.getCategory(category.categoryName)!!
        } else {
            isExistCategory
        }
    }

}