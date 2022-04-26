package im.zego.advancedaudioprocessing.audioeffectplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import im.zego.advancedaudioprocessing.R;
import im.zego.advancedaudioprocessing.audio3a.Audio3aActivity;
import im.zego.advancedaudioprocessing.earreturnandchannelsettings.EarReturnandChannelSettingsActivity;
import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoAudioEffectPlayer;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoAudioEffectPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoAudioEffectPlayConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class AudioEffectPlayerActivity extends AppCompatActivity {

    Button startPublishingButton;
    Button startPlayingButton;
    TextureView preview;
    TextureView playView;
    TextView userIDText;
    TextView roomState;
    SwitchMaterial publishOutSwitch1;
    EditText playCountEdit1;
    Button playButton1;
    Button pauseButton1;
    Button resumeButton1;
    Button stopButton1;
    Button loadResourceButton1;
    Button unloadButton1;
    EditText resourceEdit1;
    EditText resourceEdit2;
    SwitchMaterial publishOutSwitch2;
    EditText playCountEdit2;
    Button playButton2;
    Button pauseButton2;
    Button resumeButton2;
    Button stopButton2;
    Button loadResourceButton2;
    Button unloadButton2;
    Button pauseAllButton;
    Button resumeAllButton;
    Button stopAllButton;

    Long appID;
    String userID;
    String token;
    String roomID;
    String streamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoAudioEffectPlayer audioEffectPlayer;
    final List<String> fileNames = new ArrayList<>();
    ZegoAudioEffectPlayConfig playConfig1 = new ZegoAudioEffectPlayConfig();
    ZegoAudioEffectPlayConfig playConfig2 = new ZegoAudioEffectPlayConfig();
    String path1;
    String path2;
    int audioEffectID1 = 1;
    int audioEffectID2 = 2;

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
        setContentView(R.layout.activity_audio_effect_player);
        bindView();
        initData();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoom();
        initAudioPlayer();
        setStartPlayButtonEvent();
        setStartPublishButtonEvent();
        setEventHandler();
        setLogComponent();
        setApiCalledResult();
        setPublishOutSwitch1();
        setLoadResourceButton1();
        setUnloadButton1();
        setUnloadButton2();
        setLoadResourceButton2();
        setPublishOutSwitch2();
        setPlayButton1();
        setPlayButton2();
        setPauseButton1();
        setPauseButton2();
        setResumeButton1();
        setResumeButton2();
        setStopButton1();
        setStopButton2();
        setPauseAllButton();
        setResumeAllButton();
        setStopAllButton();
    }
    public void bindView(){
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        userIDText = findViewById(R.id.userIDText);
        roomState = findViewById(R.id.roomState);
        publishOutSwitch1 = findViewById(R.id.publishOutSwitch1);
        playCountEdit1 = findViewById(R.id.playCountEdit1);
        playButton1 = findViewById(R.id.playButton1);
        pauseButton1 = findViewById(R.id.pauseButton1);
        resumeButton1 = findViewById(R.id.resumeButton1);
        stopButton1 = findViewById(R.id.stopButton1);
        loadResourceButton1 = findViewById(R.id.loadResourcesButton1);
        unloadButton1 = findViewById(R.id.unloadResourcesButton1);
        resourceEdit1 = findViewById(R.id.loadResourcesEdit1);
        resourceEdit2 = findViewById(R.id.loadResourcesEdit2);
        publishOutSwitch2 = findViewById(R.id.publishOutSwitch2);
        playCountEdit2 = findViewById(R.id.playCountEdit2);
        playButton2 = findViewById(R.id.playButton2);
        pauseButton2 = findViewById(R.id.pauseButton2);
        resumeButton2 = findViewById(R.id.resumeButton2);
        stopButton2 = findViewById(R.id.stopButton2);
        loadResourceButton2 = findViewById(R.id.loadResourcesButton2);
        unloadButton2 = findViewById(R.id.unloadResourcesButton2);
        pauseAllButton = findViewById(R.id.pauseAllButton);
        resumeAllButton = findViewById(R.id.resumeAllButton);
        stopAllButton = findViewById(R.id.stopAllButton);
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void setDefaultValue(){
        //set default publish  and play streamID
        streamID = "0020";
        roomID = "0020";

        userIDText.setText(userID);
        setTitle(getString(R.string.audio_effect_player));
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
    public void initAudioPlayer(){
        audioEffectPlayer = engine.createAudioEffectPlayer();
        path1 = getExternalFilesDir("").getPath()+"/effect_1_stereo.wav";
        path2 = getExternalFilesDir("").getPath()+"/effect_2_mono.wav";
    }
    public void startPublish(){
        engine.startPreview(new ZegoCanvas(preview));
        engine.startPublishingStream(streamID);
        AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
        startPublishingButton.setText(getString(R.string.stop_publishing));
    }
    public void stopPublish(){
        engine.stopPreview();
        engine.stopPublishingStream();
        AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
        startPublishingButton.setText(getString(R.string.start_publishing));
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (isPlay){
                    engine.stopPlayingStream(streamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                    startPlayingButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    engine.startPlayingStream(streamID, new ZegoCanvas(playView));
                    startPlayingButton.setText(getString(R.string.stop_playing));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
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
                    startPublish();
                    isPublish = true;
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
    public void setLoadResourceButton1(){
        loadResourceButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.loadResource(audioEffectID1, path1, new IZegoAudioEffectPlayerLoadResourceCallback() {
                    @Override
                    public void onLoadResourceCallback(int i) {
                        if (i == 0){
                            AppLogger.getInstance().receiveCallback("Load resource successfully!");
                        } else {
                            AppLogger.getInstance().fail("[%d] Fail to load resource...",i);
                        }
                    }
                });
                AppLogger.getInstance().callApi("Load Resources from:%s",path1);
            }
        });
    }
    public void setUnloadButton1(){
        unloadButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.unloadResource(audioEffectID1);
                AppLogger.getInstance().callApi("Unload Resources");
            }
        });
    }
    public void setUnloadButton2(){
        unloadButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.unloadResource(audioEffectID2);
                AppLogger.getInstance().callApi("Unload Resources");
            }
        });
    }
    public void setLoadResourceButton2(){
        loadResourceButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.loadResource(audioEffectID2, path2, new IZegoAudioEffectPlayerLoadResourceCallback() {
                    @Override
                    public void onLoadResourceCallback(int i) {
                        if (i == 0){
                            AppLogger.getInstance().receiveCallback("Load resource successfully!");
                        } else {
                            AppLogger.getInstance().fail("[%d] Fail to load resource...",i);
                        }
                    }
                });
                AppLogger.getInstance().callApi("Load Resources from:%s",path2);
            }
        });
    }
    public void setPublishOutSwitch1(){
        publishOutSwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playConfig1.isPublishOut = isChecked;
                audioEffectPlayer.stop(audioEffectID1);
                AppLogger.getInstance().callApi("Enable Publish Out:%b",isChecked);
            }
        });
    }
    public void setPublishOutSwitch2(){
        publishOutSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playConfig2.isPublishOut = isChecked;
                audioEffectPlayer.stop(audioEffectID2);
                AppLogger.getInstance().callApi("Enable Publish Out:%b",isChecked);
            }
        });
    }
    public void setPlayButton1(){
        playButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playCountEdit1.getText().toString().isEmpty()){
                    playConfig1.playCount = Integer.valueOf(playCountEdit1.getText().toString());
                }
                audioEffectPlayer.start(audioEffectID1,path1,playConfig1);
                AppLogger.getInstance().callApi("Start playing :%s, AudioEffectID:%d",path1,audioEffectID1);
            }
        });
    }
    public void setPlayButton2(){
        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playCountEdit2.getText().toString().isEmpty()){
                    playConfig2.playCount = Integer.valueOf(playCountEdit2.getText().toString());
                }
                audioEffectPlayer.start(audioEffectID2,path2,playConfig2);
                AppLogger.getInstance().callApi("Start playing :%s, AudioEffectID:%d",path2,audioEffectID2);
            }
        });
    }
    public void setPauseButton1(){
        pauseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.pause(audioEffectID1);
                AppLogger.getInstance().callApi("Pause Playing :%s, AudioEffectID:%d",path1,audioEffectID1);
            }
        });
    }
    public void setPauseButton2(){
        pauseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.pause(audioEffectID2);
                AppLogger.getInstance().callApi("Pause Playing :%s, AudioEffectID:%d",path2,audioEffectID2);
            }
        });
    }
    public void setResumeButton1(){
        resumeButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.resume(audioEffectID1);
                AppLogger.getInstance().callApi("Resume Playing :%s, AudioEffectID:%d",path1,audioEffectID1);
            }
        });
    }
    public void setResumeButton2(){
        resumeButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.resume(audioEffectID2);
                AppLogger.getInstance().callApi("Resume Playing :%s, AudioEffectID:%d",path2,audioEffectID2);
            }
        });
    }
    public void setStopButton1(){
        stopButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.stop(audioEffectID1);
                AppLogger.getInstance().callApi("Stop Playing :%s, AudioEffectID:%d",path1,audioEffectID1);
            }
        });
    }
    public void setStopButton2(){
        stopButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.stop(audioEffectID2);
                AppLogger.getInstance().callApi("Stop Playing :%s, AudioEffectID:%d",path2,audioEffectID2);
            }
        });
    }
    public void setPauseAllButton(){
        pauseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.pauseAll();
                AppLogger.getInstance().callApi("Pause All");
            }
        });
    }
    public void setResumeAllButton(){
        resumeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.resumeAll();
                AppLogger.getInstance().callApi("Resume All");
            }
        });
    }
    public void setStopAllButton(){
        stopAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioEffectPlayer.stopAll();
                AppLogger.getInstance().callApi("Stop All");
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
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, AudioEffectPlayerActivity.class);
        activity.startActivity(intent);
    }
    private void initData() {
        fileNames.add("effect_1_stereo.wav");
        fileNames.add("effect_2_mono.wav");

        copyAssetsFiles(fileNames);
    }


    private void copyAssetsFiles(final List<String> fileNames) {
        new Thread() {
            public void run() {
                for (String fileName : fileNames) {
                    copyAssetsFile(fileName);
                }
            }
        }.start();
    }

    private void copyAssetsFile(String fileName) {
        final File file = new File(getExternalFilesDir(""), fileName);//getFilesDir()方法用于获取/data/data//files目录
        System.out.println("File Path---->" + file.getAbsolutePath());
        if (file.exists()) {
            System.out.println("File exists.");
            return;
        }
        try {
            // get Assets.
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            // Write file.
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}