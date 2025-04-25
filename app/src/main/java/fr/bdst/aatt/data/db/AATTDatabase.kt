package fr.bdst.aatt.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.bdst.aatt.data.dao.ActivityDao
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.util.Converters
import fr.bdst.aatt.data.util.ExternalStorageHelper

/**
 * Classe principale de la base de données Room pour l'application AATT
 */
@Database(
    entities = [Activity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AATTDatabase : RoomDatabase() {

    /**
     * Fournit l'accès au DAO des activités
     */
    abstract fun activityDao(): ActivityDao

    companion object {
        private const val TAG = "AATTDatabase"
        private const val DB_NAME = ExternalStorageHelper.DB_NAME
        
        @Volatile
        private var INSTANCE: AATTDatabase? = null

        /**
         * Obtient une instance de la base de données,
         * en la créant si elle n'existe pas encore
         */
        fun getDatabase(context: Context): AATTDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = createDatabase(context)
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Crée la base de données dans le stockage interne de l'application
         */
        private fun createDatabase(context: Context): AATTDatabase {
            Log.d(TAG, "Initializing database in internal storage")
            
            return Room.databaseBuilder(
                context.applicationContext,
                AATTDatabase::class.java,
                DB_NAME
            )
            .fallbackToDestructiveMigration()
            .build()
        }
        
        /**
         * Ferme la connexion à la base de données
         * Utile avant de restaurer une sauvegarde
         */
        fun closeDatabase() {
            try {
                INSTANCE?.let { db ->
                    if (db.isOpen) {
                        Log.d(TAG, "Closing database connection")
                        db.close()
                    }
                }
                INSTANCE = null
            } catch (e: Exception) {
                Log.e(TAG, "Error closing database", e)
            }
        }
        
        /**
         * Réouvre la base de données après une restauration
         * Cette méthode assure une réinitialisation complète de la base de données
         */
        fun reopenDatabase(context: Context): AATTDatabase {
            Log.d(TAG, "Reopening database after restore")
            synchronized(this) {
                try {
                    // Fermer l'instance existante si elle existe
                    INSTANCE?.close()
                    INSTANCE = null
                    
                    // Petit délai pour s'assurer que toutes les ressources sont libérées
                    Thread.sleep(100)
                    
                    // Demander au garbage collector de nettoyer les références non utilisées
                    System.gc()
                    Thread.sleep(50)
                    
                    // Créer une nouvelle instance avec un builder spécifique pour la restauration
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AATTDatabase::class.java,
                        DB_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    
                    // Tester la connexion pour s'assurer qu'elle fonctionne
                    db.openHelper.readableDatabase
                    db.openHelper.writableDatabase
                    
                    INSTANCE = db
                    return db
                } catch (e: Exception) {
                    Log.e(TAG, "Error reopening database", e)
                    
                    // En cas d'erreur, tenter une approche plus simple
                    val fallbackDb = createDatabase(context)
                    INSTANCE = fallbackDb
                    return fallbackDb
                }
            }
        }
    }
}