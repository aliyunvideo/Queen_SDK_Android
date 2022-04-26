package com.example.others.streammixing;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoCDNConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoMixerAudioConfig;
import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerOutput;
import im.zego.zegoexpress.entity.ZegoAutoMixerTask;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoStream;

public class AutoMixerStartActivity extends AppCompatActivity implements IMixerStreamUpdateHandler {
    private static String mixStreamID = "mix_0025";
    private ZegoAutoMixerTask currentMixTask ;
    private boolean isMixing = false;
    private boolean isPlayAutoMixStream = false;
    private Button button_startAutoMixing;
    private Button startPlayingAutoMixButton;
    private EditText editAutoMixPlayStreamID;
    private EditText editAutoMixPlayUrl;
    private EditText editAutoMixTaskID;
    private EditText editAutoMixOut;
    private String playAutoMixStreamID;
    ListView streamList;
    ArrayList<String> streamSoundLevels = new ArrayList<>();
    ArrayAdapter<String> adapter;
    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_mixer_start);
        bindView();
        setLogComponent();
        setUI();
        setLayout();

        MixerMainActivity.registerStreamUpdateNotify(this);
    }

    private void setLayout() {
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, streamSoundLevels) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
//                TextView text = view.findViewById(android.R.id.text1);
//                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        streamList.setAdapter(adapter);

        onRoomStreamUpdate();
    }

    private void bindView() {
        button_startAutoMixing = findViewById(R.id.btn_auto_mix_start);
        startPlayingAutoMixButton = findViewById(R.id.btn_auto_mix_play);
        editAutoMixPlayStreamID = findViewById(R.id.text_auto_mix_play_stream_id);
        editAutoMixPlayUrl = findViewById(R.id.text_auto_mix_play_cdn_url);
        editAutoMixTaskID = findViewById(R.id.text_auto_mix_task_id);
        editAutoMixOut = findViewById(R.id.text_auto_mix_stream_id);
        streamList = findViewById(R.id.streamListView);
    }

    public void setUI(){
        TextView tv_room = findViewById(R.id.tv_auto_mix_room_id);
        tv_room.setText(MixerMainActivity.roomID);
        editAutoMixOut.setText("mix_0025");
        editAutoMixTaskID.setText("0025");
        editAutoMixPlayStreamID.setText("mix_0025");
        setTitle(getString(R.string.auto_stream_mixing));
    }
    @Override
    protected void onDestroy() {
        //stop task
        if(isMixing)
        {
            stopAutoMixing();
        }
        super.onDestroy();
        MixerMainActivity.registerStreamUpdateNotify(null);
    }

    // Start auto stream mixing
    public void startAutoMixing()
    {
        int count = 0;

        ZegoAutoMixerTask task = new ZegoAutoMixerTask();

        task.taskID = editAutoMixTaskID.getText().toString();//"task1";
        task.roomID = "0025";
        mixStreamID = editAutoMixOut.getText().toString();
        ArrayList<ZegoMixerOutput> outputList = new ArrayList<>();
        ZegoMixerOutput output = new ZegoMixerOutput(mixStreamID);
        ZegoMixerAudioConfig audioConfig = new ZegoMixerAudioConfig();

        task.audioConfig = audioConfig;
        outputList.add(output);
        task.enableSoundLevel = true;
        task.outputList = outputList;

        button_startAutoMixing.setEnabled(false);

        MixerMainActivity.engine.startAutoMixerTask(task, new IZegoMixerStartCallback() {

            @Override
            public void onMixerStartResult(int errorCode, JSONObject var2) {
                button_startAutoMixing.setEnabled(true);
                currentMixTask = task;
                AppLogger.getInstance().receiveCallback("onAutoMixerStartResult: result = " + errorCode);
                if (errorCode != 0) {
                    String msg = getString(R.string.tx_mixer_start_fail) + errorCode;
                    Toast.makeText(AutoMixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();

                    button_startAutoMixing.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.start_auto_stream_mixing));
                }
                else {
                    isMixing = true;
                    String msg = getString(R.string.tx_mixer_start_ok);
                    Toast.makeText(AutoMixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();

                    button_startAutoMixing.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.stop_auto_stream_mixing));
                }
            }
        });
    }

    // Stop auto stream mixing
    public void stopAutoMixing()
    {
        button_startAutoMixing.setEnabled(false);

        // Stop task
        MixerMainActivity.engine.stopAutoMixerTask(currentMixTask, new IZegoMixerStopCallback() {
            @Override
            public void onMixerStopResult(int i) {
                button_startAutoMixing.setEnabled(true);
                AppLogger.getInstance().receiveCallback("onAutoMixerStartResult: result = " + i);
                if (i != 0) {
                    String msg = getString(R.string.tx_mixer_stop_fail) + i;
                    Toast.makeText(AutoMixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    button_startAutoMixing.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.stop_auto_stream_mixing));
                }
                else {
                    isMixing = false;
                    String msg = getString(R.string.tx_mixer_stop_ok);
                    Toast.makeText(AutoMixerStartActivity.this, msg, Toast.LENGTH_SHORT).show();
                    button_startAutoMixing.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.start_auto_stream_mixing));
                }
            }
        });
    }

    public void ClickStartAutoMix(View view) {
        if(isMixing == false)
        {
            startAutoMixing();
        }
        else
        {
            stopAutoMixing();
        }
    }

    public void ClickStartPlayAutoMix(View view) {
        if (!isPlayAutoMixStream) {
            playAutoMixStreamID = editAutoMixPlayStreamID.getText().toString();
            String playAutoMixCdnUrl = editAutoMixPlayUrl.getText().toString();
            ZegoPlayerConfig config = new ZegoPlayerConfig();
            ZegoCDNConfig cdn_config = new ZegoCDNConfig();
            cdn_config.url = playAutoMixCdnUrl;
            if(!cdn_config.url.isEmpty())
            {
                config.cdnConfig = cdn_config;
                config.resourceMode = ZegoStreamResourceMode.ONLY_CDN;
            }

            // Start publishing stream
            editAutoMixPlayStreamID.setEnabled(false);
            editAutoMixPlayUrl.setEnabled(false);
            MixerMainActivity.engine.startPlayingStream(playAutoMixStreamID,null, config);
            AppLogger.getInstance().callApi("Start Playing Stream:%s, CDN url:%s",playAutoMixStreamID, playAutoMixCdnUrl);
            startPlayingAutoMixButton.setText(getResources().getString(R.string.stop_playing));
            isPlayAutoMixStream = true;
        } else {
            editAutoMixPlayStreamID.setEnabled(true);
            editAutoMixPlayUrl.setEnabled(true);
            MixerMainActivity.engine.stopPlayingStream(playAutoMixStreamID);
            isPlayAutoMixStream = false;
            startPlayingAutoMixButton.setText(getResources().getString(R.string.start_playing));
            AppLogger.getInstance().callApi("Stop Playing Stream:%s",playAutoMixStreamID);
        }
    }

    public void onRoomStreamUpdate() {
        TextView streamListTitle = findViewById(R.id.streamListTitle);
        streamListTitle.setText(String.format("streamList(%d)", MixerMainActivity.streamInfoList.size()));
        streamSoundLevels.clear();
        for(ZegoStream stream : MixerMainActivity.streamInfoList)
        {
            String soundLevelInfo = String.format("userID:%s streamID:%s soundLevel:%s", stream.user.userID, stream.streamID, stream.extraInfo);
            streamSoundLevels.add(soundLevelInfo);
        }
        adapter.notifyDataSetChanged();
    }

    public void onAutoSoundLevelUpdate() {
        streamSoundLevels.clear();
        for(ZegoStream stream : MixerMainActivity.streamInfoList)
        {
            String soundLevelInfo = String.format("userID:%s streamID:%s soundLevel:%s", stream.user.userID, stream.streamID, stream.extraInfo);
            streamSoundLevels.add(soundLevelInfo);
        }
        adapter.notifyDataSetChanged();
    }

    public void ClickStopAutoWatch(View view) {
        MixerMainActivity.engine.stopPlayingStream(mixStreamID);
        AppLogger.getInstance().callApi("Stop Playing Stream:",mixStreamID);
        Button step2 = findViewById(R.id.btn_auto_mix_start);
        step2.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.tx_mixer_step2));
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, AutoMixerStartActivity.class);
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

    // Stop auto mix streaming
    public void ClickStopAutoMix(View view) {

    }
}
