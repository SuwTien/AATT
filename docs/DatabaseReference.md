# Documentation de la base de données AATT

## Architecture générale

L'application AATT utilise Room comme ORM (Object-Relational Mapping) pour gérer sa base de données SQLite. La base de données est le cœur de l'application, stockant toutes les activités et permettant leur analyse.

### Composants principaux

```
fr.bdst.aatt.data/
├── db/
│   └── AATTDatabase.kt       # Classe principale de la base de données
├── dao/
│   └── ActivityDao.kt        # Interface DAO avec requêtes SQL
├── model/
│   ├── Activity.kt           # Entité principale
│   └── ActivityType.kt       # Enum des types d'activités
├── repository/
│   └── ActivityRepository.kt # Repository pour isoler la logique d'accès
└── util/
    ├── Converters.kt         # Convertisseurs pour types complexes
    └── ExternalStorageHelper.kt # Gestion des sauvegardes externes
```

## Entités et relations

### Entité `Activity`

Représente une activité de l'utilisateur avec une heure de début, une heure de fin optionnelle, et un type.

```kotlin
@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "type")
    val type: ActivityType,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long,  // Timestamp en millisecondes
    
    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,  // Timestamp en millisecondes, null si activité en cours
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true  // true si l'activité est en cours
)
```

### Enum `ActivityType`

```kotlin
enum class ActivityType {
    VS,        // Visite Semestrielle
    ROUTE,     // Route
    DOMICILE,  // Domicile
    PAUSE,     // Pause
    DEPLACEMENT // Déplacement
}
```

## DAO et requêtes SQL

Le `ActivityDao` définit toutes les opérations possibles sur la base de données :

```kotlin
@Dao
interface ActivityDao {
    // Opérations CRUD de base
    @Insert
    suspend fun insertActivity(activity: Activity): Long
    
    @Update
    suspend fun updateActivity(activity: Activity)
    
    @Delete
    suspend fun deleteActivity(activity: Activity)
    
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): Activity?
    
    // Requêtes spécialisées
    @Query("SELECT * FROM activities WHERE is_active = 1")
    fun getActiveActivities(): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE is_active = 0 ORDER BY start_time DESC")
    fun getCompletedActivities(): Flow<List<Activity>>
    
    // ...autres requêtes...
}
```

## Repository

Le `ActivityRepository` encapsule la logique d'accès aux données et fournit une API plus intuitive pour le reste de l'application.

```kotlin
class ActivityRepository(private val activityDao: ActivityDao) {
    // Démarrer une nouvelle activité
    suspend fun startActivity(
        activityType: ActivityType,
        endCurrentActivities: Boolean = true,
        exceptTypes: List<ActivityType> = listOf(ActivityType.PAUSE)
    ): Long {
        // Implémentation...
    }
    
    // Terminer une activité
    suspend fun endActivity(activityId: Long, endTime: Long = System.currentTimeMillis()) {
        // Implémentation...
    }
    
    // ...autres méthodes...
}
```

## Règles métier et comportements spéciaux

### Gestion simultanée des activités

1. **Exclusion mutuelle** : Les activités VS, ROUTE et DOMICILE s'excluent mutuellement. Le démarrage d'une de ces activités arrête automatiquement toute autre activité de ce groupe.

2. **Cas spécial des PAUSE** : Une PAUSE peut coexister avec une autre activité. Démarrer une PAUSE ne termine pas les autres activités en cours.

```kotlin
// Exemple dans startActivity() du repository
if (endCurrentActivities) {
    val typesToEnd = ActivityType.values().filter { it !in exceptTypes }.toList()
    if (typesToEnd.isNotEmpty()) {
        activityDao.endActiveActivities(currentTime, typesToEnd)
    }
}
```

## Gestion du stockage

### Stockage interne

La base de données active est stockée dans l'espace privé de l'application pour des raisons de performance et de sécurité.

```kotlin
private fun createDatabase(context: Context): AATTDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AATTDatabase::class.java,
        DB_NAME
    )
    .fallbackToDestructiveMigration()
    .build()
}
```

### Système de sauvegarde/restauration

L'application utilise une approche JSON pour la sauvegarde et restauration de la base de données, ce qui offre plusieurs avantages par rapport à une copie directe du fichier SQLite :

```kotlin
// Dans DatabaseBackupHelper
fun exportToJson(context: Context, customName: String? = null): String? {
    // Exportation des données au format JSON dans Documents/AATT/
    // Retourne le chemin du fichier de sauvegarde ou null en cas d'échec
}

fun importFromJson(context: Context, backupFile: String): Boolean {
    // Importation des données depuis un fichier JSON
    // Retourne true si la restauration a réussi, false sinon
}

fun listBackups(context: Context): List<Pair<String, String>> {
    // Retourne une liste de paires (nom de la sauvegarde, chemin du fichier)
}
```

Format du fichier JSON de sauvegarde :
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

### Avantages de l'approche JSON
1. **Robustesse accrue** - Évite les problèmes liés aux connexions de base de données Room
2. **Meilleure compatibilité** entre les versions d'Android
3. **Indépendance du schéma** - Permet des migrations plus faciles
4. **Débogage simplifié** - Les fichiers JSON sont lisibles par l'humain
5. **Flexibilité** - Possibilité d'importation/exportation partielle

## API complète pour les opérations sur les activités

### Création et manipulation

| Méthode | Description |
|---------|-------------|
| `startActivity(type, endCurrentActivities, exceptTypes)` | Démarre une nouvelle activité du type spécifié |
| `endActivity(id, endTime)` | Termine une activité spécifique |
| `endActivitiesByType(type, endTime)` | Termine toutes les activités d'un type donné |
| `endActiveActivities(endTime, exceptTypes)` | Termine toutes les activités actives, avec exceptions optionnelles |
| `updateActivity(activity)` | Met à jour les détails d'une activité existante |
| `updateActivityStartTime(id, newStartTime)` | Modifie uniquement l'heure de début d'une activité existante |
| `updateActivityEndTime(id, newEndTime)` | Modifie uniquement l'heure de fin d'une activité existante |
| `deleteActivity(activity)` | Supprime une activité |
| `clearAllActivities()` | Réinitialise la base de données en supprimant toutes les activités |

### Requêtes et analyses

| Méthode | Description |
|---------|-------------|
| `getActiveActivities()` | Récupère toutes les activités en cours |
| `getCompletedActivities()` | Récupère toutes les activités terminées |
| `getActivitiesForDay(calendar)` | Récupère les activités pour une journée spécifique |
| `getActivitiesForWeek(startDate)` | Récupère les activités pour une semaine spécifique |
| `getActivitiesForMonth(year, month)` | Récupère les activités pour un mois spécifique |
| `getActivityById(id)` | Récupère une activité par son ID |
| `getActivitiesByType(type)` | Récupère les activités d'un type spécifique |
| `getActivitiesBetween(startTime, endTime)` | Récupère les activités dans un intervalle de temps |

## StatisticsCalculator

La classe `StatisticsCalculator` encapsule les règles métier pour calculer les durées et statistiques d'activités.

```kotlin
object StatisticsCalculator {
    /**
     * Calcule la durée totale des activités par type
     */
    fun calculateDurationByType(activities: List<Activity>, currentTime: Long = System.currentTimeMillis()): Map<ActivityType, Long> {
        // Implémentation...
    }
    
    /**
     * Formate une durée en millisecondes en chaîne lisible (HH:MM:SS)
     */
    fun formatDuration(durationMs: Long): String {
        // Implémentation...
    }
    
    // Autres méthodes pour les règles métier spécifiques...
}
```

## Prochaines étapes de développement

### À implémenter

1. **Interface utilisateur pour les sauvegardes/restaurations**
   - Liste des sauvegardes disponibles
   - Interface pour créer une nouvelle sauvegarde avec nom personnalisé
   - Interface pour restaurer une sauvegarde spécifique

2. **Règles métier pour les statistiques**
   - Implémentation des calculs spécifiques (ex: déduction de 1h30 par jour pour les activités ROUTE)
   - Agrégation des données par jour, semaine, et mois

3. **Migration de schéma**
   - Stratégies de migration pour les futures mises à jour de schéma

### Tests à réaliser

1. **Tests unitaires pour les opérations DAO**
2. **Tests unitaires pour les règles métier dans StatisticsCalculator**
3. **Tests d'intégration pour le Repository**
4. **Tests de performance pour les requêtes complexes**