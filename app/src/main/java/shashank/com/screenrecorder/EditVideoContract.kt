package shashank.com.screenrecorder

import java.io.File

/**
 * Created by shashankm on 10/03/17.
 */
interface EditVideoContract {
    fun trimFile(file: File, start: String, end: String)

    fun convertVideoToGif(file: File)

    fun slowDownVideo(file: File, quality: String)

    interface Response {
        fun showProgress(title: String, message: String)

        fun finishedSuccessFully()

        fun onFailure(message: String)
    }
}