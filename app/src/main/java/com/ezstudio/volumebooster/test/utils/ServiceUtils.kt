package com.ezstudio.volumebooster.test.utils

import android.app.ActivityManager
import android.content.Context
import android.media.AudioFormat

object ServiceUtils {
    fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name==service.service.className) {
                return true
            }
        }
        return false
    }
}