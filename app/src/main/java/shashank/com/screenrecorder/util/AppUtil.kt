package shashank.com.screenrecorder.util

import java.util.*

/**
 * Created by shashankm on 11/05/17.
 */
object AppUtil {

    fun getMinsAndSecs(value: Float): String {
        val secs = (value / 1000).toInt()
        val minutes = secs / 60
        return String.format(Locale.getDefault(), "%02d", minutes) + ":" + String.format(Locale.getDefault(), "%02d", secs % 60)
    }

    fun getTime(value: Float): String {
        val secs = (value / 1000).toInt()
        val mins = secs / 60
        val hours = mins / 60
        return String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(),
                "%02d", mins) + ":" + String.format(Locale.getDefault(), "%02d", secs % 60)
    }

    val mimeType_Video: String = "video/mp4"

    val mimeType_Gif: String = "image/gif"

    val mimeType_Image: String = "image/jpeg"

    val mimeType_Song: String = "audio/mpeg3"

    val TRIM_VIDEO = 0

    val CONVERT_VIDEO = 2

    val SLOW_VIDEO = 1

    val SCREEN_SHARE_STOPPED = "screen_share_stopped"
}