package shashank.com.screenrecorder

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

/**
 * Created by shashankm on 10/03/17.
 */
class VideoHelper {

    fun getVideos(context: Context): MutableList<Video> {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.DURATION)
        val cursor: Cursor = context.contentResolver.query(uri, projection, null, null, null)
        val videoList: MutableList<Video> = ArrayList()
        while (cursor.moveToNext()) {
            val data = cursor.getString(0)
            val added = cursor.getLong(1)
            val name = cursor.getString(2)
            val duration = cursor.getLong(3)
            videoList.add(Video(data, added, name, duration))
        }
        cursor.close()
        return videoList
    }
}