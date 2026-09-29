package app.prism.launcher.ui.home

import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.prism.launcher.data.AppInfo

/** Milestone 1: a plain grid of all apps over the real wallpaper. No glass yet. */
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val bars = WindowInsets.systemBars.asPaddingValues() // edge-to-edge: respect status/nav bars

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 12.dp, end = 12.dp,
            top = bars.calculateTopPadding() + 16.dp,
            bottom = bars.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(state.apps, key = { it.key }) { app ->
            AppCell(app) { bounds -> viewModel.launch(app, bounds) }
        }
    }
}

private class BoundsHolder { var rect: Rect? = null } // plain holder: no recomposition on layout

private val LabelStyle = TextStyle(
    color = Color.White,
    fontSize = 12.sp,
    textAlign = TextAlign.Center,
    // Soft shadow keeps labels legible on any wallpaper (spec 4.4).
    shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f), 6f),
)

@Composable
private fun AppCell(app: AppInfo, onClick: (Rect?) -> Unit) {
    val holder = remember { BoundsHolder() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .onGloballyPositioned { c ->
                val b = c.boundsInWindow()
                holder.rect = Rect(b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt())
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                onClick(holder.rect)
            }
            .padding(horizontal = 4.dp),
    ) {
        Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(56.dp))
        BasicText(
            text = app.label,
            style = LabelStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
