package im.zego.quickstart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

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
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class Publishing extends AppCompatActivity {

    EditText editRoomID;
    EditText editUserID;
    EditText editStreamID;
    AppCompatSpinner mirrorSpinner;
    AppCompatSpinner viewModeSpinner;
    AppCompatSpinner cameraSpinner;
    SwitchMaterial cameraSwitch;
    SwitchMaterial microphoneSwitch;
    Button startPublishingButton;
    TextureView preview;
    TextView roomState;

    Long appID;
    String token;
    String roomID;
    String userID;
    String publishStreamID;
    boolean isPublish = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    ZegoExpressEngine engine;
    ZegoCanvas previewCanvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publishing);
        bindView();
        setLogComponent();
        requestPermission();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        setDefaultConfig();
        setMirrorModeEvent();
        setViewModeEvent();
        setCameraSwitchEvent();
        setCameraSpinnerEvent();
        setMicrophoneSwitchEvent();
        setEventHandler();
        setStartPublishingButtonEvent();
        setApiCalledResult();
    }
    public void bindView(){
        editRoomID = findViewById(R.id.editRoomID);
        editUserID = findViewById(R.id.editUserID);
        editStreamID = findViewById(R.id.editStreamID);
        mirrorSpinner = findViewById(R.id.mirrorSpinner);
        viewModeSpinner = findViewById(R.id.viewModeSpinner);
        cameraSpinner = findViewById(R.id.cameraSpinner);
        cameraSwitch = findViewById(R.id.cameraSwitch);
        microphoneSwitch = findViewById(R.id.microphoneSwitch);
        startPublishingButton = findViewById(R.id.startButton);
        preview = findViewById(R.id.textureView);
        roomState = findViewById(R.id.roomState);
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
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);
    }
    public void loginRoom(){
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
        publishStreamID = "0002";
        //set default room ID
        roomID = "0002";
        //set Zego Canvas
        previewCanvas = new ZegoCanvas(preview);

        previewCanvas.backgroundColor = Color.WHITE;
        editUserID.setText(userID);
        editUserID.setEnabled(false);
        cameraSwitch.setChecked(true);
        setTitle(getString(R.string.publishing));
    }
    //set the mirror mode
    public void setMirrorModeEvent(){
        mirrorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.mirrorMode);
                switch (options[position]){
                    case "OnlyPreview":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR");
                        break;
                    case "OnlyPublish":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR");
                        break;
                    case "Both":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.BOTH_MIRROR");
                        break;
                    case "None":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.NO_MIRROR");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    // Set view mode
    public void setViewModeEvent(){
         viewModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.viewMode);
                if (isPublish) {
                    // To set the view mode, preview should be stopped firstly.
                    engine.stopPreview();
                }
                switch (options[position]) {
                    case "AspectFit":
                        previewCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
                        AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FIT");
                        break;
                    case "AspectFill":
                        previewCanvas.viewMode = ZegoViewMode.ASPECT_FILL;
                        AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FILL");
                        break;
                    case "ScaleToFill":
                        previewCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
                        AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.SCALE_TO_FILL");
                        break;
                }
                if (isPublish) {
                    // Restart preview.
                    engine.startPreview(previewCanvas);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    // Set camera switch event
    public void setCameraSwitchEvent(){
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    engine.enableCamera(true);
                    AppLogger.getInstance().callApi("Camera On");
                } else {
                    engine.enableCamera(false);
                    AppLogger.getInstance().callApi("Camera Off");
                }
            }
        });
    }
    public void setCameraSpinnerEvent(){
        cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    public void setMicrophoneSwitchEvent(){
        microphoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    engine.muteMicrophone(false);
                    AppLogger.getInstance().callApi("Microphone On");
                } else {
                    engine.muteMicrophone(true);
                    AppLogger.getInstance().callApi("Microphone Off");
                }
            }
        });
    }
    public void setStartPublishingButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPublish) {
                    // get roomID, stream ID and user ID
                    roomID = editRoomID.getText().toString();
                    publishStreamID = editStreamID.getText().toString();
                    userID = editUserID.getText().toString();
                    // Login the room
                    loginRoom();
                    editRoomID.setEnabled(false);
                    editStreamID.setEnabled(false);
                    editUserID.setEnabled(false);
                    engine.startPreview(previewCanvas);
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    // Logout room
                    engine.logoutRoom(roomID);
                    AppLogger.getInstance().callApi("Logout Room:%s",roomID);
                    editRoomID.setEnabled(true);
                    editStreamID.setEnabled(true);
                    editUserID.setEnabled(false);
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
                // Update room state
                if (state.equals(ZegoRoomState.CONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                } else if (state.equals(ZegoRoomState.DISCONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
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
        Intent intent = new Intent(activity,Publishing.class);
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