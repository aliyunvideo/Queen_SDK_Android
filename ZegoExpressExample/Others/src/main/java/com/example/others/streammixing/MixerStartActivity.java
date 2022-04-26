package com.example.others.streammixing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.others.R;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.constants.ZegoMixerInputContentType;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoMixerAudioConfig;
import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerOutput;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import im.zego.zegoexpress.entity.ZegoWatermark;
import im.zego.zegoexpress.entity.ZegoStream;

public class MixerStartActivity extends AppCompatActivity implements IMixerStreamUpdateHandler {

    private static ArrayList<CheckBox> checkBoxList=new ArrayList<CheckBox>();
    private static LinearLayout ll_checkBoxList;
    private static String mixStreamID = "mix_0025";
    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer_start);

        setLogComponent();
        setUI();

        MixerMainActivity.registerStreamUpdateNotify(this);
    }
    public void setUI(){
        ll_checkBoxList = findViewById(R.id.ll_CheckBoxList);
        TextView tv_room = findViewById(R.id.tv_room_id3);
        tv_room.setText(MixerMainActivity.roomID);
        ll_checkBoxList.removeAllViews();
        checkBoxList.clear();
        for(ZegoStream stream: MixerMainActivity.streamInfoList){
            CheckBox checkBox=(CheckBox) View.inflate(this, R.layout.checkbox, null);
            checkBox.setText(stream.streamID);
            ll_checkBoxList.addView(checkBox);
            checkBoxList.add(checkBox);
        }
        setTitle(getString(R.string.stream_mixing));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixerMainActivity.registerStreamUpdateNotify(null);
    }

    private ZegoMixerTask task ;

    public void ClickStartMix(View view) {
        int count = 0;
        String streamID_1 = "";
        String streamID_2 = "";
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isChecked()) {
                count++;
                if (streamID_1.equals("")) {
                    streamID_1 = checkBoxList.get(i).getText().toString();
                }
                else if (streamID_2.equals("")){
                    streamID_2 = checkBoxList.get(i).getText().toString();
                }
            }
        }
        if (count != 2) {
            Toast.makeText(this, getString(R.string.tx_mixer_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        task = new ZegoMixerTask("task1");
        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        ZegoMixerInput input_1 = new ZegoMixerInput(streamID_1, ZegoMixerInputContentType.VIDEO, new Rect(0, 0, 360, 320));
        input_1.soundLevelID = 123;
        input_1.label.text = "zego";
        input_1.label.font.border = true;
        inputList.add(input_1);

        ZegoMixerInput input_2 = new ZegoMixerInput(streamID_2, ZegoMixerInputContentType.VIDEO, new Rect(0, 320, 360, 640));
        input_2.soundLevelID = 1235;
        input_2.label.text = "zego";
        input_2.label.font.border = true;
        input_2.label.font.borderColor = 255;
        inputList.add(input_2);

        ArrayList<ZegoMixerOutput> outputList = new ArrayList<>();
        ZegoMixerOutput output = new ZegoMixerOutput(mixStreamID);
        ZegoMixerAudioConfig audioConfig = new ZegoMixerAudioConfig();
        ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig();
        task.setVideoConfig(videoConfig);
        task.setAudioConfig(audioConfig);
        outputList.add(output);
        task.enableSoundLevel(true);
        task.setInputList(inputList);
        task.setOutputList(outputList);


        ZegoWatermark watermark = new ZegoWatermark("preset-id://zegowp.png", new Rect(0,0,300,200));
        task.setWatermark(watermark);

        task.setBackgroundImageURL("preset-id://zegobg.png");

        MixerMainActivity.engine.startMixerTask(task, new IZegoMixerStartCallback() {

            @Override
            public void onMixerStartResult(int errorCode, JSONObject var2) {
                AppLogger.getInstance().receiveCallback("onMixerStartResult: result = " + errorCode);
                if (errorCode != 0) {
                    String msg = getString(R.string.tx_mixer_start_fail) + errorCode;
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Button step1 = findViewById(R.id.btn_mix_start_mix);
                    step1.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.tx_mixer_step1));
                }
                else {
                    String msg = getString(R.string.tx_mixer_start_ok);
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Button step1 = findViewById(R.id.btn_mix_start_mix);
                    step1.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.tx_mixer_step1));
                }
            }
        });

        ZegoExpressEngine.getEngine().setAudioConfig(new ZegoAudioConfig());
        TextureView tv_play_mix = findViewById(R.id.tv_play_mix);
        ZegoCanvas canvas = new ZegoCanvas(tv_play_mix);
        MixerMainActivity.engine.startPlayingStream(mixStreamID, canvas);
    }

    public void onRoomStreamUpdate() {
        ll_checkBoxList.removeAllViews();
        checkBoxList.clear();
        for(ZegoStream stream: MixerMainActivity.streamInfoList){
            CheckBox checkBox=(CheckBox) View.inflate(this, R.layout.checkbox, null);
            checkBox.setText(stream.streamID);
            ll_checkBoxList.addView(checkBox);
            checkBoxList.add(checkBox);
        }
    }

    public void onAutoSoundLevelUpdate() {}

    public void ClickStopWatch(View view) {
        MixerMainActivity.engine.stopPlayingStream(mixStreamID);
        AppLogger.getInstance().callApi("Stop Playing Stream:",mixStreamID);
        Button step2 = findViewById(R.id.btn_mix_stopwatchstream);
        step2.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.tx_mixer_step2));
    }

    public void ClickStopMix(View view) {
        MixerMainActivity.engine.stopMixerTask(task, new IZegoMixerStopCallback() {
            @Override
            public void onMixerStopResult(int i) {
                AppLogger.getInstance().receiveCallback("onMixerStartResult: result = " + i);
                if (i != 0) {
                    String msg = getString(R.string.tx_mixer_stop_fail) + i;
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Button step3 = findViewById(R.id.btn_mix_stop_mix);
                    step3.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.tx_mixer_step3));
                }
                else {
                    String msg = getString(R.string.tx_mixer_stop_ok);
                    Toast.makeText(MixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    Button step1 = findViewById(R.id.btn_mix_stop_mix);
                    step1.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.tx_mixer_step3));
                }
            }
        });
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MixerStartActivity.class);
        activity.startActivity(intent);
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
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
    // Unicode
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
