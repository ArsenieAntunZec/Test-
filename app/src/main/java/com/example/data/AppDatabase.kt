package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessDeveloper::class,
        CoachingSchedule::class,
        CapabilityAssessment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salesCoachDao(): SalesCoachDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_coach_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed initial data in a background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).salesCoachDao()

                // 1. Seed Business Developers
                val bdaId = dao.insertBD(BusinessDeveloper(name = "Adrian (BD A)", role = "Retail Specialist", avatarColorHex = "#4CAF50"))
                val bdbId = dao.insertBD(BusinessDeveloper(name = "Beatriz (BD B)", role = "Key Account Manager", avatarColorHex = "#2196F3"))
                val bdcId = dao.insertBD(BusinessDeveloper(name = "Carlos (BD C)", role = "Territory BD", avatarColorHex = "#00BCD4"))
                val bddId = dao.insertBD(BusinessDeveloper(name = "Daniel (BD D)", role = "Junior Representative", avatarColorHex = "#9C27B0"))
                val bdeId = dao.insertBD(BusinessDeveloper(name = "Elena (BD E)", role = "Trade Specialist", avatarColorHex = "#FF9800"))

                // 2. Seed Coaching Schedules
                val schedules = listOf(
                    // Adrian (BD A)
                    CoachingSchedule(bdId = bdaId.toInt(), weekNumber = 1, dayOfWeek = "Wed", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bdaId.toInt(), weekNumber = 2, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdaId.toInt(), weekNumber = 3, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdaId.toInt(), weekNumber = 4, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdaId.toInt(), weekNumber = 5, dayOfWeek = "Mon", status = "SUCCESSFUL"),

                    // Beatriz (BD B)
                    CoachingSchedule(bdId = bdbId.toInt(), weekNumber = 1, dayOfWeek = "Thur", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bdbId.toInt(), weekNumber = 2, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdbId.toInt(), weekNumber = 3, dayOfWeek = "Wed", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bdbId.toInt(), weekNumber = 4, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdbId.toInt(), weekNumber = 5, dayOfWeek = "Tues", status = "SUCCESSFUL"),

                    // Carlos (BD C)
                    CoachingSchedule(bdId = bdcId.toInt(), weekNumber = 1, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdcId.toInt(), weekNumber = 2, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdcId.toInt(), weekNumber = 3, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdcId.toInt(), weekNumber = 4, dayOfWeek = "Thur", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bdcId.toInt(), weekNumber = 5, dayOfWeek = "", status = "NONE"),

                    // Daniel (BD D)
                    CoachingSchedule(bdId = bddId.toInt(), weekNumber = 1, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bddId.toInt(), weekNumber = 2, dayOfWeek = "Tues", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bddId.toInt(), weekNumber = 3, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bddId.toInt(), weekNumber = 4, dayOfWeek = "Fri", status = "CANCELLED"),
                    CoachingSchedule(bdId = bddId.toInt(), weekNumber = 5, dayOfWeek = "", status = "NONE"),

                    // Elena (BD E)
                    CoachingSchedule(bdId = bdeId.toInt(), weekNumber = 1, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdeId.toInt(), weekNumber = 2, dayOfWeek = "Wed", status = "SUCCESSFUL"),
                    CoachingSchedule(bdId = bdeId.toInt(), weekNumber = 3, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdeId.toInt(), weekNumber = 4, dayOfWeek = "", status = "NONE"),
                    CoachingSchedule(bdId = bdeId.toInt(), weekNumber = 5, dayOfWeek = "", status = "NONE")
                )
                dao.insertSchedules(schedules)

                // 3. Seed initial assessments (with varying capability profiles)
                // Adrian (BD A) - Planning is good, review is emerging
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bdaId.toInt(),
                        dateMillis = System.currentTimeMillis() - 10 * 24 * 3600 * 1000, // 10 days ago
                        marketKnowledge = 2, opportunitiesIdentified = 1, objectivesSet = 1, preparedToAchieve = 2,
                        jpAndDrivers = 1, brilliantPresentation = 1, nextStepsAdmin = 1, activationFocus = 2,
                        compliance = 1, salesPerformanceOutcomes = 1, personalReview = 1,
                        coachingNotes = "Initial evaluation session. Adrian exhibits strong interest, but requires alignment on structuring account-based smart objectives.",
                        actionPlans = "Focus on the SMART framework for objectives prior to next visit."
                    )
                )
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bdaId.toInt(),
                        dateMillis = System.currentTimeMillis() - 1 * 24 * 3600 * 1000, // 1 day ago
                        marketKnowledge = 3, opportunitiesIdentified = 2, objectivesSet = 2, preparedToAchieve = 3,
                        jpAndDrivers = 2, brilliantPresentation = 2, nextStepsAdmin = 1, activationFocus = 2,
                        compliance = 2, salesPerformanceOutcomes = 1, personalReview = 2,
                        coachingNotes = "Significant progress in planning and market knowledge. Adrian prepares well and is consistent with basic selling standards.",
                        actionPlans = "Improve next steps follow-up tracking on SFA app. Secure decision-maker presence on calls."
                    )
                )

                // Beatriz (BD B) - High performer (Level 2 & 3 throughout)
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bdbId.toInt(),
                        dateMillis = System.currentTimeMillis() - 5 * 24 * 3600 * 1000,
                        marketKnowledge = 3, opportunitiesIdentified = 3, objectivesSet = 2, preparedToAchieve = 3,
                        jpAndDrivers = 2, brilliantPresentation = 3, nextStepsAdmin = 3, activationFocus = 2,
                        compliance = 3, salesPerformanceOutcomes = 2, personalReview = 3,
                        coachingNotes = "Outstanding execution in the field. High compliance levels and brilliant customer presentations.",
                        actionPlans = "Incorporate P.sell and Profit calculations more extensively to drive commercial discussion."
                    )
                )

                // Carlos (BD C) - Stable Level 2
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bdcId.toInt(),
                        dateMillis = System.currentTimeMillis() - 8 * 24 * 3600 * 1000,
                        marketKnowledge = 2, opportunitiesIdentified = 2, objectivesSet = 2, preparedToAchieve = 2,
                        jpAndDrivers = 2, brilliantPresentation = 2, nextStepsAdmin = 2, activationFocus = 2,
                        compliance = 2, salesPerformanceOutcomes = 2, personalReview = 2,
                        coachingNotes = "Solid, consistent delivery across standard routines. Undergoes broad reviews and maintains stable activation focus.",
                        actionPlans = "Work towards Progressive Level 3 of market awareness and deep-dive opportunities."
                    )
                )

                // Daniel (BD D) - Junior, Level 1 and some Level 2
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bddId.toInt(),
                        dateMillis = System.currentTimeMillis() - 12 * 24 * 3600 * 1000,
                        marketKnowledge = 1, opportunitiesIdentified = 1, objectivesSet = 1, preparedToAchieve = 1,
                        jpAndDrivers = 1, brilliantPresentation = 1, nextStepsAdmin = 1, activationFocus = 1,
                        compliance = 1, salesPerformanceOutcomes = 1, personalReview = 1,
                        coachingNotes = "New to territory. Daniel requires on-boarding coaching to establish consistent selling structures and JP routing discipline.",
                        actionPlans = "Learn JP call routines and entry criteria. Establish standard account schedules."
                    )
                )

                // Elena (BD E) - Emerging, good DO criteria
                dao.insertAssessment(
                    CapabilityAssessment(
                        bdId = bdeId.toInt(),
                        dateMillis = System.currentTimeMillis() - 6 * 24 * 3600 * 1000,
                        marketKnowledge = 2, opportunitiesIdentified = 2, objectivesSet = 1, preparedToAchieve = 2,
                        jpAndDrivers = 3, brilliantPresentation = 2, nextStepsAdmin = 2, activationFocus = 2,
                        compliance = 2, salesPerformanceOutcomes = 2, personalReview = 2,
                        coachingNotes = "Good presentation skills and disciplined JP drive checks. Needs to translate opportunities into SMART objective settings.",
                        actionPlans = "Draft specific targets/objectives prior to retail meetings."
                    )
                )
            }
        }
    }
}
