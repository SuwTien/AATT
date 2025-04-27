package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
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
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = if (weeklyActivitiesByDay.isEmpty()) Arrangement.Center else Arrangement.Top
    ) {
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
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card de résumé hebdomadaire
            Card(
                modifier = Modifier.fillMaxWidth(),
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
            
            // Titre de la section détaillée
            Text(
                text = "DÉTAIL PAR JOUR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Tableau des détails par jour
            Card(
                modifier = Modifier.fillMaxWidth(),
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
        
        // Colonne temps de route
        Column(
            modifier = Modifier.width(120.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Route/Pause",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
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
    val vsDuration = activities.filter { it.type == ActivityType.VS }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val domicileDuration = activities.filter { it.type == ActivityType.DOMICILE }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val deplacementDuration = activities.filter { it.type == ActivityType.DEPLACEMENT }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val routeDuration = activities.filter { it.type == ActivityType.ROUTE }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val pauseDuration = activities.filter { it.type == ActivityType.PAUSE }
        .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
    
    val routeAdjusted = if (routeDuration > 0) {
        maxOf(0, routeDuration - 5400000) // 1h30 = 5400000ms
    } else 0
    
    val totalWorkTime = vsDuration + domicileDuration + deplacementDuration + routeAdjusted
    
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
            if (vsDuration > 0) {
                Text(
                    text = "VS: ${StatisticsCalculator.formatDuration(vsDuration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (domicileDuration > 0) {
                Text(
                    text = "DOM: ${StatisticsCalculator.formatDuration(domicileDuration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (deplacementDuration > 0) {
                Text(
                    text = "DEPL: ${StatisticsCalculator.formatDuration(deplacementDuration)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Text(
                text = "Total travail: ${StatisticsCalculator.formatDuration(totalWorkTime)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Route et pause
        Column(
            modifier = Modifier.width(120.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (routeDuration > 0) {
                Text(
                    text = "Route: ${StatisticsCalculator.formatDuration(routeDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End
                )
                
                if (routeAdjusted > 0) {
                    Text(
                        text = "Comptée: ${StatisticsCalculator.formatDuration(routeAdjusted)}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            if (pauseDuration > 0) {
                Text(
                    text = "Pause: ${StatisticsCalculator.formatDuration(pauseDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End
                )
            }
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