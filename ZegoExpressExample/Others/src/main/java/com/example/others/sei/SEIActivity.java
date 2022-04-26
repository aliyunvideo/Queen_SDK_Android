package com.example.others.sei;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.others.R;
import com.example.others.security.SecurityActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class SEIActivity extends AppCompatActivity {

    TextureView preview;
    TextureView playView;
    Button startPublishingButton;
    Button startPlayingButton;
    TextView userIDText;
    EditText publishStreamIDEdit;
    EditText playStreamIDEdit;
    TextView roomState;
    EditText seiEdit;
    Button sendSEIButton;
    ListView receivedSEIList;
    ArrayAdapter<String> adapter;

    String userID;
    String publishStreamID;
    String playStreamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    //Store whether the user is playing the stream
    Boolean isPlay = false;
    //Store whether the user is publishing the stream
    Boolean isPublish = false;
    ArrayList<String> receivedSEI = new ArrayList<>();

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sei);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoom();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setEventHandler();
        setApiCalledResult();
        setLogComponent();
        setLayout();
        setSendSEIButton();
    }
    public void bindView(){
        preview = findViewById(R.id.Preview);
        playView = findViewById(R.id.playView);
        startPlayingButton = findViewById(R.id.startPlayButton);
        startPublishingButton = findViewById(R.id.startPublishButton);
        userIDText = findViewById(R.id.userID);
        publishStreamIDEdit = findViewById(R.id.editPublishStreamID);
        playStreamIDEdit = findViewById(R.id.editPlayStreamID);
        roomState = findViewById(R.id.roomState);
        seiEdit = findViewById(R.id.seiEdit);
        sendSEIButton = findViewById(R.id.sendSeiButton);
        receivedSEIList = findViewById(R.id.receivedSEIList);
    }
    public void setDefaultValue(){
        roomID = "0034";
        publishStreamID = "0034";
        playStreamID = "0034";
        userIDText.setText(userID);
        setTitle(getString(R.string.sei));
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
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPublish) {
                    engine.startPreview(new ZegoCanvas(preview));
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    startPublishingButton.setText(getResources().getString(R.string.stop_publishing));
                    isPublish = true;
                } else {
                    // Stop preview
                    engine.stopPreview();
                    // Stop publishing
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    isPublish = false;
                    startPublishingButton.setText(getResources().getString(R.string.start_publishing));
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay){
                    playStreamID = playStreamIDEdit.getText().toString();
                    if (playStreamID!=null){
                        // Start playing
                        engine.startPlayingStream(playStreamID,new ZegoCanvas(playView));
                        AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
                        isPlay = true;
                        startPlayingButton.setText(getResources().getString(R.string.stop_playing));
                    }
                } else {
                    // Stop playing
                    engine.stopPlayingStream(playStreamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",playStreamID);
                    isPlay = false;
                    startPlayingButton.setText(getResources().getString(R.string.start_playing));
                }
            }
        });
    }
    public void setLayout(){
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, receivedSEI) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        receivedSEIList.setAdapter(adapter);
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
            @Override
            public void onPlayerRecvSEI(String streamID, byte[] data) {
                super.onPlayerRecvSEI(streamID, data);
                if (streamID.equals(playStreamID)){
                    receivedSEI.add(new String(data));
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SEIActivity.class);
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
    public void setSendSEIButton(){
        sendSEIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPublish) {
                    byte[] sei = seiEdit.getText().toString().getBytes();
                    engine.sendSEI(sei);
                } else {
                    Toast.makeText(SEIActivity.this, "Please start publishing firstly!", Toast.LENGTH_SHORT).show();
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
        //log out the room
        engine.logoutRoom(roomID);
        //destroy the engine
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}