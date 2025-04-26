package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ModeOfTravel
import androidx.compose.material.icons.filled.Pause
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
        // Suppression de la TopBar
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
                // Nouveau titre sur deux lignes
                Text(
                    text = "Atlantic Automatic",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Time Tracker",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Boutons d'édition et de statistiques qui prennent toute la largeur
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = navigateToEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary // Définir une couleur explicite pour l'icône
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Éditer les activités",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary // Couleur explicite pour l'icône
                            )
                        }
                    }
                    
                    Button(
                        onClick = navigateToStats,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary // Définir une couleur explicite pour l'icône
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "Statistiques",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary // Couleur explicite pour l'icône
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
                                        ActivityType.DEPLACEMENT -> "Déplacement"
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
                                                    ActivityType.DEPLACEMENT -> Color(0xFF4CAF50) // Changé de violet à vert pour correspondre au bouton
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
                
                // Barre avec les 5 boutons d'activité organisés en 3 colonnes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Colonne de gauche: ROUTE et DEPLACEMENT
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityButton(
                                type = ActivityType.ROUTE,
                                label = "",
                                color = Color(0xFF2196F3),  // Bleu
                                isActive = activeActivities.any { it.type == ActivityType.ROUTE },
                                isEnabled = !isLoading,
                                showIcon = true,
                                showLabel = false,
                                onClick = { viewModel.toggleActivity(ActivityType.ROUTE) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityButton(
                                type = ActivityType.DEPLACEMENT,
                                label = "",
                                color = Color(0xFF4CAF50),  // Vert (comme VS)
                                isActive = activeActivities.any { it.type == ActivityType.DEPLACEMENT },
                                isEnabled = !isLoading,
                                showIcon = true,
                                showLabel = false,
                                onClick = { viewModel.toggleActivity(ActivityType.DEPLACEMENT) }
                            )
                        }
                    }
                    
                    // Espace entre les colonnes
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Colonne du milieu: PAUSE
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        ActivityButton(
                            type = ActivityType.PAUSE,
                            label = "",
                            color = Color(0xFFF44336),  // Rouge
                            isActive = activeActivities.any { it.type == ActivityType.PAUSE },
                            isEnabled = !isLoading,
                            showIcon = true,
                            showLabel = false,
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            onClick = { viewModel.toggleActivity(ActivityType.PAUSE) }
                        )
                    }
                    
                    // Espace entre les colonnes
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Colonne de droite: VS et DOMICILE
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityButton(
                                type = ActivityType.VS,
                                label = "VS",
                                color = Color(0xFF4CAF50),  // Vert
                                isActive = activeActivities.any { it.type == ActivityType.VS },
                                isEnabled = !isLoading,
                                showIcon = false,
                                showLabel = true,
                                onClick = { viewModel.toggleActivity(ActivityType.VS) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            ActivityButton(
                                type = ActivityType.DOMICILE,
                                label = "",
                                color = Color(0xFFFF9800),  // Orange
                                isActive = activeActivities.any { it.type == ActivityType.DOMICILE },
                                isEnabled = !isLoading,
                                showIcon = true,
                                showLabel = false,
                                onClick = { viewModel.toggleActivity(ActivityType.DOMICILE) }
                            )
                        }
                    }
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
    showIcon: Boolean = true,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) color else color.copy(alpha = 0.6f),
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium,
        enabled = isEnabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (showIcon) {
                val icon = when (type) {
                    ActivityType.ROUTE -> Icons.Filled.DirectionsCar
                    ActivityType.VS -> Icons.Filled.LocationOn
                    ActivityType.DOMICILE -> Icons.Filled.Home
                    ActivityType.DEPLACEMENT -> Icons.Filled.ModeOfTravel
                    ActivityType.PAUSE -> Icons.Filled.Pause
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = when(type) {
                        ActivityType.ROUTE -> "Route"
                        ActivityType.VS -> "Visite Semestrielle"
                        ActivityType.DOMICILE -> "Domicile"
                        ActivityType.DEPLACEMENT -> "Déplacement"
                        ActivityType.PAUSE -> "Pause"
                    },
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (showLabel && label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}