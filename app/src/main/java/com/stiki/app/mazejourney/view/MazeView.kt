package com.stiki.app.mazejourney.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import com.stiki.app.mazejourney.R

class MazeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), SensorEventListener {

    private var currentLevel: Int = 1
    private var maze: Array<Array<Cell>> = emptyArray()
    private var playerX = 0
    private var playerY = 0
    private var startX = 0
    private var startY = 0
    private var cellSize: Int = 0
    private var listener: OnMazeCompleteListener? = null

    private val wallBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_wall)
    private val pathBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_path)
    private val finishBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.finish)
    private val startBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_start)
    private val playerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_player)

    private var darkBitmap: Bitmap? = null

    // Preallocate Rect objects
    private val rect = Rect()

    // Sensor cahaya
    private var lightSensor: Sensor? = null
    private var sensorManager: SensorManager? = null
    private var lightLevel: Float = 0.0f // Nilai sensor cahaya

    init {
        // Inisialisasi sensor manager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    fun setLevel(level: Int) {
        currentLevel = level
        maze = generateMaze(level)
        playerX = startX
        playerY = startY
        invalidate()
    }

    fun setOnMazeCompleteListener(listener: OnMazeCompleteListener) {
        this.listener = listener
    }

    interface OnMazeCompleteListener {
        fun onMazeComplete()
        fun onWallCollision()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellSize = minOf(width / maze.size, height / maze.size)

        val isLightingEnabled = if (currentLevel in 6..10) {
            lightLevel < 10 // Threshold untuk menentukan map gelap atau terang
        } else {
            false // Untuk level < 6, map tidak akan gelap
        }

        for (y in maze.indices) {
            for (x in maze[y].indices) {
                val cell = maze[y][x]
                val left = x * cellSize
                val top = y * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                rect.set(left, top, right, bottom)

                if (isLightingEnabled) {
                    // Cek apakah sel berada dalam 1 blok dari posisi pemain
                    val isNearby = (x in playerX - 1..playerX + 1) && (y in playerY - 1..playerY + 1)

                    if (isNearby) {
                        // Gambar area terang (jalur atau tembok sesuai dengan jenis sel)
                        val bitmap = when {
                            cell.isWall -> wallBitmap
                            cell.isFinish -> finishBitmap
                            cell.isStart -> startBitmap
                            else -> pathBitmap
                        }
                        canvas.drawBitmap(bitmap, null, rect, null)
                    } else {
                        // Gambar area gelap
                        canvas.drawBitmap(darkBitmap!!, null, rect, null)
                    }
                } else {
                    // Level selain 6-10, gambar seluruh peta seperti biasa
                    val bitmap = when {
                        cell.isWall -> wallBitmap
                        cell.isFinish -> finishBitmap
                        cell.isStart -> startBitmap
                        else -> pathBitmap
                    }
                    canvas.drawBitmap(bitmap, null, rect, null)
                }
            }
        }

        // Gambar pemain
        val playerLeft = playerX * cellSize
        val playerTop = playerY * cellSize
        rect.set(playerLeft, playerTop, playerLeft + cellSize, playerTop + cellSize)
        canvas.drawBitmap(playerBitmap, null, rect, null)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // Cek apakah bitmap sudah dibuat sebelumnya
        if (darkBitmap == null) {
            darkBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            val canvasDark = Canvas(darkBitmap!!)
            canvasDark.drawColor(Color.parseColor("#66000000"))
        }
    }

    fun movePlayer(direction: Direction) {
        val nextX = when (direction) {
            Direction.LEFT -> playerX - 1
            Direction.RIGHT -> playerX + 1
            else -> playerX
        }
        val nextY = when (direction) {
            Direction.UP -> playerY - 1
            Direction.DOWN -> playerY + 1
            else -> playerY
        }

        if (nextX in maze[0].indices && nextY in maze.indices) {
            if (maze[nextY][nextX].isWall) {
                resetPlayer()
                listener?.onWallCollision()
            } else {
                playerX = nextX
                playerY = nextY
                if (maze[playerY][playerX].isFinish) {
                    listener?.onMazeComplete()
                }
                invalidate()
            }
        }
    }

    fun resetPlayer() {
        playerX = startX
        playerY = startY
        invalidate()
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Mengimplementasikan event listener untuk sensor cahaya
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
            lightLevel = event.values[0] // Menyimpan nilai sensor cahaya
            invalidate() // Redraw tampilan berdasarkan nilai cahaya
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak digunakan dalam kasus ini
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager?.unregisterListener(this)
    }

    private fun generateMaze(level: Int): Array<Array<Cell>> {
        val size = 8 // Fixed size for all levels based on the image
        val maze = Array(size) { Array(size) { Cell() } }

        // Initialize all cells as walls
        for (y in maze.indices) {
            for (x in maze[y].indices) {
                maze[y][x].isWall = true
            }
        }

        // Define the specific paths and walls for each level
        val pathCoordinates = when (level) {
            1 -> listOf(
                0 to 0, 0 to 1, 1 to 1, 1 to 2, 2 to 2, 2 to 3, 3 to 3, 3 to 4, 4 to 4, 4 to 5,
                5 to 5, 5 to 6, 6 to 6, 6 to 7, 7 to 7
            )
            2 -> listOf(
                0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5, 0 to 6, 0 to 7, 1 to 6, 2 to 0,
                2 to 1, 2 to 2, 2 to 3, 2 to 4, 2 to 5, 2 to 6, 2 to 7, 3 to 1, 4 to 0, 4 to 1,
                4 to 2, 4 to 3, 4 to 4, 4 to 5, 4 to 6, 4 to 7, 5 to 4, 6 to 0, 6 to 1, 6 to 2,
                6 to 3, 6 to 4, 6 to 5, 6 to 6, 7 to 6, 7 to 7
            )
            3 -> listOf(
                0 to 0, 0 to 1, 0 to 3, 0 to 4, 0 to 5, 1 to 1, 1 to 3, 1 to 5, 1 to 6, 1 to 7,
                2 to 1, 2 to 2, 2 to 3, 2 to 5, 2 to 7, 3 to 5, 3 to 7, 4 to 1, 4 to 2, 4 to 3,
                4 to 4, 4 to 5, 5 to 1, 5 to 3, 5 to 5, 5 to 6, 5 to 7, 6 to 1, 6 to 3, 7 to 3,
                7 to 4, 7 to 5, 7 to 6, 7 to 7
            )
            4 -> listOf(
                0 to 0, 0 to 1, 0 to 4, 0 to 5, 0 to 6, 0 to 7, 1 to 1, 1 to 2, 1 to 4, 1 to 7,
                2 to 2, 2 to 3, 2 to 4, 2 to 6, 2 to 7, 3 to 1, 3 to 5, 3 to 6, 4 to 0, 4 to 1,
                4 to 2, 4 to 3, 4 to 4, 4 to 5, 5 to 0, 5 to 3, 5 to 7, 6 to 0, 6 to 2, 6 to 3,
                6 to 5, 6 to 6, 6 to 7, 7 to 0, 7 to 3, 7 to 4, 7 to 5, 7 to 7
            )
            5 -> listOf(
                0 to 0, 0 to 2, 0 to 3, 0 to 4, 0 to 5, 1 to 0, 1 to 1, 1 to 2, 1 to 4, 1 to 7,
                2 to 4, 2 to 5, 2 to 6, 2 to 7, 3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 6, 4 to 0,
                4 to 3, 4 to 4, 4 to 5, 4 to 6, 5 to 0, 5 to 1, 5 to 2, 6 to 2, 6 to 4, 6 to 5,
                6 to 6, 7 to 0, 7 to 1, 7 to 2, 7 to 3, 7 to 4, 7 to 6, 7 to 7
            )
            6 -> listOf(
                0 to 0, 0 to 1, 0 to 2, 0 to 4, 1 to 2, 1 to 4, 2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4, 2 to 5,
                2 to 6, 3 to 0, 3 to 6, 4 to 0, 4 to 2, 4 to 6, 5 to 2, 5 to 3, 5 to 4, 5 to 6,
                6 to 0, 6 to 1, 6 to 6, 7 to 1, 7 to 2, 7 to 3, 7 to 4, 7 to 5, 7 to 6, 7 to 7
            )

            7 -> listOf(
                0 to 0, 0 to 1, 0 to 3, 0 to 4, 1 to 0, 1 to 1, 1 to 3, 1 to 5, 1 to 6, 1 to 7,
                2 to 1, 2 to 3, 3 to 1, 3 to 3, 3 to 4, 3 to 5, 3 to 7, 4 to 0, 4 to 1, 4 to 2, 4 to 3, 5 to 0,
                5 to 3, 5 to 4, 5 to 5, 5 to 6, 6 to 0, 6 to 6, 6 to 7, 7 to 0, 7 to 2, 7 to 3,
                7 to 4, 7 to 7
            )

            8 -> listOf(
                0 to 0, 0 to 1, 0 to 3, 1 to 1, 1 to 3, 1 to 5, 1 to 6, 1 to 7, 2 to 0, 2 to 1,
                2 to 3, 2 to 5, 3 to 1, 3 to 2, 3 to 3, 3 to 4, 3 to 5, 3 to 6, 4 to 0, 4 to 1, 4 to 5, 4 to 6,
                5 to 0, 5 to 3, 5 to 6, 6 to 0, 6 to 1, 6 to 2, 6 to 3, 6 to 6, 6 to 7, 7 to 3,
                7 to 7
            )

            9 -> listOf(
                0 to 0, 0 to 2, 0 to 3, 1 to 0, 1 to 1, 1 to 2, 1 to 3, 1 to 5, 1 to 6, 2 to 3,
                2 to 4, 2 to 5, 3 to 0, 3 to 3, 3 to 6, 3 to 7, 4 to 0, 4 to 1, 4 to 2, 4 to 3,
                4 to 6, 5 to 3, 5 to 4, 5 to 6, 5 to 7, 6 to 1, 6 to 3, 6 to 4, 7 to 0, 7 to 3, 7 to 4,
                7 to 5, 7 to 6, 7 to 7
            )

            10 -> listOf(
                0 to 0, 0 to 1, 0 to 3, 0 to 4, 1 to 0, 1 to 1, 1 to 4, 1 to 5, 1 to 7, 2 to 1,
                2 to 2, 2 to 4, 3 to 0, 3 to 1, 3 to 2, 3 to 4, 3 to 5, 3 to 7, 4 to 1, 4 to 4,
                4 to 7, 5 to 0, 5 to 1, 5 to 2, 5 to 3, 5 to 4, 5 to 7, 6 to 0, 6 to 1, 6 to 3, 6 to 7,
                7 to 0, 7 to 3, 7 to 4, 7 to 5, 7 to 6, 7 to 7
            )

            // Additional levels
            else -> emptyList()
        }

        // Tandai path sebagai bukan dinding
        pathCoordinates.forEach { (y, x) ->
            maze[y][x].isWall = false
        }

        // Tentukan titik start dan finish
        startX = 0
        startY = 0
        maze[startY][startX].isStart = true

        val finishX = maze.size - 1
        val finishY = maze.size - 1
        maze[finishY][finishX].isFinish = true

        return maze
    }

    data class Cell(
        var isWall: Boolean = true,
        var isStart: Boolean = false,
        var isFinish: Boolean = false
    )
}
