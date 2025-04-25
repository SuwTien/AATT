package fr.bdst.aatt.data.repository

import fr.bdst.aatt.data.dao.ActivityDao
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.util.*

/**
 * Repository pour la gestion des activités
 * Encapsule toutes les opérations sur la base de données
 */
class ActivityRepository(private var activityDao: ActivityDao) {

    // Cache de rafraîchissement pour forcer la mise à jour des Flow
    private val refreshTrigger = MutableStateFlow(0)

    /**
     * Met à jour le DAO utilisé par ce repository
     * Utilisé après une restauration de la base de données
     */
    fun updateDao(newDao: ActivityDao) {
        activityDao = newDao
        forceRefresh()
    }

    /**
     * Force le rafraîchissement des données
     * Utilisé après la restauration de la base de données
     */
    fun forceRefresh() {
        refreshTrigger.update { it + 1 }
    }

    /**
     * Démarre une nouvelle activité
     * Si demandé, termine les activités actives existantes
     */
    suspend fun startActivity(
        activityType: ActivityType,
        endCurrentActivities: Boolean = true,
        exceptTypes: List<ActivityType> = listOf(ActivityType.PAUSE)
    ): Long {
        val currentTime = System.currentTimeMillis()
        
        // Si demandé, termine les activités en cours sauf les types dans exceptTypes
        if (endCurrentActivities) {
            val typesToEnd = ActivityType.values().filter { it !in exceptTypes }.toList()
            if (typesToEnd.isNotEmpty()) {
                activityDao.endActiveActivities(currentTime, typesToEnd)
            }
        }
        
        // Crée et insère la nouvelle activité
        val newActivity = Activity(
            type = activityType,
            startTime = currentTime,
            isActive = true
        )
        
        return activityDao.insertActivity(newActivity)
    }

    /**
     * Termine une activité spécifique
     */
    suspend fun endActivity(activityId: Long, endTime: Long = System.currentTimeMillis()) {
        val activity = activityDao.getActivityById(activityId)
        activity?.let {
            val updatedActivity = it.copy(isActive = false, endTime = endTime)
            activityDao.updateActivity(updatedActivity)
        }
    }

    /**
     * Termine toutes les activités du type spécifié
     */
    suspend fun endActivitiesByType(activityType: ActivityType, endTime: Long = System.currentTimeMillis()) {
        activityDao.endActiveActivities(endTime, listOf(activityType))
    }
    
    /**
     * Termine toutes les activités actives, avec possibilité d'exclure certains types
     */
    suspend fun endActiveActivities(endTime: Long = System.currentTimeMillis(), exceptTypes: List<ActivityType>? = null) {
        if (exceptTypes == null || exceptTypes.isEmpty()) {
            activityDao.endAllActiveActivities(endTime)
        } else {
            val typesToEnd = ActivityType.values().filter { it !in exceptTypes }.toList()
            if (typesToEnd.isNotEmpty()) {
                activityDao.endActiveActivities(endTime, typesToEnd)
            }
        }
    }
    
    /**
     * Récupère toutes les activités actives
     */
    fun getActiveActivities(): Flow<List<Activity>> = activityDao.getActiveActivities()
    
    /**
     * Récupère toutes les activités terminées (non actives)
     */
    fun getCompletedActivities(): Flow<List<Activity>> = activityDao.getCompletedActivities()

    /**
     * Récupère les activités pour une journée spécifique
     * Par défaut, utilise la journée courante (00:00:00 à 23:59:59)
     */
    fun getActivitiesForDay(calendar: Calendar = Calendar.getInstance()): Flow<List<Activity>> {
        // Début du jour: 00:00:00
        val startOfDay = calendar.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)
        
        // Fin du jour: 23:59:59.999
        val endOfDay = calendar.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)
        
        return activityDao.getActivitiesForDay(startOfDay.timeInMillis, endOfDay.timeInMillis)
    }
    
    /**
     * Récupère les activités pour une semaine spécifique
     */
    fun getActivitiesForWeek(startDate: Calendar): Flow<List<Activity>> {
        val startOfWeek = startDate.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        startOfWeek.set(Calendar.MILLISECOND, 0)
        
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_YEAR, 6)
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
        endOfWeek.set(Calendar.MINUTE, 59)
        endOfWeek.set(Calendar.SECOND, 59)
        endOfWeek.set(Calendar.MILLISECOND, 999)
        
        return activityDao.getActivitiesBetween(startOfWeek.timeInMillis, endOfWeek.timeInMillis)
    }
    
    /**
     * Récupère les activités pour un mois spécifique
     */
    fun getActivitiesForMonth(year: Int, month: Int): Flow<List<Activity>> {
        val startOfMonth = Calendar.getInstance()
        startOfMonth.set(year, month, 1, 0, 0, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)
        
        val endOfMonth = startOfMonth.clone() as Calendar
        endOfMonth.add(Calendar.MONTH, 1)
        endOfMonth.add(Calendar.MILLISECOND, -1) // Dernier milliseconde du mois
        
        return activityDao.getActivitiesBetween(startOfMonth.timeInMillis, endOfMonth.timeInMillis)
    }
    
    /**
     * Supprime une activité
     */
    suspend fun deleteActivity(activity: Activity) = activityDao.deleteActivity(activity)
    
    /**
     * Met à jour une activité
     */
    suspend fun updateActivity(activity: Activity) = activityDao.updateActivity(activity)
    
    /**
     * Récupère une activité par son ID
     */
    suspend fun getActivityById(id: Long) = activityDao.getActivityById(id)
    
    /**
     * Réinitialise la base de données en supprimant toutes les activités
     */
    suspend fun clearAllActivities() = activityDao.deleteAllActivities()

    /**
     * Récupère toutes les activités (actives et terminées) en une seule fois
     * Utilisé pour l'exportation des données
     */
    suspend fun getAllActivitiesOneShot(): List<Activity> {
        val activeActivities = activityDao.getActiveActivities().first()
        val completedActivities = activityDao.getCompletedActivities().first()
        return activeActivities + completedActivities
    }
    
    /**
     * Importe une activité dans la base de données
     * Utilisé pour la restauration depuis un fichier JSON
     */
    suspend fun importActivity(activity: Activity): Long {
        return activityDao.insertActivity(activity.copy(id = 0)) // Reset ID to let Room generate a new one
    }
}