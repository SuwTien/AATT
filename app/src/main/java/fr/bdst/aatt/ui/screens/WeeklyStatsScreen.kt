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
    val selectedDate by viewModel.selectedDate.collectAsState()
    
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // Centrer le contenu horizontalement
                    ) {
                        Text(
                            text = "Semaine ${selectedDate.get(Calendar.WEEK_OF_YEAR)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        weeklyStats?.let { stats -> 
                            // Détail par type d'activité
                            val vsDuration = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.VS }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val domicileDuration = weeklyActivitiesByDay.values.flatten()
                                .filter { it.type == ActivityType.DOMICILE }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val deplacementDuration = stats.deplacementDuration
                            
                            val routeExcessDuration = stats.routeDurationAdjusted
                            
                            val routeDeduction = stats.routeDuration - stats.routeDurationAdjusted
                            
                            // Calcul des totaux corrigés
                            val vsPlusDeplacementPlusRouteDuration = vsDuration + deplacementDuration + routeExcessDuration
                            val allWorkDuration = vsDuration + deplacementDuration + routeExcessDuration + domicileDuration
                            val grandTotalDuration = allWorkDuration + routeDeduction // Grand total incluant la route < 1h30
                            
                            val daysWithRoute = weeklyActivitiesByDay.values.count { dayActivities ->
                                dayActivities.any { it.type == ActivityType.ROUTE }
                            }
                            
                            // Nouvelle organisation avec nom à gauche et temps à droite
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "VS:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(vsDuration),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Déplacements:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(deplacementDuration),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Route Payée:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(routeExcessDuration),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // Total VS + Déplacements en gras
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:", 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StatisticsCalculator.formatDuration(vsPlusDeplacementPlusRouteDuration),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tavail à domicile:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(domicileDuration),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // Total Route + Déplacements en gras
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:", 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StatisticsCalculator.formatDuration(allWorkDuration),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Route < 1h30:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(routeDeduction),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // Total travail (tout inclus)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TOTAL:", 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = StatisticsCalculator.formatDuration(grandTotalDuration),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Pauses:", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = StatisticsCalculator.formatDuration(stats.pauseDuration),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Jours travaillés:", style = MaterialTheme.typography.bodyLarge, )
                                Text(
                                    text = "${stats.workDays}/7",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Tableau des détails par jour - sans titre explicite
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
                        // Corps du tableau - affichage simplifié des jours sans en-tête
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
                    }
                }
            }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Jour en titre au milieu avec fond gris
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayFormatter.format(day.time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Message "Aucune activité"
        Text(
            text = "--- Aucune activité ---",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
                activitiesToDisplay.add(Pair(
                    if (pendingDeplacementDuration > 0) "VS + Dépl." else "VS", 
                    StatisticsCalculator.formatDuration(totalDuration)
                ))
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
    val totalWorkWithoutDomicile = vsDuration + totalDeplacementDuration + routeAdjusted
    val totalWorkTime = totalWorkWithoutDomicile + domicileDuration
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Jour en titre au milieu avec fond gris
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayFormatter.format(day.time),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Liste des activités - chaque type à gauche et durée à droite
        activitiesToDisplay.forEach { (type, duration) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Afficher le total PAUSE si présent
        if (pauseDuration > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pause",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = StatisticsCalculator.formatDuration(pauseDuration),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Afficher un sous-total sans domicile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total hors domicile",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = StatisticsCalculator.formatDuration(totalWorkWithoutDomicile),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Afficher le total DOMICILE à la fin
        if (domicileDuration > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Domicile",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = StatisticsCalculator.formatDuration(domicileDuration),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Afficher le total du travail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total travail",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = StatisticsCalculator.formatDuration(totalWorkTime),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}