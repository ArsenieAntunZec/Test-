package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: SalesCoachRepository) : ViewModel() {

    // List of all business developers
    val allBDs: StateFlow<List<BusinessDeveloper>> = repository.allBusinessDevelopers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of all calendars/schedules
    val allSchedules: StateFlow<List<CoachingSchedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently selected BD
    private val _selectedBd = MutableStateFlow<BusinessDeveloper?>(null)
    val selectedBd: StateFlow<BusinessDeveloper?> = _selectedBd.asStateFlow()

    // Initialize selection once data is loaded
    init {
        viewModelScope.launch {
            allBDs.firstOrNull { it.isNotEmpty() }?.let { bds ->
                if (_selectedBd.value == null && bds.isNotEmpty()) {
                    _selectedBd.value = bds.first()
                }
            }
        }
    }

    // Active screen navigation
    // 0: Dashboard (Month Overview Grid like spreadsheet), 1: Capability Assessments, 2: Analytics & Insights
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun selectBd(bd: BusinessDeveloper) {
        _selectedBd.value = bd
    }

    // Schedules for selected BD
    val selectedBdSchedules: StateFlow<List<CoachingSchedule>> = selectedBd
        .flatMapLatest { bd ->
            if (bd == null) flowOf(emptyList())
            else repository.getSchedulesForBD(bd.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Assessments for selected BD
    val selectedBdAssessments: StateFlow<List<CapabilityAssessment>> = selectedBd
        .flatMapLatest { bd ->
            if (bd == null) flowOf(emptyList())
            else repository.getAssessmentsForBD(bd.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Latest assessment for selected BD
    val selectedBdLatestAssessment: StateFlow<CapabilityAssessment?> = selectedBd
        .flatMapLatest { bd ->
            if (bd == null) flowOf(null)
            else repository.getLatestAssessmentForBD(bd.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Operations
    fun addBusinessDeveloper(name: String, role: String, colorHex: String) {
        viewModelScope.launch {
            val newBd = BusinessDeveloper(name = name, role = role, avatarColorHex = colorHex)
            val id = repository.insertBD(newBd)
            // Auto select newly added BD
            _selectedBd.value = newBd.copy(id = id)
        }
    }

    fun deleteBusinessDeveloper(bd: BusinessDeveloper) {
        viewModelScope.launch {
            repository.deleteBD(bd)
            if (_selectedBd.value?.id == bd.id) {
                // select another one
                val remaining = allBDs.value.filter { it.id != bd.id }
                _selectedBd.value = remaining.firstOrNull()
            }
        }
    }

    fun updateCoachingSchedule(schedule: CoachingSchedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun saveAssessment(
        bdId: Int,
        marketKnowledge: Int,
        opportunitiesIdentified: Int,
        objectivesSet: Int,
        preparedToAchieve: Int,
        jpAndDrivers: Int,
        brilliantPresentation: Int,
        nextStepsAdmin: Int,
        activationFocus: Int,
        compliance: Int,
        salesPerformanceOutcomes: Int,
        personalReview: Int,
        coachingNotes: String,
        actionPlans: String
    ) {
        viewModelScope.launch {
            val assessment = CapabilityAssessment(
                bdId = bdId,
                dateMillis = System.currentTimeMillis(),
                marketKnowledge = marketKnowledge,
                opportunitiesIdentified = opportunitiesIdentified,
                objectivesSet = objectivesSet,
                preparedToAchieve = preparedToAchieve,
                jpAndDrivers = jpAndDrivers,
                brilliantPresentation = brilliantPresentation,
                nextStepsAdmin = nextStepsAdmin,
                activationFocus = activationFocus,
                compliance = compliance,
                salesPerformanceOutcomes = salesPerformanceOutcomes,
                personalReview = personalReview,
                coachingNotes = coachingNotes,
                actionPlans = actionPlans
            )
            repository.insertAssessment(assessment)
        }
    }

    fun deleteAssessmentRecord(assessment: CapabilityAssessment) {
        viewModelScope.launch {
            repository.deleteAssessment(assessment)
        }
    }
}

class MainViewModelFactory(private val repository: SalesCoachRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
