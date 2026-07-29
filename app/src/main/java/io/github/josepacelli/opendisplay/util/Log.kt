package io.github.josepacelli.opendisplay.util

import android.util.Log as AndroidLog

/** Thin wrapper over [android.util.Log], one tag for the whole app — mirrors
 * the upstream `iOS/Log.swift`/`Mac/Log.swift` convention so `adb logcat -s OpenDisplay:*`
 * shows the full pipeline story in one filter. */
object Log {
    private const val TAG = "OpenDisplay"

    fun info(message: String) {
        AndroidLog.i(TAG, message)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        AndroidLog.w(TAG, message, throwable)
    }

    fun error(message: String, throwable: Throwable? = null) {
        AndroidLog.e(TAG, message, throwable)
    }
}
