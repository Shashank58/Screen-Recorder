package shashank.com.screenrecorder

import android.os.Environment
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.*
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

/**
 * Created by shashankm on 09/03/17.
 */

class TrimVideoUtils(val ffmpeg: FFmpeg?) {

    fun trimFile(file: File) {
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg?.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val croppedFile: File = File(Environment.getExternalStorageDirectory().absolutePath + "/croppedVideo.mp4")
                val command = arrayOf("-y", "-i", file.absolutePath, "-crf:", "27", "-preset", "veryfast", "-ss", "00:00:02", "-to", "00:00:05", "-strict", "-2", "-async", "1", croppedFile.absolutePath)
                execFFmpegCommand(command)
            }
        }
    }

    private fun execFFmpegCommand(command: Array<String>) {
        val executeHandler: FFmpegExecuteResponseHandler = ExecuteHandler()
        try {
            ffmpeg?.execute(command, executeHandler)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
        }

    }

    private class Load : LoadBinaryResponseHandler(), FFmpegLoadBinaryResponseHandler {
        override fun onFailure() {
            super.onFailure()
            Log.d("FFMPEG", "ffmpeg : Failure")
        }

        override fun onSuccess() {
            super.onSuccess()
            Log.d("FFMPEG", "ffmpeg : Success")
        }
    }

    private class ExecuteHandler : ExecuteBinaryResponseHandler(), FFmpegExecuteResponseHandler {
        override fun onFailure(message: String?) {
            super.onFailure(message)
            Log.d("ExecuteHandler", "ffmpeg : Failure " + message)
        }

        override fun onStart() {
            super.onStart()
            Log.d("ExecuteHandler", "ffmpeg : Started!")
        }

        override fun onSuccess(message: String?) {
            super.onSuccess(message)
            Log.d("ExecuteHandler", "ffmpeg : SUCCESS!")
        }

        override fun onProgress(message: String?) {
            super.onProgress(message)
            Log.d("ExecuteHandler", "Progresing.....")
        }
    }
}
