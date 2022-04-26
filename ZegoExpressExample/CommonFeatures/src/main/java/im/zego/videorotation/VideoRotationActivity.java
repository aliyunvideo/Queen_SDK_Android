package im.zego.videorotation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.commonvideoconfig.R;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class VideoRotationActivity extends AppCompatActivity {

    EditText editRoomID;
    EditText editUserID;
    EditText editStreamID;
    AppCompatSpinner rotateMode;
    Button startButton;
    TextureView videoView;
    TextView roomState;

    String streamID;
    String userID;
    String roomID;
    Long appID;
    String token;
    String type;

    ZegoExpressEngine engine;
    ZegoVideoConfig config = new ZegoVideoConfig();
    ZegoCanvas canvas;

    //show info
    TextView rotateModeTitle;
    // Whether the view is playing or publishing
    boolean isStart = false;
    // Whether user has logged in the room or not.
    boolean isLogin = false;
    // Whether the rotate mode is set to AUTO.
    boolean isAuto = false;
    // Whether the user choose to publish stream.
    boolean isPublish;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_rotation);
        bindView();
        getAppIDAndUserIDAndToken();
        initUI();
        requestPermission();
        setDefaultValue();
        initEngine();
        setRotateModeButtonEvent();
        setStartButtonEvent();
        setEventHandler();
        setApiCalledResult();
        setLogComponent();
        showInfo();
    }

    public void showInfo() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.rotateModeTitle){
                    Toast.makeText(getApplicationContext(),R.string.RotateNotice,Toast.LENGTH_LONG).show();
                    return;
                }
            }
        };
        rotateModeTitle.setOnClickListener(listener);
    }

    public void bindView(){
        editRoomID = findViewById(R.id.editRoomID);
        editUserID = findViewById(R.id.editUserID);
        editStreamID = findViewById(R.id.editStreamID);
        startButton = findViewById(R.id.startButton);
        rotateMode = findViewById(R.id.rotateMode);
        videoView = findViewById(R.id.textureView);
        roomState = findViewById(R.id.roomState);
        rotateModeTitle = findViewById(R.id.rotateModeTitle);
    }
    public void initUI(){
        //get user's choice.
        type = getIntent().getStringExtra("type");
        setTitle(type);

        //if the user choose to play the stream
        if (type.equals(getString(R.string.play_stream))){
            startButton.setText(getString(R.string.start_playing));
            // do not need set rotatation
            rotateModeTitle.setVisibility(View.GONE);
            rotateMode.setVisibility(View.GONE);
        }
        else
        {
            startButton.setText(getString(R.string.start_publishing));
        }
        //update userID with the default value.
        editUserID.setText(userID);
        editUserID.setEnabled(false);
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
    public void setDefaultValue(){
        streamID = editStreamID.getText().toString();
        roomID = editRoomID.getText().toString();

        //set the default encoded resolution as 370X640
        config.setEncodeResolution(360,640);

        // update isPublish
        if (type.equals(getString(R.string.publish_stream))){
            isPublish = true;
        } else {
            isPublish = false;
        }

        //set the canvas background color as white.
        canvas = new ZegoCanvas(videoView);
        canvas.backgroundColor = Color.WHITE;
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void initEngine(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
    }
    public void loginRoom(){
        //login room
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        engine.loginRoom(roomID, new ZegoUser(userID), config);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
    }
    public void setRotateModeButtonEvent(){
        rotateMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.orientation);
                // The user should stop publishing/playing before modifying rotate mode.
                if (isStart) {
                    Toast.makeText(getApplicationContext(), "Please stop publishing/playing first", Toast.LENGTH_LONG).show();
                    //set to default
                    parent.setSelection(0);
                } else {
                    switch (options[position]) {
                        case "Fixed Portrait":
                            // Lock layout orientation to Portrait
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            if (isPublish) {
                                config.setEncodeResolution(360, 640);
                                engine.setAppOrientation(ZegoOrientation.ORIENTATION_0);
                                isAuto = false;
                            }
                            break;
                        case "Fixed Landscape":
                            // Lock layout orientation to landscape.
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            if (isPublish) {
                                config.setEncodeResolution(640, 360);
                                engine.setAppOrientation(ZegoOrientation.ORIENTATION_90);
                                isAuto = false;
                            }
                            break;
                        case "Auto":
                            // Allow automatic rotation
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            if (isPublish) {
                                isAuto = true;
                            }
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setStartButtonEvent(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.setVideoConfig(config);
                roomID = editRoomID.getText().toString();
                streamID = editStreamID.getText().toString();
                userID = editUserID.getText().toString();

                isLogin = true;

                // if the user choose to publish the stream.
                if (isPublish){
                    // if the user is publishing stream.
                    if (isStart){
                        engine.stopPreview();
                        engine.stopPublishingStream();
                        AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
                        engine.logoutRoom(roomID);
                        AppLogger.getInstance().callApi("LogoutRoom: %s",roomID);
                        editRoomID.setEnabled(true);
                        editStreamID.setEnabled(true);
                        editUserID.setEnabled(false);
                        isStart = false;
                        startButton.setText(getString(R.string.start_publishing));
                    }else {
                        loginRoom();
                        engine.startPreview(canvas);
                        engine.startPublishingStream(streamID);
                        AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
                        isStart = true;
                        editRoomID.setEnabled(false);
                        editStreamID.setEnabled(false);
                        editUserID.setEnabled(false);
                        startButton.setText(getString(R.string.stop_publishing));
                    }
                }
                // if the user choose to play the stream.
                else {
                    //if the user is playing stream.
                    if (isStart) {
                        engine.stopPlayingStream(streamID);
                        AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                        engine.logoutRoom(roomID);
                        AppLogger.getInstance().callApi("LogoutRoom: %s",roomID);
                        editRoomID.setEnabled(true);
                        editStreamID.setEnabled(true);
                        editUserID.setEnabled(true);
                        startButton.setText(getString(R.string.start_playing));
                        isStart = false;
                    } else {
                        loginRoom();
                        engine.startPlayingStream(streamID, canvas);
                        AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                        startButton.setText(getString(R.string.stop_playing));
                        editRoomID.setEnabled(false);
                        editStreamID.setEnabled(false);
                        editUserID.setEnabled(false);
                        isStart = true;
                    }
                }
            }
        });
    }

    //Called by the system when the device configuration changes
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isAuto) {
            if (isPublish) {
                ZegoOrientation orientation = ZegoOrientation.ORIENTATION_0;
                if (Surface.ROTATION_0 == this.getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_0;
                    config.setEncodeResolution(360, 640);
                } else if (Surface.ROTATION_180 == this.getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_180;
                    config.setEncodeResolution(360, 640);
                } else if (Surface.ROTATION_270 == this.getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_270;
                    config.setEncodeResolution(640, 360);
                } else if (Surface.ROTATION_90 == this.getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_90;
                    config.setEncodeResolution(640, 360);
                }
                engine.setAppOrientation(orientation);
                engine.setVideoConfig(config);
            }
        }
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
                if (isPublish) {
                    if (errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                        if (isStart) {
                            startButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                        }
                    } else {
                        if (isStart) {
                            startButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
                        }
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
                if (!isPublish) {
                    if (errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                        if (isStart) {
                            startButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                        }
                    } else {
                        if (isStart) {
                            startButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                        }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.logoutRoom(roomID);
        engine.setEventHandler(null);
        ZegoExpressEngine.destroyEngine(null);
    }
}