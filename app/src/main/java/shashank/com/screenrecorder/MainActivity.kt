package shashank.com.screenrecorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_card.view.*
import org.jetbrains.anko.intentFor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val REQUEST_PERMISSION = 2
    private var screenRecordHelper: ScreenRecordHelper? = null

    private var adapter: VideoAdapter? = null
    private var editVideo: EditVideoContract? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val mediaRecorder: MediaRecorder = MediaRecorder()
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val screenDensity = displayMetrics.densityDpi
        screenRecordHelper = ScreenRecordHelper(projectionManager, mediaRecorder, this, screenDensity)
        editVideo = EditVideoUtils(this)

        video_list.layoutManager = LinearLayoutManager(this@MainActivity)
        toggle_screen_record.setOnClickListener(this)

        adapter = VideoAdapter(VideoHelper().getVideos(this))
        video_list.adapter = adapter
    }

    override fun onClick(v: View?) {
        if (v != null) when (v.id) {
            R.id.toggle_screen_record -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                            .permission.RECORD_AUDIO), REQUEST_PERMISSION)
                    return
                }

                if (null == screenRecordHelper) return

                if (screenRecordHelper?.isRecording()!!) {
                    screenRecordHelper?.stopRecording()
                    toggle_screen_record.setCardBackgroundColor(ContextCompat.getColor(this, R.color.teal_500))
                    record_text.text = getString(R.string.start_record)
                } else {
                    screenRecordHelper?.initRecording()
                    toggle_screen_record.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
                    record_text.text = getString(R.string.stop_record)
                }
            }
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

    inner class VideoAdapter(val videoList: MutableList<Video>) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.video_card, parent, false),
                    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(videoList[position])
        }

        override fun getItemCount(): Int = videoList.size

        inner class ViewHolder(itemView: View?, val simpleDateFormat: SimpleDateFormat) : RecyclerView.ViewHolder(itemView) {

            fun bind(video: Video) {
                with(video) {
                    var secs: Int = (duration / 1000).toInt()
                    val mins: Int = (secs / 60)
                    secs %= 60
                    val videoDuration: String = java.lang.String.format(Locale.getDefault(), "%02d", mins) + ":" +
                            java.lang.String.format(Locale.getDefault(), "%02d", secs)
                    itemView.video_name.text = name
                    itemView.date.text = simpleDateFormat.format(Date(addedOn * 1000L))
                    itemView.duration.text = videoDuration
                    Glide.with(this@MainActivity)
                            .load(Uri.fromFile(File(data)))
                            .centerCrop()
                            .into(itemView.video_thumbnail)
                    itemView.video_card.setOnClickListener {
                        startActivity(intentFor<EditVideoActivity>("data" to video.data, "duration" to duration))
                    }

                    itemView.video_options.setOnClickListener {
                        val popUpMenu: PopupMenu = PopupMenu(this@MainActivity, itemView.video_options)
                        popUpMenu.menuInflater.inflate(R.menu.video_options, popUpMenu.menu)
                        popUpMenu.setOnMenuItemClickListener {
                            when(it.itemId) {
                                R.id.convert_to_gif -> {
                                    editVideo?.convertVideoToGif(File(Uri.parse(video.data).path))
                                    true
                                }
                                R.id.slow_video -> {
                                    editVideo?.slowDownVideo(File(Uri.parse(video.data).path))
                                    true
                                }
                                else -> true
                            }
                        }
                        popUpMenu.show()
                    }
                }
            }
        }
    }
}