package fr.bdst.aatt.data.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilitaire de journalisation dans un fichier externe.
 * Permet de capturer des logs même en version release pour faciliter le débogage.
 */
object FileLogger {
    private const val TAG = "FileLogger"
    private const val LOG_FOLDER = "AATT_Logs"
    private const val LOG_FILENAME = "aatt_debug.log"
    
    private var logFile: File? = null
    
    /**
     * Initialise le système de logs.
     * Doit être appelé au démarrage de l'application.
     */
    fun init(context: Context) {
        try {
            // Utiliser le dossier Documents qui est accessible même après réinstallation
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val logFolder = File(documentsDir, LOG_FOLDER)
            
            // Créer le dossier de logs s'il n'existe pas
            if (!logFolder.exists()) {
                if (!logFolder.mkdirs()) {
                    Log.e(TAG, "Impossible de créer le dossier de logs: $logFolder")
                    return
                }
            }
            
            // Créer ou ouvrir le fichier de log
            logFile = File(logFolder, LOG_FILENAME)
            
            // Écrire un en-tête de session
            val header = """
                
                =========================================
                AATT Log Session - ${getTimestamp()}
                Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}
                Build type: ${if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) "DEBUG" else "RELEASE"}
                =========================================
                
            """.trimIndent()
            
            FileWriter(logFile, true).use { writer ->
                writer.append(header)
                writer.append("\n")
            }
            
            Log.i(TAG, "Système de logs initialisé: ${logFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'initialisation du système de logs", e)
        }
    }
    
    /**
     * Écrit un message d'information dans le fichier de logs
     */
    fun i(tag: String, message: String) {
        writeLog("INFO", tag, message)
    }
    
    /**
     * Écrit un message d'erreur dans le fichier de logs
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        writeLog("ERROR", tag, message)
        if (throwable != null) {
            writeException(throwable)
        }
    }
    
    /**
     * Écrit un message de débogage dans le fichier de logs
     */
    fun d(tag: String, message: String) {
        writeLog("DEBUG", tag, message)
    }
    
    /**
     * Écrit un message de warning dans le fichier de logs
     */
    fun w(tag: String, message: String) {
        writeLog("WARN", tag, message)
    }
    
    /**
     * Écrit un message dans le fichier de logs avec un timestamp
     */
    private fun writeLog(level: String, tag: String, message: String) {
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    FileWriter(file, true).use { writer ->
                        writer.append("${getTimestamp()} [$level] $tag: $message\n")
                    }
                }
            }
            
            // Également écrire dans LogCat
            when (level) {
                "INFO" -> Log.i(tag, message)
                "ERROR" -> Log.e(tag, message)
                "DEBUG" -> Log.d(tag, message)
                "WARN" -> Log.w(tag, message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'écriture des logs", e)
        }
    }
    
    /**
     * Écrit la trace complète d'une exception dans le fichier de logs
     */
    private fun writeException(throwable: Throwable) {
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    FileWriter(file, true).use { fileWriter ->
                        PrintWriter(fileWriter).use { printWriter ->
                            throwable.printStackTrace(printWriter)
                            printWriter.append("\n")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'écriture de l'exception dans le fichier de logs", e)
        }
    }
    
    /**
     * Obtient un timestamp formaté pour les logs
     */
    private fun getTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date())
    }
}