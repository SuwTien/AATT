# Guide du système d'icônes - AATT

Ce document explique comment les icônes sont organisées dans l'application AATT et comment les gérer correctement pour éviter les problèmes de compilation.

## Structure des icônes dans l'application

### 1. Icônes de l'application (launcher icons)

L'application utilise le système moderne d'icônes adaptatives d'Android, composé de plusieurs éléments :

- **Répertoires de ressources** : Les icônes sont organisées par densité d'écran dans les répertoires `mipmap-*dpi`.
  - `mipmap-mdpi` : Écrans à densité moyenne (environ 160dpi)
  - `mipmap-hdpi` : Écrans à haute densité (environ 240dpi)
  - `mipmap-xhdpi` : Écrans à très haute densité (environ 320dpi)
  - `mipmap-xxhdpi` : Écrans à très très haute densité (environ 480dpi)
  - `mipmap-xxxhdpi` : Écrans à très très très haute densité (environ 640dpi)

- **Système d'icônes adaptatives** : Utilise deux couches distinctes définies dans `mipmap-anydpi`:
  - Un arrière-plan (`background`) défini par la couleur dans `ic_launcher_background.xml`
  - Un premier plan (`foreground`) défini dans `ic_launcher_foreground.xml`

- **Format des fichiers** : Toutes les icônes sont au format WebP pour maximiser les performances.

### 2. Icônes utilisées dans l'interface

L'application utilise les icônes de Material Design via Jetpack Compose :

```kotlin
// Exemples d'icônes utilisées
Icons.Filled.DirectionsCar     // Pour le type "ROUTE"
Icons.Filled.LocationOn        // Pour le type "VS" (Visite Semestrielle)
Icons.Filled.Home              // Pour le type "DOMICILE" 
Icons.Default.DirectionsWalk   // Pour "DEPLACEMENT"
Icons.Filled.Pause             // Pour "PAUSE"
Icons.Filled.BarChart          // Pour les statistiques
```

## Problèmes connus et solutions

### Conflit de ressources

**Problème** : Duplication de ressources avec le même nom mais des formats différents (par exemple, un `.xml` et un `.png` nommés identiquement).

**Symptôme** : Erreur de compilation comme celle-ci :
```
[drawable/ic_launcher_foreground] J:\DEV\Android\AATT\app\src\main\res\drawable\ic_launcher_foreground.png 
[drawable/ic_launcher_foreground] J:\DEV\Android\AATT\app\src\main\res\drawable\ic_launcher_foreground.xml
```

**Solution** : 
1. Supprimer l'un des fichiers en conflit
2. Ou renommer l'un des fichiers pour éviter la collision

### Fichier R.jar verrouillé

**Problème** : Le fichier de ressources généré est verrouillé par un processus système.

**Symptôme** : Erreur comme :
```
java.io.IOException: Couldn't delete J:\DEV\Android\AATT\app\build\intermediates\compile_and_runtime_not_namespaced_r_class_jar\debug\processDebugResources\R.jar
```

**Solutions** :
1. Fermer l'émulateur Android ou Android Studio s'ils sont ouverts
2. Exécuter `./gradlew clean` pour nettoyer le projet
3. En dernier recours, supprimer manuellement le dossier `build/` et relancer la compilation

## Bonnes pratiques

### 1. Mise à jour des icônes

Pour mettre à jour les icônes de l'application :

1. Convertir toutes les icônes au format WebP pour chaque densité d'écran
2. Remplacer les fichiers dans les répertoires `mipmap-*dpi` correspondants
3. Si nécessaire, mettre à jour les ressources d'icônes adaptatives :
   - Modifier la couleur dans `ic_launcher_background.xml` si besoin
   - Mettre à jour `ic_launcher_foreground.xml` ou remplacer par une image PNG avec transparence

### 2. Utilisation correcte des icônes Material

```kotlin
// Importation correcte des icônes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
// etc.

// Utilisation dans un composant
Icon(
    imageVector = Icons.Filled.DirectionsCar,
    contentDescription = "Route",
    modifier = Modifier.size(36.dp)
)
```

### 3. Vérification avant déploiement

Avant de finaliser un bundle pour le Play Store :

1. Vérifier l'apparence des icônes sur différentes densités d'écran
2. S'assurer que le système d'icônes adaptatives fonctionne correctement
3. Tester l'application sur des appareils avec différentes formes d'icônes (carrée, ronde, etc.)

## Références utiles

- [Documentation Android sur les icônes adaptatives](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)
- [Guide Material Design sur les icônes](https://material.io/design/iconography/system-icons.html)