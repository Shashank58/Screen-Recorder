package shashank.com.screenrecorder.android

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_edit_video.*
import shashank.com.screenrecorder.R
import shashank.com.screenrecorder.helper.EditVideoContract
import shashank.com.screenrecorder.helper.FfmpegHelper
import shashank.com.screenrecorder.util.AppUtil
import shashank.com.screenrecorder.util.CustomRange
import java.io.File


/**
 * Created by shashankm on 23/03/17.
 */

class EditVideoActivity : AppCompatActivity(), CustomRange.RangeChangeListener, View.OnClickListener, EditVideoContract.Response {
    private val handler = Handler()

    private val hideHandler = Handler()
    private var data: String? = null
    private var progressDialog: ProgressDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_video)


        data = intent.getStringExtra("data")
        val duration = intent.getLongExtra("duration", 0).toInt()
        back.setOnClickListener(this)

        val maxFileDuration = 1000 * 60 * 5 // 5 minutes
        if (duration > maxFileDuration) {
            val builder = AlertDialog.Builder(this)
                    .setTitle("Error!")
                    .setMessage("The file size is too large. It's gonna take forever to trim. Have mercy and pick a smaller file")
                    .setPositiveButton("Cool") { _, _ -> }
            builder.show()
            return
        }

        video.setVideoURI(Uri.parse(data))
        video.start()
        seek_bar.max = duration
        video_range_picker.minValue = 0f
        video_range_picker.maxValue = duration.toFloat()
        video_range_picker.setRangeChangeListener(this)

        start_time!!.text = AppUtil.getMinsAndSecs(0f)
        end_time!!.text = AppUtil.getMinsAndSecs(duration.toFloat())

        seekUpdation()
        hidePlayPause()

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    video!!.seekTo(progress)
                    seekBar.progress = progress
                    video_current_time!!.text = AppUtil.getMinsAndSecs(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        video_container.setOnClickListener(this)
        trim_video.setOnClickListener(this)
    }

    private fun hidePlayPause() {
        hideHandler.postDelayed({ play_pause!!.visibility = View.GONE }, 3000)
    }

    override fun onRangeChanged(startValue: Float, endValue: Float) {
        val startInt = startValue.toInt()
        seek_bar.progress = startInt
        video.seekTo(startInt)
        video.pause()
        start_time.text = AppUtil.getMinsAndSecs(startValue)
        end_time.text = AppUtil.getMinsAndSecs(endValue)
    }

    private val runnable = Runnable { seekUpdation() }

    private fun seekUpdation() {
        seek_bar.progress = video.currentPosition
        video_current_time.text = AppUtil.getMinsAndSecs(video.currentPosition.toFloat())
        handler.postDelayed(runnable, 1000)
    }

    override fun showBusy() {
        AlertDialog.Builder(this)
                .setTitle("Busy!")
                .setMessage("Please wait while we finish up with your earlier media transformation")
                .setPositiveButton("Ok", { _, _ ->  })
                .create().show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.video_container -> {
                play_pause.visibility = View.VISIBLE
                if (video.isPlaying) {
                    video.pause()
                    play_pause.setImageResource(R.drawable.ic_play_arrow)
                } else {
                    video.start()
                    play_pause.setImageResource(R.drawable.ic_pause)
                    hidePlayPause()
                }
            }

            R.id.trim_video -> {
                val ffmpegUtil = FfmpegHelper(this, this)

                val startTime = AppUtil.getTime(video_range_picker.startValue)
                val endTime = AppUtil.getTime(video_range_picker.endValue)
                ffmpegUtil.trimVideo(File(Uri.parse(data).path), (video_range_picker.endValue - video_range_picker.startValue).toInt(),
                        startTime, endTime)

                AlertDialog.Builder(this)
                        .setMessage("Your media conversion has started, you can track its progress in the notification bar")
                        .setPositiveButton("Ok", { _, _ ->  })
                        .create().show()
            }

            R.id.back -> onBackPressed()
        }
    }

    override fun showProgress(title: String, message: String) {
        progressDialog = ProgressDialog.show(this, title, message)
        progressDialog!!.show()
    }

    override fun finishedSuccessFully(path: String?) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your file is successfully saved at $path in your phone. You can also check it out in your gallery!")
                .setPositiveButton("Great") { _, _ -> }.create().show()
    }

    override fun onFailure(message: String) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }
}
