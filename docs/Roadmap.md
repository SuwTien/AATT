# Feuille de Route - AATT (Atlantic Automatique Time Tracker)

## Outils et Technologies

### Base de développement
- **Langage**: Kotlin
- **SDK minimum**: 29 (Android 10)
- **SDK cible**: 35
- **Interface utilisateur**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Base de données**: Room Database
- **Gestion des états**: StateFlow / SharedFlow
- **Navigation**: Jetpack Compose Navigation
- **Injection de dépendances**: Hilt (si nécessaire)
- **Tests unitaires**: JUnit 4
- **Tests d'UI**: Compose UI Testing
- **Sérialisation JSON**: Gson

### Bibliothèques complémentaires
- **Kotlin Coroutines**: Pour les opérations asynchrones
- **Material Design 3**: Pour les composants UI
- **Accompanist**: Utilitaires supplémentaires pour Compose

## Phases de Développement

### Phase 1: Structure de base et stockage des données ✅
1. **Mise en place de la structure du projet** ✅
   - Configuration des packages selon l'architecture définie
   - Mise en place des fichiers de base

2. **Implémentation de la base de données** ✅
   - Création de l'entité Activity
   - Configuration de Room Database avec stockage externe
   - Implémentation des DAOs (Data Access Objects)
   - Création du repository pour l'accès aux données

3. **Permissions et stockage externe** ✅
   - Implémentation des demandes de permissions
   - Configuration du stockage dans le répertoire `/AATT/`

### Phase 2: Interface utilisateur de base ✅
1. **Navigation principale** ✅
   - Configuration du système de navigation entre les écrans
   - Implémentation de la barre de navigation supérieure

2. **Page principale** ✅
   - Création de l'interface avec l'affichage de l'activité en cours
   - Implémentation des 4 boutons d'activité dans la barre inférieure
   - Logique de démarrage/arrêt des activités

3. **Page d'édition** ✅
   - Interface de liste des activités terminées
   - Fonctionnalités de modification (début, fin, suppression, réactivation)

4. **Page de statistiques - structure de base** ✅
   - Navigation entre les trois sous-pages (jour, semaine, mois)
   - Structure générale de l'affichage des statistiques

### Phase 3: Fonctionnalités avancées ⏳
1. **Système de sauvegarde/restauration robuste** ✅
   - Implémentation d'un système basé sur l'exportation/importation JSON
   - Interface utilisateur pour la gestion des sauvegardes
   - Événements globaux pour synchroniser les écrans après restauration
   - Documentation du système dans BackupRestoreSystem.md

2. **Logique des statistiques** ⏳
   - Implémentation des calculs spécifiques pour chaque période
   - Application des règles métier (déduction de 1h30 pour la ROUTE, etc.)
   - Prise en compte du nouveau type d'activité DEPLACEMENT

3. **Améliorations UI/UX** ✅
   - Sélecteurs de date/heure pour modifier les activités
   - Réorganisation des boutons d'activité en format 3+2 optimisé
   - Interface utilisateur optimisée avec textes plus grands et centrés
   - Réduction des paddings pour maximiser l'espace vertical
   - Utilisation de codes couleur cohérents pour les activités

4. **Optimisation des icônes et ressources graphiques** ✅
   - Conversion des icônes au format WebP pour améliorer les performances
   - Organisation optimale des ressources pour toutes les densités d'écran
   - Configuration correcte du système d'icônes adaptatives d'Android
   - Résolution des problèmes de duplication de ressources
   - Documentation du système d'icônes pour maintenir la cohérence future

### Phase 4: Tests et finalisation 🔜
1. **Tests unitaires**
   - Tests pour la base de données et les repositories
   - Tests pour les calculs et la logique métier

2. **Tests d'UI**
   - Vérification des interactions principales
   - Validation des flux utilisateurs

3. **Optimisations**
   - Amélioration des performances
   - Réduction de la consommation de ressources

## État d'avancement actuel

### Complété ✅
- Structure MVVM de base
- Base de données Room et repository
- Interface utilisateur principale avec boutons d'activité
- Page d'édition des activités terminées avec support du nouveau type DEPLACEMENT
- Structure de la page de statistiques
- Système de sauvegarde/restauration JSON robuste (migration réussie depuis l'approche précédente)
- Sélecteurs de date et d'heure pour la modification des activités
- Filtrage par date dans la page d'édition
- Optimisation de l'interface avec meilleure utilisation de l'espace vertical
- Réorganisation des boutons d'activité en deux rangées (3+2) pour une meilleure ergonomie
- Version 1.1.2 avec icônes optimisées au format WebP déployée sur le Google Play Store

### En cours ⏳
- Implémentation complète des calculs statistiques avec règles métier incluant le type DEPLACEMENT

### À venir 🔜
- Tests unitaires et d'UI
- Optimisations de performance
- Documentation complète du code
- Ajouter des filtres avancés dans la page d'édition

## Priorités pour la prochaine itération

1. **Amélioration visuelle de l'onglet hebdomadaire - Phase 2 : Visuel des jours** ✅
   - Mise en évidence des jours de la semaine avec un style plus visible et hiérarchisé ✅
   - Ajout d'indicateurs visuels (icônes ou couleurs) pour identifier rapidement les types d'activités ✅
   - Organisation plus claire des activités journalières avec meilleure séparation visuelle ✅
   - Optimisation de l'affichage des jours sans activité (masquer ou réduire visuellement) ✅
   - ~~Évaluer la possibilité d'ajouter un petit graphique par jour montrant la répartition du temps~~ (reporté)
   - Conserver la note explicative sur la comptabilisation des déplacements ✅
   - Ajout d'une ligne "Total hors domicile" pour mieux distinguer les types de travail ✅

2. **Calculateurs statistiques jour/semaine/mois** 
   - Finaliser l'implémentation des calculs avec les règles métier spécifiques
   - Inclure le temps de DEPLACEMENT avec VS et DOMICILE dans les calculs de travail
   - Ajouter des visualisations claires des temps par type d'activité
   - Implémenter la règle de déduction de 1h30 pour ROUTE

3. **Améliorations visuelles de la page de statistiques** 🔝
   - Ajouter des graphiques pour visualiser la répartition du temps
   - Améliorer l'organisation des informations pour une meilleure lisibilité
   - Utiliser des couleurs cohérentes avec les boutons d'activité

4. **Documentation et tests** 🔝
   - Ajouter des commentaires KDoc dans le code source
   - Créer des tests unitaires pour les fonctions principales
   - Documenter les cas d'utilisation principaux

## Versions publiées

### Version 1.1.2 - Code version Play Store : 4 (4 mai 2025) ✅
- Optimisation des icônes de l'application au format WebP
- Résolution des problèmes de duplication des ressources d'icônes
- Configuration du système d'icônes adaptatives d'Android
- Amélioration de la stabilité et des performances de l'application

### Version 1.1.1 - Code version Play Store : 3 (3 mai 2025) ✅
- Ajout d'un écran de démarrage personnalisé avec l'image AATT
- Refonte du sélecteur d'heures avec roue de défilement intuitive
- Amélioration des statistiques hebdomadaires
- Correction des bugs de synchronisation lors de l'édition des activités

### Version 1.1.0 - Code version Play Store : 2 (28 avril 2025) ✅
- Architecture modulaire pour les écrans de statistiques
- Optimisation de l'interface utilisateur
- Système de sauvegarde/restauration JSON robuste

### Version 1.0.0 - Code version Play Store : 1 (25 avril 2025) ✅
- Première version publiée sur le Google Play Store
- Fonctionnalités de base pour le suivi d'activités professionnelles
- Interface utilisateur intuitive avec Jetpack Compose
- Organisation des boutons d'activité en deux rangées (3+2)