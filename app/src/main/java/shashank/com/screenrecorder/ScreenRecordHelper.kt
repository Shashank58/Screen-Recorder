package shashank.com.screenrecorder

import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.util.SparseIntArray
import java.io.IOException

/**
 * Created by shashankm on 02/03/17.
 */
class ScreenRecordHelper(val projectionManager: MediaProjectionManager, val mediaRecorder: MediaRecorder, val
            activity: MainActivity, val screenDensity: Int) {
    val REQUEST_CODE = 1

    private val ORIENTATION = SparseIntArray()
    private val DISPLAY_WIDTH = 720
    private val DISPLAY_HEIGHT = 1280

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val mediaProjectionCallback: MediaProjectionCallback = MediaProjectionCallback(this)

    fun initRecording() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().path + "/video.mp4")
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

    private fun startRecording() {
        if (mediaProjection == null) {
            activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }

        virtualDisplay = createVirtualDisplay()
        mediaRecorder.start()
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("Main Activity", DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.surface, null, null)
    }

    fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()

        if (virtualDisplay == null) {
            return
        }

        virtualDisplay?.release()
        destroyMediaProjection()
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
            screenRecordHelper.mediaRecorder.stop()
            screenRecordHelper.mediaRecorder.reset()

            screenRecordHelper.mediaProjection = null
            screenRecordHelper.stopScreenSharing()
        }
    }

    private fun stopScreenSharing() {
        if (virtualDisplay == null) {
            return
        }

        virtualDisplay?.release()
        destroyMediaProjection()
    }
}