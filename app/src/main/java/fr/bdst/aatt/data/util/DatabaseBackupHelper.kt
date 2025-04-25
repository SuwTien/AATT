package fr.bdst.aatt.data.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.repository.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe utilitaire pour gérer l'exportation et l'importation des données de la base de données
 * Cette approche basée sur les données est plus fiable que la manipulation directe des fichiers
 */
class DatabaseBackupHelper(private val context: Context, private val repository: ActivityRepository) {
    
    companion object {
        private const val TAG = "DatabaseBackupHelper"
        private const val BACKUP_FOLDER_NAME = "AATT"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }
    
    private val gson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }
    
    /**
     * Exporte toutes les activités de la base de données vers un fichier JSON
     * @param backupName Nom optionnel pour le fichier de sauvegarde (sans extension)
     * @return Le chemin du fichier de sauvegarde ou null en cas d'échec
     */
    suspend fun exportToJson(backupName: String = ""): String? = withContext(Dispatchers.IO) {
        try {
            // Récupérer le dossier de sauvegarde
            val backupFolder = getBackupFolder() ?: return@withContext null
            
            // Générer un nom de fichier avec horodatage si non fourni
            val fileName = if (backupName.isEmpty()) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                "backup_$timestamp$BACKUP_FILE_EXTENSION"
            } else {
                "$backupName$BACKUP_FILE_EXTENSION"
            }
            
            val backupFile = File(backupFolder, fileName)
            
            // Récupérer toutes les activités (actives et terminées)
            val allActivities = repository.getAllActivitiesOneShot()
            Log.d(TAG, "Exporting ${allActivities.size} activities to JSON")
            
            // Créer un objet de sauvegarde avec métadonnées
            val backup = DatabaseBackup(
                timestamp = System.currentTimeMillis(),
                version = 1,
                activities = allActivities
            )
            
            // Sérialiser en JSON et écrire dans le fichier
            FileWriter(backupFile).use { writer ->
                gson.toJson(backup, writer)
            }
            
            Log.d(TAG, "Database successfully exported to ${backupFile.absolutePath}")
            return@withContext backupFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting database to JSON", e)
            return@withContext null
        }
    }
    
    /**
     * Importe les activités d'un fichier JSON vers la base de données
     * @param backupFilePath Chemin du fichier de sauvegarde JSON
     * @return true si l'importation a réussi
     */
    suspend fun importFromJson(backupFilePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupFilePath)
            
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupFilePath")
                return@withContext false
            }
            
            // Lire et désérialiser le fichier JSON
            val backup: DatabaseBackup = FileReader(backupFile).use { reader ->
                gson.fromJson(reader, object : TypeToken<DatabaseBackup>() {}.type)
            }
            
            val activitiesToImport = backup.activities
            if (activitiesToImport.isEmpty()) {
                Log.w(TAG, "No activities found in backup file")
            } else {
                Log.d(TAG, "Importing ${activitiesToImport.size} activities from JSON")
            }
            
            // Vider la base de données actuelle
            repository.clearAllActivities()
            
            // Importer toutes les activités
            activitiesToImport.forEach { activity ->
                repository.importActivity(activity)
            }
            
            Log.d(TAG, "Database successfully imported from ${backupFile.absolutePath}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing database from JSON", e)
            return@withContext false
        }
    }
    
    /**
     * Liste toutes les sauvegardes disponibles
     * @return Liste des fichiers de sauvegarde (nom, chemin)
     */
    fun listBackups(): List<Pair<String, String>> {
        val folder = getBackupFolder() ?: return emptyList()
        
        return folder.listFiles { file -> 
            file.isFile && file.name.endsWith(BACKUP_FILE_EXTENSION)
        }?.map { file ->
            Pair(file.name, file.absolutePath)
        }?.sortedByDescending { (name, _) ->
            // Trier par date de modification décroissante
            File(name).lastModified()
        } ?: emptyList()
    }
    
    /**
     * Supprime une sauvegarde
     * @param backupFilePath Chemin du fichier à supprimer
     * @return true si la suppression a réussi
     */
    fun deleteBackup(backupFilePath: String): Boolean {
        try {
            val file = File(backupFilePath)
            if (!file.exists()) {
                return false
            }
            return file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup", e)
            return false
        }
    }
    
    /**
     * Récupère le dossier de sauvegarde dans Documents/AATT
     */
    private fun getBackupFolder(): File? {
        try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val backupFolder = File(documentsDir, BACKUP_FOLDER_NAME)
            
            if (!backupFolder.exists()) {
                val success = backupFolder.mkdirs()
                if (!success) {
                    Log.e(TAG, "Failed to create backup directory: $backupFolder")
                    return null
                }
            }
            
            return backupFolder
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing backup folder", e)
            return null
        }
    }
    
    /**
     * Classe représentant une sauvegarde complète
     */
    data class DatabaseBackup(
        val timestamp: Long,
        val version: Int,
        val activities: List<Activity>
    )
}