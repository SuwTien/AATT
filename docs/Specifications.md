# Spécifications AATT

## Objectifs et philosophie de design
L'application AATT (Atlantic Automatique Time Tracker) est conçue pour être un outil quotidien de suivi du temps de travail. Sa conception est guidée par les principes suivants:

- **Simplicité et rapidité**: L'interface doit permettre de démarrer/arrêter une activité en un seul tap
- **Fluidité**: Aucune friction dans l'utilisation quotidienne, pas d'étapes intermédiaires inutiles
- **Efficacité**: L'application doit devenir un outil indispensable au quotidien
- **Optimisation**: Performances et réactivité maximales pour ne pas créer de frustration
- **Minimalisme**: Uniquement les fonctionnalités nécessaires sans surcharge d'options

L'application doit être si intuitive et rapide à utiliser qu'elle s'intègre naturellement dans la routine quotidienne.

## Base de données
L'application utilisera une base de données pour stocker des activités. 

### Stockage des activités
- La base de données sera utilisée pour enregistrer différentes activités
- Application dédiée à un usage personnel avec des besoins spécifiques

### Emplacement de la base de données
- La base de données active sera stockée dans l'espace privé de l'application pour des raisons de performance et de sécurité
- Les sauvegardes de la base de données seront stockées dans un dossier `/AATT/` du stockage externe (Documents)
- Cette approche permettra aux sauvegardes d'être conservées même en cas de désinstallation de l'application
- Nécessitera les permissions d'accès au stockage externe dans AndroidManifest.xml pour les opérations de sauvegarde/restauration

### Types d'activités
Pour l'instant, cinq types d'activités sont prévus :
- VS (Visite Semestrielle) - Comptée comme travail
- ROUTE - Comptée avec une règle spéciale: 1h30 non comptée par jour
- DOMICILE - Comptée comme travail
- PAUSES - Ne désactive pas les autres activités en cours (comportement spécial)
- DEPLACEMENT - Comptée comme travail

### Règles de comptabilisation des activités
- **VS, DOMICILE et DEPLACEMENT**: Ces activités sont considérées comme du travail et sont comptabilisées ensemble dans les statistiques
- **ROUTE**: Pour cette activité, 1h30 ne sont pas comptées chaque jour
- **PAUSES**: 
  - Comportement spécial : contrairement aux autres activités, ne désactive pas l'activité en cours (VS, ROUTE, DOMICILE ou DEPLACEMENT)
  - Pourrait théoriquement être de deux types : pauses pendant le travail et pauses pendant la route
  - Fonctionnalité secondaire, l'objectif principal étant de compter le temps de travail

### Structure des données d'activité
Chaque activité stockée dans la base de données contiendra :
- ID : Identifiant unique de l'activité (clé primaire autogénérée)
- Type d'activité (VS, ROUTE, DOMICILE, PAUSES ou DEPLACEMENT)
- Date et heure de début
- Date et heure de fin (null si l'activité est en cours)
- Flag indiquant si l'activité est active ou non

### Définition technique de l'entité Activity
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
    val endTime: Long?,   // Nullable, null si l'activité est active
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean
)

enum class ActivityType {
    VS,      // Visite Semestrielle
    ROUTE,   // Route
    DOMICILE, // Domicile
    PAUSE,    // Pause
    DEPLACEMENT // Déplacement
}
```

## Interface utilisateur

### Structure générale
- Une barre de navigation supérieure avec des boutons pour naviguer entre les pages
- Structure à 3 pages (extensible pour d'autres pages à l'avenir)

### Pages de l'application

1. **Page principale**
   - Affiche un titre de l'application "Atlantic Automatic Time Tracker"
   - Affiche uniquement l'activité en cours (si elle existe)
   - Contient deux rangées de boutons d'activité organisés comme suit:
     - Rangée gauche: ROUTE et DEPLACEMENT
     - Rangée centrale: PAUSE (bouton plus grand)
     - Rangée droite: VS et DOMICILE
   
   **Fonctionnement des boutons:**
   - Quand on appuie sur un bouton, cela démarre ou arrête des activités
   - Au démarrage d'une activité: 
     - Création d'un nouvel objet dans la base
     - Enregistrement de la date et l'heure courante comme heure de début
     - Définition du flag "actif" à vrai
   - Si une activité est déjà en cours quand on appuie sur un bouton:
     - **Cas spécial pour PAUSE**: 
       - Si on appuie sur PAUSE, l'activité en cours (VS, ROUTE, DOMICILE ou DEPLACEMENT) continue
       - La pause est enregistrée en parallèle
     - **Pour les autres types d'activités** (VS, ROUTE, DOMICILE, DEPLACEMENT):
       - L'activité en cours s'arrête (enregistrement de la date et l'heure de fin)
       - Le flag "actif" est mis à faux
       - Si le bouton appuyé correspond à un type différent de l'activité arrêtée:
         - Une nouvelle activité du type correspondant au bouton est démarrée
       - Si le bouton appuyé correspond au même type que l'activité arrêtée:
         - Aucune nouvelle activité n'est démarrée

2. **Page d'édition des activités** (gauche)
   - Affichage des activités terminées (non actives)
   - Possibilité de modifier la date et l'heure de début
   - Possibilité de modifier la date et l'heure de fin
   - Fonctionnalité pour supprimer une activité
   - Option pour réactiver une activité terminée
   - Les activités en cours ne sont pas affichées dans cette page
   - **Gestion de la base de données:**
     - Option pour sauvegarder la base de données actuelle
     - Option pour charger une base de données sauvegardée
     - Option pour effacer complètement la base de données actuelle (réinitialisation)

3. **Page de statistiques** (droite)
   - Affichage de statistiques et de calculs basés sur les activités enregistrées
   - Contient 3 sous-pages accessibles via des onglets:
     - Statistiques par jour
     - Statistiques par semaine
     - Statistiques par mois

## Architecture technique

### Structure du projet
Le projet suivra une architecture MVVM (Model-View-ViewModel) avec les composants suivants:

1. **Model**
   - Entités de base de données (Activity)
   - Room Database pour la persistance des données
   - Repositories pour gérer l'accès aux données

2. **View**
   - Composants Jetpack Compose pour l'UI
   - Écrans principaux: MainScreen, EditScreen, StatsScreen
   - Navigation entre les écrans

3. **ViewModel**
   - Logique métier pour chaque écran
   - Connexion entre les vues et les repositories
   - Gestion des états des écrans

### Organisation des packages
- `fr.bdst.aatt`
  - `.ui`: Composants d'interface utilisateur
    - `.screens`: Écrans principaux de l'application
    - `.components`: Composants réutilisables
    - `.theme`: Thèmes et styles
  - `.data`: Couche de données
    - `.model`: Entités et classes de modèle
    - `.repository`: Interfaces et implémentations des repositories
    - `.db`: Configuration de la base de données Room
  - `.viewmodel`: ViewModels pour chaque écran

### Implémentation du stockage externe pour les sauvegardes

Pour gérer les sauvegardes de la base de données dans le stockage externe (`/AATT/`), nous utiliserons l'approche suivante:

1. **Base de données active dans le stockage interne**
   - La base de données principale sera stockée dans l'espace privé de l'application
   - Meilleure performance et sécurité pour les opérations quotidiennes
   - Accès rapide ne nécessitant pas de permissions spéciales

2. **Sauvegardes dans le stockage externe**
   - Les fichiers de sauvegarde seront stockés dans le dossier `Documents/AATT/`
   - Ce dossier sera créé lors de la première sauvegarde si nécessaire
   - Les sauvegardes seront conservées même en cas de désinstallation de l'application

3. **Fonctionnalités de sauvegarde/restauration**
   - Option pour créer une sauvegarde manuellement (avec nom personnalisé ou horodaté)
   - Possibilité de restaurer une sauvegarde précédente
   - Gestion des sauvegardes (listing, suppression)

4. **Gestion des permissions**
   - Demande des permissions appropriées selon la version d'Android
   - Pour Android 13+ (API 33+): Permissions READ_MEDIA_*
   - Pour versions antérieures: READ_EXTERNAL_STORAGE et WRITE_EXTERNAL_STORAGE

5. **Gestion des erreurs**
   - Solution de repli si le stockage externe n'est pas disponible
   - Messages appropriés à l'utilisateur en cas d'échec des opérations

### Calculs spécifiques pour les statistiques
1. **Statistiques journalières**:
   - Temps total de travail (VS + DOMICILE + DEPLACEMENT)
   - Temps de route avec indication du total brut et du total après déduction de 1h30
   - Temps de pause

2. **Statistiques hebdomadaires**:
   - Cumul du temps de travail (VS + DOMICILE + DEPLACEMENT) sur la semaine
   - Cumul du temps de route sur la semaine, avec déduction de 1h30 par jour travaillé
   - Nombre de jours travaillés dans la semaine

3. **Statistiques mensuelles**:
   - Cumul du temps de travail (VS + DOMICILE + DEPLACEMENT) sur le mois
   - Cumul du temps de route sur le mois, avec déduction de 1h30 par jour travaillé
   - Nombre de jours travaillés dans le mois

## Évolutions futures possibles

### Fonctionnalités secondaires/tertiaires
- **Exportation des statistiques en PDF**: Permettre l'export des données de statistiques sous forme de documents PDF
- **Visualisations graphiques**: Ajout de graphiques pour visualiser la répartition du temps
- **Améliorations de l'interface utilisateur**: Personnalisation des couleurs, thèmes, etc.

_Note: Ce document évoluera avec les spécifications détaillées à mesure qu'elles sont définies._