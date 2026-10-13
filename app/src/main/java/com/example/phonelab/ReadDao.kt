package com.example.phonelab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReadDao {

    @Insert
    suspend fun insert(read: ReadAccelerometer)

    @Query("SELECT * FROM accelerometer_reads ORDER BY id DESC")
    suspend fun getAll(): List<ReadAccelerometer>

    @Query("DELETE FROM accelerometer_reads")
    suspend fun deleteAll()
}