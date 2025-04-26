package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.ui.components.DateSelector
import fr.bdst.aatt.ui.components.DateTimePickerDialog
import fr.bdst.aatt.viewmodel.EditViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    viewModel: EditViewModel,
    navigateBack: () -> Unit
) {
    val dailyActivities by viewModel.dailyActivities.collectAsState()
    val backups by viewModel.backups.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // Formats de date/heure
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val context = LocalContext.current
    val operationResult by viewModel.operationResult.collectAsState()
    
    // États pour gérer les boîtes de dialogue
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showBackupListDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf<String?>(null) } // Stocke le chemin de la sauvegarde à restaurer
    var backupName by remember { mutableStateOf("") }
    
    // État pour le menu déroulant
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    // État pour le Snackbar de résultat
    var showSnackbar by remember { mutableStateOf(false) }
    
    // Effet pour afficher le Snackbar quand un résultat d'opération est disponible
    LaunchedEffect(operationResult) {
        if (operationResult != null) {
            showSnackbar = true
        }
    }
    
    // Charger la liste des sauvegardes au lancement de l'écran
    LaunchedEffect(key1 = Unit) {
        viewModel.refreshBackupsList(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Édition") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Menu à trois points remplaçant les trois boutons
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Plus d'options"
                            )
                        }
                        
                        // Menu déroulant avec les options
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            // Option Charger une sauvegarde
                            DropdownMenuItem(
                                text = { Text("Charger") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Charger une sauvegarde"
                                    )
                                },
                                onClick = { 
                                    viewModel.refreshBackupsList(context)
                                    showDropdownMenu = false
                                    showBackupListDialog = true
                                }
                            )
                            
                            // Option Sauvegarder
                            DropdownMenuItem(
                                text = { Text("Enregistrer") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Enregistrer une sauvegarde"
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showBackupDialog = true
                                }
                            )
                            
                            // Séparateur
                            HorizontalDivider()
                            
                            // Option Effacer tout (en rouge)
                            DropdownMenuItem(
                                text = { Text("Effacer tout", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Effacer toutes les activités",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showClearConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { _ ->
                Snackbar(
                    action = {
                        TextButton(onClick = { 
                            showSnackbar = false
                            viewModel.clearOperationResult() 
                        }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(operationResult?.message ?: "")
                }
            }
        }
    ) { innerPadding -> 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sélecteur de date
            DateSelector(
                selectedDate = selectedDate,
                onPreviousDay = { viewModel.navigateToPreviousDay() },
                onNextDay = { viewModel.navigateToNextDay() },
                onSelectDate = { year, month, day -> viewModel.setSelectedDate(year, month, day) },
                onToday = { viewModel.goToToday() }
            )
            
            // Contenu principal (activités du jour)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (dailyActivities.isEmpty()) {
                    // Affichage lorsqu'il n'y a pas d'activités pour ce jour
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune activité pour ce jour",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Liste des activités du jour sélectionné
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(dailyActivities.sortedBy { it.startTime }) { activity ->
                            ActivityItem(
                                activity = activity,
                                timeFormat = timeFormat,
                                onDelete = { viewModel.deleteActivity(activity.id) },
                                onReactivate = { viewModel.reactivateActivity(activity.id) },
                                onEditStartTime = { newTime -> viewModel.updateStartTime(activity.id, newTime) },
                                onEditEndTime = { newTime -> viewModel.updateEndTime(activity.id, newTime) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        // Boîte de dialogue de confirmation pour effacer toutes les activités
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Confirmation") },
                text = { Text("Êtes-vous sûr de vouloir effacer toutes les activités ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllActivities()
                            showClearConfirmDialog = false
                        }
                    ) {
                        Text("Effacer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearConfirmDialog = false }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Boîte de dialogue pour sauvegarder la base de données
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Sauvegarder les activités") },
                text = { 
                    Column {
                        Text("Entrez un nom pour cette sauvegarde (optionnel)")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = backupName,
                            onValueChange = { backupName = it },
                            label = { Text("Nom de la sauvegarde") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Laissez vide pour utiliser la date/heure") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.backupDatabase(context, backupName.trim())
                            showBackupDialog = false
                            backupName = ""
                        }
                    ) {
                        Text("Sauvegarder")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showBackupDialog = false 
                        backupName = ""
                    }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Boîte de dialogue pour afficher la liste des sauvegardes
        if (showBackupListDialog) {
            BackupListDialog(
                backups = backups,
                onDismiss = { showBackupListDialog = false },
                onRestore = { backupPath -> 
                    // Maintenant on stocke juste le chemin et on montre une confirmation
                    showBackupListDialog = false
                    showRestoreConfirmDialog = backupPath
                },
                onDelete = { backupPath -> 
                    viewModel.deleteBackup(context, backupPath)
                }
            )
        }
        
        // Boîte de dialogue de confirmation pour restaurer une sauvegarde
        showRestoreConfirmDialog?.let { backupPath -> 
            AlertDialog(
                onDismissRequest = { showRestoreConfirmDialog = null },
                title = { Text("Confirmation de restauration") },
                text = { 
                    Text(
                        "Êtes-vous sûr de vouloir restaurer cette sauvegarde ? " +
                        "Toutes les activités actuelles seront remplacées."
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Restaurer la sauvegarde avec la nouvelle approche JSON
                            // Pas besoin de fermer la base de données manuellement
                            viewModel.restoreDatabase(context, backupPath)
                            showRestoreConfirmDialog = null
                        }
                    ) {
                        Text("Restaurer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirmDialog = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        // Snackbar pour afficher le résultat des opérations
        if (showSnackbar && operationResult != null) {
            LaunchedEffect(operationResult) {
                // Le Snackbar se fermera automatiquement après un délai
                kotlinx.coroutines.delay(3000)
                showSnackbar = false
                viewModel.clearOperationResult()
            }
        }
    }
}

// Remaining code remains unchanged
@Composable
fun BackupListDialog(
    backups: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestion des sauvegardes") },
        text = {
            if (backups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune sauvegarde disponible")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp)
                ) {
                    items(backups) { (name, path) -> 
                        BackupItem(
                            backupName = name,
                            backupPath = path,
                            onRestore = { onRestore(path) },
                            onDelete = { showDeleteConfirmDialog = path }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
    
    // Boîte de dialogue de confirmation de suppression
    showDeleteConfirmDialog?.let { path -> 
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Confirmation") },
            text = { 
                Text(
                    "Êtes-vous sûr de vouloir supprimer cette sauvegarde ? " +
                    "Cette action est irréversible."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(path)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun BackupItem(
    backupName: String,
    backupPath: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val file = remember(backupPath) { File(backupPath) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val lastModified = remember(file) { dateFormat.format(Date(file.lastModified())) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône et informations sur la sauvegarde
        Icon(
            imageVector = Icons.Default.Backup,
            contentDescription = "Sauvegarde",
            modifier = Modifier.padding(end = 16.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = backupName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Modifié le: $lastModified",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Boutons d'action
        IconButton(onClick = onRestore) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Restaurer cette sauvegarde"
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer cette sauvegarde"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItem(
    activity: Activity,
    timeFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onReactivate: () -> Unit,
    onEditStartTime: (Long) -> Unit,
    onEditEndTime: (Long) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // Format de date pour l'affichage
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type d'activité (centré et en majuscules)
            Text(
                text = when (activity.type) {
                    ActivityType.VS -> "VISITE SEMESTRIELLE"
                    ActivityType.ROUTE -> "ROUTE"
                    ActivityType.DOMICILE -> "DOMICILE"
                    ActivityType.PAUSE -> "PAUSE"
                    ActivityType.DEPLACEMENT -> "DÉPLACEMENT"
                },
                style = MaterialTheme.typography.titleLarge, // Texte plus grand
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp)) // Réduit
            
            // Heures de début et de fin (avec textes centrés)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Colonne Début
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Début",
                        style = MaterialTheme.typography.bodyMedium, // Plus grand
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = dateFormat.format(Date(activity.startTime)),
                        style = MaterialTheme.typography.bodyLarge, // Plus grand
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = timeFormat.format(Date(activity.startTime)),
                        style = MaterialTheme.typography.titleMedium, // Plus grand
                        textAlign = TextAlign.Center
                    )
                }
                
                // Séparateur vertical
                VerticalDivider(
                    modifier = Modifier
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                
                // Colonne Fin
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Fin",
                        style = MaterialTheme.typography.bodyMedium, // Plus grand
                        textAlign = TextAlign.Center
                    )
                    if (activity.endTime != null) {
                        Text(
                            text = dateFormat.format(Date(activity.endTime)),
                            style = MaterialTheme.typography.bodyLarge, // Plus grand
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = timeFormat.format(Date(activity.endTime)),
                            style = MaterialTheme.typography.titleMedium, // Plus grand
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "En cours",
                            style = MaterialTheme.typography.titleMedium, // Plus grand
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp)) // Réduit
            
            // Durée de l'activité (centrée)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)  // Prend 60% de la largeur
            ) {
                Text(
                    text = "Durée: ${
                        StatisticsCalculator.formatDuration(
                            activity.endTime?.let { it - activity.startTime } ?: 0L
                        )
                    }",
                    style = MaterialTheme.typography.bodyLarge, // Plus grand
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp) // Padding vertical réduit
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Boutons d'action - Format corrigé, plus compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Bouton Réactiver (format compact)
                TextButton(
                    onClick = onReactivate,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Réactiver",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Réactiver",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Bouton Supprimer (format compact)
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Supprimer",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    // Boîte de dialogue de confirmation de suppression
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirmation") },
            text = { Text("Êtes-vous sûr de vouloir supprimer cette activité ?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    // Boîte de dialogue d'édition améliorée
    if (showEditDialog) {
        ActivityEditDialog(
            activity = activity,
            onDismiss = { showEditDialog = false },
            onEditStartTime = { newTime -> 
                onEditStartTime(newTime)
                showEditDialog = false 
            },
            onEditEndTime = { newTime -> 
                onEditEndTime(newTime)
                showEditDialog = false 
            }
        )
    }
}

/**
 * Boîte de dialogue pour éditer les heures de début et de fin d'une activité
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityEditDialog(
    activity: Activity,
    onDismiss: () -> Unit,
    onEditStartTime: (Long) -> Unit,
    onEditEndTime: (Long) -> Unit
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
        if (editingStart) {
            onEditStartTime(startCalendar.timeInMillis)
        } else if (activity.endTime != null) {
            onEditEndTime(endCalendar.timeInMillis)
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
                
                // Boutons Début / Fin - padding réduit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp) // Réduit de 8dp à 4dp
                ) {
                    // Bouton Début
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { editingStart = true },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Début",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (editingStart) FontWeight.Bold else FontWeight.Normal,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Indicateur de sélection
                        HorizontalDivider(
                            modifier = Modifier.width(40.dp),
                            thickness = 2.dp,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                    }
                    
                    // Bouton Fin
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = activity.endTime != null) { 
                                if (activity.endTime != null) editingStart = false 
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (activity.endTime != null) "Fin" else "En cours",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!editingStart) FontWeight.Bold else FontWeight.Normal,
                            color = if (!editingStart) 
                                MaterialTheme.colorScheme.primary
                            else if (activity.endTime == null)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Indicateur de sélection
                        HorizontalDivider(
                            modifier = Modifier.width(40.dp),
                            thickness = 2.dp,
                            color = if (!editingStart) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
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
                            onClick = { showDatePicker = true },
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
                            onClick = { showDatePicker = false },
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
                            // Sélecteur d'heure (horloge simplifiée)
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
                                    // Sélecteur d'heure (0-23)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Heure", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { 
                                                    selectedHour = if (selectedHour > 0) selectedHour - 1 else 23
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                    contentDescription = "Heure précédente"
                                                )
                                            }
                                            
                                            Text(
                                                text = selectedHour.toString().padStart(2, '0'),
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            
                                            IconButton(
                                                onClick = { 
                                                    selectedHour = if (selectedHour < 23) selectedHour + 1 else 0
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = "Heure suivante"
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Séparateur
                                    Text(
                                        text = ":",
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    
                                    // Sélecteur de minutes (0-59)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Minute", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { 
                                                    selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                    contentDescription = "Minute précédente"
                                                )
                                            }
                                            
                                            Text(
                                                text = selectedMinute.toString().padStart(2, '0'),
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            
                                            IconButton(
                                                onClick = { 
                                                    selectedMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = "Minute suivante"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Spacer réduit
                Spacer(modifier = Modifier.height(8.dp)) // Réduit de 16dp à 8dp
                
                // Affichage des deux colonnes Début / Fin - hauteur optimisée
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Colonne Début
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp), // Réduit
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Début",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp)) // Réduit de 4dp à 2dp
                        Text(
                            text = dateFormat.format(startCalendar.time),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(1.dp)) // Réduit de 2dp à 1dp
                        Text(
                            text = timeFormat.format(startCalendar.time),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    // Séparateur vertical - hauteur réduite
                    VerticalDivider(
                        modifier = Modifier
                            .height(70.dp) // Réduit de 80dp à 70dp
                            .padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    // Colonne Fin
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 2.dp), // Réduit
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Fin",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!editingStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp)) // Réduit de 4dp à 2dp
                        if (activity.endTime != null) {
                            Text(
                                text = dateFormat.format(endCalendar.time),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(1.dp)) // Réduit de 2dp à 1dp
                            Text(
                                text = timeFormat.format(endCalendar.time),
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text(
                                text = "En cours",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(1.dp)) // Réduit de 2dp à 1dp
                            Text(
                                text = "---",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
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