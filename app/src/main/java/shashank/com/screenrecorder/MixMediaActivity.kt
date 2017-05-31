package shashank.com.screenrecorder

import android.Manifest
import android.app.AlertDialog
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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_mix_media.*
import kotlinx.android.synthetic.main.song_card.view.*
import kotlinx.android.synthetic.main.video_card.view.*
import java.io.File

class MixMediaActivity : AppCompatActivity(), EditVideoContract.Response {
    private val REQUEST_PERMISSION = 1

    private var videoList = ArrayList<Video>()
    private var songsList = ArrayList<Song>()
    private var progressDialog: ProgressDialog? = null

    lateinit private var videoFile: File
    lateinit private var videoAdapter: VideoAdapter
    lateinit private var songAdapter: SongAdapter

    private val ffmpegUtil: EditVideoContract by lazy {
        FfmpegUtil(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mix_media)

        screen_title.text = getString(R.string.select_video)
        media_list.layoutManager = GridLayoutManager(this, 3)
        media_list.setHasFixedSize(true)

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
                    .permission.RECORD_AUDIO), REQUEST_PERMISSION)
            return
        }

        setUpAdapter()
    }

    override fun showProgress(title: String, message: String) {
        progressDialog = ProgressDialog.show(this, title, message)
        progressDialog?.show()
    }

    override fun finishedSuccessFully(path: String?) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your file is successfully saved at $path in your phone. You can also check it out in your gallery!")
                .setPositiveButton("Great", { _, _ ->  })
                .create().show()
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

    fun selectVideoFile(video: Video) {
        videoFile = File(Uri.parse(video.data).path)
        video_name.text = video.name

        added_video_layout.visibility = View.VISIBLE
        screen_title.text = getString(R.string.select_song)
        media_list.adapter = songAdapter

        remove_video.setOnClickListener {
            added_video_layout.visibility = View.GONE
            screen_title.text = getString(R.string.select_video)
            media_list.adapter = videoAdapter
        }
    }

    private fun setUpAdapter() {
        val mediaHelper = MediaHelper()
        videoList = mediaHelper.getVideos(this) as ArrayList<Video>
        songsList = mediaHelper.getSongs(this) as ArrayList<Song>

        videoAdapter = VideoAdapter()
        songAdapter = SongAdapter()

        media_list.adapter = videoAdapter
    }

    inner class VideoAdapter : RecyclerView.Adapter<VideoAdapter.Holder>() {
        override fun onBindViewHolder(holder: Holder, position: Int) {
            val video: Video = videoList[position]
            with(video) {
                var secs: Int = (duration / 1000).toInt()
                val mins: Int = (secs / 60)
                secs %= 60
                val videoDuration: String = java.lang.String.format(java.util.Locale.getDefault(), "%02d", mins) + ":" +
                        java.lang.String.format(java.util.Locale.getDefault(), "%02d", secs)
                holder.itemView.duration.text = videoDuration
                Glide.with(this@MixMediaActivity)
                        .load(android.net.Uri.fromFile(java.io.File(data)))
                        .centerCrop()
                        .into(holder.itemView.video_thumbnail)
                holder.itemView.video_card.setOnClickListener { selectVideoFile(video) }
            }
        }

        override fun getItemCount(): Int = videoList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent?.context).inflate(R.layout.video_card, parent, false))
        }

        inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    }

    inner class SongAdapter : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
        val colors: Array<Int> = arrayOf(R.color.red_a_100, R.color.pink_a_100, R.color.indigo_a_100, R.color.teal_a_100,
                R.color.amber_a_100, R.color.orange_a_100, R.color.light_blue_a_100)

        override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
            val song: Song = songsList[position]
            with(song) {
                holder.itemView.song_name.text = trackName
                holder.itemView.artist_name.text = artist

                if (rawArt == null) {
                    holder.itemView
                    .song_cover.setBackgroundColor(ContextCompat.getColor(this@MixMediaActivity, colors[position % 6]))
                } else {
                    holder.itemView.song_cover.setBackgroundColor(0)
                    Glide.with(this@MixMediaActivity)
                            .load(rawArt)
                            .into(holder.itemView.song_cover)
                    holder.itemView.song_card.setOnClickListener {
                        ffmpegUtil.mixVideoWithSong(File(Uri.parse(path).path), videoFile)
                    }
                }
            }
        }

        override fun getItemCount(): Int = songsList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongAdapter.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.song_card, parent, false))
        }

        inner class ViewHolder(item: View?) : RecyclerView.ViewHolder(item)
    }
}
