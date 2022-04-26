package com.example.others.streammixing;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.others.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import im.zego.commontools.logtools.AppLogger;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import java.nio.ByteBuffer;

public class MixerMainActivity extends AppCompatActivity {
    public static String roomID = "0025";
    public static String userID;
    public static ZegoExpressEngine engine;
    public static ArrayList<ZegoStream> streamInfoList = new ArrayList<>();
    private static IMixerStreamUpdateHandler notifyHandler = null;
    private static boolean loginFlag = false;
    long appID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixer_main);
        setDefaultValue();
        initEngine();
        setEventHandler();
        setApiCalledResult();
    }
    public void setDefaultValue(){
        userID = KeyCenter.getInstance().getUserID();
        TextView tv_room = findViewById(R.id.tv_room_id);
        tv_room.setText(roomID);
        appID = KeyCenter.getInstance().getAppID();
        setTitle(getString(R.string.stream_mixing));
    }
    public void initEngine(){
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
        streamInfoList.clear();
        loginFlag = false;
    }
    public void loginRoom(){
        ZegoUser user = new ZegoUser(userID);
        if(!loginFlag) {
            ZegoRoomConfig config = new ZegoRoomConfig();
            config.token = KeyCenter.getInstance().getToken();
            engine.loginRoom(roomID, user, config);
            AppLogger.getInstance().callApi("Login Room:%s",roomID);
        }

    }
    public void setEventHandler(){
        if (engine != null) {
            IZegoEventHandler handler = new IZegoEventHandler() {

                @Override
                public void onMixerSoundLevelUpdate(HashMap<Integer, Float> soundLevels) {
                    Log.e("onMixerSoundLevelUpdate", "test");
                }

                @Override
                public void onAutoMixerSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                    Log.e("onMixerSoundLevelUpdate", "test");
                    Iterator iter = soundLevels.entrySet().iterator();
                    while (iter.hasNext()) {
                        HashMap.Entry entry = (HashMap.Entry) iter.next();
                        Object key = entry.getKey();
                        Object val = entry.getValue();
                        AppLogger.getInstance().i("streamID:" + key + "soundLevel:" + val);

                        for (int i = 0;i < streamInfoList.size() ;i++ ) {
                            if(streamInfoList.get(i).streamID.equals(key))
                            {
                                streamInfoList.get(i).extraInfo = String.format("%f",val);
                                if(notifyHandler != null) {
                                    notifyHandler.onAutoSoundLevelUpdate();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                    ZegoStream streamInfo;
                    for (int i = 0; i < streamList.size(); i++) {
                        boolean isFind = false;
                        for(int j = 0;j<streamInfoList.size();j++)
                        {
                            String streamID = streamInfoList.get(j).streamID;
                            if(streamID.equals(streamList.get(i).streamID))
                            {
                                isFind = true;
                                if (updateType == ZegoUpdateType.DELETE) {
                                    streamInfoList.remove(j--);
                                }
                            }
                        }
                        if(isFind == false)
                        {
                            if (updateType == ZegoUpdateType.ADD) {
                                streamInfoList.add(streamList.get(i));
                            }
                        }

                        AppLogger.getInstance().i("onRoomStreamUpdate: roomID = " + roomID + ", updateType =" + updateType + ", streamID = " + streamList.get(i).streamID);
                    }
                    if (notifyHandler != null) {
                        notifyHandler.onRoomStreamUpdate();
                    }
                }



                @Override
                public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                    AppLogger.getInstance().receiveCallback("onPublisherStateUpdate：state =" + state + ", streamID = " + streamID + ", errorCode = " + errorCode);
                    if (state == ZegoPublisherState.PUBLISHING) {
                        Toast.makeText(MixerMainActivity.this, getString(R.string.tx_mixer_publish_ok), Toast.LENGTH_SHORT).show();
                    }
                    else if (state == ZegoPublisherState.PUBLISH_REQUESTING) {
                        Toast.makeText(MixerMainActivity.this, getString(R.string.tx_mixer_publish_request), Toast.LENGTH_SHORT).show();
                    }
                    else if (state == ZegoPublisherState.NO_PUBLISH) {
                        if (errorCode == 0) {
                            Toast.makeText(MixerMainActivity.this, getString(R.string.tx_mixer_stop_publish_ok), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MixerMainActivity.this, getString(R.string.tx_mixer_publish_fail) + errorCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                    /** 房间状态回调，在登录房间后，当房间状态发生变化（例如房间断开，认证失败等），SDK会通过该接口通知 */
                    /** Room status update callback: after logging into the room, when the room connection status changes
                     * (such as room disconnection, login authentication failure, etc.), the SDK will notify through the callback
                     */
                    AppLogger.getInstance().i("onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
                    if (errorCode != 0) {
                        Toast.makeText(MixerMainActivity.this, String.format("login room fail, errorCode: %d", errorCode), Toast.LENGTH_LONG).show();
                    }
                    if(state== ZegoRoomState.CONNECTED){
                        loginFlag = true;
                    }else{
                        loginFlag = false;
//                        streamInfoList.clear();
                    }
                }
            };
            engine.setEventHandler(handler);
        }
    }
    public void ClickPublishActivity(View view) {
        loginRoom();
        MixerPublishActivity.actionStart(this);
    }

    public void ClickMixActivity(View view) {
        loginRoom();
        MixerStartActivity.actionStart(this);
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MixerMainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static void registerStreamUpdateNotify(IMixerStreamUpdateHandler handler) {
        notifyHandler = handler;
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

    public void ClickAutoMixActivity(View view) {
        loginRoom();
        AutoMixerStartActivity.actionStart(this);
    }
}
