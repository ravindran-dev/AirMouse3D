package com.airmouse3d.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Gyroscope/accelerometer access requires no runtime permission on Android (that only applies
 * to [Manifest.permission.BODY_SENSORS]-gated biometric sensors). The one runtime permission
 * this app needs is POST_NOTIFICATIONS, to keep the foreground-service notification visible
 * on Android 13+.
 */
object PermissionUtils {

    fun notificationPermissionRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun hasNotificationPermission(context: Context): Boolean {
        if (!notificationPermissionRequired()) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
