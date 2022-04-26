package im.zego.quickstart;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;

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
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class VideoChat extends AppCompatActivity {

    TextureView previewView;
    TextureView playView;

    TextView roomIDTextView;
    TextView userIDTextView;
    TextView publishStreamIDTextView;

    Button stopButton;

    String roomID;
    String userID;
    String publishStreamID;
    String playStreamID;

    ZegoExpressEngine engine;
    ZegoRoomState roomState;
    ZegoPublisherState publisherState;
    ZegoPlayerState playerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        bindView();
        getVideoChatConfig();
        setupView();
        setLogComponent();
        createEngine();
        setEventHandler();
        loginRoom();
        setStopButtonEvent();
        setApiCalledResult();
    }

    @Override
    protected void onDestroy() {
        // Stop preview
        engine.stopPreview();

        // Stop Publishing
        if (publisherState != null && publisherState != ZegoPublisherState.NO_PUBLISH) {
            engine.stopPublishingStream();
        }

        // Stop Playing
        if (playerState != null && playerState != ZegoPlayerState.NO_PLAY) {
            engine.stopPlayingStream(playStreamID);
        }

        // Logout room
        if (roomState != null && roomState != ZegoRoomState.DISCONNECTED) {
            engine.logoutRoom(roomID);
        }

        // Destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }

    public void bindView() {
        previewView = findViewById(R.id.previewView);
        playView = findViewById(R.id.playView);

        roomIDTextView = findViewById(R.id.roomIDTextView);
        userIDTextView = findViewById(R.id.userIDTextView);
        publishStreamIDTextView = findViewById(R.id.publishStreamIDTextView);

        stopButton = findViewById(R.id.stopButton);
    }

    public void getVideoChatConfig() {
        roomID = getIntent().getStringExtra("roomID");
        userID = getIntent().getStringExtra("userID");
        publishStreamID = getIntent().getStringExtra("publishStreamID");
    }

    public void setupView() {
        setTitle(R.string.video_chat);

        roomIDTextView.setText(String.format("roomID:%s", roomID));
        userIDTextView.setText(String.format("userID:%s", userID));
        publishStreamIDTextView.setText(String.format("publishStreamID:%s", publishStreamID));

        playView.setVisibility(View.INVISIBLE);
    }

    // Set log component. It includes a pop-up dialog.
    public void setLogComponent() {
        logLinearLayout logHiddenView = findViewById(R.id.logView);
        logHiddenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogView logview = new LogView(getApplicationContext());
                logview.show(getSupportFragmentManager(),null);
            }
        });
    }

    public void createEngine() {
        // Initialize ZegoExpressEngine
        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = KeyCenter.getInstance().getAppID();
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);
    }

    public void setEventHandler() {
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                AppLogger.getInstance().receiveCallback("onRoomStateUpdate, errorCode:%d", errorCode);
                roomState = state;
                if (errorCode == 0 && state == ZegoRoomState.CONNECTED) {
                    startLive();
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (updateType == ZegoUpdateType.ADD) {
                    // When the updateType is Add, stop playing current stream(if exist) and start playing new stream.
                    if (playerState != ZegoPlayerState.NO_PLAY) {
                        engine.stopPlayingStream(playStreamID);
                        playStreamID = null;
                    }

                    // No processing, just play the first stream
                    ZegoStream stream = streamList.get(0);
                    playStreamID = stream.streamID;
                    ZegoCanvas playCanvas = new ZegoCanvas(playView);
                    engine.startPlayingStream(playStreamID, playCanvas);
                } else {
                    // When the updateType is Delete, if the stream is being played, stop playing the stream.
                    if (playerState == ZegoPlayerState.NO_PLAY) {
                        return;
                    }
                    for (ZegoStream stream : streamList) {
                        if (playStreamID.equals(stream.streamID)) {
                            engine.stopPlayingStream(playStreamID);
                            playStreamID = null;
                        }
                    }
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                AppLogger.getInstance().receiveCallback("Publisher State Update State: %d", state.value());
                publisherState = state;
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                AppLogger.getInstance().receiveCallback("Player State Update State: %d", state.value());
                playerState = state;
                if (playerState == ZegoPlayerState.PLAYING) {
                    playView.setVisibility(View.VISIBLE);
                } else {
                    playView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void loginRoom() {
        // Login room
        AppLogger.getInstance().callApi("Login Room, roomID:%s", roomID);
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = KeyCenter.getInstance().getToken();
        engine.loginRoom(roomID, new ZegoUser(userID), config);
    }

    public void startLive() {
        // Start preview
        AppLogger.getInstance().callApi("Start preview");
        ZegoCanvas previewCanvas = new ZegoCanvas(previewView);
        engine.startPreview(previewCanvas);

        // Start publish
        AppLogger.getInstance().callApi("Start publishing stream. streamID:%s", publishStreamID);
        engine.startPublishingStream(publishStreamID);
    }

    public void setStopButtonEvent() {
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Destroy Activity,
                finish();
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
}
