package com.example.phonelab

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ReadAccelerometer::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun readDao(): ReadDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de la versión 1 a la versión 2.
        // Agrega la columna "sincronizada" sin borrar las lecturas existentes.
        private val MIGRATION_1_2 =
            object : Migration(1, 2) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {
                    database.execSQL(
                        """
                        ALTER TABLE accelerometer_reads
                        ADD COLUMN sincronizada INTEGER NOT NULL DEFAULT 0
                        """.trimIndent()
                    )
                }
            }

        fun getInstance(
            context: Context
        ): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "phonelab_database"
                    )
                        .addMigrations(
                            MIGRATION_1_2
                        )
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}