package dev.edgepanel.app.core.common

import android.util.Log
import javax.inject.Inject

class AndroidEdgeLogger @Inject constructor() : EdgeLogger {
    override fun debug(message: String) {
        Log.d(TAG, message)
    }

    override fun info(message: String) {
        Log.i(TAG, message)
    }

    override fun error(message: String, error: Throwable?) {
        if (error == null) {
            Log.e(TAG, message)
        } else {
            Log.e(TAG, message, error)
        }
    }

    private companion object {
        const val TAG = "EDGE"
    }
}
