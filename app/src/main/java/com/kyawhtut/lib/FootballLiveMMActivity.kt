package com.kyawhtut.lib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kyawhtut.lib.FootballLiveMMSportLinkDialog.Companion.showFootballLiveSportLink
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.sport.livefootball.FootballLiveMM
import com.kyawhtut.sport.livefootball.`object`.FootballLiveModel
import kotlinx.android.synthetic.main.activity_football_live_mm.*
import kotlinx.android.synthetic.main.item_football_live_mm.view.*
import kotlinx.coroutines.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class FootballLiveMMActivity : AppCompatActivity(R.layout.activity_football_live_mm) {

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
            layoutManager = LinearLayoutManager(
                this@FootballLiveMMActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
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
                    R.id.action_live -> FootballLiveMM.getLive()
                    else -> FootballLiveMM.getHighlight()
                }
                withContext(main) {
                    sportAdapter.list = sport
                    view_loading.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(main) {
                    Toast.makeText(
                        this@FootballLiveMMActivity,
                        e.localizedMessage ?: "Unknown error found.",
                        Toast.LENGTH_LONG
                    ).show()
                    view_loading.visibility = View.GONE
                }
            }
        }
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var list: List<FootballLiveModel> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(
                    this@FootballLiveMMActivity
                ).inflate(
                    R.layout.item_football_live_mm,
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

        fun bind(data: FootballLiveModel) {
            itemView.setOnClickListener {
                if (index == R.id.action_highlight) {
//                    showSportLink(
//                        data.title,
//                        data.url.filter { !it.contains("api/ip/check", true) }
//                            .mapIndexed { index, s ->
//                                "Link - ${index + 1}" to s
//                            }
//                    ) {
//                        startActivity(
//                            Intent(
//                                this@FootballLiveMMActivity,
//                                PlayerActivity::class.java
//                            ).apply {
//                                putExtra(
//                                    PlayerActivity.extraPlayerObject,
//                                    PlayerObject(data.title, it)
//                                )
//                            }
//                        )
//                    }
                } else {
                    view_loading.visibility = View.VISIBLE
                    io.launch {
                        try {
                            val url = FootballLiveMM.parseLiveURL(data)
                            withContext(main) {
                                view_loading.visibility = View.GONE
                                showFootballLiveSportLink(data, url.map {
                                    it.title to it.link
                                }) {
                                    startActivity(
                                        Intent(
                                            this@FootballLiveMMActivity,
                                            PlayerActivity::class.java
                                        ).apply {
                                            putExtra(
                                                PlayerActivity.extraPlayerObject,
                                                PlayerObject(data.matchType, it)
                                            )
                                        }
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(main) {
                                Toast.makeText(
                                    this@FootballLiveMMActivity,
                                    e.localizedMessage ?: "Unknown error found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                view_loading.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            Glide.with(itemView.iv_home_team.context)
                .load(data.homeTeamImage)
                .into(itemView.iv_home_team)
            Glide.with(itemView.iv_away_team.context)
                .load(data.awayTeamImage)
                .into(itemView.iv_away_team)
            itemView.tv_home_team.text = data.homeTeam
            itemView.tv_away_team.text = data.awayTeam
            itemView.tv_match_type.text = data.matchType
            itemView.tv_time.text = "%s,%s".format(data.date, data.time)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
