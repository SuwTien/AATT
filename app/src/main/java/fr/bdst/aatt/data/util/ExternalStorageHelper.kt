package fr.bdst.aatt.data.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe utilitaire pour gérer les sauvegardes de la base de données dans le stockage externe
 */
class ExternalStorageHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "ExternalStorageHelper"
        const val DB_FOLDER_NAME = "AATT"
        const val DB_NAME = "aatt_database.db"
    }
    
    /**
     * Vérifie si le stockage externe est disponible en écriture
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
    
    /**
     * Vérifie si le stockage externe est disponible en lecture
     */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
    
    /**
     * Retourne le chemin du dossier AATT dans le stockage externe (Documents)
     * Ce dossier est dans Documents pour être accessible même après désinstallation
     */
    fun getAATTFolder(): File? {
        try {
            // Utilisation du dossier Documents qui survit à la désinstallation
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val aattFolder = File(documentsDir, DB_FOLDER_NAME)
            
            if (!aattFolder.exists()) {
                val success = aattFolder.mkdirs()
                if (!success) {
                    Log.e(TAG, "Failed to create directory: $aattFolder")
                    return null
                }
            }
            
            return aattFolder
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing external storage", e)
            return null
        }
    }
    
    /**
     * Sauvegarde la base de données actuelle vers un fichier dans le stockage externe
     * @param backupName Nom du fichier de sauvegarde (sans extension)
     * @return true si la sauvegarde a réussi
     */
    fun backupDatabase(backupName: String = ""): Boolean {
        try {
            val currentDbFile = context.getDatabasePath(DB_NAME)
            if (!currentDbFile.exists()) {
                Log.e(TAG, "Current database does not exist")
                return false
            }
            
            val folder = getAATTFolder() ?: return false
            
            // Si aucun nom n'est fourni, utiliser un horodatage
            val fileName = if (backupName.isEmpty()) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                "backup_$timestamp.db"
            } else {
                "$backupName.db"
            }
            
            val backupFile = File(folder, fileName)
            
            currentDbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "Database successfully backed up to ${backupFile.absolutePath}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up database", e)
            return false
        }
    }
    
    /**
     * Restaure une sauvegarde de la base de données depuis le stockage externe
     * @param backupFilePath Chemin complet du fichier de sauvegarde
     * @return true si la restauration a réussi
     */
    fun restoreDatabase(backupFilePath: String): Boolean {
        try {
            val backupFile = File(backupFilePath)
            
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist: $backupFilePath")
                return false
            }
            
            // Vérifier la taille du fichier de sauvegarde
            val backupSize = backupFile.length()
            if (backupSize <= 0) {
                Log.e(TAG, "Backup file is empty: $backupFilePath (size: $backupSize)")
                return false
            }
            
            Log.d(TAG, "Backup file found: $backupFilePath (size: $backupSize bytes)")
            
            val currentDbPath = context.getDatabasePath(DB_NAME)
            val currentDbFile = currentDbPath.absolutePath
            
            // S'assurer que le dossier parent existe
            currentDbPath.parentFile?.mkdirs()
            
            // Vérifier l'état actuel de la base de données
            if (currentDbPath.exists()) {
                Log.d(TAG, "Current database exists: $currentDbFile (size: ${currentDbPath.length()} bytes)")
            } else {
                Log.d(TAG, "Current database does not exist yet")
            }
            
            // Supprimer tous les fichiers associés à la base de données actuelle
            val filesToDelete = listOf(
                File(currentDbFile),
                File("$currentDbFile-journal"),
                File("$currentDbFile-shm"),
                File("$currentDbFile-wal")
            )
            
            // Tenter de supprimer tous les fichiers associés
            filesToDelete.forEach { file ->
                if (file.exists()) {
                    val deleted = file.delete()
                    if (!deleted) {
                        Log.w(TAG, "Could not delete file: ${file.absolutePath}")
                    } else {
                        Log.d(TAG, "Deleted file: ${file.absolutePath}")
                    }
                }
            }
            
            // Attendre un peu pour s'assurer que le système de fichiers a bien enregistré les suppressions
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                Log.w(TAG, "Sleep interrupted", e)
            }
            
            // Copier le fichier de sauvegarde vers l'emplacement de la base de données
            try {
                backupFile.inputStream().use { input ->
                    File(currentDbFile).outputStream().use { output ->
                        val bytesCopied = input.copyTo(output)
                        Log.d(TAG, "Copied $bytesCopied bytes from backup to database file")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during file copy operation", e)
                return false
            }
            
            // Vérifier que le fichier a été correctement copié
            val newDbFile = File(currentDbFile)
            if (!newDbFile.exists()) {
                Log.e(TAG, "Database file does not exist after copy operation")
                return false
            }
            
            val newSize = newDbFile.length()
            if (newSize <= 0 || newSize != backupSize) {
                Log.e(TAG, "Database file size mismatch after copy: expected $backupSize, got $newSize")
                return false
            }
            
            Log.d(TAG, "Database successfully restored: $currentDbFile (size: $newSize bytes)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring database", e)
            return false
        }
    }
    
    /**
     * Liste toutes les sauvegardes disponibles dans le dossier AATT
     * @return Liste de paires (nom du fichier, chemin complet)
     */
    fun listBackups(): List<Pair<String, String>> {
        val folder = getAATTFolder() ?: return emptyList()
        
        return folder.listFiles { file -> 
            file.isFile && file.name.endsWith(".db")
        }?.map { file ->
            Pair(file.name, file.absolutePath)
        } ?: emptyList()
    }
    
    /**
     * Supprime une sauvegarde spécifique
     * @param backupFilePath Chemin complet du fichier de sauvegarde à supprimer
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
}