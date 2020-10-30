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
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.sport.fefatv.FeFaSport
import com.kyawhtut.sport.fefatv.`object`.FeFaModel
import com.kyawhtut.sport.volasport.VolaSport
import kotlinx.android.synthetic.main.activity_fefatv.*
import kotlinx.android.synthetic.main.item_fefa_tv_category.view.*
import kotlinx.coroutines.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class FeFaTvCategoryActivity : AppCompatActivity(R.layout.activity_fefatv) {

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

    private var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rv_sport.apply {
            layoutManager = GridLayoutManager(this@FeFaTvCategoryActivity, 2)
            adapter = sportAdapter
        }

        getSportData(true)

        btn_menu.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_live -> {
                    index = 0
                    getSportData(true)
                }
                R.id.action_highlight -> {
                    index = 1
                    getSportData(false)
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun getSportData(isLive: Boolean) {
        view_loading.visibility = View.VISIBLE
        io.launch {
            try {
                val vola = VolaSport.getVolaSport()
                val categoryList =
                    if (isLive) FeFaSport.getLiveSport(1) else FeFaSport.getHighlightSport(1)
                withContext(main) {
                    sportAdapter.list = categoryList
                    view_loading.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(main) {
                    Toast.makeText(
                        this@FeFaTvCategoryActivity,
                        e.localizedMessage ?: "Unknown error found.",
                        Toast.LENGTH_LONG
                    ).show()
                    view_loading.visibility = View.GONE
                }
            }
        }
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var list: List<FeFaModel> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(
                    this@FeFaTvCategoryActivity
                ).inflate(
                    R.layout.item_fefa_tv_category,
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

        fun bind(data: FeFaModel) {
            itemView.setOnClickListener {
                if (index == 1)
                    startActivity(
                        Intent(
                            this@FeFaTvCategoryActivity,
                            PlayerActivity::class.java
                        ).apply {
                            putExtra(
                                PlayerActivity.extraPlayerObject,
                                PlayerObject(data.channelTitle, data.channelPlayURL)
                            )
                        }
                    )
                else {
                    view_loading.visibility = View.VISIBLE
                    io.launch {
                        try {
                            val url = FeFaSport.parseLiveURL(data.channelPlayURL)
                            withContext(main) {
                                view_loading.visibility = View.GONE
                                startActivity(
                                    Intent(
                                        this@FeFaTvCategoryActivity,
                                        PlayerActivity::class.java
                                    ).apply {
                                        putExtra(
                                            PlayerActivity.extraPlayerObject,
                                            PlayerObject(data.channelTitle, url)
                                        )
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(main) {
                                Toast.makeText(
                                    this@FeFaTvCategoryActivity,
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
                .load(data.channelThumbnail)
                .into(itemView.iv_category_image)
            itemView.tv_category_name.text = data.channelTitle
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
