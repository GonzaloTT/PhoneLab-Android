package com.example.phonelab

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReadDao {

    @Insert
    suspend fun insert(
        read: ReadAccelerometer
    ): Long

    @Query(
        """
        SELECT *
        FROM accelerometer_reads
        ORDER BY timestamp DESC
        LIMIT 200
        """
    )
    suspend fun getAll():
            List<ReadAccelerometer>

    @Query(
        """
        SELECT COUNT(*)
        FROM accelerometer_reads
        """
    )
    suspend fun countAll(): Int

    @Query(
        """
        DELETE FROM accelerometer_reads
        """
    )
    suspend fun deleteAll(): Int

    @Query(
        """
        SELECT *
        FROM accelerometer_reads
        WHERE sincronizada = 0
        ORDER BY id ASC
        LIMIT :limit
        """
    )
    suspend fun getPending(
        limit: Int
    ): List<ReadAccelerometer>

    @Query(
        """
        SELECT COUNT(*)
        FROM accelerometer_reads
        WHERE sincronizada = 0
        """
    )
    suspend fun countPending(): Int

    @Query(
        """
        UPDATE accelerometer_reads
        SET sincronizada = 1
        WHERE id IN (:ids)
        """
    )
    suspend fun markAsSynced(
        ids: List<Long>
    ): Int
}