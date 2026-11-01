
# 🧩 Maze Journey

<p align="center">
  **An Android Maze Puzzle Game**
  <br />
  Navigate through challenging mazes, beat the clock, and explore sensor-based gameplay.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Gradle-8.3.2-02303A?logo=gradle&logoColor=white" alt="Gradle" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue" alt="Minimum SDK" />
</p>

## 🎮 Overview

**Maze Journey** is an Android-based maze puzzle game developed using Kotlin. It offers an interactive gaming experience where players navigate through challenging mazes, solve navigation puzzles, and race against a countdown timer.

Designed to combine entertainment with cognitive challenges, the game features progressive maze levels, motion-based controls, and ambient light sensor integration. Players can navigate using on-screen directional buttons or device motion, while selected levels introduce a dark maze environment that responds to surrounding light conditions.

## ✨ Key Features

| Feature | Description |
|---|---|
| 🧩 **Progressive Maze Challenges** | Explore maze levels with increasing challenges, a 60-second countdown timer, and a score system that rewards faster completion. |
| 📱 **Motion-Based Controls** | Navigate the player using directional buttons or device motion through gyroscope and accelerometer integration. |
| 💡 **Light Sensor Integration** | Experience special dark maze challenges where visibility dynamically responds to ambient light conditions. |

### 🕹️ Gameplay Highlights

- **Level Progression:** Unlock subsequent levels by successfully completing previous challenges.
- **Time-Based Scoring:** Earn points based on the remaining time, with 10 points awarded for every second left.
- **Collision Feedback:** Receive vibration feedback when hitting maze walls, with the player returning to the starting position.
- **Interactive Navigation:** Choose between touchscreen directional controls and motion-based movement.
- **Dynamic Visibility:** Explore limited-visibility mazes in levels 6–10, where nearby areas become visible based on the light sensor readings.

## 🛠️ Tech Stack

### Languages & Platform
- **Kotlin** – Primary programming language.
- **Android SDK** – Native Android application development.

### Android Libraries & Components
- AndroidX AppCompat
- AndroidX Core KTX
- Material Components
- ConstraintLayout
- CardView

### Tools & Build System
- Android Studio
- Gradle (Kotlin DSL)
- Android Emulator / Physical Android Device

## 📱 Platform Requirements

- **Platform:** Android
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)
- **Sensors:** Gyroscope and Light Sensor support; accelerometer is used as a movement fallback when the gyroscope is unavailable.

## 🚀 Future Improvements

- Add more maze layouts and difficulty variations.
- Introduce additional gameplay modes and challenges.
- Improve level progression and player statistics.
- Enhance the user interface and gameplay experience.

---

<p align="center">
  **Maze Journey**
  <br />
  <i>Navigate the maze. Challenge your mind. Beat the clock.</i>
  <br /><br />
  Developed with ❤️ using Kotlin.
</p>
