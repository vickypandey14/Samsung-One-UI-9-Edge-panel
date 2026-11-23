package dev.edgepanel.app.features.clipboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.edgepanel.app.core.datastore.EdgePreferencesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A focused, invisible activity. One UI only gives clipboard text to the
 * focused app, and the overlay window is not focused.
 */
@AndroidEntryPoint
class ClipboardReadActivity : ComponentActivity() {
    @Inject lateinit var clipboard: EdgeClipboard
    @Inject lateinit var preferences: EdgePreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val text = clipboard.read()
            if (text == null) {
                clipboard.report("Clipboard is empty.")
            } else {
                preferences.setScratch(text)
                clipboard.report("Clipboard replaced the note.")
            }
            finish()
        }
    }
}
