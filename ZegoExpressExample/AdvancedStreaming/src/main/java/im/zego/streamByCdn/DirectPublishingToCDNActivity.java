package im.zego.streamByCdn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
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
import im.zego.keycenter.KeyCenter;
import im.zego.streammonitoring.R;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCDNConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class DirectPublishingToCDNActivity extends AppCompatActivity {

    TextureView preview;
    TextureView playView;
    TextView userIDText;
    SwitchMaterial enablePublishDirectToCdnSwitch;
    Button startPublishingButton;
    EditText publishCdnUrlEdit;
    EditText playCdnUrlEdit;
    Button playStreamFromUrlButton;
    TextView roomState;

    Long appID;
    String userID;
    String token;
    String roomID;
    String publishStreamID;
    String playStreamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    String publishCdnURL;
    String playCdnUrl;

    // To store whether the user enables publishing direct to CDN
    boolean isEnablePublishDirectToCdn = false;
    // To store whether the user is publishing the stream
    boolean isPublish = false;
    // To store whether the user is playing the stream
    boolean isPlay = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_publishing_to_cdn);
        bindView();
        getAppIDAndUserIDAndToken();
        setLogComponent();
        initEngineAndUser();
        setDefaultConfig();
        setEnablePublishDirectToCdnSwitchEvent();
        setStartPublishingButtonEvent();
        setPlayStreamFromUrlButtonEvent();
        setEventHandler();
        setApiCalledResult();
    }
    public void bindView(){
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        userIDText = findViewById(R.id.userID);
        enablePublishDirectToCdnSwitch = findViewById(R.id.step1Switch);
        startPublishingButton = findViewById(R.id.step2Button);
        publishCdnUrlEdit = findViewById(R.id.publishCdnUrlEditText);
        playCdnUrlEdit = findViewById(R.id.playCdnUrlEditText);
        playStreamFromUrlButton = findViewById(R.id.playStreamFromUrlButton);
        roomState = findViewById(R.id.roomState);
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
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
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        engine = ZegoExpressEngine.getEngine();
        if (engine == null){
            ZegoEngineProfile profile = new ZegoEngineProfile();
            profile.appID = appID;
            profile.scenario = ZegoScenario.GENERAL;
            profile.application = getApplication();
            engine = ZegoExpressEngine.createEngine(profile, null);
            AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        }
        engine.setEventHandler(null);
        //create the user
        user = new ZegoUser(userID);
    }
    public void setDefaultConfig(){
        //set default play streamID
        playStreamID = "0010";
        //set default publish StreamID
        publishStreamID = "0010";
        //set default user ID
        roomID = "0008";
        userIDText.setText(userID);
        setTitle(getString(R.string.direct_publishing_to_cdn));
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

    /** Step 1: Enable publish direct to CDN **/
    public void setEnablePublishDirectToCdnSwitchEvent(){
        enablePublishDirectToCdnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                publishCdnURL = publishCdnUrlEdit.getText().toString();
                // The user should enter CDN URL firstly before enabling publishing direct to CDN.
                if (publishCdnURL.equals("")){
                    Toast.makeText(DirectPublishingToCDNActivity.this, getString(R.string.cdn_empty_remind), Toast.LENGTH_LONG).show();
                    enablePublishDirectToCdnSwitch.setChecked(false);
                }
                else {
                    if (isChecked) {
                        ZegoCDNConfig config = new ZegoCDNConfig();
                        // set CDN URL
                        config.url = publishCdnURL;
                        AppLogger.getInstance().callApi("Add Publish CDN URL: %s",publishCdnURL);
                        // Enable publish direct to CDN
                        engine.enablePublishDirectToCDN(true, config);
                        AppLogger.getInstance().callApi("EnablePublishDirectToCDN: enable = true");
                        isEnablePublishDirectToCdn = true;
                    } else {
                        // If the user is publishing to CDN, it will not disable direct publishing to cdn.
                        if (isPublish){
                            Toast.makeText(DirectPublishingToCDNActivity.this,getString(R.string.please_stop_publishing_firstly),Toast.LENGTH_LONG).show();
                            enablePublishDirectToCdnSwitch.setChecked(true);
                        } else {
                            // Disable publishing direct to CDN
                            ZegoCDNConfig config = new ZegoCDNConfig();
                            engine.enablePublishDirectToCDN(false, config);
                            AppLogger.getInstance().callApi("EnablePublishDirectToCDN: enable = false");
                            isEnablePublishDirectToCdn = false;
                        }
                    }
                }
            }
        });
    }
    /** Step 2: Start publishing **/
    public void setStartPublishingButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if the user is publishing the stream, this button is used to stop publishing.
                if (isPublish){
                    engine.stopPublishingStream();
                    engine.stopPreview();
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    AppLogger.getInstance().callApi("Start Publishing Stream Directly To CDN: %s",publishCdnURL);
                    isPublish = false;
                } else {
                    // if the user has not start publishing, this button is used to start publishing.
                    // if the user enables publishing directly to CDN, stream will start to be published.
                    // Otherwise, it will reminds user to enable direct publishing to CDN firstly.
                    if (isEnablePublishDirectToCdn) {
                        engine.startPreview(new ZegoCanvas(preview));
                        engine.startPublishingStream(publishStreamID);
                        startPublishingButton.setText(getString(R.string.stop_publishing));
                        isPublish = true;
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.please_enable_publish_direct_to_cdn_firstly), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    // Play stream from url
    public void setPlayStreamFromUrlButtonEvent(){
        playStreamFromUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing.
                // Otherwise, this button is used to start playing.
                if (isPlay){
                    engine.stopPlayingStream(playStreamID);
                    isPlay = false;
                    playStreamFromUrlButton.setText(getString(R.string.play_stream_from_url));
                } else {
                    // set CDN configuration
                    ZegoCDNConfig config = new ZegoCDNConfig();
                    playCdnUrl = playCdnUrlEdit.getText().toString();
                    ZegoPlayerConfig zegoPlayerConfig = new ZegoPlayerConfig();
                    zegoPlayerConfig.cdnConfig = config;
                    if (!playCdnUrl.equals("")) {
                        // set CDN url
                        config.url = playCdnUrl;
                        // start playing stream
                        engine.startPlayingStream(playStreamID,new ZegoCanvas(playView),zegoPlayerConfig);
                        AppLogger.getInstance().callApi("Start Playing Stream From URL: %s",playCdnUrl);
                        isPlay = true;
                        playStreamFromUrlButton.setText(getString(R.string.stop_playing));
                    } else {
                        Toast.makeText(DirectPublishingToCDNActivity.this, getString(R.string.cdn_empty_remind), Toast.LENGTH_LONG).show();
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
                        playStreamFromUrlButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        playStreamFromUrlButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                    }
                }
            }
        });
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.setEventHandler(null);
        engine.logoutRoom(roomID);
        //Destroy the engine
        ZegoExpressEngine.destroyEngine(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initEngineAndUser();
        loginRoom();
        setEventHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}