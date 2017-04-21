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
object ScreenRecordHelper  {
    val REQUEST_CODE = 1

    private val ORIENTATION = SparseIntArray()
    private val DISPLAY_WIDTH = 720
    private val DISPLAY_HEIGHT = 1280
    private val mediaProjectionCallback: MediaProjectionCallback = MediaProjectionCallback(this)

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isRecording = false
    private var projectionManager: MediaProjectionManager? = null
    private var mediaRecorder: MediaRecorder? = null
    private var activity: MainActivity? = null
    private var screenDensity: Int = 0
    private var recordContract: RecordContract? = null

    interface RecordContract {
        fun onRecordingStarted()
    }

    fun init (projectionManager: MediaProjectionManager, mediaRecorder: MediaRecorder, activity: MainActivity,
              screenDensity: Int, recordContract: RecordContract) {
        this.projectionManager = projectionManager
        this.mediaRecorder = mediaRecorder
        this.activity = activity
        this.screenDensity = screenDensity
        this.recordContract = recordContract
    }

    fun initRecording() {
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setOutputFile(Environment.getExternalStorageDirectory().path + "/"+ System
                    .currentTimeMillis() +".mp4")
            mediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setVideoEncodingBitRate(3000000)
            mediaRecorder!!.setVideoFrameRate(30)
            val rotation = activity!!.windowManager.defaultDisplay.rotation
            mediaRecorder!!.setOrientationHint(ORIENTATION.get(rotation + 90))
            mediaRecorder!!.prepare()
            startRecording()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun startRecording() {
        if (mediaProjection == null) {
            activity!!.startActivityForResult(projectionManager!!.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }

        virtualDisplay = createVirtualDisplay()
        mediaRecorder!!.start()
        recordContract!!.onRecordingStarted()
        isRecording = true
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("Main Activity", DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder!!.surface, null, null)
    }

    fun stopRecording() {
        if (mediaRecorder == null) return

        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        isRecording = false
        if (virtualDisplay == null) {
            return
        }

        virtualDisplay?.release()
        destroyMediaProjection()
    }

    fun pauseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder?.stop()
            isRecording = false
        }
    }

    fun resumeRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder?.start()
            isRecording = true
        }
    }

    fun registerMediaProjection(resultCode: Int, data: Intent?) {
        mediaProjection = projectionManager!!.getMediaProjection(resultCode, data)
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
            screenRecordHelper.mediaRecorder!!.stop()
            screenRecordHelper.mediaRecorder!!.reset()

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