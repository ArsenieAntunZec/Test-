package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_developers")
data class BusinessDeveloper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String = "Business Developer",
    val avatarColorHex: String = "#3F51B5"
)

@Entity(tableName = "coaching_schedules")
data class CoachingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bdId: Int,
    val weekNumber: Int, // 1, 2, 3, 4, 5
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri", ""
    val status: String // "SUCCESSFUL", "CANCELLED", "NONE"
)

@Entity(tableName = "capability_assessments")
data class CapabilityAssessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bdId: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    
    // Scores (1 = Developing, 2 = Stable, 3 = Progressive)
    val marketKnowledge: Int = 1,
    val opportunitiesIdentified: Int = 1,
    val objectivesSet: Int = 1,
    val preparedToAchieve: Int = 1,
    
    val jpAndDrivers: Int = 1,
    val brilliantPresentation: Int = 1,
    val nextStepsAdmin: Int = 1,
    val activationFocus: Int = 1,
    
    val compliance: Int = 1,
    val salesPerformanceOutcomes: Int = 1,
    val personalReview: Int = 1,
    
    val coachingNotes: String = "",
    val actionPlans: String = ""
)
