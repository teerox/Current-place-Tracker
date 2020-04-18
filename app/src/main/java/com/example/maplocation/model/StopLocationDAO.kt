package com.example.maplocation.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StopLocationDAO {
    @Query("SELECT * FROM stopTable ORDER BY uid DESC")
    fun getAllstoplonglat():List<StopLocation>

    @Insert
    fun insertAllstop(vararg stoplocation:StopLocation)
}