package fr.bdst.aatt.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité représentant une activité dans la base de données
 */
@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "type")
    val type: ActivityType,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Timestamp en millisecondes
    
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,   // Nullable, null si l'activité est active
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false
)

/**
 * Énumération des types d'activités disponibles
 */
enum class ActivityType {
    VS,      // Visite Semestrielle
    ROUTE,   // Route
    DOMICILE, // Domicile
    PAUSE,   // Pause
    DEPLACEMENT // Déplacement
}