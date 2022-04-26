package im.zego.Scenes.VideoForMultipleUsers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

import im.zego.R;
import im.zego.commontools.logtools.AppLogger;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class VideoViewAdapter extends RecyclerView.Adapter {

    ZegoExpressEngine engine;
    //To store all users in the room.
    ArrayList<ZegoUser> userList = new ArrayList<>();
    //To store all streams in the room.
    ArrayList<ZegoStream> streams = new ArrayList<>();
    //To store the quality attributions of all streams in the room.
    HashMap<String,ZegoPlayStreamQuality> streamQuality = new HashMap<>();
    //To store the size of all streams in the room.
    HashMap<String,int[]> videoSize = new HashMap<>();
    Context context;
    String myStreamID = "";
    //The quality attributions of the publishing stream.
    ZegoPublishStreamQuality publishQuality = new ZegoPublishStreamQuality();
    //Whether the user is publishing the stream.
    Boolean isPreview = false;
    HashMap<String,Boolean> isPlay = new HashMap<>();
    boolean isPlubish = false;
    // If publisherState == 0, it means the state is cleared. The user has not started publishing.
    // If publisherState == 1, it means the user publishes stream successfully.
    // If publisherState == 2, it means the user fails to publish the stream.
    int publisherState = 0;
    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    String playerStateStreamID;
    // If playerState == 0, it means the state is cleared. The user has not started playing.
    // If playerState == 1, it means the user plays stream successfully.
    // If playerState == 2, it means the user fails to play the stream.
    HashMap<String, Integer> playerState = new HashMap<>();


    public VideoViewAdapter (Context context){
        /*
        this.userList = UserList;
        this.streams = StreamList;
        this.streamQuality = StreamQuality;
        this.videoSize = VideoSize;
        this.publishQuality = myQuality;
        this.myStreamID = myStreamID;
         */
        this.context = context;
        this.engine = ZegoExpressEngine.getEngine();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         if (viewType == 1){
             // Return publishing view
             return new PreviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview, null));//推流视图
        }
         else {
             // Return playing view
             return new PlayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_view, null));//拉流视图
         }
    }

    @Override
    public int getItemViewType(int position) {
        // The publishing view is at position 0.
        if (position == 0)
            // Return publishing view type
            return 1;
        else
            // Return playing view type
            return 0;
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //if it is play view, set the play view
        if (holder instanceof PlayViewHolder) {
            setPlayingView((PlayViewHolder) holder,position);
        }
        else {
            //If it is preview, set the preview
            setPreview((PreviewViewHolder) holder,position);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    //return the streamID of a specific user
    public String getStream(ZegoUser goalUser, ArrayList<ZegoStream> StreamList){
        for (ZegoStream stream:StreamList){
            if (stream.user.userID.equals(goalUser.userID))
                return stream.streamID;
        }
        return "";
    }

    //set the play view
    public void setPlayingView(PlayViewHolder holder, int position){
        //get the corresponding user
        ZegoUser user = userList.get(position);
        ZegoCanvas playView = new ZegoCanvas(holder.playVideo);
        // get streamID of the user
        String streamID = getStream(user, streams);
        if (!isPlay.containsKey(streamID)){
            isPlay.put(streamID,false);
        }
        if (!playerState.containsKey(streamID)){
            playerState.put(streamID,0);
        }

        initPlayerText(holder,user.userName,streamID);
        setPlayButtonEvent(holder,streamID,playView);
        setPlayingAudioButtonEvent(holder,streamID);
        setPlayingVideoButtonEvent(holder,streamID);
        setViewModeButtonEvent(holder,streamID,playView);
        setPlayerQuality(holder,streamID);
        updatePlayerState(holder,streamID);
    }
    public void initPlayerText(PlayViewHolder holder, String userName, String streamID){
        holder.myName.setText(userName);
        holder.streamID.setText(streamID);
    }
    public void setPlayButtonEvent(PlayViewHolder holder,String streamID,ZegoCanvas playView){
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //check whether the view is playing stream
                if (isPlay.get(streamID)) {
                    engine.stopPlayingStream(streamID);
                    holder.playButton.setText(context.getResources().getString(R.string.start_playing));
                    holder.qualityLayout.setVisibility(View.INVISIBLE);
                    isPlay.put(streamID,false);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                } else {
                    //check whether the stream has been published.
                    if (!streamID.equals("")) {
                        engine.startPlayingStream(streamID, playView);
                        holder.playButton.setText(context.getResources().getString(R.string.stop_playing));
                        holder.qualityLayout.setVisibility(View.VISIBLE);
                        isPlay.put(streamID,true);
                        AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                    }
                    else {
                        Toast.makeText(context,"The publisher has not started publishing.",Toast.LENGTH_LONG).show();
                        holder.qualityLayout.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }
    public void setPlayingAudioButtonEvent(PlayViewHolder holder,String streamID){
        holder.audioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // control whether the audio will be played
                if (isChecked) {
                    engine.mutePlayStreamAudio(streamID, false);
                    AppLogger.getInstance().callApi("Disable Audio");
                } else {
                    engine.mutePlayStreamAudio(streamID, true);
                    AppLogger.getInstance().callApi("Enable Audio");
                }
            }
        });
    }
    public void setPlayingVideoButtonEvent(PlayViewHolder holder,String streamID){
        holder.videoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // control whether the video will be played
                if (isChecked) {
                    engine.mutePlayStreamVideo(streamID, false);
                    AppLogger.getInstance().callApi("Disable Video");
                } else {
                    engine.mutePlayStreamVideo(streamID, true);
                    AppLogger.getInstance().callApi("Enable Video");
                }
            }
        });
    }
    public void setViewModeButtonEvent(PlayViewHolder holder,String streamID,ZegoCanvas playView){
        holder.ViewModeButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] ViewOptions = context.getResources().getStringArray(R.array.viewMode);
                //check whether the stream has been published. The stream should stop playing before modifying the view mode.
                if (!streamID.equals("") && isPlay.get(streamID)) {
                    engine.stopPlayingStream(streamID);
                    switch (ViewOptions[position]) {
                        case "AspectFit":
                            playView.viewMode = ZegoViewMode.ASPECT_FIT;
                            AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FIT");
                            break;
                        case "AspectFill":
                            playView.viewMode = ZegoViewMode.ASPECT_FILL;
                            AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.ASPECT_FILL");
                            break;
                        case "ScaleToFill":
                            playView.viewMode = ZegoViewMode.SCALE_TO_FILL;
                            AppLogger.getInstance().callApi("Change View Mode: mode = ZegoViewMode.SCALE_TO_FILL");
                            break;
                    }
                    if (!streamID.equals("") && isPlay.get(streamID)) {
                        engine.startPlayingStream(streamID, playView);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setPlayerQuality(PlayViewHolder holder, String streamID){
        // display the video size and quality attributions of playing streams
        if (videoSize.get(streamID)!=null){
            holder.resolution.setText(videoSize.get(streamID)[0]+"x"+ videoSize.get(streamID)[1]);
        }
        if (streamQuality.get(streamID)!=null){
            ZegoPlayStreamQuality quality = streamQuality.get(streamID);
            holder.videoSendBitrate.setText(String.format("%.2f", quality.videoKBPS) + "kbps");
            holder.videoFPS.setText(String.format("%.2f",quality.videoRecvFPS) + "f/s");
            holder.rtt.setText(quality.rtt + "ms");
            holder.delay.setText(quality.delay + "ms");
            holder.packetLoss.setText(quality.packetLostRate + "%");
        }
    }
    public void updatePlayerState(PlayViewHolder holder, String streamID){
        switch (playerState.get(streamID)){
            case 0:
                holder.playButton.setText(context.getResources().getString(R.string.start_playing));
                break;
            case 1:
                holder.playButton.setText(getEmojiStringByUnicode(crossEmoji) + context.getResources().getString(R.string.stop_playing));
                break;
            case 2:
                holder.playButton.setText(getEmojiStringByUnicode(checkEmoji) + context.getResources().getString(R.string.stop_playing));
                break;
        }
    }
    // Set player state based on ZegoPlayerState and errorCode
    public void setPlayerState(String streamID, ZegoPlayerState state, int errorCode){
        if (!playerState.containsKey(streamID)){
            return;
        }
        if (!isPlay.containsKey(streamID)){
            return;
        }
        playerStateStreamID = streamID;
        if (errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
            if (isPlay.get(streamID)){
                // The user fail to play the stream.
                playerState.put(streamID,1);
            } else {
                playerState.put(streamID,0);
            }
        } else {
            if (isPlay.get(streamID)) {
                // The user is playing the stream successfully.
                playerState.put(streamID,2);
            } else {
                playerState.put(streamID,0);
            }
        }
    }
    public void setPreview(PreviewViewHolder holder,int position){
        //get the user
        ZegoUser user = userList.get(position);
        //if the preview has not started, start the preview and initialize the UI
        if (!isPreview){
            engine.startPreview(new ZegoCanvas(holder.preview));
            isPreview = true;
            String name = context.getResources().getString(R.string.Me);
            holder.myName.setText(name + user.userName);
            holder.qualityLayout.setVisibility(View.GONE);
        }
        setStartPublishButtonEvent(holder);
        setEnableCameraButtonEvent(holder);
        setEnableMicrophoneButtonEvent(holder);
        setMirrorButtonEvent(holder);
        setCameraSwitchButtonEvent(holder);
        setPublishQuality(holder);
        updatePublisherState(holder);

    }
    // Update publisher State in UI
    public void updatePublisherState(PreviewViewHolder holder){
        switch (publisherState){
            case 1:
                holder.startPublishButton.setText(getEmojiStringByUnicode(crossEmoji) + context.getResources().getString(R.string.stop_publishing));
                break;
            case 2:
                holder.startPublishButton.setText(getEmojiStringByUnicode(checkEmoji) + context.getResources().getString(R.string.stop_publishing));
                break;
        }
    }
    public void setPublisherState(int state){
        publisherState = state;
    }
    public void setStartPublishButtonEvent(PreviewViewHolder holder){
        holder.startPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlubish) {
                    engine.stopPublishingStream();
                    holder.startPublishButton.setText(context.getResources().getString(R.string.start_publishing));
                    isPlubish = false;
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",myStreamID);
                    holder.qualityLayout.setVisibility(View.INVISIBLE);
                } else {
                    engine.startPublishingStream(myStreamID);
                    holder.startPublishButton.setText(context.getResources().getString(R.string.stop_publishing));
                    holder.resolution.setText(engine.getVideoConfig().encodeWidth+"x"+engine.getVideoConfig().encodeHeight);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",myStreamID);
                    isPlubish = true;
                    holder.qualityLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
    public void setEnableCameraButtonEvent(PreviewViewHolder holder){
        holder.enableCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // control whether the camera is enable.
                if (isChecked) {
                    engine.enableCamera(true);
                    AppLogger.getInstance().callApi("Enable Video");
                } else {
                    engine.enableCamera(false);
                    AppLogger.getInstance().callApi("Disable Video");
                }
            }
        });
    }
    public void setEnableMicrophoneButtonEvent(PreviewViewHolder holder){
        holder.enableMicrophone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // control whether the microphone is enable.
                if (isChecked) {
                    engine.muteMicrophone(false);
                    AppLogger.getInstance().callApi("Enable Audio");
                } else {
                    engine.muteMicrophone(true);
                    AppLogger.getInstance().callApi("Disable Audio");
                }
            }
        });
    }
    public void setMirrorButtonEvent(PreviewViewHolder holder){
        holder.mirrorButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //control the mirror mode
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] MirrorOptions = context.getResources().getStringArray(R.array.mirrorMode);
                switch (MirrorOptions[position]){
                    case "OnlyPreview":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PREVIEW_MIRROR");
                        break;
                    case "OnlyPublish":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.ONLY_PUBLISH_MIRROR");
                        break;
                    case "Both":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.BOTH_MIRROR");
                        break;
                    case "None":
                        engine.setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR);
                        AppLogger.getInstance().callApi("Change Mirror Mode: mode = ZegoVideoMirrorMode.NO_MIRROR");
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setCameraSwitchButtonEvent(PreviewViewHolder holder) {
        //switch the camera
        holder.cameraSwitchButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] CameraOptions = context.getResources().getStringArray(R.array.cameraSwitch);
                switch (CameraOptions[position]){
                    case "Front":
                        engine.useFrontCamera(true);
                        AppLogger.getInstance().callApi("Switch Camera: Front");
                        break;
                    case "Back":
                        engine.useFrontCamera(false);
                        AppLogger.getInstance().callApi("Switch Camera: Back");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setPublishQuality(PreviewViewHolder holder) {
        //display quality attributions
        if (publishQuality != null) {
            holder.videoSendBitrate.setText(String.format("%.2f", publishQuality.videoKBPS) + "kbps");
            holder.videoFPS.setText(String.format("%.2f", publishQuality.videoSendFPS) + "f/s");
            holder.rtt.setText(publishQuality.rtt + "ms");
            holder.packetLoss.setText(publishQuality.packetLostRate + "%");
        }
    }
    public class PlayViewHolder extends RecyclerView.ViewHolder{
        SurfaceView playVideo;
        TextView myName;
        TextView resolution;
        TextView videoSendBitrate;
        TextView videoFPS;
        TextView delay;
        TextView rtt;
        TextView packetLoss;
        TextView userName;
        TextView streamID;
        Spinner ViewModeButton;
        SwitchMaterial videoButton;
        SwitchMaterial audioButton;
        Button playButton;
        RelativeLayout qualityLayout;

        public PlayViewHolder(@NonNull View itemView) {
            super(itemView);
            playVideo = itemView.findViewById(R.id.playView);
            myName = itemView.findViewById(R.id.userName);
            resolution = itemView.findViewById(R.id.playerResolution);
            videoSendBitrate = itemView.findViewById(R.id.playerBitrate);
            videoFPS = itemView.findViewById(R.id.videoFps);
            rtt = itemView.findViewById(R.id.rtt);
            delay = itemView.findViewById(R.id.delay);
            packetLoss = itemView.findViewById(R.id.loss);
            ViewModeButton = itemView.findViewById(R.id.viewModeButton);
            videoButton = itemView.findViewById(R.id.videoButton);
            audioButton = itemView.findViewById(R.id.audioButton);
            playButton = itemView.findViewById(R.id.playButton);
            userName = itemView.findViewById(R.id.userName);
            streamID = itemView.findViewById(R.id.streamID);
            qualityLayout = itemView.findViewById(R.id.qualityLayout);
        }
    }

    public class PreviewViewHolder extends RecyclerView.ViewHolder{
        Button startPublishButton;
        Button startPlayButton;
        Spinner mirrorButton;
        Spinner cameraSwitchButton;
        SurfaceView preview;
        TextView myName;
        TextView resolution;
        TextView videoSendBitrate;
        TextView videoFPS;
        TextView rtt;
        TextView packetLoss;
        RelativeLayout qualityLayout;
        SwitchMaterial enableCamera;
        SwitchMaterial enableMicrophone;
        RecyclerView playView;
        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            startPublishButton = itemView.findViewById(R.id.publishButton);
            startPlayButton = itemView.findViewById(R.id.playButton);
            mirrorButton = itemView.findViewById(R.id.mirrorButton);
            cameraSwitchButton = itemView.findViewById(R.id.switchButton);
            preview = itemView.findViewById(R.id.preview);
            myName = itemView.findViewById(R.id.myName);
            resolution = itemView.findViewById(R.id.playerResolution);
            videoSendBitrate = itemView.findViewById(R.id.playerBitrate);
            videoFPS = itemView.findViewById(R.id.videoFps);
            rtt = itemView.findViewById(R.id.rtt);
            packetLoss = itemView.findViewById(R.id.loss);
            qualityLayout = itemView.findViewById(R.id.qualityLayout);
            enableCamera = itemView.findViewById(R.id.cameraButton);
            enableMicrophone = itemView.findViewById(R.id.microphoneButton);
            playView = itemView.findViewById(R.id.allVideo);
        }
    }
}

