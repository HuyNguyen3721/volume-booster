package com.ezstudio.volumebooster.test.broadcast

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler

class SettingsContentObserver(var context: Context, handler: Handler?) : ContentObserver(handler) {
    var previousVolume: Int

    var onChangeSettings: ((Int) -> Unit)? = null
    override fun deliverSelfNotifications(): Boolean {
        return super.deliverSelfNotifications()
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val delta = previousVolume - currentVolume
        if (delta > 0) {
            previousVolume = currentVolume
            onChangeSettings?.invoke(previousVolume)
        } else if (delta < 0) {
            previousVolume = currentVolume
            onChangeSettings?.invoke(previousVolume)
        }
    }

    init {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}