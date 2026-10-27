package com.example.phonelab

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "accelerometer_reads"
)
data class ReadAccelerometer(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val x: Float,

    val y: Float,

    val z: Float,

    val magnitude: Float,

    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "0")
    val sincronizada: Boolean = false
)