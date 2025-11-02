package com.foxelectronic.audioplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val folders: List<String> = emptyList(),
    val accentHex: String = "#6750A4",
    val fontType: FontType = FontType.JETBRAINS_MONO
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SettingsRepository(application)
    val settingsRepository: SettingsRepository
        get() = repo

    val uiState: StateFlow<SettingsUiState> = combine(
        repo.themeFlow,
        repo.foldersFlow,
        repo.accentFlow,
        repo.fontTypeFlow
    ) { theme, folders, accent, fontType ->
        SettingsUiState(theme, folders, accent, fontType)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { repo.setTheme(theme) }
    }

    fun setFolders(uris: List<String>) {
        viewModelScope.launch { repo.setFolders(uris) }
    }

    fun setAccent(hex: String) {
        viewModelScope.launch { repo.setAccent(hex) }
    }

    fun setFontType(fontType: FontType) {
        viewModelScope.launch { repo.setFontType(fontType) }
    }
}
