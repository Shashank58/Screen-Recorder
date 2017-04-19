package shashank.com.screenrecorder

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_videos.*
import kotlinx.android.synthetic.main.video_card.view.*
import org.jetbrains.anko.intentFor
import java.io.File
import java.util.*

class VideosActivity : AppCompatActivity() {
    private var adapter: VideoAdapter? = null
    private var editVideo: EditVideoContract? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        video_list.layoutManager = GridLayoutManager(this, 3)
        adapter = VideoAdapter(VideoHelper().getVideos(this))
        editVideo = EditVideoUtils(this)

        video_list.adapter = adapter
    }

    inner class VideoAdapter(val videoList: MutableList<Video>) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

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
                        startActivity(intentFor<EditVideoActivity>("data" to video.data, "duration" to duration))
                    }
                }
            }
        }
    }
}
