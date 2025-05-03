package fr.bdst.aatt.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.SAFBackupHelper
import fr.bdst.aatt.data.util.StatisticsCalculator
import fr.bdst.aatt.ui.components.DateSelector
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
    val hasSAFDirectory by viewModel.hasSAFDirectory.collectAsState()
    
    // Formats de date/heure
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val context = LocalContext.current
    val operationResult by viewModel.operationResult.collectAsState()
    
    // États pour gérer les boîtes de dialogue
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showBackupListDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf<Uri?>(null) } // Stocke l'URI de la sauvegarde à restaurer
    var showSetupSAFDialog by remember { mutableStateOf(false) }
    var backupName by remember { mutableStateOf("") }
    
    // État pour le menu déroulant
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    // État pour le Snackbar de résultat
    var showSnackbar by remember { mutableStateOf(false) }
    
    // Launcher pour sélectionner le dossier de sauvegarde
    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.setBackupDirectoryUri(context, uri)
        }
    }
    
    // Effet pour afficher le Snackbar quand un résultat d'opération est disponible
    LaunchedEffect(operationResult) {
        if (operationResult != null) {
            showSnackbar = true
        }
    }
    
    // Vérifier si le dossier de sauvegarde est défini au lancement de l'écran
    LaunchedEffect(key1 = Unit) {
        viewModel.checkSAFDirectory(context)
        if (hasSAFDirectory) {
            viewModel.refreshSAFBackupsList(context)
        }
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
                                    if (!hasSAFDirectory) {
                                        showSetupSAFDialog = true
                                    } else {
                                        viewModel.refreshSAFBackupsList(context)
                                        showBackupListDialog = true
                                    }
                                    showDropdownMenu = false
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
                                    if (!hasSAFDirectory) {
                                        showSetupSAFDialog = true
                                    } else {
                                        showBackupDialog = true
                                    }
                                }
                            )
                            
                            // Option Configurer dossier (nouveau)
                            DropdownMenuItem(
                                text = { Text("Dossier") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Configurer le dossier de sauvegarde"
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    directoryLauncher.launch(null)
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
                                onEditEndTime = { newTime -> viewModel.updateEndTime(activity.id, newTime) },
                                onEditBothTimes = { newStartTime, newEndTime -> 
                                    viewModel.updateStartAndEndTime(activity.id, newStartTime, newEndTime)
                                },
                                viewModel = viewModel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        // Boîte de dialogue de configuration initiale du SAF
        if (showSetupSAFDialog) {
            AlertDialog(
                onDismissRequest = { showSetupSAFDialog = false },
                title = { Text("Configuration requise") },
                text = { 
                    Text(
                        "Pour utiliser les fonctionnalités de sauvegarde, vous devez d'abord " +
                        "sélectionner un dossier où stocker vos sauvegardes."
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            directoryLauncher.launch(null)
                            showSetupSAFDialog = false
                        }
                    ) {
                        Text("Sélectionner un dossier")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSetupSAFDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
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
                            viewModel.backupDatabaseSAF(context, backupName.trim())
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
            SAFBackupListDialog(
                backups = backups,
                onDismiss = { showBackupListDialog = false },
                onRestore = { backupUri -> 
                    showBackupListDialog = false
                    showRestoreConfirmDialog = backupUri
                },
                onDelete = { backupUri -> 
                    viewModel.deleteBackupSAF(context, backupUri)
                }
            )
        }
        
        // Boîte de dialogue de confirmation pour restaurer une sauvegarde
        showRestoreConfirmDialog?.let { backupUri -> 
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
                            viewModel.restoreDatabaseSAF(context, backupUri)
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

// Composant pour afficher la liste des sauvegardes SAF
@Composable
fun SAFBackupListDialog(
    backups: List<SAFBackupHelper.BackupInfo>,
    onDismiss: () -> Unit,
    onRestore: (Uri) -> Unit,
    onDelete: (Uri) -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf<Uri?>(null) }
    
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
                    items(backups) { backup -> 
                        SAFBackupItem(
                            backup = backup,
                            onRestore = { onRestore(backup.uri) },
                            onDelete = { showDeleteConfirmDialog = backup.uri }
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
    showDeleteConfirmDialog?.let { uri -> 
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
                        onDelete(uri)
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
fun SAFBackupItem(
    backup: SAFBackupHelper.BackupInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
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
                text = backup.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Modifié le: ${backup.getFormattedDate()}",
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
    onEditEndTime: (Long) -> Unit,
    onEditBothTimes: (Long, Long) -> Unit = { _, _ -> }, // Nouveau callback pour éditer les deux valeurs
    viewModel: EditViewModel? = null // Ajout du viewModel pour pouvoir rafraîchir les activités
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
    
    // Utilisation du composant ActivityEditDialog externalisé
    if (showEditDialog) {
        ActivityEditDialog(
            activity = activity,
            onDismiss = { showEditDialog = false },
            onEditStartTime = { newTime -> 
                onEditStartTime(newTime)
                showEditDialog = false
                viewModel?.refreshActivitiesForCurrentDay() // Rafraîchir après l'édition
            },
            onEditEndTime = { newTime -> 
                onEditEndTime(newTime)
                showEditDialog = false
                viewModel?.refreshActivitiesForCurrentDay() // Rafraîchir après l'édition
            },
            onEditBothTimes = { newStartTime, newEndTime ->
                onEditBothTimes(newStartTime, newEndTime)
                showEditDialog = false
                viewModel?.refreshActivitiesForCurrentDay() // Rafraîchir après l'édition
            }
        )
    }
}