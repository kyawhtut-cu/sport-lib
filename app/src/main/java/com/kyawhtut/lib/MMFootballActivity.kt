package com.kyawhtut.lib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyawhtut.lib.VolaSportLinkDialog.Companion.showSportLink
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.sport.mmfootball.MMFootball
import com.kyawhtut.sport.mmfootball.`object`.MMFootballModel
import kotlinx.android.synthetic.main.activity_mmfootball.*
import kotlinx.android.synthetic.main.item_mm_football.view.*
import kotlinx.coroutines.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class MMFootballActivity : AppCompatActivity(R.layout.activity_mmfootball) {

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
        Adapter()
    }

    private var index: Int = R.id.action_live

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rv_sport.apply {
            layoutManager = GridLayoutManager(this@MMFootballActivity, 3)
            adapter = sportAdapter
        }

        getSportData()

        btn_menu.setOnNavigationItemSelectedListener {
            index = it.itemId
            getSportData()
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun getSportData() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            try {
                val sport = when (index) {
                    R.id.action_live -> MMFootball.getLive()
                    R.id.action_popular -> MMFootball.getPopular()
                    else -> MMFootball.getHighlight()
                }
                withContext(main) {
                    sportAdapter.list = sport
                    view_loading.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(main) {
                    Toast.makeText(
                        this@MMFootballActivity,
                        e.localizedMessage ?: "Unknown error found.",
                        Toast.LENGTH_LONG
                    ).show()
                    view_loading.visibility = View.GONE
                }
            }
        }
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var list: List<MMFootballModel> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(
                    this@MMFootballActivity
                ).inflate(
                    R.layout.item_mm_football,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: MMFootballModel) {
            itemView.setOnClickListener {
                if (index == R.id.action_highlight) {
                    showSportLink(
                        data.title,
                        data.url.filter { !it.contains("api/ip/check", true) }
                            .mapIndexed { index, s ->
                                "Link - ${index + 1}" to s
                            }
                    ) {
                        startActivity(
                            Intent(
                                this@MMFootballActivity,
                                PlayerActivity::class.java
                            ).apply {
                                putExtra(
                                    PlayerActivity.extraPlayerObject,
                                    PlayerObject(data.title, it)
                                )
                            }
                        )
                    }
                } else {
                    view_loading.visibility = View.VISIBLE
                    io.launch {
                        try {
                            val url = MMFootball.parseLiveURL(data)
                            withContext(main) {
                                view_loading.visibility = View.GONE
                                startActivity(
                                    Intent(
                                        this@MMFootballActivity,
                                        PlayerActivity::class.java
                                    ).apply {
                                        putExtra(
                                            PlayerActivity.extraPlayerObject,
                                            PlayerObject(data.title, url)
                                        )
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(main) {
                                Toast.makeText(
                                    this@MMFootballActivity,
                                    e.localizedMessage ?: "Unknown error found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                view_loading.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            Glide.with(itemView.iv_category_image.context)
                .load(data.image)
                .into(itemView.iv_category_image)
            itemView.tv_category_name.text = data.title
            itemView.tv_quality.text = data.quality
            itemView.tv_time.text = data.time ?: ""
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
