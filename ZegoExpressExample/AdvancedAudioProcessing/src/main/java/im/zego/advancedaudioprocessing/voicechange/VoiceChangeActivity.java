package im.zego.advancedaudioprocessing.voicechange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.IOException;

import im.zego.advancedaudioprocessing.R;
import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioCaptureStereoMode;
import im.zego.zegoexpress.constants.ZegoAudioChannel;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoReverbPreset;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVoiceChangerPreset;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoReverbAdvancedParam;
import im.zego.zegoexpress.entity.ZegoReverbEchoParam;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVoiceChangerParam;

public class VoiceChangeActivity extends AppCompatActivity {

    TextView userIDText;
    TextView roomState;
    TextureView preview;
    TextureView playView;
    SwitchMaterial encoderStereoSwitch;
    AppCompatSpinner captureStereoSpinner;
    SwitchMaterial bgmMusicSwitch;
    Button startPlayingButton;
    Button startPublishingButton;
    AppCompatSpinner changerPresetSpinner;
    SeekBar pitchSeekBar;
    SwitchMaterial changerCustomParamSwitch;
    AppCompatSpinner reverbPresetSpinner;
    SeekBar roomSizeSeekBar;
    SeekBar damppingSeekBar;
    SeekBar wetGainSeekBar;
    SeekBar toneLowSeekBar;
    SeekBar preDelaySeekBar;
    SwitchMaterial reverbCustomParamSwitch;
    SeekBar reverberanceSeekBar;
    SwitchMaterial wetOnlySwitch;
    SeekBar dryGainSeekBar;
    SeekBar toneHighSeekBar;
    SeekBar stereoWidthSeekBar;
    AppCompatSpinner reverbEchoPresetSpinner;
    SwitchMaterial virtualStereoSwitch;
    SeekBar angleSeekBar;

    Long appID;
    String userID;
    String token;
    String roomID;
    String streamID;
    ZegoExpressEngine engine;
    ZegoUser user;
    ZegoAudioConfig audioConfig = new ZegoAudioConfig();
    MediaPlayer mediaPlayer;
    ZegoVoiceChangerParam changerParam = new ZegoVoiceChangerParam();
    ZegoReverbAdvancedParam advancedParam = new ZegoReverbAdvancedParam();
    ZegoReverbEchoParam echoParamEthereal = new ZegoReverbEchoParam();
    ZegoReverbEchoParam echoParamRobot = new ZegoReverbEchoParam();
    ZegoReverbEchoParam echoParamNone = new ZegoReverbEchoParam();

    //Store whether the user is publishing the stream
    boolean isPublish = false;
    //Store whether the user is playing the stream
    boolean isPlay = false;
    boolean enableChangerCustomParam = false;
    boolean enableReverbCustomParam = false;
    boolean enableVitualStereo = false;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_change);
        bindView();
        requestPermission();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoom();
        controlCustomParamRelatedWidgets();
        setStartPlayButtonEvent();
        setStartPublishButtonEvent();
        setEncoderStereoSwitch();
        setCaptureStereoSpinner();
        setBgmMusicSwitch();
        setPitchSeekBar();
        setReverbPresetSpinner();
        setReverbEchoPreset();
        setReverbCustomParamSwitch();
        setReverberanceSeekBar();
        setDamppingSeekBar();
        setRoomSizeSeekBar();
        setWetOnlySwitch();
        setWetGainSeekBar();
        setDryGainSeekBar();
        setToneLowSeekBar();
        setPreDelaySeekBar();
        setStereoWidthSeekBar();
        setReverbEchoPresetSpinner();
        setVirtualStereoSwitch();
        setAngleSeekBar();
        setLogComponent();
        setEventHandler();
        setChangerCustomParamSwitch();
        setChangerPresetSpinner();
        setApiCalledResult();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userID);
        roomState = findViewById(R.id.roomState);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        encoderStereoSwitch = findViewById(R.id.encoderStereoSwitch);
        captureStereoSpinner = findViewById(R.id.captureStereoSpinner);
        bgmMusicSwitch  = findViewById(R.id.bgmSwitch);
        startPlayingButton = findViewById(R.id.startPlayButton);
        startPublishingButton = findViewById(R.id.startPublishButton);
        changerPresetSpinner = findViewById(R.id.changerPresetSpinner);
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        changerCustomParamSwitch = findViewById(R.id.voiceChangeCustomParamSwitch);
        reverbPresetSpinner = findViewById(R.id.reverbPresetSpinner);
        roomSizeSeekBar = findViewById(R.id.roomSizeSeekBar);
        damppingSeekBar = findViewById(R.id.damppingSeekBar);
        wetGainSeekBar = findViewById(R.id.wetGainSeekBar);
        toneLowSeekBar = findViewById(R.id.toneLowSeekBar);
        preDelaySeekBar = findViewById(R.id.preDelaySeekBar);
        reverbCustomParamSwitch = findViewById(R.id.reverbCustomParamSwitch);
        reverberanceSeekBar = findViewById(R.id.reverberanceSeekBar);
        wetOnlySwitch = findViewById(R.id.wetOnlySwitch);
        dryGainSeekBar = findViewById(R.id.dryGainSeekBar);
        toneHighSeekBar = findViewById(R.id.toneHighSeekBar);
        stereoWidthSeekBar = findViewById(R.id.stereoWidthSeekBar);
        reverbEchoPresetSpinner = findViewById(R.id.reverbEchoPresetSpinner);
        virtualStereoSwitch = findViewById(R.id.virtualStereoSwitch);
        angleSeekBar = findViewById(R.id.angleSeekBar);
    }
   // request for permission
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
    public void setDefaultValue(){
        //set default publish  and play streamID
        streamID = "0016";
        roomID = "0016";

        userIDText.setText(userID);
        setTitle(getString(R.string.voice_change_reverb_stereo));

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = getAssets().openFd("sample.mp3");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),fileDescriptor.getLength());
            mediaPlayer.prepare();
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setEncoderStereoSwitch(){
        encoderStereoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isPublish){
                    stopPublish();
                    isPublish = false;
                }
                if (isChecked) {
                    audioConfig.channel = ZegoAudioChannel.STEREO;
                } else {
                    audioConfig.channel = ZegoAudioChannel.MONO;
                }
                engine.setAudioConfig(audioConfig);
            }
        });
    }
    public void setCaptureStereoSpinner(){
        captureStereoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPublish){
                    stopPublish();
                    isPublish = false;
                }
                String[] options = getResources().getStringArray(R.array.captureStereoMode);
                switch (options[position]){
                    case "NONE":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.NONE);
                        break;
                    case "AlWAYS":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.ALWAYS);
                        break;
                    case "ADAPTIVE":
                        engine.setAudioCaptureStereoMode(ZegoAudioCaptureStereoMode.ADAPTIVE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setBgmMusicSwitch(){
        bgmMusicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                }
            }
        });
    }
    public void setChangerPresetSpinner(){
        changerPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.voicePreset);
                switch(options[position]){
                    case "NONE":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.NONE);
                        break;
                    case "MEN_TO_CHILD":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.MEN_TO_CHILD);
                        break;
                    case "MEN_TO_WOMEN":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.MEN_TO_WOMEN);
                        break;
                    case "WOMEN_TO_CHILD":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.WOMEN_TO_CHILD);
                        break;
                    case "WOMEN_TO_MEN":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.WOMEN_TO_MEN);
                        break;
                    case "FOREIGNER":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.FOREIGNER);
                        break;
                    case "OPTIMUS_PRIME":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.OPTIMUS_PRIME);
                        break;
                    case "ANDROID":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.ANDROID);
                        break;
                    case "ETHEREAL":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.ETHEREAL);
                        break;
                    case "MALE_MAGNETIC":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.MALE_MAGNETIC);
                        break;
                    case "FEMALE_FRESH":
                        engine.setVoiceChangerPreset(ZegoVoiceChangerPreset.FEMALE_FRESH);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setPitchSeekBar(){
        pitchSeekBar.setEnabled(false);
        pitchSeekBar.setMax(160);
        pitchSeekBar.setProgress(80);
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changerParam.pitch = ((float)progress/10-8.0f);
                engine.setVoiceChangerParam(changerParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
    public void setChangerCustomParamSwitch(){
        changerCustomParamSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    enableChangerCustomParam = true;
                    pitchSeekBar.setEnabled(true);
                    changerPresetSpinner.setEnabled(false);
                } else {
                    enableChangerCustomParam = false;
                    pitchSeekBar.setEnabled(false);
                    changerParam.pitch = 0.0f;
                    changerPresetSpinner.setEnabled(true);
                    engine.setVoiceChangerParam(changerParam);
                }
            }
        });
    }
    public void setReverbPresetSpinner(){
        reverbPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.reverbPreset);
                switch (options[position]){
                    case "NONE":
                        engine.setReverbPreset(ZegoReverbPreset.NONE);
                        break;
                    case "SOFT_ROOM":
                        engine.setReverbPreset(ZegoReverbPreset.SOFT_ROOM);
                        break;
                    case "LARGE_ROOM":
                        engine.setReverbPreset(ZegoReverbPreset.LARGE_ROOM);
                        break;
                    case "CONCERT_HALL":
                        engine.setReverbPreset(ZegoReverbPreset.CONCERT_HALL);
                        break;
                    case "VALLEY":
                        engine.setReverbPreset(ZegoReverbPreset.VALLEY);
                        break;
                    case "RECORDING_STUDIO":
                        engine.setReverbPreset(ZegoReverbPreset.RECORDING_STUDIO);
                        break;
                    case "BASEMENT":
                        engine.setReverbPreset(ZegoReverbPreset.BASEMENT);
                        break;
                    case "KTV":
                        engine.setReverbPreset(ZegoReverbPreset.KTV);
                        break;
                    case "POPULAR":
                        engine.setReverbPreset(ZegoReverbPreset.POPULAR);
                        break;
                    case "ROCK":
                        engine.setReverbPreset(ZegoReverbPreset.ROCK);
                        break;
                    case "VOCAL_CONCERT":
                        engine.setReverbPreset(ZegoReverbPreset.VOCAL_CONCERT);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setReverbEchoPreset(){
        echoParamEthereal.inGain = 0.8f;
        echoParamEthereal.outGain = 1.0f;
        echoParamEthereal.numDelays = 7;
        echoParamEthereal.delay = new int[]{230,460,690,920,1150,1380,1610};
        echoParamEthereal.decay = new float[]{0.41f,0.18f,0.08f,0.03f,0.009f,0.003f,0.001f};

        echoParamRobot.inGain = 0.8f;
        echoParamRobot.outGain = 1.0f;
        echoParamRobot.numDelays = 7;
        echoParamRobot.delay = new int[]{60,210,180,240,300,360,420};
        echoParamRobot.decay = new float[]{0.51f,0.26f,0.12f,0.05f,0.02f,0.009f,0.001f};

        echoParamNone.inGain = 1.0f;
        echoParamNone.outGain = 1.0f;
        echoParamNone.numDelays = 0;
        echoParamNone.delay = new int[]{0,0,0,0,0,0,40};
        echoParamNone.decay = new float[]{0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
    }
    public void setReverbCustomParamSwitch(){
        reverbCustomParamSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    reverbPresetSpinner.setEnabled(false);
                } else {
                    reverbPresetSpinner.setEnabled(true);
                }
                enableReverbCustomParam = isChecked;
                controlCustomParamRelatedWidgets();
                engine.setReverbAdvancedParam(advancedParam);
            }
        });
    }
    public void setRoomSizeSeekBar(){
        roomSizeSeekBar.setMax(100);
        roomSizeSeekBar.setProgress(0);
        roomSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.roomSize = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setReverberanceSeekBar(){
        reverberanceSeekBar.setMax(100);
        reverberanceSeekBar.setProgress(0);
        reverberanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.reverberance = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setDamppingSeekBar(){
        damppingSeekBar.setMax(100);
        damppingSeekBar.setProgress(0);
        damppingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.damping = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setWetOnlySwitch(){
        wetOnlySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                advancedParam.wetOnly = isChecked;
                engine.setReverbAdvancedParam(advancedParam);
            }
        });
    }
    public void setWetGainSeekBar(){
        wetGainSeekBar.setMax(30);
        wetGainSeekBar.setProgress(0);
        wetGainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.wetGain = progress - 20;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setDryGainSeekBar(){
        dryGainSeekBar.setMax(30);
        dryGainSeekBar.setProgress(0);
        dryGainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.dryGain = progress - 20;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setToneLowSeekBar(){
        toneLowSeekBar.setMax(100);
        toneLowSeekBar.setProgress(100);
        toneLowSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.toneLow = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setPreDelaySeekBar(){
        preDelaySeekBar.setMax(200);
        preDelaySeekBar.setProgress(0);
        preDelaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.preDelay = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setStereoWidthSeekBar(){
        stereoWidthSeekBar.setMax(100);
        stereoWidthSeekBar.setProgress(0);
        stereoWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                advancedParam.stereoWidth = progress;
                engine.setReverbAdvancedParam(advancedParam);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setReverbEchoPresetSpinner(){
        reverbEchoPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.reverbEchoPreset);
                switch (options[position]){
                    case "NONE":
                        engine.setReverbEchoParam(echoParamNone);
                        break;
                    case "ROBOT":
                        engine.setReverbEchoParam(echoParamRobot);
                        break;
                    case "ETHEREAL":
                        engine.setReverbEchoParam(echoParamEthereal);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setAngleSeekBar(){
        angleSeekBar.setEnabled(false);
        angleSeekBar.setMax(360 + 1); // To make the "-1" as the minimum value
        angleSeekBar.setProgress(90 + 1);
        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress - 1;
                engine.enableVirtualStereo(true, progress);

                AppLogger.getInstance().callApi("Enable Virtual Stero, angle: %d", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setVirtualStereoSwitch(){
        virtualStereoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (!engine.getAudioConfig().channel.equals(ZegoAudioChannel.STEREO)) {
                        // Set to stereo mode
                        encoderStereoSwitch.setChecked(true);
                    }
                    angleSeekBar.setEnabled(true);
                    angleSeekBar.setProgress(90);
                    engine.enableVirtualStereo(true,90);
                } else {
                    angleSeekBar.setEnabled(false);
                    engine.enableVirtualStereo(false,0);
                }
            }
        });
    }
    public void controlCustomParamRelatedWidgets(){
        boolean isClickable;
        if (enableReverbCustomParam) {
            isClickable = true;
        } else {
            isClickable = false;
        }
        roomSizeSeekBar.setEnabled(isClickable);
        roomSizeSeekBar.setProgress(0);
        advancedParam.roomSize = 0.0f;
        damppingSeekBar.setEnabled(isClickable);
        damppingSeekBar.setProgress(0);
        advancedParam.damping = 0.0f;
        wetGainSeekBar.setEnabled(isClickable);
        wetGainSeekBar.setProgress(20);
        advancedParam.wetGain = 0.0f;
        toneLowSeekBar.setEnabled(isClickable);
        toneLowSeekBar.setProgress(100);
        advancedParam.toneLow = 100;
        toneHighSeekBar.setEnabled(isClickable);
        toneHighSeekBar.setProgress(100);
        advancedParam.toneHigh = 100;
        preDelaySeekBar.setEnabled(isClickable);
        preDelaySeekBar.setProgress(0);
        advancedParam.preDelay = 0;
        dryGainSeekBar.setEnabled(isClickable);
        dryGainSeekBar.setProgress(20);
        advancedParam.dryGain = 0.0f;
        wetOnlySwitch.setEnabled(isClickable);
        wetOnlySwitch.setChecked(false);
        advancedParam.wetOnly = false;
        reverberanceSeekBar.setEnabled(isClickable);
        reverberanceSeekBar.setProgress(0);
        advancedParam.reverberance = 0.0f;
        stereoWidthSeekBar.setEnabled(isClickable);
        stereoWidthSeekBar.setProgress(0);
        advancedParam.stereoWidth = 0;
    }
    public void startPublish(){
        engine.startPreview(new ZegoCanvas(preview));
        engine.setAudioConfig(audioConfig);
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
    // Set log commponent. It includes a pop-up dialog.
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
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, VoiceChangeActivity.class);
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
    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        mediaPlayer.release();
        super.onDestroy();
    }
}