package com.example.maplocation.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DistanceDAO {
    @Query("SELECT * FROM distanceTable ORDER BY uid DESC")
    fun getDistance():List<Distance>
    @Insert
    fun insertDistance(vararg distance:Distance)
}