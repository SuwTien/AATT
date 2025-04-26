package fr.bdst.aatt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import java.text.SimpleDateFormat
import java.util.*

/**
 * Un composant de dialogue pour sélectionner une date et une heure
 *
 * @param timestamp Le timestamp initial (en millisecondes)
 * @param onDismissRequest Callback appelé lorsque l'utilisateur rejette le dialogue
 * @param onConfirm Callback appelé avec le timestamp sélectionné lorsque l'utilisateur confirme
 * @param title Titre du dialogue (optionnel)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    timestamp: Long,
    onDismissRequest: () -> Unit,
    onConfirm: (Long) -> Unit,
    title: String = "Sélectionner la date et l'heure"
) {
    // État pour suivre la date et l'heure sélectionnées
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = timestamp }) }
    
    // Format de date pour l'affichage
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // État pour suivre quel sélecteur est affiché (date ou heure)
    var showDatePicker by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre du dialogue
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Affichage de la date et de l'heure sélectionnées
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sélecteur de date
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showDatePicker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showDatePicker = true }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Sélection de date",
                            tint = if (showDatePicker) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormatter.format(selectedDate.time),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = if (showDatePicker) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Sélecteur d'heure
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!showDatePicker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showDatePicker = false }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Sélection d'heure",
                            tint = if (!showDatePicker) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeFormatter.format(selectedDate.time),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = if (!showDatePicker) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Contenu du sélecteur (date ou heure)
                if (showDatePicker) {
                    DatePickerContent(
                        selectedDate = selectedDate,
                        onDateSelected = { year, month, day ->
                            selectedDate = selectedDate.apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                        }
                    )
                } else {
                    TimePickerContent(
                        selectedDate = selectedDate,
                        onTimeSelected = { hour, minute ->
                            selectedDate = selectedDate.apply {
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                        }
                    )
                }
                
                // Boutons d'action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Bouton d'annulation
                    TextButton(onClick = onDismissRequest) {
                        Text("Annuler")
                    }
                    
                    // Bouton de confirmation
                    Button(
                        onClick = { onConfirm(selectedDate.timeInMillis) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}

/**
 * Contenu du sélecteur de date
 */
@Composable
fun DatePickerContent(
    selectedDate: Calendar,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val currentYear = selectedDate.get(Calendar.YEAR)
    val currentMonth = selectedDate.get(Calendar.MONTH)
    val currentDay = selectedDate.get(Calendar.DAY_OF_MONTH)
    
    // Liste des jours, mois et années pour la sélection
    val years = (currentYear - 5..currentYear + 5).toList()
    val months = (0..11).toList()
    val days = (1..31).toList()
    
    // Noms des mois pour l'affichage
    val monthNames = remember {
        listOf("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
               "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre")
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Sélecteur pour le jour
            DateTimePickerWheel(
                items = days,
                initialSelectedIndex = currentDay - 1,
                onSelectionChanged = { day ->
                    onDateSelected(currentYear, currentMonth, day)
                },
                displayValue = { it.toString() },
                modifier = Modifier.weight(1f)
            )
            
            // Sélecteur pour le mois
            DateTimePickerWheel(
                items = months,
                initialSelectedIndex = currentMonth,
                onSelectionChanged = { month ->
                    onDateSelected(currentYear, month, currentDay)
                },
                displayValue = { monthNames[it] },
                modifier = Modifier.weight(2f)
            )
            
            // Sélecteur pour l'année
            DateTimePickerWheel(
                items = years,
                initialSelectedIndex = years.indexOf(currentYear),
                onSelectionChanged = { year ->
                    onDateSelected(year, currentMonth, currentDay)
                },
                displayValue = { it.toString() },
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

/**
 * Contenu du sélecteur d'heure
 */
@Composable
fun TimePickerContent(
    selectedDate: Calendar,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val currentHour = selectedDate.get(Calendar.HOUR_OF_DAY)
    val currentMinute = selectedDate.get(Calendar.MINUTE)
    
    // Liste des heures et minutes pour la sélection
    val hours = (0..23).toList()
    val minutes = (0..59).toList()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Sélecteur pour l'heure
        DateTimePickerWheel(
            items = hours,
            initialSelectedIndex = hours.indexOf(currentHour),
            onSelectionChanged = { hour ->
                onTimeSelected(hour, currentMinute)
            },
            displayValue = { String.format("%02d", it) },
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        // Sélecteur pour les minutes
        DateTimePickerWheel(
            items = minutes,
            initialSelectedIndex = minutes.indexOf(currentMinute),
            onSelectionChanged = { minute ->
                onTimeSelected(currentHour, minute)
            },
            displayValue = { String.format("%02d", it) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Composant de roue pour la sélection d'éléments
 */
@Composable
fun <T> DateTimePickerWheel(
    items: List<T>,
    initialSelectedIndex: Int,
    onSelectionChanged: (T) -> Unit,
    displayValue: (T) -> String,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(initialSelectedIndex.coerceIn(0, items.size - 1)) }
    
    Column(
        modifier = modifier
            .height(120.dp)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Flèche vers le haut (pour augmenter la valeur)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clickable(enabled = selectedIndex < items.size - 1) {
                    if (selectedIndex < items.size - 1) {
                        selectedIndex++
                        onSelectionChanged(items[selectedIndex])
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.arrow_up_float),
                contentDescription = "Augmenter",
                tint = if (selectedIndex < items.size - 1) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Valeur sélectionnée
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue(items[selectedIndex]),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        
        // Flèche vers le bas (pour diminuer la valeur)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clickable(enabled = selectedIndex > 0) {
                    if (selectedIndex > 0) {
                        selectedIndex--
                        onSelectionChanged(items[selectedIndex])
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.arrow_down_float),
                contentDescription = "Diminuer",
                tint = if (selectedIndex > 0) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}