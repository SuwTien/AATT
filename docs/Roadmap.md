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
- **DateTimePicker**: Pour la sélection de date et heure dans la page d'édition

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

3. **Améliorations UI/UX** ⏳
   - Sélecteurs de date/heure pour modifier les activités
   - Animations et transitions
   - Affichage optimisé des informations
   - Amélioration du design visuel

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
- Page d'édition des activités terminées
- Structure de la page de statistiques
- Système de sauvegarde/restauration JSON robuste

### En cours ⏳
- Implémentation complète des calculs statistiques avec règles métier
- Sélecteurs de date/heure pour la modification des activités

### À venir 🔜
- Tests unitaires et d'UI
- Optimisations de performance
- Améliorations UI/UX avancées

## Priorités pour la prochaine itération

1. Finaliser l'implémentation des calculs statistiques selon les règles métier
2. Ajouter des sélecteurs de date/heure pour la modification des activités
3. Améliorer l'interface utilisateur de la page de statistiques
4. Commencer à mettre en place des tests unitaires pour la logique métier