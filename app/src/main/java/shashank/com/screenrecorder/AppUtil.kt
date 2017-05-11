package shashank.com.screenrecorder

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
}