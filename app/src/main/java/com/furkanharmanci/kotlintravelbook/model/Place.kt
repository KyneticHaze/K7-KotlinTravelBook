package com.furkanharmanci.kotlintravelbook.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "place")
class Place (
    @ColumnInfo(name = "name") var name : String,
    @ColumnInfo(name = "latitude") var latitude : Double,
    @ColumnInfo(name = "longitude") var longitude : Double
    ) : Serializable {
    @PrimaryKey(autoGenerate = true) // autoGenerate = kendin oluştur
    var id = 0
}