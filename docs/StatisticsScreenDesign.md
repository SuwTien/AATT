# Conception des écrans de statistiques AATT

Ce document détaille la conception UI des trois modes d'affichage des statistiques dans l'application AATT : jour, semaine et mois.

## Principes généraux d'affichage

### Navigation et sélection de période

Pour assurer une expérience utilisateur cohérente, les trois modes de statistiques (jour, semaine, mois) utiliseront un en-tête de navigation identique :

```
┌─────────────────────────────────────────┐
│  ◀  |  Période active  |  ▶  |  Aujourdhui  │
└─────────────────────────────────────────┘
```

- Le bouton `◀` permet de naviguer vers la période précédente
- Le bouton `▶` permet de naviguer vers la période suivante
- Le bouton `Aujourd'hui` permet de revenir à la période courante
- Le texte `Période active` affiche la période sélectionnée au format approprié :
  - Jour : "Mercredi 27/04/2025"
  - Semaine : "Du 21/04 au 27/04/2025"
  - Mois : "Avril 2025"

Cette cohérence d'interface permettra à l'utilisateur de naviguer facilement entre les périodes, quel que soit le mode de statistiques.

### Éléments communs

1. **Code couleur cohérent**
   - VS : Vert (#4CAF50)
   - ROUTE : Bleu (#2196F3)
   - DOMICILE : Orange (#FF9800)
   - PAUSE : Rouge (#F44336)
   - DEPLACEMENT : Vert (#4CAF50) - même couleur que VS

2. **Formatage des durées**
   - Format standard : HH:MM (ex: 07:30)
   - Durées nulles affichées comme "00:00"

3. **Structure générale**
   - Titre avec période
   - Card principale avec résumé
   - Section détaillée (selon le mode)

### Note sur les types d'activité ROUTE et DEPLACEMENT

Il est important de comprendre la distinction entre ces deux types d'activité :

- **ROUTE** : Représente le trajet domicile-travail et travail-domicile (généralement en début et fin de journée)
- **DEPLACEMENT** : Représente les trajets effectués pendant la journée de travail, entre deux visites semestrielles

Une séquence typique d'activités dans une journée pourrait être :
```
ROUTE (aller au travail) → VS → DEPLACEMENT → VS → DEPLACEMENT → VS → ROUTE (rentrer chez soi)
```

Cette distinction est importante pour la comptabilisation du temps :
- La ROUTE est soumise à la règle de déduction de 1h30 par jour
- Le DEPLACEMENT est entièrement compté comme du temps de travail

### Exemple concret d'une journée de travail

```
07:30-08:30 : ROUTE (1h00 - trajet domicile → site de travail)
08:30-10:30 : VS (2h00 - visite semestrielle site A)
10:30-11:00 : DEPLACEMENT (0h30 - trajet site A → site B)
11:00-12:30 : VS (1h30 - visite semestrielle site B)
12:30-13:30 : PAUSE (1h00 - déjeuner)
13:30-14:00 : DEPLACEMENT (0h30 - trajet site B → site C)
14:00-16:30 : VS (2h30 - visite semestrielle site C)
16:30-17:30 : ROUTE (1h00 - trajet site de travail → domicile)
```

Calcul du temps de travail pour cette journée :
- VS : 2h00 + 1h30 + 2h30 = 6h00
- DEPLACEMENT : 0h30 + 0h30 = 1h00
- ROUTE : 1h00 + 1h00 = 2h00 (dont 0h30 comptées comme travail après déduction de 1h30)
- PAUSE : 1h00 (non comptée comme travail)

**Total temps de travail** : 6h00 (VS) + 1h00 (DEPLACEMENT) + 0h30 (ROUTE au-delà de 1h30) = **7h30**

## Mode Journalier

### Structure proposée

```
┌─────────────────────────────────────────┐
│  ◀  |  Mercredi 27/04/2025  |  ▶  |  Aujourdhui  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ RÉSUMÉ DU JOUR                          │
│                                         │
│ Travail: 07:30                          │
│ Route: 02:30 (brut: 04:00)              │
│ Pause: 01:00                            │
│                                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ACTIVITÉS DÉTAILLÉES                    │
│                                         │
│ ┌─────────────┐                         │
│ │ 07:30-08:30 │ ROUTE                  ↑│
│ │ 🚗 1h00     │                         │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 08:30-10:30 │ VS                      │
│ │ 🏢 2h00     │ Site A                  │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 10:30-11:00 │ DEPLACEMENT             │
│ │ 🚶 0h30     │ Site A → Site B         │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 11:00-12:30 │ VS                      │
│ │ 🏢 1h30     │ Site B                  │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 12:30-13:30 │ PAUSE                   │
│ │ ☕ 1h00     │ Déjeuner                │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 13:30-14:00 │ DEPLACEMENT             │
│ │ 🚶 0h30     │ Site B → Site C         │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 14:00-16:30 │ VS                      │
│ │ 🏢 2h30     │ Site C                  │
│ └─────────────┘                         │
│                                         │
│ ┌─────────────┐                         │
│ │ 16:30-17:30 │ ROUTE                  ↓│
│ │ 🚗 1h00     │                         │
│ └─────────────┘                         │
└─────────────────────────────────────────┘
```

### Informations affichées

1. **En-tête** : Navigation identique aux autres modes

2. **Résumé** : Card avec les totaux principaux du jour
   - Temps de travail total
   - Temps de route ajusté (avec indication du brut)
   - Temps de pause total

3. **Activités détaillées** : Affichage chronologique visuel
   - Chaque activité représentée par une carte distincte
   - Heure de début et de fin
   - Type d'activité avec icône
   - Durée calculée
   - Détails additionnels si disponibles (lieu, description)
   - Flèches directionnelles (↑/↓) pour les activités ROUTE indiquant aller/retour
   - Code couleur correspondant au type d'activité

Cette présentation visuelle détaillée permet à l'utilisateur de voir rapidement l'enchaînement des activités de sa journée, avec leurs durées respectives et les transitions entre elles.

## Mode Hebdomadaire

### Structure proposée

```
┌─────────────────────────────────────────┐
│  ◀  |  Du 21/04 au 27/04/2025  |  ▶  |  Aujourdhui  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ RÉSUMÉ DE LA SEMAINE                    │
│                                         │
│ Total travail: 37:30                    │
│  • VS: 15:30                            │
│  • DOMICILE: 13:00                      │
│  • DEPLACEMENT: 08:00                   │
│  • ROUTE comptée (>1h30/jour): 01:00    │
│                                         │
│ Total route brut: 07:00                 │
│ Déduction route: -06:00 (4 jours x 1h30)│
│ Route comptabilisée: 01:00              │
│                                         │
│ Total pauses: 01:00                     │
│ Jours travaillés: 4/7                   │
│                                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ DÉTAIL PAR JOUR                         │
│                                         │
│ Lu 21 │ VS: 04:00   │ Route: 02:00      │
│       │ DOM: 02:00  │ Route comptée: 00:30 │
│       │ DEPL: 02:00 │ Pause: 00:00      │
│       │ Total travail: 08:30            │
│───────┼────────────┼────────────────────┤
│ Ma 22 │ VS: 03:30   │ Route: 03:00      │
│       │ DOM: 01:30  │ Route comptée: 01:30 │
│       │ DEPL: 02:00 │ Pause: 01:00      │
│       │ Total travail: 08:30            │
│───────┼────────────┼────────────────────┤
│ Me 23 │      --- Aucune activité ---    │
│───────┼────────────┼────────────────────┤
│ Je 24 │ VS: 03:00   │ Route: 01:00      │
│       │ DOM: 03:00  │ Route comptée: 00:00 │
│       │ DEPL: 00:00 │ Pause: 00:00      │
│       │ Total travail: 06:00            │
│───────┼────────────┼────────────────────┤
│ Ve 25 │ VS: 05:00   │ Route: 01:00      │
│       │ DOM: 02:00  │ Route comptée: 00:00 │
│       │ DEPL: 00:00 │ Pause: 00:00      │
│       │ Total travail: 07:00            │
│───────┼────────────┼────────────────────┤
│ Sa 26 │      --- Aucune activité ---    │
│───────┼────────────┼────────────────────┤
│ Di 27 │      --- Aucune activité ---    │
│───────┼────────────┼────────────────────┤
│ TOTAL │ VS: 15:30   │ Route: 07:00      │
│       │ DOM: 08:30  │ Route comptée: 01:00 │
│       │ DEPL: 04:00 │ Pause: 01:00      │
│       │ Total travail: 29:00            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│           [Graphique à barres]          │
│       répartition par jour travaillé    │
└─────────────────────────────────────────┘
```

## Mode Mensuel

### Structure proposée

```
┌─────────────────────────────────────────┐
│  ◀  |  Avril 2025  |  ▶  |  Aujourdhui  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ RÉSUMÉ DU MOIS                          │
│                                         │
│ Total travail: 142:30                   │
│  • VS: 72:30                            │
│  • DOMICILE: 50:00                      │
│  • DEPLACEMENT: 20:00                   │
│                                         │
│ Total route: 04:00 (brut: 25:00)        │
│ Déduction: -21:00 (14 jours × 1h30)     │
│                                         │
│ Total pauses: 05:00                     │
│ Jours travaillés: 14/30                 │
│                                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ DÉTAIL PAR SEMAINE                      │
│                                         │
│ 31/03-06/04 │ Trav: 35:00 │ Route: 00:30│
│ (Semaine 14)│(brut: 06:00)│ Jours: 3/7  │
│─────────────┼────────────┼─────────────┤
│ 07/04-13/04 │ Trav: 37:30 │ Route: 01:00│
│ (Semaine 15)│(brut: 07:00)│ Jours: 4/7  │
│─────────────┼────────────┼─────────────┤
│ 14/04-20/04 │ Trav: 42:00 │ Route: 01:30│
│ (Semaine 16)│(brut: 09:00)│ Jours: 5/7  │
│─────────────┼────────────┼─────────────┤
│ 21/04-27/04 │ Trav: 28:00 │ Route: 01:00│
│ (Semaine 17)│(brut: 07:00)│ Jours: 4/7  │
│─────────────┼────────────┼─────────────┤
│ 28/04-30/04 │ Trav: 00:00 │ Route: 00:00│
│ (Semaine 18)│(brut: 00:00)│ Jours: 0/3  │
└─────────────┴────────────┴────────────┘

┌─────────────────────────────────────────┐
│           RÉPARTITION VISUELLE          │
│                                         │
│ [Calendrier mensuel avec code couleur]  │
│ [Intensité de couleur selon le temps    │
│  de travail de chaque jour]             │
│                                         │
└─────────────────────────────────────────┘
```

### Informations affichées

1. **En-tête** : Navigation identique aux autres modes

2. **Résumé** : Card avec les totaux principaux du mois
   - Temps de travail avec ventilation par type (VS, DOMICILE, DEPLACEMENT)
   - Temps de route brut et ajusté avec détail du calcul de déduction
   - Temps de pause total
   - Nombre de jours travaillés

3. **Tableau par semaine** : Résumé hebdomadaire
   - Cette section reste identique à la conception précédente

4. **Visualisation** : Calendrier mensuel
   - Affichage sous forme de calendrier traditionnel
   - Code couleur selon l'intensité du travail de chaque jour
   - Possibilité de cliquer sur un jour pour passer à la vue quotidienne

Cette vue mensuelle simplifiée est plus axée sur la répartition visuelle du travail que sur les détails précis, qui seraient trop nombreux à l'échelle d'un mois.

## Design adaptatif

Pour optimiser l'expérience sur des écrans de tailles différentes, quelques adaptations sont recommandées :

### Petits écrans
- Prioriser les informations essentielles
- Graphiques optionnels masqués ou réduits
- Tableaux avec défilement horizontal si nécessaire

### Grands écrans
- Affichage de plus de détails simultanément
- Graphiques plus grands et plus détaillés
- Possibilité d'afficher plusieurs périodes côte à côte

## Accent sur la page hebdomadaire

Puisque la page hebdomadaire est la plus importante pour l'utilisateur, voici des optimisations spécifiques :

1. **Tableau compact mais lisible**
   - Police légèrement plus grande pour les informations clés
   - Surbrillance légère pour les jours avec plus d'activité
   - Séparation visuelle claire entre les jours

2. **Possibilité d'interaction**
   - Tap sur un jour pour voir plus de détails
   - Option pour passer directement à la vue journalière

3. **Mise en évidence des valeurs importantes**
   - Affichage en gras des totaux
   - Code couleur pour faciliter la lecture rapide

4. **Filtrage optionnel**
   - Bouton pour afficher/masquer les jours sans activité
   - Option pour se concentrer uniquement sur les jours travaillés

## Recommandations techniques

Pour implémenter ces designs, les composants Jetpack Compose suivants sont recommandés :

1. **Card** pour les sections principales
2. **LazyColumn** pour les listes défilantes
3. **TabRow** pour la navigation entre modes (jour/semaine/mois)
4. **Row** et **Column** pour l'organisation des informations
5. **Divider** pour les séparations visuelles dans les tableaux
6. **Canvas** ou bibliothèque tierce pour les graphiques