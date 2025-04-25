package fr.bdst.aatt.data.util

import androidx.room.TypeConverter
import fr.bdst.aatt.data.model.ActivityType

/**
 * Classe de convertisseurs pour Room Database
 * Permet de convertir des types complexes en types primitifs stockables dans SQLite
 */
class Converters {
    /**
     * Convertit un ActivityType en String pour le stockage
     */
    @TypeConverter
    fun fromActivityType(activityType: ActivityType): String {
        return activityType.name
    }

    /**
     * Convertit une String en ActivityType pour l'utilisation dans l'application
     */
    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return try {
            ActivityType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ActivityType.DOMICILE // Valeur par défaut en cas d'erreur
        }
    }

    /**
     * Convertit un Long nullable en Long pour le stockage
     */
    @TypeConverter
    fun fromLongNullable(value: Long?): Long {
        return value ?: 0L
    }

    /**
     * Convertit un Long en Long nullable pour l'utilisation dans l'application
     * Pour les timestamps de fin qui peuvent être null (activité en cours)
     */
    @TypeConverter
    fun toLongNullable(value: Long): Long? {
        return if (value == 0L) null else value
    }
}