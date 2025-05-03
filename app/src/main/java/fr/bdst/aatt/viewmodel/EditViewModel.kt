package fr.bdst.aatt.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.data.util.AppEvents
import fr.bdst.aatt.data.util.SAFBackupHelper
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
    
    // État pour les sauvegardes disponibles (modifié pour utiliser le nouveau format)
    private val _backups = MutableStateFlow<List<SAFBackupHelper.BackupInfo>>(emptyList())
    val backups: StateFlow<List<SAFBackupHelper.BackupInfo>> = _backups
    
    // État pour les résultats des opérations de sauvegarde/restauration
    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult
    
    // État pour savoir si un dossier de sauvegarde a été défini
    private val _hasSAFDirectory = MutableStateFlow(false)
    val hasSAFDirectory: StateFlow<Boolean> = _hasSAFDirectory
    
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
     * Vérifie si un dossier de sauvegarde SAF a été défini
     */
    fun checkSAFDirectory(context: Context) {
        viewModelScope.launch {
            val safBackupHelper = SAFBackupHelper(context, repository)
            _hasSAFDirectory.value = safBackupHelper.hasBackupDirectory()
        }
    }
    
    /**
     * Définit le dossier de sauvegarde SAF
     */
    fun setBackupDirectoryUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val safBackupHelper = SAFBackupHelper(context, repository)
            safBackupHelper.setBackupDirectoryUri(uri)
            _hasSAFDirectory.value = true
            
            // Rafraîchir la liste des sauvegardes
            refreshSAFBackupsList(context)
        }
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
     * Rafraîchit la liste des activités pour le jour sélectionné
     * Cette méthode publique permet de forcer le rafraîchissement depuis l'UI
     */
    fun refreshActivitiesForCurrentDay() {
        refreshDailyActivities()
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
     * Sauvegarde la base de données via SAF
     */
    fun backupDatabaseSAF(context: Context, backupName: String = "") {
        viewModelScope.launch {
            try {
                val safBackupHelper = SAFBackupHelper(context, repository)
                
                if (!safBackupHelper.hasBackupDirectory()) {
                    _operationResult.value = OperationResult(
                        success = false,
                        isBackup = true,
                        message = "Aucun dossier de sauvegarde sélectionné"
                    )
                    return@launch
                }
                
                val backupUri = safBackupHelper.exportToJson(backupName)
                
                val success = backupUri != null
                _operationResult.value = OperationResult(
                    success = success,
                    isBackup = true,
                    message = if (success) "Sauvegarde réussie" else "Échec de la sauvegarde"
                )
                
                // Rafraîchir la liste des sauvegardes
                refreshSAFBackupsList(context)
            } catch (e: Exception) {
                Log.e("EditViewModel", "Erreur lors de la sauvegarde SAF", e)
                _operationResult.value = OperationResult(
                    success = false,
                    isBackup = true,
                    message = "Erreur: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Restaure la base de données à partir d'une sauvegarde via SAF
     */
    fun restoreDatabaseSAF(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val safBackupHelper = SAFBackupHelper(context, repository)
                val success = safBackupHelper.importFromJson(uri)
                
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
                Log.e("EditViewModel", "Erreur lors de la restauration SAF", e)
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
     * Supprime une sauvegarde via SAF
     */
    fun deleteBackupSAF(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val safBackupHelper = SAFBackupHelper(context, repository)
                val success = safBackupHelper.deleteBackup(uri)
                
                if (success) {
                    // Rafraîchir la liste des sauvegardes
                    refreshSAFBackupsList(context)
                }
            } catch (e: Exception) {
                Log.e("EditViewModel", "Erreur lors de la suppression de sauvegarde SAF", e)
                _operationResult.value = OperationResult(
                    success = false,
                    isBackup = true,
                    message = "Erreur lors de la suppression: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Actualise la liste des sauvegardes SAF disponibles
     */
    fun refreshSAFBackupsList(context: Context) {
        viewModelScope.launch {
            try {
                val safBackupHelper = SAFBackupHelper(context, repository)
                _backups.value = safBackupHelper.listBackups()
            } catch (e: Exception) {
                Log.e("EditViewModel", "Erreur lors du rafraîchissement des sauvegardes SAF", e)
                _backups.value = emptyList()
            }
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