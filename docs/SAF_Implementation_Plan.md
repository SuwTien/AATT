# Storage Access Framework (SAF) - Documentation

## Problématique d'origine

### Description du problème
L'application utilisait initialement une approche directe pour sauvegarder les données dans le dossier `Documents/AATT/` du stockage externe sous format JSON. Cette approche présentait un défaut majeur : **après désinstallation puis réinstallation de l'application, les fichiers de sauvegarde existants n'étaient plus visibles dans l'application**, bien qu'ils soient toujours physiquement présents sur l'appareil.

### Cause technique
Ce problème était dû aux restrictions de sécurité introduites dans les versions récentes d'Android :
1. Chaque application reçoit un identifiant unique (UID) lors de l'installation
2. Après une réinstallation, l'application reçoit un nouvel UID différent
3. Les fichiers créés précédemment sont associés à l'ancien UID
4. Même avec les permissions générales de stockage, l'application avec le nouvel UID ne peut pas accéder aux fichiers créés par l'ancienne instance

## Solution implémentée : Storage Access Framework (SAF)

Le Storage Access Framework (SAF) est l'approche recommandée par Google pour gérer l'accès aux fichiers de manière persistante et compatible avec les restrictions de sécurité d'Android.

### Avantages du SAF
1. **Permissions persistantes** : Les URI de documents persistent à travers les réinstallations
2. **Meilleure expérience utilisateur** : Interface de sélection de fichiers standard d'Android
3. **Compatibilité étendue** : Fonctionne sur toutes les versions d'Android récentes
4. **Support du cloud** : Accès transparent aux documents dans le cloud (Google Drive, etc.)
5. **Sécurité** : Respecte le modèle de sécurité d'Android

## Architecture implémentée

### Vue d'ensemble
L'implémentation du SAF dans AATT repose sur trois composants principaux :

1. **SAFBackupHelper** : Classe utilitaire qui gère toutes les opérations sur les fichiers via SAF
2. **EditViewModel** : Gère les interactions entre l'UI et le système de sauvegarde
3. **EditScreen** : Interface utilisateur avec les launchers d'activité SAF et les contrôles utilisateur

### Flux des opérations

#### Sauvegarde (Exportation)
1. L'utilisateur sélectionne d'abord un dossier de destination (une seule fois)
2. L'utilisateur déclenche une opération de sauvegarde et fournit éventuellement un nom
3. Les données sont sérialisées en JSON avec le même format que précédemment
4. Le fichier est écrit dans le dossier choisi via ContentResolver
5. L'URI du dossier est stocké de manière persistante pour un accès futur

#### Restauration (Importation)
1. L'utilisateur sélectionne une sauvegarde dans la liste
2. L'application lit le contenu du fichier via ContentResolver
3. Les données sont désérialisées depuis JSON
4. La base de données locale est mise à jour avec les données importées
5. L'UI est rafraîchie pour refléter les nouvelles données

#### Listage des sauvegardes
1. L'application récupère l'URI du dossier de sauvegarde
2. Elle utilise DocumentFile pour lister tous les fichiers JSON dans ce dossier
3. Les métadonnées (nom, date de modification) sont extraites pour chaque fichier
4. Les sauvegardes sont affichées de manière triée (la plus récente en premier)

## Composants techniques

### SAFBackupHelper

La classe `SAFBackupHelper` est le cœur de notre implémentation SAF. Voici ses principales fonctionnalités :

```kotlin
class SAFBackupHelper(
    private val context: Context,
    private val repository: ActivityRepository
) {
    // Gestion du dossier de sauvegarde
    fun setBackupDirectoryUri(uri: Uri)
    fun getBackupDirectoryUri(): Uri?
    fun hasBackupDirectory(): Boolean
    
    // Opérations sur les fichiers
    suspend fun exportToJson(backupName: String = ""): Uri?
    suspend fun importFromJson(uri: Uri): Boolean
    suspend fun listBackups(): List<BackupInfo>
    suspend fun deleteBackup(uri: Uri): Boolean
    
    // Classes de données
    data class BackupInfo(
        val name: String,
        val uri: Uri,
        val lastModified: Long
    )
    
    data class DatabaseBackup(
        val timestamp: Long,
        val version: Int,
        val activities: List<Activity>
    )
}
```

#### Points clés d'implémentation :

1. **Gestion des URI persistants** : 
   - Utilisation de `takePersistableUriPermission` pour maintenir l'accès
   - Stockage des URI dans SharedPreferences pour les récupérer après redémarrage

2. **Utilisation de DocumentFile** :
   - Approche recommandée pour manipuler les fichiers via SAF
   - Abstraction qui simplifie les opérations sur les documents

3. **Opérations asynchrones** :
   - Toutes les opérations sur les fichiers sont suspendues et exécutées sur Dispatchers.IO
   - Évite de bloquer le thread principal

### EditViewModel

Le ViewModel coordonne les opérations de sauvegarde et restauration :

```kotlin
class EditViewModel(private val repository: ActivityRepository) : ViewModel() {
    // États observables
    val backups: StateFlow<List<SAFBackupHelper.BackupInfo>>
    val hasSAFDirectory: StateFlow<Boolean>
    val operationResult: StateFlow<OperationResult?>
    
    // Fonctions de gestion SAF
    fun checkSAFDirectory(context: Context)
    fun setBackupDirectoryUri(context: Context, uri: Uri)
    fun refreshSAFBackupsList(context: Context)
    
    // Opérations de sauvegarde/restauration
    fun backupDatabaseSAF(context: Context, backupName: String = "")
    fun restoreDatabaseSAF(context: Context, uri: Uri)
    fun deleteBackupSAF(context: Context, uri: Uri)
}
```

### EditScreen

L'interface utilisateur inclut maintenant des launchers pour la sélection de dossier :

```kotlin
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    navigateBack: () -> Unit
) {
    // État SAF
    val hasSAFDirectory by viewModel.hasSAFDirectory.collectAsState()
    
    // Launcher pour sélectionner le dossier de sauvegarde
    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.setBackupDirectoryUri(context, uri)
        }
    }
    
    // ...
    
    // Nouveaux composants UI pour SAF
    SAFBackupListDialog(
        backups = backups,
        onDismiss = { /* ... */ },
        onRestore = { /* ... */ },
        onDelete = { /* ... */ }
    )
}
```

## Guide d'utilisation

### Configuration initiale

Lors de la première utilisation des fonctionnalités de sauvegarde/restauration, l'utilisateur devra :

1. **Sélectionner un dossier de sauvegarde** :
   - L'application affichera une boîte de dialogue demandant à l'utilisateur de sélectionner un dossier
   - Ce dossier sera utilisé pour toutes les opérations futures de sauvegarde
   - L'utilisateur peut modifier ce dossier à tout moment via l'option "Dossier" du menu

2. **Permissions persistantes** :
   - L'application demandera automatiquement les permissions persistantes pour ce dossier
   - Ces permissions seront conservées après redémarrage et même après réinstallation

### Opérations quotidiennes

Une fois la configuration initiale effectuée, les opérations suivantes sont disponibles :

#### Création d'une sauvegarde
- Accéder à l'écran d'édition
- Sélectionner "Enregistrer" dans le menu
- Entrer un nom personnalisé (optionnel) ou utiliser la date/heure automatique
- Confirmer la sauvegarde

#### Restauration d'une sauvegarde
- Accéder à l'écran d'édition
- Sélectionner "Charger" dans le menu
- Sélectionner une sauvegarde dans la liste
- Confirmer la restauration

#### Gestion des sauvegardes
- Liste triée par date (la plus récente en premier)
- Possibilité de supprimer les sauvegardes obsolètes
- Affichage des métadonnées (nom, date de modification)

## Bonnes pratiques

1. **Nommage des sauvegardes** :
   - Utilisez des noms significatifs pour vos sauvegardes importantes
   - Exemple : "avant_maj_données", "fin_semaine_12", etc.

2. **Emplacement des sauvegardes** :
   - Préférez un dossier dédié à l'application
   - Évitez les dossiers système ou temporaires
   - Si vous utilisez Google Drive, assurez-vous que la synchronisation est activée

3. **Fréquence des sauvegardes** :
   - Créez une sauvegarde avant toute modification majeure
   - Effectuez des sauvegardes régulières selon votre usage

## Notes techniques

### Format du fichier de sauvegarde

Le format JSON des fichiers de sauvegarde reste inchangé pour maintenir la compatibilité :

```json
{
  "timestamp": 1714042001234,
  "version": 1,
  "activities": [
    {
      "id": 1,
      "type": "VS",
      "startTime": 1714041000000,
      "endTime": 1714042000000,
      "isActive": false
    },
    ...
  ]
}
```

### Compatibilité

- **Versions d'Android** : Compatible avec Android 10+ (API 29+)
- **Stockages supportés** : Stockage interne, cartes SD, Google Drive, et autres fournisseurs de documents

### Dépendances

Cette implémentation nécessite la bibliothèque DocumentFile :
```kotlin
implementation("androidx.documentfile:documentfile:1.0.1")
```

## Conclusion

L'implémentation du Storage Access Framework résout efficacement le problème de perte d'accès aux sauvegardes après réinstallation. Elle offre également une meilleure expérience utilisateur et une plus grande flexibilité dans le choix de l'emplacement de sauvegarde.

Cette nouvelle approche est plus robuste, conforme aux recommandations actuelles de Google, et permet d'étendre facilement les fonctionnalités de sauvegarde dans le futur.