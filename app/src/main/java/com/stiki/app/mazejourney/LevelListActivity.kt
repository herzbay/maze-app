package com.stiki.app.mazejourney

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LevelListActivity : AppCompatActivity() {

    private lateinit var levelListView: ListView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var greetingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_list)

        sharedPreferences = getSharedPreferences("MazeJourneyPrefs", MODE_PRIVATE)

        val username = intent.getStringExtra("USERNAME") ?: "Player"
        greetingTextView = findViewById(R.id.greetingTextView)
        greetingTextView.text = "Halo, $username!"

        val levels = (1..25).map { "Level $it" }.toTypedArray()
        val unlockedLevel = sharedPreferences.getInt("UNLOCKED_LEVEL", 1)

        levelListView = findViewById(R.id.levelListView)
        val adapter = CustomAdapter(levels, unlockedLevel)
        levelListView.adapter = adapter

        levelListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (isLevelUnlocked(position + 1)) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("LEVEL", position + 1)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Selesaikan level sebelumnya dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLevelUnlocked(level: Int): Boolean {
        val unlockedLevel = sharedPreferences.getInt("UNLOCKED_LEVEL", 1)
        return level <= unlockedLevel
    }

    inner class CustomAdapter(private val levels: Array<String>, private val unlockedLevel: Int) : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, levels) {
        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            if (position + 1 > unlockedLevel) {
                textView.text = "${levels[position]} - Terkunci"
            }
            return view
        }
    }
}