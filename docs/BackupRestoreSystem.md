# Système de Sauvegarde et Restauration de Base de Données

## Aperçu du système

L'application AATT dispose d'un système complet de sauvegarde et restauration des données utilisateur. Ce document décrit l'implémentation et l'utilisation de ce système.

## Approche basée sur les données (JSON)

À partir d'avril 2025, nous avons migré d'une approche basée sur les fichiers (copie directe de la base de données SQLite) vers une approche basée sur les données (exportation/importation JSON). Cette migration résout plusieurs problèmes rencontrés avec l'ancienne approche.

### Avantages de l'approche JSON

1. **Plus robuste** : Évite les problèmes liés aux connexions de base de données Room et aux fichiers SQLite sous-jacents
2. **Meilleure compatibilité** : Fonctionne de manière fiable sur différentes versions d'Android
3. **Indépendance du schéma** : Permet de restaurer des données même si le schéma de la base de données a légèrement évolué
4. **Facilité de débogage** : Les fichiers JSON sont lisibles par l'humain, ce qui facilite le dépannage
5. **Flexibilité** : Permet d'importer/exporter des données partielles si nécessaire

### Architecture

Le système est composé des éléments suivants :

1. **DatabaseBackupHelper** : Classe utilitaire principale qui gère l'exportation et l'importation des données vers/depuis des fichiers JSON
2. **ActivityRepository** : Augmenté avec des méthodes spécifiques pour l'exportation/importation
3. **EditViewModel** : Interface entre l'UI et le système de sauvegarde/restauration

## Fonctionnement

### Sauvegarde (Exportation)

Le processus de sauvegarde comprend les étapes suivantes :

1. L'utilisateur demande une sauvegarde via l'interface utilisateur
2. Le ViewModel appelle `DatabaseBackupHelper.exportToJson()`
3. Toutes les activités sont récupérées via le repository
4. Les données sont encapsulées dans un objet `DatabaseBackup` avec des métadonnées (timestamp, version)
5. L'objet est sérialisé en JSON et écrit dans un fichier dans le dossier Documents/AATT/
6. Le nom du fichier inclut la date/heure ou un nom personnalisé fourni par l'utilisateur

### Restauration (Importation)

Le processus de restauration comprend les étapes suivantes :

1. L'utilisateur sélectionne une sauvegarde à restaurer
2. Le ViewModel appelle `DatabaseBackupHelper.importFromJson()`
3. Le fichier JSON est lu et désérialisé en objet `DatabaseBackup`
4. Toutes les activités actuelles sont effacées de la base de données
5. Les activités contenues dans la sauvegarde sont importées une par une
6. Un événement global est émis pour informer l'application du changement
7. Les données affichées sont rafraîchies

## Format des fichiers de sauvegarde

Les sauvegardes sont stockées sous forme de fichiers JSON avec l'extension `.json` dans le dossier public Documents/AATT/.

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

### Exportation (Sauvegarde)

```kotlin
val backupHelper = DatabaseBackupHelper(context, repository)
val backupPath = backupHelper.exportToJson("ma_sauvegarde")
```

### Importation (Restauration)

```kotlin
val backupHelper = DatabaseBackupHelper(context, repository)
val success = backupHelper.importFromJson("/chemin/vers/sauvegarde.json")
```

### Listing des sauvegardes

```kotlin
val backupHelper = DatabaseBackupHelper(context, repository)
val backups = backupHelper.listBackups()
```

## Considérations futures

Voici quelques améliorations possibles pour les versions futures :

1. Support pour les sauvegardes automatiques programmées
2. Compression des fichiers de sauvegarde
3. Sauvegarde dans le cloud (Google Drive, etc.)
4. Sauvegarde sélective/partielle (par période, par type d'activité, etc.)
5. Chiffrement des données de sauvegarde pour plus de sécurité