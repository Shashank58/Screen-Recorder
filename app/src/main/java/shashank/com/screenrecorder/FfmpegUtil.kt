package shashank.com.screenrecorder

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.*
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.uiThread
import java.io.File





/**
 * Created by shashankm on 09/03/17.
 */
class FfmpegUtil(val context: Context, val response: EditVideoContract.Response) : EditVideoContract {
    var ffmpeg: FFmpeg = FFmpeg.getInstance(context)
    var isTwice = false
    var count = 0
    lateinit var path: String
    lateinit var type: String
    var duration: Int = -1

    override fun trimVideo(file: File, duration: Int, start: String, end: String) {
        context.startService(context.intentFor<ConvertMediaService>("title" to "Trimming", "description" to "Yup working on it!"))
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val croppedFile: File = getFile(".mp4", null) ?: return@uiThread
                val command = arrayOf("-y", "-i", file.absolutePath, "-crf:", "27", "-preset", "veryfast", "-ss", start, "-to", end, "-strict", "-2", "-async", "1", croppedFile.absolutePath)
                path = croppedFile.absolutePath
                type = AppUtil.mimeType_Video
                this@FfmpegUtil.duration = duration
                execFFmpegCommand(command)
            }
        }
    }

    override fun convertVideoToGif(file: File) {
        Log.d("ExecuteHandler", "ffmpeg : STARTED NOW!")
        isTwice = true
        count = 0
        context.startService(context.intentFor<ConvertMediaService>("title" to "Converting", "description" to "Working our magic!"))
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val gifFile: File = getFile(".gif", null) ?: return@uiThread
                val pallet: File = getFile(".png", "pallet") ?: return@uiThread
                val palletCommand = arrayOf("-i", file.absolutePath, "-vf", "fps=10,scale=320:-1:flags=lanczos,palettegen", pallet.absolutePath)
                val gifCommand = arrayOf("-i", file.absolutePath, "-i", pallet.absolutePath, "-filter_complex", "fps=10,scale=320:-1:flags=lanczos [x]; [x][1:v] paletteuse", gifFile.absolutePath)
                path = gifFile.absolutePath
                type = AppUtil.mimeType_Gif
                execFFmpegCommand(palletCommand)
                execFFmpegCommand(gifCommand)
            }
        }
    }

    override fun slowDownVideo(file: File, duration: Int, quality: String, clipAudio: Boolean) {
        context.startService(context.intentFor<ConvertMediaService>("title" to "Converting", "description" to "Slowing it down!"))
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val slowedVideo: File = getFile(".mp4", null) ?: return@uiThread
                path = slowedVideo.absolutePath
                type = AppUtil.mimeType_Video
                this@FfmpegUtil.duration = duration
                if (clipAudio) {
                    isTwice = true
                    val mutedVideo = getFile(".mp4", "mutedVideo") ?: return@uiThread
                    execFFmpegCommand(arrayOf("-i", file.absolutePath, "-an", mutedVideo.absolutePath))
                    execFFmpegCommand(arrayOf("-i", mutedVideo.absolutePath, "-r", quality, "-filter:v", "setpts=3.5*PTS", "-preset", "ultrafast", slowedVideo.absolutePath))
                } else {
                    execFFmpegCommand(arrayOf("-i", file.absolutePath, "-r", quality, "-filter:v", "setpts=3.5*PTS", "-preset", "ultrafast", slowedVideo.absolutePath))
                }
            }
        }
    }

    override fun trimSong(file: File, start: String, difference: String) {
        context.startService(context.intentFor<ConvertMediaService>("title" to "Converting", "description" to "Trimming down to your needs!"))
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val trimSong: File = getFile(".mp3", null) ?: return@uiThread
                val command = arrayOf("-ss", start, "-t", difference, "-i", file.absolutePath, trimSong.absolutePath)
                path = trimSong.absolutePath
                type = AppUtil.mimeType_Song
                duration = difference.toInt()
                execFFmpegCommand(command)
            }
        }
    }

    override fun mixVideoWithSong(songFile: File, videoFile: File) {
        context.startService(context.intentFor<ConvertMediaService>("title" to "Mixing", "description" to "Combining media..."))
        doAsync {
            try {
                val loadResponse: Load = Load()
                ffmpeg.loadBinary(loadResponse)
            } catch (e: FFmpegNotSupportedException) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Not supported")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("FFMPEG", "ffmpeg : Exception")
            }
            uiThread {
                val mixedMedia: File = getFile(".mp4", null) ?: return@uiThread
                val mutedVideo: File = getFile(".mp4", "mutedVideo") ?: return@uiThread

                isTwice = true
                path = mixedMedia.absolutePath
                type = AppUtil.mimeType_Video

                execFFmpegCommand(arrayOf("-i", videoFile.absolutePath, "-an", mutedVideo.absolutePath))
                execFFmpegCommand(arrayOf("-i", mutedVideo.absolutePath, "-i", songFile.absolutePath, "-c:v", "copy",
                        "-c:a", "aac", "-strict", "experimental", "-shortest", mixedMedia.absolutePath))
            }
        }
    }

    fun addMediaToGallery() {
        val values = ContentValues()
        when (type) {
            AppUtil.mimeType_Gif -> {
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(MediaStore.Images.Media.MIME_TYPE, type)
                val pallet: File? = getFile(".png", "pallet")
                pallet?.delete()
            }

            AppUtil.mimeType_Image -> {
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(MediaStore.Images.Media.MIME_TYPE, type)
            }

            AppUtil.mimeType_Song -> {
                values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis())
                values.put(MediaStore.Audio.Media.MIME_TYPE, type)
                values.put(MediaStore.Audio.Media.IS_MUSIC, 1)
                values.put(MediaStore.Audio.Media.DURATION, duration)
            }

            AppUtil.mimeType_Video -> {
                values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                values.put(MediaStore.Video.Media.MIME_TYPE, type)
                val muteVideo: File? = getFile(".mp4", "mutedVideo")
                muteVideo?.delete()
            }
        }
        values.put(MediaStore.Video.Media.MIME_TYPE, type)
        values.put(MediaStore.MediaColumns.DATA, path)

        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        MediaScannerConnection.scanFile(context, arrayOf(path), arrayOf(type), null)
        val intent = Intent(NotificationCallbacks.CONVERSION_SUCCESS)
        intent.putExtra("path", path)
        intent.putExtra("type", type)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun getFile(fileFormat: String, name: String?): File? {
        if (Environment.getExternalStorageDirectory() != null) {
            val mediaStorageDir = File(Environment.getExternalStorageDirectory(),
                    "ScreenRecorder")

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("Media Util", "failed to create directory")
                    return null
                }
            }

            val fileName = name ?: System.currentTimeMillis()
            return File(mediaStorageDir.path + File.separator + fileName + fileFormat)
        } else {
            return null
        }
    }

    private fun execFFmpegCommand(command: Array<String>) {
        val executeHandler: FFmpegExecuteResponseHandler = ExecuteHandler()
        try {
            ffmpeg.execute(command, executeHandler)
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
            if (isTwice && count == 0) {
                count++
                return
            }

            isTwice = false

            addMediaToGallery()
            Log.d("ExecuteHandler", "ffmpeg : SUCCESS!")
        }

        override fun onProgress(message: String?) {
            super.onProgress(message)
            Log.d("ExecuteHandler", "Progresing..... $message")
        }
    }
}
