package fr.bdst.aatt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.data.util.StatisticsCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel pour la gestion des statistiques dans l'application
 */
class StatsViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val TAG = "StatsViewModel"
    
    // État pour suivre la date/période sélectionnée
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate
    
    // États pour le mode journalier
    private val _dailyStats = MutableStateFlow<StatisticsCalculator.ActivityStats?>(null)
    val dailyStats: StateFlow<StatisticsCalculator.ActivityStats?> = _dailyStats
    
    private val _dailyActivities = MutableStateFlow<List<Activity>>(emptyList())
    val dailyActivities: StateFlow<List<Activity>> = _dailyActivities
    
    // États pour le mode hebdomadaire
    private val _weeklyStats = MutableStateFlow<StatisticsCalculator.ActivityStats?>(null)
    val weeklyStats: StateFlow<StatisticsCalculator.ActivityStats?> = _weeklyStats
    
    private val _weeklyActivitiesByDay = MutableStateFlow<Map<Calendar, List<Activity>>>(emptyMap())
    val weeklyActivitiesByDay: StateFlow<Map<Calendar, List<Activity>>> = _weeklyActivitiesByDay
    
    // États pour le mode mensuel
    private val _monthlyStats = MutableStateFlow<StatisticsCalculator.ActivityStats?>(null)
    val monthlyStats: StateFlow<StatisticsCalculator.ActivityStats?> = _monthlyStats
    
    private val _monthlyActivitiesByWeek = MutableStateFlow<Map<Calendar, List<Activity>>>(emptyMap())
    val monthlyActivitiesByWeek: StateFlow<Map<Calendar, List<Activity>>> = _monthlyActivitiesByWeek
    
    // État pour indiquer un chargement en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // État pour les messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Formateurs de date réutilisables
    val dayFormatter = SimpleDateFormat("EEEE dd/MM/yyyy", Locale.FRANCE)
    val shortDayFormatter = SimpleDateFormat("EEE dd/MM", Locale.FRANCE)
    val weekFormatter = SimpleDateFormat("'Du' dd/MM 'au' dd/MM/yyyy", Locale.FRANCE)
    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)
    val monthShortFormatter = SimpleDateFormat("MMM yyyy", Locale.FRANCE)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.FRANCE)
    
    /**
     * Charge les statistiques pour le jour actuellement sélectionné
     */
    fun loadDailyStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calendar = _selectedDate.value
                
                // Récupère les activités pour la journée spécifiée (en utilisant la méthode du repository)
                val activitiesForDay = repository.getActivitiesForDay(calendar).first()
                
                // Calcule les statistiques pour ce jour
                val stats = StatisticsCalculator.calculateDailyStats(calendar, activitiesForDay)
                
                _dailyStats.value = stats
                _dailyActivities.value = activitiesForDay
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement des statistiques journalières", e)
                _errorMessage.value = "Erreur: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Charge les statistiques pour la semaine actuellement sélectionnée
     */
    fun loadWeeklyStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calendar = _selectedDate.value
                
                // Récupère les activités pour la semaine spécifiée (en utilisant la méthode du repository)
                val activitiesForWeek = repository.getActivitiesForWeek(calendar).first()
                
                // Calcule les statistiques pour cette semaine
                val stats = StatisticsCalculator.calculateWeeklyStats(calendar, activitiesForWeek)
                
                // Regroupe les activités par jour pour la semaine
                val activitiesByDay = groupActivitiesByDay(activitiesForWeek)
                
                _weeklyStats.value = stats
                _weeklyActivitiesByDay.value = activitiesByDay
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement des statistiques hebdomadaires", e)
                _errorMessage.value = "Erreur: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Charge les statistiques pour le mois actuellement sélectionné
     */
    fun loadMonthlyStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calendar = _selectedDate.value
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                
                // Récupère les activités pour le mois spécifié (en utilisant la méthode du repository)
                val activitiesForMonth = repository.getActivitiesForMonth(year, month).first()
                
                // Calcule les statistiques pour ce mois
                val stats = StatisticsCalculator.calculateMonthlyStats(year, month, activitiesForMonth)
                
                // Regroupe les activités par semaine pour le mois
                val activitiesByWeek = groupActivitiesByWeek(activitiesForMonth, year, month)
                
                _monthlyStats.value = stats
                _monthlyActivitiesByWeek.value = activitiesByWeek
                
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement des statistiques mensuelles", e)
                _errorMessage.value = "Erreur: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Regroupe une liste d'activités par jour
     */
    private fun groupActivitiesByDay(activities: List<Activity>): Map<Calendar, List<Activity>> {
        val result = mutableMapOf<Calendar, MutableList<Activity>>()
        
        for (activity in activities) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = activity.startTime
            
            // Réinitialise l'heure à minuit pour avoir une clé pour le jour
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            
            // Cherche si une clé identique existe déjà
            val existingKey = result.keys.find { 
                it.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                it.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) 
            } ?: cal.clone() as Calendar
            
            // Ajoute l'activité au jour correspondant
            if (!result.containsKey(existingKey)) {
                result[existingKey] = mutableListOf()
            }
            
            result[existingKey]?.add(activity)
        }
        
        return result
    }
    
    /**
     * Regroupe une liste d'activités par semaine pour un mois donné
     */
    private fun groupActivitiesByWeek(activities: List<Activity>, year: Int, month: Int): Map<Calendar, List<Activity>> {
        val result = mutableMapOf<Calendar, MutableList<Activity>>()
        
        // Obtient le premier jour du mois
        val firstDayOfMonth = Calendar.getInstance().apply {
            set(year, month, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Trouve le premier lundi du mois (ou le dernier lundi du mois précédent)
        val firstMondayOfMonth = firstDayOfMonth.clone() as Calendar
        while (firstMondayOfMonth.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            firstMondayOfMonth.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        // Obtient le dernier jour du mois
        val lastDayOfMonth = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        // Crée des entrées pour chaque semaine
        val currentWeekStart = firstMondayOfMonth.clone() as Calendar
        while (currentWeekStart.timeInMillis <= lastDayOfMonth.timeInMillis) {
            val nextWeekStart = currentWeekStart.clone() as Calendar
            nextWeekStart.add(Calendar.DAY_OF_MONTH, 7)
            
            // Filtre les activités pour cette semaine
            val activitiesForWeek = activities.filter {
                it.startTime >= currentWeekStart.timeInMillis && it.startTime < nextWeekStart.timeInMillis
            }
            
            // Ajoute la semaine uniquement si elle contient des activités
            if (activitiesForWeek.isNotEmpty()) {
                result[currentWeekStart.clone() as Calendar] = activitiesForWeek.toMutableList()
            } else {
                // Ajoute quand même la semaine vide si elle fait partie du mois
                val weekEndDate = currentWeekStart.clone() as Calendar
                weekEndDate.add(Calendar.DAY_OF_MONTH, 6)
                
                if ((currentWeekStart.get(Calendar.MONTH) == month || weekEndDate.get(Calendar.MONTH) == month) &&
                    (currentWeekStart.get(Calendar.YEAR) == year || weekEndDate.get(Calendar.YEAR) == year)) {
                    result[currentWeekStart.clone() as Calendar] = mutableListOf()
                }
            }
            
            currentWeekStart.add(Calendar.DAY_OF_MONTH, 7)
        }
        
        return result
    }
    
    /**
     * Navigue au jour précédent
     */
    fun navigateToPreviousDay() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.DAY_OF_MONTH, -1)
        }
        loadDailyStats()
    }
    
    /**
     * Navigue au jour suivant
     */
    fun navigateToNextDay() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1)
        }
        loadDailyStats()
    }
    
    /**
     * Navigue à la semaine précédente
     */
    fun navigateToPreviousWeek() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        loadWeeklyStats()
    }
    
    /**
     * Navigue à la semaine suivante
     */
    fun navigateToNextWeek() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        loadWeeklyStats()
    }
    
    /**
     * Navigue au mois précédent
     */
    fun navigateToPreviousMonth() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.MONTH, -1)
        }
        loadMonthlyStats()
    }
    
    /**
     * Navigue au mois suivant
     */
    fun navigateToNextMonth() {
        _selectedDate.value = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value.timeInMillis
            add(Calendar.MONTH, 1)
        }
        loadMonthlyStats()
    }
    
    /**
     * Navigue à aujourd'hui
     */
    fun navigateToToday() {
        _selectedDate.value = Calendar.getInstance()
        loadDailyStats()
    }
    
    /**
     * Navigue à la semaine courante
     */
    fun navigateToCurrentWeek() {
        _selectedDate.value = Calendar.getInstance()
        loadWeeklyStats()
    }
    
    /**
     * Navigue au mois courant
     */
    fun navigateToCurrentMonth() {
        _selectedDate.value = Calendar.getInstance()
        loadMonthlyStats()
    }

    /**
     * Factory pour créer le ViewModel avec le repository
     */
    class Factory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                return StatsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}