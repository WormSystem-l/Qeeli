package app.prism.launcher.data

import android.content.ComponentName
import android.os.UserHandle
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/** One launchable activity. Immutable so Compose can skip recomposition. */
@Immutable
data class AppInfo(
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    val icon: ImageBitmap,
) {
    val key: String get() = "${component.flattenToShortString()}#${user.hashCode()}"
}
