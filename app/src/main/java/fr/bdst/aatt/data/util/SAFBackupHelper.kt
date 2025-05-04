package fr.bdst.aatt.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.repository.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import java.lang.reflect.Type

// Ajout de l'import pour la classe commune
import fr.bdst.aatt.data.util.DatabaseBackup

/**
 * Classe utilitaire pour gérer les sauvegardes via le Storage Access Framework (SAF)
 * Cette approche permet de maintenir l'accès aux sauvegardes même après réinstallation
 */
class SAFBackupHelper(
    private val context: Context,
    private val repository: ActivityRepository
) {
    companion object {
        private const val TAG = "SAFBackupHelper"
        private const val SHARED_PREFS_NAME = "saf_backup_prefs"
        private const val KEY_BACKUP_DIRECTORY_URI = "backup_directory_uri"
        
        // Définir une instance de Type statique pour éviter les problèmes avec TypeToken et R8
        private val DATABASE_BACKUP_TYPE: Type = object : TypeToken<DatabaseBackup>() {}.type
    }
    
    // Sérialisation/Désérialisation JSON avec le même format que l'ancien système
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()
        
    /**
     * Définit le dossier principal de sauvegarde
     * @param uri URI du dossier sélectionné par l'utilisateur
     */
    fun setBackupDirectoryUri(uri: Uri) {
        // Prendre les permissions persistantes
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            
            // Stocker l'URI
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BACKUP_DIRECTORY_URI, uri.toString())
                .apply()
                
            Log.d(TAG, "Backup directory set to: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take persistable permission for URI: $uri", e)
        }
    }
    
    /**
     * Récupère l'URI du dossier de sauvegarde
     * @return l'URI du dossier ou null si non défini
     */
    fun getBackupDirectoryUri(): Uri? {
        val uriString = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BACKUP_DIRECTORY_URI, null)
            
        return if (uriString != null) Uri.parse(uriString) else null
    }
    
    /**
     * Vérifie si un dossier de sauvegarde est défini
     */
    fun hasBackupDirectory(): Boolean {
        val uri = getBackupDirectoryUri() ?: return false
        
        // Vérifier que les permissions sont toujours valides
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val persistedPermissions = context.contentResolver.persistedUriPermissions
        
        val hasPermission = persistedPermissions.any { 
            it.uri == uri && it.isReadPermission && it.isWritePermission 
        }
        
        return hasPermission
    }
    
    /**
     * Exporte toutes les activités vers un fichier JSON via SAF
     * @param backupName Nom optionnel pour la sauvegarde (sans extension)
     * @return URI du fichier créé ou null en cas d'échec
     */
    suspend fun exportToJson(backupName: String = ""): Uri? = withContext(Dispatchers.IO) {
        val directoryUri = getBackupDirectoryUri()
        if (directoryUri == null) {
            Log.e(TAG, "Export failed: no backup directory set")
            return@withContext null
        }
        
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        if (directory == null) {
            Log.e(TAG, "Export failed: could not get DocumentFile from URI: $directoryUri")
            return@withContext null
        }
        
        try {
            // Générer un nom de fichier avec horodatage si non fourni
            val fileName = if (backupName.isEmpty()) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                "backup_$timestamp.json"
            } else {
                "$backupName.json"
            }
            
            // Créer un nouveau fichier dans le dossier
            val backupFile = directory.createFile("application/json", fileName)
                ?: throw IOException("Failed to create backup file")
                
            // Récupérer toutes les activités
            val allActivities = repository.getAllActivitiesOneShot()
            Log.d(TAG, "Exporting ${allActivities.size} activities to JSON")
            
            // Créer un objet de sauvegarde avec métadonnées (même format que l'ancien système)
            val backup = DatabaseBackup(
                timestamp = System.currentTimeMillis(),
                version = 1,
                activities = allActivities
            )
            
            // Sérialiser et écrire dans le fichier
            context.contentResolver.openOutputStream(backupFile.uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(backup, writer)
                }
            }
            
            Log.d(TAG, "Database successfully exported to ${backupFile.uri}")
            return@withContext backupFile.uri
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting database to JSON via SAF", e)
            return@withContext null
        }
    }
    
    /**
     * Importe des activités depuis un fichier JSON via SAF
     * @param uri URI du fichier de sauvegarde
     * @return true si l'importation a réussi
     */
    suspend fun importFromJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Vérifier que l'URI est valide et accessible
            val canRead = try {
                context.contentResolver.openInputStream(uri)?.close()
                true
            } catch (e: Exception) {
                Log.e(TAG, "URI is not accessible for reading", e)
                false
            }
            
            if (!canRead) {
                Log.e(TAG, "Cannot access file: $uri")
                return@withContext false
            }
            
            // Lire et désérialiser le fichier JSON
            var jsonString: String? = null
            try {
                jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                }
                
                if (jsonString == null) {
                    Log.e(TAG, "JSON file content null or empty")
                    throw IOException("Failed to read from URI: $uri")
                }
                
                Log.d(TAG, "JSON file read successfully, size: ${jsonString.length} characters")
            } catch (e: Exception) {
                Log.e(TAG, "Error reading JSON file", e)
                return@withContext false
            }
            
            // Désérialiser le JSON
            var backup: DatabaseBackup? = null
            try {
                // Utiliser le type statique défini dans le companion object
                backup = gson.fromJson<DatabaseBackup>(
                    jsonString, 
                    DATABASE_BACKUP_TYPE
                )
                
                if (backup == null) {
                    Log.e(TAG, "Deserialization returned null object")
                    return@withContext false
                }
                
                Log.d(TAG, "Deserialization successful - Activities: ${backup.activities.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error during JSON deserialization", e)
                // Tentative alternative avec une approche manuelle de désérialisation
                try {
                    Log.d(TAG, "Attempting manual deserialization")
                    // Essayer avec une conversion directe sans TypeToken
                    val jsonObject = gson.fromJson(jsonString, Map::class.java)
                    val timestamp = (jsonObject["timestamp"] as? Number)?.toLong() ?: 0L
                    val version = (jsonObject["version"] as? Number)?.toInt() ?: 1
                    val activitiesJson = jsonObject["activities"] as? List<*> ?: emptyList<Any>()
                    
                    // Convertir manuellement les activités
                    val activities = activitiesJson.mapNotNull { activityObj ->
                        try {
                            val activityJson = gson.toJson(activityObj)
                            gson.fromJson(activityJson, Activity::class.java)
                        } catch (ex: Exception) {
                            Log.e(TAG, "Failed to convert an activity", ex)
                            null
                        }
                    }
                    
                    backup = DatabaseBackup(timestamp, version, activities)
                    Log.d(TAG, "Manual deserialization successful - Activities: ${activities.size}")
                } catch (manualEx: Exception) {
                    Log.e(TAG, "Manual deserialization also failed", manualEx)
                    return@withContext false
                }
            }
            
            // Importer les activités dans la base de données
            try {
                val activitiesToImport = backup.activities
                if (activitiesToImport.isEmpty()) {
                    Log.w(TAG, "No activities found in backup file")
                } else {
                    Log.i(TAG, "Importing ${activitiesToImport.size} activities from JSON")
                }
                
                // Vider la base de données actuelle
                repository.clearAllActivities()
                
                // Importer toutes les activités
                activitiesToImport.forEach { activity ->
                    try {
                        repository.importActivity(activity)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error importing activity ID:${activity.id}", e)
                        // Continue avec les autres activités
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error importing activities to database", e)
                return@withContext false
            }
            
            Log.d(TAG, "Database successfully imported from $uri")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing database from JSON via SAF", e)
            return@withContext false
        }
    }
    
    /**
     * Liste toutes les sauvegardes disponibles
     * @return Liste des informations sur les sauvegardes (nom, URI, date)
     */
    suspend fun listBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<BackupInfo>()
        val directoryUri = getBackupDirectoryUri() ?: return@withContext result
        
        try {
            val directory = DocumentFile.fromTreeUri(context, directoryUri) ?: return@withContext result
            
            // Lister les fichiers JSON dans le répertoire
            directory.listFiles().forEach { file ->
                if (!file.isDirectory && file.name?.endsWith(".json") == true) {
                    val displayName = file.name ?: "Unknown"
                    val lastModified = file.lastModified()
                    
                    result.add(BackupInfo(
                        name = displayName,
                        uri = file.uri,
                        lastModified = lastModified
                    ))
                }
            }
            
            // Trier par date de modification (plus récent d'abord)
            return@withContext result.sortedByDescending { it.lastModified }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing backups via SAF", e)
            return@withContext result
        }
    }
    
    /**
     * Supprime une sauvegarde
     * @param uri URI du fichier à supprimer
     * @return true si la suppression a réussi
     */
    suspend fun deleteBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false
            val success = file.delete()
            
            if (success) {
                Log.d(TAG, "Successfully deleted backup: $uri")
            } else {
                Log.e(TAG, "Failed to delete backup: $uri")
            }
            
            return@withContext success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup", e)
            return@withContext false
        }
    }
    
    /**
     * Classe représentant les informations d'une sauvegarde
     */
    data class BackupInfo(
        val name: String,
        val uri: Uri,
        val lastModified: Long
    ) {
        fun getFormattedDate(): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
    }
}