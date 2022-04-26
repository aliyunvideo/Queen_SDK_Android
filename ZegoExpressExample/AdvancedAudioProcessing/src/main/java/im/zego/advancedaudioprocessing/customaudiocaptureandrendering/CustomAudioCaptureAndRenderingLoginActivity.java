package im.zego.advancedaudioprocessing.customaudiocaptureandrendering;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import im.zego.advancedaudioprocessing.R;

public class CustomAudioCaptureAndRenderingLoginActivity extends AppCompatActivity {

    EditText roomIDEdit;
    EditText publishStreamIDEdit;
    EditText playStreamIDEdit;
    RadioGroup audioSourceRadioGroup;
    SwitchMaterial customCaptureSwitch;
    Button startButton;

    boolean isMicrophone = false;
    boolean enableCustomCapture = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_audio_capture_and_rendering_login);
        bindView();
        setAudioSourceRadioGroup();
        setCustomCaptureSwitch();
        setStartButton();

        setTitle(getString(R.string.custom_audio_capture_and_render));
    }
    public void bindView(){
        roomIDEdit = findViewById(R.id.roomIDEdit);
        publishStreamIDEdit = findViewById(R.id.publishStreamIDEdit);
        playStreamIDEdit = findViewById(R.id.playStreamIDEdit);
        audioSourceRadioGroup = findViewById(R.id.audioSourceRadioGroup);
        customCaptureSwitch = findViewById(R.id.customAudioCaptureSwitch);
        startButton = findViewById(R.id.startButton);
    }
    public void setAudioSourceRadioGroup(){
        audioSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.localMediaButton){
                    isMicrophone = false;
                } else {
                    isMicrophone = true;
                }
            }
        });
    }
    public void setCustomCaptureSwitch(){
        customCaptureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableCustomCapture = isChecked;
            }
        });
    }
    public void setStartButton(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CustomAudioCaptureAndRenderingActivity.class);
                intent.putExtra("roomID",roomIDEdit.getText().toString());
                intent.putExtra("publishStreamID",publishStreamIDEdit.getText().toString());
                intent.putExtra("playStreamID",playStreamIDEdit.getText().toString());
                intent.putExtra("isMicrophone",isMicrophone);
                intent.putExtra("enableCustomCapture",enableCustomCapture);
                startActivity(intent);
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, CustomAudioCaptureAndRenderingLoginActivity.class);
        activity.startActivity(intent);
    }
}