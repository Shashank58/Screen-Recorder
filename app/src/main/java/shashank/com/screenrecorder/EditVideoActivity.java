package shashank.com.screenrecorder;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;
import java.util.Locale;


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
    private final Handler hideHandler = new Handler();
    private String data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        data = getIntent().getStringExtra("data");
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
        Log.d(TAG, "onCreate: max - " + rangePicker.getMaxValue());

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

        videoContainer.setOnClickListener(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_video_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                EditVideoUtils editVideoUtils = new EditVideoUtils(this, this);

                String startTime = getDate(rangePicker.getStartValue());
                String endTime = getDate(rangePicker.getEndValue());
                Log.d(TAG, "onOptionsItemSelected: " + startTime);
                Log.d(TAG, "onOptionsItemSelected: " + endTime);
                editVideoUtils.trimFile(new File(Uri.parse(data).getPath()), startTime, endTime);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private String getDate(float value) {
        int secs = (int) (value / 1000);
        int mins = secs / 60;
        int hours = mins / 60;
        return String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(),
                "%02d", mins) + ":" + String.format(Locale.getDefault(), "%02d", secs % 60);
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
        }
    }

    @Override
    public void showProgress(@NonNull String title, @NonNull String message) {

    }

    @Override
    public void finishedSuccessFully() {

    }

    @Override
    public void onFailure(@NonNull String message) {

    }
}
