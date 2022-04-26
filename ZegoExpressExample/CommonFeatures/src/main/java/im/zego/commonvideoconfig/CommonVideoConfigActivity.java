package im.zego.commonvideoconfig;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

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
import im.zego.zegoexpress.constants.ZegoVideoCodecID;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;


public class CommonVideoConfigActivity extends AppCompatActivity {

    AppCompatSpinner previewViewMode;
    AppCompatSpinner playViewMode;
    AppCompatSpinner mirrorMode;
    AppCompatSpinner codecID;
    EditText editPublishStreamID;
    EditText editPlayStreamID;
    EditText encodeResolutionWidth;
    EditText encodeResolutionHeight;
    EditText captureResolutionWidth;
    EditText captureResolutionHeight;
    EditText fps;
    EditText bitrate;
    Button startPublishButton;
    Button startPlayButton;
    TextureView preview;
    TextureView playView;
    TextView roomState;
    Switch hardwareEncode;
    Switch hardwareDecode;

    Long appID;
    String roomID = "0005";
    String userID;
    String token;
    String publishStreamID;
    String playStreamID;
    boolean isPublish = false;
    boolean isPlay = false;

    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoVideoConfig videoConfig;
    ZegoCanvas playCanvas;
    ZegoCanvas previewCanvas;

    //TextView show info
    TextView encodeResolutionTitle;
    TextView captureResolutionTitle;
    TextView ViewModeNotice;
    TextView videoFpsTitle;
    TextView videoBitrateTitle;
    TextView mirrorTitle;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_video_config);
        setTitle(getString(R.string.common_video_config));
        bindView();
        requestPermission();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        LoginRoom();
        setDefaultConfig();
        setMirrorModeEvent();
        setPlayViewModeEvent();
        setPublishViewModeEvent();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setLogComponent();
        setEventHandler();
        setApiCalledResult();
        addEditListener();
        addQuestionToast();
    }

    public void bindView(){
        previewViewMode = findViewById(R.id.publishVideMode);
        playViewMode = findViewById(R.id.playVideMode);
        mirrorMode = findViewById(R.id.mirrorMode);
        editPublishStreamID = findViewById(R.id.editPublishStreamID);
        editPlayStreamID = findViewById(R.id.editPlayStreamID);
        editPublishStreamID = findViewById(R.id.editPublishStreamID);
        encodeResolutionWidth = findViewById(R.id.encodeResolutionWidth);
        encodeResolutionHeight = findViewById(R.id.encodeResolutionHeight);
        captureResolutionWidth = findViewById(R.id.captureResolutionWidth);
        captureResolutionHeight = findViewById(R.id.captureResolutionHeight);
        fps = findViewById(R.id.videoFps);
        bitrate = findViewById(R.id.videoBitrate);
        startPlayButton = findViewById(R.id.startPlayButton);
        startPublishButton = findViewById(R.id.startPublishButton);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);

        playCanvas = new ZegoCanvas(playView);
        previewCanvas = new ZegoCanvas(preview);
        playCanvas.backgroundColor = Color.WHITE;
        previewCanvas.backgroundColor = Color.WHITE;
        roomState = findViewById(R.id.roomState);

        mirrorTitle = findViewById(R.id.mirrorTitle);
        videoBitrateTitle = findViewById(R.id.videoBitrateTitle);
        encodeResolutionTitle = findViewById(R.id.encodeResolutionTitle);
        captureResolutionTitle = findViewById(R.id.captureResolutionTitle);
        ViewModeNotice = findViewById(R.id.ViewModeNotice);
        videoFpsTitle = findViewById(R.id.videoFpsTitle);

        hardwareEncode = findViewById(R.id.hardwareEncode);
        hardwareDecode = findViewById(R.id.hardwareDecode);
        codecID = findViewById(R.id.codecID);
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
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    //initial Engine and user
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
    public void LoginRoom(){
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
    public void setDefaultConfig(){
        //set the default video configuration
        videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);
        //set default play streamID
        playStreamID = "0005";
        //set default publish StreamID
        publishStreamID = "0005";

    }
    //set the mirror mode
    public void setMirrorModeEvent(){
        mirrorMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.mirrorMode);
                switch (options[position]){
                    case "OnlyPreview":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR);
                        AppLogger.getInstance().callApi("Change Video Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR");
                        break;
                    case "OnlyPublish":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR);
                        AppLogger.getInstance().callApi("Change Video Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR");
                        break;
                    case "Both":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR);
                        AppLogger.getInstance().callApi("Change Video Mirror Mode: mode = ZegoVideoMirrorMode.BOTH_MIRROR");
                        break;
                    case "None":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR);
                        AppLogger.getInstance().callApi("Change Video Mirror Mode: mode = ZegoVideoMirrorMode.NO_MIRROR");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setPlayViewModeEvent(){
        playViewMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.viewMode);
                //if it is playing the stream, stop playing the stream.
                if (isPlay){
                    engine.stopPlayingStream(playStreamID);
                }
                    if (!playStreamID.isEmpty()&&!playStreamID.equals("")) {
                        switch (options[position]) {
                            case "AspectFit":
                                playCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
                                AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FIT");
                                break;
                            case "AspectFill":
                                playCanvas.viewMode = ZegoViewMode.ASPECT_FILL;
                                AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FILL");
                                break;
                            case "ScaleToFill":
                                playCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
                                AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.SCALE_TO_FILL");
                                break;
                        }
                    }
                    // Restart the playing stream
                if (isPlay) {
                    engine.startPlayingStream(playStreamID, playCanvas);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setPublishViewModeEvent(){
        previewViewMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.viewMode);
                //To set the view mode, preview should be stopped firstly.
                engine.stopPreview();
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
                engine.startPreview(previewCanvas);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setStartPublishButtonEvent(){
        startPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPublish) {
                    engine.startPreview(previewCanvas);
                    publishStreamID = editPublishStreamID.getText().toString();
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

                    if (captureResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Capture Width cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureWidth = Integer.parseInt(captureResolutionWidth.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Capture Width is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (captureResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Capture Height cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureHeight = Integer.parseInt(captureResolutionHeight.getText().toString());
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Capture Height is too large", Toast.LENGTH_SHORT).show();
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

                    switch (codecID.getSelectedItem().toString()){
                        case "ZegoVideoCodecID.DEFAULT":
                            videoConfig.codecID = ZegoVideoCodecID.DEFAULT;
                            break;
                        case "ZegoVideoCodecID.SVC":
                            videoConfig.codecID = ZegoVideoCodecID.SVC;
                            break;
                        case "ZegoVideoCodecID.H265":
                            videoConfig.codecID = ZegoVideoCodecID.H265;
                            break;
                        case "ZegoVideoCodecID.VP8":
                            videoConfig.codecID = ZegoVideoCodecID.VP8;
                            break;
                    }

                    // set video configuration
                    ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);

                    // enable hardware encode
                    ZegoExpressEngine.getEngine().enableHardwareEncoder(hardwareEncode.isChecked());

                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    isPublish = false;
                    startPublishButton.setText(getResources().getString(R.string.start_publishing));
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay){
                    playStreamID = editPlayStreamID.getText().toString();
                    if (playStreamID!=null){
                        // enable hardware decode
                        ZegoExpressEngine.getEngine().enableHardwareDecoder(hardwareDecode.isChecked());

                        engine.startPlayingStream(playStreamID,playCanvas);
                        AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                        isPlay = true;
                        startPlayButton.setText(getResources().getString(R.string.stop_playing));
                    }
                } else {
                    engine.stopPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    isPlay = false;
                    startPlayButton.setText(getResources().getString(R.string.start_playing));
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
            // The callback triggered when the state of stream publishing changes.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        startPublishButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        startPublishButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
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
                        startPlayButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        startPlayButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
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

    public void addQuestionToast() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.encodeResolutionTitle) {
                    Toast.makeText(getApplicationContext(), R.string.encodeResolutionTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.captureResolutionTitle) {
                    Toast.makeText(getApplicationContext(), R.string.captureResolutionTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.ViewModeNotice) {
                    Toast.makeText(getApplicationContext(), R.string.ViewModeNotice, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.videoFpsTitle) {
                    Toast.makeText(getApplicationContext(), R.string.videoFpsTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.videoBitrateTitle) {
                    Toast.makeText(getApplicationContext(), R.string.videoBitrateTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.mirrorTitle) {
                    Toast.makeText(getApplicationContext(), R.string.mirrorTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        };

        //add Toast in order to show info
        encodeResolutionTitle.setOnClickListener(listener);
        captureResolutionTitle.setOnClickListener(listener);
        ViewModeNotice.setOnClickListener(listener);
        videoFpsTitle.setOnClickListener(listener);
        videoBitrateTitle.setOnClickListener(listener);
        mirrorTitle.setOnClickListener(listener);

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
        Intent intent = new Intent(activity, im.zego.commonvideoconfig.CommonVideoConfigActivity.class);
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