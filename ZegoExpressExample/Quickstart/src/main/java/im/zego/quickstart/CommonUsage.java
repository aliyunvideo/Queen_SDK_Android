package im.zego.quickstart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

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

public class CommonUsage extends AppCompatActivity {

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
    Button destroyButton;
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;

    Long appID;
    String userID;
    String token;
    String roomID;
    String publishStreamID;
    String playStreamID;
    ZegoExpressEngine engine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_usage);
        bindView();
        requestPermission();
        setLogComponent();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        setCreateEngineButtonEvent();
        setLoginRoomButtonEvent();
        setStartPublishingButton();
        setStartPlayingButton();
        setDestroyButtonEvent();
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
        destroyButton = findViewById(R.id.destroyButton);
    }
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
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void setDefaultValue(){
        //set default play streamID
        playStreamID = "0001";
        //set default publish StreamID
        publishStreamID = "0001";
        roomID = "0001";

        userIDEdit.setText(userID);
        userIDEdit.setEnabled(false);
        setTitle(getString(R.string.common_usage));
    }
    public void setCreateEngineButtonEvent(){
        createEngineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // Initialize ZegoExpressEngine
                    ZegoEngineProfile profile = new ZegoEngineProfile();
                    profile.appID = appID;
                    profile.scenario = ZegoScenario.GENERAL;
                    profile.application = getApplication();
                    engine = ZegoExpressEngine.createEngine(profile, null);

                    Toast.makeText(CommonUsage.this, getString(R.string.creat_engine_success), Toast.LENGTH_SHORT).show();
                    AppLogger.getInstance().callApi("Create ZegoExpressEngine");
                    createEngineButton.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.create_engine));
                    setApiCalledResult();
                    setEventHandler();
            }
        });
    }
    public void setLoginRoomButtonEvent(){
        loginRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(CommonUsage.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    AppLogger.getInstance().fail("Login Fail");
                    return;
                }
                    roomID = roomIDEdit.getText().toString();
                    userID = userIDEdit.getText().toString();
                    // Create user
                    ZegoUser user = new ZegoUser(userID);

                    // Begin to login room
                    ZegoRoomConfig config = new ZegoRoomConfig();
                    config.token = token;
                    engine.loginRoom(roomID, user, config);

                    AppLogger.getInstance().callApi("LoginRoom:%s",roomID);
            }
        });
    }
    public void setStartPublishingButton(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(CommonUsage.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    return;
                }
                    publishStreamID = publishStreamIDEdit.getText().toString();
                    // Start publishing stream
                    engine.startPublishingStream(publishStreamID);
                    // Start preview and set local preview
                    engine.startPreview(new ZegoCanvas(preview));
                AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
            }
        });
    }
    public void setStartPlayingButton(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (engine == null) {
                    Toast.makeText(CommonUsage.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    return;
                }
                    playStreamID = playStreamIDEdit.getText().toString();
                    // Start playing stream
                    engine.startPlayingStream(playStreamID, new ZegoCanvas(playView));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",playStreamID);
            }
        });
    }
    public void setDestroyButtonEvent(){
        destroyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogger.getInstance().callApi("Destroy Engine");
                if (engine == null) {
                    Toast.makeText(CommonUsage.this, getString(R.string.create_engine_reminder), Toast.LENGTH_SHORT).show();
                    AppLogger.getInstance().fail("Destroy Fail");
                    return;
                }
                ZegoExpressEngine.getEngine().setEventHandler(null);
                ZegoExpressEngine.getEngine().setApiCalledCallback(null);
                // Destroy Engine
                ZegoExpressEngine.destroyEngine(null);
                engine = null;
                createEngineButton.setText(getString(R.string.create_engine));
                loginRoomButton.setText(getString(R.string.login_room));
                startPublishingButton.setText(getString(R.string.start_publishing));
                startPlayingButton.setText(getString(R.string.start_playing));
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity,CommonUsage.class);
        activity.startActivity(intent);
    }
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
    @Override
    protected void onDestroy() {
        // Release SDK resources
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
    public void setApiCalledResult(){
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
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                if (state == ZegoPublisherState.PUBLISHING) {
                    startPublishingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.start_publishing));
                } else if (state == ZegoPublisherState.NO_PUBLISH){
                    startPublishingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.start_publishing));
                }
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                if (streamID.equals(playStreamID)){
                    if (state == ZegoPlayerState.PLAYING) {
                        startPlayingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.start_playing));
                    } else if (state == ZegoPlayerState.NO_PLAY){
                        startPlayingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.start_playing));
                    }
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                if (roomID.equals(roomID)){
                    if (state == ZegoRoomState.CONNECTED){
                        loginRoomButton.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.login_room));
                    } else if (state == ZegoRoomState.DISCONNECTED){
                        loginRoomButton.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.login_room));
                    }
                }
            }

        });
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}