package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navigateToEdit: () -> Unit,
    navigateToStats: () -> Unit
) {
    val activeActivities by viewModel.activeActivities.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AATT", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = navigateToEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Éditer les activités"
                        )
                    }
                    IconButton(onClick = navigateToStats) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "Statistiques"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zone d'affichage de l'activité en cours
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Contenu normal
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (activeActivities.isEmpty()) {
                                Text(
                                    text = "Aucune activité en cours",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                // On peut avoir plusieurs activités actives si PAUSE est active en même temps qu'une autre
                                for (activity in activeActivities) {
                                    val activityLabel = when (activity.type) {
                                        ActivityType.VS -> "Visite Semestrielle"
                                        ActivityType.ROUTE -> "Route"
                                        ActivityType.DOMICILE -> "Domicile"
                                        ActivityType.PAUSE -> "Pause"
                                    }
                                    
                                    Text(
                                        text = activityLabel,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Début: ${dateFormat.format(Date(activity.startTime))}",
                                        fontSize = 18.sp
                                    )
                                    
                                    // Ajout d'indicateurs visuels sur l'état de l'activité
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when(activity.type) {
                                                    ActivityType.VS -> Color(0xFF4CAF50)
                                                    ActivityType.ROUTE -> Color(0xFF2196F3)
                                                    ActivityType.DOMICILE -> Color(0xFFFF9800)
                                                    ActivityType.PAUSE -> Color(0xFFF44336)
                                                }
                                            ),
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                        
                        // Indicateur de chargement centré
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(50.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Barre avec les 4 boutons d'activité
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActivityButton(
                        type = ActivityType.ROUTE,
                        label = "ROUTE",
                        color = Color(0xFF2196F3),  // Bleu
                        isActive = activeActivities.any { it.type == ActivityType.ROUTE },
                        isEnabled = !isLoading,
                        onClick = { viewModel.toggleActivity(ActivityType.ROUTE) }
                    )
                    
                    ActivityButton(
                        type = ActivityType.VS,
                        label = "VS",
                        color = Color(0xFF4CAF50),  // Vert
                        isActive = activeActivities.any { it.type == ActivityType.VS },
                        isEnabled = !isLoading,
                        onClick = { viewModel.toggleActivity(ActivityType.VS) }
                    )
                    
                    ActivityButton(
                        type = ActivityType.DOMICILE,
                        label = "DOM",
                        color = Color(0xFFFF9800),  // Orange
                        isActive = activeActivities.any { it.type == ActivityType.DOMICILE },
                        isEnabled = !isLoading,
                        onClick = { viewModel.toggleActivity(ActivityType.DOMICILE) }
                    )
                    
                    ActivityButton(
                        type = ActivityType.PAUSE,
                        label = "PAUSE",
                        color = Color(0xFFF44336),  // Rouge
                        isActive = activeActivities.any { it.type == ActivityType.PAUSE },
                        isEnabled = !isLoading,
                        onClick = { viewModel.toggleActivity(ActivityType.PAUSE) }
                    )
                }
            }
            
            // Message d'erreur en snackbar
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        IconButton(onClick = { viewModel.clearErrorMessage() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Fermer"
                            )
                        }
                    },
                    dismissAction = null,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun ActivityButton(
    type: ActivityType,
    label: String,
    color: Color,
    isActive: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) color else color.copy(alpha = 0.6f),
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium,
        enabled = isEnabled
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}