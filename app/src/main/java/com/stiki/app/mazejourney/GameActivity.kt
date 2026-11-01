package com.stiki.app.mazejourney

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.stiki.app.mazejourney.view.MazeView

class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mazeView: MazeView
    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var lastShakeTime: Long = 0
    private val shakeThreshold = 250
    private lateinit var timerTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 60000 // 60 seconds
    private var currentLevel: Int = 1
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        sharedPreferences = getSharedPreferences("MazeJourneyPrefs", MODE_PRIVATE)
        currentLevel = intent.getIntExtra("LEVEL", 1)


        val currentLevel = intent.getIntExtra("LEVEL", 1)
        val currentLevelTextView: TextView = findViewById(R.id.currentLevelTextView)
        currentLevelTextView.text = "LEVEL $currentLevel"


        mazeView = findViewById(R.id.mazeView)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Cek apakah gyroscope tersedia
        if (gyroscopeSensor == null && accelerometerSensor == null) {
            Toast.makeText(this, "Gyroscope dan Accelerometer tidak tersedia di perangkat ini.", Toast.LENGTH_LONG).show()
        }

        timerTextView = findViewById(R.id.timerTextView)

        mazeView.setLevel(currentLevel)
        mazeView.setOnMazeCompleteListener(object : MazeView.OnMazeCompleteListener {
            override fun onMazeComplete() {
                completeLevel()
            }

            override fun onWallCollision() {
                handleWallCollision()
            }
        })

        setupMovementButtons()
        startTimer()
    }

    private fun setupMovementButtons() {
        findViewById<ImageButton>(R.id.buttonUp).setOnClickListener { mazeView.movePlayer(MazeView.Direction.UP) }
        findViewById<ImageButton>(R.id.buttonDown).setOnClickListener { mazeView.movePlayer(MazeView.Direction.DOWN) }
        findViewById<ImageButton>(R.id.buttonLeft).setOnClickListener { mazeView.movePlayer(MazeView.Direction.LEFT) }
        findViewById<ImageButton>(R.id.buttonRight).setOnClickListener { mazeView.movePlayer(MazeView.Direction.RIGHT) }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                gameOver()
            }
        }.start()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timeFormatted
    }

    private fun gameOver() {
        Toast.makeText(this, "Waktu habis! Kamu kalah", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun completeLevel() {
        countDownTimer.cancel()
        val unlockedLevel = sharedPreferences.getInt("UNLOCKED_LEVEL", 1)
        if (currentLevel >= unlockedLevel) {
            sharedPreferences.edit().putInt("UNLOCKED_LEVEL", currentLevel + 1).apply()
        }

        score = (timeLeftInMillis / 1000).toInt() * 10 // Example scoring: 10 points per second left
        showCompletionDialog()
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Selamat! Berhasil Lolos")
            .setMessage("Kamu telah menyelesaikan level $currentLevel.\nScore: $score/600")
            .setPositiveButton("LANJUT") { _, _ ->
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("LEVEL", currentLevel + 1)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("KEMBALI") { _, _ ->
                val intent = Intent(this, LevelListActivity::class.java)
                startActivity(intent)
                finish()
            }
            .show()
    }

    private fun handleWallCollision() {
        Toast.makeText(this, "Awas! Kamu menabrak dinding", Toast.LENGTH_SHORT).show()
        @Suppress("DEPRECATION") val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200) // Getar selama 200ms
        }
        mazeView.resetPlayer()
    }

    override fun onResume() {
        super.onResume()
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        } ?: accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                handleGyroscope(it)
            } else if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                handleAccelerometer(it)
            }
        }
    }

    private fun handleGyroscope(event: SensorEvent) {
        val rotationRateX = event.values[0]  // Sumbu X
        val rotationRateY = event.values[1]  // Sumbu Y

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime > shakeThreshold) {
            lastShakeTime = currentTime

            when {
                rotationRateY > 1.0 -> mazeView.movePlayer(MazeView.Direction.RIGHT)  // Gerakan ke kanan
                rotationRateY < -1.0 -> mazeView.movePlayer(MazeView.Direction.LEFT)   // Gerakan ke kiri
                rotationRateX < -1.0 -> mazeView.movePlayer(MazeView.Direction.UP)      // Gerakan ke atas
                rotationRateX > 1.0 -> mazeView.movePlayer(MazeView.Direction.DOWN)   // Gerakan ke bawah
            }
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val accelX = event.values[0]  // Akselerasi pada sumbu X
        val accelY = event.values[1]  // Akselerasi pada sumbu Y

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime > shakeThreshold) {
            lastShakeTime = currentTime

            when {
                accelY > 12 -> mazeView.movePlayer(MazeView.Direction.RIGHT)  // Gerakan ke kanan
                accelY < -12 -> mazeView.movePlayer(MazeView.Direction.LEFT)   // Gerakan ke kiri
                accelX < -12 -> mazeView.movePlayer(MazeView.Direction.UP)      // Gerakan ke atas
                accelX > 12 -> mazeView.movePlayer(MazeView.Direction.DOWN)   // Gerakan ke bawah
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
