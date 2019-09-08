package com.example.microphone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isRecordingMode = false
    private lateinit var receiver: AmplitudeValueReceiver

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val isAccepted = requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (isAccepted) {
            isRecordingMode = true
            updateRecordButtonView()
            bar_chart.changeConfig(100)
            startService(RecordService.getIntent(this))
        } else {
            Toast.makeText(this, R.string.no_audio_record_permission_message, Toast.LENGTH_SHORT).show()
        }
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
                }
            }
        }

        receiver = AmplitudeValueReceiver()
    }

    override fun onStart() {
        val intentFilter = IntentFilter(RecordService.SEND_AMPLITUDE_VALUES_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)
        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (isRecordingMode) bar_chart.clear()
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_RECORDING_MODE_EXTRA, isRecordingMode)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopRecording()
    }

    private fun startRecording() {
        if (!isRecordingMode) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    private fun stopRecording() {
        if (isRecordingMode) {
            stopService(RecordService.getIntent(this))
            bar_chart.clear()
            isRecordingMode = false
            updateRecordButtonView()
        }
    }

    private fun updateRecordButtonView() {
        record_button.setText(if (isRecordingMode) R.string.stop_label else R.string.start_label)
    }

    private inner class AmplitudeValueReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (isRecordingMode && intent.action == RecordService.SEND_AMPLITUDE_VALUES_ACTION) {
                    val x = RecordService.getAmplitudeValueExtra(intent)
                    bar_chart.addValue(x)
                }
            }
        }
    }

    companion object {
        private const val IS_RECORDING_MODE_EXTRA = "is_recording_mode_extra"
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    }
}
