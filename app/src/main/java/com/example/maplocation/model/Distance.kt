package com.example.maplocation.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "distanceTable")
data class Distance(
        @ColumnInfo(name = "distance")
        var distance:String
): Parcelable

{
    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)var uid:Int = 0
}