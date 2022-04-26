package im.zego.customrender.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import im.zego.customvideorendering.R;

public class ZGVideoRenderTypeUI extends AppCompatActivity {

    Boolean isRGB = false;
    Boolean isRawData = true;

    // load c++ so
    static {
        System.loadLibrary("nativeCutPlane1");
    }

    EditText editPublishStreamID;
    EditText editPlayStreamID;
    EditText editRoomID;
    Spinner frameTypeButton;
    Spinner frameFormatButton;
    Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_render_selection);
        setUI();
        requestPermission();
        setFrameTypeButton();
        setFrameFormatButton();
        setStartButton();
    }
    public void setUI() {
        editPublishStreamID = findViewById(R.id.editPublishStreamID);
        editPlayStreamID = findViewById(R.id.editPlayStreamID);
        editRoomID = findViewById(R.id.editRoomID);
        frameTypeButton = findViewById(R.id.FrameTypeButton);
        frameFormatButton = findViewById(R.id.FrameFormatButton);
        startButton = findViewById(R.id.startBtn);
        setTitle(getString(R.string.custom_video_rendering));
    }
    public void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
            }
        }
    }
    public void setFrameTypeButton(){
        frameTypeButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] ViewOptions = getApplicationContext().getResources().getStringArray(R.array.bufferType);
                switch (ViewOptions[position]) {
                    case "Raw_Data":
                        isRawData = true;
                        break;
                    case "Encoded_Data":
                        isRawData = false;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setFrameFormatButton(){
        frameFormatButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] ViewOptions = getApplicationContext().getResources().getStringArray(R.array.frameFormat);
                switch (ViewOptions[position]) {
                    case "RGB":
                        isRGB = true;
                        break;
                    case "YUV":
                        isRGB = false;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setStartButton(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent temp= new Intent(ZGVideoRenderTypeUI.this,VideoRenderPublish.class);
                temp.putExtra("isRGB",isRGB);
                temp.putExtra("isRawData",isRawData);
                temp.putExtra("roomID",editRoomID.getText().toString());
                temp.putExtra("playStreamID",editPlayStreamID.getText().toString());
                temp.putExtra("publishStreamID",editPublishStreamID.getText().toString());
                startActivity(temp);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, ZGVideoRenderTypeUI.class);
        activity.startActivity(intent);
    }
}