# Historique des versions - AATT (Atlantic Automatique Time Tracker)

## Version de développement - 3 mai 2025 (après-midi)

### Correction de bugs dans l'édition d'activités 🐛

- Résolution de problèmes de synchronisation lors de l'édition des activités :
  - Correction du bug où les modifications d'heure de début étaient écrasées par les modifications d'heure de fin
  - Implémentation d'une méthode `updateStartAndEndTime` pour mettre à jour les deux valeurs en une seule opération atomique
  - Ajout d'un mécanisme de rafraîchissement explicite de l'UI après les modifications

- Améliorations techniques :
  - Création d'une méthode publique `refreshActivitiesForCurrentDay()` dans EditViewModel
  - Intégration du rafraîchissement dans les callbacks de la boîte de dialogue d'édition
  - Meilleure séparation des responsabilités entre le ViewModel et l'UI

- Impacts des modifications :
  - Interface utilisateur plus réactive après les modifications
  - Correction des problèmes de visibilité des changements effectués
  - Élimination des cas où les modifications n'apparaissaient pas immédiatement
  - Meilleure expérience utilisateur lors de l'édition des activités

## Version de développement - 3 mai 2025 (matin)

### Améliorations de l'interface des statistiques hebdomadaires 📊

- Optimisation de l'affichage du détail des jours :
  - Suppression de l'en-tête "DÉTAIL PAR JOUR" et des colonnes Jour/Activités pour une interface plus épurée
  - Amélioration visuelle des titres de jours avec un fond coloré et centrage du texte
  - Ajout d'une ligne "Total hors domicile" pour distinguer clairement le travail sur site et à domicile
  - Affichage simplifié des déplacements associés aux VS ("VS + Dépl.")

- Impacts des modifications :
  - Meilleure lisibilité des informations quotidiennes
  - Distinction plus claire des différentes catégories de temps de travail
  - Hiérarchie visuelle améliorée avec mise en évidence des jours par un bandeau coloré
  - Interface plus compacte et mieux organisée

## Version de développement - 28 avril 2025

### Refactoring majeur du module statistiques 📊

- Architecture modulaire pour les écrans de statistiques :
  - Séparation en fichiers distincts : `DailyStatsScreen.kt`, `WeeklyStatsScreen.kt`, `MonthlyStatsScreen.kt`
  - Création d'un fichier `StatsCommonComponents.kt` pour les composants partagés
  - Restructuration de `StatsScreen.kt` comme point d'entrée principal

- Documentation technique :
  - Création du document `StatisticsImplementation.md` détaillant l'architecture technique
  - Mise à jour de `StatisticsScreenDesign.md` avec la nouvelle structure
  - Plan de développement détaillé dans `ProgressTracking.md`

- Améliorations techniques :
  - Remplacement de `Divider` par `HorizontalDivider` (nouvelle API Material3)
  - Résolution des problèmes de typage avec les triplets
  - Correction des référencements d'icônes manquantes

### Impacts positifs
- Meilleure séparation des préoccupations
- Code plus facile à maintenir et à faire évoluer
- Réduction de la duplication de code
- Meilleure organisation du projet

## Version 1.0.0 (Prévue - 2025) - Code version Play Store : 1

### Première version prévue pour le Google Play Store 🎉

#### Fonctionnalités principales
- Suivi d'activités professionnelles (Visite Semestrielle, Route, Domicile, Pause, Déplacement)
- Interface utilisateur intuitive avec Jetpack Compose
- Organisation optimisée des boutons d'activité en deux rangées (3+2)
- Édition des activités terminées (modification de dates/heures, suppression, réactivation)
- Système de sauvegarde/restauration JSON robuste
- Interface visuelle pour la gestion des sauvegardes (liste, création, restauration, suppression)
- Structure de base pour les statistiques avec différentes périodes (jour, semaine, mois)
- Interface utilisateur optimisée avec textes plus grands et centrés
- Sélecteurs de date et heure améliorés pour une meilleure ergonomie

#### Détails techniques
- Structure MVVM avec Kotlin et Jetpack Compose
- Base de données locale avec Room
- Exportation/importation des données au format JSON
- Gestion des permissions de stockage adaptée aux versions récentes d'Android
- Optimisations d'interface pour maximiser l'espace vertical disponible
- Système de calcul statistique prenant en compte les règles métier spécifiques

#### Notes de publication
Cette première version sera publiée en test sur le Google Play Store pour recueillir des retours utilisateurs et identifier d'éventuels problèmes non détectés pendant le développement.

#### Prochaines évolutions prévues
- Implémentation complète des statistiques avec visualisations par jour, semaine et mois
- Intégration des règles métier pour le calcul du temps de travail :
  - Déduction de 1h30 par jour pour les activités ROUTE
  - Comptabilisation du DEPLACEMENT comme temps de travail
  - Option de répartition du temps DOMICILE sur d'autres activités
- Interface harmonisée pour la navigation entre périodes statistiques
- Affichage détaillé des activités dans la vue journalière
- Tableau récapitulatif complet pour la vue hebdomadaire
- Visualisation calendaire pour la vue mensuelle
- Tests unitaires pour les fonctions de calcul statistique
- Documentation utilisateur complète