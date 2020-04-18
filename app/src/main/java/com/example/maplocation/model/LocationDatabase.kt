package com.example.maplocation.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StartLocation::class, StopLocation::class, Distance::class], version = 1,exportSchema = false)
abstract class LocationDatabase:RoomDatabase(){
    abstract fun StartLocationDao():StartLocationDAO
    abstract fun StopLocationDao():StopLocationDAO
    abstract fun DistanceDao():DistanceDAO

    companion object {
        private var instance: LocationDatabase? = null
        fun getInstance(context: Context): LocationDatabase? {

            if (instance == null) {
                instance = Room.databaseBuilder(context, LocationDatabase::class.java, "location-db")
                        .build()
            }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }
    }
}