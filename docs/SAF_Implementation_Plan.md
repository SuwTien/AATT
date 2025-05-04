# Plan d'implémentation du Storage Access Framework (SAF)

## Contexte

L'application AATT stocke actuellement ses sauvegardes dans le dossier Documents/AATT du stockage externe. Cette approche présente un problème majeur : après désinstallation/réinstallation de l'application, l'utilisateur ne peut plus accéder visuellement à ses anciennes sauvegardes car le nouvel UID de l'application ne correspond plus à celui qui a créé les fichiers.

## Solution : Storage Access Framework (SAF)

Le Storage Access Framework d'Android est conçu pour résoudre ce problème en permettant à l'application de demander un accès persistant à un dossier spécifique choisi par l'utilisateur.

### Avantages de SAF

1. Accès persistant aux fichiers même après réinstallation
2. Interface utilisateur standard pour la sélection de fichiers et dossiers
3. Compatible avec les stockages externes et cloud (Google Drive, etc.)
4. Respecte les nouvelles restrictions de confidentialité d'Android

## État d'implémentation

### ✅ Terminé

1. **Création de la classe SAFBackupHelper** : Implémentée pour gérer toutes les opérations de sauvegarde via SAF.
2. **Migration de EditViewModel** : Toutes les opérations de sauvegarde/restauration utilisent maintenant SAFBackupHelper.
3. **Extraction des modèles communs** : La classe `DatabaseBackup` a été extraite dans `BackupModels.kt`.
4. **Suppression du code obsolète** : Les anciennes classes `DatabaseBackupHelper` et `ExternalStorageHelper` ont été supprimées.
5. **Préservation des constantes** : Les constantes essentielles ont été déplacées dans `DatabaseConstants.kt`.
6. **Correction du problème de build release** : 
   - Ajout de règles ProGuard spécifiques pour préserver les informations de type générique
   - Modification de l'approche de désérialisation pour être plus robuste face à l'obfuscation

### 🔄 Tâches de maintenance future (optionnelles)

1. **Optimisations des performances** : Si nécessaire, optimiser le processus de sauvegarde/restauration pour les grands jeux de données.
2. **Améliorations de l'interface utilisateur** : Envisager d'ajouter une progression visuelle lors des opérations de sauvegarde/restauration longues.
3. **Gestion des erreurs avancée** : Implémenter une gestion plus fine des erreurs pour les cas particuliers (stockage plein, fichiers corrompus, etc.).

## Architecture technique

### SAFBackupHelper

La classe `SAFBackupHelper` est le cœur de notre implémentation SAF. Voici ses principales fonctionnalités :

```kotlin
class SAFBackupHelper(
    private val context: Context,
    private val repository: ActivityRepository
) {
    companion object {
        // Type static pour éviter les problèmes avec TypeToken et R8
        private val DATABASE_BACKUP_TYPE: Type = object : TypeToken<DatabaseBackup>() {}.type
    }

    // Gestion du dossier de sauvegarde
    fun setBackupDirectoryUri(uri: Uri)
    fun getBackupDirectoryUri(): Uri?
    fun hasBackupDirectory(): Boolean
    
    // Opérations de sauvegarde/restauration
    suspend fun exportToJson(backupName: String = ""): Uri?
    suspend fun importFromJson(uri: Uri): Boolean
    suspend fun listBackups(): List<BackupInfo>
    suspend fun deleteBackup(uri: Uri): Boolean
    
    // Classe interne pour les informations de sauvegarde
    data class BackupInfo(val name: String, val uri: Uri, val lastModified: Long)
}
```

### BackupModels

La classe `BackupModels` contient la structure de données commune pour les sauvegardes :

```kotlin
data class DatabaseBackup(
    val timestamp: Long,
    val version: Int,
    val activities: List<Activity>
)
```

### DatabaseConstants

La classe `DatabaseConstants` contient les constantes essentielles préservées de l'ancien système :

```kotlin
object DatabaseConstants {
    const val DB_FOLDER_NAME = "AATT"
    const val DB_NAME = "aatt_database.db"
}
```

### Règles ProGuard spécifiques

Les règles ProGuard suivantes sont essentielles pour garantir le bon fonctionnement de GSON avec le Storage Access Framework en version release :

```
# Règles CRITIQUES pour préserver les informations de type générique utilisées par GSON
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Préserver les classes de modèle pour GSON
-keep class fr.bdst.aatt.data.util.DatabaseBackup { *; }
-keep class fr.bdst.aatt.data.model.** { *; }
```

## Flux d'utilisation

1. **Premier lancement** : L'utilisateur est invité à sélectionner un dossier de sauvegarde
2. **Sauvegarde** : L'utilisateur peut créer une sauvegarde avec un nom personnalisé
3. **Restauration** : L'utilisateur peut sélectionner une sauvegarde existante à restaurer
4. **Gestion** : L'utilisateur peut lister et supprimer des sauvegardes

## Problèmes résolus

1. **Accès après réinstallation** : Grâce au SAF, l'utilisateur peut accéder à ses sauvegardes même après réinstallation de l'application.

2. **Désérialisation en version release** : Le problème de désérialisation GSON en version release a été résolu par :
   - L'utilisation d'un type statique pour éviter la création d'instances TypeToken à la volée
   - L'ajout de règles ProGuard spécifiques pour préserver les signatures génériques
   - L'implémentation d'une méthode de secours pour la désérialisation

3. **Nettoyage du code** : Les anciens systèmes de sauvegarde ont été entièrement supprimés, avec extraction des parties communes nécessaires.