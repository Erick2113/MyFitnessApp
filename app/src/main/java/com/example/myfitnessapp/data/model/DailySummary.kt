package com.example.myfitnessapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey
    val dateKey: String,
    val dayLabel: String,
    val steps: Int,
    val distanceKm: Float,
    val calories: Float,
    val activeSeconds: Long
)
