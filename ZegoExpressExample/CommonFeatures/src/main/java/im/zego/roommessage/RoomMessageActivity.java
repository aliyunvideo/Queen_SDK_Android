package im.zego.roommessage;

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
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.commonvideoconfig.R;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendBroadcastMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoUser;

public class RoomMessageActivity extends AppCompatActivity {

    EditText editPublishStreamID;
    EditText editPlayStreamID;
    Button startPublishingButton;
    Button startPlayingButton;
    TextView textRoomID;
    TextView textUserID;
    TextView broadcastMessage;
    EditText editBroadcastMessage;
    Button sendBroadcastButton;
    TextView barrageMessage;
    EditText editBarrageMessage;
    Button sendBarrageButton;
    TextView customCommandMessage;
    EditText editCustomCommand;
    EditText editCustomCommandUserID;
    Button sendCustomCommandButton;
    TextView roomExtraInfo;
    EditText editRoomExtraInfo;
    Button setRoomExtraInfoButton;
    TextureView preview;
    TextureView playView;
    TextView roomState;

    Long appID;
    String roomID;
    String userID;
    String token;
    String publishStreamID;
    String playStreamID;
    boolean isPublish = false;
    boolean isPlay = false;
    ArrayList<ZegoUser> roomUserList  = new ArrayList<>();

    ZegoExpressEngine engine;
    ZegoUser user;
    //Show info
    TextView roomExtraInfoTitle;
    TextView broadcastMessageReceiveTitle;
    TextView barrageMessageReceiveTitle;
    TextView customCommandReceiveTitle;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_message);
        bindView();
        requestPermission();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        setDefaultConfig();
        setEventHandler();
        loginRoom();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();
        setSendBroadcastMessageButtonEvent();
        setSendBarrageButtonEvent();
        setRoomExtraInfoButtonEvent();
        setSendCustomCommandMessageEvent();
        setLogComponent();
        setApiCalledResult();
        showInfo();
    }
    public void bindView(){
        editPublishStreamID = findViewById(R.id.editPublishStreamID);
        editPlayStreamID = findViewById(R.id.editPlayStreamID);
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        textRoomID = findViewById(R.id.editRoomID);
        textUserID = findViewById(R.id.userIDText);
        broadcastMessage = findViewById(R.id.broadcastMessageReceived);
        editBroadcastMessage = findViewById(R.id.editBroadcast);
        sendBroadcastButton = findViewById(R.id.broadcastSendButton);
        barrageMessage = findViewById(R.id.barrageMessageReceived);
        editBroadcastMessage = findViewById(R.id.editBroadcast);
        editBroadcastMessage = findViewById(R.id.editBroadcast);
        editBarrageMessage = findViewById(R.id.editBarrage);
        sendBarrageButton = findViewById(R.id.barrageSendButton);
        customCommandMessage = findViewById(R.id.customCommandReceived);
        editCustomCommand = findViewById(R.id.editCommandMessage);
        editCustomCommandUserID = findViewById(R.id.editCommandUserID);
        sendCustomCommandButton = findViewById(R.id.commandSendButton);
        roomExtraInfo = findViewById(R.id.roomExtraInfo);
        editRoomExtraInfo = findViewById(R.id.editRoomInfo);
        setRoomExtraInfoButton = findViewById(R.id.roomExtraInfoSetButton);
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        roomState = findViewById(R.id.roomState);
        roomExtraInfoTitle = findViewById(R.id.roomExtraInfoTitle);
        broadcastMessageReceiveTitle = findViewById(R.id.broadcastMessageReceiveTitle);
        barrageMessageReceiveTitle = findViewById(R.id.barrageMessageReceiveTitle);
        customCommandReceiveTitle = findViewById(R.id.customCommandReceiveTitle);
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
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.isUserStatusNotify = true;
        roomConfig.token = token;

        //login room
        engine.loginRoom(roomID, user,roomConfig);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false); }
    public void setDefaultConfig(){
        //set default play streamID
        playStreamID = "0007";
        //set default publish StreamID
        publishStreamID = "0007";
        //set default room ID
        roomID = "0007";
        textUserID.setText(userID);
        editCustomCommandUserID.setText(userID);
        setTitle(getString(R.string.room_message));
    }
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPublish) {
                    engine.startPreview(new ZegoCanvas(preview));
                    publishStreamID = editPublishStreamID.getText().toString();
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
                    playStreamID = editPlayStreamID.getText().toString();
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
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            // The callback triggered when Broadcast Messages are received.
            // This callback is used to receive broadcast messages sent by other users,
            // and barrage messages sent by users themselves will not be notified through this callback.
            @Override
            public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
                super.onIMRecvBroadcastMessage(roomID, messageList);
                String message = messageList.get(messageList.size()-1).message;
                String user = messageList.get(messageList.size()-1).fromUser.userID;
                broadcastMessage.setText(user+":"+message);
                AppLogger.getInstance().callApi("Received BroadcastMessage:%s",message);
            }

            //The callback triggered when Barrage Messages are received.
            //This callback is used to receive barrage messages sent by other users,
            // and barrage messages sent by users themselves will not be notified through this callback.
            @Override
            public void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList) {
                super.onIMRecvBarrageMessage(roomID, messageList);
                String message = messageList.get(messageList.size()-1).message;
                String user = messageList.get(messageList.size()-1).fromUser.userID;
                barrageMessage.setText(user+":"+message);
                AppLogger.getInstance().callApi("Received BarrageMessage:%s",message);
            }

            // The callback triggered when a Custom Command is received.
            // This callback is used to receive custom signaling sent by other users,
            // and barrage messages sent by users themselves will not be notified through this callback.
            @Override
            public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
                super.onIMRecvCustomCommand(roomID, fromUser, command);
                customCommandMessage.setText(fromUser.userName+":"+command);
                AppLogger.getInstance().callApi("Received CustomCommand: %s UserID:%s",command,fromUser.userID);
            }

            // The callback triggered when there is an update on the extra information of the room.
            // When a user update the room extra information, other users in the same room will receive the callback
            @Override
            public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                super.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
                String message = roomExtraInfoList.get(roomExtraInfoList.size()-1).value;
                roomExtraInfo.setText(message);
                AppLogger.getInstance().callApi("RoomExtraInfoUpdated:%s",message);
            }
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
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                if (updateType.equals(ZegoUpdateType.ADD)){
                    roomUserList.addAll(userList);
                    AppLogger.getInstance().receiveCallback("New user(s) login room ");
                } else {
                    roomUserList.removeAll(userList);
                    AppLogger.getInstance().receiveCallback("User(s) logout room ");
                }
            }
        });
    }
    // Send Broadcast Message
    public void setSendBroadcastMessageButtonEvent(){
        sendBroadcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send a Broadcast Message.
                engine.sendBroadcastMessage(roomID, editBroadcastMessage.getText().toString(), new IZegoIMSendBroadcastMessageCallback() {
                    @Override
                    public void onIMSendBroadcastMessageResult(int i, long l) {
                        if (i == 0){
                            Toast.makeText(RoomMessageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            // Show message.
                            broadcastMessage.setText(userID+":"+editBroadcastMessage.getText().toString());
                            sendBroadcastButton.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.send_broadcastmessage));
                        } else {
                            sendBroadcastButton.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.send_broadcastmessage));
                        }
                    }
                });
                AppLogger.getInstance().callApi("Send BroadcastMessage:%s",editBroadcastMessage.getText().toString());
            }
        });
    }
    // Send Barrage Message
    public void setSendBarrageButtonEvent(){
        sendBarrageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send a Barrage Message (bullet screen) to all users in the same room, without guaranteeing the delivery.
                engine.sendBarrageMessage(roomID, editBarrageMessage.getText().toString(), new IZegoIMSendBarrageMessageCallback() {
                    @Override
                    public void onIMSendBarrageMessageResult(int i, String s) {
                        if (i==0){
                            Toast.makeText(RoomMessageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            // Show message.
                            barrageMessage.setText(userID+":"+editBarrageMessage.getText().toString());
                            sendBarrageButton.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.send_barragemessage));
                        } else {
                            sendBarrageButton.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.send_barragemessage));
                        }
                    }
                });
                AppLogger.getInstance().callApi("Send BarrageMessage:%s",editBarrageMessage.getText().toString());
            }
        });
    }
    // Send custom command message
    public void setSendCustomCommandMessageEvent(){
        sendCustomCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editCustomCommandUserID.getText().toString().equals("")) {
                    ArrayList<ZegoUser> userList = new ArrayList<>();
                    userList.add(new ZegoUser(editCustomCommandUserID.getText().toString()));
                    //Send a Custom Command to the specified users in the same room.
                    engine.sendCustomCommand(roomID, editCustomCommand.getText().toString(), userList, new IZegoIMSendCustomCommandCallback() {
                        @Override
                        public void onIMSendCustomCommandResult(int i) {
                            if (i == 0) {
                                Toast.makeText(RoomMessageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                // Show message
                                customCommandMessage.setText(userID + ":" + editCustomCommand.getText().toString());
                                sendCustomCommandButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.send_customcommand));
                            } else {
                                sendCustomCommandButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.send_customcommand));
                            }
                        }
                    });
                    AppLogger.getInstance().callApi("Send editCustomCommand:%s", editCustomCommand.getText().toString());
                } else {
                    engine.sendCustomCommand(roomID, editCustomCommand.getText().toString(), roomUserList, new IZegoIMSendCustomCommandCallback() {
                        @Override
                        public void onIMSendCustomCommandResult(int i) {
                            if (i == 0) {
                                Toast.makeText(RoomMessageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                                // Show message
                                customCommandMessage.setText(userID + ":" + editCustomCommand.getText().toString());
                                sendCustomCommandButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.send_customcommand));
                            } else {
                                sendCustomCommandButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.send_customcommand));
                            }
                        }
                    });
                    AppLogger.getInstance().callApi("Send editCustomCommand:%s to all users in the room", editCustomCommand.getText().toString());
                }
            }
        });
    }
    // Set room extra information
    public void setRoomExtraInfoButtonEvent(){
        setRoomExtraInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.setRoomExtraInfo(roomID, "key", editRoomExtraInfo.getText().toString(), new IZegoRoomSetRoomExtraInfoCallback() {
                    @Override
                    public void onRoomSetRoomExtraInfoResult(int i) {
                        if (i==0) {
                            // Show information
                            Toast.makeText(RoomMessageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            roomExtraInfo.setText(userID+":"+editRoomExtraInfo.getText().toString());
                            setRoomExtraInfoButton.setText(getEmojiStringByUnicode(checkEmoji)+getString(R.string.set_roomextrainfo));
                        } else {
                            setRoomExtraInfoButton.setText(getEmojiStringByUnicode(crossEmoji)+getString(R.string.set_roomextrainfo));
                        }
                    }
                });
                AppLogger.getInstance().callApi("setRoomExtraInfo:%s",editRoomExtraInfo.getText().toString());
            }
        });
    }

    //show info to tell user what meaning of the words
    public void showInfo(){
        View.OnClickListener listener  = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.roomExtraInfoTitle){
                    Toast.makeText(getApplicationContext(),R.string.RoomExtraInfoNotice,Toast.LENGTH_LONG).show();
                    return;
                }
                if(view.getId() == R.id.broadcastMessageReceiveTitle){
                    Toast.makeText(getApplicationContext(),R.string.BroadcastMessageNotice,Toast.LENGTH_LONG).show();
                    return;
                }
                if(view.getId() == R.id.barrageMessageReceiveTitle){
                    Toast.makeText(getApplicationContext(),R.string.BarrageMessageNotice,Toast.LENGTH_LONG).show();
                    return;
                }
                if(view.getId() == R.id.customCommandReceiveTitle){
                    Toast.makeText(getApplicationContext(),R.string.CustomCommandNotice,Toast.LENGTH_LONG).show();
                    return;
                }
            }
        };
        roomExtraInfoTitle.setOnClickListener(listener);
        broadcastMessageReceiveTitle.setOnClickListener(listener);
        barrageMessageReceiveTitle.setOnClickListener(listener);
        customCommandReceiveTitle.setOnClickListener(listener);
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, RoomMessageActivity.class);
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