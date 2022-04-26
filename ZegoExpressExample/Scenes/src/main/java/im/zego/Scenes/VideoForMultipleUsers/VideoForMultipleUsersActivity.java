package im.zego.Scenes.VideoForMultipleUsers;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import im.zego.R;
import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoBufferType;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class VideoForMultipleUsersActivity extends AppCompatActivity {

    String userID;
    String roomID;
    String userName;
    String streamID;
    ZegoUser user;

    //Whether the user is publishing the stream.
    Boolean[] isPublish = {false};

    ZegoExpressEngine engine;
    //The number of users in the room.
    int userCount = 0;
    //The number of streams in the room.
    int streamCount = 0;

    VideoViewAdapter videoViewAdapter;
    GridLayoutManager layoutManager;
    RecyclerView playView;
    TextView roomIDText;
    TextView streamIDText;
    TextView userIDText;
    TextView userNameText;
    Button streamListButton;
    Button userListButton;
    TextView roomState;

    // Unicode of Emoji
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_call);
        requestPermission();
        setLogComponent();
        initUI();
        getUserSettings();
        initEngineAndUser();
        setEventHandler();
        bindView();
        initTextView();
        setUserListButtonClickEvent();
        setStreamListButtonClickEvent();
        setApiCalledResult();
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
    public void getUserSettings(){
        streamID = String.valueOf((int)((Math.random()*9+1)*100000));
        videoViewAdapter.myStreamID = streamID;
        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        roomID = getIntent().getStringExtra("roomID");
    }
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        engine = ZegoExpressEngine.getEngine();
        //create the user
        user = new ZegoUser(userID, userName);
        //add the user into user list
        videoViewAdapter.userList.add(user);
        //update the number of user
        userCount += 1;

        videoViewAdapter.notifyDataSetChanged();
    }
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            @Override
            public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                super.onPublisherQualityUpdate(streamID, quality);
                //After calling the [startPlayingStream] successfully, this callback will be triggered every 3 seconds.
                //The collection frame rate, bit rate, RTT, packet loss rate and other quality data can be obtained,
                //such the health of the publish stream can be monitored in real time.

                //update publish quality
                videoViewAdapter.publishQuality = quality;
                //notify the adapter to update the view.
                videoViewAdapter.notifyItemChanged(0);
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                // The callback triggered when the number of other users in the room increases or decreases.

                //if the number of users increases
                if (updateType.equals(ZegoUpdateType.ADD)) {
                    for (ZegoUser user : userList) {
                        //add user in to the user list
                        videoViewAdapter.userList.add(user);
                        //insert the user into the view.
                        videoViewAdapter.notifyItemInserted(videoViewAdapter.userList.size());
                    }
                    //update the number of users in the room.
                    userCount += userList.size();
                } else {
                    //if the number of users decreases
                    for (ZegoUser User : userList) {
                        //get the index of the users who log out in user list
                        for (int i = 0; i < videoViewAdapter.userList.size(); i++) {
                            if (videoViewAdapter.userList.get(i).userID.equals(User.userID)) {
                                //remove the user from user list
                                videoViewAdapter.userList.remove(i);
                                //remove the user from view.
                                videoViewAdapter.notifyItemRemoved(i);
                            }
                        }
                    }
                    //update the number of users in the room.
                    userCount -= userList.size();
                }
                //update the text of the button.
                userListButton.setText("UserList("+userCount+")");
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                // The callback triggered when the number of streams published by the other users in the same room increases or decreases.

                //if the number of streams increases
                if (updateType.equals(ZegoUpdateType.ADD)) {
                    for (ZegoStream stream : streamList) {
                        //add the stream to the stream list
                        videoViewAdapter.streams.add(stream);
                        //notify the adapter to update the view
                        int index = getStreamIndex(stream.streamID, videoViewAdapter.streams);
                        if (index!=-1) {
                            videoViewAdapter.notifyItemChanged(getStreamIndex(stream.streamID, videoViewAdapter.streams));
                        } else {
                            videoViewAdapter.notifyDataSetChanged();
                        }
                    }
                    //update the number of streams
                    streamCount += streamList.size();
                } else {
                    //if the number of streams decreases
                    for (ZegoStream stream : streamList) {
                        for (int i = 0; i < videoViewAdapter.streams.size(); i++) {
                                videoViewAdapter.isPlay.remove(stream.streamID);
                            //get the index of streams which quit the room.
                            if (videoViewAdapter.streams.get(i).streamID.equals(stream.streamID)) {
                                //notify the adapter to update the view.
                                videoViewAdapter.notifyItemChanged(getStreamIndex(stream.streamID, videoViewAdapter.streams));
                                //remove the stream from stream list
                                videoViewAdapter.streams.remove(i);
                            }
                        }
                    }
                    //update the number of streams.
                    streamCount -= streamList.size();
                }
                //update the text of the button.
                streamListButton.setText("StreamList("+ streamCount +")");
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                //The callback triggered when the state of stream publishing changes.

                //if the user is publishing the stream
                if (state.equals(ZegoPublisherState.PUBLISHING)) {
                    //update the number of stream
                    if (!isPublish[0]) {
                        //update the number of stream
                        streamCount += 1;
                    }
                    //update publish status
                    isPublish[0] = true;
                    //update the text of button.
                    streamListButton.setText("StreamList(" + streamCount + ")");
                } else if (state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish[0]) {
                        //update the number of stream
                        streamCount -= 1;
                    }
                    //update publish status
                    isPublish[0] = false;
                    //update the text of button.
                    streamListButton.setText("StreamList(" + streamCount + ")");
                }
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if (errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish[0]) {
                        // The user fails to publish the stream.
                        videoViewAdapter.setPublisherState(1);
                        videoViewAdapter.notifyItemChanged(0);
                    }
                } else {
                    if (isPublish[0]) {
                        // The user is publishing the stream successfully.
                        videoViewAdapter.setPublisherState(2);
                        videoViewAdapter.notifyItemChanged(0);
                    }
                }
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                videoViewAdapter.setPlayerState(streamID,state,errorCode);
                videoViewAdapter.notifyItemChanged(getStreamIndex(streamID,videoViewAdapter.streams));
            }

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
                // Callback for current stream playing quality.
                // After calling the [startPlayingStream] successfully, this callback will be triggered every 3 seconds.

                //if the stream has been in the streamQuality hash map, update the hash map. Otherwise, add the stream with its quality into hash map.
                if (videoViewAdapter.streamQuality.containsKey(streamID)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        videoViewAdapter.streamQuality.replace(streamID,quality);
                    }
                } else {
                    videoViewAdapter.streamQuality.put(streamID,quality);
                }
                //update the view
                videoViewAdapter.notifyItemChanged(getStreamIndex(streamID, videoViewAdapter.streams));
            }

            @Override
            public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
                super.onPlayerVideoSizeChanged(streamID, width, height);
                // The callback triggered when the stream playback resolution changes.

                int[] temp = {width,height};
                //if the stream has been in the videoSize hash map, update the hash map. Otherwise, add the stream with its size into hash map.
                if (videoViewAdapter.videoSize.containsKey(streamID)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        videoViewAdapter.videoSize.replace(streamID,temp);
                    }
                } else {
                    videoViewAdapter.videoSize.put(streamID,temp);
                }
                //update the view
                videoViewAdapter.notifyItemChanged(getStreamIndex(streamID, videoViewAdapter.streams));
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
        });
    }
    public void initUI() {
        bindView();
        initTextView();
        setLayout();
    }
    public void bindView(){
        playView = findViewById(R.id.allVideo);
        roomIDText = findViewById(R.id.roomID);
        streamIDText = findViewById(R.id.streamID);
        userNameText = findViewById(R.id.userName);
        userIDText = findViewById(R.id.userID);
        streamListButton = findViewById(R.id.streamListButton);
        userListButton = findViewById(R.id.userListButton);
        roomState = findViewById(R.id.roomState);
    }
    public void initTextView(){
        roomIDText.setText(roomID);
        userNameText.setText(userName);
        streamIDText.setText(streamID);
        userIDText.setText(userID);
        streamListButton.setText("StreamList(0)");
        userListButton.setText("Userlist(" + userCount + ")");
        setTitle(getString(R.string.video_for_multiple_users));
    }
    public void setLayout(){
        layoutManager = new GridLayoutManager(this, 2);
        videoViewAdapter = new VideoViewAdapter(getApplicationContext());
        //Set the adapter and layout manager of view
        playView.setLayoutManager(layoutManager);
        playView.setAdapter(videoViewAdapter);
        //disable the item animator to avoid blinking
        playView.setItemAnimator(null);
    }
    public void setUserListButtonClickEvent() {

        userListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.Builder Builder = new ListDialog.Builder(VideoForMultipleUsersActivity.this);
                Builder.setTitle("UserList");
                Builder.setUserListString(videoViewAdapter.userList);
                ListDialog dialog = Builder.create();
                Builder.refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //update user list
                        Builder.setUserListString(videoViewAdapter.userList);
                        //update the view.
                        Builder.refresh();
                    }
                });
                dialog.show();
            }
        });
    }

    public void setStreamListButtonClickEvent() {
        streamListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog.Builder Builder = new ListDialog.Builder(VideoForMultipleUsersActivity.this);
                Builder.setTitle("StreamList");
                Builder.setMyStream(user, streamID);
                Builder.setStreamListString(videoViewAdapter.streams,isPublish[0]);
                ListDialog dialog = Builder.create();
                Builder.refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //update the stream list and publish status
                        Builder.setStreamListString(videoViewAdapter.streams,isPublish[0]);
                        //update the view
                        Builder.refresh();
                    }
                });
                dialog.show();
            }
        });
    }

    //return the index of a specific stream in the stream list 获得指定stream在流list中的index
    public int getStreamIndex (String StreamID, ArrayList<ZegoStream> StreamList){
        for (ZegoStream stream:StreamList)
        {
            if (StreamID.equals(stream.streamID)){
                for (int i = 0; i< videoViewAdapter.userList.size(); i++){
                    if (videoViewAdapter.userList.get(i).userID.equals(stream.user.userID)){
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    public String getStreamID(String userID,ArrayList<ZegoStream> streamList){
        for (ZegoStream stream:streamList){
            if (stream.user.userID.equals(userID)){
                return stream.streamID;
            }
        }
        return "";
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
    @Override
    protected void onDestroy() {
        //Log out the room and destroy the engine
        ZegoExpressEngine.getEngine().logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}