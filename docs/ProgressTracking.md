# Suivi d'avancement du projet AATT

## Fonctionnalités implémentées

### Architecture de base
- [x] Structure MVVM mise en place
- [x] Base de données Room configurée
- [x] Repository pour les opérations sur les activités
- [x] ViewModel pour les écrans principaux
- [x] Navigation entre les écrans avec Jetpack Compose Navigation

### Modèles de données
- [x] Entités Activity et enum ActivityType définis avec 5 types (VS, ROUTE, DOMICILE, PAUSE, DEPLACEMENT)
- [x] Converters pour les types de données complexes
- [x] ActivityDao pour les opérations CRUD

### Interface utilisateur
- [x] MainScreen avec boutons d'activité et affichage de l'activité en cours
- [x] Organisation optimisée des boutons d'activité en deux rangées (3+2)
- [x] EditScreen pour gérer les activités terminées
- [x] StatsScreen avec trois onglets (jour, semaine, mois) - structure modulaire
- [x] Composants réutilisables (ActivityButton, etc.)
- [x] Interfaces de dialogue optimisées et compactes pour l'édition des activités
- [x] Sélecteurs de date et heure améliorés pour une meilleure ergonomie

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
3. Architecture modulaire avec séparation claire des responsabilités:
   - `StatsScreen.kt`: Structure principale et navigation entre onglets
   - `DailyStatsScreen.kt`: Affichage des statistiques journalières
   - `WeeklyStatsScreen.kt`: Affichage des statistiques hebdomadaires
   - `MonthlyStatsScreen.kt`: Affichage des statistiques mensuelles
   - `StatsCommonComponents.kt`: Composants partagés entre les différents écrans

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
- Remplacement de `Divider` par `HorizontalDivider` conformément aux nouvelles recommandations Material3

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
- [x] Boîtes de dialogue pour l'édition des heures de début/fin des activités
- [ ] Amélioration de l'UI/UX
- [ ] Finalisation des écrans de statistiques

### Améliorations techniques prévues
- [ ] Tests unitaires pour les principales fonctionnalités
- [ ] Optimisations de performance
- [ ] Surveillance des problèmes potentiels liés au stockage externe
- [ ] Mise à jour continue de l'interface utilisateur selon les évolutions de Material3

## Plan de développement des écrans statistiques

Après la migration réussie des écrans de statistiques vers une architecture modulaire, les prochaines étapes de développement sont planifiées selon la priorité suivante :

### Phase 1: Précision fonctionnelle (Mai 2025)
1. **Vérification et correction des calculs statistiques**
   - Implémenter correctement la déduction de 1h30 par jour pour les activités ROUTE
   - S'assurer que les calculs sont cohérents entre les vues journalières, hebdomadaires et mensuelles
   - Compléter les méthodes du `StatisticsCalculator` pour gérer tous les cas d'usage

2. **Enrichissement du StatisticsViewModel**
   - Ajouter les méthodes manquantes pour calculer les statistiques spécifiques à chaque vue
   - Implémenter le filtrage des données par type d'activité
   - Optimiser les requêtes de données pour éviter les recalculs inutiles

3. **Tests et validation**
   - Développer des tests unitaires pour valider les calculs
   - Vérifier avec des jeux de données réels pour tous les scénarios possibles
   - Documenter les cas limites et leur traitement

### Phase 2: Optimisation des informations affichées (Juin 2025)
1. **Vue journalière**
   - Revoir la pertinence des informations affichées dans le résumé
   - Améliorer l'affichage chronologique des activités
   - Ajouter des fonctionnalités d'édition rapide des activités (raccourci vers EditScreen)

2. **Vue hebdomadaire**
   - Optimiser le tableau récapitulatif par jour
   - Ajouter un graphique visuel de répartition du temps
   - Implémenter un système de filtre pour masquer/afficher les jours sans activité

3. **Vue mensuelle**
   - Implémenter un calendrier visuel avec code couleur d'intensité
   - Optimiser l'affichage des semaines pour avoir une vue synthétique efficace
   - Ajouter des fonctionnalités de navigation vers une semaine/journée spécifique

### Phase 3: Amélioration de l'expérience utilisateur (Juillet 2025)
1. **Navigation inter-écrans**
   - Permettre de naviguer facilement entre les niveaux (mois → semaine → jour)
   - Ajouter des animations de transition entre les écrans
   - Implémenter des gestes tactiles pour faciliter la navigation

2. **Design et esthétique**
   - Harmoniser les styles avec le reste de l'application
   - Optimiser les espacements pour une meilleure lisibilité
   - Améliorer les contrastes et la hiérarchie visuelle des informations

3. **Fonctionnalités avancées**
   - Ajouter des fonctionnalités d'export des données statistiques (CSV, PDF)
   - Implémenter des options de personnalisation de l'affichage
   - Développer des visualisations avancées (graphiques, tendances)

### Documentation associée
- Mise à jour continue du document `StatisticsRules.md` avec les règles métier précises
- Création d'un document `StatisticsImplementation.md` pour détailler les algorithmes
- Enrichissement de la documentation utilisateur avec des tutoriels d'utilisation des écrans statistiques

Cette planification permettra de développer les écrans statistiques de manière méthodique, en privilégiant d'abord la précision des calculs avant de travailler sur l'esthétique et les fonctionnalités avancées.

## Journal des modifications

### 2025-05-03 (fin d'après-midi)
- Améliorations majeures de l'interface utilisateur :
  - Refonte complète du sélecteur d'heures dans le dialogue d'édition avec une roue de défilement intuitive
  - Optimisation de l'affichage du calendrier avec marges réduites et taille du cercle de sélection ajustée
  - Ajout d'un écran de démarrage personnalisé avec l'image AATT en plein écran
  - Changement de l'icône de l'application avec AATTIcone512
  - Modification de l'onglet par défaut des statistiques de "Jour" à "Semaine"
  - Mise à jour du CHANGELOG.md pour documenter les améliorations apportées

### 2025-05-03 (après-midi)
- Correction des problèmes de synchronisation lors de l'édition des activités :
  - Résolution du bug où les modifications d'heure de début étaient écrasées par les modifications d'heure de fin
  - Implémentation d'une méthode `updateStartAndEndTime` pour mettre à jour les deux valeurs en une seule opération atomique
  - Ajout d'une fonction publique `refreshActivitiesForCurrentDay()` dans EditViewModel pour rafraîchir l'UI après les modifications
  - Correction du problème de non-rafraîchissement de la liste des activités après édition
  - Amélioration de l'interface utilisateur pour une expérience plus réactive

### 2025-05-03 (matin)
- Finalisation des améliorations visuelles des statistiques hebdomadaires :
  - Ajout d'un fond coloré pour les titres des jours avec centrage du texte
  - Suppression de la date numérique des titres de jours pour une interface plus épurée
  - Ajout d'une ligne "Total hors domicile" avant le temps de domicile pour distinguer clairement ces catégories de temps 
  - Optimisation de l'affichage des activités avec déplacements associés ("VS + Dépl.")
  - Mise à jour de la documentation StatisticsScreenDesign.md avec le nouveau design
  - Mise à jour du CHANGELOG.md pour documenter les améliorations apportées

### 2025-04-29
- Amélioration de l'interface des statistiques hebdomadaires :
  - Refonte du résumé hebdomadaire avec titre "Semaine XX" bien centré
  - Réorganisation complète des informations avec libellés à gauche et valeurs à droite
  - Présentation claire des totaux avec mise en évidence des informations importantes
  - Ajout d'une note explicative sur le comptage des déplacements
  - Documentation des modifications dans le journal de progression

### Prochaines étapes - Statistiques hebdomadaires
- **Visuel des jours** (à implémenter dans un prochain chat) :
  - Améliorer l'affichage de chaque jour dans le tableau détaillé
  - Mettre en évidence les jours de la semaine avec un style plus visible
  - Ajouter des indicateurs visuels pour faciliter la lecture des activités
  - Possibilité d'ajouter une représentation graphique simple de la répartition du temps
  - Optimiser l'espace pour afficher plus d'informations pertinentes

### 2025-04-27
- Migration complète des écrans de statistiques vers une architecture modulaire:
  - Séparation des onglets Jour/Semaine/Mois dans des fichiers distincts
  - Création d'un fichier de composants communs réutilisables
  - Amélioration de la maintenance et lisibilité du code
- Nettoyage du code : suppression de DateTimePickerDialog.kt devenu obsolète
- Élimination des importations inutilisées dans EditScreen.kt
- Documentation mise à jour pour refléter l'achèvement des fonctionnalités UI
- Boîtes de dialogue d'édition des heures de début/fin des activités complétées

### 2025-04-26 (soir)
- Ajout du nouveau type d'activité DEPLACEMENT à l'énumération ActivityType
- Réorganisation des boutons d'activité en deux rangées (3+2) pour une meilleure ergonomie
- Mise à jour du StatisticsCalculator pour prendre en compte le nouveau type d'activité
- Modification de l'interface d'édition pour gérer le type DEPLACEMENT
- Mise à jour de la documentation pour refléter l'ajout de ce nouveau type

### 2025-04-27
- Optimisation de l'interface utilisateur des boîtes de dialogue d'édition
- Refonte du sélecteur de date avec affichage complet du mois sans défilement
- Amélioration du DateSelector avec boutons transparents et hauteur réduite
- Réorganisation de l'affichage des dates et heures dans le dialogue d'édition
- Optimisation de l'espace vertical dans l'interface
- Implémentation d'onglets transparents avec encadrement pour une meilleure navigation
- Amélioration de l'affichage des activités avec textes plus grands et centrés

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

## 27 avril 2025 - Restructuration modulaire de l'écran de statistiques

### Problème résolu
- Structure monolithique de l'écran de statistiques rendant la maintenance difficile
- Duplication de code entre les différents onglets (jour/semaine/mois)
- Fichier StatsScreen.kt trop volumineux et difficile à maintenir

### Améliorations apportées
1. **Architecture modulaire des écrans de statistiques**
   - Séparation en fichiers dédiés pour chaque mode d'affichage:
     - `DailyStatsScreen.kt`: Affichage des statistiques journalières
     - `WeeklyStatsScreen.kt`: Affichage des statistiques hebdomadaires
     - `MonthlyStatsScreen.kt`: Affichage des statistiques mensuelles
     - `StatsCommonComponents.kt`: Composants partagés entre les différents écrans
   - Fichier principal `StatsScreen.kt` restructuré pour utiliser les composants séparés
   
2. **Avantages de la nouvelle architecture**
   - Meilleure séparation des préoccupations
   - Code plus facile à maintenir et faire évoluer
   - Réduction de la duplication de code
   - Meilleure organisation du projet
   
3. **Mise à jour technique**
   - Remplacement de `Divider` par `HorizontalDivider` (nouvelle API Material3)
   - Correction des problèmes de typage avec les triplets
   - Amélioration de la gestion des imports d'icônes

### Impact sur la maintenabilité
- Structure plus claire facilitant les évolutions futures
- Isolation des bugs potentiels dans des fichiers dédiés
- Modifications plus simples et moins risquées
- Réutilisation améliorée des composants communs

### Améliorations techniques
- Ajout explicite des types pour éviter les ambiguïtés
- Correction des références aux composants obsolètes
- Optimisation des imports

## 27 avril 2025 - Optimisation de l'affichage des activités et amélioration de l'ergonomie

### Problème résolu
- Interface utilisateur trop encombrée avec des marges et paddings excessifs
- Textes trop petits rendant la lecture difficile sur certains appareils
- Utilisation inefficace de l'espace vertical disponible

### Améliorations apportées
1. **Refonte de l'affichage des activités dans EditScreen**
   - Réduction des paddings verticaux pour une meilleure utilisation de l'espace
   - Augmentation de la taille des textes pour une meilleure lisibilité
   - Conversion des titres d'activités en majuscules pour une meilleure hiérarchie visuelle
   - Centrage des informations pour une meilleure cohérence
   
2. **Optimisation des styles typographiques**
   - Utilisation de `typography.titleLarge` pour les titres d'activités
   - Passage à `typography.bodyLarge` et `typography.titleMedium` pour les dates et heures
   - Choix de styles offrant un meilleur contraste et une meilleure lisibilité
   
3. **Ajustement des espacements**
   - Réduction des espacements verticaux de 8.dp à 4.dp
   - Optimisation des paddings dans les Cards pour maximiser l'espace utilisable
   - Conception plus compacte tout en préservant la lisibilité

### Impact sur l'expérience utilisateur
- Interface plus efficace montrant plus d'informations sans défilement
- Meilleure lisibilité des informations essentielles (types d'activités, heures)
- Expérience utilisateur plus fluide avec moins de défilements nécessaires
- Hiérarchie visuelle plus claire entre les différents types d'informations

### Améliorations techniques
- Optimisation des composables Jetpack Compose
- Utilisation plus efficace du système typographique de Material3
- Réduction de la hauteur des éléments UI sans compromettre l'expérience tactile

### Captures d'écran comparatives
- Des captures d'écran avant/après ont été prises pour documenter les améliorations (à ajouter)

### Retours utilisateurs
- Interface plus lisible et agréable à utiliser
- Information plus facile à scanner visuellement
- Expérience plus professionnelle et raffinée

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