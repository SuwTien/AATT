package fr.bdst.aatt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

/**
 * Activité d'écran de démarrage personnalisé
 * Affiche l'image en plein écran sans cadre
 */
class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DELAY = 800L // Durée d'affichage en millisecondes
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuration pour affichage en plein écran
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // Définir le layout
        setContentView(R.layout.activity_splash)
        
        // Délai avant de lancer l'activité principale
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Fermer cette activité pour qu'on ne puisse pas y revenir avec le bouton retour
        }, SPLASH_DELAY)
    }
}