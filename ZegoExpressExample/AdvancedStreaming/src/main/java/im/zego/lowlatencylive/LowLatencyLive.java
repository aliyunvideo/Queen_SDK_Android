package im.zego.lowlatencylive;

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

import javax.security.auth.Destroyable;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.streammonitoring.R;
import im.zego.streammonitoring.StreamMonitoring;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class LowLatencyLive extends AppCompatActivity {

    TextureView preview;
    TextureView playView;
    EditText roomIDEdit;
    EditText userIDEdit;
    EditText publishStreamIDEdit;
    EditText playStreamIDEdit;
    Button createEngineButton;
    Button loginRoomButton;
    Button startPublishingButton;
    Button startPlayingButton;
    TextView roomState;

    Long appID;
    String userID;
    String token;
    String roomID;
    String publishStreamID;
    String playStreamID;
    ZegoExpressEngine engine;

    // Store whether the engine is created
    boolean isCreate = false;
    // Store whether the user has logged in the room.
    boolean isLoginRoom = false;
    // Store whether the user is publishing the stream
    boolean isPublish = false;
    // Store whether the user is playing the stream
    boolean isPlay = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_latency_live);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        setCreateEngineButtonEvent();
        setLoginRoomButtonEvent();
        setStartPublishingButton();
        setStartPlayingButton();
        setLogComponent();
        setApiCalledResult();
    }
    public void bindView(){
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        roomIDEdit = findViewById(R.id.editRoomID);
        userIDEdit = findViewById(R.id.editUserID);
        publishStreamIDEdit = findViewById(R.id.publishStreamID);
        playStreamIDEdit = findViewById(R.id.playStreamID);
        createEngineButton = findViewById(R.id.createEngineButton);
        loginRoomButton = findViewById(R.id.loginRoomButton);
        startPublishingButton = findViewById(R.id.startPublishingButton);
        startPlayingButton = findViewById(R.id.startPlayingButton);
        roomState = findViewById(R.id.roomState);
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void setDefaultValue(){
        //set default play streamID
        playStreamID = "0011";
        //set default publish StreamID
        publishStreamID = "0011";
        //set default user ID
        roomID = "0011";

        userIDEdit.setText(userID);
        userIDEdit.setEnabled(false);
        setTitle(getString(R.string.low_latency_live));
    }
    public void setCreateEngineButtonEvent(){
        createEngineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCreate){
                    engine.setEventHandler(null);
                    // Destroy Engine
                    ZegoExpressEngine.destroyEngine(null);
                    engine = null;
                    Toast.makeText(LowLatencyLive.this, getString(R.string.destroy_engine_success), Toast.LENGTH_SHORT).show();
                    AppLogger.getInstance().callApi("Destroy ZegoExpressEngine");
                    createEngineButton.setText(getString(R.string.create_engine));
                    isCreate = false;
                } else {
                    // Initialize ZegoExpressEngine
                    ZegoEngineProfile profile = new ZegoEngineProfile();
                    profile.appID = appID;
                    profile.scenario = ZegoScenario.GENERAL;
                    profile.application = getApplication();
                    engine = ZegoExpressEngine.createEngine(profile, null);

                    AppLogger.getInstance().callApi("Create ZegoExpressEngine");
                    Toast.makeText(LowLatencyLive.this, getString(R.string.creat_engine_success), Toast.LENGTH_SHORT).show();
                    createEngineButton.setText(getString(R.string.destroy_engine));
                    isCreate = true;

                    setEventHandler();
                    setApiCalledResult();
                }
            }
        });
    }
    public void setLoginRoomButtonEvent(){
        loginRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(LowLatencyLive.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isLoginRoom){
                    // Begin to logout room
                    engine.logoutRoom(roomID);
                    AppLogger.getInstance().callApi("Logout Room: %s",roomID);
                    Toast.makeText(LowLatencyLive.this, getString(R.string.logout_room_success), Toast.LENGTH_SHORT).show();
                    loginRoomButton.setText(getString(R.string.login_room));
                    isLoginRoom = false;
                } else {
                    roomID = roomIDEdit.getText().toString();
                    userID = userIDEdit.getText().toString();
                    // Create user
                    ZegoUser user = new ZegoUser(userID);

                    // Begin to login room */
                    ZegoRoomConfig config = new ZegoRoomConfig();
                    config.token = token;
                    engine.loginRoom(roomID, user, config);
                    AppLogger.getInstance().callApi("Login Room: %s",roomID);

                    Toast.makeText(LowLatencyLive.this, getString(R.string.login_room_success), Toast.LENGTH_SHORT).show();
                    loginRoomButton.setText(getString(R.string.logout_room));
                    isLoginRoom = true;
                }
            }
        });
    }
    public void setStartPublishingButton(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(LowLatencyLive.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isPublish){
                    // Begin to stop publish stream
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    // Stop preview
                    engine.stopPreview();
                    Toast.makeText(LowLatencyLive.this,getString(R.string.stop_publishing_success),Toast.LENGTH_SHORT).show();
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;
                } else {
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    // Start preview and set local preview
                    engine.startPreview(new ZegoCanvas(preview));
                    Toast.makeText(LowLatencyLive.this,getString(R.string.publish_stream_success),Toast.LENGTH_SHORT).show();
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
    }
    public void setStartPlayingButton(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(LowLatencyLive.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isPlay){
                    // Begin to stop play stream
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    Toast.makeText(LowLatencyLive.this,getString(R.string.stop_playing_success),Toast.LENGTH_SHORT).show();
                    startPlayingButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    //set Low-latency Live Streaming configuration
                    playStreamID = playStreamIDEdit.getText().toString();
                    ZegoPlayerConfig playerConfig = new ZegoPlayerConfig();
                    playerConfig.resourceMode = ZegoStreamResourceMode.ONLY_L3;
                    // Start playing stream
                    engine.startPlayingStream(playStreamID, new ZegoCanvas(playView),playerConfig);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    //engine.startPlayingStream(playStreamID, new ZegoCanvas(playView), playerConfig);
                    Toast.makeText(LowLatencyLive.this,getString(R.string.play_stream_success),Toast.LENGTH_SHORT).show();
                    startPlayingButton.setText(getString(R.string.stop_playing));
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
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity,LowLatencyLive.class);
        activity.startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        // Release SDK resources
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }

}