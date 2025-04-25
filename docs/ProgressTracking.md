# Suivi d'avancement du projet AATT

## Fonctionnalités implémentées

### Architecture de base
- [x] Structure MVVM mise en place
- [x] Base de données Room configurée
- [x] Repository pour les opérations sur les activités
- [x] ViewModel pour les écrans principaux
- [x] Navigation entre les écrans avec Jetpack Compose Navigation

### Modèles de données
- [x] Entités Activity et enum ActivityType définis
- [x] Converters pour les types de données complexes
- [x] ActivityDao pour les opérations CRUD

### Interface utilisateur
- [x] MainScreen avec boutons d'activité et affichage de l'activité en cours
- [x] EditScreen pour gérer les activités terminées
- [x] StatsScreen (structure de base)
- [x] Composants réutilisables (ActivityButton, etc.)

### Stockage des données
- [x] Base de données active dans le stockage interne de l'application
- [x] Système de sauvegarde/restauration JSON dans le dossier Documents/AATT/ externe
- [x] Gestion des permissions de stockage selon les versions d'Android

## Solutions techniques mises en œuvre

### Gestion du stockage externe pour les sauvegardes
Nous avons choisi de stocker la base de données active dans l'espace privé de l'application pour des raisons de sécurité et de performance, tout en permettant les sauvegardes dans le stockage externe pour la persistance.

**Solution implémentée :**
1. Une classe `DatabaseBackupHelper` gère les opérations d'exportation/importation en format JSON dans le dossier Documents/AATT/
2. Permissions adaptées selon la version d'Android:
   - Android 13+ (API 33+): Permissions READ_MEDIA_*
   - Versions antérieures: READ/WRITE_EXTERNAL_STORAGE
3. Mécanisme de sauvegarde avec horodatage automatique ou nom personnalisé
4. Stratégie de repli vers le stockage interne en cas d'échec
5. Importation/exportation basée sur les données plutôt que sur la manipulation directe des fichiers de base de données

### Gestion des activités actives
Pour gérer le comportement spécial des pauses qui peuvent coexister avec d'autres activités:

**Solution implémentée :**
1. Une liste d'activités actives plutôt qu'une seule activité active
2. Logique d'exclusion: les activités VS, ROUTE et DOMICILE s'excluent mutuellement
3. Exception pour les PAUSES qui peuvent coexister avec d'autres types

### Calcul des statistiques
Architecture flexible pour permettre le calcul des statistiques avec des règles métier spécifiques:

**Solution implémentée :**
1. Classe `StatisticsCalculator` qui encapsule les règles métier (ex: déduction de 1h30 par jour pour les ROUTE)
2. Affichage par onglets (jour, semaine, mois)

## Défis rencontrés et solutions

### Configuration de l'environnement de build
**Problème :** Incompatibilités entre versions de bibliothèques et plugins Gradle.

**Solution :** 
- Mise à jour du fichier libs.versions.toml avec des versions compatibles
- Configuration du plugin kapt sans référence de version explicite
- Gestion des dépendances pour Room, Navigation et Material3

### API expérimentale de Material3
**Problème :** Avertissements et erreurs liés à l'utilisation d'API expérimentales dans Material3.

**Solution :**
- Ajout de l'annotation `@OptIn(ExperimentalMaterial3Api::class)` 
- Simplification de certains composants UI pour éviter les problèmes de compatibilité

### Problème de restauration de la base de données
**Problème :** La restauration de la base de données échouait avec l'erreur "connection pool has been closed" et les activités n'étaient pas correctement restaurées.

**Solution :**
- Migration vers une approche basée sur les données (JSON) plutôt que sur la manipulation directe des fichiers de base de données
- Création d'une nouvelle classe `DatabaseBackupHelper` pour gérer l'exportation/importation des données
- Utilisation de Gson pour sérialiser/désérialiser les données
- Élimination des problèmes liés aux connexions Room lors de la restauration

## Prochaines étapes

### Fonctionnalités à implémenter
- [x] Interface utilisateur pour la gestion des sauvegardes/restaurations
- [ ] Calculs statistiques réels (avec rules business)
- [ ] Boîtes de dialogue pour l'édition des heures de début/fin des activités
- [ ] Amélioration de l'UI/UX

### Améliorations techniques prévues
- [ ] Tests unitaires pour les principales fonctionnalités
- [ ] Optimisations de performance
- [ ] Surveillance des problèmes potentiels liés au stockage externe

## Journal des modifications

### 2025-04-26
- Migration du système de sauvegarde/restauration vers une approche basée sur JSON
- Création d'une classe `DatabaseBackupHelper` pour gérer l'exportation/importation des données
- Résolution du problème "connection pool has been closed" lors de la restauration
- Ajout de documentation détaillée sur le nouveau système dans BackupRestoreSystem.md
- Interface utilisateur de gestion des sauvegardes améliorée et fiable

### 2025-04-23
- Architecture de base du projet mise en place
- Structure MVVM avec Jetpack Compose
- Configuration de Room Database
- Implémentation du système de stockage externe pour les sauvegardes
- Correction des problèmes de compilation liés aux versions de bibliothèques
- Documentation mise à jour pour refléter l'approche de stockage
- Correction du bug "value missed" lié aux paramètres nullables dans les requêtes Room

# Suivi de progression du projet AATT

## 26 avril 2025 - Système robuste de sauvegarde et restauration JSON

### Problème résolu
- Correction du problème "connection pool has been closed" lors de la restauration de la base de données
- Résolution des problèmes de synchronisation entre différentes parties de l'application après restauration

### Améliorations apportées
1. **Nouvelle approche d'exportation/importation JSON**
   - Migration de la manipulation directe des fichiers de base de données vers une approche basée sur les données
   - Utilisation de Gson pour la sérialisation/désérialisation des données
   - Sauvegarde complète avec métadonnées (timestamp, version)
   
2. **Architecture améliorée du système de sauvegarde**
   - Nouvelle classe `DatabaseBackupHelper` pour gérer les opérations d'exportation/importation
   - Méthodes dédiées dans le Repository pour la récupération et l'importation des données
   - Gestion propre des événements de restauration à travers l'application

3. **Interface utilisateur fiable**
   - Affichage cohérent des données après restauration sans nécessiter de redémarrage
   - Notification claire des succès/échecs de sauvegarde et restauration
   - Gestion améliorée des fichiers de sauvegarde (liste, suppression)

4. **Documentation complète**
   - Ajout d'un document détaillé BackupRestoreSystem.md expliquant la nouvelle approche
   - Mise à jour de la documentation existante pour refléter les changements

### Impact sur les performances et la stabilité
- Restauration fiable sans nécessiter de redémarrage de l'application
- Élimination des problèmes liés aux connexions de base de données Room
- Meilleure robustesse face aux erreurs de manipulation de fichiers
- Possibilité d'évolution future (sauvegarde sélective, compression, chiffrement)

## 23 avril 2025 - Amélioration de la robustesse des boutons d'activité

### Problème résolu
- Correction du crash lors de l'appui sur les boutons d'activité par l'ajout d'une gestion robuste des erreurs
- Correction du bug "value missed" causé par les paramètres de liste nullable dans les requêtes SQL de Room

### Améliorations apportées
1. **Gestion avancée des erreurs dans le MainViewModel**
   - Capture des exceptions dans toutes les méthodes critiques
   - Ajout de logs détaillés pour diagnostic
   - État d'erreur observable via StateFlow pour l'UI

2. **Améliorations de l'interface utilisateur**
   - Indicateur de chargement pendant les opérations de base de données
   - Affichage des messages d'erreur dans une Snackbar
   - Désactivation des boutons pendant le chargement pour éviter les actions multiples

3. **Flux de données plus sûrs**
   - Utilisation de `.catch` sur les flows pour gérer les erreurs de collection

4. **Optimisation des requêtes Room**
   - Séparation des méthodes DAO pour éviter les problèmes avec les paramètres nullables
   - Meilleure gestion des cas limites dans les requêtes SQL
   - Renforcement des vérifications dans le Repository pour prévenir les erreurs

### À faire prochainement
- Implémenter l'écran d'édition des activités
- Ajouter les fonctions pour modifier les dates et heures des activités existantes
- Développer l'interface pour les sauvegardes/restaurations

## 23 avril 2025 - Optimisation de batterie et améliorations UI

### Problème résolu
- Suppression du timer en temps réel qui consommait inutilement de l'énergie
- Simplification de l'interface utilisateur pour une meilleure lisibilité

### Améliorations apportées
1. **Optimisation de consommation énergétique**
   - Suppression de la mise à jour périodique du temps dans le MainViewModel (1 seconde)
   - Remplacement par une mise à jour uniquement lors des changements d'état d'activité
   - Réduction significative de la consommation CPU en arrière-plan

2. **Améliorations de l'interface utilisateur**
   - Ajout d'un indicateur visuel coloré "ACTIVE" pour indiquer l'état de chaque activité
   - Simplification de l'affichage des informations temporelles (heure de début uniquement)
   - Utilisation de codes couleur cohérents entre les boutons et les indicateurs d'activité

### Impact sur les performances
- Durée de vie de la batterie améliorée pour une utilisation prolongée
- Réduction des cycles de rendu UI inutiles
- Diminution de la charge de travail du thread UI