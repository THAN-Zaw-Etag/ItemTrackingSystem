package com.tzh.itemTrackingSystem.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tzh.itemTrackingSystem.data.TableNameConstant
import com.tzh.itemTrackingSystem.data.entity.ItemEntity
import com.tzh.itemTrackingSystem.data.entity.ItemPlanEntity
import com.tzh.itemTrackingSystem.data.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlanDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addPlan(category: PlanEntity): Long

    @Query("SELECT * FROM ${TableNameConstant.PLAN} order by `PlanName`")
    abstract fun getAllPlan(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM ${TableNameConstant.PLAN} where PlanName = :name  Limit 1")
    abstract suspend fun getPlan(name: String): PlanEntity?

    @Query("SELECT * FROM ${TableNameConstant.PLAN} where id= :id  Limit 1")
    abstract suspend fun getPlan(id: Int): PlanEntity?

    @Update()
    abstract suspend fun updatePlan(item: PlanEntity)

    @Query(
        """ SELECT * FROM ${TableNameConstant.ITEM}
               WHERE ItemId IN (
                         SELECT ItemId FROM ${TableNameConstant.ITEM_PLAN} 
                         WHERE PlanId = :planId)
                """
    )
    abstract fun getItemsByPlanId(planId: Int): Flow<List<ItemEntity>>

    @Query(
        """SELECT * FROM ${TableNameConstant.ITEM} 
                WHERE ItemId IN (
                SELECT ItemId FROM ${TableNameConstant.ITEM_PLAN} 
                WHERE PlanId != :planId
            )"""
    )
    abstract fun getItemsNotByPlanId(planId: Int): Flow<List<ItemEntity>>

    @Insert
    abstract suspend fun addPlanItem(itemPlanList: List<ItemPlanEntity>)

    @Query("DELETE FROM ${TableNameConstant.ITEM_PLAN} WHERE PlanId =:planId AND ItemId =:itemId")
    abstract suspend fun planItemRemove(planId: Int, itemId: Int)

}