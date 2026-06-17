package com.marconius.wordbopper.ui

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper

/**
 * Whether the system-wide "remove animations" setting is on. Android exposes this as an
 * animator duration scale of 0 (Settings > Accessibility > Remove animations, also reachable
 * via Developer options). This mirrors iOS's accessibilityReduceMotion: when true, gameplay
 * animations should be skipped in favor of instant state changes.
 *
 * Defaults to false so animations play unless the user has explicitly disabled them system-wide.
 */
val LocalReduceMotion = compositionLocalOf { false }

private fun animationsDisabled(resolver: android.content.ContentResolver): Boolean {
    val scale = Settings.Global.getFloat(
        resolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    return scale == 0f
}

/**
 * Reads the system animator duration scale and keeps it live via a ContentObserver, so toggling
 * "Remove animations" while the app is open takes effect immediately.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val resolver = context.contentResolver
    var reduceMotion by remember { mutableStateOf(animationsDisabled(resolver)) }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduceMotion = animationsDisabled(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        // Re-read in case it changed between the initial read and registration.
        reduceMotion = animationsDisabled(resolver)
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduceMotion
}
