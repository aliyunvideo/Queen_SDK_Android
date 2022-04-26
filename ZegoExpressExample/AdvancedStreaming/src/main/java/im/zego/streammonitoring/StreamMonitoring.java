package im.zego.streammonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerMediaEvent;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStreamRelayCDNInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class StreamMonitoring extends AppCompatActivity {

    Button startPublishButton;
    Button startPlayButton;
    TextView publisherResolutionText;
    TextView publisherBitrateText;
    TextView publisherFpsText;
    TextView publisherRttText;
    TextView publisherLossText;
    TextView playerResolutionText;
    TextView playerBitrateText;
    TextView playerFpsText;
    TextView playerRttText;
    TextView playerDelayText;
    TextView playerLossText;
    EditText publishStreamIDText;
    EditText playStreamIDText;
    TextureView preview;
    TextureView playView;

    Long appID;
    String userID;
    String token;
    String roomID;
    String publishStreamID;
    String playStreamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoCanvas zegoPreview;
    ZegoCanvas zegoPlayView;
    TextView roomState;
    //video encode resolution
    int publisherWidth = 360;
    int publisherHeight = 640;

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
        setContentView(R.layout.activity_stream_monitoring);
        bindView();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        setDefaultConfig();
        LoginRoom();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setEngineEventHandler();
        setApiCalledResult();
        setLogComponent();
    }
    public void bindView(){
        startPublishButton = findViewById(R.id.startPublishButton);
        startPlayButton = findViewById(R.id.startPlayButton);
        publisherResolutionText = findViewById(R.id.publisherResolution);
        publisherBitrateText = findViewById(R.id.publisherBitrate);
        publisherFpsText = findViewById(R.id.prevVdeoFps);
        publisherRttText = findViewById(R.id.publisherRtt);
        publisherLossText = findViewById(R.id.publisherLoss);
        playerResolutionText = findViewById(R.id.playerResolution);
        playerBitrateText = findViewById(R.id.playerBitrate);
        playerFpsText = findViewById(R.id.videoFps);
        playerRttText = findViewById(R.id.rtt);
        playerDelayText = findViewById(R.id.delay);
        playerLossText = findViewById(R.id.loss);
        publishStreamIDText = findViewById(R.id.editPublishStreamID);
        playStreamIDText = findViewById(R.id.editPlayStreamID);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
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
        //set default play streamID
        playStreamID = "0008";
        //set default publish StreamID
        publishStreamID = "0008";

        roomID = "0008";
        //set video encode resolution
        ZegoVideoConfig config = new ZegoVideoConfig();
        config.setEncodeResolution(publisherWidth,publisherHeight);
        engine.setVideoConfig(config);
        //set background color of view
        zegoPlayView = new ZegoCanvas(playView);
        zegoPreview = new ZegoCanvas(preview);
        zegoPlayView.backgroundColor = Color.WHITE;
        zegoPreview.backgroundColor = Color.WHITE;

        setTitle(getString(R.string.stream_monitoring));
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
    public void setStartPublishButtonEvent(){
        startPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishStreamID = publishStreamIDText.getText().toString();
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    startPublishButton.setText(getString(R.string.start_publishing));
                    isPublish = false;
                } else {
                    engine.startPreview(zegoPreview);
                    engine.startPublishingStream(publishStreamID);
                    startPublishButton.setText(getString(R.string.stop_publishing));
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    isPublish = true;
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStreamID = playStreamIDText.getText().toString();
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (isPlay){
                    engine.stopPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    startPlayButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    engine.startPlayingStream(playStreamID, zegoPlayView);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                    startPlayButton.setText(getString(R.string.stop_playing));
                    isPlay = true;
                }
            }
        });
    }
    public void setEngineEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            //After calling the [startPublishingStream] successfully, the callback will be received every 3 seconds.
            // Through the callback, the collection frame rate, bit rate, RTT, packet loss rate and other quality data
            // of the published audio and video stream can be obtained, and the health of the publish stream can be monitored
            // in real time.
            @Override
            public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                super.onPublisherQualityUpdate(streamID, quality);
                publisherResolutionText.setText(publisherWidth+"x"+publisherHeight);
                publisherBitrateText.setText(String.format("%.2f", quality.videoKBPS) + "kbps");
                publisherFpsText.setText(String.format("%.2f",quality.videoSendFPS) + "f/s");
                publisherRttText.setText(quality.rtt + "ms");
                publisherLossText.setText(String.format("%.2f",quality.packetLostRate)+"%");
                AppLogger.getInstance().receiveCallback("onPublisherStateUpdate");
            }
            //After calling the [startPlayingStream] successfully, this callback will be triggered every 3 seconds.
            // The collection frame rate, bit rate, RTT, packet loss rate and other quality data can be obtained,
            // such the health of the publish stream can be monitored in real time.
            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
                playerBitrateText.setText(String.format("%.2f", quality.videoKBPS) + "kbps");
                playerFpsText.setText(String.format("%.2f",quality.videoRecvFPS) + "f/s");
                playerRttText.setText(quality.rtt + "ms");
                playerDelayText.setText(quality.delay + "ms");
                publisherLossText.setText(String.format("%.2f",quality.packetLostRate)+"%");
                AppLogger.getInstance().receiveCallback("onPlayerStateUpdate:%s",playStreamID);
            }

            // The callback triggered when the first audio frame is captured.
            @Override
            public void onPublisherCapturedAudioFirstFrame() {
                super.onPublisherCapturedAudioFirstFrame();
                AppLogger.getInstance().receiveCallback("onPublisherCapturedAudioFirstFrame");
            }

            @Override
            public void onPublisherCapturedVideoFirstFrame(ZegoPublishChannel channel) {
                super.onPublisherCapturedVideoFirstFrame(channel);
                AppLogger.getInstance().receiveCallback("onPublisherCapturedVideoFirstFrame");
            }

            //The callback triggered when the stream playback resolution changes.
            @Override
            public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
                super.onPlayerVideoSizeChanged(streamID, width, height);
                if (streamID.equals(playStreamID)) {
                    playerResolutionText.setText(width + "x" + height);
                }
                if (streamID.equals(publishStreamID)){
                    publisherResolutionText.setText(width+"x"+height);
                }
                AppLogger.getInstance().receiveCallback("onPlayerVideoSizeChanged streamID:%s VideoSize:%dx%d",streamID,width,height);
            }

            // The callback triggered when the state of relayed streaming to CDN changes.
            @Override
            public void onPublisherRelayCDNStateUpdate(String streamID, ArrayList<ZegoStreamRelayCDNInfo> infoList) {
                super.onPublisherRelayCDNStateUpdate(streamID, infoList);
                AppLogger.getInstance().receiveCallback("onPublisherRelayCDNStateUpdate streamID:%s",streamID);
            }

            // The callback triggered when a media event occurs during streaming playing.
            @Override
            public void onPlayerMediaEvent(String streamID, ZegoPlayerMediaEvent event) {
                super.onPlayerMediaEvent(streamID, event);
                AppLogger.getInstance().receiveCallback("onPlayerMediaEvent streamID:%s",streamID);
            }

            // The callback triggered when the first audio frame is received.
            @Override
            public void onPlayerRecvAudioFirstFrame(String streamID) {
                super.onPlayerRecvAudioFirstFrame(streamID);
                AppLogger.getInstance().receiveCallback("onPlayerRecvAudioFirstFrame streamID:%s",streamID);
            }

            // The callback triggered when the first video frame is rendered.
            @Override
            public void onPlayerRenderVideoFirstFrame(String streamID) {
                super.onPlayerRenderVideoFirstFrame(streamID);
                AppLogger.getInstance().receiveCallback("onPlayerRenderVideoFirstFrame streamID:%s",streamID);
            }

            // The callback triggered when the first video frame is received.
            @Override
            public void onPlayerRecvVideoFirstFrame(String streamID) {
                super.onPlayerRecvVideoFirstFrame(streamID);
                AppLogger.getInstance().receiveCallback("onPlayerRecvVideoFirstFrame streamID:%s",streamID);
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
                AppLogger.getInstance().receiveCallback("onPublisherStateUpdate streamID:%s",streamID);
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
                AppLogger.getInstance().receiveCallback("onPlayerStateUpdate streamID:%s",streamID);
            }

            @Override
            public void onPublisherVideoSizeChanged(int width, int height, ZegoPublishChannel channel) {
                super.onPublisherVideoSizeChanged(width, height, channel);
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity,StreamMonitoring.class);
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
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}