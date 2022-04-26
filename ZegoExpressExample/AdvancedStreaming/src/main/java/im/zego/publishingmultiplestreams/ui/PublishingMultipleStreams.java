package im.zego.publishingmultiplestreams.ui;

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

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.streammonitoring.R;
import im.zego.keycenter.KeyCenter;
import im.zego.publishingmultiplestreams.camera.VideoCaptureFromImage2;
import im.zego.publishingmultiplestreams.camera.ZegoVideoCaptureCallback;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoCaptureConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class PublishingMultipleStreams extends AppCompatActivity {

    TextView userIDText;
    EditText firstPublishStreamIDText;
    EditText secondPublishStreamIDText;
    EditText firstPlayStreamIDText;
    EditText secondPlayStreamIDText;
    Button publishMainChannelButton;
    Button publishAuxChannelButton;
    Button playFirstStreamButton;
    Button playSecondStreamButton;
    TextureView publishMainChannelPreview;
    TextureView publishAuxChannelPreview;
    TextureView playFirstStreamView;
    TextureView playSecondStreamView;
    ZegoCanvas publishMainCanvas;
    ZegoCanvas playFirstStreamCanvas;
    ZegoCanvas playSecondStreamCanvas;
    TextView roomState;

    Long appID;
    String userID;
    String token;
    String roomID;
    String publishMainStreamID;
    String publishAuxStreamID;
    String playFirstStreamID;
    String playSecondStreamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoCustomVideoCaptureConfig captureConfig;

    boolean isPublishMain = false;
    boolean isPublishAux = false;
    boolean isPlayFirst = false;
    boolean isPlaySecond = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publishing_multiple_streams);
        bindView();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        setDefaultConfig();
        loginRoom();
        setPublishMainChannelButtonEvent();
        setPublishAuxChannelButtonEvent();
        setPlayFirstStreamButtonEvent();
        setPlaySecondStreamButtonEvent();
        setEventHandler();
        setLogComponent();
        setApiCalledResult();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userID);
        firstPublishStreamIDText = findViewById(R.id.editPublishMainStreamID);
        secondPublishStreamIDText = findViewById(R.id.editPublishAuxStreamID);
        firstPlayStreamIDText = findViewById(R.id.editPlayFirstStreamID);
        secondPlayStreamIDText = findViewById(R.id.editPlaySecondStreamID);
        publishMainChannelButton = findViewById(R.id.startPublishMainButton);
        publishAuxChannelButton = findViewById(R.id.startPublishAuxButton);
        playFirstStreamButton = findViewById(R.id.startPlayFirstButton);
        playSecondStreamButton = findViewById(R.id.startPlaySecondButton);
        publishMainChannelPreview = findViewById(R.id.publishMainChannelPreview);
        publishAuxChannelPreview = findViewById(R.id.publishAuxChannelPreview);
        playFirstStreamView = findViewById(R.id.playFirstStreamView);
        playSecondStreamView = findViewById(R.id.playSecondStreamView);
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
        //create the user
        user = new ZegoUser(userID);
    }
    public void setDefaultConfig(){
        //set default publish main channel streamID
        publishMainStreamID = "00091";
        //set default publish aux channel streamID
        publishAuxStreamID = "00092";
        //set default play main channel streamID
        playFirstStreamID = "00091";
        //set default play aux channel streamID
        playSecondStreamID = "00092";
        //set default user ID
        userIDText.setText(userID);
        roomID = "0008";

        //set custom video capture configuration.
        captureConfig = new ZegoCustomVideoCaptureConfig();
        captureConfig.bufferType= ZegoVideoBufferType.GL_TEXTURE_2D;
        engine.enableCustomVideoCapture(true,captureConfig,ZegoPublishChannel.AUX);
        ZegoVideoCaptureCallback videoCapture = null;
        videoCapture = new VideoCaptureFromImage2(this.getApplicationContext(), engine);
        videoCapture.setView(publishAuxChannelPreview);

        //set canvas vide mode setting.
        publishMainCanvas = new ZegoCanvas(publishMainChannelPreview);
        playFirstStreamCanvas = new ZegoCanvas(playFirstStreamView);
        playSecondStreamCanvas = new ZegoCanvas(playSecondStreamView);

        publishMainCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
        playFirstStreamCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
        playSecondStreamCanvas.viewMode = ZegoViewMode.SCALE_TO_FILL;
        engine.setCustomVideoCaptureHandler(videoCapture);
        setTitle(getString(R.string.publishing_multiple_streams));
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
    public void setPublishMainChannelButtonEvent(){
        publishMainChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMainStreamID = firstPublishStreamIDText.getText().toString();
                //if the user is publishing the MainChannel, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublishMain){
                    publishMainChannelButton.setText(getString(R.string.publish_main_channel));
                    engine.stopPublishingStream(ZegoPublishChannel.MAIN);
                    AppLogger.getInstance().callApi("Stop Publishing Stream On Channel: MainChannel");
                    engine.stopPreview();
                    isPublishMain = false;
                } else {
                    engine.startPreview(publishMainCanvas);
                    publishMainChannelButton.setText(getString(R.string.stop_publishing));
                    //ZegoPublishChannel.MAIN parameter indicates that the operation is mainstream
                    engine.startPublishingStream(publishMainStreamID,ZegoPublishChannel.MAIN);
                    AppLogger.getInstance().callApi("Start Publishing Stream On Channel: MainChannel");
                    isPublishMain = true;
                }
            }
        });
    }
    public void setPublishAuxChannelButtonEvent(){
        publishAuxChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishAuxStreamID = secondPublishStreamIDText.getText().toString();
                //if the user is publishing the AuxChannel, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublishAux){
                    //The ZegoPublishChannel.AUX parameter indicates that the auxiliary stream is operated
                    publishAuxChannelButton.setText(getString(R.string.publish_aux_channel));
                    AppLogger.getInstance().callApi("Stop Publishing Stream On Channel: AuxChannel");
                    engine.stopPublishingStream(ZegoPublishChannel.AUX);
                    isPublishAux = false;
                } else {
                    publishAuxChannelButton.setText(getString(R.string.stop_publishing));
                    engine.startPublishingStream(publishAuxStreamID,ZegoPublishChannel.AUX);
                    AppLogger.getInstance().callApi("Start Publishing Stream On Channel: AuxChannel");
                    isPublishAux = true;
                }
            }
        });
    }
    public void setPlayFirstStreamButtonEvent(){
        playFirstStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playFirstStreamID = firstPlayStreamIDText.getText().toString();
                //if the user is playing the first stream, this button is used to stop playing. Otherwise, this button is used to start playing.
                if (isPlayFirst){
                    playFirstStreamButton.setText(getString(R.string.play_first_stream));
                    engine.stopPlayingStream(playFirstStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playFirstStreamID);
                    isPlayFirst = false;
                } else {
                    playFirstStreamButton.setText(getString(R.string.stop_playing));
                    engine.startPlayingStream(playFirstStreamID,playFirstStreamCanvas);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playFirstStreamID);
                    isPlayFirst = true;
                }
            }
        });
    }
    public void setPlaySecondStreamButtonEvent(){
        playSecondStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSecondStreamID = secondPlayStreamIDText.getText().toString();
                //if the user is playing the second stream, this button is used to stop playing. Otherwise, this button is used to start playing.
                if (isPlaySecond){
                    playSecondStreamButton.setText(getString(R.string.play_second_stream));
                    engine.stopPlayingStream(playSecondStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playSecondStreamID);
                    isPlaySecond = false;
                } else {
                    playSecondStreamButton.setText(getString(R.string.stop_playing));
                    engine.startPlayingStream(playSecondStreamID,playSecondStreamCanvas);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playSecondStreamID);
                    isPlaySecond = true;
                }
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, PublishingMultipleStreams.class);
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
                if (streamID.equals(publishAuxStreamID))
                if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublishAux) {
                        publishAuxChannelButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublishAux) {
                        publishAuxChannelButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
                    }
                } else if (streamID.equals(publishMainStreamID)){
                    if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublishMain) {
                        publishMainChannelButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                        if (isPublishMain) {
                            publishMainChannelButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
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
                if (streamID.equals(playFirstStreamID)) {
                    if (errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                        if (isPlayFirst) {
                            playFirstStreamButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                        }
                    } else {
                        if (isPlayFirst) {
                            playFirstStreamButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                        }
                    }
                } else if (streamID.equals(playSecondStreamID)){
                    if (errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                        if (isPlaySecond) {
                            playSecondStreamButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                        }
                    } else {
                        if (isPlaySecond) {
                            playSecondStreamButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                        }
                    }
                }
            }

        });
    }
    @Override
    protected void onDestroy() {
        //logout and destroy the engine
        engine.logoutRoom(roomID);
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