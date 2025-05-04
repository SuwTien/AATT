package fr.bdst.aatt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.bdst.aatt.data.db.AATTDatabase
import fr.bdst.aatt.data.repository.ActivityRepository
import fr.bdst.aatt.data.util.AppEvents
import fr.bdst.aatt.data.util.FileLogger
import fr.bdst.aatt.ui.navigation.NavRoutes
import fr.bdst.aatt.ui.screens.EditScreen
import fr.bdst.aatt.ui.screens.MainScreen
import fr.bdst.aatt.ui.screens.StatsScreen
import fr.bdst.aatt.ui.theme.AATTTheme
import fr.bdst.aatt.viewmodel.EditViewModel
import fr.bdst.aatt.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: ActivityRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var editViewModel: EditViewModel
    
    // État pour contrôler l'affichage de l'écran de démarrage
    private var isAppReady = false
    
    // Gestionnaire de demande de permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Permissions accordées", Toast.LENGTH_SHORT).show()
            initializeApp()
        } else {
            Toast.makeText(this, "L'application peut rencontrer des limitations sans les permissions", Toast.LENGTH_LONG).show()
            initializeApp() // Initialiser quand même, mais en mode limité
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Installer l'écran de démarrage avant d'appeler super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialiser le système de logs
        initializeLogging()
        
        // Maintenir l'écran de démarrage visible jusqu'à ce que l'app soit prête
        splashScreen.setKeepOnScreenCondition { !isAppReady }
        
        // Vérifier et demander les permissions au démarrage
        checkAndRequestPermissions()
        
        // Configurer l'écouteur d'événements de restauration de base de données
        setupDatabaseRestoreListener()
    }
    
    /**
     * Initialise le système de logs pour le débogage en production
     */
    private fun initializeLogging() {
        try {
            FileLogger.init(this)
            FileLogger.i("MainActivity", "Application démarrée - ${if (isDebuggable()) "DEBUG" else "RELEASE"}")
        } catch (e: Exception) {
            // Si le logging échoue, on utilise au moins LogCat
            android.util.Log.e("MainActivity", "Impossible d'initialiser le système de logs", e)
        }
    }
    
    /**
     * Vérifie si l'application est en mode debug
     */
    private fun isDebuggable(): Boolean {
        return applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
    
    /**
     * Configure l'écouteur d'événements de restauration de base de données
     */
    private fun setupDatabaseRestoreListener() {
        lifecycleScope.launch {
            AppEvents.databaseRestoreEvent.collectLatest { event ->
                if (event.success) {
                    // Réinitialiser la base de données et les ViewModels après une restauration
                    reinitializeDatabaseAndViewModels()
                    
                    // Notifier l'utilisateur
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity, 
                            "Base de données restaurée avec succès", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    
    /**
     * Réinitialise la base de données et les ViewModels après une restauration
     */
    private fun reinitializeDatabaseAndViewModels() {
        // Avec l'approche JSON, pas besoin de rouvrir la base de données
        // On utilise simplement la connexion existante et on met à jour le repository
        val database = AATTDatabase.getDatabase(applicationContext)
        repository = ActivityRepository(database.activityDao())
        
        // Recréer les ViewModels
        mainViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(repository)
        )[MainViewModel::class.java]
        
        editViewModel = ViewModelProvider(
            this,
            EditViewModel.Factory(repository)
        )[EditViewModel::class.java]
        
        // Mettre à jour le contenu de l'activité
        setAppContent()
    }
    
    /**
     * Vérifie si les permissions nécessaires sont accordées, sinon les demande
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            // Permissions pour Android 13+
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ).forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
        } else {
            // Permissions pour Android < 13
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Toutes les permissions sont déjà accordées
            initializeApp()
        }
    }
    
    /**
     * Initialise l'application une fois les permissions gérées
     */
    private fun initializeApp() {
        // Initialisation de la base de données et du repository
        val database = AATTDatabase.getDatabase(applicationContext)
        repository = ActivityRepository(database.activityDao())
        
        // Création des ViewModels
        mainViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(repository)
        )[MainViewModel::class.java]
        
        editViewModel = ViewModelProvider(
            this,
            EditViewModel.Factory(repository)
        )[EditViewModel::class.java]
        
        // Configurer l'interface
        setAppContent()
        
        // Indiquer que l'application est prête à être affichée
        isAppReady = true
    }
    
    /**
     * Configure le contenu de l'interface utilisateur
     */
    private fun setAppContent() {
        setContent {
            AATTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Main.route
                    ) {
                        // Page principale
                        composable(NavRoutes.Main.route) {
                            MainScreen(
                                viewModel = mainViewModel,
                                navigateToEdit = { navController.navigate(NavRoutes.Edit.route) },
                                navigateToStats = { navController.navigate(NavRoutes.Stats.route) }
                            )
                        }
                        
                        // Page d'édition
                        composable(NavRoutes.Edit.route) {
                            EditScreen(
                                viewModel = editViewModel,
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Page de statistiques
                        composable(NavRoutes.Stats.route) {
                            StatsScreen(
                                repository = repository,
                                navigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}