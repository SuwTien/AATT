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
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.viewmodel.StatsViewModel

/**
 * Écran des statistiques journalières
 */
@Composable
fun DailyStatsContent(viewModel: StatsViewModel) {
    // Collecte des états
    val dailyStats by viewModel.dailyStats.collectAsState()
    val dailyActivities by viewModel.dailyActivities.collectAsState()
    
    if (dailyActivities.isEmpty()) {
        // Aucune activité pour ce jour
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucune activité pour ce jour",
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
            // Élément pour l'espacement supérieur
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Élément pour la card de résumé
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
                            text = "RÉSUMÉ DU JOUR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Affichage des statistiques
                        dailyStats?.let { stats ->
                            Text(
                                text = "Travail: ${StatisticsCalculator.formatDuration(stats.workDuration)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = "Route: ${StatisticsCalculator.formatDuration(stats.routeDurationAdjusted)} " +
                                    "(brut: ${StatisticsCalculator.formatDuration(stats.routeDuration)})",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                text = "Pause: ${StatisticsCalculator.formatDuration(stats.pauseDuration)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Élément pour le titre des activités détaillées
            item {
                Text(
                    text = "ACTIVITÉS DÉTAILLÉES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Éléments pour les activités détaillées
            items(dailyActivities.sortedBy { it.startTime }) { activity ->
                ActivityDetailCard(
                    activity = activity,
                    timeFormatter = viewModel.timeFormatter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}