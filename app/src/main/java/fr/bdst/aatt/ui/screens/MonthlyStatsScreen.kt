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
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.viewmodel.StatsViewModel
import java.util.*

/**
 * Écran des statistiques mensuelles
 */
@Composable
fun MonthlyStatsContent(viewModel: StatsViewModel) {
    // Collecte des états
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val monthlyActivitiesByWeek by viewModel.monthlyActivitiesByWeek.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = if (monthlyActivitiesByWeek.isEmpty()) Arrangement.Center else Arrangement.Top
    ) {
        if (monthlyActivitiesByWeek.isEmpty()) {
            // Aucune activité pour ce mois
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune activité pour ce mois",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card de résumé mensuel
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
                        text = "RÉSUMÉ DU MOIS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    monthlyStats?.let { stats ->
                        // Résumé du temps de travail
                        Text(
                            text = "Total travail: ${StatisticsCalculator.formatDuration(stats.workDuration)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Détail par type d'activité
                        val vsDuration = monthlyActivitiesByWeek.values.flatten()
                            .filter { it.type == ActivityType.VS }
                            .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                        
                        val domicileDuration = monthlyActivitiesByWeek.values.flatten()
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
                        val daysWithRoute = monthlyActivitiesByWeek.values.flatten()
                            .groupBy { activity ->
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = activity.startTime
                                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
                            }
                            .count { (_, activities) ->
                                activities.any { it.type == ActivityType.ROUTE }
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
                        
                        // Obtention du nombre de jours dans le mois
                        val cal = selectedDate.clone() as Calendar
                        val month = cal.get(Calendar.MONTH)
                        val year = cal.get(Calendar.YEAR)
                        
                        val daysInMonth = when (month) {
                            Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, 
                            Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
                            Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
                            Calendar.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                            else -> 30 // Par défaut
                        }
                        
                        Text(
                            text = "Jours travaillés: ${stats.workDays}/$daysInMonth",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Titre de la section détaillée par semaine
            Text(
                text = "DÉTAIL PAR SEMAINE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Liste des semaines
            LazyColumn {
                items(monthlyActivitiesByWeek.entries.sortedBy { it.key.timeInMillis }) { (weekStartDate, weekActivities) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            // En-tête de la semaine
                            val startDay = weekStartDate.get(Calendar.DAY_OF_MONTH)
                            val startMonth = weekStartDate.get(Calendar.MONTH) + 1
                            
                            val endDateCal = Calendar.getInstance()
                            endDateCal.time = weekStartDate.time
                            endDateCal.add(Calendar.DAY_OF_YEAR, 6)
                            
                            val endDay = endDateCal.get(Calendar.DAY_OF_MONTH)
                            val endMonth = endDateCal.get(Calendar.MONTH) + 1
                            
                            val weekLabel = if (startMonth == endMonth) {
                                "Semaine du $startDay au $endDay/$endMonth"
                            } else {
                                "Semaine du $startDay/$startMonth au $endDay/$endMonth"
                            }
                            
                            Text(
                                text = weekLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Statistiques des activités de la semaine
                            val vsDuration = weekActivities.filter { it.type == ActivityType.VS }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val domicileDuration = weekActivities.filter { it.type == ActivityType.DOMICILE }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val deplacementDuration = weekActivities.filter { it.type == ActivityType.DEPLACEMENT }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            val routeDuration = weekActivities.filter { it.type == ActivityType.ROUTE }
                                .sumOf { StatisticsCalculator.calculateActivityDuration(it) }
                            
                            // Calcul des jours avec route dans la semaine
                            val daysWithRoute = weekActivities
                                .groupBy { activity ->
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = activity.startTime
                                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
                                }
                                .count { (_, activities) ->
                                    activities.any { it.type == ActivityType.ROUTE }
                                }
                            
                            // Calcul de la déduction route (1h30 par jour avec route)
                            val routeDeduction = minOf(routeDuration, daysWithRoute * 5400000L) // 1h30 = 5400000ms
                            val routeAdjusted = maxOf(0, routeDuration - routeDeduction)
                            
                            // Calcul du temps de travail total
                            val totalWorkTime = vsDuration + domicileDuration + deplacementDuration + routeAdjusted
                            
                            // Calcul des jours travaillés
                            val workDays = weekActivities
                                .groupBy { activity ->
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = activity.startTime
                                    "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
                                }
                                .count()
                            
                            // Affichage des statistiques
                            
                            // Total travail
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total travail:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Text(
                                    text = StatisticsCalculator.formatDuration(totalWorkTime),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // VS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "VS:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = StatisticsCalculator.formatDuration(vsDuration),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            // Domicile
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Domicile:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = StatisticsCalculator.formatDuration(domicileDuration),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            // Déplacement
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Déplacement:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = StatisticsCalculator.formatDuration(deplacementDuration),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            // Route comptée
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Route comptée:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = StatisticsCalculator.formatDuration(routeAdjusted),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Jours travaillés
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Jours travaillés:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = "$workDays/7",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}