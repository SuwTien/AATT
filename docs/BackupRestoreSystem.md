# Système de Sauvegarde et Restauration de Base de Données

## Aperçu du système

L'application AATT dispose d'un système complet de sauvegarde et restauration des données utilisateur. Ce document décrit l'implémentation et l'utilisation de ce système.

## Historique des approches

### Version initiale (avant avril 2025)
La première version utilisait une copie directe des fichiers de base de données SQLite.

### JSON direct (avril 2025)
Migration vers une approche basée sur les données (exportation/importation JSON) dans le dossier Documents/AATT/.

### Storage Access Framework (mai 2025)
La version actuelle utilise le Storage Access Framework (SAF) d'Android pour résoudre les problèmes de persistance des permissions après réinstallation de l'application.

## Approche actuelle : Storage Access Framework (SAF)

L'implémentation actuelle utilise le SAF qui offre plusieurs avantages :

1. **Permissions persistantes** : Les URI de documents persistent après réinstallation de l'application
2. **Meilleure expérience utilisateur** : Interface de sélection de fichiers standard d'Android
3. **Flexibilité de stockage** : Support pour le stockage local et cloud (Google Drive, etc.)
4. **Sécurité renforcée** : Respect du modèle de sécurité d'Android

### Architecture

Le système SAF est composé des éléments suivants :

1. **SAFBackupHelper** : Classe utilitaire principale qui gère les opérations via SAF
2. **ActivityRepository** : Contient les méthodes pour récupérer et importer les données
3. **EditViewModel** : Interface entre l'UI et le système de sauvegarde/restauration

## Fonctionnement

### Configuration initiale

Avant d'utiliser les fonctionnalités de sauvegarde/restauration :

1. L'utilisateur doit sélectionner un dossier de destination via le sélecteur de documents Android
2. L'application obtient des permissions persistantes pour ce dossier
3. L'URI du dossier est stocké dans les préférences partagées pour un accès futur

### Sauvegarde (Exportation)

Le processus de sauvegarde comprend les étapes suivantes :

1. L'utilisateur demande une sauvegarde via l'interface utilisateur
2. Le ViewModel appelle `SAFBackupHelper.exportToJson()`
3. Toutes les activités sont récupérées via le repository
4. Les données sont encapsulées dans un objet `DatabaseBackup` avec métadonnées (timestamp, version)
5. L'objet est sérialisé en JSON et écrit dans un fichier dans le dossier sélectionné via ContentResolver
6. Le nom du fichier inclut la date/heure ou un nom personnalisé fourni par l'utilisateur

### Restauration (Importation)

Le processus de restauration comprend les étapes suivantes :

1. L'utilisateur sélectionne une sauvegarde à restaurer dans la liste
2. Le ViewModel appelle `SAFBackupHelper.importFromJson()`
3. Le fichier JSON est lu via ContentResolver et désérialisé en objet `DatabaseBackup`
4. Toutes les activités actuelles sont effacées de la base de données
5. Les activités contenues dans la sauvegarde sont importées une par une
6. Un événement global est émis pour informer l'application du changement
7. Les données affichées sont rafraîchies

## Format des fichiers de sauvegarde

Les sauvegardes sont stockées sous forme de fichiers JSON avec l'extension `.json` dans le dossier sélectionné par l'utilisateur.

Structure d'un fichier de sauvegarde :
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

## Utilisation depuis le code

### Configuration du dossier de sauvegarde

```kotlin
// Dans EditViewModel
fun setBackupDirectoryUri(context: Context, uri: Uri) {
    val safBackupHelper = SAFBackupHelper(context, repository)
    safBackupHelper.setBackupDirectoryUri(uri)
}

// Dans EditScreen (Compose)
val directoryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree()
) { uri ->
    uri?.let {
        viewModel.setBackupDirectoryUri(context, uri)
    }
}
```

### Exportation (Sauvegarde)

```kotlin
val safBackupHelper = SAFBackupHelper(context, repository)
val backupUri = safBackupHelper.exportToJson("ma_sauvegarde")
```

### Importation (Restauration)

```kotlin
val safBackupHelper = SAFBackupHelper(context, repository)
val success = safBackupHelper.importFromJson(uri)
```

### Listing des sauvegardes

```kotlin
val safBackupHelper = SAFBackupHelper(context, repository)
val backups = safBackupHelper.listBackups()
```

## Gestion des URI persistants

Le SAF nécessite l'utilisation de permissions persistantes pour maintenir l'accès aux fichiers, même après un redémarrage de l'application :

```kotlin
// Obtenir les permissions persistantes
context.contentResolver.takePersistableUriPermission(
    uri,
    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
)

// Stocker l'URI dans les préférences
context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    .edit()
    .putString(KEY_BACKUP_DIRECTORY_URI, uri.toString())
    .apply()
```

## Considérations futures

Voici quelques améliorations possibles pour les versions futures :

1. Support pour les sauvegardes automatiques programmées
2. Compression des fichiers de sauvegarde
3. Migration automatique des anciennes sauvegardes vers le nouveau système SAF
4. Sauvegarde sélective/partielle (par période, par type d'activité, etc.)
5. Chiffrement des données de sauvegarde pour plus de sécurité
6. Synchronisation bidirectionnelle avec des services cloud