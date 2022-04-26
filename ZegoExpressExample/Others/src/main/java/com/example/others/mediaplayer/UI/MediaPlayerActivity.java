package com.example.others.mediaplayer.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.others.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerSeekToCallback;
import im.zego.zegoexpress.constants.ZegoMediaPlayerAudioChannel;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zegoexpress.entity.ZegoVoiceChangerParam;

public class MediaPlayerActivity extends AppCompatActivity {

    Button startPublishingButton;
    Button startPlayingButton;
    TextureView preview;
    TextureView playView;
    TextView userIDText;
    TextView mediaTypeText;
    Button playButton;
    Button pauseButton;
    Button resumeButton;
    Button stopButton;
    SeekBar volumeSeekBar;
    SwitchMaterial repeatSwitch;
    SwitchMaterial auxSwitch;
    SwitchMaterial muteLocal;
    RadioGroup audioTrackIndex;
    TextView pitchText;
    SeekBar pitchSeekBar;
    TextView speedText;
    SeekBar speedSeekBar;
    TextureView mediaPlayerView;
    SeekBar progressBar;
    TextView roomState;

    EditText encodeResolutionWidth;
    EditText encodeResolutionHeight;
    EditText captureResolutionWidth;
    EditText captureResolutionHeight;
    TextView encodeResolutionTitle;
    TextView captureResolutionTitle;

    String userID;
    String roomID;
    String streamID;
    ZegoExpressEngine engine;
    ZegoMediaPlayer mediaPlayer;
    String path;

    //Store whether the user is playing the stream
    Boolean isPlay = false;
    //Store whether the user is publishing the stream
    Boolean isPublish = false;
    ZegoVoiceChangerParam voiceChangerParam = new ZegoVoiceChangerParam();

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        bindView();
        setDefaultValue();
        setLogComponent();
        setEventHandler();
        setApiCalledResult();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setMediaPlayer();
        setProgressBar();
        setProgressBarEvent();
        setPlayButtonEvent();
        setPauseButtonEvent();
        setResumeButtonEvent();
        setStopButtonEvent();
        setVolumeSeekBarEvent();
        setRepeatSwitchEvent();
        setEnableAuxSwitchEvent();
        setMuteLocalSwitchEvent();
        setAudioTrackIndexEvent();
        setPitchSeekBarEvent();
        setSpeedSeekBarEvent();
        addQuestionToast();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userIDText);
        mediaTypeText = findViewById(R.id.mediaType);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        mediaTypeText = findViewById(R.id.mediaType);
        progressBar = findViewById(R.id.mediaPlayerSeekBar);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        stopButton = findViewById(R.id.stopButton);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        repeatSwitch = findViewById(R.id.repeatSwitch);
        auxSwitch = findViewById(R.id.enableAuxSwitch);
        muteLocal = findViewById(R.id.muteLocalSwitch);
        audioTrackIndex = findViewById(R.id.audioTrack);
        pitchText = findViewById(R.id.pitch);
        speedText = findViewById(R.id.speed);
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        speedSeekBar = findViewById(R.id.speedSeekBar);
        mediaPlayerView = findViewById(R.id.mediaPlayerView);
        roomState = findViewById(R.id.roomState);

        encodeResolutionWidth = findViewById(R.id.encodeResolutionWidthMP);
        encodeResolutionHeight = findViewById(R.id.encodeResolutionHeightMP);
        captureResolutionWidth = findViewById(R.id.captureResolutionWidthMP);
        captureResolutionHeight = findViewById(R.id.captureResolutionHeightMP);
        encodeResolutionTitle = findViewById(R.id.encodeResolutionTitleMP);
        captureResolutionTitle = findViewById(R.id.captureResolutionTitleMP);
    }
    public void setDefaultValue(){
        userID = KeyCenter.getInstance().getUserID();
        roomID = "0027";
        streamID = "0027";
        // Get data from intent
        path = getIntent().getStringExtra("path");
        boolean isVideo = getIntent().getBooleanExtra("type",true);
        if (isVideo){
            mediaTypeText.setText("Video");
        } else {
            mediaTypeText.setText("Audio");
        }
        engine = ZegoExpressEngine.getEngine();
        setTitle(getString(R.string.media_player));
        roomState.setText(getEmojiStringByUnicode(roomConnectedEmoji));
        userIDText.setText(userID);
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;
                    encodeResolutionHeight.setEnabled(true);
                    encodeResolutionWidth.setEnabled(true);
                    captureResolutionHeight.setEnabled(true);
                    captureResolutionWidth.setEnabled(true);
                } else {
                    ZegoVideoConfig videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);
                    if (encodeResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeWidth = Integer.parseInt(encodeResolutionWidth.getText().toString());
                        encodeResolutionWidth.setEnabled(false);
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Width is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (encodeResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.encodeHeight = Integer.parseInt(encodeResolutionHeight.getText().toString());
                        encodeResolutionHeight.setEnabled(false);
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Encode Height is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (captureResolutionWidth.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Capture Width cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureWidth = Integer.parseInt(captureResolutionWidth.getText().toString());
                        captureResolutionWidth.setEnabled(false);
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Capture Width is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (captureResolutionHeight.getText().toString().equals(""))
                    {
                        Toast.makeText(getApplicationContext(), "Capture Height cannot be Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        videoConfig.captureHeight = Integer.parseInt(captureResolutionHeight.getText().toString());
                        captureResolutionHeight.setEnabled(false);
                    } catch (NumberFormatException e)
                    {
                        Toast.makeText(getApplicationContext(), "Capture Height is too large", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    engine.setVideoConfig(videoConfig);
                    engine.startPreview(new ZegoCanvas(preview));
                    engine.startPublishingStream(streamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
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
    public void setMediaPlayer(){
        mediaPlayer = engine.createMediaPlayer();
        AppLogger.getInstance().callApi("create Media Player!");
        mediaPlayer.setPlayerCanvas(new ZegoCanvas(mediaPlayerView));
        mediaPlayer.loadResource(path, new IZegoMediaPlayerLoadResourceCallback() {
            @Override
            public void onLoadResourceCallback(int i) {
                if (i == 0){
                    AppLogger.getInstance().receiveCallback("Load resource successfully!");
                    progressBar.setMax((int) (mediaPlayer.getTotalDuration()/1000));
                } else {
                    AppLogger.getInstance().fail("[%d] Fail to load resource...",i);
                }
            }
        });
    }
    public void setProgressBar(){
        mediaPlayer.setEventHandler(new IZegoMediaPlayerEventHandler() {
            @Override
            public void onMediaPlayerPlayingProgress(ZegoMediaPlayer mediaPlayer, long millisecond) {
                super.onMediaPlayerPlayingProgress(mediaPlayer, millisecond);
                int second = (int)(millisecond/1000);
                progressBar.setProgress(second);
            }
        });
    }
    public void setPlayButtonEvent(){
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                AppLogger.getInstance().callApi("Start Playing");
            }
        });
    }
    public void setPauseButtonEvent(){
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                AppLogger.getInstance().callApi("Pause Playing");
            }
        });
    }
    public void setResumeButtonEvent(){
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.resume();
                AppLogger.getInstance().callApi("Resume Playing");
            }
        });
    }
    public void setStopButtonEvent(){
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                AppLogger.getInstance().callApi("Stop Playing");
            }
        });
    }
    public void setProgressBarEvent(){
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = progressBar.getProgress();
                mediaPlayer.seekTo((long) (progress * 1000), new IZegoMediaPlayerSeekToCallback() {
                    @Override
                    public void onSeekToTimeCallback(int i) {
                        if (i == 0){
                            AppLogger.getInstance().receiveCallback("Seek to a given position successfully!");
                        } else {
                            AppLogger.getInstance().fail("[%d] Failed to seek to a given position",i);
                        }
                    }
                });
            }
        });
    }
    public void setVolumeSeekBarEvent(){
        volumeSeekBar.setMax(200);
        volumeSeekBar.setProgress(60);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mediaPlayer.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setRepeatSwitchEvent(){
        repeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mediaPlayer.enableRepeat(isChecked);
            }
        });
    }
    public void setEnableAuxSwitchEvent(){
        auxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mediaPlayer.enableAux(isChecked);
            }
        });
    }
    public void setMuteLocalSwitchEvent(){
        muteLocal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mediaPlayer.muteLocal(isChecked);
            }
        });
    }
    public void setAudioTrackIndexEvent(){
        audioTrackIndex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.audioTrack1){
                    mediaPlayer.setAudioTrackIndex(0);
                    AppLogger.getInstance().callApi("Set Audio Track: 0");
                } else {
                    mediaPlayer.setAudioTrackIndex(1);
                    AppLogger.getInstance().callApi("Set Audio Track: 1");
                }
            }
        });
    }
    public void setPitchSeekBarEvent(){
        pitchText.setText("0.00");
        pitchSeekBar.setMax(1600);
        pitchSeekBar.setProgress(800);
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                voiceChangerParam.pitch = (float)(progress-800)/100;
                mediaPlayer.setVoiceChangerParam(ZegoMediaPlayerAudioChannel.ALL,voiceChangerParam);
                pitchText.setText(String.valueOf(voiceChangerParam.pitch));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setSpeedSeekBarEvent(){
        speedText.setText("1.00");
        speedSeekBar.setMax(250);
        speedSeekBar.setProgress(50);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float curSpeed = (float)(progress+50)/100;
                mediaPlayer.setPlaySpeed(curSpeed);
                speedText.setText(String.valueOf(curSpeed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
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
    public void addQuestionToast() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.encodeResolutionTitleMP) {
                    Toast.makeText(getApplicationContext(), R.string.encodeResolutionTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (view.getId() == R.id.captureResolutionTitleMP) {
                    Toast.makeText(getApplicationContext(), R.string.captureResolutionTitle, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        };

        //add Toast in order to show info
        encodeResolutionTitle.setOnClickListener(listener);
        captureResolutionTitle.setOnClickListener(listener);
    }
    @Override
    protected void onDestroy() {
        engine.destroyMediaPlayer(mediaPlayer);
        engine.setEventHandler(null);
        super.onDestroy();
    }
}