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
import com.kyawhtut.lib.VolaSportLinkDialog.Companion.showSportLink
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.sport.volasport.VolaSport
import com.kyawhtut.sport.volasport.`object`.VolaSportModel
import com.kyawhtut.sport.volasport.`object`.VolaSportType
import kotlinx.android.synthetic.main.activity_vola.*
import kotlinx.android.synthetic.main.item_vola.view.*
import kotlinx.android.synthetic.main.item_vola_header.view.*
import kotlinx.coroutines.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class VolaSportActivity : AppCompatActivity(R.layout.activity_vola) {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rv_sport.apply {
            layoutManager =
                LinearLayoutManager(this@VolaSportActivity, LinearLayoutManager.VERTICAL, false)
            adapter = sportAdapter
        }

        getSportData()
    }

    private fun getSportData() {
        view_loading.visibility = View.VISIBLE
        io.launch {
            try {
                val vola = VolaSport.getVolaSport()
                withContext(main) {
                    sportAdapter.list = vola.map {
                        AdapterData(it)
                    }.toMutableList().apply {
                        with(this.indexOfFirst { it.data !is String && (it.data as VolaSportModel).type == VolaSportType.LIVE }) {
                            if (this != -1)
                                add(
                                    this,
                                    AdapterData("Live")
                                )
                        }

                        with(this.indexOfFirst { it.data !is String && (it.data as VolaSportModel).type == VolaSportType.HIGHLIGHT }) {
                            if (this != -1) {
                                add(
                                    this,
                                    AdapterData("Highlight")
                                )
                            }
                        }
                        with(this.indexOfFirst { it.data !is String && (it.data as VolaSportModel).type == VolaSportType.NEWS }) {
                            if (this != -1) {
                                add(
                                    this,
                                    AdapterData("News")
                                )
                            }
                        }
                    }
                    view_loading.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(main) {
                    Toast.makeText(
                        this@VolaSportActivity,
                        e.localizedMessage ?: "Unknown error found.",
                        Toast.LENGTH_LONG
                    ).show()
                    view_loading.visibility = View.GONE
                }
            }
        }
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var list: List<AdapterData> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        private val header: Int = 0
        private val content: Int = 1

        override fun getItemViewType(position: Int): Int {
            return if (list[position].data is String) header else content
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(
                    this@VolaSportActivity
                ).inflate(
                    if (viewType == header) R.layout.item_vola_header else R.layout.item_vola,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (list[position].data is String) holder.bind(list[position].data as String)
            else holder.bind(list[position].data as VolaSportModel)
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(title: String) {
            itemView.tv_header.text = title
        }

        fun bind(data: VolaSportModel) {
            itemView.setOnClickListener {
                if (data.type == VolaSportType.HIGHLIGHT) {
                    view_loading.visibility = View.VISIBLE
                    io.launch {
                        try {
                            val url = VolaSport.parsePlayURL(data.url)
                            withContext(main) {
                                view_loading.visibility = View.GONE
                                startActivity(
                                    Intent(
                                        this@VolaSportActivity,
                                        PlayerActivity::class.java
                                    ).apply {
                                        putExtra(
                                            PlayerActivity.extraPlayerObject,
                                            PlayerObject(data.title, url)
                                        )
                                    })
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(main) {
                                Toast.makeText(
                                    this@VolaSportActivity,
                                    e.localizedMessage ?: "Unknown error found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                view_loading.visibility = View.GONE
                            }
                        }
                    }
                } else if (data.type == VolaSportType.LIVE && data.isLive) {
                    view_loading.visibility = View.VISIBLE
                    io.launch {
                        try {
                            val link = VolaSport.getLivePlayLink(data.url)
                            withContext(main) {
                                showSportLink(data.title, link) {
                                    io.launch {
                                        val url = VolaSport.parsePlayURL(it)
                                        withContext(main) {
                                            view_loading.visibility = View.GONE
                                            startActivity(
                                                Intent(
                                                    this@VolaSportActivity,
                                                    PlayerActivity::class.java
                                                ).apply {
                                                    putExtra(
                                                        PlayerActivity.extraPlayerObject,
                                                        PlayerObject(
                                                            data.title,
                                                            url
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(main) {
                                Toast.makeText(
                                    this@VolaSportActivity,
                                    e.localizedMessage ?: "Unknown error found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                view_loading.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@VolaSportActivity,
                        "Sport is not start.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            Glide.with(itemView.iv_category_image.context)
                .load(data.image)
                .into(itemView.iv_category_image)
            itemView.tv_category_name.text = data.title
            itemView.iv_isLive.visibility = if (data.isLive) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    data class AdapterData(
        val data: Any
    )
}
