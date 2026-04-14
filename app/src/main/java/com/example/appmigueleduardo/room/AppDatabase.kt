package com.example.appmigueleduardo.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CoordinatesEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coordinatesDao(): ICoordinatesDao // Acceso al DAO [cite: 29]

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la devuelve; si no, la crea [cite: 33]
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coordinates_database" // Nombre del archivo de la DB [cite: 34]
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}