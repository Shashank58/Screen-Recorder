package shashank.com.screenrecorder;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;


/**
 * Created by shashankm on 23/03/17.
 */

public class EditVideoActivity extends AppCompatActivity implements CustomRange.RangeChangeListener {
    private Handler handler = new Handler();

    private VideoView video;
    private SeekBar seekBar;
    private CustomRange rangePicker;
    private ImageView playPause;
    private final Handler hideHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        String data = getIntent().getStringExtra("data");
        video = (VideoView) findViewById(R.id.video);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        rangePicker = (CustomRange) findViewById(R.id.video_range_picker);
        playPause = (ImageView) findViewById(R.id.play_pause);
        View videoContainer = findViewById(R.id.video_container);

        int duration = (int) getIntent().getLongExtra("duration", 0);
        video.setVideoURI(Uri.parse(data));
        video.start();
        seekBar.setMax(duration);
        rangePicker.setMinValue(0);
        rangePicker.setMaxValue(duration);
        rangePicker.setRangeChangeListener(this);

        seekUpdation();
        hidePlayPause();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    video.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        videoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause.setVisibility(View.VISIBLE);
                if (video.isPlaying()) {
                    video.pause();
                    playPause.setImageResource(R.drawable.ic_play_arrow);
                } else {
                    video.start();
                    playPause.setImageResource(R.drawable.ic_pause);
                    hidePlayPause();
                }
            }
        });
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
        seekBar.setProgress((int) startValue);
        video.seekTo((int) startValue);
        video.pause();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    private void seekUpdation() {
        seekBar.setProgress(video.getCurrentPosition());
        handler.postDelayed(runnable, 1000);
    }
}
