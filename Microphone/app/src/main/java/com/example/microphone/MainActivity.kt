package com.example.microphone

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isRecordingMode = false
    private val handler = Handler()

    private val chartUpdating: Runnable = object : Runnable {
        override fun run() {
            RecorderHolder.recorder?.let {
                bar_chart.addValue(it.maxAmplitude)
                handler.postDelayed(this, 30)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isAccepted = requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (!isAccepted) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState?.apply {
            isRecordingMode = getBoolean(IS_RECORDING_MODE_EXTRA)
        }

        updateRecordButtonView()
        record_button.apply {
            setOnClickListener {
                if (isRecordingMode) {
                    stopRecording()
                } else {
                    startRecording()
                    handler.post(chartUpdating)
                }
                isRecordingMode = !isRecordingMode
                updateRecordButtonView()
            }
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE)
    }

    override fun onStart() {
        if (isRecordingMode) handler.post(chartUpdating)
        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_RECORDING_MODE_EXTRA, isRecordingMode)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(chartUpdating)
    }

    private fun startRecording() {
        RecorderHolder.recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(getOutputFileName())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, "'prepare' method is failed")
            }
            start()
            bar_chart.changeConfig(50)
        }
    }

    private fun stopRecording() {
        RecorderHolder.recorder?.apply {
            stop()
            release()
            RecorderHolder.recorder = null
            bar_chart.clear()
        }
    }

    private fun getOutputFileName(): String {
        return "${externalCacheDir!!.absolutePath}/${Date()}.3gp"
    }

    private fun updateRecordButtonView() {
        record_button.setText(if (isRecordingMode) R.string.stop_label else R.string.start_label)
    }

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
        private const val IS_RECORDING_MODE_EXTRA = "is_recording_mode_extra"
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    }
}
