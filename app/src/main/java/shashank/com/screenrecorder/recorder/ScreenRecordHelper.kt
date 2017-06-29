package shashank.com.screenrecorder.recorder

import android.content.ContentValues
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import shashank.com.screenrecorder.android.MainActivity
import java.io.File
import java.io.IOException


/**
 * Created by shashankm on 02/03/17.
 */
object ScreenRecordHelper {
    val REQUEST_CODE = 1

    private val ORIENTATION = SparseIntArray()
    private val DISPLAY_WIDTH = 720
    private val DISPLAY_HEIGHT = 1280
    private val mediaProjectionCallback: MediaProjectionCallback = MediaProjectionCallback(this)

    private var mediaProjection: MediaProjection? = null
    lateinit private var virtualDisplay: VirtualDisplay
    private var isRecording = false
    lateinit private var projectionManager: MediaProjectionManager
    lateinit private var mediaRecorder: MediaRecorder
    lateinit private var activity: MainActivity
    private var screenDensity: Int = 0
    lateinit private var recordContract: RecordContract
    private var path: String = ""

    interface RecordContract {
        fun onRecordingStarted()
    }

    fun init(projectionManager: MediaProjectionManager, mediaRecorder: MediaRecorder, activity: MainActivity,
             screenDensity: Int, recordContract: RecordContract) {
        ScreenRecordHelper.projectionManager = projectionManager
        ScreenRecordHelper.mediaRecorder = mediaRecorder
        ScreenRecordHelper.activity = activity
        ScreenRecordHelper.screenDensity = screenDensity
        ScreenRecordHelper.recordContract = recordContract
    }

    fun initRecording() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            if (Environment.getExternalStorageDirectory() != null) {
                val mediaStorageDir = File(Environment.getExternalStorageDirectory(),
                        "ScreenRecorder")

                // Create the storage directory if it does not exist
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("Media Util", "failed to create directory")
                        return
                    }
                }

                path = mediaStorageDir.path + File.separator + System.currentTimeMillis() + ".mp4"
                mediaRecorder.setOutputFile(path)
            } else {
                return
            }

            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setVideoEncodingBitRate(3000000)
            mediaRecorder.setVideoFrameRate(30)
            val rotation = activity.windowManager.defaultDisplay.rotation
            mediaRecorder.setOrientationHint(ORIENTATION.get(rotation + 90))
            mediaRecorder.prepare()
            startRecording()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun startRecording() {
        if (mediaProjection == null) {
            activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }

        virtualDisplay = createVirtualDisplay()
        mediaRecorder.start()
        recordContract.onRecordingStarted()
        isRecording = true
    }

    fun getProjectionManager(): MediaProjectionManager = projectionManager

    private fun createVirtualDisplay(): VirtualDisplay {
        return mediaProjection!!.createVirtualDisplay("Main Activity", DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.surface, null, null)
    }

    fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        isRecording = false
        virtualDisplay.release()
        destroyMediaProjection()
        addMediaToGallery()
    }

    fun addMediaToGallery() {
        val values = ContentValues()
        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.MediaColumns.DATA, path)

        activity.contentResolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return
        MediaScannerConnection.scanFile(activity, arrayOf(path), arrayOf("video/mp4"), null)
    }

    fun pauseRecorder() {
        mediaRecorder.stop()
        isRecording = false
    }

    fun resumeRecorder() {
        mediaRecorder.start()
        isRecording = true
    }

    fun registerMediaProjection(resultCode: Int, data: Intent?) {
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(mediaProjectionCallback, null)
        startRecording()
    }

    private fun destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection?.unregisterCallback(mediaProjectionCallback)
            mediaProjection?.stop()
            mediaProjection = null
        }
    }

    private class MediaProjectionCallback(val screenRecordHelper: ScreenRecordHelper) : MediaProjection.Callback() {

        override fun onStop() {
            super.onStop()
            mediaRecorder.stop()
            mediaRecorder.reset()

            mediaProjection = null
            stopScreenSharing()
        }
    }

    private fun stopScreenSharing() {
        virtualDisplay.release()
        destroyMediaProjection()
    }
}