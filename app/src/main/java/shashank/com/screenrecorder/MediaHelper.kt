package shashank.com.screenrecorder

import android.content.Context
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException


/**
 * Created by shashankm on 10/03/17.
 */
class MediaHelper {
    val track_id = MediaStore.Audio.Media._ID
    val track_no = MediaStore.Audio.Media.TRACK
    val track_name = MediaStore.Audio.Media.TITLE
    val artist = MediaStore.Audio.Media.ARTIST
    val duration = MediaStore.Audio.Media.DURATION
    val album = MediaStore.Audio.Media.ALBUM
    val path = MediaStore.Audio.Media.DATA
    var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val mediaPlayer = MediaPlayer()

    private var isMediaPlayerReleased = true

    init {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
    }

    fun getVideos(context: Context): MutableList<Video> {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.DURATION)
        val cursor: Cursor = context.contentResolver.query(uri, projection, null, null, null)
        val videoList: MutableList<Video> = ArrayList()
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            val added = cursor.getLong(1)
            val name: String? = cursor.getString(2)
            val duration = cursor.getLong(3)
            videoList.add(Video(data, added, name, duration))
        }
        cursor.close()
        return videoList
    }

    fun getSongs(context: Context): MutableList<Song> {
        val songsList: MutableList<Song> = ArrayList()
        val columns = arrayOf(track_id, track_no, artist, track_name, album, duration, path)
        val cursor = context.contentResolver.query(uri, columns, null, null, null)
        while (cursor.moveToNext()) {
            val trackId = cursor.getLong(0)
            val trackNo = cursor.getInt(1)
            val artist = cursor.getString(2)
            val trackName = cursor.getString(3)
            val album = cursor.getString(4)
            val duration = cursor.getLong(5)
            val path = cursor.getString(6)
            val song: Song = Song(trackId, trackNo, artist, trackName, album, duration, path)
            song.setRawArt(context)
            songsList.add(song)
        }
        cursor.close()
        return songsList
    }

    fun initializeSong(path: String) {
        isMediaPlayerReleased = false
        try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
        } catch (e: IOException) {

        }
    }

    fun isMediaPlayerPlaying(): Boolean = !isMediaPlayerReleased && mediaPlayer.isPlaying

    fun toggleMediaPlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
    }

    fun stopMediaPlayer() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        isMediaPlayerReleased = true
    }

    fun getCurrentPosition(): Int = mediaPlayer.currentPosition

    fun seekTo(progress: Int) {
        mediaPlayer.seekTo(progress)
    }

    fun release() = mediaPlayer.release()
}