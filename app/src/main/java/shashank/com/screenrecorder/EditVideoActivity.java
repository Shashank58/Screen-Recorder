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
        seekBar.setMax(video.getDuration());
        handler.postDelayed(runnable, 100);
        video.start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(video.getCurrentPosition());
            handler.postDelayed(this, 100);
        }
    };
}
