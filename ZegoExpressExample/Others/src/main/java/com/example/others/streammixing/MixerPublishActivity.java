package com.example.others.streammixing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.others.R;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoCanvas;

public class MixerPublishActivity extends AppCompatActivity {
    boolean publishMicEnable = true;
    boolean publishCameraEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer_publish);
        setUI();
        setLogComponent();
    }
    public void setUI(){
        TextView tv_room = findViewById(R.id.tv_room_id2);
        tv_room.setText(MixerMainActivity.roomID);
        setTitle(getString(R.string.stream_mixing));
    }
    public void ClickPublish(View view) {
        TextureView v = findViewById(R.id.tv_preview);
        ZegoCanvas canvas = new ZegoCanvas(v);
        MixerMainActivity.engine.startPreview(canvas);
        EditText et_stream = findViewById(R.id.et_stream_id);
        MixerMainActivity.engine.startPublishingStream(et_stream.getText().toString());
        AppLogger.getInstance().receiveCallback("Start publish stream，streamID = " + et_stream.getText().toString());
    }

    public void ClickStopPublish(View view) {
        MixerMainActivity.engine.stopPublishingStream();
        MixerMainActivity.engine.stopPreview();
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MixerPublishActivity.class);
        activity.startActivity(intent);
    }

    public void enableLocalMic(View view) {
        ImageButton ib_local_mic = findViewById(R.id.ib_local_mic);
        publishMicEnable = !publishMicEnable;

        if (publishMicEnable) {
            ib_local_mic.setImageDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_on));
        } else {
            ib_local_mic.setImageDrawable(getResources().getDrawable(R.drawable.ic_bottom_microphone_off));
        }

        /* Enable Mic*/
        MixerMainActivity.engine.muteMicrophone(!publishMicEnable);
    }

    public void enableLocalCamera(View view) {
        ImageButton ib_local_camera = findViewById(R.id.ib_local_camera);
        publishCameraEnable = !publishCameraEnable;

        if (publishCameraEnable) {
            ib_local_camera.setImageDrawable(getResources().getDrawable(R.drawable.bottom_camera_on_icon));
        } else {
            ib_local_camera.setImageDrawable(getResources().getDrawable(R.drawable.bottom_camera_off_icon));
        }
        /* Enable Mic*/
        MixerMainActivity.engine.enableCamera(publishCameraEnable);
    }
    // Set log component. It includes a pop-up dialog.
    public void setLogComponent(){
        logLinearLayout logHiddenView = findViewById(R.id.logView);
        logHiddenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogView logview = new LogView(getApplicationContext());
                logview.show(getSupportFragmentManager(),null);
            }
        });
    }
    // Unicode
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ZegoExpressEngine.getEngine().logoutRoom(MixerMainActivity.roomID);
    }
}
