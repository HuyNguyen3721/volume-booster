package com.ezstudio.volumebooster.test.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.ezstudio.volumebooster.test.R
import com.ezstudio.volumebooster.test.activity.SplashActivity
import com.ezstudio.volumebooster.test.broadcast.BroadCastActionMusic
import com.ezstudio.volumebooster.test.viewmodel.MusicActiveViewModel
import org.koin.android.ext.android.inject
import java.io.IOException

class LoudnessService : Service() {
    var mEqualizer: Equalizer? = null
    var bassBoost: BassBoost? = null
    var virtualization: PresetReverb? = null
    var mediaPlayer: MediaPlayer? = null
    var laugh: LoudnessEnhancer? = null

    //
    private var audioManager: AudioManager? = null
    private var playbackAttributes: AudioAttributesCompat? = null
    private var focusRequest: AudioFocusRequestCompat? = null
    var telephonyManager: TelephonyManager? = null

    //
    private var broadCastActionMusic: BroadCastActionMusic? = null

    //
    private val viewModel by inject<MusicActiveViewModel>()

    companion object {
        var isRunning = false
    }


    // service chuyền dữ liệu trả về dạng binder
    class MyBinder(val service: LoudnessService) : Binder()

    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    fun createNotification(valueVolume: Int? = null) {
        createChannel()
        val removeView = createRemoveView(valueVolume)
        val notification = NotificationCompat.Builder(this, "No")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_icon_notify_display)
            .setContent(removeView)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        pendingIntent(removeView, mediaPlayer?.isPlaying == true)
        startForeground(1, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("No", "Music", importance)
            mChannel.setSound(null, null)
            mChannel.description = "No"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun createRemoveView(valueVolumeNotify: Int? = null): RemoteViews {
        val remoteView = RemoteViews(packageName, R.layout.layout_notification)
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
        val valueVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        if (mediaPlayer != null) {
            remoteView.setViewVisibility(R.id.layout_control_music_notify, View.VISIBLE)
            remoteView.setTextViewText(
                R.id.name_song, viewModel.nameMusicSong.value
            )
            remoteView.setTextViewText(
                R.id.author_name, viewModel.nameAuthorSong.value
            )
            remoteView.setImageViewResource(
                R.id.ic_play_pause,
                if (mediaPlayer?.isPlaying == true) R.drawable.ic_pause_notify else R.drawable.ic_play_notify
            )
        } else {
            remoteView.setViewVisibility(R.id.layout_control_music_notify, View.GONE)
        }
        if (maxVolume != 0) {
            remoteView.setViewVisibility(R.id.txt_value, View.VISIBLE)
            when (valueVolume) {
                (maxVolume * 0.3).toInt() -> {
                    remoteView.setTextViewText(
                        R.id.txt_value,
                        "${30 + viewModel.percentValue.value!!}%"
                    )
                }
                (maxVolume * 0.6).toInt() -> {
                    remoteView.setTextViewText(
                        R.id.txt_value,
                        "${60 + viewModel.percentValue.value!!}%"
                    )
                }
                else -> {
                    remoteView.setTextViewText(
                        R.id.txt_value,
                        "${valueVolumeNotify ?: (((valueVolume / maxVolume.toFloat()) * 100).toInt()) + viewModel.percentValue.value!!}%"
                    )
                }
            }

        } else {
            remoteView.setViewVisibility(R.id.txt_value, View.GONE)
        }
        return remoteView
    }

    private fun pendingIntent(rv: RemoteViews, isPlaying: Boolean) {
        //play pause
        if (isPlaying) {
            val intentPause = Intent()
            intentPause.action = getString(R.string.action_pause)
            val pendingIntentPause =
                PendingIntent.getBroadcast(this, 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.ic_play_pause, pendingIntentPause)
        } else {
            val intentPlay = Intent()
            intentPlay.action = getString(R.string.action_play)
            val pendingIntentPlay =
                PendingIntent.getBroadcast(this, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT)
            rv.setOnClickPendingIntent(R.id.ic_play_pause, pendingIntentPlay)
        }
        // back
        val intentBack = Intent()
        intentBack.action = getString(R.string.action_back)
        val pendingIntentBack =
            PendingIntent.getBroadcast(this, 0, intentBack, PendingIntent.FLAG_UPDATE_CURRENT)
        rv.setOnClickPendingIntent(R.id.ic_back_notify, pendingIntentBack)
        // next
        val intentNext = Intent()
        intentNext.action = getString(R.string.action_next)
        val pendingIntentPause =
            PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT)
        rv.setOnClickPendingIntent(R.id.ic_next_notify, pendingIntentPause)

        //close
        val intentClose = Intent()
        intentClose.action = getString(R.string.action_close)
        val pendingIntentClose =
            PendingIntent.getBroadcast(this, 0, intentClose, PendingIntent.FLAG_UPDATE_CURRENT)
        rv.setOnClickPendingIntent(R.id.ic_close, pendingIntentClose)

        //open
        val intentOpen = Intent(this, SplashActivity::class.java)
        val pendingIntentOpen = PendingIntent.getActivity(this, 0, intentOpen, 0)
        rv.setOnClickPendingIntent(R.id.layout_notify, pendingIntentOpen)
        // booster now
        val intentBoosterNow = Intent(this, SplashActivity::class.java)
        val pendingIntentBoosterNow = PendingIntent.getActivity(this, 0, intentBoosterNow, 0)
        rv.setOnClickPendingIntent(R.id.txt_boost_now, pendingIntentBoosterNow)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        registerBroadcast()
        createAudioManager()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return MyBinder(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ServiceCast")
    fun incomingCallMusic() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        onPauseMusic()
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        onPauseMusic()
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        onPauseMusic()
                    }
                }
            }
        }
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    //
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
//        removeAudioFocus
        removeAudioFocus()
        // unRegisterBroadcast
        unRegisterBroadcast()
        //
        laugh?.let {
            it.enabled = false
            it.release()
            laugh = null
        }
        // mediaPlay
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        //releaseEqualizer
        releaseEqualizer()

    }

    // mediaCompact
    private fun createAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        playbackAttributes = AudioAttributesCompat.Builder()
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_SPEECH)
            .build()

        focusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setAudioAttributes(playbackAttributes ?: return)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(autofocusChangeListener)
            .build()
    }

    // audio manager
    private var autofocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        onPlayMusic(null)
                    } else {
                        onPauseMusic()
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                onPauseMusic()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                onPauseMusic()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.setVolume(0.1f, 0.1f)
                    }
                }
            }
        }
    }

    private fun requestAudioFocus() {
        if (audioManager != null && focusRequest != null) {
            AudioManagerCompat.requestAudioFocus(
                audioManager!!,
                focusRequest!!,
            )
        }
    }

    private fun removeAudioFocus() {
        if (audioManager != null && focusRequest != null) {
            AudioManagerCompat.abandonAudioFocusRequest(
                audioManager!!,
                focusRequest!!,
            )
        }
    }

    // play
    fun onPlayMusic(path: String?, complete: ((Boolean) -> Unit)? = null) {
        //
        requestAudioFocus()
        //
        try {
            if (path != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.reset()
                mediaPlayer?.release()
                mediaPlayer = null
                mediaPlayer = MediaPlayer()
                mediaPlayer?.apply {
                    setDataSource(path)
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    prepareAsync()
                }
                mediaPlayer?.setOnPreparedListener {
                    it.start()
                    complete?.invoke(true)
                    createNotification()
                    viewModel.isMusicActive.value = (true)
                }
//                mediaPlayer?.setOnCompletionListener {
//
//                }
            } else {
                mediaPlayer?.let {
                    viewModel.isMusicActive.value = (true)
                    it.start()
                    complete?.invoke(true)
                    createNotification()
                }
            }
            mediaPlayer?.setOnCompletionListener {
                complete?.invoke(true)
                onPauseMusic()
            }
        } catch (e: IOException) {
            Toast.makeText(this, getString(R.string.cant_play_song), Toast.LENGTH_SHORT).show()
            onPauseMusic()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

    }

    // pause
    fun onPauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        viewModel.isMusicActive.value = (false)
        createNotification()
    }

    //rigisterBroadcast
    private fun registerBroadcast() {
        if (broadCastActionMusic == null) {
            val intent = IntentFilter()
            intent.addAction(getString(R.string.action_back))
            intent.addAction(getString(R.string.action_close))
            intent.addAction(getString(R.string.action_next))
            intent.addAction(getString(R.string.action_pause))
            intent.addAction(getString(R.string.action_play))
            broadCastActionMusic = BroadCastActionMusic()
            // listener
            broadCastActionMusic?.listenerPlay = {
                onPlayMusic(null)
            }
            broadCastActionMusic?.listenerPause = {
                onPauseMusic()
            }
            broadCastActionMusic?.listenerBack = {
                viewModel.actionBack.value = (true)
            }
            broadCastActionMusic?.listenerNext = {
                viewModel.actionNext.value = (true)
            }
            broadCastActionMusic?.listenerClose = {
                stopForeground(true)
                stopSelf()
            }
            this.registerReceiver(broadCastActionMusic, intent)
        }
    }

    private fun unRegisterBroadcast() {
        this.unregisterReceiver(broadCastActionMusic)
    }

    fun releaseEqualizer() {
        if (mEqualizer != null) {
            mEqualizer!!.enabled = false
            mEqualizer!!.release()
            mEqualizer = null
        }
        //
        if (bassBoost != null) {
            bassBoost!!.enabled = false
            bassBoost!!.release()
            bassBoost = null
        }
        //
        if (virtualization != null) {
            virtualization!!.enabled = false
            virtualization!!.release()
            virtualization = null
        }
    }

}