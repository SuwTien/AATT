package fr.bdst.aatt.data.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Classe singleton pour gérer les événements globaux de l'application
 */
object AppEvents {
    // Flow pour les événements de restauration de base de données
    private val _databaseRestoreEvent = MutableSharedFlow<DatabaseRestoreEvent>()
    val databaseRestoreEvent: SharedFlow<DatabaseRestoreEvent> = _databaseRestoreEvent.asSharedFlow()
    
    // Émet un événement de restauration de base de données
    suspend fun emitDatabaseRestored(success: Boolean) {
        _databaseRestoreEvent.emit(DatabaseRestoreEvent(success))
    }
    
    // Classe représentant un événement de restauration
    data class DatabaseRestoreEvent(val success: Boolean)
}