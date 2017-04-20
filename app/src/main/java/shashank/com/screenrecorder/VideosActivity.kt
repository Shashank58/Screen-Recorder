package shashank.com.screenrecorder

import android.animation.Animator
import android.app.ProgressDialog
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

class VideosActivity : AppCompatActivity(), EditVideoContract.Response {
    private var adapter: VideoAdapter? = null
    private var editVideo: EditVideoContract? = null
    private var purpose: Int = 0
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        video_list.layoutManager = GridLayoutManager(this, 3) as RecyclerView.LayoutManager?
        adapter = VideoAdapter(VideoHelper().getVideos(this))
        editVideo = EditVideoUtils(this, this)

        purpose = intent.getIntExtra("purpose", 0)

        video_list.adapter = adapter
    }

    override fun finishedSuccessFully() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun onFailure(message: String) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    override fun showProgress(title: String, message: String) {
        progressDialog = ProgressDialog.show(this, title, message)
        progressDialog?.show()
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
                        if (purpose == 0) {
                            startActivity(intentFor<EditVideoActivity>("data" to video.data, "duration" to duration))
                        } else {
                            quality_popup.visibility = View.VISIBLE
                            quality_popup.animate().setListener(null).scaleY(1f).scaleX(1f).start()
                            blur.visibility = View.VISIBLE

                            blur.setOnClickListener {
                                hidePopup(null, null)
                            }

                            low.setOnClickListener {
                                hidePopup("8", video.data)
                            }

                            medium.setOnClickListener {
                                hidePopup("16", video.data)
                            }

                            high.setOnClickListener {
                                hidePopup("30", video.data)
                            }

                            very_high.setOnClickListener {
                                hidePopup("60", video.data)
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

                        })
                        .scaleY(0f).scaleX(0f).start()

            }
        }
    }
}