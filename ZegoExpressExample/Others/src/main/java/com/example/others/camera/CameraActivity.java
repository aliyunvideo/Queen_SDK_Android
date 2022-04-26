package com.example.others.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
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
import im.zego.zegoexpress.constants.ZegoCameraExposureMode;
import im.zego.zegoexpress.constants.ZegoCameraFocusMode;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoEngineProfile;

public class CameraActivity extends AppCompatActivity {

    EditText editStreamID;
    AppCompatSpinner cameraSelectionSpinner;
    SwitchMaterial cameraFocusSwitch;
    SwitchMaterial cameraExposureSwitch;
    AppCompatSpinner exposureModeSpinner;
    AppCompatSpinner focusModeSpinner;
    SeekBar zoomFactorSeekBar;
    SeekBar exposureCompensationSeekBar;
    TextView zoomFactorValue;
    TextView exposureCompensationValue;
    TextView maxZoomFactorValue;
    TextView roomAndUserID;
    TextView supportFocusState;
    Button startPublishingButton;
    TextureView preview;

    Long appID;
    String roomID = "0028";
    String userID;
    String token;
    String publishStreamID = "0028";
    boolean isPublish = false;
    float zoomMax = 2.0f;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int focusSupported = 0x1F7E2;
    int focusNotSupport = 0x1F534;

    ZegoExpressEngine engine;
    ZegoCanvas previewCanvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        bindView();
        setLogComponent();
        requestPermission();
        getAppIDAndUserIDAndToken();
        setDefaultConfig();
        initEngineAndLogin();
        setCameraSelectionSpinnerEvent();
        setCameraFocusSwitchEvent();
        setCameraExposureSwitchEvent();
        setCameraFocusModeSpinnerEvent();
        setCameraExposureModeSpinnerEvent();
        setCameraZoomFactorSeekBarEvent();
        setCameraExposureCompensationSeekBarEvent();
        setEventHandler();
        setStartPublishingButtonEvent();
        setApiCalledResult();
        setPreviewTouchEvent();
    }
    public void bindView(){
        editStreamID = findViewById(R.id.editStreamID);
        cameraSelectionSpinner = findViewById(R.id.cameraSelectionSpinner);
        cameraFocusSwitch = findViewById(R.id.cameraFocusSwitch);
        cameraExposureSwitch = findViewById(R.id.cameraExposureSwitch);
        focusModeSpinner = findViewById(R.id.focusModeSpinner);
        exposureModeSpinner = findViewById(R.id.exposureModeSpinner);
        zoomFactorSeekBar = findViewById(R.id.zoomSeekBar);
        exposureCompensationSeekBar = findViewById(R.id.exposureCompensationSeekBar);
        zoomFactorValue = findViewById(R.id.zoomFactorValue);
        exposureCompensationValue = findViewById(R.id.exposureCompensationValue);
        maxZoomFactorValue = findViewById(R.id.maxZoomFactor);
        startPublishingButton = findViewById(R.id.startButton);
        supportFocusState = findViewById(R.id.supportFocusState);
        roomAndUserID = findViewById(R.id.roomAndUserIDTitle);
        preview = findViewById(R.id.textureView);
    }

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
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void initEngineAndLogin() {
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        //create the user
        ZegoUser user = new ZegoUser(userID);
        //login room
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        engine.loginRoom(roomID, user, config);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
    }

    public void setDefaultConfig(){
        //set default publish StreamID
        publishStreamID = "0028";
        editStreamID.setText(publishStreamID);
        //set default room ID
        roomID = "0028";

        roomAndUserID.setText("UserID:" + userID + " RoomID:" + roomID);
        //set Zego Canvas
        previewCanvas = new ZegoCanvas(preview);

        previewCanvas.backgroundColor = Color.WHITE;

        cameraFocusSwitch.setChecked(true);
        cameraExposureSwitch.setChecked(true);
        cameraSelectionSpinner.setSelection(1);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setPreviewTouchEvent() {
        preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();
                    if (cameraFocusSwitch.isChecked()) {
                        engine.setCameraFocusPointInPreview(x, y, ZegoPublishChannel.MAIN);
                    }
                    if (cameraExposureSwitch.isChecked()) {
                        engine.setCameraExposurePointInPreview(x, y, ZegoPublishChannel.MAIN);
                    }
                }
                return true;
            }
        });
    }

    public void setCameraSelectionSpinnerEvent(){
        cameraSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.cameraSelection);
                switch (options[position]) {
                    case "Front":
                        engine.useFrontCamera(true);
                        AppLogger.getInstance().callApi("Switch Camera: Front");
                        break;
                    case "Back":
                        engine.useFrontCamera(false);
                        AppLogger.getInstance().callApi("Switch Camera: Back");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setCameraFocusSwitchEvent() {
        cameraFocusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppLogger.getInstance().callApi("Camera Focus On");
                } else {
                    AppLogger.getInstance().callApi("Camera Focus Off");
                }
            }
        });
    }

    public void setCameraExposureSwitchEvent() {
        cameraExposureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppLogger.getInstance().callApi("Camera Exposure On");
                } else {
                    AppLogger.getInstance().callApi("Camera Exposure Off");
                }
            }
        });
    }

    public void setCameraFocusModeSpinnerEvent(){
        focusModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.focusMode);
                switch (options[position]) {
                    case "ContinuousAuto":
                        engine.setCameraFocusMode(ZegoCameraFocusMode.CONTINUOUS_AUTO_FOCUS, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Focus Mode: ContinuousAuto");
                        break;
                    case "Auto":
                        engine.setCameraFocusMode(ZegoCameraFocusMode.AUTO_FOCUS, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Focus Mode: Auto");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setCameraExposureModeSpinnerEvent(){
        exposureModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.exposureMode);
                switch (options[position]) {
                    case "ContinuousAuto":
                        engine.setCameraExposureMode(ZegoCameraExposureMode.CONTINUOUS_AUTO_EXPOSURE, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: ContinuousAuto");
                        break;
                    case "Auto":
                        engine.setCameraExposureMode(ZegoCameraExposureMode.AUTO_EXPOSURE, ZegoPublishChannel.MAIN);
                        AppLogger.getInstance().callApi("Switch Exposure Mode: Auto");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setCameraZoomFactorSeekBarEvent() {
        zoomFactorSeekBar.setProgress(0);
        // Because the range of zoom factor is [1.0, zoomMax]
        // Set the range of seek bar is [0, zoomMax*10]
        zoomFactorSeekBar.setMax((int)(zoomMax*10)-10);
        zoomFactorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float zoomFactor = (float)progress/10+1.0f;
                engine.setCameraZoomFactor(zoomFactor);
                zoomFactorValue.setText(String.valueOf(zoomFactor));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setCameraExposureCompensationSeekBarEvent() {
        exposureCompensationSeekBar.setProgress(0);
        // Because the range of exposure compensation is [-1, 1]
        // Set the range of seek bar is [-10, 10]
        exposureCompensationSeekBar.setMax(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            exposureCompensationSeekBar.setMin(-10);
        }

        exposureCompensationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = (float)progress/10;
                engine.setCameraExposureCompensation(value);
                exposureCompensationValue.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setStartPublishingButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPublish) {
                    // get roomID, stream ID and user ID
                    publishStreamID = editStreamID.getText().toString();
                    // Login the room
                    editStreamID.setEnabled(false);
                    engine.startPreview(previewCanvas);
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    // Logout room
                    AppLogger.getInstance().callApi("Logout Room:%s",roomID);
                    editStreamID.setEnabled(true);
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    isPublish = false;
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getResources().getString(R.string.start_publishing));
                }
            }
        });
    }
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
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
            // The callback triggered when the room connection state changes.
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
            }

            @Override
            public void onPublisherCapturedVideoFirstFrame(ZegoPublishChannel channel) {
                super.onPublisherCapturedVideoFirstFrame(channel);
                zoomMax = engine.getCameraMaxZoomFactor();
                maxZoomFactorValue.setText(String.valueOf("Max: " + zoomMax));
                zoomFactorSeekBar.setMax((int)(zoomMax*10)-10);

                boolean isSupported = engine.isCameraFocusSupported(ZegoPublishChannel.MAIN);
                focusModeSpinner.setEnabled(isSupported);
                if (isSupported) {
                    supportFocusState.setText("Support Focus: " + getEmojiStringByUnicode(focusSupported));
                } else {
                    supportFocusState.setText("Support Focus: " + getEmojiStringByUnicode(focusNotSupport));
                }
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
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivity(intent);
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
    @Override
    protected void onDestroy() {
        // Destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}