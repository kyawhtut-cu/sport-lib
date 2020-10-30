package com.kyawhtut.lib.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kyawhtut.lib.R
import com.kyawhtut.sport.sport5.`object`.Sport5Model
import com.kyawhtut.sport.sport5.`object`.Sport5Type
import kotlinx.android.synthetic.main.item_sport.view.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class SportAdapter(private val onClick: (Sport5Model) -> Unit) :
    RecyclerView.Adapter<SportAdapter.SportViewHolder>() {

    private val itemList: MutableList<Sport5Model> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportViewHolder {
        return SportViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sport, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SportViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun addAll(data: List<Sport5Model>) {
        itemList.clear()
        itemList.addAll(0, data)
        notifyDataSetChanged()
    }

    fun addAll(index: Int, data: List<Sport5Model>) {
        itemList.addAll(index, data)
        notifyItemRangeInserted(index, data.size)
    }

    fun add(data: Sport5Model) {
        itemList.add(data)
        notifyItemInserted(itemList.size)
    }

    inner class SportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(sport5Model: Sport5Model) {
            itemView.setOnClickListener {
                onClick(sport5Model)
            }
            itemView.iv_sport_type.setImageResource(
                when (sport5Model.sport5Type) {
                    Sport5Type.FOOTBALL -> R.drawable.ic_football
                    Sport5Type.BASKETBALL -> R.drawable.ic_basketball
                    Sport5Type.TENNIS -> R.drawable.ic_tennis
                    Sport5Type.ESPORTS -> R.drawable.ic_esport
                }
            )
            itemView.tv_match_title.text = sport5Model.matchTitle
            itemView.tv_home_team.text = sport5Model.homeTeam
            itemView.tv_away_team.text = sport5Model.awayTeam
            itemView.iv_live.visibility = if (sport5Model.isLive) View.VISIBLE else View.INVISIBLE
            itemView.tv_score.text =
                if (sport5Model.homeScore.isEmpty() || sport5Model.awayScore.isEmpty()) "-"
                else "%s\n%s".format(
                    sport5Model.homeScore,
                    sport5Model.awayScore
                )
        }
    }
}
