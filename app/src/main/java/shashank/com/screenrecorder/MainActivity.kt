package shashank.com.screenrecorder

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.OvershootInterpolator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.intentFor


class MainActivity : AppCompatActivity(), View.OnClickListener, ScreenRecordHelper.RecordContract {
    private val REQUEST_PERMISSION = 2
    private var screenRecordHelper: ScreenRecordHelper = ScreenRecordHelper
    private var circularAnimationDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val mediaRecorder: MediaRecorder = MediaRecorder()
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenDensity = displayMetrics.densityDpi

        if (!screenRecordHelper.isRecording()) {
            screenRecordHelper.init(projectionManager, mediaRecorder, this, screenDensity, this)
        } else {
            circularAnimationDone = true
            onRecordingStarted()
        }

        val interpolator: OvershootInterpolator = OvershootInterpolator(2.5f)

        record_screen.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission
                            .RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                                .permission.RECORD_AUDIO), REQUEST_PERMISSION)
                        return@setOnTouchListener true
                    }

                    if (circularAnimationDone) {
                        if (screenRecordHelper.isRecording()) {
                            screenRecordHelper.stopRecording()
                        } else {
                            screenRecordHelper.initRecording()
                        }
                        return@setOnTouchListener true
                    }

                    circularAnimationDone = true
                    circularRevealAnimation(event, interpolator, R.string.start_recording, R.drawable.ic_play_arrow)
                }
                else -> {
                }
            }

            return@setOnTouchListener true
        }
        edit_video.setOnClickListener(this)
        slow_motion.setOnClickListener(this)
        convert_video_to_gif.setOnClickListener(this)
    }

    override fun onRecordingStarted() {
        record_icon.setImageResource(R.drawable.ic_stop)
        record_title.text = getString(R.string.stop_record)
        if (record_description.visibility == View.VISIBLE) {
            record_description.visibility = View.GONE
            circular_reveal.visibility = View.VISIBLE
            record_title.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        circular_reveal.setBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
    }

    private fun circularRevealAnimation(event: MotionEvent, interpolator: OvershootInterpolator, recordTitle: Int, recordIcon: Int) {
        record_icon.animate().scaleY(0f).scaleX(0f).setDuration(250).start()
        record_title.animate().scaleY(0f).scaleX(0f).setDuration(250).start()
        record_description.animate().scaleY(0f).scaleX(0f).setDuration(250).start()

        record_icon.setImageResource(recordIcon)
        record_title.text = getText(recordTitle)
        record_title.setTextColor(ContextCompat.getColor(this, R.color.white))

        val cx = event.x.toInt() //Visible layout
        val cy = event.y.toInt()

        val finalRadius = Math.hypot(circular_reveal.width.toDouble(), circular_reveal.height.toDouble()).toInt()
        val initialRadius = 0

        val anim = ViewAnimationUtils.createCircularReveal(circular_reveal, cx, cy, initialRadius.toFloat(),
                finalRadius.toFloat())

        anim.duration = 500

        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                record_icon.animate().scaleY(1f).scaleX(1f).setInterpolator(interpolator).setDuration(300).start()
                record_title.animate().scaleY(1f).scaleX(1f).setInterpolator(interpolator).setDuration(300).start()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })

        circular_reveal.visibility = View.VISIBLE
        anim.start()
    }

    override fun onClick(v: View?) {
        if (v != null) when (v.id) {
            R.id.edit_video -> {
                animateCard(edit_video, 0)
            }

            R.id.slow_motion -> {
                animateCard(slow_motion, 1)
            }

            R.id.convert_video_to_gif -> {
                animateCard(convert_video_to_gif, 2)
            }
        }
    }

    private fun animateCard(view: View, purpose: Int) {
        view.animate()
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        view.animate()
                                .setListener(object : Animator.AnimatorListener {
                                    override fun onAnimationRepeat(animation: Animator?) {

                                    }

                                    override fun onAnimationEnd(animation: Animator?) {
                                        startActivity(intentFor<VideosActivity>("purpose" to purpose))
                                    }

                                    override fun onAnimationStart(animation: Animator?) {

                                    }

                                    override fun onAnimationCancel(animation: Animator?) {

                                    }

                                })
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                    }
                })
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != screenRecordHelper.REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return
        }

        screenRecordHelper.registerMediaProjection(resultCode, data)
        startService(intentFor<RecordService>())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]
                    == PackageManager.PERMISSION_GRANTED) {
                screenRecordHelper.initRecording()
            }
        }
    }
}