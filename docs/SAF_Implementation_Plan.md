# Plan d'implémentation du Storage Access Framework (SAF)

## Problématique actuelle

### Description du problème
L'application utilise actuellement une approche directe pour sauvegarder les données dans le dossier `Documents/AATT/` du stockage externe sous format JSON. Cependant, cette approche présente un défaut majeur : **après désinstallation puis réinstallation de l'application, les fichiers de sauvegarde existants ne sont plus visibles dans l'application**, bien qu'ils soient toujours physiquement présents sur l'appareil.

### Cause technique
Ce problème est dû aux restrictions de sécurité introduites dans les versions récentes d'Android :
1. Chaque application reçoit un identifiant unique (UID) lors de l'installation
2. Après une réinstallation, l'application reçoit un nouvel UID différent
3. Les fichiers créés précédemment sont associés à l'ancien UID
4. Même avec les permissions générales de stockage, l'application avec le nouvel UID ne peut pas accéder aux fichiers créés par l'ancienne instance

## Solution proposée : Storage Access Framework (SAF)

Le Storage Access Framework (SAF) est l'approche recommandée par Google pour gérer l'accès aux fichiers de manière persistante et compatible avec les restrictions de sécurité d'Android.

### Avantages du SAF
1. **Permissions persistantes** : Les URI de documents peuvent persister à travers les réinstallations
2. **Meilleure expérience utilisateur** : Interface de sélection de fichiers standard
3. **Compatibilité étendue** : Fonctionne sur toutes les versions d'Android récentes
4. **Support du cloud** : Accès transparent aux documents dans le cloud (Google Drive, etc.)
5. **Sécurité** : Respecte le modèle de sécurité d'Android

## Plan d'implémentation

### Phase 1 : Préparation de l'infrastructure

1. **Créer une nouvelle classe utilitaire `SAFBackupHelper`**
   - Responsable de toutes les opérations SAF
   - Gestion des URI persistants
   - Méthodes pour sauvegarder, charger et lister les fichiers

2. **Implémenter le stockage des URI persistants**
   - Utiliser `SharedPreferences` pour stocker les URI de documents auxquels l'application a reçu un accès persistant
   - Créer des méthodes utilitaires pour gérer ces URI

3. **Modifier le `Repository` pour supporter les deux approches**
   - Maintenir la compatibilité avec l'ancienne approche pendant la transition
   - Préparer les méthodes pour utiliser le nouveau helper SAF

### Phase 2 : Implémentation des opérations de base

#### 1. Opération de sauvegarde (exportation)
```kotlin
// Dans MainActivity ou autre Activity
private val createDocumentLauncher = registerForActivityResult(
    ActivityResultContracts.CreateDocument("application/json")
) { uri ->
    uri?.let {
        // Persister l'accès à l'URI
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        
        // Sauvegarder les données
        viewModelScope.launch {
            safBackupHelper.exportToJson(uri)
        }
    }
}

// Fonction pour déclencher la sauvegarde
fun startBackup(fileName: String = "aatt_backup_${getCurrentDateTime()}.json") {
    createDocumentLauncher.launch(fileName)
}
```

#### 2. Opération de restauration (importation)
```kotlin
private val openDocumentLauncher = registerForActivityResult(
    ActivityResultContracts.OpenDocument()
) { uri ->
    uri?.let {
        // Persister l'accès à l'URI
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        
        // Restaurer les données
        viewModelScope.launch {
            val success = safBackupHelper.importFromJson(uri)
            // Gérer le résultat...
        }
    }
}

// Fonction pour déclencher la restauration
fun startRestore() {
    openDocumentLauncher.launch(arrayOf("application/json"))
}
```

#### 3. Listage des sauvegardes disponibles
Pour lister les sauvegardes auxquelles l'application a un accès persistant :
```kotlin
fun listSavedBackups(): List<UriBackupInfo> {
    val persistedUris = getPersistedUris() // Récupérer depuis SharedPreferences
    return persistedUris.map { uri ->
        // Extraire les métadonnées (nom, date, etc.)
        UriBackupInfo(
            uri = uri,
            displayName = getDisplayName(uri),
            lastModified = getLastModified(uri)
        )
    }
}
```

### Phase 3 : Intégration dans l'interface utilisateur

1. **Modifier `EditScreen`**
   - Adapter les boutons de sauvegarde/restauration pour utiliser SAF
   - Utiliser les launchers d'activité pour les opérations de fichiers
   - Mettre à jour l'affichage des sauvegardes disponibles

2. **Créer de nouveaux composants UI si nécessaire**
   - Liste des URI persistants
   - Option pour retirer l'accès à certains fichiers
   - Interface pour les nouvelles fonctionnalités

3. **Optimiser l'expérience utilisateur**
   - Suggérer un nom de fichier par défaut avec date/heure
   - Afficher des indicateurs de progression
   - Gérer les erreurs avec des messages informatifs

### Phase 4 : Migration et compatibilité

1. **Stratégie de migration pour les utilisateurs existants**
   - Détecter les anciennes sauvegardes au premier lancement après mise à jour
   - Proposer de les migrer vers le nouveau système
   - Guider l'utilisateur dans le processus

2. **Fonctionnalités de compatibilité**
   - Option pour importer d'anciennes sauvegardes manuellement
   - Support des deux formats (direct et SAF) pendant la période de transition

## Détails techniques d'implémentation

### Classe SAFBackupHelper

```kotlin
class SAFBackupHelper(
    private val context: Context,
    private val repository: ActivityRepository
) {
    companion object {
        private const val TAG = "SAFBackupHelper"
        private const val SHARED_PREFS_NAME = "saf_backup_prefs"
        private const val KEY_PERSISTED_URIS = "persisted_uris"
    }

    // Sérialisation/Désérialisation JSON
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create()

    // Sauvegarder dans un fichier via SAF
    suspend fun exportToJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Récupérer les activités
            val activities = repository.getAllActivitiesOneShot()
            
            // 2. Créer l'objet de sauvegarde
            val backup = DatabaseBackup(
                timestamp = System.currentTimeMillis(),
                version = 1,
                activities = activities
            )
            
            // 3. Sérialiser en JSON
            val jsonString = gson.toJson(backup)
            
            // 4. Écrire dans le fichier via ContentResolver
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
            
            // 5. Stocker l'URI pour accès futur
            storePersistedUri(uri)
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to JSON via SAF", e)
            return@withContext false
        }
    }
    
    // Restaurer depuis un fichier via SAF
    suspend fun importFromJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Lire le fichier via ContentResolver
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: throw IOException("Could not read from URI: $uri")
            
            // 2. Désérialiser le JSON
            val backup: DatabaseBackup = gson.fromJson(
                jsonString,
                object : TypeToken<DatabaseBackup>() {}.type
            )
            
            // 3. Vider la base de données actuelle
            repository.clearAllActivities()
            
            // 4. Importer les activités
            backup.activities.forEach { activity ->
                repository.importActivity(activity)
            }
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing from JSON via SAF", e)
            return@withContext false
        }
    }
    
    // Gestion des URI persistants
    private fun storePersistedUri(uri: Uri) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val persistedUris = getPersistedUrisSet()
        
        // Ajouter le nouvel URI s'il n'existe pas déjà
        if (uri.toString() !in persistedUris) {
            persistedUris.add(uri.toString())
            prefs.edit().putStringSet(KEY_PERSISTED_URIS, persistedUris).apply()
        }
    }
    
    // Récupérer tous les URI persistants
    fun getPersistedUris(): List<Uri> {
        return getPersistedUrisSet().map { Uri.parse(it) }
    }
    
    private fun getPersistedUrisSet(): MutableSet<String> {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_PERSISTED_URIS, mutableSetOf()) ?: mutableSetOf()
    }
    
    // Obtenir les métadonnées d'un fichier
    fun getDisplayName(uri: Uri): String {
        val cursor = context.contentResolver.query(
            uri, 
            arrayOf(OpenableColumns.DISPLAY_NAME), 
            null, null, null
        )
        
        return cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return@use it.getString(displayNameIndex)
                }
            }
            return@use "Sauvegarde sans nom"
        } ?: "Sauvegarde sans nom"
    }
    
    fun getLastModified(uri: Uri): Long {
        // Certains URI ne supportent pas cette opération
        try {
            val cursor = context.contentResolver.query(
                uri, 
                arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED), 
                null, null, null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val lastModIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    if (lastModIndex != -1) {
                        return it.getLong(lastModIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get last modified date for URI: $uri", e)
        }
        
        return System.currentTimeMillis()
    }
    
    // Classe représentant les informations d'une sauvegarde
    data class UriBackupInfo(
        val uri: Uri,
        val displayName: String,
        val lastModified: Long
    )
    
    // Même structure de sauvegarde que la version actuelle pour compatibilité
    data class DatabaseBackup(
        val timestamp: Long,
        val version: Int,
        val activities: List<Activity>
    )
}
```

## Calendrier d'implémentation suggéré

1. **Semaine 1**: Infrastructure et opérations de base
   - Implémenter SAFBackupHelper
   - Créer les launchers d'activité pour SAF
   - Tester les opérations de base isolément

2. **Semaine 2**: Intégration UI et tests
   - Modifier l'interface utilisateur pour utiliser SAF
   - Tester sur différentes versions d'Android
   - Implémenter la gestion des erreurs

3. **Semaine 3**: Migration et finalisation
   - Développer la stratégie de migration
   - Effectuer des tests approfondis
   - Vérifier la compatibilité avec les sauvegardes existantes

## Risques et mitigations

| Risque | Impact | Mitigation |
|--------|--------|------------|
| Accès révoqué aux URI | Perte d'accès aux sauvegardes | Stocker dans un dossier standard pour faciliter la relocalisation |
| Compatibilité entre versions d'Android | Comportement inconsistant | Tester sur plusieurs API levels et appareils |
| Résistance utilisateur au changement | Confusion, frustration | Interface claire et guide de migration |
| Échec de migration des anciennes sauvegardes | Perte de données historiques | Option d'import manuel et support temporaire des deux systèmes |

## Conclusion

L'implémentation du Storage Access Framework (SAF) résoudra le problème de perte d'accès aux sauvegardes après réinstallation. Cette approche est alignée avec les recommandations de Google pour la gestion des fichiers sur Android moderne et offre plusieurs avantages supplémentaires en termes de sécurité et de flexibilité.

En suivant ce plan d'implémentation détaillé, nous pourrons migrer progressivement vers SAF tout en maintenant la compatibilité avec les données existantes des utilisateurs.