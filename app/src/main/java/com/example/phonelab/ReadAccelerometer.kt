package com.example.phonelab

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accelerometer_reads")
data class ReadAccelerometer(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val x: Float,

    val y: Float,

    val z: Float,

    val magnitude: Float,

    val timestamp: Long = System.currentTimeMillis()
)