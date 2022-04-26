package im.zego.videorotation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import im.zego.commonvideoconfig.R;

public class VideoRotationSelectionActivity extends AppCompatActivity {

    Button publishingButton;
    Button playingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_rotation_selection);

        setTitle(getString(R.string.video_rotation));

        publishingButton = findViewById(R.id.publishStreamButton);
        playingButton = findViewById(R.id.playStreamButton);

        publishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),VideoRotationActivity.class);
                intent.putExtra("type",getString(R.string.publish_stream));
                startActivity(intent);
            }
        });
        playingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),VideoRotationActivity.class);
                intent.putExtra("type",getString(R.string.play_stream));
                startActivity(intent);
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, VideoRotationSelectionActivity.class);
        activity.startActivity(intent);
    }
}