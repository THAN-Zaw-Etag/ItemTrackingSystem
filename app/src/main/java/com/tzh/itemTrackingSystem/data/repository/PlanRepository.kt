package com.tzh.itemTrackingSystem.data.repository

import com.tzh.itemTrackingSystem.data.dao.PlanDao
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.entity.ItemPlanEntity
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import com.tzh.itemTrackingSystem.ulti.SaveResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlanRepository(val planDao: PlanDao) {

    suspend fun addPlan(planEntity: PlanEntity) {
        planDao.addPlan(planEntity)
    }

    fun addPlanItem(planId: Int, item: List<ItemEntity>): Flow<SaveResult> = flow {

        emit(SaveResult.Loading)
        delay(1500)
        try {
            val itemPlanList = item.map {
                ItemPlanEntity(
                    planId = planId, itemId = it.id
                )
            }
            planDao.addPlanItem(itemPlanList)
            emit(SaveResult.Success)
        } catch (e: Exception) {
            emit(SaveResult.Error(e.toString()))
        }
    }

    fun getAllPlan() = planDao.getAllPlan()

    fun getAllItemByPlanId(planId: Int) = planDao.getItemsByPlanId(planId)
    fun getAllItemNotByPlanId(planId: Int) = planDao.getItemsNotByPlanId(planId)
    suspend fun planItemRemove(planId: Int, itemId: Int) = planDao.planItemRemove(planId, itemId)

}