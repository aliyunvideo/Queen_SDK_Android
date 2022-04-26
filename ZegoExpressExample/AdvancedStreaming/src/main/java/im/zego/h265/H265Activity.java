package im.zego.h265;

import static java.lang.Math.pow;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.streammonitoring.R;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoEngineState;
import im.zego.zegoexpress.constants.ZegoMixerInputContentType;
import im.zego.zegoexpress.constants.ZegoNetworkMode;
import im.zego.zegoexpress.constants.ZegoNetworkSpeedTestType;
import im.zego.zegoexpress.constants.ZegoPlayerMediaEvent;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoCodecID;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoMixerAudioConfig;
import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerOutput;
import im.zego.zegoexpress.entity.ZegoMixerOutputVideoConfig;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestQuality;
import im.zego.zegoexpress.entity.ZegoPerformanceStatus;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoSoundLevelInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoStreamRelayCDNInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zegoexpress.entity.ZegoWatermark;

public class H265Activity extends AppCompatActivity {

    // Preview view
    TextView localPreviewViewTextView;
    TextView localPreviewViewVideoCodecTextView;
    TextView localPreviewViewResolutionTextView;
    TextView localPreviewViewQualityTextView;
    TextureView previewView;

    // Play view I
    TextView remotePlayViewTextView;
    TextView remotePlayViewVideoCodecTextView;
    TextView remotePlayViewResolutionTextView;
    TextView remotePlayViewQualityTextView;
    TextureView remotePlayView;
    EditText remotePlayStreamIDEditText;
    Button remoteStartPlayingButton;

    // Play view II
    TextView remotePlayView2TextView;
    TextView remotePlayView2VideoCodecTextView;
    TextView remotePlayView2ResolutionTextView;
    TextView remotePlayView2QualityTextView;
    TextureView remotePlayView2;
    EditText remotePlayStreamID2EditText;
    Button remoteStartPlaying2Button;

    // Play view III
    TextView remotePlayView3TextView;
    TextView remotePlayView3VideoCodecTextView;
    TextView remotePlayView3ResolutionTextView;
    TextView remotePlayView3QualityTextView;
    TextureView remotePlayView3;
    EditText remotePlayStreamID3EditText;
    Button remoteStartPlaying3Button;

    // PublishStream
    String publishStreamID;
    int publishFPS;
    int publishResolutionWidth;
    int publishResolutionHeight;
    int publishBitrate;
    EditText publishStreamIDEditText;
    EditText publishBitrateEditText;
    Spinner publishFPSSpinner;
    Spinner publishResolutionSpinner;
    Button startPublishingButton;

    // MixStream
    int mixStreamFPS;
    int mixStreamResolutionWidth;
    int mixStreamResolutionHeight;
    int mixStreamH264Bitrate;
    int mixStreamH265Bitrate;
    Spinner mixStreamFPSSpinner;
    Spinner mixStreamResolutionSpinner;
    EditText mixStreamBitrateH264EditText;
    EditText mixStreamIDH264EditText;
    EditText mixStreamBitrateH265EditText;
    EditText mixStreamIDH265EditText;
    Button startMixStreamButton;

    ZegoExpressEngine engine;
    ZegoMixerTask mixerTask;

    String roomID;
    long appID;
    String userID;
    String token;

    Map<Integer, String> playViewToStreamIDMap;
    ArrayList<ZegoStream> remoteStreamList;
    ZegoPublisherState publisherState;
    boolean isMixing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h265);

        playViewToStreamIDMap = new HashMap<Integer, String>();
        remoteStreamList = new ArrayList<>();

        bindView();
        getAppIDAndUserIDAndToken();
        setLogComponent();
        initVideoConfig();
        setDefaultValue();
        getUserSettings();
        setupEngineAndLogin();
        setEventHandle();
        startPreview();
        setRemoteStartPlayingButtonEvent();
        setRemoteStartPlaying2ButtonEvent();
        setRemoteStartPlaying3ButtonEvent();
        setPublishFPSSpinnerEvent();
        setPublishResolutionSpinnerEvent();
        setStartPublishingButtonEvent();
        setmixStreamFPSSpinnerEvent();
        setMixStreamResolutionSpinnerEvent();
        setStartMixStreamButtonEvent();

        setApiCalledResult();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(publisherState == ZegoPublisherState.PUBLISHING) {
            engine.stopPublishingStream();
        }
        if(isMixing) {
            engine.stopMixerTask(mixerTask, null);
        }
        engine.logoutRoom();
        engine.setEventHandler(null);
        ZegoExpressEngine.destroyEngine(null);
    }

    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }

    public void bindView(){
        // Preview view
        localPreviewViewTextView = findViewById(R.id.localPreviewViewTextView);
        localPreviewViewVideoCodecTextView = findViewById(R.id.localPreviewViewVideoCodecTextView);
        localPreviewViewResolutionTextView = findViewById(R.id.localPreviewViewResolutionTextView);
        localPreviewViewQualityTextView = findViewById(R.id.localPreviewViewQualityTextView);
        previewView = findViewById(R.id.previewView);

        // Play view I
        remotePlayViewTextView = findViewById(R.id.playViewTextView);
        remotePlayViewVideoCodecTextView = findViewById(R.id.playStreamVideoCodecTextView);
        remotePlayViewResolutionTextView = findViewById(R.id.playStreamResolutionTextView);
        remotePlayViewQualityTextView = findViewById(R.id.playStreamQualityTextView);
        remotePlayView = findViewById(R.id.playView);
        remotePlayStreamIDEditText = findViewById(R.id.playStreamIDEditText);
        remoteStartPlayingButton = findViewById(R.id.h265StartPlayingButton);

        // Play view II
        remotePlayView2TextView = findViewById(R.id.playView2TextView);
        remotePlayView2VideoCodecTextView = findViewById(R.id.playStream2VideoCodecTextView);
        remotePlayView2ResolutionTextView = findViewById(R.id.playStream2ResolutionTextView);
        remotePlayView2QualityTextView = findViewById(R.id.playStream2QualityTextView);
        remotePlayView2 = findViewById(R.id.playView2);
        remotePlayStreamID2EditText = findViewById(R.id.playStreamID2EditText);
        remoteStartPlaying2Button = findViewById(R.id.h265StartPlaying2Button);

        // Play view III
        remotePlayView3TextView = findViewById(R.id.playView3TextView);
        remotePlayView3VideoCodecTextView = findViewById(R.id.playStream3VideoCodecTextView);
        remotePlayView3ResolutionTextView = findViewById(R.id.playStream3ResolutionTextView);
        remotePlayView3QualityTextView = findViewById(R.id.playStream3QualityTextView);
        remotePlayView3 = findViewById(R.id.playView3);
        remotePlayStreamID3EditText = findViewById(R.id.playStreamID3EditText);
        remoteStartPlaying3Button = findViewById(R.id.h265StartPlaying3Button);

        // Publish config
        publishStreamIDEditText = findViewById(R.id.publishStreamIDEditText);
        publishBitrateEditText = findViewById(R.id.publishBitrateEditText);
        publishFPSSpinner = findViewById(R.id.publishFPSSpinner);
        publishResolutionSpinner = findViewById(R.id.publishResolutionSpinner);
        startPublishingButton = findViewById(R.id.publishingButton);

        // MixStream config
        mixStreamFPSSpinner = findViewById(R.id.mixStreamFPSSpinner);
        mixStreamResolutionSpinner = findViewById(R.id.mixStreamResolutionSpinner);
        mixStreamBitrateH264EditText = findViewById(R.id.mixStreamH264BitrateEditText);
        mixStreamIDH264EditText = findViewById(R.id.mixStreamIDH264EditText);
        mixStreamBitrateH265EditText = findViewById(R.id.mixStreamH265BitrateEditText);
        mixStreamIDH265EditText = findViewById(R.id.mixStreamIDH265EditText);
        startMixStreamButton = findViewById(R.id.mixStreamButton);
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

    public void initVideoConfig() {
        // PublishStream config
        publishFPS = 15;
        publishResolutionWidth = 360;
        publishResolutionHeight = 600;

        // MixStream config
        mixStreamFPS = 15;
        mixStreamResolutionWidth = 360;
        mixStreamResolutionHeight = 600;

        // update bitrate
        updatePublishStreamBitrate();
        updateMixStreamBitrate();
    }

    public void setDefaultValue(){
        publishStreamIDEditText.setText("0001");
        mixStreamIDH264EditText.setText("h264");
        mixStreamIDH265EditText.setText("h265");

        remotePlayStreamIDEditText.setText("0001");
        remotePlayStreamID2EditText.setText("h264");
        remotePlayStreamID3EditText.setText("h265");

        localPreviewViewTextView.setText(R.string.preview);
        remotePlayViewTextView.setText(R.string.play_stream);
        remotePlayView2TextView.setText(R.string.play_stream);
        remotePlayView3TextView.setText(R.string.play_stream);
    }

    public void getUserSettings() {
        roomID = getIntent().getStringExtra("roomID");
    }

    public void setupEngineAndLogin() {

        //initialize ZegoEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        // H265 need hardware encode(publishing) and hardware decoder(playing)
        engine.enableHardwareEncoder(true);
        engine.enableHardwareDecoder(true);
        if(engine.isVideoEncoderSupported(ZegoVideoCodecID.getZegoVideoCodecID(3))) {
            AppLogger.getInstance().callApi("Current device support H265 encoder.");
        } else {
            AppLogger.getInstance().callApi("Current device does not support H265 encoder.");
        }
        if(engine.isVideoDecoderSupported(ZegoVideoCodecID.getZegoVideoCodecID(3))) {
            AppLogger.getInstance().callApi("Current device support H265 decoder.");
        } else {
            AppLogger.getInstance().callApi("Current device does not support H265 decoder.");
        }
        //create the user
        ZegoUser user = new ZegoUser(userID);
        //login room
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        engine.loginRoom(roomID, user, config);

        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
    }

    public void startPreview() {
        updateVideoConfig();

        ZegoCanvas previewCanvas = new ZegoCanvas(previewView);
        engine.startPreview(previewCanvas);
    }

    public void setRemoteStartPlayingButtonEvent() {
        remoteStartPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String streamID = remotePlayStreamIDEditText.getText().toString();
                boolean selected = v.isSelected();
                if(selected) {
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                    engine.stopPlayingStream(streamID);
                    remotePlayStreamIDEditText.setEnabled(true);
                    playViewToStreamIDMap.remove(1);
                    remoteStartPlayingButton.setText(R.string.start_playing);
                } else {
                    ZegoCanvas playCanvas = new ZegoCanvas(remotePlayView);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                    engine.startPlayingStream(streamID, playCanvas);
                    remotePlayStreamIDEditText.setEnabled(false);
                    playViewToStreamIDMap.put(1, streamID);
                    remoteStartPlayingButton.setText(R.string.stop_playing);
                }
                v.setSelected(!selected);
            }
        });
    }

    public void setRemoteStartPlaying2ButtonEvent() {
        remoteStartPlaying2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String streamID = remotePlayStreamID2EditText.getText().toString();
                boolean selected = v.isSelected();
                if(selected) {
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                    engine.stopPlayingStream(streamID);
                    remotePlayStreamID2EditText.setEnabled(true);
                    playViewToStreamIDMap.remove(2);
                    remoteStartPlaying2Button.setText(R.string.start_playing);
                } else {
                    ZegoCanvas playCanvas = new ZegoCanvas(remotePlayView2);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                    engine.startPlayingStream(streamID, playCanvas);
                    remotePlayStreamID2EditText.setEnabled(false);
                    playViewToStreamIDMap.put(new Integer(2), streamID);
                    remoteStartPlaying2Button.setText(R.string.stop_playing);
                }
                v.setSelected(!selected);
            }
        });
    }

    public void setRemoteStartPlaying3ButtonEvent() {
        remoteStartPlaying3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String streamID = remotePlayStreamID3EditText.getText().toString();
                boolean selected = v.isSelected();
                if(selected) {
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                    engine.stopPlayingStream(streamID);
                    remotePlayStreamID3EditText.setEnabled(true);
                    playViewToStreamIDMap.remove(new Integer(3));
                    remoteStartPlaying3Button.setText(R.string.start_playing);
                } else {
                    ZegoCanvas playCanvas = new ZegoCanvas(remotePlayView3);
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                    engine.startPlayingStream(streamID, playCanvas);
                    remotePlayStreamID3EditText.setEnabled(false);
                    playViewToStreamIDMap.put(new Integer(3), streamID);
                    remoteStartPlaying3Button.setText(R.string.stop_playing);
                }
                v.setSelected(!selected);
            }
        });
    }

    public void setPublishFPSSpinnerEvent() {
        publishFPSSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] options = getResources().getStringArray(R.array.h265_fps);
                switch (options[i]){
                    case "15":
                        publishFPS = 15;
                        updatePublishStreamBitrate();
                        break;
                    case "30":
                        publishFPS = 30;
                        updatePublishStreamBitrate();
                        break;
                    case "60":
                        publishFPS = 60;
                        updatePublishStreamBitrate();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setPublishResolutionSpinnerEvent() {
        publishResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] options = getResources().getStringArray(R.array.h265_resolution);
                switch (options[i]){
                    case "360x600":
                        publishResolutionWidth = 360;
                        publishResolutionHeight = 600;
                        updatePublishStreamBitrate();
                        break;
                    case "720x1280":
                        publishResolutionWidth = 720;
                        publishResolutionHeight = 1280;
                        updatePublishStreamBitrate();
                        break;
                    case "1080x1920":
                        publishResolutionWidth = 1080;
                        publishResolutionHeight = 1920;
                        updatePublishStreamBitrate();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setStartPublishingButtonEvent() {
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(publisherState == ZegoPublisherState.PUBLISHING) {
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",publishStreamID);
                    engine.stopPublishingStream();
                } else {
                    updateVideoConfig();
                    publishStreamID = publishStreamIDEditText.getText().toString();
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",publishStreamID);
                    engine.startPublishingStream(publishStreamID);
                }
            }
        });
    }

    public void setmixStreamFPSSpinnerEvent() {
        mixStreamFPSSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] options = getResources().getStringArray(R.array.h265_fps);
                switch (options[i]){
                    case "15":
                        mixStreamFPS = 15;
                        updateMixStreamBitrate();
                        break;
                    case "30":
                        mixStreamFPS = 30;
                        updateMixStreamBitrate();
                        break;
                    case "60":
                        mixStreamFPS = 60;
                        updateMixStreamBitrate();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setMixStreamResolutionSpinnerEvent() {
        mixStreamResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] options = getResources().getStringArray(R.array.h265_resolution);
                switch (options[i]){
                    case "360x600":
                        mixStreamResolutionWidth = 360;
                        mixStreamResolutionHeight = 600;
                        updateMixStreamBitrate();
                        break;
                    case "720x1280":
                        mixStreamResolutionWidth = 720;
                        mixStreamResolutionHeight = 1280;
                        updateMixStreamBitrate();
                        break;
                    case "1080x1920":
                        mixStreamResolutionWidth = 1080;
                        mixStreamResolutionHeight = 1920;
                        updateMixStreamBitrate();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setStartMixStreamButtonEvent() {
        startMixStreamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMixing){
                    stopMixStream();
                } else {
                    startMixStream();
                }
            }
        });
    }

    public void startMixStream() {
        AppLogger.getInstance().callApi("Start mixer task");

        String taskID = String.format("%s_MixStream", roomID);
        ZegoMixerTask task = new ZegoMixerTask(taskID);

        ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig(mixStreamResolutionWidth, mixStreamResolutionHeight, mixStreamFPS, 3000);
        task.setVideoConfig(videoConfig);

        int streamCount = checkNumberOfStream();
        if (streamCount <= 0) {
            AppLogger.getInstance().callApi("Start mix stream fail, because the number of stream is 0.");
        }

        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();

        Rect firstRect = new Rect(0, 0, videoConfig.width/2, videoConfig.height/2);
        Rect secondRect = new Rect(videoConfig.width/2, 0, videoConfig.width, videoConfig.height/2);
        Rect thirdRect = new Rect(0, videoConfig.height/2, videoConfig.width/2, videoConfig.height);
        Rect fourthRect = new Rect(videoConfig.width/2, videoConfig.height/2, videoConfig.width, videoConfig.height);
        Rect[] rectArrayList = new Rect[]{firstRect, secondRect, thirdRect, fourthRect};

        if(publisherState == ZegoPublisherState.PUBLISHING) {
            ZegoMixerInput firstInput = new ZegoMixerInput(publishStreamID, ZegoMixerInputContentType.VIDEO, rectArrayList[0]);
            inputList.add(firstInput);
            for(int idx = 0; idx < 3 && idx < remoteStreamList.size(); ++idx) {
                ZegoMixerInput input = new ZegoMixerInput(remoteStreamList.get(idx).streamID, ZegoMixerInputContentType.VIDEO, rectArrayList[idx + 1]);
                inputList.add(input);
            }
        } else {
            for(int idx = 0; idx < 4 && idx < remoteStreamList.size(); ++idx) {
                ZegoMixerInput input = new ZegoMixerInput(remoteStreamList.get(idx).streamID, ZegoMixerInputContentType.VIDEO, rectArrayList[idx]);
                inputList.add(input);
            }
        }
        task.setInputList(inputList);

        ArrayList<ZegoMixerOutput> outputList = new ArrayList<>();

        ZegoMixerOutput outputH264 = new ZegoMixerOutput(mixStreamIDH264EditText.getText().toString());
        ZegoMixerOutputVideoConfig outputH264VideoConfig = new ZegoMixerOutputVideoConfig(ZegoVideoCodecID.getZegoVideoCodecID(0), mixStreamH264Bitrate);
        outputH264.setVideoConfig(outputH264VideoConfig);
        outputList.add(outputH264);
        ZegoMixerOutput outputH265 = new ZegoMixerOutput(mixStreamIDH265EditText.getText().toString());
        ZegoMixerOutputVideoConfig outputH265VideoConfig = new ZegoMixerOutputVideoConfig(ZegoVideoCodecID.getZegoVideoCodecID(3), mixStreamH265Bitrate);
        outputH265.setVideoConfig(outputH265VideoConfig);
        outputList.add(outputH265);
        task.setOutputList(outputList);

        engine.startMixerTask(task, new IZegoMixerStartCallback() {

            @Override
            public void onMixerStartResult(int errorCode, JSONObject var2) {
                AppLogger.getInstance().receiveCallback("onMixerStartResult: result = " + errorCode);
                if (errorCode != 0) {
                    String msg = getString(R.string.tx_mixer_start_fail) + errorCode;
                    Toast.makeText(H265Activity.this, msg, Toast.LENGTH_SHORT).show();
                }
                else {
                    String msg = getString(R.string.tx_mixer_start_ok);
                    Toast.makeText(H265Activity.this, msg, Toast.LENGTH_SHORT).show();
                    isMixing = true;
                    updateStartMixStreamButtonText(isMixing);
                }
            }
        });
    }

    public void stopMixStream() {
        AppLogger.getInstance().callApi("Stop mixer task");
        engine.stopMixerTask(mixerTask, new IZegoMixerStopCallback() {
            @Override
            public void onMixerStopResult(int i) {
                AppLogger.getInstance().callApi("Stop mixer task result errorCode: %d", i);
            }
        });

        isMixing = false;
        updateStartMixStreamButtonText(isMixing);
    }


    public void setEventHandle() {
        engine.setEventHandler(new IZegoEventHandler() {

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                AppLogger.getInstance().callApi("Room State Update Callback: %d, errorCode: %d, roomID: %s", state.value(), errorCode, roomID);
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                for (int i = 0; i < streamList.size(); i++) {
                    boolean isFind = false;
                    for(int j = 0;j<remoteStreamList.size();j++)
                    {
                        String streamID = remoteStreamList.get(j).streamID;
                        if(streamID.equals(streamList.get(i).streamID))
                        {
                            isFind = true;
                            if (updateType == ZegoUpdateType.DELETE) {
                                remoteStreamList.remove(j--);
                            }
                        }
                    }
                    if(isFind == false)
                    {
                        if (updateType == ZegoUpdateType.ADD) {
                            remoteStreamList.add(streamList.get(i));
                        }
                    }

                    AppLogger.getInstance().i("onRoomStreamUpdate: roomID = " + roomID + ", updateType =" + updateType + ", streamID = " + streamList.get(i).streamID);
                }
                if(isMixing) {
                    stopMixStream();
                    startMixStream();
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                updateStartPublishingButtonText(state);
            }

            @Override
            public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                super.onPublisherQualityUpdate(streamID, quality);
                String videoCodec = "";
                switch (quality.videoCodecID.value()) {
                    case 0:
                        videoCodec = "H264";
                        break;
                    case 1:
                        videoCodec = "SVC";
                        break;
                    case 2:
                        videoCodec = "VP8";
                        break;
                    case 3:
                        videoCodec = "H265";
                        break;
                    default:
                        break;
                }
                localPreviewViewVideoCodecTextView.setText("VideoCodec: " + videoCodec);
                String text = String.format("Bitrate: %.2fkbps \nFPS: %.2f", quality.videoKBPS, quality.videoEncodeFPS);
                localPreviewViewQualityTextView.setText(text);
                AppLogger.getInstance().callApi("Publisher Quality Update:%s",streamID);
            }

            @Override
            public void onPublisherCapturedAudioFirstFrame() {
                super.onPublisherCapturedAudioFirstFrame();
            }

            @Override
            public void onPublisherCapturedVideoFirstFrame(ZegoPublishChannel channel) {
                super.onPublisherCapturedVideoFirstFrame(channel);
            }

            @Override
            public void onPublisherRenderVideoFirstFrame(ZegoPublishChannel channel) {
                super.onPublisherRenderVideoFirstFrame(channel);
            }

            @Override
            public void onPublisherVideoSizeChanged(int width, int height, ZegoPublishChannel channel) {
                super.onPublisherVideoSizeChanged(width, height, channel);
                String text = String.format("Resolution: %dx%d", width, height);
                localPreviewViewResolutionTextView.setText(text);
            }

            @Override
            public void onPublisherRelayCDNStateUpdate(String streamID, ArrayList<ZegoStreamRelayCDNInfo> infoList) {
                super.onPublisherRelayCDNStateUpdate(streamID, infoList);
            }

            @Override
            public void onPublisherVideoEncoderChanged(ZegoVideoCodecID fromCodecID, ZegoVideoCodecID toCodecID, ZegoPublishChannel channel) {
                super.onPublisherVideoEncoderChanged(fromCodecID, toCodecID, channel);
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
            }

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
                String videoCodec = "VideoCodec: ";
                switch (quality.videoCodecID.value()) {
                    case 0:
                        videoCodec += "H264";
                        break;
                    case 1:
                        videoCodec += "SVC";
                        break;
                    case 2:
                        videoCodec += "VP8";
                        break;
                    case 3:
                        videoCodec += "H265";
                        break;
                    default:
                        break;
                }
                String text = String.format("Bitrate: %.2fkbps \nFPS: %.2f", quality.videoKBPS, quality.videoDecodeFPS);
                Integer idx = 0;
                Set<Map.Entry<Integer, String>> entries = playViewToStreamIDMap.entrySet();
                for (Map.Entry<Integer, String> entry : entries) {
                    if (entry.getValue().equals(streamID))
                        idx = entry.getKey();
                }

                switch (idx) {
                    case 1:
                        remotePlayViewVideoCodecTextView.setText(videoCodec);
                        remotePlayViewQualityTextView.setText(text);
                        break;
                    case 2:
                        remotePlayView2VideoCodecTextView.setText(videoCodec);
                        remotePlayView2QualityTextView.setText(text);
                        break;
                    case 3:
                        remotePlayView3VideoCodecTextView.setText(videoCodec);
                        remotePlayView3QualityTextView.setText(text);
                        break;
                    default:
                        break;
                }
                AppLogger.getInstance().callApi("Player Quality Update:%s",streamID);
            }

            @Override
            public void onPlayerMediaEvent(String streamID, ZegoPlayerMediaEvent event) {
                super.onPlayerMediaEvent(streamID, event);
            }

            @Override
            public void onPlayerRecvAudioFirstFrame(String streamID) {
                super.onPlayerRecvAudioFirstFrame(streamID);
            }

            @Override
            public void onPlayerRecvVideoFirstFrame(String streamID) {
                super.onPlayerRecvVideoFirstFrame(streamID);
            }

            @Override
            public void onPlayerRenderVideoFirstFrame(String streamID) {
                super.onPlayerRenderVideoFirstFrame(streamID);
            }

            @Override
            public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
                super.onPlayerVideoSizeChanged(streamID, width, height);
                String text = String.format("Resolution: %dx%d", width, height);
                Integer idx = 0;
                Set<Map.Entry<Integer, String>> entries = playViewToStreamIDMap.entrySet();
                for (Map.Entry<Integer, String> entry : entries) {
                    if (entry.getValue().equals(streamID))
                        idx = entry.getKey();
                }
                switch (idx) {
                    case 1:
                        remotePlayViewResolutionTextView.setText(text);
                        break;
                    case 2:
                        remotePlayView2ResolutionTextView.setText(text);
                        break;
                    case 3:
                        remotePlayView3ResolutionTextView.setText(text);
                        break;
                    default:
                        break;
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

    public int checkNumberOfStream() {
        return (int)remoteStreamList.size() + (publisherState == ZegoPublisherState.PUBLISHING ? 1 : 0);
    }

    public void updateVideoConfig() {
        // Start preview
        /// Capture resolution, control the resolution of camera image acquisition. SDK requires the width and height to be set to even numbers. Only the camera is not started and the custom video capture is not used, the setting is effective. For performance reasons, the SDK scales the video frame to the encoding resolution after capturing from camera and before rendering to the preview view. Therefore, the resolution of the preview image is the encoding resolution. If you need the resolution of the preview image to be this value, Please call [setCapturePipelineScaleMode] first to change the capture pipeline scale mode to [Post]
        ZegoVideoConfig zegoVideoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.getZegoVideoConfigPreset(5));
        zegoVideoConfig.setEncodeResolution(publishResolutionWidth, publishResolutionHeight);
        zegoVideoConfig.setVideoFPS(publishFPS);
        zegoVideoConfig.setVideoBitrate(publishBitrate);
        zegoVideoConfig.setCodecID(ZegoVideoCodecID.getZegoVideoCodecID(3));
        engine.setVideoConfig(zegoVideoConfig);
    }

    public void updatePublishStreamBitrate() {
        publishBitrate = getBitrateWithFPS(publishFPS, publishResolutionWidth, publishResolutionHeight, 3);
        publishBitrateEditText.setText(publishBitrate + "");
    }

    public void updateMixStreamBitrate() {
        mixStreamH264Bitrate = getBitrateWithFPS(mixStreamFPS, mixStreamResolutionWidth, mixStreamResolutionHeight, 0);
        mixStreamBitrateH264EditText.setText(mixStreamH264Bitrate + "");
        mixStreamH265Bitrate = getBitrateWithFPS(mixStreamFPS, mixStreamResolutionWidth, mixStreamResolutionHeight, 3);
        mixStreamBitrateH265EditText.setText(mixStreamH265Bitrate + "");
    }

    public int getBitrateWithFPS(int fps, int resolutionWidth, int resolutionHeight, int videoCodecID) {
        return (int) (0.0901 * getCoefficientOfVideoCodec(videoCodecID) * getCoefficientOfFPS(fps) * pow(resolutionWidth * resolutionHeight, 0.7371));
    }

    public double getCoefficientOfFPS(int fps) {
        double coefficient = 1.0;
        switch(fps) {
            case 15:
                coefficient = 1.0;
                break;
            case 30:
                coefficient = 1.5;
                break;
            case 60:
                coefficient = 1.8;
                break;
        }
        return coefficient;
    }

    public double getCoefficientOfVideoCodec(int videoCodecID) {
        double coefficient = 1.0;
        switch (videoCodecID) {
            case 0:
                coefficient = 1.0;
                break;
            case 3:
                coefficient = 0.8;
                break;
            default:
        }
        return coefficient;
    }

    public void updateStartPublishingButtonText(ZegoPublisherState zegoPublisherState) {
        publisherState = zegoPublisherState;
        if(publisherState == ZegoPublisherState.PUBLISHING) {
            startPublishingButton.setText(R.string.stop_publishing);
        } else {
            startPublishingButton.setText(R.string.start_publishing);
        }
    }

    public void updateStartMixStreamButtonText(boolean isMix) {
        isMixing = isMix;
        if(isMixing) {
            startMixStreamButton.setText(R.string.stop_mixing);
        } else {
            startMixStreamButton.setText(R.string.start_mixing);
        }
    }
}
