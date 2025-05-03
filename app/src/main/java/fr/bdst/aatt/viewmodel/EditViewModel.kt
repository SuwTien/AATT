package fr.bdst.aatt.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.bdst.aatt.data.db.AATTDatabase
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.data.util.AppEvents
import fr.bdst.aatt.data.util.DatabaseBackupHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel pour la page d'édition des activités
 */
class EditViewModel(private val repository: ActivityRepository) : ViewModel() {
    
    // État pour les activités terminées
    private val _completedActivities = MutableStateFlow<List<Activity>>(emptyList())
    val completedActivities: StateFlow<List<Activity>> = _completedActivities
    
    // État pour la date sélectionnée (par défaut: aujourd'hui)
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate
    
    // État pour les activités du jour sélectionné
    private val _dailyActivities = MutableStateFlow<List<Activity>>(emptyList())
    val dailyActivities: StateFlow<List<Activity>> = _dailyActivities
    
    // État pour les sauvegardes disponibles
    private val _backups = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val backups: StateFlow<List<Pair<String, String>>> = _backups
    
    // État pour les résultats des opérations de sauvegarde/restauration
    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult
    
    init {
        // Collecter les activités terminées (toutes)
        viewModelScope.launch {
            repository.getCompletedActivities().collect { activities ->
                _completedActivities.value = activities
            }
        }
        
        // Collecter les activités du jour sélectionné
        refreshDailyActivities()
    }
    
    /**
     * Rafraîchit la liste des activités pour le jour sélectionné
     */
    private fun refreshDailyActivities() {
        viewModelScope.launch {
            repository.getActivitiesForDay(_selectedDate.value).collect { activities ->
                _dailyActivities.value = activities
            }
        }
    }
    
    /**
     * Passe au jour suivant
     */
    fun navigateToNextDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        _selectedDate.value = calendar
        refreshDailyActivities()
    }
    
    /**
     * Passe au jour précédent
     */
    fun navigateToPreviousDay() {
        val calendar = _selectedDate.value.clone() as Calendar
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        _selectedDate.value = calendar
        refreshDailyActivities()
    }
    
    /**
     * Définit une date spécifique
     */
    fun setSelectedDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        _selectedDate.value = calendar
        refreshDailyActivities()
    }
    
    /**
     * Revient à la date d'aujourd'hui
     */
    fun goToToday() {
        _selectedDate.value = Calendar.getInstance()
        refreshDailyActivities()
    }
    
    /**
     * Met à jour la date et l'heure de début d'une activité
     */
    fun updateStartTime(activityId: Long, startTime: Long) {
        viewModelScope.launch {
            val activity = repository.getActivityById(activityId)
            activity?.let {
                val updated = it.copy(startTime = startTime)
                repository.updateActivity(updated)
            }
        }
    }
    
    /**
     * Met à jour la date et l'heure de fin d'une activité
     */
    fun updateEndTime(activityId: Long, endTime: Long) {
        viewModelScope.launch {
            val activity = repository.getActivityById(activityId)
            activity?.let {
                val updated = it.copy(endTime = endTime)
                repository.updateActivity(updated)
            }
        }
    }
    
    /**
     * Met à jour à la fois la date/heure de début et de fin d'une activité en une seule opération
     */
    fun updateStartAndEndTime(activityId: Long, startTime: Long, endTime: Long?) {
        viewModelScope.launch {
            val activity = repository.getActivityById(activityId)
            activity?.let {
                val updated = it.copy(startTime = startTime, endTime = endTime)
                repository.updateActivity(updated)
            }
        }
    }
    
    /**
     * Supprime une activité
     */
    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            val activity = repository.getActivityById(activityId)
            activity?.let {
                repository.deleteActivity(it)
            }
        }
    }
    
    /**
     * Réactive une activité terminée
     * Cette fonction met l'activité en état actif et supprime son heure de fin
     */
    fun reactivateActivity(activityId: Long) {
        viewModelScope.launch {
            val activity = repository.getActivityById(activityId)
            activity?.let {
                val updated = it.copy(isActive = true, endTime = null)
                repository.updateActivity(updated)
            }
        }
    }
    
    /**
     * Efface toutes les activités de la base de données
     */
    fun clearAllActivities() {
        viewModelScope.launch {
            repository.clearAllActivities()
        }
    }
    
    /**
     * Sauvegarde la base de données
     * (Version JSON)
     */
    fun backupDatabase(context: Context, backupName: String = "") {
        viewModelScope.launch {
            try {
                val backupHelper = DatabaseBackupHelper(context, repository)
                val backupPath = backupHelper.exportToJson(backupName)
                
                val success = backupPath != null
                _operationResult.value = OperationResult(
                    success = success,
                    isBackup = true,
                    message = if (success) "Sauvegarde réussie" else "Échec de la sauvegarde"
                )
                
                // Rafraîchir la liste des sauvegardes
                refreshBackupsList(context)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Erreur lors de la sauvegarde", e)
                _operationResult.value = OperationResult(
                    success = false,
                    isBackup = true,
                    message = "Erreur: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Restaure la base de données à partir d'une sauvegarde
     * (Version JSON)
     */
    fun restoreDatabase(context: Context, backupFilePath: String) {
        viewModelScope.launch {
            try {
                val backupHelper = DatabaseBackupHelper(context, repository)
                val success = backupHelper.importFromJson(backupFilePath)
                
                _operationResult.value = OperationResult(
                    success = success,
                    isBackup = false,
                    message = if (success) "Restauration réussie" else "Échec de la restauration"
                )
                
                // Forcer le rafraîchissement des données
                repository.forceRefresh()
                
                // Émettre un événement global pour informer l'application
                AppEvents.emitDatabaseRestored(true)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Erreur lors de la restauration", e)
                _operationResult.value = OperationResult(
                    success = false,
                    isBackup = false,
                    message = "Erreur: ${e.localizedMessage}"
                )
                
                // Émettre un événement d'échec
                AppEvents.emitDatabaseRestored(false)
            }
        }
    }
    
    /**
     * Supprime une sauvegarde
     */
    fun deleteBackup(context: Context, backupFilePath: String) {
        viewModelScope.launch {
            val backupHelper = DatabaseBackupHelper(context, repository)
            val success = backupHelper.deleteBackup(backupFilePath)
            if (success) {
                // Rafraîchir la liste des sauvegardes
                refreshBackupsList(context)
            }
        }
    }
    
    /**
     * Actualise la liste des sauvegardes disponibles
     */
    fun refreshBackupsList(context: Context) {
        viewModelScope.launch {
            val backupHelper = DatabaseBackupHelper(context, repository)
            _backups.value = backupHelper.listBackups()
        }
    }
    
    /**
     * Réinitialise le résultat de l'opération
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }
    
    /**
     * Classe représentant le résultat d'une opération de sauvegarde/restauration
     */
    data class OperationResult(
        val success: Boolean,
        val isBackup: Boolean,
        val message: String
    )

    /**
     * Factory pour créer une instance du ViewModel avec le repository
     */
    class Factory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
                return EditViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}