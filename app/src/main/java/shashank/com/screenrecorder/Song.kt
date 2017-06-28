package shashank.com.screenrecorder

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever

/**
 * Created by shashankm on 09/05/17.
 */
data class Song(val trackId: Long, val trackNo: Int, val artist: String?, val trackName: String, val album: String?, val duration: Long, val path: String) {
    val mmr = MediaMetadataRetriever()
    var rawArt: ByteArray? = null

    fun setRawArt(context: Context) {
        val uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
        if (uri != null) mmr.setDataSource(context, uri)
        this.rawArt = mmr.embeddedPicture
    }
}
