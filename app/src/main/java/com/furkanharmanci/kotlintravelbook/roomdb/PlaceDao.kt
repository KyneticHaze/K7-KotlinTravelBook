package com.furkanharmanci.kotlintravelbook.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.furkanharmanci.kotlintravelbook.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface PlaceDao {
    @Query("SELECT * FROM place")
    fun getAll() : Flowable<List<Place>>

    @Insert
    fun insert(place: Place) : Completable

    @Delete
    fun delete(place: Place) : Completable
}