package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Boîte de dialogue pour éditer les heures de début et de fin d'une activité
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityEditDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onEditStartTime: (Long) -> Unit,
    onEditEndTime: (Long) -> Unit,
    onEditBothTimes: (Long, Long) -> Unit = { _, _ -> } // Nouveau callback pour éditer les deux valeurs
) {
    // États pour savoir ce qu'on est en train d'éditer
    var editingStart by remember { mutableStateOf(true) } // true = début, false = fin
    var showDatePicker by remember { mutableStateOf(true) } // true = afficher le calendrier, false = afficher l'horloge
    
    // Calendriers pour stocker les dates/heures en cours d'édition (mutableStateOf pour réagir aux changements)
    var startCalendar by remember { 
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = activity.startTime })
    }
    var endCalendar by remember { 
        mutableStateOf(
            Calendar.getInstance().apply { 
                timeInMillis = activity.endTime ?: System.currentTimeMillis() 
            }
        )
    }
    
    // Variables d'état pour suivre les modifications en temps réel
    var selectedYear by remember { mutableStateOf(0) }
    var selectedMonth by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf(0) }
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    // Pour le calendrier
    var displayedMonth by remember { mutableStateOf(0) }
    var displayedYear by remember { mutableStateOf(0) }
    
    // Mettre à jour les valeurs sélectionnées quand on change d'onglet
    LaunchedEffect(editingStart) {
        val calendar = if (editingStart) startCalendar else endCalendar
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)
        displayedMonth = selectedMonth
        displayedYear = selectedYear
    }
    
    // Fonction pour appliquer les modifications au calendrier actuel
    fun applyCurrentChanges() {
        val calendar = if (editingStart) startCalendar else endCalendar
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Créer un nouveau Calendar pour forcer la recomposition
        if (editingStart) {
            startCalendar = calendar.clone() as Calendar
        } else {
            endCalendar = calendar.clone() as Calendar
        }
    }
    
    // Surveillez les changements dans les valeurs sélectionnées et mettez à jour le calendrier correspondant
    LaunchedEffect(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
        applyCurrentChanges()
    }
    
    // Formats pour l'affichage des dates et heures
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Pour calculer la durée totale (recalculée à chaque changement des calendriers)
    val durationMillis by derivedStateOf {
        if (activity.endTime != null) {
            endCalendar.timeInMillis - startCalendar.timeInMillis
        } else 0L
    }
    
    // Pour sauvegarder les modifications
    fun saveChanges() {
        // Si l'activité a une fin, sauvegarder les deux valeurs en une seule opération
        if (activity.endTime != null) {
            onEditBothTimes(startCalendar.timeInMillis, endCalendar.timeInMillis)
        } else {
            // Si l'activité est en cours, on ne peut modifier que l'heure de début
            onEditStartTime(startCalendar.timeInMillis)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Réduction du padding vertical
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Réduction du padding vertical
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre du dialogue - type d'activité affiché en majuscules
                Text(
                    text = when (activity.type) {
                        ActivityType.VS -> "VISITE SEMESTRIELLE"
                        ActivityType.ROUTE -> "ROUTE"
                        ActivityType.DOMICILE -> "DOMICILE"
                        ActivityType.PAUSE -> "PAUSE"
                        ActivityType.DEPLACEMENT -> "DÉPLACEMENT"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                // Zone cliquable pour afficher/modifier les dates de début et fin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Colonne Début - cliquable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                // Sauvegarder les modifications actuelles avant de changer d'onglet
                                applyCurrentChanges()
                                editingStart = true 
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Titre avec indicateur visuel
                        Text(
                            text = "Début",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (editingStart) FontWeight.Bold else FontWeight.Normal,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Indicateur de sélection
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .width(40.dp)
                                .height(2.dp)
                                .background(if (editingStart) MaterialTheme.colorScheme.primary else Color.Transparent)
                        )
                        
                        Text(
                            text = dateFormat.format(startCalendar.time),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = timeFormat.format(startCalendar.time),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Séparateur vertical
                    VerticalDivider(
                        modifier = Modifier
                            .height(70.dp)
                            .padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    // Colonne Fin - cliquable seulement si l'activité est terminée
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = activity.endTime != null) { 
                                // Sauvegarder les modifications actuelles avant de changer d'onglet
                                applyCurrentChanges()
                                if (activity.endTime != null) editingStart = false 
                            }
                            .padding(vertical = 8.dp)
                            .graphicsLayer(alpha = if (activity.endTime == null) 0.6f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Titre avec indicateur visuel
                        Text(
                            text = if (activity.endTime != null) "Fin" else "En cours",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!editingStart) FontWeight.Bold else FontWeight.Normal,
                            color = if (!editingStart && activity.endTime != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Indicateur de sélection
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    if (!editingStart && activity.endTime != null) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        Color.Transparent
                                )
                        )
                        
                        if (activity.endTime != null) {
                            Text(
                                text = dateFormat.format(endCalendar.time),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (!editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = timeFormat.format(endCalendar.time),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (!editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "---",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "---",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                // Séparateur
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                // Spacer réduit
                Spacer(modifier = Modifier.height(4.dp)) // Réduit de 8dp à 4dp
                
                // Icônes Date / Heure - positionnement amélioré
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly // Utilisation de SpaceEvenly au lieu de Center
                ) {
                    // Colonne Calendrier
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f) // Poids égal pour les deux colonnes
                    ) {
                        IconButton(
                            onClick = { 
                                // Sauvegarder les modifications actuelles avant de basculer vers la vue calendrier
                                applyCurrentChanges()
                                showDatePicker = true
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Sélectionner la date",
                                tint = if (showDatePicker) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    // Colonne Horloge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f) // Poids égal pour les deux colonnes
                    ) {
                        IconButton(
                            onClick = { 
                                // Sauvegarder les modifications actuelles avant de basculer vers la vue horloge
                                applyCurrentChanges()
                                showDatePicker = false 
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Sélectionner l'heure",
                                tint = if (!showDatePicker) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // Spacer réduit
                Spacer(modifier = Modifier.height(4.dp)) // Réduit de 8dp à 4dp
                
                // Zone de sélection dynamique - hauteur légèrement réduite
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp), // Réduit de 240dp à 220dp
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp), // Réduit de 8dp à 4dp
                        contentAlignment = Alignment.Center
                    ) {
                        if (showDatePicker) {
                            // Calendrier visuel
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Entête du calendrier avec navigation mois/année
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp), // Réduit de 8dp à 4dp
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bouton mois précédent
                                    IconButton(onClick = {
                                        if (displayedMonth > 0) {
                                            displayedMonth--
                                        } else {
                                            displayedMonth = 11
                                            displayedYear--
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Mois précédent"
                                        )
                                    }
                                    
                                    // Affichage mois et année
                                    val monthNames = listOf(
                                        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                                        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
                                    )
                                    Text(
                                        text = "${monthNames[displayedMonth]} $displayedYear",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // Bouton mois suivant
                                    IconButton(onClick = {
                                        if (displayedMonth < 11) {
                                            displayedMonth++
                                        } else {
                                            displayedMonth = 0
                                            displayedYear++
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Mois suivant"
                                        )
                                    }
                                }
                                
                                // Jours de la semaine
                                val weekDays = listOf("Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    weekDays.forEach { day -> 
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(2.dp)) // Réduit de 4dp à 2dp
                                
                                // Grille des jours du mois
                                val calendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, displayedYear)
                                    set(Calendar.MONTH, displayedMonth)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                }
                                
                                // Déterminer le premier jour du mois (0 = dimanche, 1 = lundi, etc.)
                                val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
                                // Ajuster pour commencer la semaine le lundi (1) au lieu de dimanche (0)
                                val firstDayOffset = (firstDayOfMonth + 5) % 7
                                
                                // Obtenir le nombre de jours dans le mois
                                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                                
                                // Calculer le nombre de semaines nécessaires pour afficher ce mois
                                val numRows = (daysInMonth + firstDayOffset + 6) / 7
                                
                                // Afficher les semaines dans le mois
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Utiliser un LazyVerticalGrid ici cause le problème de chevauchement
                                    // On le remplace par des Row dans une Column
                                    for (row in 0 until numRows) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(24.dp), // Hauteur fixe pour chaque ligne
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            for (col in 0 until 7) {
                                                val dayNumber = row * 7 + col + 1 - firstDayOffset
                                                
                                                if (dayNumber in 1..daysInMonth) {
                                                    // C'est le jour sélectionné (même année, mois et jour)
                                                    val isSelected = dayNumber == selectedDay && 
                                                                      displayedMonth == selectedMonth && 
                                                                      displayedYear == selectedYear
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isSelected) 
                                                                    MaterialTheme.colorScheme.primary 
                                                                else 
                                                                    Color.Transparent
                                                            )
                                                            .clickable {
                                                                selectedDay = dayNumber
                                                                selectedMonth = displayedMonth
                                                                selectedYear = displayedYear
                                                                // Appliquer immédiatement les changements
                                                                applyCurrentChanges()
                                                            }
                                                            .padding(2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = dayNumber.toString(),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (isSelected) 
                                                                MaterialTheme.colorScheme.onPrimary 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                } else {
                                                    // Case vide pour les jours hors du mois
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Sélecteur d'heure (horloge simplifiée) avec orientation verticale et gestes de glissement
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Une rangée pour sélectionner l'heure et les minutes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sélecteur d'heure (0-23) - Version améliorée avec flèches verticales
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text("Heure", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Flèche vers le haut
                                        IconButton(
                                            onClick = { 
                                                selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                                                applyCurrentChanges()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Heure suivante",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        // Valeur avec geste de glissement
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .size(60.dp, 50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .pointerInput(Unit) {
                                                    detectVerticalDragGestures { _, dragAmount ->
                                                        when {
                                                            dragAmount < -16 -> { // Seuil augmenté de -2 à -8 pour réduire la sensibilité
                                                                selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                                                                applyCurrentChanges()
                                                            }
                                                            dragAmount > 16 -> { // Seuil augmenté de 2 à 8 pour réduire la sensibilité
                                                                selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                                                                applyCurrentChanges()
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = selectedHour.toString().padStart(2, '0'),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Flèche vers le bas
                                        IconButton(
                                            onClick = { 
                                                selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                                                applyCurrentChanges()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Heure précédente",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    // Séparateur
                                    Text(
                                        text = ":",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    
                                    // Sélecteur de minutes (0-59) - Version améliorée avec flèches verticales
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text("Minute", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Flèche vers le haut
                                        IconButton(
                                            onClick = { 
                                                selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                                                applyCurrentChanges()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Minute suivante",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        // Valeur avec geste de glissement
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .size(60.dp, 50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .pointerInput(Unit) {
                                                    detectVerticalDragGestures { _, dragAmount ->
                                                        when {
                                                            dragAmount < -8 -> { // Seuil augmenté de -2 à -8 pour réduire la sensibilité
                                                                selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                                                                applyCurrentChanges()
                                                            }
                                                            dragAmount > 8 -> { // Seuil augmenté de 2 à 8 pour réduire la sensibilité
                                                                selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                                                                applyCurrentChanges()
                                                            }
                                                        }
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = selectedMinute.toString().padStart(2, '0'),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Flèche vers le bas
                                        IconButton(
                                            onClick = { 
                                                selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                                                applyCurrentChanges()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Minute précédente",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Spacer réduit
                Spacer(modifier = Modifier.height(8.dp)) // Réduit de 16dp à 8dp
                
                // Affichage du temps total - padding réduit
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp), // Réduit de 8dp à 6dp
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Temps total:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (activity.endTime != null) 
                                StatisticsCalculator.formatDuration(durationMillis)
                            else "En cours",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Spacer réduit
                Spacer(modifier = Modifier.height(12.dp)) // Réduit de 24dp à 12dp
                
                // Boutons d'action
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Annuler")
                    }
                    
                    Button(
                        onClick = { 
                            saveChanges()
                            onDismiss()
                        }
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}