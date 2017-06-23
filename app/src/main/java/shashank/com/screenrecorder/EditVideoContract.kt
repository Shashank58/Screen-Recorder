package shashank.com.screenrecorder

import java.io.File

/**
 * Created by shashankm on 10/03/17.
 */
interface EditVideoContract {
    fun trimVideo(file: File, duration: Int, start: String, end: String)

    fun convertVideoToGif(file: File)

    fun slowDownVideo(file: File, duration: Int, quality: String, clipAudio: Boolean)

    fun trimSong(file: File, start: String, difference: String)

    fun mixVideoWithSong(songFile: File, videoFile: File)

    interface Response {
        fun showProgress(title: String, message: String)

        fun finishedSuccessFully(path: String?)

        fun onFailure(message: String)

        fun showBusy()
    }
}