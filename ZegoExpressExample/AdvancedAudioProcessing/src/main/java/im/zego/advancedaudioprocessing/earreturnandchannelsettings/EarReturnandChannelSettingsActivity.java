package im.zego.advancedaudioprocessing.earreturnandchannelsettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.IOException;

import im.zego.advancedaudioprocessing.R;
import im.zego.advancedaudioprocessing.voicechange.VoiceChangeActivity;
import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioCaptureStereoMode;
import im.zego.zegoexpress.constants.ZegoAudioChannel;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class EarReturnandChannelSettingsActivity extends AppCompatActivity {

    TextView userIDText;
    TextureView preview;
    TextureView playView;
    TextView roomState;
    EditText publishStreamIDEdit;
    EditText playStreamIDEdit;
    Button startPublishingButton;
    Button startPlayingButton;
    SwitchMaterial headPhoneMonitorSwitch;
    SeekBar volumeSeekBar;
    TextView volumeText;
    SwitchMaterial encoderStereoSwitch;
    AppCompatSpinner captureStereoSpinner;

    Long appID;
    String userID;
    String token;
    String roomID;
    String playStreamID;
    String publishStreamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoAudioConfig audioConfig = new ZegoAudioConfig();

    //Store whether the user is publishing the stream
    boolean isPublish = false;
    //Store whether the user is playing the stream
    boolean isPlay = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ear_returnand_channel_settings);
        requestPermission();
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoom();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setVolumeSeekBar();
        setHeadPhoneMonitorSwitch();
        setEncoderStereoSwitch();
        setCaptureStereoSpinner();
        setEventHandler();
        setLogComponent();
        setApiCalledResult();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userIDText);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        roomState = findViewById(R.id.roomState);
        publishStreamIDEdit = findViewById(R.id.editPublishStreamID);
        playStreamIDEdit = findViewById(R.id.editPlayStreamID);
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        headPhoneMonitorSwitch = findViewById(R.id.headPhoneMonitorSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeText = findViewById(R.id.volumeText);
        encoderStereoSwitch = findViewById(R.id.encoderStereoSwitch);
       captureStereoSpinner = findViewById(R.id.captureStereoSpinner);
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
    public void setDefaultValue(){
        //set default publish  and play streamID
        playStreamID = "0017";
        publishStreamID = "0017";
        roomID = "0017";

        userIDText.setText(userID);
        setTitle(getString(R.string.ear_return_and_channel_settings));

    }
    public void startPublish(){
        engine.startPreview(new ZegoCanvas(preview));
        engine.setAudioConfig(audioConfig);
        engine.startPublishingStream(publishStreamID);
        AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
        startPublishingButton.setText(getString(R.string.stop_publishing));
    }
    public void stopPublish(){
        engine.stopPreview();
        engine.stopPublishingStream();
        AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
        startPublishingButton.setText(getString(R.string.start_publishing));
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
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    stopPublish();
                    isPublish = false;
                } else {
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    startPublish();
                    isPublish = true;
                }
            }
        });
    }
    public void setVolumeSeekBar(){
        volumeSeekBar.setMax(200);
        volumeSeekBar.setProgress(100);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                engine.setHeadphoneMonitorVolume(progress);
                volumeText.setText(String.valueOf(progress));
                AppLogger.getInstance().callApi("Change Volume: %d",progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setHeadPhoneMonitorSwitch(){
        headPhoneMonitorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                engine.enableHeadphoneMonitor(isChecked);
                AppLogger.getInstance().callApi("Enable HeadphoneMonitor: %b",isChecked);
            }
        });
    }
    public void setEncoderStereoSwitch(){
        encoderStereoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isPublish){
                    stopPublish();
                    isPublish = false;
                }
                if (isChecked) {
                    audioConfig.channel = ZegoAudioChannel.STEREO;
                } else {
                    audioConfig.channel = ZegoAudioChannel.MONO;
                }
                engine.setAudioConfig(audioConfig);
            }
        });
    }
    public void setCaptureStereoSpinner(){
        captureStereoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPublish){
                    stopPublish();
                    isPublish = false;
                }
                String[] options = getResources().getStringArray(R.array.captureStereoMode);
                switch (options[position]){
                    case "NONE":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.NONE);
                        break;
                    case "AlWAYS":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.ALWAYS);
                        break;
                    case "ADAPTIVE":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.ADAPTIVE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        Intent intent = new Intent(activity, EarReturnandChannelSettingsActivity.class);
        activity.startActivity(intent);

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