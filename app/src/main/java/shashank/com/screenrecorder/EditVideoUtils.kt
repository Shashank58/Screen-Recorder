package shashank.com.screenrecorder

import android.content.Context
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
class EditVideoUtils(context: Context, val response: EditVideoContract.Response) : EditVideoContract {
    var ffmpeg: FFmpeg? = null
    var isConvertToGif = false
    var count = 0

    init {
        if (ffmpeg == null) {
            ffmpeg = FFmpeg.getInstance(context)
        }
    }

    override fun trimFile(file: File, start: String, end: String) {
        response.showProgress("Trimming", "Yup working on it!")
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
                val croppedFile: File = File(Environment.getExternalStorageDirectory().absolutePath + "/"+ System.currentTimeMillis() +".mp4")
                val command = arrayOf("-y", "-i", file.absolutePath, "-crf:", "27", "-preset", "veryfast", "-ss", start, "-to", end, "-strict", "-2", "-async", "1", croppedFile.absolutePath)
                execFFmpegCommand(command)
            }
        }
    }

    override fun convertVideoToGif(file: File) {
        isConvertToGif = true
        count = 0
        response.showProgress("Converting", "Working our magic!")
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
                val croppedFile: File = File(Environment.getExternalStorageDirectory().absolutePath + "/"+ System.currentTimeMillis() + ".gif")
                val pallet: File = File(Environment.getExternalStorageDirectory().absolutePath + "/" + System.currentTimeMillis() + "_pallet.png")
                val palletCommand = arrayOf("-i", file.absolutePath, "-vf", "fps=10,scale=320:-1:flags=lanczos,palettegen", pallet.absolutePath)
                val gitCommand = arrayOf("-i", file.absolutePath, "-i", pallet.absolutePath, "-filter_complex", "fps=10,scale=320:-1:flags=lanczos [x]; [x][1:v] paletteuse", croppedFile.absolutePath)
                execFFmpegCommand(palletCommand)
                execFFmpegCommand(gitCommand)
            }
        }
    }

    override fun slowDownVideo(file: File, quality: String) {
        response.showProgress("Converting", "Slowing it down!")
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
                val slowedVideo: File = File(Environment.getExternalStorageDirectory().absolutePath + "/"+ System.currentTimeMillis() + ".mp4")
                val gitCommand = arrayOf("-i", file.absolutePath, "-r", quality, "-filter:v", "setpts=4.0*PTS", "-preset", "ultrafast", slowedVideo.absolutePath)
                execFFmpegCommand(gitCommand)
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

    private inner class Load : LoadBinaryResponseHandler(), FFmpegLoadBinaryResponseHandler {
        override fun onFailure() {
            super.onFailure()
            Log.d("FFMPEG", "ffmpeg : Failure")
            response.onFailure("Failed!")
        }

        override fun onSuccess() {
            super.onSuccess()
            Log.d("FFMPEG", "ffmpeg : Success")
        }
    }

    private inner class ExecuteHandler : ExecuteBinaryResponseHandler(), FFmpegExecuteResponseHandler {

        override fun onFailure(message: String?) {
            super.onFailure(message)
            response.onFailure("Could not perform said action!")
            Log.d("ExecuteHandler", "ffmpeg : Failure " + message)
        }

        override fun onStart() {
            super.onStart()
            Log.d("ExecuteHandler", "ffmpeg : Started!")
        }

        override fun onSuccess(message: String?) {
            super.onSuccess(message)
            if (isConvertToGif && count == 0) {
                count++
                return
            }

            isConvertToGif = false
            response.finishedSuccessFully()
            Log.d("ExecuteHandler", "ffmpeg : SUCCESS!")
        }

        override fun onProgress(message: String?) {
            super.onProgress(message)
            Log.d("ExecuteHandler", "Progresing....." + message)
        }
    }
}
