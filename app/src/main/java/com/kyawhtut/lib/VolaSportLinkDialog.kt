package com.kyawhtut.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_vola_sport.*
import kotlinx.android.synthetic.main.item_link.view.*

/**
 * @author kyawhtut
 * @date 30/10/2020
 */
class VolaSportLinkDialog(
    private val title: String,
    private val link: List<Pair<String, String>>,
    private val callback: (String) -> Unit
) :
    BottomSheetDialogFragment() {

    companion object {
        fun FragmentActivity.showSportLink(
            title: String,
            link: List<Pair<String, String>>,
            callback: (String) -> Unit
        ) {
            VolaSportLinkDialog(title, link, callback).show(
                this.supportFragmentManager,
                VolaSportLinkDialog::class.java.name
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_vola_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title.text = title
        rv_link.apply {
            adapter = Adapter(link)
            layoutManager = LinearLayoutManager(
                this@VolaSportLinkDialog.requireContext(),
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
