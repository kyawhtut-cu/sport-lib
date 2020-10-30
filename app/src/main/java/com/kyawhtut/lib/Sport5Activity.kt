package com.kyawhtut.lib

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.lib.adapter.SportAdapter
import com.kyawhtut.sport.sport5.Sport5
import kotlinx.android.synthetic.main.activity_sport5.*
import kotlinx.android.synthetic.main.live_switch.view.*
import kotlinx.coroutines.*

class Sport5Activity : AppCompatActivity() {

    private var isIncludeUnLive = false

    private val job by lazy {
        Job()
    }
    private val io by lazy {
        CoroutineScope(job + Dispatchers.IO)
    }
    private val main by lazy {
        job + Dispatchers.Main
    }

    private val sportAdapter by lazy {
        SportAdapter {
            view_loading.visibility = View.VISIBLE
            io.launch {
                try {
                    val response = Sport5.parseURL(it)
                    withContext(main) {
                        view_loading.visibility = View.GONE
                        startActivity(
                            Intent(
                                this@Sport5Activity,
                                PlayerActivity::class.java
                            ).apply {
                                putExtra(
                                    PlayerActivity.extraPlayerObject, PlayerObject(
                                        response.matchTitle,
                                        response.playURL ?: ""
                                    )
                                )
                            })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(main) {
                        Toast.makeText(
                            this@Sport5Activity,
                            e.localizedMessage ?: "Unknown error found",
                            Toast.LENGTH_LONG
                        ).show()
                        view_loading.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport5)

        btn_menu.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_all -> getData()
                R.id.action_football -> getFootball()
                R.id.action_basketball -> getBasketball()
                R.id.action_tennis -> getTennis()
                R.id.action_esport -> getESport()
            }
            return@setOnNavigationItemSelectedListener true
        }

        rv_sport.apply {
            adapter = sportAdapter
            layoutManager =
                LinearLayoutManager(this@Sport5Activity, LinearLayoutManager.VERTICAL, false)
        }

        getData()
    }

    private fun getData() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            val result = Sport5.getAll(isIncludeUnLive)
            withContext(main) {
                sportAdapter.addAll(result)
                view_loading.visibility = View.GONE
            }
        }
    }

    private fun getFootball() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            val result = Sport5.getFootball(isIncludeUnLive)
            withContext(main) {
                sportAdapter.addAll(result)
                view_loading.visibility = View.GONE
            }
        }
    }

    private fun getBasketball() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            val result = Sport5.getBasketball(isIncludeUnLive)
            withContext(main) {
                sportAdapter.addAll(result)
                view_loading.visibility = View.GONE
            }
        }
    }

    private fun getTennis() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            val result = Sport5.getTennis(isIncludeUnLive)
            withContext(main) {
                sportAdapter.addAll(result)
                view_loading.visibility = View.GONE
            }
        }
    }

    private fun getESport() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            val result = Sport5.getESport(isIncludeUnLive)
            withContext(main) {
                sportAdapter.addAll(result)
                view_loading.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)

        val switch = menu!!.findItem(R.id.app_bar_switch).actionView.live_switch
        switch.setOnCheckedChangeListener { _, isCheck ->
            isIncludeUnLive = isCheck
            getData()
        }
        return true
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
