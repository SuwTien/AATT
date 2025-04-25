package fr.bdst.aatt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.bdst.aatt.data.db.AATTDatabase
import fr.bdst.aatt.data.model.Activity
import fr.bdst.aatt.data.model.ActivityType
import fr.bdst.aatt.data.util.StatisticsCalculator
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
    val completedActivities by viewModel.completedActivities.collectAsState()
    val backups by viewModel.backups.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    val operationResult by viewModel.operationResult.collectAsState()
    
    // États pour gérer les boîtes de dialogue
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showBackupListDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf<String?>(null) } // Stocke le chemin de la sauvegarde à restaurer
    var backupName by remember { mutableStateOf("") }
    
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
                title = { Text("Édition des activités") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Bouton pour gérer les sauvegardes
                    IconButton(onClick = { 
                        viewModel.refreshBackupsList(context)
                        showBackupListDialog = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Gérer les sauvegardes"
                        )
                    }
                    
                    // Bouton de sauvegarde
                    IconButton(onClick = { showBackupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Sauvegarder les activités"
                        )
                    }
                    
                    // Bouton pour effacer toutes les activités
                    IconButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Effacer toutes les activités"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) { data ->
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (completedActivities.isEmpty()) {
                // Affichage lorsqu'il n'y a pas d'activités terminées
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune activité terminée à afficher",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Liste des activités terminées
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(completedActivities) { activity ->
                        ActivityItem(
                            activity = activity,
                            dateFormat = dateFormat,
                            onDelete = { viewModel.deleteActivity(activity.id) },
                            onReactivate = { viewModel.reactivateActivity(activity.id) },
                            onEditStartTime = { newTime -> viewModel.updateStartTime(activity.id, newTime) },
                            onEditEndTime = { newTime -> viewModel.updateEndTime(activity.id, newTime) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                        TextButton(
                            onClick = { 
                                showBackupDialog = false 
                                backupName = ""
                            }
                        ) {
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
}

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
                        Divider()
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
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onReactivate: () -> Unit,
    onEditStartTime: (Long) -> Unit,
    onEditEndTime: (Long) -> Unit
) {
    var showEditStartDialog by remember { mutableStateOf(false) }
    var showEditEndDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Type d'activité
            Text(
                text = when (activity.type) {
                    ActivityType.VS -> "Visite Semestrielle"
                    ActivityType.ROUTE -> "Route"
                    ActivityType.DOMICILE -> "Domicile"
                    ActivityType.PAUSE -> "Pause"
                },
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Heures de début et de fin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Début",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateFormat.format(Date(activity.startTime)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { showEditStartDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier l'heure de début"
                            )
                        }
                    }
                }
                
                Column {
                    Text(
                        text = "Fin",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activity.endTime?.let { dateFormat.format(Date(it)) } ?: "En cours",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { showEditEndDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier l'heure de fin"
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Durée de l'activité
            Text(
                text = "Durée: ${
                    StatisticsCalculator.formatDuration(
                        activity.endTime?.let { it - activity.startTime } ?: 0L
                    )
                }",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Boutons d'action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onReactivate) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Réactiver"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Réactiver")
                }
                
                TextButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Supprimer")
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
    
    // Note: Pour une implémentation complète, il faudrait ajouter des sélecteurs de date et heure
    // pour éditer les heures de début et fin. On pourrait utiliser une bibliothèque comme
    // Material DateTimePicker pour cela.
}