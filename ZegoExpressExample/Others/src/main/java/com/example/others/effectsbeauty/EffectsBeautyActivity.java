package com.example.others.effectsbeauty;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEffectsBeautyParam;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class EffectsBeautyActivity extends AppCompatActivity {

    TextureView preview;
    TextView roomState;

    SwitchMaterial effectsBeautySwitch;
    SeekBar whitenSeekBar;
    SeekBar rosySeekBar;
    SeekBar smoothSeekBar;
    SeekBar sharpenSeekBar;
    TextView whitenValue;
    TextView rosyValue;
    TextView smoothValue;
    TextView sharpenValue;
    Button startPublishingButton;

    EditText encodeResolutionWidth;
    EditText encodeResolutionHeight;
    EditText fps;
    EditText bitrate;

    ZegoVideoConfig videoConfig;

    String userID;
    String streamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    ZegoEffectsBeautyParam param;

    //Store whether the user is publishing the stream
    Boolean isPublish = false;

    //Jump to teaching document
    TextView document;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    // request for permission
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects_beauty);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        requestPermission();
        setStartPublishButtonEvent();
        setEffectsBeautyEvent();
        setLogComponent();
        addEditListener();
        setApiCalledResult();
        setEventHandler();
        addWaterMarkInfoAndUrl();
    }
    public void bindView(){
        preview = findViewById(R.id.textureView);

        encodeResolutionWidth = findViewById(R.id.encodeResolutionWidth);
        encodeResolutionHeight = findViewById(R.id.encodeResolutionHeight);
        fps = findViewById(R.id.videoFps);
        bitrate = findViewById(R.id.videoBitrate);

        effectsBeautySwitch = findViewById(R.id.effectsBeautySwitch);
        whitenSeekBar = findViewById(R.id.whitenSeekBar);
        rosySeekBar = findViewById(R.id.rosySeekBar);
        smoothSeekBar = findViewById(R.id.smoothSeekBar);
        sharpenSeekBar = findViewById(R.id.sharpenSeekBar);
        whitenValue = findViewById(R.id.whitenValueTextView);
        rosyValue = findViewById(R.id.rosyValueTextView);
        smoothValue = findViewById(R.id.smoothValueTextView);
        sharpenValue = findViewById(R.id.sharpenValueTextView);
        startPublishingButton = findViewById(R.id.startPublishButton);

        roomState = findViewById(R.id.roomState);
        document = findViewById(R.id.document);
    }
    public void setDefaultValue(){
        roomID = "0024";
        streamID = "0024";
        //set the default video configuration
        videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);

        param = new ZegoEffectsBeautyParam();
        param.whitenIntensity = 50;
        param.rosyIntensity = 50;
        param.smoothIntensity = 50;
        param.sharpenIntensity = 50;
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

        // 初始化 Effects 美颜环境
        engine.startEffectsEnv();

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
                    engine.logoutRoom();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;

                } else {

                    // get configuration set by the user

                    if (encodeResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeWidth = Integer.parseInt(encodeResolutionWidth.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (encodeResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeHeight = Integer.parseInt(encodeResolutionHeight.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (fps.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "FPS cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.fps = Integer.parseInt(fps.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "FPS is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (bitrate.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.bitrate = Integer.parseInt(bitrate.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Bitrate is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // set video configuration
                    ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);

                    loginRoom();
                    engine.startPreview(new ZegoCanvas(preview));
                    engine.startPublishingStream(streamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
    }

    public void setEffectsBeautyEvent() {
        effectsBeautySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                engine.enableEffectsBeauty(isChecked);
            }
        });

        whitenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                whitenValue.setText(String.valueOf(progress));
                param.whitenIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        rosySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rosyValue.setText(String.valueOf(progress));
                param.rosyIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        smoothSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                smoothValue.setText(String.valueOf(progress));
                param.smoothIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sharpenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sharpenValue.setText(String.valueOf(progress));
                param.sharpenIntensity = progress;
                engine.setEffectsBeautyParam(param);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
                if (errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
                    }
                }
            }
        });
    }

    //Add EditText listener in order to configure fps and bitrate
    private void addEditListener() {
        fps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (fps.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "FPS cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    videoConfig.fps = Integer.parseInt(fps.getText().toString());
                } catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(), "FPS is too large", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // set video configuration
                ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                AppLogger.getInstance().callApi("Change FPS");
            }
        });
        bitrate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (bitrate.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Bitrate cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    videoConfig.bitrate = Integer.parseInt(bitrate.getText().toString());
                } catch (NumberFormatException e)
                {
                    Toast.makeText(getApplicationContext(), "Bitrate is too large", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // set video configuration
                ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                AppLogger.getInstance().callApi("Change bitrate");
            }
        });
    }

    public void addWaterMarkInfoAndUrl(){
        //add an underline and url jump to the website
        document.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG );
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://doc-zh.zego.im/article/11257");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
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

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, EffectsBeautyActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        //log out the room
        engine.logoutRoom(roomID);
        //destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}
