package com.example.maplocation.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "startTable")
data class StartLocation(
        @ColumnInfo(name = "longitude")
        var longitude:String,
        @ColumnInfo(name = "latitude")
        var latitude:String
):Parcelable

{
    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)var uid:Int = 0
}