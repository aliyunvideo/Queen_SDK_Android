package im.zego.advancedaudioprocessing.soundlevelandspectrum.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import im.zego.advancedaudioprocessing.R;
import im.zego.advancedaudioprocessing.soundlevelandspectrum.widget.SoundLevelAndSpectrumItem;
import im.zego.advancedaudioprocessing.soundlevelandspectrum.widget.SpectrumView;
import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class SoundLevelAndSpectrumMainActivity extends AppCompatActivity {

    ZegoExpressEngine engine;

    TextView mTvSoundlevelandspectrumRoomid;
    Switch mSwSoundlevelMonitor;
    Switch mSwSpectrumMonitor;
    // 本地推流的声浪的展现，需要获取该控件来设置进度值
    // To show the sound of local push, you need to get this control to set the progress value
    public ProgressBar mPbCaptureSoundLevel;
    TextView mTvSoundlevelandspectrumUserid ;
    TextView mTvSoundlevelandspectrumStreamid ;
    public SpectrumView mCaptureSpectrumView;
    TextView roomState;
    // 使用线性布局作为容器，以动态添加所拉的流频谱和声浪展现
    //  Use a linear layout as a container to dynamically add the stream spectrum and sound wave presentation
    public LinearLayout ll_container;

    long appID;
    String token;
    ZegoUser user;
    String roomID;
    String userID;
    String streamID;

    // 拉多条流的时候，使用list来保存展现的频谱和声浪的视图
    //  When pulling multiple streams, use list to save the displayed spectrum and sound wave view
    public ArrayList<SoundLevelAndSpectrumItem> frequencySpectrumAndSoundLevelItemList = new ArrayList<>();

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundlevelandspectrum);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoomAndPublishStream();
        setEventHandler();
        setSoundLevelMonitor();
        setSpectrumMonitor();
        setLogComponent();
        setApiCalledResult();
    }
    public void bindView(){
        mTvSoundlevelandspectrumRoomid = findViewById(R.id.tv_soundlevelandspectrum_roomid);
        mSwSoundlevelMonitor = findViewById(R.id.sw_soundlevelandspectrum_soundlevel_monitor);
        mSwSpectrumMonitor = findViewById(R.id.sw_soundlevelandspectrum_spectrum_monitor);
        mPbCaptureSoundLevel = findViewById(R.id.pb_sound_level);
        mTvSoundlevelandspectrumUserid = findViewById(R.id.tv_soundlevelandspectrum_userid);
        mTvSoundlevelandspectrumStreamid = findViewById(R.id.tv_soundlevelandspectrum_streamid);
        mCaptureSpectrumView = findViewById(R.id.soundlevelandspectrum_spectrum_view);
        ll_container = findViewById(R.id.ll_container);
        roomState = findViewById(R.id.roomState);
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
    public void loginRoomAndPublishStream(){
        ZegoRoomConfig config = new ZegoRoomConfig();
        // 使能用户登录/登出房间通知
        // Enable notification when user login or logout
        config.isUserStatusNotify = true;
        config.token = token;
        //login room
        engine.loginRoom(roomID, user, config);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
        engine.startPublishingStream(streamID);
    }
    public void setDefaultValue(){
        //set default publish streamID
        streamID = String.valueOf((int)((Math.random()*9+1)*100000));
        roomID = "0018";

        mTvSoundlevelandspectrumUserid.setText(userID);
        mTvSoundlevelandspectrumStreamid.setText(streamID);
        mTvSoundlevelandspectrumRoomid.setText(roomID);
        setTitle(getString(R.string.soundlevel_and_audiospectrum));
    }
    public void setEventHandler(){
        // 增加本专题所用的回调
        // Add the callback used on this Page
        engine.setEventHandler(new IZegoEventHandler() {
            // 由于本专题中声浪需要做动画效果，这里使用两个实例变量来保存上一次SDK声浪回调中抛出的值，以实现动画过渡的效果
            // Since the sound waves on this page need to be animated, two variables are used here to save the value thrown in the previous SDK sound wave callback to achieve the effect of animation transition

            // 上一次本地采集的进度值
            // The progress value of the last local collection
            private double last_progress_captured = 0.0;
            // 默认情况SDK默认支持最多拉12路流，这里使用一个12长度的int数值来保存所拉的流监控周期
            // The SDK supports up to 12 streams by default. A 12-length int value is used to save the stream monitoring period.
            private HashMap<String, Float> last_stream_to_progress_value = new HashMap();

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList);
                AppLogger.getInstance().receiveCallback("onRoomStreamUpdate: roomID" + roomID + ", updateType:" + updateType.value() + ", streamList: " + streamList);
                // 这里拉流之后动态添加渲染的View
                // Add the rendered view dynamically after playing the stream here
                if(updateType == ZegoUpdateType.ADD){
                    for(ZegoStream zegoStream: streamList){
                        engine.startPlayingStream(zegoStream.streamID, new ZegoCanvas(null));
                        SoundLevelAndSpectrumItem soundLevelAndSpectrumItem = new SoundLevelAndSpectrumItem(SoundLevelAndSpectrumMainActivity.this, null);
                        ll_container.addView(soundLevelAndSpectrumItem);
                        soundLevelAndSpectrumItem.getTvStreamId().setText(zegoStream.streamID);
                        soundLevelAndSpectrumItem.getUserId().setText(zegoStream.user.userID);
                        soundLevelAndSpectrumItem.setStreamid(zegoStream.streamID);
                        last_stream_to_progress_value.put(zegoStream.streamID, 0.0f);
                        frequencySpectrumAndSoundLevelItemList.add(soundLevelAndSpectrumItem);

                    }
                }else if(updateType == ZegoUpdateType.DELETE){
                    for(ZegoStream zegoStream: streamList){
                        engine.stopPlayingStream(zegoStream.streamID);
                        Iterator<SoundLevelAndSpectrumItem> it = frequencySpectrumAndSoundLevelItemList.iterator();
                        while(it.hasNext()){
                            SoundLevelAndSpectrumItem soundLevelAndSpectrumItemTmp = it.next();
                            if(soundLevelAndSpectrumItemTmp.getStreamid().equals(zegoStream.streamID)){
                                it.remove();
                                ll_container.removeView(soundLevelAndSpectrumItemTmp);
                                last_stream_to_progress_value.remove(zegoStream.streamID);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                ValueAnimator animator = ValueAnimator.ofFloat((float) last_progress_captured, (float)soundLevel).setDuration(100);
                animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        mPbCaptureSoundLevel.setProgress((int)((Float)valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                animator.start();
                last_progress_captured = soundLevel;
            }
            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                Iterator<HashMap.Entry<String, Float>> it = soundLevels.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry<String, Float> entry = it.next();
                    String streamid = entry.getKey();
                    Float value = entry.getValue();
                    for(final SoundLevelAndSpectrumItem soundLevelAndSpectrumItem: frequencySpectrumAndSoundLevelItemList){
                        if(streamid.equals(soundLevelAndSpectrumItem.getStreamid())){
                            ValueAnimator animator = ValueAnimator.ofFloat(value.floatValue(), soundLevels.get(streamid).floatValue()).setDuration(100);
                            animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    soundLevelAndSpectrumItem.getPbSoundLevel().setProgress(((Float)(valueAnimator.getAnimatedValue())).intValue());
                                }
                            });
                            animator.start();
                            last_stream_to_progress_value.put(streamid, value);
                        }
                    }
                }
            }
            @Override
            public void onCapturedAudioSpectrumUpdate(float[] frequencySpectrum) {
                super.onCapturedAudioSpectrumUpdate(frequencySpectrum);
                mCaptureSpectrumView.updateFrequencySpectrum(frequencySpectrum);
            }
            @Override
            public void onRemoteAudioSpectrumUpdate(HashMap<String, float[]> frequencySpectrums) {
                super.onRemoteAudioSpectrumUpdate(frequencySpectrums);
                Iterator<HashMap.Entry<String, float[]>> it = frequencySpectrums.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry<String, float[]> entry = it.next();
                    String streamid = entry.getKey();
                    float[] values = entry.getValue();

                    for(SoundLevelAndSpectrumItem soundLevelAndSpectrumItem: frequencySpectrumAndSoundLevelItemList){
                        if(streamid.equals(soundLevelAndSpectrumItem.getStreamid())){
                            soundLevelAndSpectrumItem.getSpectrumView().updateFrequencySpectrum(values);
                        }
                    }
                }
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                /** 房间状态回调，在登录房间后，当房间状态发生变化（例如房间断开，认证失败等），SDK会通过该接口通知 */
                /** Room status update callback: after logging into the room, when the room connection status changes
                 * (such as room disconnection, login authentication failure, etc.), the SDK will notify through the callback
                 */
                AppLogger.getInstance().receiveCallback("onRoomStateUpdate: roomID = " + roomID + ", state = " + state + ", errorCode = " + errorCode);
                if (errorCode != 0) {
                    Toast.makeText(SoundLevelAndSpectrumMainActivity.this, String.format("登陆房间失败, 错误码: %d", errorCode), Toast.LENGTH_LONG).show();
                }
                // Update room state
                if (state.equals(ZegoRoomState.CONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                } else if (state.equals(ZegoRoomState.DISCONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                AppLogger.getInstance().receiveCallback("onPublisherStateUpdate: errorcode:"+ errorCode + ", streamID:" + streamID + ", state:" + state.value());

            }
        });

    }
    public void setSoundLevelMonitor(){
        mSwSoundlevelMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    engine.startSoundLevelMonitor();
                }else {
                    engine.stopSoundLevelMonitor();
                }
            }
        });
    }
    public void setSpectrumMonitor(){
        mSwSpectrumMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    engine.startAudioSpectrumMonitor();
                }else {
                    engine.stopAudioSpectrumMonitor();
                }
            }
        });
    }
    @Override
    protected void onDestroy() {

        engine.stopAudioSpectrumMonitor();
        engine.stopSoundLevelMonitor();


        engine.stopPublishingStream();
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);

        super.onDestroy();
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SoundLevelAndSpectrumMainActivity.class);
        activity.startActivity(intent);
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
}
