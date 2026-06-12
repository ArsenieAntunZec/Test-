package com.example.data

import kotlinx.coroutines.flow.Flow

class SalesCoachRepository(private val dao: SalesCoachDao) {

    val allBusinessDevelopers: Flow<List<BusinessDeveloper>> = dao.getAllBDs()
    val allSchedules: Flow<List<CoachingSchedule>> = dao.getAllSchedules()

    fun getBDById(id: Int): Flow<BusinessDeveloper?> = dao.getBDById(id)

    fun getSchedulesForBD(bdId: Int): Flow<List<CoachingSchedule>> = dao.getSchedulesForBD(bdId)

    fun getAssessmentsForBD(bdId: Int): Flow<List<CapabilityAssessment>> = dao.getAssessmentsForBD(bdId)

    fun getLatestAssessmentForBD(bdId: Int): Flow<CapabilityAssessment?> = dao.getLatestAssessmentForBD(bdId)

    suspend fun insertBD(bd: BusinessDeveloper): Int {
        val id = dao.insertBD(bd).toInt()
        
        // When a new BD is added, pre-populate placeholder coaching schedules for Weeks 1 to 5
        val defaultSchedules = (1..5).map { weekNum ->
            CoachingSchedule(
                bdId = id,
                weekNumber = weekNum,
                dayOfWeek = "",
                status = "NONE"
            )
        }
        dao.insertSchedules(defaultSchedules)
        return id
    }

    suspend fun updateBD(bd: BusinessDeveloper) {
        dao.updateBD(bd)
    }

    suspend fun deleteBD(bd: BusinessDeveloper) {
        // Cascading deletes manually to avoid database constraints issues
        dao.deleteSchedulesForBD(bd.id)
        dao.deleteAssessmentsForBD(bd.id)
        dao.deleteBD(bd)
    }

    suspend fun updateSchedule(schedule: CoachingSchedule) {
        dao.insertSchedule(schedule) // inserts or replaces since we set REPLACE conflict strategy
    }

    suspend fun insertAssessment(assessment: CapabilityAssessment): Long {
        return dao.insertAssessment(assessment)
    }

    suspend fun deleteAssessment(assessment: CapabilityAssessment) {
        dao.deleteAssessment(assessment)
    }
}
