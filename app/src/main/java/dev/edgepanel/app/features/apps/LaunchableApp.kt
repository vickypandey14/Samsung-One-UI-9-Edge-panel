package dev.edgepanel.app.features.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dev.edgepanel.app.core.common.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LaunchableApp(
    val component: String,
    val label: String,
)

@Singleton
class LaunchableAppCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun load(): List<LaunchableApp> = withContext(dispatchers.io) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(intent, 0)
        }
        resolved.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            val component = ComponentName(activity.packageName, activity.name).flattenToString()
            val label = info.loadLabel(context.packageManager).toString().trim()
            if (label.isEmpty()) return@mapNotNull null
            LaunchableApp(component = component, label = label)
        }.distinctBy { it.component }
            .sortedBy { it.label.lowercase() }
    }
}
