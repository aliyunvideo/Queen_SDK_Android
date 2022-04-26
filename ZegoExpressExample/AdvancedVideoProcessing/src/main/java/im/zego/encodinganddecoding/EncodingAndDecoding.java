package im.zego.encodinganddecoding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.customrender.ui.ZGVideoRenderTypeUI;
import im.zego.customvideorendering.R;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPlayerVideoLayer;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoCodecID;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoVideoStreamType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class EncodingAndDecoding extends AppCompatActivity {
    TextView userIDText;
    TextureView preview;
    TextureView playView;
    EditText editPublishStreamID;
    EditText editPlayStreamID;
    Button startPublishingButton;
    Button startPlayingButton;
    SwitchMaterial hardwareEncoderSwitch;
    SwitchMaterial hardwareDecoderSwitch;
    AppCompatSpinner codecIDSpinner;
    AppCompatSpinner videoLayerSpinner;
    SwitchMaterial scalableVideoCodingSwitch;
    TextView roomState;

    Long appID;
    String roomID = "0012";
    String userID;
    String token;
    String publishStreamID;
    String playStreamID;
    boolean isPublish = false;
    boolean isPlay = false;

    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoVideoConfig videoConfig;
    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding_and_decoding);
        bindView();
        requestPermission();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        loginRoom();
        setDefaultConfig();
        setStartPlayButtonEvent();
        setStartPublishButtonEvent();
        setHardwareDecoderSwitchEvent();
        setHardwareEncoderSwitchEvent();
        setScalableVideoCodingSwitchEvent();
        setCodecIDSpinnerEvent();
        setEventHandler();
        setLogComponent();
        setVideoLayerSpinner();
        setApiCalledResult();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userID);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        editPublishStreamID = findViewById(R.id.editPublishStreamID);
        editPlayStreamID = findViewById(R.id.editPlayStreamID);
        startPlayingButton = findViewById(R.id.startPlayButton);
        startPublishingButton = findViewById(R.id.startPublishButton);
        hardwareDecoderSwitch = findViewById(R.id.hardwareDecodeSwitch);
        hardwareEncoderSwitch = findViewById(R.id.hardwareEncodeSwitch);
        codecIDSpinner = findViewById(R.id.codecIDSpinner);
        scalableVideoCodingSwitch = findViewById(R.id.scalableVideoCodingSwitch);
        roomState = findViewById(R.id.roomState);
        videoLayerSpinner = findViewById(R.id.videoLayerSpinner);

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
    public void setDefaultConfig(){
        //set the default video configuration
        videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);
        //set default play streamID
        playStreamID = "0012";
        //set default publish StreamID
        publishStreamID = "0012";
        userIDText.setText(userID);
        setTitle(getString(R.string.encoding_decoding));
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (!isPublish) {
                    engine.startPreview(new ZegoCanvas(preview));
                    publishStreamID = editPublishStreamID.getText().toString();
                    // set video configuration
                    ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    stopPublishing();
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (!isPlay){
                    playStreamID = editPlayStreamID.getText().toString();
                    engine.startPlayingStream(playStreamID,new ZegoCanvas(playView));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    isPlay = true;
                    startPlayingButton.setText(getResources().getString(R.string.stop_playing));
                    hardwareDecoderSwitch.setClickable(false);
                } else {
                    stopPlaying();
                }
            }
        });
    }
    public void stopPublishing(){
        engine.stopPreview();
        engine.stopPublishingStream();
        AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
        isPublish = false;
        startPublishingButton.setText(getResources().getString(R.string.start_publishing));
    }
    public void stopPlaying(){
        engine.stopPlayingStream(playStreamID);
        AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
        isPlay = false;
        startPlayingButton.setText(getResources().getString(R.string.start_playing));
        hardwareDecoderSwitch.setClickable(true);
    }
    public void setHardwareEncoderSwitchEvent(){
        hardwareEncoderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isPublish) {
                    stopPublishing();
                }
                // Enable hardware encoder
                if (isChecked){
                    engine.enableHardwareEncoder(true);
                    AppLogger.getInstance().callApi("Enable Hardware Encoder");
                } else {
                    engine.enableHardwareEncoder(false);
                    AppLogger.getInstance().callApi("Disable Hardware Encoder");
                }
            }
        });
    }
    public void setHardwareDecoderSwitchEvent(){
        hardwareDecoderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isPlay) {
                    stopPlaying();
                }
                // Enable hardware decoder
                if (isChecked){
                    engine.enableHardwareDecoder(true);
                } else {
                    engine.enableHardwareDecoder(false);
                }
            }
        });
    }
    public void setCodecIDSpinnerEvent(){
        codecIDSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPublish) {
                    stopPublishing();
                }
                String[] options = getResources().getStringArray(R.array.codecID);
                switch (options[position]){
                    // set CodecID and update the scalable video coding status. If the user set codecID as ZegoVideoCodecID.SVC,
                    // the scalable video coding will be turned on. Otherwise, it will be turned off.
                    case "ZegoVideoCodecID.SVC":
                        videoConfig.setCodecID(ZegoVideoCodecID.SVC);
                        scalableVideoCodingSwitch.setChecked(true);
                        AppLogger.getInstance().callApi("Set CodecID:ZegoVideoCodecID.SVC");
                        break;
                    case "ZegoVideoCodecID.H265":
                        videoConfig.setCodecID(ZegoVideoCodecID.H265);
                        AppLogger.getInstance().callApi("Set CodecID:ZegoVideoCodecID.H265");
                        scalableVideoCodingSwitch.setChecked(false);
                        break;
                    case "ZegoVideoCodecID.DEFAULT":
                        videoConfig.setCodecID(ZegoVideoCodecID.DEFAULT);
                        AppLogger.getInstance().callApi("Set CodecID:ZegoVideoCodecID.DEFAULT");
                        scalableVideoCodingSwitch.setChecked(false);
                        break;
                    case "ZegoVideoCodecID.VP8":
                        videoConfig.setCodecID(ZegoVideoCodecID.VP8);
                        AppLogger.getInstance().callApi("Set CodecID:ZegoVideoCodecID.VP8");
                        scalableVideoCodingSwitch.setChecked(false);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setScalableVideoCodingSwitchEvent(){
        scalableVideoCodingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isPublish) {
                    stopPublishing();
                }
                if (isChecked){
                    if (!codecIDSpinner.getSelectedItem().toString().equals("ZegoVideoCodecID.SVC")){
                        // if the user choose to enable scalable video coding, then set the codecID as ZegoVideoCodecID.SVC
                        // to enable  scalable video coding.
                        codecIDSpinner.setSelection(0);
                        AppLogger.getInstance().success("Enable Scalable VideoCoding");
                    }
                } else {
                    if (codecIDSpinner.getSelectedItem().toString().equals("ZegoVideoCodecID.SVC")){
                        // if the user choose to disable scalable video coding, then set the codecID as ZegoVideoCodecID.DEFAULT
                        // to disable scalable video coding
                        codecIDSpinner.setSelection(2);
                        AppLogger.getInstance().success("Disable Scalable VideoCoding");
                    }
                }
            }
        });
    }
    public void setVideoLayerSpinner(){
        videoLayerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String options[] = getResources().getStringArray(R.array.videoLayer);
                switch (options[position]){
                    case "DEFAULT":
                        engine.setPlayStreamVideoType(playStreamID,ZegoVideoStreamType.DEFAULT);
                        break;
                    case "SMALL":
                        engine.setPlayStreamVideoType(playStreamID,ZegoVideoStreamType.SMALL);
                        break;
                    case "BIG":
                        engine.setPlayStreamVideoType(playStreamID,ZegoVideoStreamType.BIG);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, EncodingAndDecoding.class);
        activity.startActivity(intent);
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
        // Release the rendering class
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);
    }
}