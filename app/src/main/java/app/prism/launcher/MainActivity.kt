package app.prism.launcher

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import app.prism.launcher.ui.home.HomeScreen
import app.prism.launcher.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private val roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge with transparent bars (Android 10 gesture navigation).
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.isNavigationBarContrastEnforced = false // API 29, our minSdk

        // A home app must not exit on Back. Later milestones: close folder/drawer here.
        onBackPressedDispatcher.addCallback(this) { /* intentionally empty */ }

        setContent { HomeScreen(viewModel) }
        window.decorView.post { offerDefaultLauncherOnce() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home pressed while already open (singleTask). Milestone 2+: go to first page, close folders/drawer.
    }

    /** First run only: ask to become the home app. RoleManager.ROLE_HOME is API 29. */
    private fun offerDefaultLauncherOnce() {
        val prefs = getSharedPreferences("prism_prefs", MODE_PRIVATE) // moves to DataStore in milestone 8
        if (prefs.getBoolean("asked_default", false)) return
        val roles = getSystemService(RoleManager::class.java)
        if (roles.isRoleHeld(RoleManager.ROLE_HOME)) return
        prefs.edit().putBoolean("asked_default", true).apply()

        val intent = if (roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
            roles.createRequestRoleIntent(RoleManager.ROLE_HOME)
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS) // fallback: system "Home app" screen
        }
        try { roleRequest.launch(intent) } catch (_: ActivityNotFoundException) { }
    }
}
