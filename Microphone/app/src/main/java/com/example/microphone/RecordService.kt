package com.example.microphone

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.IOException
import java.util.*

class RecordService : Service() {

    private val handler = Handler()
    private lateinit var recorder: MediaRecorder

    private val dataUpdating: Runnable = object : Runnable {
        override fun run() {
            sendAmplitudeValue(recorder.maxAmplitude)
            handler.postDelayed(this, 30)
        }
    }

    override fun onCreate() {
        super.onCreate()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(getOutputFileName())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "'prepare' method is failed")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (intent.action == SEND_AMPLITUDE_VALUES_ACTION) {
                recorder.start()
                handler.post(dataUpdating)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(dataUpdating)
        recorder.stop()
        recorder.release()
        super.onDestroy()
    }

    private fun sendAmplitudeValue(value: Int) {
        val intent = Intent(SEND_AMPLITUDE_VALUES_ACTION).putExtra(AMPLITUDE_VALUE_EXTRA, value)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun getOutputFileName(): String {
        return "${externalCacheDir!!.absolutePath}/${Date()}.3gp"
    }

    companion object {
        const val SEND_AMPLITUDE_VALUES_ACTION = "send_amplitude_values_action"
        private const val TAG = "RECORD_SERVICE"
        private const val AMPLITUDE_VALUE_EXTRA = "amplitude_value_extra"

        fun getAmplitudeValueExtra(intent: Intent): Int {
            return intent.getIntExtra(AMPLITUDE_VALUE_EXTRA, 0)
        }

        fun getIntent(context: Context): Intent {
            return Intent(context, RecordService::class.java).apply {
                action = SEND_AMPLITUDE_VALUES_ACTION
            }
        }
    }
}