package shashank.com.screenrecorder;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.VideoView;

/**
 * Created by shashankm on 23/03/17.
 */

public class EditVideoActivity extends AppCompatActivity {
    private Handler handler = new Handler();

    private VideoView video;
    private SeekBar seekBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        String data = getIntent().getStringExtra("data");
        video = (VideoView) findViewById(R.id.video);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);

        video.setVideoURI(Uri.parse(data));
        video.start();
        seekBar.setMax((int) getIntent().getLongExtra("duration", 0));
        seekUpdation();

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
