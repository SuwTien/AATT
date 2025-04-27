# Implémentation Technique des Statistiques AATT

Ce document décrit l'architecture technique et l'implémentation des calculs statistiques dans l'application AATT. Il complète le document `StatisticsRules.md` qui définit les règles métier.

## Architecture globale

### Vue d'ensemble des composants

L'implémentation des statistiques repose sur trois composants principaux:

1. **StatisticsCalculator** : Classe utilitaire contenant la logique de calcul pure
2. **StatsViewModel** : ViewModel gérant les données et les états pour l'UI
3. **Écrans de statistiques** : Composables Jetpack Compose organisés en architecture modulaire:
   - `StatsScreen.kt` : Structure principale avec navigation par onglets
   - `DailyStatsScreen.kt`, `WeeklyStatsScreen.kt`, `MonthlyStatsScreen.kt` : Affichages spécifiques
   - `StatsCommonComponents.kt` : Composants partagés

### Flux de données

Le flux de données pour les statistiques suit le schéma suivant :

```
Repository → StatsViewModel → StatisticsCalculator → StatsViewModel → UI
```

1. Le `StatsViewModel` récupère les activités depuis le `Repository`
2. Les données sont transmises au `StatisticsCalculator` pour traitement
3. Les statistiques calculées sont stockées dans le `StatsViewModel` via des `StateFlow`
4. L'UI observe ces états et affiche les données

## Classe StatisticsCalculator

### Structure et responsabilités

`StatisticsCalculator` est une classe utilitaire avec méthodes statiques qui :
- Calcule les statistiques selon les règles métier définies
- Formatte les durées pour l'affichage
- Gère le regroupement des activités par période

### Classe de données ActivityStats

Cette classe encapsule toutes les statistiques calculées pour une période donnée :

```kotlin
data class ActivityStats(
    val workDuration: Long = 0L,              // Durée totale de travail (VS + DOMICILE + DEPLACEMENT)
    val routeDuration: Long = 0L,             // Durée brute de route
    val routeDurationAdjusted: Long = 0L,     // Durée de route après déduction de 1h30 par jour
    val pauseDuration: Long = 0L,             // Durée totale des pauses
    val workDays: Int = 0,                    // Nombre de jours travaillés
    val vsActivityCount: Int = 0,             // Nombre d'activités VS
    val routeActivityCount: Int = 0,          // Nombre d'activités ROUTE
    val domicileActivityCount: Int = 0,       // Nombre d'activités DOMICILE
    val pauseActivityCount: Int = 0,          // Nombre d'activités PAUSE
    val deplacementActivityCount: Int = 0,    // Nombre d'activités DEPLACEMENT
    val deplacementDuration: Long = 0L        // Durée totale des déplacements (inclus dans workDuration)
)
```

### Méthodes principales

#### calculateStats()

Cette méthode centrale effectue tous les calculs selon les règles métier :

```kotlin
fun calculateStats(activities: List<Activity>, calculateByDay: Boolean = true): ActivityStats
```

- `activities` : Liste d'activités à analyser
- `calculateByDay` : Si true, applique la déduction de route par jour (1h30 par jour avec ROUTE)
- Retourne un objet `ActivityStats` avec tous les résultats calculés

#### calculateDailyStats(), calculateWeeklyStats(), calculateMonthlyStats()

Ces méthodes spécialisées filtrent les activités par période avant d'appeler `calculateStats()`:

```kotlin
fun calculateDailyStats(date: Calendar, activities: List<Activity>): ActivityStats
fun calculateWeeklyStats(weekStartDate: Calendar, activities: List<Activity>): ActivityStats
fun calculateMonthlyStats(year: Int, month: Int, activities: List<Activity>): ActivityStats
```

### Implémentation de la règle de déduction ROUTE

La déduction de 1h30 par jour pour les activités ROUTE est implémentée comme suit :

1. Constante définissant la déduction : `ROUTE_DEDUCTION_MS = 5400000L` (1h30 en millisecondes)
2. Identification des jours avec activité ROUTE
3. Pour chaque jour avec ROUTE, une déduction de 1h30 est appliquée
4. Le temps total de ROUTE moins les déductions donne le temps ROUTE comptabilisé

```kotlin
// Extrait simplifié de l'algorithme
if (hasRouteThisDay) {
    routeDeductionTotal += ROUTE_DEDUCTION_MS
}
// [...]
val routeDurationAdjusted = max(0, totalRouteDuration - routeDeductionTotal)
```

## StatsViewModel

### Responsabilités

Le `StatsViewModel` joue un rôle central dans l'architecture :
- Interface entre le repository et l'UI
- Gestion des états via des `StateFlow`
- Chargement des données par période
- Navigation temporelle (précédent/suivant/aujourd'hui)

### États principaux

```kotlin
// États communs
val selectedDate: StateFlow<Calendar>
val isLoading: StateFlow<Boolean>
val errorMessage: StateFlow<String?>

// États journaliers
val dailyStats: StateFlow<ActivityStats?>
val dailyActivities: StateFlow<List<Activity>>

// États hebdomadaires
val weeklyStats: StateFlow<ActivityStats?>
val weeklyActivitiesByDay: StateFlow<Map<Calendar, List<Activity>>>

// États mensuels
val monthlyStats: StateFlow<ActivityStats?>
val monthlyActivitiesByWeek: StateFlow<Map<Calendar, List<Activity>>>
```

### Méthodes de chargement

```kotlin
fun loadDailyStats()
fun loadWeeklyStats()
fun loadMonthlyStats()
```

Ces méthodes :
1. Marquent le début du chargement via `_isLoading.value = true`
2. Récupèrent les activités depuis le repository
3. Appellent les méthodes appropriées du `StatisticsCalculator`
4. Mettent à jour les `StateFlow` avec les résultats
5. Gèrent les erreurs potentielles
6. Marquent la fin du chargement via `_isLoading.value = false`

### Méthodes utilitaires

Le ViewModel contient également des fonctions pour :
- Grouper les activités par jour ou par semaine
- Naviguer dans le temps (jour/semaine/mois précédent/suivant)
- Revenir à la période actuelle

## Interface utilisateur des statistiques

### Architecture modulaire

L'UI des statistiques est organisée en composants séparés :

1. **StatsScreen.kt**
   - Structure principale de la page
   - Navigation entre les onglets via `TabRow`
   - Gestion des états de chargement et d'erreur

2. **Composants spécifiques à chaque période**
   - `DailyStatsScreen.kt` : Statistiques journalières
   - `WeeklyStatsScreen.kt` : Statistiques hebdomadaires
   - `MonthlyStatsScreen.kt` : Statistiques mensuelles

3. **Composants communs**
   - `PeriodNavigationHeader` : En-tête avec navigation temporelle
   - `ActivityDetailCard` : Affichage détaillé d'une activité

### Flux d'affichage typique

1. L'utilisateur sélectionne un onglet (jour/semaine/mois)
2. Le `StatsScreen` appelle la méthode de chargement appropriée du ViewModel
3. Pendant le chargement, un indicateur de progression est affiché
4. Les données calculées sont observées via les StateFlow
5. Les composants spécifiques à la période affichent les données
6. L'utilisateur peut naviguer temporellement via le `PeriodNavigationHeader`

## Optimisations et défis techniques

### Gestion des performances

Pour optimiser les performances :

1. **Calculs à la demande** : Les statistiques ne sont calculées que lorsque nécessaire (changement de période)
2. **Mise en cache des résultats** : Les résultats sont conservés dans les StateFlow
3. **Exécution des calculs en arrière-plan** : Via les coroutines Kotlin

### Défis d'implémentation

1. **Précision des calculs temporels** :
   - Utilisation de `Calendar` pour la manipulation des dates
   - Gestion attentive des fuseaux horaires et changements d'heure

2. **Regroupement des activités** :
   - Implémentation de logique complexe pour regrouper par jour/semaine
   - Gestion des cas limites (activités à cheval sur deux périodes)

3. **Cohérence des calculs entre les vues** :
   - Assurer que les calculs journaliers/hebdomadaires/mensuels soient cohérents entre eux
   - Validation des totaux avec des ensembles de données de test

## Futures améliorations prévues

### Phase 1 : Calculs et précision (Mai 2025)

- Vérification complète de la précision des calculs actuels
- Optimisation du `StatisticsCalculator` pour gérer des volumes plus importants
- Mise en place de tests unitaires exhaustifs pour les calculs

### Phase 2 : Interface utilisateur (Juin 2025)

- Amélioration de l'affichage des données dans chaque vue
- Ajout de graphiques pour visualiser les répartitions
- Implémentation d'interactions avancées (clic sur un jour pour voir le détail)

### Phase 3 : Fonctionnalités avancées (Juillet 2025)

- Export des statistiques au format PDF ou CSV
- Système de filtres personnalisables
- Visualisations avancées adaptées aux besoins utilisateur

## Annexe : Diagramme de classes

```
+------------------------+       +------------------------+       +------------------------+
|   ActivityRepository   |------>|     StatsViewModel     |------>|  StatisticsCalculator  |
+------------------------+       +------------------------+       +------------------------+
          |                               |                               |
          |                               |                               |
          v                               v                               v
+------------------------+       +------------------------+       +------------------------+
|      Activity DAO      |       |     StateFlow<T>       |       |    ActivityStats      |
+------------------------+       +------------------------+       +------------------------+
          |                               |
          |                               |
          v                               v
+------------------------+       +------------------------+
|    Room Database       |       |   UI Components        |
+------------------------+       +------------------------+
```

## Références

- [StatisticsRules.md](StatisticsRules.md) - Règles métier pour les calculs statistiques
- [StatisticsScreenDesign.md](StatisticsScreenDesign.md) - Design détaillé des écrans de statistiques