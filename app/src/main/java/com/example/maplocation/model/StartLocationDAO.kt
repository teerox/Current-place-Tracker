package com.example.maplocation.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StartLocationDAO {
    @Query("SELECT * FROM startTable ORDER BY uid DESC")
    fun getAllstartlonglat():List<StartLocation>
    @Insert
    fun insertAllstart(vararg startlocation:StartLocation)
}