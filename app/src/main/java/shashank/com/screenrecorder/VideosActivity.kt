package shashank.com.screenrecorder

import android.Manifest
import android.animation.Animator
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_videos.*
import kotlinx.android.synthetic.main.song_card.view.*
import kotlinx.android.synthetic.main.video_card.view.*
import org.jetbrains.anko.intentFor
import java.io.File
import java.util.*
import kotlin.collections.ArrayList



class VideosActivity : AppCompatActivity(), EditVideoContract.Response, View.OnClickListener {
    private val REQUEST_PERMISSION = 1

    private var videoAdapter: VideoAdapter? = null
    private var songAdapter: SongAdapter? = null
    private var editVideo: EditVideoContract? = null
    private var purpose: Int = 0
    private var progressDialog: ProgressDialog? = null
    private var isVideo = true
    private var videoList: MutableList<Video> = ArrayList()
    private var songsList: MutableList<Song> = ArrayList()
    private val layoutManager: GridLayoutManager = GridLayoutManager(this, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        video_list.layoutManager = layoutManager as RecyclerView.LayoutManager?

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                    .permission.RECORD_AUDIO), REQUEST_PERMISSION)
            return
        }

        setUpAdapter()
    }

    private fun setUpAdapter() {
        videoList = MediaHelper().getVideos(this)

        videoAdapter = VideoAdapter()
        songAdapter = SongAdapter()
        editVideo = EditVideoUtils(this, this)

        purpose = intent.getIntExtra("purpose", 0)

        if (purpose == 0) {
            media_select.visibility = View.VISIBLE
            media_select.setOnClickListener(this)
            songsList = MediaHelper().getSongs(this)
        }

        video_list.adapter = videoAdapter
    }

    override fun finishedSuccessFully() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.media_select) {
            if (isVideo) {
                isVideo = false
                Glide.with(this)
                        .load(R.drawable.ic_video)
                        .into(media_select)
                layoutManager.spanCount = 2
                video_list.adapter = songAdapter
            } else {
                isVideo = true
                Glide.with(this)
                        .load(R.drawable.ic_music)
                        .into(media_select)
                layoutManager.spanCount = 3
                video_list.adapter = videoAdapter
            }
        }
    }

    override fun onFailure(message: String) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpAdapter()
            }
        }
    }

    override fun showProgress(title: String, message: String) {
        progressDialog = ProgressDialog.show(this, title, message)
        progressDialog?.show()
    }

    inner class VideoAdapter : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.video_card, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(videoList[position])
        }

        override fun getItemCount(): Int = videoList.size

        inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            val interpolator: AnticipateOvershootInterpolator = AnticipateOvershootInterpolator()

            fun bind(video: Video) {
                with(video) {
                    var secs: Int = (duration / 1000).toInt()
                    val mins: Int = (secs / 60)
                    secs %= 60
                    val videoDuration: String = java.lang.String.format(Locale.getDefault(), "%02d", mins) + ":" +
                            java.lang.String.format(Locale.getDefault(), "%02d", secs)
                    itemView.duration.text = videoDuration
                    Glide.with(this@VideosActivity)
                            .load(Uri.fromFile(File(data)))
                            .centerCrop()
                            .into(itemView.video_thumbnail)
                    itemView.video_card.setOnClickListener {
                        when(purpose) {
                            0 -> {
                                startActivity(intentFor<EditVideoActivity>("data" to video.data, "duration" to duration))
                            }

                            1 -> {
                                quality_popup.visibility = View.VISIBLE
                                quality_popup.animate().setInterpolator(interpolator).setListener(null).scaleY(1f).scaleX(1f).start()
                                blur.visibility = View.VISIBLE

                                blur.setOnClickListener {
                                    hidePopup(null, null)
                                }

                                low.setOnClickListener {
                                    hidePopup("8", data)
                                }

                                medium.setOnClickListener {
                                    hidePopup("16", data)
                                }

                                high.setOnClickListener {
                                    hidePopup("30", data)
                                }

                                very_high.setOnClickListener {
                                    hidePopup("60", data)
                                }
                            }

                            2 -> {
                                editVideo?.convertVideoToGif(File(Uri.parse(data).path))
                            }
                        }
                    }
                }
            }

            private fun hidePopup(quality: String?, data: String?) {
                quality_popup.animate()
                        .setListener(object: Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                quality_popup.visibility = View.GONE
                                blur.visibility = View.GONE
                                if (quality != null && data != null) {
                                    editVideo?.slowDownVideo(File(Uri.parse(data).path), quality)
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }

                        }).setInterpolator(interpolator)
                        .scaleY(0f).scaleX(0f).start()

            }
        }
    }

    inner class SongAdapter : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
        override fun getItemCount(): Int = songsList.size

        override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
            holder.bind(songsList[position], position)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongViewHolder = SongViewHolder(LayoutInflater
                .from(parent?.context).inflate(R.layout.song_card, parent, false))

        inner class SongViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            val colors: Array<Int> = arrayOf(R.color.red_a_100, R.color.pink_a_100, R.color.indigo_a_100, R.color.teal_a_100,
                    R.color.amber_a_100, R.color.orange_a_100, R.color.light_blue_a_100)

            fun bind(song: Song, position: Int) {
                with(song) {
                    itemView.song_name.text = trackName
                    itemView.artist_name.text = artist

                    if (rawArt == null) {
                        itemView.song_cover.setBackgroundColor(ContextCompat.getColor(this@VideosActivity, colors[position % 6]))
                    } else {
                        itemView.song_cover.setBackgroundColor(0)
                        Glide.with(this@VideosActivity)
                                .load(rawArt)
                                .into(itemView.song_cover)
                    }
                }
            }
        }

    }
}
