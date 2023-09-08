package com.furkanharmanci.kotlintravelbook.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.furkanharmanci.kotlintravelbook.R
import com.furkanharmanci.kotlintravelbook.adapter.PlaceAdapter
import com.furkanharmanci.kotlintravelbook.databinding.ActivityMainBinding
import com.furkanharmanci.kotlintravelbook.model.Place
import com.furkanharmanci.kotlintravelbook.roomdb.PlaceDao
import com.furkanharmanci.kotlintravelbook.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var compositeDisposable = CompositeDisposable()
    private lateinit var database : PlaceDatabase
    private lateinit var placeDao : PlaceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java,"Places").build()
        placeDao = database.placeDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(placeList : List<Place>) {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        val adapter = PlaceAdapter(placeList)
        binding.recycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.travel_item,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_travel) {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("info","newPosition")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}