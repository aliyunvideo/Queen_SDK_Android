package com.example.others.flowcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.others.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoTrafficControlMinVideoBitrateMode;
import im.zego.zegoexpress.constants.ZegoTrafficControlProperty;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class FlowControlActivity extends AppCompatActivity {

    TextView userIDText;
    TextureView preview;
    TextureView playView;
    Button startPublishingButton;
    Button startPlayingButton;
    EditText publishStreamIDEdit;
    EditText playStreamIDEdit;
    SwitchMaterial trafficControlSwitch;
    TextView minVideoBitrateText;
    SeekBar minVideoBitrateSeekBar;
    TextView roomState;
    RadioGroup minVideoBitrateModeRadioGroup;

    ZegoTrafficControlMinVideoBitrateMode bitrateMode = ZegoTrafficControlMinVideoBitrateMode.NO_VIDEO;
    String userID;
    String publishStreamID;
    String playStreamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    //Store whether the user is playing the stream
    Boolean isPlay = false;
    //Store whether the user is publishing the stream
    Boolean isPublish = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_control);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        setLogComponent();
        initEngineAndUser();
        setEventHandler();
        setApiCalledResult();
        loginRoom();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setTrafficControlSwitchEvent();
        setMinVideoBitrateModeRadioGroup();
        setMinVideoBitrateSeekBar();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userIDText);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        publishStreamIDEdit = findViewById(R.id.editPublishStreamID);
        playStreamIDEdit = findViewById(R.id.editPlayStreamID);
        trafficControlSwitch = findViewById(R.id.trafficControlSwitch);
        minVideoBitrateText = findViewById(R.id.minVideoBitrateText);
        minVideoBitrateSeekBar = findViewById(R.id.minVideoBitrateSeekBar);
        minVideoBitrateModeRadioGroup = findViewById(R.id.minVideoBitrateMode);
        roomState = findViewById(R.id.roomState);
    }
    public void setDefaultValue(){
        roomID = "0030";
        publishStreamID = "0030";
        playStreamID = "0030";
        userIDText.setText(userID);
        setTitle(getString(R.string.flow_control));
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        //create the user
        user = new ZegoUser(userID);
    }
    public void loginRoom(){
        //login room
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        engine.loginRoom(roomID, user, config);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;
                } else {
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    engine.startPreview(new ZegoCanvas(preview));
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (isPlay){
                    engine.stopPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    startPlayingButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    playStreamID = playStreamIDEdit.getText().toString();
                    engine.startPlayingStream(playStreamID, new ZegoCanvas(playView));
                    startPlayingButton.setText(getString(R.string.stop_playing));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    isPlay = true;
                }
            }
        });
    }
    public void setTrafficControlSwitchEvent(){
        trafficControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                engine.enableTrafficControl(isChecked, ZegoTrafficControlProperty.BASIC.value());
            }
        });
    }
    public void setMinVideoBitrateSeekBar(){
        minVideoBitrateSeekBar.setProgress(0);
        minVideoBitrateSeekBar.setMax(500);
        minVideoBitrateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setMinVideoBitrate(progress,bitrateMode);
                minVideoBitrateText.setText(progress+"kbps");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setMinVideoBitrateModeRadioGroup(){
        minVideoBitrateModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.minVideoBitrateMode1) {
                    bitrateMode = ZegoTrafficControlMinVideoBitrateMode.NO_VIDEO;
                } else {
                    bitrateMode = ZegoTrafficControlMinVideoBitrateMode.ULTRA_LOW_FPS;
                }
                setMinVideoBitrate(minVideoBitrateSeekBar.getProgress(),bitrateMode);
            }
        });
    }
    public void setMinVideoBitrate(int bitrate, ZegoTrafficControlMinVideoBitrateMode mode){
        engine.setMinVideoBitrateForTrafficControl(bitrate,mode);
    }
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            // The callback triggered when the room connection state changes.
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                // Update room state
                if (state.equals(ZegoRoomState.CONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                } else if (state.equals(ZegoRoomState.DISCONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
                }
            }
            // The callback triggered when the state of stream publishing changes.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
                    }
                }
            }
            // The callback triggered when the state of stream playing changes.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PLAYER_STATE_NO_PLAY and the errcode is not 0, it means that stream playing has failed and
                // no more retry will be attempted by the engine. At this point, the failure of stream playing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                    if (isPlay) {
                        startPlayingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        startPlayingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                    }
                }
            }

        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, FlowControlActivity.class);
        activity.startActivity(intent);
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
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
    public void setApiCalledResult(){
        // Update log with api called results
        ZegoExpressEngine.setApiCalledCallback(new IZegoApiCalledEventHandler() {
            @Override
            public void onApiCalledResult(int errorCode, String funcName, String info) {
                super.onApiCalledResult(errorCode, funcName, info);
                if (errorCode == 0){
                    AppLogger.getInstance().success("[%s]:%s", funcName, info);
                } else {
                    AppLogger.getInstance().fail("[%d]%s:%s", errorCode, funcName, info);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}