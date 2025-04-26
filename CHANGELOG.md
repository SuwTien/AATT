# Historique des versions - AATT (Atlantic Automatique Time Tracker)

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