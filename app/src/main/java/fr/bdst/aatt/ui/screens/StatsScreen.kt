package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.data.util.StatisticsCalculator
import kotlinx.coroutines.flow.map
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: ActivityRepository,
    navigateBack: () -> Unit
) {
    // États pour suivre la période sélectionnée
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Jour", "Semaine", "Mois")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Onglets pour choisir la période
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Contenu de l'onglet sélectionné
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> DailyStatsContent(repository)
                    1 -> WeeklyStatsContent(repository)
                    2 -> MonthlyStatsContent(repository)
                }
            }
        }
    }
}

@Composable
fun DailyStatsContent(repository: ActivityRepository) {
    // Pour une vraie implémentation, nous collecterions les activités du jour
    // et calculerions les statistiques. Pour l'instant, c'est un placeholder.
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Statistiques journalières",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cette fonctionnalité sera implémentée prochainement",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun WeeklyStatsContent(repository: ActivityRepository) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Statistiques hebdomadaires",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cette fonctionnalité sera implémentée prochainement",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun MonthlyStatsContent(repository: ActivityRepository) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Statistiques mensuelles",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cette fonctionnalité sera implémentée prochainement",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}