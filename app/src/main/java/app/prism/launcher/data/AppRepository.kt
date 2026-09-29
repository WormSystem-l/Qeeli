package app.prism.launcher.data

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.UserManager
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads launchable apps through LauncherApps (API 21+, fine on Android 10). */
class AppRepository(context: Context) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)

    // Icons are rasterised once at ~56dp; the glass renderer (milestone 4) will replace this.
    private val iconPx = (appContext.resources.displayMetrics.density * 56).roundToInt()

    /** Heavy work (icon rasterising) runs off the main thread. */
    suspend fun loadApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val result = ArrayList<AppInfo>()
        for (user in userManager.userProfiles) {
            for (activity in launcherApps.getActivityList(null, user)) {
                if (activity.componentName.packageName == appContext.packageName) continue
                // getBadgedIcon keeps the work-profile badge; dpi 0 = launcher default density.
                val drawable = activity.getBadgedIcon(0)
                result += AppInfo(
                    label = activity.label.toString(),
                    component = activity.componentName,
                    user = user,
                    icon = drawable.toBitmap(iconPx, iconPx).asImageBitmap(),
                )
            }
        }
        result.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    /** Must be called from a foreground UI action (Android 10 background-start rules). */
    fun launch(app: AppInfo, sourceBounds: Rect?) {
        try {
            launcherApps.startMainActivity(app.component, app.user, sourceBounds, null)
        } catch (e: Exception) {
            Log.w("AppRepository", "Could not launch ${app.component}", e)
        }
    }
}
