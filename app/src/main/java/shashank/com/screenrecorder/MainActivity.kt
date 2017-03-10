package shashank.com.screenrecorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity, View.OnClickListener {
    private val REQUEST_PERMISSION = 2
    private var screenRecordHelper: ScreenRecordHelper? = null
    private var ffmpeg: FFmpeg? = null

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val mediaRecorder: MediaRecorder = MediaRecorder()
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenDensity = displayMetrics.densityDpi
        screenRecordHelper = ScreenRecordHelper(projectionManager, mediaRecorder, this, screenDensity)

        start_recording.setOnClickListener(this)
        stop_recording.setOnClickListener(this)

        val sdcard = Environment.getExternalStorageDirectory()
        val file = File(sdcard, "video.mp4")


        if (ffmpeg == null) {
            ffmpeg = FFmpeg.getInstance(this)
        }
        TrimVideoUtils(ffmpeg).trimFile(file)
    }

    override fun onClick(v: View?) {
        if (v != null) when (v.id) {
            R.id.start_recording -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                            .permission.RECORD_AUDIO), REQUEST_PERMISSION)
                    return
                }
                screenRecordHelper?.initRecording()
            }

            R.id.stop_recording -> screenRecordHelper?.stopRecording()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != screenRecordHelper?.REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }

        screenRecordHelper?.registerMediaProjection(resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                screenRecordHelper?.initRecording()
            }
        }
    }
}