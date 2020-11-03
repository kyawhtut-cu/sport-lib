package com.kyawhtut.lib

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btn_sport5.setOnClickListener {
            startActivity(Intent(this, Sport5Activity::class.java))
        }

        btn_fefatv.setOnClickListener {
            startActivity(Intent(this, FeFaTvCategoryActivity::class.java))
        }

        btn_vola_sport.setOnClickListener {
            startActivity(Intent(this, VolaSportActivity::class.java))
        }

        btn_mm_football.setOnClickListener {
            startActivity(Intent(this, MMFootballActivity::class.java))
        }

        btn_football_live_mm.setOnClickListener {
            startActivity(Intent(this, FootballLiveMMActivity::class.java))
        }
    }
}
