package app.prism.launcher.ui.home

import android.app.Application
import android.graphics.Rect
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.prism.launcher.data.AppInfo
import app.prism.launcher.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class HomeUiState(val apps: List<AppInfo> = emptyList(), val loading: Boolean = true)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(application)
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch { _state.value = HomeUiState(apps = repo.loadApps(), loading = false) }
    }

    fun launch(app: AppInfo, bounds: Rect?) = repo.launch(app, bounds)
}
