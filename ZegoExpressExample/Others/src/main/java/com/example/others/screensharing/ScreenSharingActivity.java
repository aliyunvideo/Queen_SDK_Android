package com.example.others.screensharing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.others.R;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class ScreenSharingActivity extends AppCompatActivity {

    TextView userIDText;
    EditText roomIDEdit;
    EditText publishStreamIDEdit;
    Button startScreenCaptureButton;
    EditText playStreamIDEdit;
    Button playButton;
    TextureView playView;
    TextView roomState;

    String userID;
    String publishStreamID;
    String playStreamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;
    static MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE = 1001;
    private Intent service;
    public static MediaProjection mMediaProjection;
    private static final int DEFAULT_VIDEO_WIDTH = 360;
    private static final int DEFAULT_VIDEO_HEIGHT = 640;

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
        setContentView(R.layout.activity_screen_sharing);
        bindView();
        requestPermission();
        setLogComponent();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        prepareScreenCapture();
        setPlayButtonEvent();
        setEventHandler();
        setStartScreenCaptureButtonEvent();
        setApiCalledResult();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userID);
        roomIDEdit = findViewById(R.id.roomIDEdit);
        publishStreamIDEdit = findViewById(R.id.publishIDEdit);
        startScreenCaptureButton = findViewById(R.id.screenCaptureButton);
        playStreamIDEdit = findViewById(R.id.editPlayStreamID);
        playButton = findViewById(R.id.playButton);
        playView = findViewById(R.id.playView);
        roomState = findViewById(R.id.roomState);

    }
    public void setDefaultValue(){
        roomID = "0033";
        publishStreamID = "0033";
        playStreamID = "0033";
        userIDText.setText(userID);
        setTitle(getString(R.string.screen_sharing));
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
    public void  prepareScreenCapture(){
        if (Build.VERSION.SDK_INT < 21) {
            Toast.makeText(this, "Require root permission", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 5.0及以上版本
            // 请求录屏权限，等待用户授权
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                // Target版本高于等于10.0需要使用前台服务，并在前台服务的onStartCommand方法中创建MediaProjection
                service=new Intent(this, CaptureScreenService.class);
                service.putExtra("code",resultCode);
                service.putExtra("data",data);
                startForegroundService(service);
            }else {
                //Target版本低于10.0直接获取MediaProjection
                mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            }
        }
    }
    public void setCustomCapture(){
        // VideoCaptureScreen inherits IZegoCustomVideoCaptureHandler, which is used to monitor custom capture onStart and onStop callbacks
        // VideoCaptureScreen继承IZegoCustomVideoCaptureHandler，用于监听自定义采集onStart和onStop回调
        VideoCaptureScreen videoCapture = new VideoCaptureScreen(mMediaProjection, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT, engine);

        engine.setCustomVideoCaptureHandler(videoCapture);
        ZegoCustomVideoCaptureConfig videoCaptureConfig=new ZegoCustomVideoCaptureConfig();
        videoCaptureConfig.bufferType= ZegoVideoBufferType.SURFACE_TEXTURE;
        // Start Custom Capture
        engine.enableCustomVideoCapture(true, videoCaptureConfig, ZegoPublishChannel.MAIN);
    }
    public void setStartScreenCaptureButtonEvent(){
        startScreenCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPublish){
                    engine.logoutRoom(roomID);
                    AppLogger.getInstance().callApi("Stop Publishing Stream: %s",publishStreamID);
                    startScreenCaptureButton.setText(getString(R.string.start_screen_capture));
                    engine.stopPublishingStream();
                    isPublish = false;
                } else {
                    loginRoom();
                    setCustomCapture();
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    engine.startPublishingStream(publishStreamID);
                    startScreenCaptureButton.setText(getString(R.string.stop_screen_capture));
                    AppLogger.getInstance().callApi("Start Publishing Stream: %s",publishStreamID);
                    isPublish = true;
                }
            }
        });
    }
    public void loginRoom(){
        roomID = roomIDEdit.getText().toString();
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
    public void setPlayButtonEvent(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (isPlay){
                    engine.stopPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    playButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    playStreamID = playStreamIDEdit.getText().toString();
                    engine.startPlayingStream(playStreamID, new ZegoCanvas(playView));
                    playButton.setText(getString(R.string.stop_playing));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    isPlay = true;
                }
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
            // The callback triggered when the state of stream playing changes.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PLAYER_STATE_NO_PLAY and the errcode is not 0, it means that stream playing has failed and
                // no more retry will be attempted by the engine. At this point, the failure of stream playing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                    if (isPlay) {
                        playButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        playButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                    }
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
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, ScreenSharingActivity.class);
        activity.startActivity(intent);
    }
    public void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.RECORD_AUDIO",
                "android.permission.FOREGROUND_SERVICE"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.FOREGROUND_SERVICE") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
            }
        }
    }
    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}