package com.example.others.multiplerooms;

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

import com.example.others.R;
import com.example.others.recording.RecordingActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomMode;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoPublisherConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class MultipleRoomsActivity extends AppCompatActivity {

    TextView userIDText;
    EditText roomID1Edit;
    EditText roomID2Edit;
    Button loginRoom1Button;
    Button loginRoom2Button;
    EditText publishRoomIDEdit;
    EditText publishStreamIDEdit;
    EditText playRoomIDEdit;
    EditText playStreamIDEdit;
    Button startPublishingButton;
    Button stopPublishingButton;
    Button startPlayingButton;
    Button stopPlayingButton;
    TextView roomState1;
    TextView roomState2;
    TextureView preview;
    TextureView playView;
    ZegoRoomConfig roomConfig = new ZegoRoomConfig();

    String userID;
    String publishStreamID;
    String playStreamID;
    String roomID1;
    String roomID2;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    // Store whether the user is publishing the stream
    Boolean isPublish = false;
    //Store whether the user is playing local media
    Boolean isPlay = false;

    //Store whether the room is login
    Boolean isLoginRoomID1 = false;
    //Store whether the room is login
    Boolean isLoginRoomID2 = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_rooms);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        setLogComponent();
        setEventHandler();
        setApiCalledResult();
        setLoginRoom1ButtonEvent();
        setLoginRoom2ButtonEvent();
        setStartPlayButtonEvent();
        setStartPublishButtonEvent();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userIDText);
        roomID1Edit = findViewById(R.id.room1IDEdit);
        roomID2Edit = findViewById(R.id.room2IDEdit);
        publishRoomIDEdit = findViewById(R.id.publishRoomIDEdit);
        publishStreamIDEdit = findViewById(R.id.publishStreamIDEdit);
        loginRoom1Button = findViewById(R.id.loginRoom1Button);
        loginRoom2Button = findViewById(R.id.loginRoom2Button);
        playRoomIDEdit = findViewById(R.id.playRoomIDEdit);
        playStreamIDEdit = findViewById(R.id.playStreamIDEdit);
        startPublishingButton = findViewById(R.id.startPublishButton);
        stopPublishingButton = findViewById(R.id.stopPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        stopPlayingButton = findViewById(R.id.stopPlayButton);
        roomState1 = findViewById(R.id.roomState1);
        roomState2 = findViewById(R.id.roomState2);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
    }
    public void setDefaultValue(){
        roomID1 = "00291";
        roomID2 = "00292";
        publishStreamID = "0029";
        playStreamID = "0029";
        // Whether to enable the user in and out of the room callback notification [onRoomUserUpdate],the default is off.
        // If developers need to use ZEGO Room user notifications, make sure that each user who login sets this flag to true
        roomConfig.isUserStatusNotify = true;
        roomConfig.token = token;

        setTitle(getString(R.string.multiple_rooms));
        userIDText.setText(userID);
        roomID1Edit.setText(roomID1);
        roomID2Edit.setText(roomID2);
        publishRoomIDEdit.setText(roomID1);
        roomState1.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
        roomState2.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void initEngineAndUser(){
        // If use multi room, must invoke "setRoomMode" before create engine
        ZegoExpressEngine.setRoomMode(ZegoRoomMode.MULTI_ROOM);
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
    public void setLoginRoom1ButtonEvent(){
        loginRoom1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginRoomID1) {
                    roomID1 = roomID1Edit.getText().toString();
                    engine.logoutRoom(roomID1);
                    AppLogger.getInstance().callApi("logout Room1:%s",roomID1);
                    loginRoom1Button.setText("Login room 1");
                    isLoginRoomID1 = false;
                } else {
                    roomID1 = roomID1Edit.getText().toString();
                    engine.loginRoom(roomID1,user,roomConfig);
                    AppLogger.getInstance().callApi("login Room1:%s",roomID1);
                    loginRoom1Button.setText("Logout room 1");
                    isLoginRoomID1 = true;
                }
            }
        });
    }
    public void setLoginRoom2ButtonEvent(){
        loginRoom2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginRoomID2) {
                    roomID2 = roomID2Edit.getText().toString();
                    engine.logoutRoom(roomID2);
                    AppLogger.getInstance().callApi("logout Room2:%s",roomID2);
                    loginRoom2Button.setText("Login room 2");
                    isLoginRoomID2 = false;
                } else {
                    roomID2 = roomID2Edit.getText().toString();
                    engine.loginRoom(roomID2,user,roomConfig);
                    AppLogger.getInstance().callApi("login Room2:%s",roomID2);
                    loginRoom2Button.setText("Logout room 2");
                    isLoginRoomID2 = true;
                }
            }
        });
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishStreamID = publishStreamIDEdit.getText().toString();
                engine.startPreview(new ZegoCanvas(preview));
                ZegoPublisherConfig config = new ZegoPublisherConfig();
                config.roomID = publishRoomIDEdit.getText().toString();
                engine.startPublishingStream(publishStreamID, config, ZegoPublishChannel.MAIN);
                AppLogger.getInstance().callApi("Start Publishing Stream:%s, Room:%s", publishStreamID, config.roomID);
                isPublish = true;
            }
        });
        stopPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishStreamID = publishStreamIDEdit.getText().toString();
                engine.stopPreview();
                engine.stopPublishingStream();
                AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                isPublish = false;
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStreamID = playStreamIDEdit.getText().toString();
                ZegoPlayerConfig config = new ZegoPlayerConfig();
                config.roomID = playRoomIDEdit.getText().toString();
                engine.startPlayingStream(playStreamID, new ZegoCanvas(playView), config);
                AppLogger.getInstance().callApi("Start Playing Stream:%s, Room:%s", playStreamID, config.roomID);
                isPlay = true;
            }
        });
        stopPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playStreamID = playStreamIDEdit.getText().toString();
                engine.stopPlayingStream(playStreamID);
                AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                isPlay = false;
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
                if (roomID.equals(roomID1)) {
                    if (state.equals(ZegoRoomState.CONNECTED)) {
                        roomState1.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.CONNECTED",roomID);
                    } else if (state.equals(ZegoRoomState.DISCONNECTED)) {
                        roomState1.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.DISCONNECTED",roomID);
                    } else {
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.CONNECTING",roomID);
                    }
                } else if (roomID.equals(roomID2)){
                    if (state.equals(ZegoRoomState.CONNECTED)) {
                        roomState2.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.CONNECTED",roomID);
                    } else if (state.equals(ZegoRoomState.DISCONNECTED)) {
                        roomState2.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.DISCONNECTED",roomID);
                    } else {
                        AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = %s, updateType = ZegoRoomState.CONNECTING",roomID);
                    }
                }
            }
            // The callback triggered when the number of other users in the room increases or decreases.
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                if (updateType.equals(ZegoUpdateType.ADD)){
                    for (ZegoUser user:userList){
                        AppLogger.getInstance().receiveCallback("[onRoomUserUpdate] Add user [userID = %s]",user.userID);
                    }
                } else {
                    for (ZegoUser user:userList){
                        AppLogger.getInstance().receiveCallback("[onRoomUserUpdate] Delete user [userID = %s]",user.userID);
                    }
                }
            }

            // The callback triggered when the number of streams published by the other users in the same room increases or decreases.
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (updateType.equals(ZegoUpdateType.ADD)){
                    for (ZegoStream stream:streamList){
                        AppLogger.getInstance().receiveCallback("[onRoomStreamUpdate] Add stream [streamID = %s]",stream.streamID);
                    }
                } else {
                    for (ZegoStream stream:streamList){
                        AppLogger.getInstance().receiveCallback("[onRoomStreamUpdate] Delete stream [streamID = %s]",stream.streamID);
                    }
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
//                    if (isPublish) {
//                        startPublishingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
//                    }
                } else {
//                    if (isPublish) {
//                        startPublishingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
//                    }
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
//                    if (isPlay) {
//                        startPlayingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
//                    }
                } else {
//                    if (isPlay) {
//                        startPlayingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
//                    }
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
        Intent intent = new Intent(activity, MultipleRoomsActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        // Restore room mode to single room
        ZegoExpressEngine.setRoomMode(ZegoRoomMode.SINGLE_ROOM);
        super.onDestroy();
    }
}