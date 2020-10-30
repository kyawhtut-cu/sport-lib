package com.kyawhtut.lib

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.kyawhtut.lib.`object`.PlayerObject
import com.kyawhtut.lib.utils.UnsafeOkHttpClient
import kotlinx.android.synthetic.main.activity_player.*

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
class PlayerActivity : AppCompatActivity(R.layout.activity_player) {

    companion object {
        private val TAG = PlayerActivity::class.simpleName
        const val extraPlayerObject = "extraPlayerObject"
    }

    private var isDoubleBackToExitPressedOnce = false

    private val bandwidthMeter by lazy {
        DefaultBandwidthMeter.Builder(this).build()
    }
    private val simpleExoPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(
            this,
            DefaultRenderersFactory(this)
        ).setTrackSelector(
            DefaultTrackSelector(
                this,
                AdaptiveTrackSelection.Factory(bandwidthMeter)
            )
        ).setLoadControl(DefaultLoadControl())
            .setBandwidthMeter(bandwidthMeter)
            .build()
    }
    private val playerObject by lazy {
        intent?.getSerializableExtra(extraPlayerObject) as PlayerObject
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simpleExoPlayer.apply {
            addListener(ComponentListener())
            playWhenReady = true
        }

        simpleExoPlayerView.apply {
            keepScreenOn = true
            simpleExoPlayerView.player = simpleExoPlayer
            useController = true
            hideController()
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

        playVideo(playerObject.url)
    }

    private fun playVideo(url: String) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Video can't play right now.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("Player URL => ", url)

        val httpDataSourceFactory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(
                this,
                applicationInfo.loadLabel(packageManager).toString()
            ), null
        )

        val okHttpDataSource: DataSource.Factory = OkHttpDataSourceFactory(
            UnsafeOkHttpClient.getUnsafeOkHttpClient().build(),
            Util.getUserAgent(
                this,
                applicationInfo.loadLabel(packageManager).toString()
            )
        ).apply {
            //fixme : for volla sport
//            _customHeader.forEach {
//                defaultRequestProperties.set(it.first, it.second)
//            }
        }
        //fixme : for volla sport
        val dataSourceFactory = if (false) {
            val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                Util.getUserAgent(
                    this,
                    applicationInfo.loadLabel(packageManager).toString()
                ), null
            ).apply {
                //fixme : for volla sport
//                _customHeader.forEach {
//                    defaultRequestProperties.set(it.first, it.second)
//                }
            }
            DefaultDataSourceFactory(applicationContext, null, httpDataSourceFactory)
        } else {
            DefaultDataSourceFactory(
                this,
                Util.getUserAgent(
                    this,
                    applicationInfo.loadLabel(packageManager).toString()
                )
            )
        }

        when (Util.inferContentType(Uri.parse(url))) {
            C.TYPE_HLS -> {
                val mediaSource =
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
                simpleExoPlayer.prepare(mediaSource)
            }
            C.TYPE_OTHER -> {
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(url))
                simpleExoPlayer.prepare(mediaSource)
            }
            else -> {
                Toast.makeText(
                    this@PlayerActivity,
                    "Video can't play right now.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        val mediaSession = MediaSessionCompat(this, this.packageName)
        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(simpleExoPlayer)
        mediaSession.isActive = true
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayerView.player = null
        simpleExoPlayer.release()
    }

    override fun onBackPressed() {
        if (isDoubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        isDoubleBackToExitPressedOnce = true
        Toast.makeText(this, "Click back again to exit.", Toast.LENGTH_LONG).show()

        Handler().postDelayed(
            {
                isDoubleBackToExitPressedOnce = false
            }, 2000
        )
    }

    private inner class ComponentListener : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
        }

        override fun onSeekProcessed() {
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
            Toast.makeText(
                this@PlayerActivity,
                error.localizedMessage ?: "Unknown error found",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        override fun onLoadingChanged(isLoading: Boolean) {
        }

        override fun onPositionDiscontinuity(reason: Int) {
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val state: String
            when (playbackState) {
                Player.STATE_IDLE -> {
                    state = "ExoPlayer.STATE_IDLE      -"
                }
                Player.STATE_BUFFERING -> {
                    loading.visibility = View.VISIBLE
                    state = "ExoPlayer.STATE_BUFFERING -"
                }
                Player.STATE_READY -> {
                    loading.visibility = View.GONE
                    state = "ExoPlayer.STATE_READY     -"
                }
                Player.STATE_ENDED -> {
                    state = "ExoPlayer.STATE_ENDED     -"
                    Toast.makeText(this@PlayerActivity, "Video finished.", Toast.LENGTH_LONG).show()
                    finish()
                }
                else -> {
                    state = "UNKNOWN_STATE             -"
                }
            }
            Log.d(TAG, state)
        }
    }
}
