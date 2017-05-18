package shashank.com.screenrecorder;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;


/**
 * Created by shashankm on 23/03/17.
 */

public class EditVideoActivity extends AppCompatActivity implements CustomRange.RangeChangeListener,
    View.OnClickListener, EditVideoContract.Response {
    private static final String TAG = EditVideoActivity.class.getSimpleName();
    private Handler handler = new Handler();

    private VideoView video;
    private SeekBar seekBar;
    private CustomRange rangePicker;
    private ImageView playPause;
    private TextView startTime, endTime, videoCurrentTime;
    private final Handler hideHandler = new Handler();
    private String data;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        data = getIntent().getStringExtra("data");
        video = (VideoView) findViewById(R.id.video);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        rangePicker = (CustomRange) findViewById(R.id.video_range_picker);
        playPause = (ImageView) findViewById(R.id.play_pause);
        startTime = (TextView) findViewById(R.id.start_time);
        endTime = (TextView) findViewById(R.id.end_time);

        videoCurrentTime = (TextView) findViewById(R.id.video_current_time);
        View videoContainer = findViewById(R.id.video_container);
        View trimVideo = findViewById(R.id.trim_video);
        View back = findViewById(R.id.back);

        int duration = (int) getIntent().getLongExtra("duration", 0);

        int maxFileDuration = 1000 * 60 * 5; // 5 minutes
        if (duration > maxFileDuration) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Error!")
                    .setMessage("The file size is too large. It's gonna take forever to trim. Have mercy and pick a smaller file")
                    .setPositiveButton("Cool", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
            return;
        }

        video.setVideoURI(Uri.parse(data));
        video.start();
        seekBar.setMax(duration);
        rangePicker.setMinValue(0);
        rangePicker.setMaxValue(duration);
        rangePicker.setRangeChangeListener(this);

        startTime.setText(AppUtil.INSTANCE.getMinsAndSecs(0f));
        endTime.setText(AppUtil.INSTANCE.getMinsAndSecs(duration));

        seekUpdation();
        hidePlayPause();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    video.seekTo(progress);
                    seekBar.setProgress(progress);
                    videoCurrentTime.setText(AppUtil.INSTANCE.getMinsAndSecs(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        videoContainer.setOnClickListener(this);
        trimVideo.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void hidePlayPause() {
        hideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playPause.setVisibility(View.GONE);
            }
        }, 3000);
    }

    @Override
    public void onRangeChanged(float startValue, float endValue) {
        int startInt = (int) startValue;
        seekBar.setProgress(startInt);
        video.seekTo(startInt);
        video.pause();
        startTime.setText(AppUtil.INSTANCE.getMinsAndSecs(startValue));
        endTime.setText(AppUtil.INSTANCE.getMinsAndSecs(endValue));
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    private void seekUpdation() {
        seekBar.setProgress(video.getCurrentPosition());
        videoCurrentTime.setText(AppUtil.INSTANCE.getMinsAndSecs(video.getCurrentPosition()));
        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_container:
                playPause.setVisibility(View.VISIBLE);
                if (video.isPlaying()) {
                    video.pause();
                    playPause.setImageResource(R.drawable.ic_play_arrow);
                } else {
                    video.start();
                    playPause.setImageResource(R.drawable.ic_pause);
                    hidePlayPause();
                }
                break;

            case R.id.trim_video:
                FfmpegUtil ffmpegUtil = new FfmpegUtil(this, this);

                String startTime = AppUtil.INSTANCE.getTime(rangePicker.getStartValue());
                String endTime = AppUtil.INSTANCE.getTime(rangePicker.getEndValue());
                ffmpegUtil.trimVideo(new File(Uri.parse(data).getPath()), startTime, endTime);
                break;

            case R.id.back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void showProgress(@NonNull String title, @NonNull String message) {
        progressDialog = ProgressDialog.show(this, title, message);
        progressDialog.show();
    }

    @Override
    public void finishedSuccessFully(String path) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your file is successfully saved at " + path + " in your phone")
                .setPositiveButton("Great", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    @Override
    public void onFailure(@NonNull String message) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
