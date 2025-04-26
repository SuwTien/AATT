# Règles de calcul des statistiques AATT

## Introduction

Ce document définit les règles précises de calcul des statistiques pour l'application AATT. Les statistiques sont affichées dans trois modes différents (jour, semaine, mois), chacun présentant des informations adaptées à la période concernée.

## Principes généraux de calcul

### Classification des activités

Les activités sont classées selon leur comptabilisation dans les statistiques :

1. **Activités de travail** : VS, DOMICILE et DEPLACEMENT
   - Ces trois types d'activités sont comptabilisés ensemble comme "temps de travail"
   - Aucune déduction particulière n'est appliquée

2. **Activité ROUTE** : 
   - Une déduction fixe de 1h30 (90 minutes) est appliquée pour chaque jour où il y a au moins une activité ROUTE
   - Cette déduction s'applique une seule fois par jour, peu importe le nombre d'activités ROUTE
   - Au-delà de 1h30 de route par jour, le temps excédentaire est comptabilisé comme du temps de travail
   - Exemple : 3h de route dans une journée = 1h30 déduites + 1h30 comptées comme temps de travail

3. **Activité PAUSE** : 
   - Comptabilisée séparément des autres types
   - Pour l'instant, simplement affichée comme temps de pause cumulé
   - Peut coexister avec d'autres activités en cours

### Définition d'un "jour travaillé"

Un jour est considéré comme "travaillé" s'il contient au moins une activité de travail (VS, DOMICILE, DEPLACEMENT) ou une activité ROUTE.

## Cas particulier de l'activité DOMICILE

L'activité DOMICILE représente le travail effectué à la maison. Cependant, cette catégorie pose un défi particulier :

1. **Problème de déclaration** : Le temps passé en activité DOMICILE ne peut pas être déclaré directement dans certains contextes administratifs.

2. **Nécessité de répartition** : Pour les besoins de rapportage, le temps passé en DOMICILE doit être réparti sur d'autres activités légitimes, principalement le temps de VS (Visite Semestrielle).

### Stratégies de répartition du temps DOMICILE

Pour gérer cette contrainte, deux approches sont possibles :

#### Option 1 : Répartition automatique dans les statistiques
- L'application continue d'enregistrer séparément le temps DOMICILE
- Dans les rapports statistiques, une option permet d'inclure automatiquement le temps DOMICILE dans le temps VS
- Cette option n'affecte que l'affichage et non les données enregistrées

#### Option 2 : Assistance à la répartition manuelle
- L'application affiche clairement le temps passé en DOMICILE
- L'utilisateur peut utiliser ces informations pour ajuster manuellement ses déclarations
- Un indicateur visuel peut montrer combien de temps DOMICILE doit être réparti

### Recommandation pour l'interface utilisateur

Pour faciliter cette gestion, l'écran de statistiques hebdomadaires pourrait inclure :
- Un bouton "Voir avec répartition DOMICILE" qui recalcule les statistiques en répartissant le temps DOMICILE sur le VS
- Un mode d'affichage alternatif qui montre côte à côte les valeurs brutes et les valeurs avec répartition
- Une indication claire du temps DOMICILE à répartir pour chaque période

## Règles spécifiques par mode d'affichage

### 1. Mode Journalier

Le mode journalier affiche les statistiques pour un jour calendaire précis, sélectionné par l'utilisateur.

#### Informations à afficher :

1. **Date du jour** : Au format "Jour DD/MM/YYYY"
2. **Temps de travail** : Somme des durées VS + DOMICILE + DEPLACEMENT
3. **Temps de route** :
   - **Brut** : Durée totale des activités ROUTE
   - **Ajusté** : Durée brute moins 1h30 (minimum 0)
4. **Temps de pause** : Durée totale des activités PAUSE
5. **Détail par type d'activité** (optionnel) :
   - Nombre d'activités de chaque type
   - Durée par type

#### Présentation visuelle recommandée :
- Card principale avec les temps totaux
- Visualisation sous forme de camembert (optionnel)

### 2. Mode Hebdomadaire

Le mode hebdomadaire affiche les statistiques pour une semaine complète, du lundi au dimanche.

#### Informations à afficher :

1. **Période de la semaine** : Au format "Du DD/MM au DD/MM/YYYY"
2. **Nombre de jours travaillés** dans la semaine
3. **Temps de travail total** : Somme des durées VS + DOMICILE + DEPLACEMENT sur la semaine
4. **Temps de route** :
   - **Brut** : Durée totale des activités ROUTE sur la semaine
   - **Ajusté** : Durée brute moins (nombre de jours avec ROUTE × 1h30)
5. **Temps de pause total** : Durée totale des activités PAUSE sur la semaine

#### Présentation visuelle recommandée :
- Card principale avec les totaux hebdomadaires
- Tableau ou graphique montrant la répartition par jour (optionnel)

### 3. Mode Mensuel

Le mode mensuel affiche les statistiques pour un mois calendaire complet.

#### Informations à afficher :

1. **Période du mois** : Au format "Mois YYYY" (ex: "Avril 2025")
2. **Nombre de jours travaillés** dans le mois
3. **Temps de travail total** : Somme des durées VS + DOMICILE + DEPLACEMENT sur le mois
4. **Temps de route** :
   - **Brut** : Durée totale des activités ROUTE sur le mois
   - **Ajusté** : Durée brute moins (nombre de jours avec ROUTE × 1h30)
5. **Temps de pause total** : Durée totale des activités PAUSE sur le mois

#### Présentation visuelle recommandée :
- Card principale avec les totaux mensuels
- Tableau ou graphique montrant la répartition par semaine (optionnel)

## Exemples de calcul

### Exemple 1 : Calcul journalier

Activités pour le 25/04/2025 :
- VS : 08:00-10:30 (2h30)
- ROUTE : 10:30-11:30 (1h)
- DEPLACEMENT : 11:30-12:00 (30min)
- PAUSE : 12:00-13:00 (1h)
- DOMICILE : 13:00-17:00 (4h)

Résultats attendus :
- Temps de travail : 2h30 (VS) + 30min (DEPLACEMENT) + 4h (DOMICILE) = 7h
- Temps de route brut : 1h
- Temps de route ajusté : 1h - 1h30 = 0h (minimum 0)
- Temps de pause : 1h
- Jour travaillé : Oui

### Exemple 2 : Calcul hebdomadaire

Activités pour la semaine du 21 au 27/04/2025 :
- Lundi : 8h de travail, 2h de route
- Mardi : 7h de travail, 3h de route, 1h de pause
- Mercredi : Aucune activité
- Jeudi : 6h de travail, 1h de route
- Vendredi : 7h de travail, 1h de route
- Samedi/Dimanche : Aucune activité

Résultats attendus :
- Jours travaillés : 4 jours
- Temps de travail total : 28h
- Temps de route brut : 7h
- Temps de route ajusté : 7h - (4 × 1h30) = 7h - 6h = 1h
- Temps de pause total : 1h

## Remarques additionnelles

1. **Périodes sans activité** : Si une période n'a aucune activité, afficher un message approprié (ex: "Aucune activité ce jour").

2. **Format d'affichage des durées** : Utiliser le format "HH:MM" pour toutes les durées (ex: "07:30" pour 7h30).

3. **Couleurs recommandées** : Utiliser des couleurs cohérentes avec les boutons d'activité de l'application.

4. **Navigation entre périodes** : Permettre à l'utilisateur de naviguer facilement entre les jours, semaines ou mois avec des boutons précédent/suivant.