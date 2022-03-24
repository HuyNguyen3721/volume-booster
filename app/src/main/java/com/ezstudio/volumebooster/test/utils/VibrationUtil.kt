package com.ezstudio.volumebooster.test.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast


object VibrationUtil {
    const val KEY_VIBRATION = "KEY_VIBRATION"
    fun startVibration(context: Context) {
        val v: Vibrator? = context.getSystemService(VIBRATOR_SERVICE) as Vibrator?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                v?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (e: NullPointerException) {
                Toast.makeText(context, "Can't start vibrate in your device .", Toast.LENGTH_SHORT).show()
            }
        } else {
            v?.vibrate(20)
        }
    }
}