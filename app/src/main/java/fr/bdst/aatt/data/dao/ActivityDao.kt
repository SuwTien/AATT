package fr.bdst.aatt.data.dao

import androidx.room.*
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Interface DAO pour les opérations CRUD sur les activités
 */
@Dao
interface ActivityDao {
    /**
     * Insère une nouvelle activité et retourne son ID
     */
    @Insert
    suspend fun insertActivity(activity: Activity): Long

    /**
     * Met à jour une activité existante
     */
    @Update
    suspend fun updateActivity(activity: Activity)

    /**
     * Supprime une activité
     */
    @Delete
    suspend fun deleteActivity(activity: Activity)

    /**
     * Récupère une activité par son ID
     */
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): Activity?

    /**
     * Récupère toutes les activités
     */
    @Query("SELECT * FROM activities ORDER BY start_time DESC")
    fun getAllActivities(): Flow<List<Activity>>

    /**
     * Récupère les activités actives
     */
    @Query("SELECT * FROM activities WHERE is_active = 1")
    fun getActiveActivities(): Flow<List<Activity>>

    /**
     * Récupère les activités terminées (non actives)
     */
    @Query("SELECT * FROM activities WHERE is_active = 0 ORDER BY start_time DESC")
    fun getCompletedActivities(): Flow<List<Activity>>

    /**
     * Récupère les activités par type
     */
    @Query("SELECT * FROM activities WHERE type = :activityType ORDER BY start_time DESC")
    fun getActivitiesByType(activityType: ActivityType): Flow<List<Activity>>

    /**
     * Récupère les activités dans un intervalle de temps
     * @param startTime Timestamp de début en millisecondes
     * @param endTime Timestamp de fin en millisecondes
     */
    @Query("SELECT * FROM activities WHERE start_time >= :startTime AND (end_time <= :endTime OR (end_time IS NULL AND start_time <= :endTime)) ORDER BY start_time")
    fun getActivitiesBetween(startTime: Long, endTime: Long): Flow<List<Activity>>

    /**
     * Récupère les activités du jour spécifié
     * @param dayStart Timestamp du début du jour en millisecondes
     * @param dayEnd Timestamp de la fin du jour en millisecondes
     */
    @Query("SELECT * FROM activities WHERE (start_time BETWEEN :dayStart AND :dayEnd) OR (is_active = 1 AND start_time <= :dayEnd) ORDER BY start_time")
    fun getActivitiesForDay(dayStart: Long, dayEnd: Long): Flow<List<Activity>>
    
    /**
     * Termine toutes les activités actives de types spécifiés
     * @param endTime Timestamp de fin en millisecondes
     * @param activityTypes Types d'activités à terminer (doit être non-null)
     */
    @Query("UPDATE activities SET end_time = :endTime, is_active = 0 WHERE is_active = 1 AND type IN (:activityTypes)")
    suspend fun endActiveActivities(endTime: Long, activityTypes: List<ActivityType>)
    
    /**
     * Termine toutes les activités actives sans exception
     * @param endTime Timestamp de fin en millisecondes
     */
    @Query("UPDATE activities SET end_time = :endTime, is_active = 0 WHERE is_active = 1")
    suspend fun endAllActiveActivities(endTime: Long)
    
    /**
     * Supprime toutes les activités de la base de données
     */
    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
}