package im.zego.streamByCdn;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.streammonitoring.R;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoPublisherUpdateCdnUrlCallback;
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

public class StreamByCdn extends AppCompatActivity {

    TextureView preview;
    TextureView playView;
    Button startPublishingButton;
    Button addPublishCdnUrlButton;
    EditText publishCdnUrlEdit;
    EditText playCdnUrlEdit;
    Button playStreamFromUrlButton;
    TextView directPublishingToCdnButton;
    TextView userIDText;

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
    TextView roomState;

    // To store whether the user is publishing stream
    boolean isPublish = false;
    // To store whether the user is publishing stream to CDN
    boolean isPublishCDN = false;
    // To store whether the user is playing stream
    boolean isPlay = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_by_cdn);

        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultConfig();
        setAddPublishCdnUrlButton();
        setStartPublishingButtonEvent();
        setPlayStreamFromUrlButtonEvent();
        setDirectPublishingToCdnButtonEvent();

        initEngineAndUser();
        loginRoom();
        setEventHandler();
        setLogComponent();
        setApiCalledResult();
    }
    public void bindView(){
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        startPublishingButton = findViewById(R.id.step1Button);
        addPublishCdnUrlButton = findViewById(R.id.step2Button);
        publishCdnUrlEdit = findViewById(R.id.publishCdnUrlEditText);
        playCdnUrlEdit = findViewById(R.id.playCdnUrlEditText);
        playStreamFromUrlButton = findViewById(R.id.playStreamFromUrlButton);
        directPublishingToCdnButton = findViewById(R.id.directPubishingToCdnButton);
        userIDText = findViewById(R.id.userID);
        roomState = findViewById(R.id.roomState);
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
        // Create the user
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
        setTitle(getString(R.string.stream_by_cdn));
    }
    public void loginRoom(){
        if (ZegoExpressEngine.getEngine()==null){
            ZegoEngineProfile profile = new ZegoEngineProfile();
            profile.appID = appID;
            profile.scenario = ZegoScenario.GENERAL;
            profile.application = getApplication();
            engine = ZegoExpressEngine.createEngine(profile, null);

            AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        }
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
    /** Step 1 : Start publishing **/
    public void setStartPublishingButtonEvent(){
        // If the user is publishing the stream, the button is used to stop publishing.
        // Otherwise, the button is used to start publishing.
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPublish){
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    // Stop preview
                    engine.stopPreview();
                    // Stop publishing
                    engine.stopPublishingStream();
                    isPublish = false;
                } else {
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    // Start preview
                    engine.startPreview(new ZegoCanvas(preview));
                    // Start publishing
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    isPublish = true;
                }
            }
        });
    }
    /** Step 2: Add Publishing CDN URL**/
    public void setAddPublishCdnUrlButton(){
        addPublishCdnUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the user is publishing the stream to CDN, this button is used to stop publishing.
                // Otherwise, it is used to stop publishing.
                if (isPublishCDN){
                    if (isPublish){
                        startPublishingButton.setText(getString(R.string.start_publishing));
                        engine.stopPreview();
                        engine.stopPublishingStream();
                        isPublish = false;
                    }
                    // Remove the publishing CDN URL
                    engine.removePublishCdnUrl(publishStreamID, publishCdnURL, new IZegoPublisherUpdateCdnUrlCallback() {
                        @Override
                        public void onPublisherUpdateCdnUrlResult(int errorCode) {
                            AppLogger.getInstance().callApi("Remove CDN URL:%s",publishCdnURL);
                            if (errorCode == 0){
                                // Remove CDN URL successfully
                                Toast.makeText(getApplicationContext(),getString(R.string.remove_success_remind),Toast.LENGTH_LONG).show();
                            } else {
                                // Fail to remove CDN URL.
                                Toast.makeText(getApplicationContext(),getString(R.string.failure),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    addPublishCdnUrlButton.setText(getString(R.string.add_publish_cdn_url));
                    isPublishCDN = false;
                } else {
                    // The user should start publishing before publishing stream to CDN
                    if (isPublish){
                        publishCdnURL = publishCdnUrlEdit.getText().toString();
                        if (!publishCdnURL.equals("")){
                           engine.addPublishCdnUrl(publishStreamID, publishCdnURL, new IZegoPublisherUpdateCdnUrlCallback() {
                               @Override
                               public void onPublisherUpdateCdnUrlResult(int errorCode) {
                                   AppLogger.getInstance().callApi("Add CDN URL:%s",publishCdnURL);
                                   if (errorCode == 0){
                                       // Add CDN URL successfully
                                       Toast.makeText(StreamByCdn.this, getString(R.string.add_success_remind), Toast.LENGTH_LONG).show();
                                   } else {
                                       // Fail to add CDN URL.
                                       Toast.makeText(StreamByCdn.this, getString(R.string.failure), Toast.LENGTH_LONG).show();
                                   }
                               }
                           });
                            addPublishCdnUrlButton.setText(getString(R.string.remove_publish_cdn_url));
                            isPublishCDN = true;
                        } else {
                            Toast.makeText(StreamByCdn.this, getString(R.string.cdn_empty_remind), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(StreamByCdn.this, getString(R.string.please_start_publishing_firstly), Toast.LENGTH_LONG).show();
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
                    AppLogger.getInstance().callApi("Stop Playing Stream From URL: ",playStreamID);
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
                        AppLogger.getInstance().callApi("Start Playing Stream From URL: ",playStreamID);
                        isPlay = true;
                        playStreamFromUrlButton.setText(getString(R.string.stop_playing));
                    } else {
                        Toast.makeText(StreamByCdn.this, getString(R.string.cdn_empty_remind), Toast.LENGTH_LONG).show();
                    }
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
    // Start direct publishing to CDN activity
    public void setDirectPublishingToCdnButtonEvent(){
        directPublishingToCdnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ZegoExpressEngine.getEngine()!=null) {
                    engine.logoutRoom(roomID);
                }
                Intent intent = new Intent(getApplicationContext(),DirectPublishingToCDNActivity.class);
                startActivity(intent);
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, StreamByCdn.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
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
}