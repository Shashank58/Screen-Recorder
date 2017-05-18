package shashank.com.screenrecorder

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.SeekBar
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
    private var editFile: EditVideoContract? = null
    private var purpose: Int = 0
    private var progressDialog: ProgressDialog? = null
    private var isVideo = true
    private var videoList: MutableList<Video> = ArrayList()
    private var songsList: MutableList<Song> = ArrayList()
    private val layoutManager: GridLayoutManager = GridLayoutManager(this, 3)

    private val interpolator: AnticipateOvershootInterpolator = AnticipateOvershootInterpolator()
    private val mediaHelper: MediaHelper = MediaHelper()
    private val handler: Handler = Handler()

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
        videoList = mediaHelper.getVideos(this)

        videoAdapter = VideoAdapter()
        songAdapter = SongAdapter()
        editFile = FfmpegUtil(this, this)

        purpose = intent.getIntExtra("purpose", 0)

        if (purpose == 0) {
            media_select.visibility = View.VISIBLE
            media_select.setOnClickListener(this)
            songsList = mediaHelper.getSongs(this)
        }

        back.setOnClickListener { onBackPressed() }
        video_list.adapter = videoAdapter
    }

    override fun finishedSuccessFully(path: String?) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        if (purpose == 0) {
            videoList = mediaHelper.getVideos(this)
            toolbar_title.text = getString(R.string.videos)
            media_select.setImageResource(R.drawable.ic_video)
            videoAdapter?.notifyDataSetChanged()
        } else {
            songsList = mediaHelper.getSongs(this)
            toolbar_title.text = getString(R.string.songs)
            media_select.setImageResource(R.drawable.ic_music)
            songAdapter?.notifyDataSetChanged()
        }

        AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your file is successfully saved at $path in your phone")
                .setPositiveButton("Great", { _, _ ->  })
                .create().show()
    }

    override fun onBackPressed() {
        if (mediaHelper.isMediaPlayerPlaying()) {
            closeSongTrimPopup()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        mediaHelper.release()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.media_select) {
            if (isVideo) {
                isVideo = false
                media_select.setImageResource(R.drawable.ic_video)
                layoutManager.spanCount = 2
                toolbar_title.text = getString(R.string.songs)
                video_list.adapter = songAdapter
            } else {
                isVideo = true
                media_select.setImageResource(R.drawable.ic_music)
                layoutManager.spanCount = 3
                toolbar_title.text = getString(R.string.videos)
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
                                editFile?.convertVideoToGif(File(Uri.parse(data).path))
                            }
                        }
                    }
                }
            }

            private fun hidePopup(quality: String?, data: String?) {
                quality_popup.animate()
                        .setListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                quality_popup.visibility = View.GONE
                                blur.visibility = View.GONE
                                if (quality != null && data != null) {
                                    editFile?.slowDownVideo(File(Uri.parse(data).path), quality)
                                }
                            }
                        }).setInterpolator(interpolator).scaleY(0f).scaleX(0f).start()
            }
        }
    }

    fun closeSongTrimPopup() {
        mediaHelper.stopMediaPlayer()
        handler.removeCallbacks(runnable)
        song_trim_card.animate().setInterpolator(interpolator)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        song_trim_card.visibility = View.GONE
                        blur.visibility = View.GONE
                    }
                }).scaleY(0f).scaleX(0f).start()
    }

    private var runnable = Runnable {
        seekUpdation()
    }

    private fun seekUpdation() {
        song_seek_bar.progress = mediaHelper.getCurrentPosition()
        song_time.text = AppUtil.getMinsAndSecs(mediaHelper.getCurrentPosition().toFloat())
        handler.postDelayed(runnable, 1000)
    }

    inner class SongAdapter : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
        override fun getItemCount(): Int = songsList.size

        override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
            holder.bind(songsList[position], position)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SongViewHolder = SongViewHolder(LayoutInflater
                .from(parent?.context).inflate(R.layout.song_card, parent, false))

        inner class SongViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView), CustomRange.RangeChangeListener {
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
                    itemView.song_card.setOnClickListener {
                        blur.visibility = View.VISIBLE
                        showSongTrimCard()
                    }
                }
            }

            private fun Song.showSongTrimCard() {
                song_trim_card.visibility = View.VISIBLE
                song_trim_card.animate().setInterpolator(interpolator).setListener(null).scaleY(1f).scaleX(1f).start()
                trim_song_name.text = trackName
                song_seek_bar.max = duration.toInt()
                song_trim_range.minValue = 0f
                song_trim_range.maxValue = duration.toFloat()
                song_trim_range.setRangeChangeListener(this@SongViewHolder)
                trim_start_time.text = AppUtil.getMinsAndSecs(0f)
                trim_end_time.text = AppUtil.getMinsAndSecs(duration.toFloat())
                toggle_music.setImageResource(R.drawable.ic_play_arrow)
                song_seek_bar.progress = 0
                song_time.text = getString(R.string.zero)

                mediaHelper.initializeSong(path)
                toggle_music.setOnClickListener {
                    val isPlaying = mediaHelper.isMediaPlayerPlaying()
                    val mediaIcon = if (isPlaying) R.drawable.ic_play_arrow else R.drawable.ic_pause

                    if (isPlaying) {
                        handler.removeCallbacks(runnable)
                    } else {
                        seekUpdation()
                    }

                    toggle_music.setImageResource(mediaIcon)
                    mediaHelper.toggleMediaPlayback()
                }

                close_song_trim.setOnClickListener {
                    closeSongTrimPopup()
                }

                song_seek_bar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mediaHelper.seekTo(progress)
                            seekBar?.progress = progress
                            song_time.text = AppUtil.getMinsAndSecs(progress.toFloat())
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })

                trim.setOnClickListener {
                    closeSongTrimPopup()
                    val start: String = AppUtil.getTime(song_trim_range.startValue)
                    val difference: String = AppUtil.getTime(song_trim_range.endValue - song_trim_range.startValue)
                    editFile?.trimSong(File(Uri.parse(path).path), start, difference)
                }
            }

            override fun onRangeChanged(startValue: Float, endValue: Float) {
                trim_start_time.text = AppUtil.getMinsAndSecs(startValue)
                trim_end_time.text = AppUtil.getMinsAndSecs(endValue)
            }
        }

    }
}
