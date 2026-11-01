package com.stiki.app.mazejourney

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var playButton: ImageButton
    private lateinit var aboutButton: ImageButton
    private lateinit var exitButton: ImageButton


    // Notification channel ID and name
    private val CHANNEL_ID = "maze_journey_channel"
    private val CHANNEL_NAME = "Maze Journey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request necessary permissions
        requestPermissions()

        usernameEditText = findViewById(R.id.usernameEditText)
        playButton = findViewById(R.id.playButton)
        aboutButton = findViewById(R.id.aboutButton)
        exitButton = findViewById(R.id.exitButton)

        createNotificationChannel() // Create notification channel if needed

        playButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(this, "Masukkan Username", Toast.LENGTH_SHORT).show()
            } else if (username.length > 10) {
                Toast.makeText(this, "Username tidak boleh melebihi 10 karakter!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, LevelListActivity::class.java)
                intent.putExtra("USERNAME", username)
                startActivity(intent)
            }
        }

        aboutButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Apakah yakin kamu ingin keluar?")
            .setPositiveButton("Iya") { dialog, _ ->
                dialog.dismiss()
                showExitNotification() // Show notification when user chooses to exit
                finish()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showExitNotification() {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_mazejourney)
            .setContentTitle("Selesaikan semua labirin!")
            .setContentText("Terima kasih telah bermain.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

    // Create notification channel for Android 8.0 (API level 26) and higher
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Maze Journey Notifications"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Request necessary permissions
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.VIBRATE,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            "android.permission.POST_NOTIFICATIONS"
        )
        ActivityCompat.requestPermissions(this, permissions, 0)
    }
}