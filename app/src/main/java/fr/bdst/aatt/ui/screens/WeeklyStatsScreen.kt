package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.viewmodel.StatsViewModel
import java.util.*

/**
 * Écran des statistiques hebdomadaires
 */
@Composable
fun WeeklyStatsContent(viewModel: StatsViewModel) {
    // Collecte des états
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val weeklyActivitiesByDay by viewModel.weeklyActivitiesByDay.collectAsState()
    
    if (weeklyActivitiesByDay.isEmpty()) {
        // Aucune activité pour cette semaine
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucune activité pour cette semaine",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Utiliser une seule LazyColumn pour tout le contenu
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Espacement supérieur
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Card de résumé hebdomadaire
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "RÉSUMÉ DE LA SEMAINE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        weeklyStats?.let { stats ->
                            // Résumé du temps de travail
                            Text(
                                text = "Total travail: ${StatisticsCalculator.formatDuration(stats.workDuration)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Détail par type d'activité
                            val vsDuration = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.VS }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val domicileDuration = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.DOMICILE }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val deplacementDuration = stats.deplacementDuration
                            
                            val routeExcessDuration = stats.routeDurationAdjusted
                            
                            // Affichage des détails avec indentation
                            Text(
                                text = " • VS: ${StatisticsCalculator.formatDuration(vsDuration)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = " • DOMICILE: ${StatisticsCalculator.formatDuration(domicileDuration)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = " • DEPLACEMENT: ${StatisticsCalculator.formatDuration(deplacementDuration)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = " • ROUTE comptée (>1h30/jour): ${StatisticsCalculator.formatDuration(routeExcessDuration)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Informations sur la route
                            Text(
                                text = "Total route brut: ${StatisticsCalculator.formatDuration(stats.routeDuration)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            val routeDeduction = stats.routeDuration - stats.routeDurationAdjusted
                            val daysWithRoute = weeklyActivitiesByDay.values.count { dayActivities ->
                                dayActivities.any { it.type == ActivityType.ROUTE }
                            }
                            
                            Text(
                                text = "Déduction route: -${StatisticsCalculator.formatDuration(routeDeduction)} ($daysWithRoute jours x 1h30)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "Route comptabilisée: ${StatisticsCalculator.formatDuration(stats.routeDurationAdjusted)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Informations additionnelles
                            Text(
                                text = "Total pauses: ${StatisticsCalculator.formatDuration(stats.pauseDuration)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = "Jours travaillés: ${stats.workDays}/7",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Titre de la section détaillée
            item {
                Text(
                    text = "DÉTAIL PAR JOUR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Tableau des détails par jour
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // En-tête du tableau
                        DayDetailHeader()
                        
                        Divider()
                        
                        // Corps du tableau
                        weeklyActivitiesByDay.entries.sortedBy { it.key.timeInMillis }.forEach { (day, activities) ->
                            if (activities.isEmpty()) {
                                DayDetailRowEmpty(
                                    day = day,
                                    dayFormatter = viewModel.shortDayFormatter
                                )
                            } else {
                                DayDetailRow(
                                    day = day,
                                    activities = activities,
                                    dayFormatter = viewModel.shortDayFormatter
                                )
                            }
                            
                            Divider()
                        }
                        
                        // Ligne de total
                        weeklyStats?.let { stats ->
                            val vsTotal = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.VS }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val domicileTotal = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.DOMICILE }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val deplacementTotal = stats.deplacementDuration
                            
                            DayDetailRowTotal(
                                vsDuration = vsTotal,
                                domicileDuration = domicileTotal,
                                deplacementDuration = deplacementTotal,
                                routeDuration = stats.routeDuration,
                                routeAdjustedDuration = stats.routeDurationAdjusted,
                                pauseDuration = stats.pauseDuration,
                                totalWorkDuration = stats.workDuration
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * En-tête des colonnes pour le tableau détaillé par jour
 */
@Composable
fun DayDetailHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Colonne jour
        Text(
            text = "Jour",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        
        // Colonne activités
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Activités",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Ligne vide pour un jour sans activité
 */
@Composable
fun DayDetailRowEmpty(
    day: Calendar,
    dayFormatter: java.text.SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jour
        Text(
            text = dayFormatter.format(day.time),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        
        // Message "Aucune activité"
        Text(
            text = "--- Aucune activité ---",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        
        // Colonne vide pour l'alignement
        Spacer(modifier = Modifier.width(120.dp))
    }
}

/**
 * Ligne détaillée pour un jour avec activités
 */
@Composable
fun DayDetailRow(
    day: Calendar,
    activities: List<Activity>,
    dayFormatter: java.text.SimpleDateFormat
) {
    // Trier les activités chronologiquement
    val sortedActivities = activities.sortedBy { it.startTime }
    
    // Calcul des différentes durées pour les totaux
    val domicileDuration = activities.filter { it.type == ActivityType.DOMICILE }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val pauseDuration = activities.filter { it.type == ActivityType.PAUSE }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    // Extraire et traiter les activités ROUTE
    val routeActivities = sortedActivities.filter { it.type == ActivityType.ROUTE }
    val routeDurations = routeActivities.map { StatisticsCalculator.calculateActivityDuration(it) }
    val totalRouteDuration = routeDurations.sum()
    
    // Calculer la déduction et la distribuer chronologiquement
    val routeDeduction = if (totalRouteDuration > 0) minOf(totalRouteDuration, 5400000L) else 0L // 1h30 = 5400000ms
    var remainingDeduction = routeDeduction
    val adjustedRouteDurations = mutableListOf<Long>()
    
    // Répartir la déduction de 1h30 sur les activités ROUTE chronologiquement
    for (duration in routeDurations) {
        if (remainingDeduction >= duration) {
            // Cette activité ROUTE est entièrement déduite
            adjustedRouteDurations.add(0L)
            remainingDeduction -= duration
        } else if (remainingDeduction > 0) {
            // Cette activité ROUTE est partiellement déduite
            val adjusted = duration - remainingDeduction
            adjustedRouteDurations.add(adjusted)
            remainingDeduction = 0L
        } else {
            // Cette activité ROUTE n'est pas déduite (déduction épuisée)
            adjustedRouteDurations.add(duration)
        }
    }
    
    // Créer une liste des activités à afficher dans l'ordre
    val activitiesToDisplay = mutableListOf<Pair<String, String>>() // (Type, Durée formatée)
    
    // Variables pour suivre les déplacements
    var pendingDeplacementDuration = 0L
    var totalDeplacementDuration = 0L
    var routeIndex = 0
    
    // Parcourir les activités triées et les préparer pour l'affichage
    for (i in sortedActivities.indices) {
        val activity = sortedActivities[i]
        
        when (activity.type) {
            ActivityType.ROUTE -> {
                // Afficher la durée ajustée de l'activité ROUTE
                val adjustedDuration = adjustedRouteDurations[routeIndex]
                activitiesToDisplay.add(Pair("Route", StatisticsCalculator.formatDuration(adjustedDuration)))
                routeIndex++
            }
            ActivityType.VS -> {
                // Si on a un déplacement en attente, l'inclure dans cette VS
                val activityDuration = StatisticsCalculator.calculateActivityDuration(activity)
                val totalDuration = activityDuration + pendingDeplacementDuration
                activitiesToDisplay.add(Pair("VS", StatisticsCalculator.formatDuration(totalDuration)))
                totalDeplacementDuration += pendingDeplacementDuration
                pendingDeplacementDuration = 0L
            }
            ActivityType.DEPLACEMENT -> {
                // Accumuler la durée du déplacement pour la VS suivante
                val activityDuration = StatisticsCalculator.calculateActivityDuration(activity)
                pendingDeplacementDuration += activityDuration
                
                // Si c'est le dernier élément ou si aucune VS ne suit ce déplacement,
                // l'afficher comme déplacement indépendant
                if (i == sortedActivities.size - 1 || 
                    sortedActivities.subList(i + 1, sortedActivities.size).none { it.type == ActivityType.VS }) {
                    activitiesToDisplay.add(Pair("Déplacement", StatisticsCalculator.formatDuration(pendingDeplacementDuration)))
                    totalDeplacementDuration += pendingDeplacementDuration
                    pendingDeplacementDuration = 0L
                }
            }
            ActivityType.PAUSE -> {
                // Les pauses sont gérées séparément, on ne les affiche pas dans la liste chronologique
            }
            ActivityType.DOMICILE -> {
                // Les activités DOMICILE sont affichées en total à la fin, pas individuellement
            }
        }
    }
    
    // Calcul du temps de route ajusté (après déduction de 1h30)
    val routeAdjusted = maxOf(0L, totalRouteDuration - routeDeduction)
    
    // Calcul du temps de travail total
    val vsDuration = activities.filter { it.type == ActivityType.VS }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    // Le temps de travail total inclut: 
    // - VS
    // - Déplacements
    // - Domicile
    // - Route au-delà de 1h30 par jour
    val totalWorkTime = vsDuration + totalDeplacementDuration + domicileDuration + routeAdjusted
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Jour
        Text(
            text = dayFormatter.format(day.time),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        
        // Activités
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Afficher toutes les activités dans l'ordre chronologique
            activitiesToDisplay.forEach { (type, duration) ->
                Text(
                    text = "$type: $duration",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Afficher le total PAUSE si présent
            if (pauseDuration > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pause: ${StatisticsCalculator.formatDuration(pauseDuration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Ajouter une ligne vide pour séparer si nécessaire
            if ((activitiesToDisplay.isNotEmpty() || pauseDuration > 0) && domicileDuration > 0) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Afficher le total DOMICILE à la fin
            if (domicileDuration > 0) {
                Text(
                    text = "Domicile: ${StatisticsCalculator.formatDuration(domicileDuration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Ajouter une ligne vide avant le total travail
            Spacer(modifier = Modifier.height(4.dp))
            
            // Afficher le total du travail
            Text(
                text = "Total travail: ${StatisticsCalculator.formatDuration(totalWorkTime)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Ligne de total pour le tableau détaillé par jour
 */
@Composable
fun DayDetailRowTotal(
    vsDuration: Long,
    domicileDuration: Long,
    deplacementDuration: Long,
    routeDuration: Long,
    routeAdjustedDuration: Long,
    pauseDuration: Long,
    totalWorkDuration: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // En-tête TOTAL
        Text(
            text = "TOTAL",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        
        // Activités
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "VS: ${StatisticsCalculator.formatDuration(vsDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "DOM: ${StatisticsCalculator.formatDuration(domicileDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "DEPL: ${StatisticsCalculator.formatDuration(deplacementDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Total travail: ${StatisticsCalculator.formatDuration(totalWorkDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        // Route et pause
        Column(
            modifier = Modifier.width(120.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Route: ${StatisticsCalculator.formatDuration(routeDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            
            Text(
                text = "Comptée: ${StatisticsCalculator.formatDuration(routeAdjustedDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
            
            Text(
                text = "Pause: ${StatisticsCalculator.formatDuration(pauseDuration)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}