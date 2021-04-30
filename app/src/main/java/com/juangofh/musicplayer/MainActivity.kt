package com.juangofh.musicplayer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_METADATA = "EXTRA_METADA_SONG"
        const val EXTRA_IMAGEDATA = "EXTRA_IMAGE_SONG"
    }

    private var musicPlayer: MediaPlayer? = null
    private var currentSong: Int = R.raw.all_my_love
    private lateinit var mmr: MediaMetadataRetriever
    private lateinit var metaData: MetaData
    private lateinit var imageData: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicPlayer = MediaPlayer.create(this, currentSong)

        setMetaData(this)
        onCreateMusicControls(this)
    }

    private fun setMetaData(context: Context) {
        mmr = MediaMetadataRetriever()
        mmr.setDataSource(
            context,
            Uri.parse("android.resource://com.juangofh.musicplayer/$currentSong")
        )

        val nameOfSong = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "unknown"
        val authorOfSong =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "unknown"
        val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "unknown"
        val genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "unknown"
        val year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) ?: "unknown"

        imageData = createAlbumArt(mmr)
        val destFolder = cacheDir.absolutePath

        val out = FileOutputStream("$destFolder/myBitmap.png")
        imageData.compress(Bitmap.CompressFormat.PNG, 100, out)

        metaData =
            MetaData(nameOfSong, authorOfSong, album, genre, year, "$destFolder/myBitmap.png")

        name_of_song.text = nameOfSong
        author.text = authorOfSong
        image_song.setImageBitmap(imageData)
        duration_number.text = musicPlayer?.duration?.let { millisecondsToString(it) } ?: "unknown"
    }

    private fun onCreateMusicControls(context: Context) {
        musicPlayer?.seekTo(0) // TODO cambiar

        musicPlayer?.setVolume(0.5f, 0.5f)
        seek_bar_volume.progress = 50

        button_play?.setOnClickListener {
            if (musicPlayer?.isPlaying == true) {
                musicPlayer?.pause()
                button_play.setImageResource(R.drawable.ic_play_arrow)
            } else {
                musicPlayer?.start()
                button_play.setImageResource(R.drawable.ic_pause)
                Thread {
                    var current = 0
                    var elapsedTime = "00:00"
                    while (musicPlayer != null) {
                        if (musicPlayer!!.isPlaying) {
                            try {
                                current = musicPlayer?.currentPosition ?: 0
                                elapsedTime = millisecondsToString(current)
                                this@MainActivity.runOnUiThread {
                                    time_number.text = elapsedTime
                                    seek_bar_time.setProgress(current, true)
                                }
                                Thread.sleep(1000)
                            } catch (e: InterruptedException) {
                            }
                        } else {
                            this@MainActivity.runOnUiThread {
                                seek_bar_time.progress = 0
                                time_number.text = "00:00"
                                if (musicPlayer?.isPlaying == true) {
                                    button_play.setImageResource(R.drawable.ic_pause)
                                }
                                musicPlayer?.seekTo(0)
                            }
                        }

                    }
                }.start()
            }
        }
        seek_bar_volume.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, isFromUser: Boolean) {
                val volume = progress / 100f
                musicPlayer?.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seek_bar_time.max = musicPlayer?.duration!!
        seek_bar_time.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicPlayer?.seekTo(progress)
                    seekBar?.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        image_song?.setOnClickListener {
            val intent = Intent(context, DetailSong::class.java)
            intent.putExtra(EXTRA_METADATA, metaData)
//            intent.putExtra(EXTRA_IMAGEDATA, imageData)

            startActivity(intent)
        }
    }

    // Utils functions

    private fun createAlbumArt(mmr: MediaMetadataRetriever): Bitmap {
        var bitmap: Bitmap? = null
        try {
            val embedPic = mmr.embeddedPicture
            bitmap = BitmapFactory.decodeByteArray(embedPic, 0, embedPic!!.size)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                mmr.release()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        return bitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_music_note_24)
    }

    private fun millisecondsToString(time: Int): String {
        val minutes: Int = time / 1000 / 60
        val seconds: Int = time / 1000 % 60
        if (seconds < 10)
            return "$minutes:0$seconds"
        return "$minutes:$seconds"
    }
}
