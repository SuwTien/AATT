# Historique des versions - AATT (Atlantic Automatique Time Tracker)

## Version 1.0.0 (26 avril 2025) - Code version Play Store : 1

### Première version de test sur le Google Play Store 🎉

#### Fonctionnalités principales
- Suivi d'activités professionnelles (Visite Semestrielle, Route, Domicile, Pause)
- Interface utilisateur intuitive avec Jetpack Compose
- Édition des activités terminées (modification de dates/heures, suppression, réactivation)
- Système de sauvegarde/restauration JSON robuste
- Structure de base pour les statistiques avec différentes périodes (jour, semaine, mois)

#### Détails techniques
- Structure MVVM avec Kotlin et Jetpack Compose
- Base de données locale avec Room
- Exportation/importation des données au format JSON
- Gestion des permissions de stockage adaptée aux versions récentes d'Android

#### Notes de publication
Cette première version est publiée en test sur le Google Play Store pour recueillir des retours utilisateurs et identifier d'éventuels problèmes non détectés pendant le développement.

#### Prochaines évolutions prévues
- Finalisation des calculs statistiques avec règles métier
- Amélioration de l'interface pour l'édition des dates/heures
- Optimisations diverses et corrections de bugs