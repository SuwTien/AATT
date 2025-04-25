package fr.bdst.aatt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel pour la page principale de l'application
 */
class MainViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val TAG = "MainViewModel"

    // État pour les activités actives
    private val _activeActivities = MutableStateFlow<List<Activity>>(emptyList())
    val activeActivities: StateFlow<List<Activity>> = _activeActivities

    // État pour l'heure courante (pour afficher l'heure de début de l'activité)
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime
    
    // État pour indiquer un chargement en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // État pour les messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // Collecter les activités actives
        viewModelScope.launch {
            try {
                repository.getActiveActivities()
                    .catch { e ->
                        Log.e(TAG, "Erreur lors de la récupération des activités actives", e)
                        _errorMessage.value = "Impossible de charger les activités: ${e.localizedMessage}"
                    }
                    .collect { activities ->
                        _activeActivities.value = activities
                        // Mettre à jour l'heure courante une fois à chaque changement d'activités
                        _currentTime.value = System.currentTimeMillis()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception lors de la configuration du flux d'activités", e)
                _errorMessage.value = "Erreur de configuration: ${e.localizedMessage}"
            }
        }
        // Note: La mise à jour périodique du temps a été supprimée pour économiser les ressources
    }

    /**
     * Démarre ou arrête une activité selon son type
     */
    fun toggleActivity(activityType: ActivityType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Vérifier si une activité de ce type est déjà active
                val activeActivity = _activeActivities.value.find { it.type == activityType }
                
                if (activeActivity != null) {
                    // Si c'est une activité PAUSE, on la termine simplement
                    if (activityType == ActivityType.PAUSE) {
                        repository.endActivity(activeActivity.id)
                    } else {
                        // Pour les autres types, on termine toutes les activités non-PAUSE
                        repository.endActivitiesByType(activityType)
                    }
                } else {
                    // Démarrer une nouvelle activité
                    // Pour PAUSE, ne pas terminer les autres activités
                    val endCurrentActivities = activityType != ActivityType.PAUSE
                    val exceptTypes = if (activityType != ActivityType.PAUSE) {
                        listOf(ActivityType.PAUSE)
                    } else {
                        emptyList()
                    }
                    
                    repository.startActivity(
                        activityType = activityType,
                        endCurrentActivities = endCurrentActivities,
                        exceptTypes = exceptTypes
                    )
                }
                // Réinitialiser le message d'erreur en cas de succès
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du toggle d'activité: $activityType", e)
                _errorMessage.value = "Échec de l'opération: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Termine toutes les activités actives
     */
    fun stopAllActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentTime = System.currentTimeMillis()
                repository.endActiveActivities(currentTime, null)
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'arrêt de toutes les activités", e)
                _errorMessage.value = "Échec de l'arrêt des activités: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Efface le message d'erreur courant
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Factory pour créer une instance du ViewModel avec le repository
     */
    class Factory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}