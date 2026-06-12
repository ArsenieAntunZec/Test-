package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesCoachDao {
    // Business Developers
    @Query("SELECT * FROM business_developers ORDER BY name ASC")
    fun getAllBDs(): Flow<List<BusinessDeveloper>>

    @Query("SELECT * FROM business_developers WHERE id = :id")
    fun getBDById(id: Int): Flow<BusinessDeveloper?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBD(bd: BusinessDeveloper): Long

    @Update
    suspend fun updateBD(bd: BusinessDeveloper)

    @Delete
    suspend fun deleteBD(bd: BusinessDeveloper)

    // Coaching Schedules
    @Query("SELECT * FROM coaching_schedules")
    fun getAllSchedules(): Flow<List<CoachingSchedule>>

    @Query("SELECT * FROM coaching_schedules WHERE bdId = :bdId ORDER BY weekNumber ASC")
    fun getSchedulesForBD(bdId: Int): Flow<List<CoachingSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: CoachingSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<CoachingSchedule>)

    @Query("DELETE FROM coaching_schedules WHERE bdId = :bdId")
    suspend fun deleteSchedulesForBD(bdId: Int)

    // Capability Assessments
    @Query("SELECT * FROM capability_assessments WHERE bdId = :bdId ORDER BY dateMillis DESC")
    fun getAssessmentsForBD(bdId: Int): Flow<List<CapabilityAssessment>>

    @Query("SELECT * FROM capability_assessments WHERE bdId = :bdId ORDER BY dateMillis DESC LIMIT 1")
    fun getLatestAssessmentForBD(bdId: Int): Flow<CapabilityAssessment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: CapabilityAssessment): Long

    @Query("DELETE FROM capability_assessments WHERE bdId = :bdId")
    suspend fun deleteAssessmentsForBD(bdId: Int)

    @Delete
    suspend fun deleteAssessment(assessment: CapabilityAssessment)
}
