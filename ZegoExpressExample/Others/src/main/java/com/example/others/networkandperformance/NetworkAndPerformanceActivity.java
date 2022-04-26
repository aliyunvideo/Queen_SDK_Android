package com.example.others.networkandperformance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.others.R;
import com.example.others.flowcontrol.FlowControlActivity;

import org.json.JSONObject;
import org.w3c.dom.Text;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoNetworkSpeedTestType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestConfig;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class NetworkAndPerformanceActivity extends AppCompatActivity {

    TextView userIDText;
    TextView downConnectCostText;
    TextView downRttText;
    TextView downPacketLostRateText;
    TextView upConnectCostText;
    TextView upRttText;
    TextView upPacketLostRateText;
    EditText expectedDownlinkBitrateEdit;
    EditText expectedUplinkBitrateEdit;
    Button networkSeedTestButton;
    TextView appCpuText;
    TextView appMemoryText;
    TextView appMemoryPercentageText;
    TextView systemMemoryPercentageText;
    TextView roomState;

    String userID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;
    ZegoNetworkSpeedTestConfig config;

    //Store whether the user is testing the network speed
    Boolean isTest = false;

    Handler handler;
    Runnable runnable;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_and_performance);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        setLogComponent();
        initEngineAndUser();
        setEventHandler();
        loginRoom();
        setApiCalledResult();
        setNetworkSeedTestButton();
        updatePerformance();
    }
    public void bindView(){
        userIDText = findViewById(R.id.userIDText);
        downConnectCostText = findViewById(R.id.downConnectCost);
        downRttText = findViewById(R.id.downRtt);
        downPacketLostRateText = findViewById(R.id.downLostRate);
        upConnectCostText = findViewById(R.id.upConnectCost);
        upRttText = findViewById(R.id.upRtt);
        upPacketLostRateText = findViewById(R.id.upLostRate);
        expectedDownlinkBitrateEdit = findViewById(R.id.expectedDownlink);
        expectedUplinkBitrateEdit = findViewById(R.id.expectedUplink);
        networkSeedTestButton = findViewById(R.id.networkSpeedTestButton);
        appCpuText = findViewById(R.id.appCpu);
        appMemoryPercentageText = findViewById(R.id.appMemoryPercentage);
        appMemoryText = findViewById(R.id.appMemory);
        systemMemoryPercentageText = findViewById(R.id.systemMemoryPercentage);
        roomState = findViewById(R.id.roomState);
    }
    public void setDefaultValue(){
        roomID = "0031";
        userIDText.setText(userID);
        setTitle(getString(R.string.network_and_performance));

        config =new ZegoNetworkSpeedTestConfig();
        config.testUplink = true;
        config.testDownlink = true;
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

            @Override
            public void onNetworkSpeedTestError(int errorCode, ZegoNetworkSpeedTestType type) {
                super.onNetworkSpeedTestError(errorCode, type);
                String error = "";
                if (type.equals(ZegoNetworkSpeedTestType.DOWNLINK)){
                    error = "Download Test Error";
                } else if (type.equals(ZegoNetworkSpeedTestType.UPLINK)){
                    error = "Upload Test Error";
                }
                AppLogger.getInstance().fail("[%d]%s",errorCode,error);
            }

            @Override
            public void onNetworkSpeedTestQualityUpdate(ZegoNetworkSpeedTestQuality quality, ZegoNetworkSpeedTestType type) {
                super.onNetworkSpeedTestQualityUpdate(quality, type);
                if (type.equals(ZegoNetworkSpeedTestType.DOWNLINK)){
                    downConnectCostText.setText(quality.connectCost + "ms");
                    downPacketLostRateText.setText(String.format("%.2f", quality.packetLostRate)+ "%");
                    downRttText.setText(quality.rtt + "ms");
                } else if (type.equals(ZegoNetworkSpeedTestType.UPLINK)) {
                    upConnectCostText.setText(quality.connectCost + "ms");
                    upPacketLostRateText.setText(String.format("%.2f", quality.packetLostRate)+ "%");
                    upRttText.setText(quality.rtt + "ms");
                }
            }
        });
    }
    public void setNetworkSeedTestButton(){
        networkSeedTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTest) {
                    if (expectedDownlinkBitrateEdit.getText().toString().equals("")) {
                        Toast.makeText(NetworkAndPerformanceActivity.this, "Expected Downlink Bitrate cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (expectedUplinkBitrateEdit.getText().toString().equals("")) {
                        Toast.makeText(NetworkAndPerformanceActivity.this, "Expected Uplink Bitrate cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    config.expectedDownlinkBitrate = Integer.valueOf(expectedDownlinkBitrateEdit.getText().toString());
                    config.expectedUplinkBitrate = Integer.valueOf(expectedUplinkBitrateEdit.getText().toString());
                    engine.startNetworkSpeedTest(config);
                    AppLogger.getInstance().callApi("Start network speed test");
                    networkSeedTestButton.setText(getString(R.string.stop_network_speed_test));
                    isTest = true;
                } else {
                    engine.stopNetworkSpeedTest();
                    networkSeedTestButton.setText(getString(R.string.start_network_speed_test));
                    AppLogger.getInstance().callApi("Stop network speed test");
                    isTest = false;
                }
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, NetworkAndPerformanceActivity.class);
        activity.startActivity(intent);
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
    public void updatePerformance(){
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                appCpuText.setText(DeviceInfoManager.getCurProcessCpuRate()+ "%");
                appMemoryText.setText(DeviceInfoManager.getUsedValue(getApplicationContext()) + "kb");
                appMemoryPercentageText.setText(DeviceInfoManager.getUsedPercentValue(getApplicationContext()));
                systemMemoryPercentageText.setText(DeviceInfoManager.getSystemMemoryRate(getApplicationContext()));
                handler.postDelayed(this, 2000);
            }
        };
        runnable.run();
    }
    @Override
    protected void onDestroy() {
        ZegoExpressEngine.destroyEngine(null);
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}