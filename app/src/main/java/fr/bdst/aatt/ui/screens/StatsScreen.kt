package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: ActivityRepository,
    navigateBack: () -> Unit
) {
    // Création du ViewModel avec sa Factory
    val viewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.Factory(repository)
    )
    
    // États pour suivre l'onglet sélectionné
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Modifié de 0 à 1 pour sélectionner l'onglet "Semaine" par défaut
    val tabTitles = listOf("Jour", "Semaine", "Mois")
    
    // Collecte des états du ViewModel
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Effet de chargement initial des données
    LaunchedEffect(key1 = selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.loadDailyStats()
            1 -> viewModel.loadWeeklyStats()
            2 -> viewModel.loadMonthlyStats()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            
            // En-tête de navigation (période précédente/suivante)
            PeriodNavigationHeader(
                selectedTabIndex = selectedTabIndex,
                selectedDate = selectedDate,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            
            // Contenu de l'onglet sélectionné
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> DailyStatsContent(viewModel)
                    1 -> WeeklyStatsContent(viewModel)
                    2 -> MonthlyStatsContent(viewModel)
                }
                
                // Indicateur de chargement
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
                
                // Message d'erreur éventuel
                errorMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(message)
                    }
                }
            }
        }
    }
}