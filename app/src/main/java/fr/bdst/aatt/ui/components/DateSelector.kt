package fr.bdst.aatt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composant pour la sélection et la navigation entre les jours
 *
 * @param selectedDate La date actuellement sélectionnée
 * @param onPreviousDay Callback appelé pour naviguer vers le jour précédent
 * @param onNextDay Callback appelé pour naviguer vers le jour suivant
 * @param onSelectDate Callback appelé quand l'utilisateur sélectionne une date précise
 * @param onToday Callback appelé pour revenir à la date d'aujourd'hui
 */
@Composable
fun DateSelector(
    selectedDate: Calendar,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSelectDate: (Int, Int, Int) -> Unit,
    onToday: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault()) }
    val formattedDate = remember(selectedDate) { dateFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() } }
    
    // État pour le dialogue de sélection de date
    var showDatePicker by remember { mutableStateOf(false) }

    // Vérifie si la date sélectionnée est aujourd'hui
    val isToday = remember(selectedDate) {
        val today = Calendar.getInstance()
        selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bouton jour précédent (transparent et plus petit)
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Jour précédent",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Date sélectionnée et icône "Aujourd'hui" en colonne
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                // Date sélectionnée (cliquable pour ouvrir le sélecteur)
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .padding(vertical = 2.dp)
                )
                
                // Icône "Aujourd'hui" (visible si la date sélectionnée n'est pas aujourd'hui)
                if (!isToday) {
                    TextButton(
                        onClick = onToday,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Aujourd'hui",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Aujourd'hui", 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Bouton jour suivant (transparent et plus petit)
            IconButton(
                onClick = onNextDay,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Jour suivant",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Dialogue de sélection de date avec calendrier
    if (showDatePicker) {
        CalendarPickerDialog(
            initialDate = selectedDate,
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { year, month, day ->
                onSelectDate(year, month, day)
                showDatePicker = false
            }
        )
    }
}

/**
 * Dialogue de sélection de date avec un calendrier visuel
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPickerDialog(
    initialDate: Calendar,
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val today = remember { Calendar.getInstance() }
    
    // État pour suivre le mois et l'année affichés dans le calendrier
    var currentMonthYear by remember { mutableStateOf(Calendar.getInstance().apply {
        set(initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH), 1)
    }) }
    
    // Formats pour l'affichage de la date
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("E", Locale.getDefault()) }
    
    // Calcul des jours du mois
    val daysInMonth = remember(currentMonthYear) {
        currentMonthYear.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    
    // Jour de la semaine du premier jour du mois (0 = Dimanche, 1 = Lundi, etc.)
    val firstDayOfMonth = remember(currentMonthYear) {
        val firstDay = currentMonthYear.clone() as Calendar
        firstDay.set(Calendar.DAY_OF_MONTH, 1)
        firstDay.get(Calendar.DAY_OF_WEEK)
    }
    
    // Nombre de jours à afficher avant le premier jour du mois (pour remplir la grille)
    val leadingDays = remember(firstDayOfMonth) {
        if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2
    }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Sélectionner une date") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Navigateur de mois
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newMonth = currentMonthYear.clone() as Calendar
                            newMonth.add(Calendar.MONTH, -1)
                            currentMonthYear = newMonth
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Mois précédent"
                        )
                    }
                    
                    Text(
                        text = monthYearFormat.format(currentMonthYear.time).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    IconButton(
                        onClick = {
                            val newMonth = currentMonthYear.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonthYear = newMonth
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Mois suivant"
                        )
                    }
                }
                
                // Jours de la semaine (entêtes)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        val day = Calendar.getInstance()
                        // Commence par lundi (2) puis mardi, etc.
                        day.set(Calendar.DAY_OF_WEEK, if (i == 0) Calendar.MONDAY else (Calendar.MONDAY + i) % 8)
                        
                        Text(
                            text = dayOfWeekFormat.format(day.time).subSequence(0, 1).toString().uppercase(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Grille de jours
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    // Espaces vides avant le premier jour du mois
                    items(leadingDays) {
                        Box(modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp))
                    }
                    
                    // Jours du mois
                    items(daysInMonth) { day -> 
                        val dayNumber = day + 1
                        val isSelectedDay = initialDate.get(Calendar.YEAR) == currentMonthYear.get(Calendar.YEAR) &&
                                            initialDate.get(Calendar.MONTH) == currentMonthYear.get(Calendar.MONTH) &&
                                            initialDate.get(Calendar.DAY_OF_MONTH) == dayNumber
                        
                        val isCurrentDay = today.get(Calendar.YEAR) == currentMonthYear.get(Calendar.YEAR) &&
                                          today.get(Calendar.MONTH) == currentMonthYear.get(Calendar.MONTH) &&
                                          today.get(Calendar.DAY_OF_MONTH) == dayNumber
                        
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clickable {
                                    onDateSelected(
                                        currentMonthYear.get(Calendar.YEAR),
                                        currentMonthYear.get(Calendar.MONTH),
                                        dayNumber
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSelectedDay -> MaterialTheme.colorScheme.primaryContainer
                                    isCurrentDay -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = when {
                                isCurrentDay && !isSelectedDay -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                else -> null
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    textAlign = TextAlign.Center,
                                    color = when {
                                        isSelectedDay -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isCurrentDay -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bouton aujourd'hui
                Button(
                    onClick = {
                        val todayCal = Calendar.getInstance()
                        onDateSelected(
                            todayCal.get(Calendar.YEAR),
                            todayCal.get(Calendar.MONTH),
                            todayCal.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Aujourd'hui"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aujourd'hui")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Utiliser la date du jour sélectionné initialement
                    onDateSelected(
                        initialDate.get(Calendar.YEAR),
                        initialDate.get(Calendar.MONTH),
                        initialDate.get(Calendar.DAY_OF_MONTH)
                    )
                }
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annuler")
            }
        }
    )
}