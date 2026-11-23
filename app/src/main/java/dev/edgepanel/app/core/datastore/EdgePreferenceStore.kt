package dev.edgepanel.app.core.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore requires exactly one instance per file. Hilt owns that instance.
 * A corrupt file is replaced and the exception is logged; it is not discarded quietly.
 */
@Singleton
class EdgePreferenceStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    val dataStore = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { error ->
            Log.e(TAG, "EDGE preferences were corrupt and have been reset.", error)
            emptyPreferences()
        },
        produceFile = { context.preferencesDataStoreFile("edge_preferences") },
    )

    private companion object {
        const val TAG = "EDGE"
    }
}
