package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.viewmodel.StatsViewModel
import java.util.Calendar
import java.util.Date

/**
 * En-tête de navigation pour les périodes (jour/semaine/mois)
 */
@Composable
fun PeriodNavigationHeader(
    selectedTabIndex: Int,
    selectedDate: Calendar,
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val periodText = when (selectedTabIndex) {
        0 -> viewModel.dayFormatter.format(selectedDate.time)
        1 -> {
            val endOfWeek = Calendar.getInstance().apply { 
                timeInMillis = selectedDate.timeInMillis
                add(Calendar.DAY_OF_MONTH, 6)
            }
            "Du ${viewModel.shortDayFormatter.format(selectedDate.time)} au ${viewModel.shortDayFormatter.format(endOfWeek.time)}"
        }
        2 -> viewModel.monthFormatter.format(selectedDate.time)
        else -> ""
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton "Précédent"
            IconButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> viewModel.navigateToPreviousDay()
                        1 -> viewModel.navigateToPreviousWeek()
                        2 -> viewModel.navigateToPreviousMonth()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowLeft,
                    contentDescription = "Période précédente"
                )
            }
            
            // Texte de la période
            Text(
                text = periodText,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            // Bouton "Suivant"
            IconButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> viewModel.navigateToNextDay()
                        1 -> viewModel.navigateToNextWeek()
                        2 -> viewModel.navigateToNextMonth()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowRight,
                    contentDescription = "Période suivante"
                )
            }
            
            // Bouton "Aujourd'hui"
            Button(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> viewModel.navigateToToday()
                        1 -> viewModel.navigateToCurrentWeek()
                        2 -> viewModel.navigateToCurrentMonth()
                    }
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aujourd'hui")
                }
            }
        }
    }
}

/**
 * Carte détaillée d'une activité avec ses informations principales
 */
@Composable
fun ActivityDetailCard(
    activity: Activity,
    timeFormatter: java.text.SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    val startTime = timeFormatter.format(Date(activity.startTime))
    val endTime = if (activity.endTime != null) {
        timeFormatter.format(Date(activity.endTime!!))
    } else {
        "En cours"
    }
    
    val duration = if (activity.endTime != null) {
        StatisticsCalculator.calculateActivityDuration(activity)
    } else {
        System.currentTimeMillis() - activity.startTime
    }
    
    val formattedDuration = StatisticsCalculator.formatDuration(duration)
    
    // Définition des couleurs et icônes selon le type d'activité
    val activityInfo: Triple<Color, ImageVector, String> = when (activity.type) {
        ActivityType.VS -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.LocationOn,
            "VS"
        )
        ActivityType.ROUTE -> Triple(
            Color(0xFF2196F3),
            Icons.Default.DirectionsCar,
            "ROUTE"
        )
        ActivityType.DOMICILE -> Triple(
            Color(0xFFFF9800),
            Icons.Default.Home,
            "DOMICILE"
        )
        ActivityType.PAUSE -> Triple(
            Color(0xFFF44336),
            Icons.Default.Pause,
            "PAUSE"
        )
        ActivityType.DEPLACEMENT -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.DirectionsWalk, // Remplacé ModeOfTravel par DirectionsWalk qui existe
            "DEPLACEMENT"
        )
    }
    
    val color = activityInfo.first
    val icon = activityInfo.second
    val label = activityInfo.third
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône de l'activité
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Informations principales
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$startTime - $endTime",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Durée
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formattedDuration,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Indicateurs spécifiques
                if (activity.type == ActivityType.ROUTE) {
                    // Afficher une flèche indiquant aller ou retour
                    // Ce serait mieux de déterminer si c'est un aller ou retour en fonction du contexte
                    // Pour l'instant, c'est juste un exemple
                    val arrowIcon = if (activity.startTime % 2 == 0L) "↑" else "↓"
                    Text(
                        text = arrowIcon,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color
                    )
                }
            }
        }
    }
}

/**
 * Obtient le nombre de jours dans le mois
 */
fun getDaysInMonth(calendar: Calendar): Int {
    val cal = calendar.clone() as Calendar
    val month = cal.get(Calendar.MONTH)
    val year = cal.get(Calendar.YEAR)
    
    return when (month) {
        Calendar.JANUARY, Calendar.MARCH, Calendar.MAY, Calendar.JULY, 
        Calendar.AUGUST, Calendar.OCTOBER, Calendar.DECEMBER -> 31
        Calendar.APRIL, Calendar.JUNE, Calendar.SEPTEMBER, Calendar.NOVEMBER -> 30
        Calendar.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30 // Par défaut
    }
}