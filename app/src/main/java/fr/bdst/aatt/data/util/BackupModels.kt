package fr.bdst.aatt.data.util

import fr.bdst.aatt.data.model.Activity

/**
 * Modèles de données pour les sauvegardes
 * Classe commune partagée entre les anciens et nouveaux systèmes de sauvegarde
 * pour assurer la compatibilité des formats et faciliter la migration
 */
data class DatabaseBackup(
    val timestamp: Long,
    val version: Int,
    val activities: List<Activity>
)