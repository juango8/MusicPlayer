package com.juangofh.musicplayer

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail_song.*

class DetailSong : AppCompatActivity() {

    private lateinit var metaData: MetaData

    companion object {
        const val EXTRA_METADATA = "EXTRA_METADA_SONG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_song)

        supportActionBar?.hide()

        metaData = intent.getSerializableExtra(EXTRA_METADATA) as MetaData

        nameOfSong.text = metaData.nameOfSong
        memberLogin.text = metaData.author
        memberCompany.text = metaData.album
        memberEmail.text = metaData.genre
        memberType.text = metaData.year
        image_album.setImageURI(Uri.parse(metaData.albumCover))

        backButton.setOnClickListener { onBackPressed() }
    }
}

