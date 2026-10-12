package com.example.myfitnessapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String,
    val durationSeconds: Long,
    val steps: Int,
    val distanceKm: Float,
    val calories: Float,
    val avgSpeedKmh: Float,
    val avgImpact: Float
)
