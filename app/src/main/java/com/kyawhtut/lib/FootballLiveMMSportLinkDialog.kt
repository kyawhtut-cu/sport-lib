package com.kyawhtut.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kyawhtut.sport.livefootball.`object`.FootballLiveModel
import kotlinx.android.synthetic.main.dialog_football_live_mm.*
import kotlinx.android.synthetic.main.dialog_vola_sport.rv_link
import kotlinx.android.synthetic.main.item_link.view.*

/**
 * @author kyawhtut
 * @date 30/10/2020
 */
class FootballLiveMMSportLinkDialog(
    private val data: FootballLiveModel,
    private val link: List<Pair<String, String>>,
    private val callback: (String) -> Unit
) :
    BottomSheetDialogFragment() {

    companion object {
        fun FragmentActivity.showFootballLiveSportLink(
            data: FootballLiveModel,
            link: List<Pair<String, String>>,
            callback: (String) -> Unit
        ) {
            FootballLiveMMSportLinkDialog(data, link, callback).show(
                this.supportFragmentManager,
                FootballLiveMMSportLinkDialog::class.java.name
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_football_live_mm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_match_type.text = data.matchType
        tv_home_team.text = data.homeTeam
        tv_away_team.text = data.awayTeam
        tv_time.text = "%s,%s".format(data.date, data.time)
        Glide.with(requireContext())
            .load(data.homeTeamImage)
            .into(iv_home_team)
        Glide.with(requireContext())
            .load(data.awayTeamImage)
            .into(iv_away_team)
        rv_link.apply {
            adapter = Adapter(link)
            layoutManager = LinearLayoutManager(
                this@FootballLiveMMSportLinkDialog.requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    private inner class Adapter(private val link: List<Pair<String, String>>) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_link, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(link[position])
        }

        override fun getItemCount(): Int {
            return this.link.size
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: Pair<String, String>) {
            itemView.tv_link_title.text = data.first
            itemView.setOnClickListener {
                callback(data.second)
                dismiss()
            }
        }
    }
}
