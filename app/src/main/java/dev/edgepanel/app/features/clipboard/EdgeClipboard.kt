package dev.edgepanel.app.features.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgeClipboard @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = context.getSystemService(ClipboardManager::class.java)
    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun read(): String? {
        val clip = manager.primaryClip
        val text = clip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
            ?.trim()
            .orEmpty()
        return text.takeIf { it.isNotEmpty() }
    }

    fun write(text: String) {
        manager.setPrimaryClip(ClipData.newPlainText("EDGE", text))
        _status.value = "Copied."
    }

    fun report(message: String) {
        _status.value = message
    }
}
