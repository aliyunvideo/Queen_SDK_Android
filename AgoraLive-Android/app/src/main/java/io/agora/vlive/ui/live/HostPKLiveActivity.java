package io.agora.vlive.ui.live;

import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.elvishew.xlog.XLog;

import io.agora.rtc.Constants;
import io.agora.rtc.video.ChannelMediaInfo;
import io.agora.rtc.video.ChannelMediaRelayConfiguration;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.PKStateMessage;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.manager.PKServiceManager;
import io.agora.vlive.protocol.model.model.RoomInfo;
import io.agora.vlive.protocol.model.model.SeatInfo;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.RoomRequest;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.protocol.model.response.Response;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.protocol.model.types.PKConstant;
import io.agora.vlive.ui.actionsheets.toolactionsheet.LiveRoomToolActionSheet;
import io.agora.vlive.ui.actionsheets.PkRoomListActionSheet;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.bottomLayout.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.ui.components.PkLayout;
import io.agora.vlive.utils.UserUtil;

public class HostPKLiveActivity extends LiveRoomActivity
        implements View.OnClickListener, PkRoomListActionSheet.OnPkRoomSelectedListener {
    private static final String TAG = HostPKLiveActivity.class.getSimpleName();

    private static final int PK_RESULT_DISPLAY_LAST = 2000;

    private RelativeLayout mLayout;
    private FrameLayout mVideoNormalLayout;
    private LiveHostNameLayout mNamePad;
    private PkRoomListActionSheet mPkRoomListActionSheet;
    private AppCompatImageView mStartPkButton;
    private PkLayout mPkLayout;
    private boolean mTopLayoutCalculated;

    private String mPKRoomId;
    private String mPKRoomUserName;
    private boolean mPkStarted;
    private boolean mBroadcastStarted;

    private PKServiceManager mPKManager;

    // When the owner returns to his room and the room
    // is in pk mode before he left, the owner needs to
    // start pk mode. But he also needs to join rtc channel
    // first. This pending request records the case.
    private boolean mPendingStartPkRequest;

    private EnterRoomResponse.RelayConfig mPendingPkConfig;

    private int mMessageListHeightInNormalMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(false);
    }

    @Override
    protected void onPermissionGranted() {
        mPKManager = new PKServiceManager(application());
        initUI();
        super.onPermissionGranted();
    }

    private void initUI() {
        mMessageListHeightInNormalMode = getResources().
                getDimensionPixelOffset(R.dimen.live_message_list_height);

        setContentView(R.layout.activity_pk_host_in);

        mLayout = findViewById(R.id.live_room_pk_room_layout);
        mVideoNormalLayout = findViewById(R.id.live_pk_video_normal_layout);
        mNamePad = findViewById(R.id.pk_host_in_name_pad);
        mNamePad.init();

        participants = findViewById(R.id.pk_host_in_participant);
        participants.init();
        participants.setUserLayoutListener(this);

        messageList = findViewById(R.id.message_list);
        messageList.init();

        bottomButtons = findViewById(R.id.pk_host_in_bottom_layout);
        bottomButtons.init();
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        mStartPkButton = findViewById(R.id.start_pk_button);
        mStartPkButton.setOnClickListener(this);

        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        mPkLayout = findViewById(R.id.pk_host_layout);

        // At the initialization phase, the room is considered to
        // be in single-broadcast mode.
        // Whether the room is already in PK mode or not depends
        // on the information returned in the "enter room" response.
        setupUIMode(false, isOwner);
        setupSingleBroadcastBehavior(isOwner, !isOwner, !isOwner);

        // If I am the room owner, I will start single broadcasting
        // right now and do not need to start in "enter room" response
        if (isOwner) mBroadcastStarted = true;

        rtcStatsView = findViewById(R.id.host_pk_rtc_stats);
        rtcStatsView.setCloseListener(view -> rtcStatsView.setVisibility(View.GONE));
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.pk_host_in_top_participant_layout);
        if (topLayout != null && !mTopLayoutCalculated) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
            mTopLayoutCalculated = true;
        }
    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        if (response.code == Response.SUCCESS) {
            Config.UserProfile profile = config().getUserProfile();
            profile.setRtcToken(response.data.user.rtcToken);
            profile.setAgoraUid(response.data.user.uid);

            rtcChannelName = response.data.room.channelName;
            roomId = response.data.room.roomId;
            roomName = response.data.room.roomName;

            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;

            // Determine if I am the owner of a host here because
            // I may leave the room unexpectedly and come once more.
            String myId = config().getUserProfile().getUserId();
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
            }

            // Result from server if the channel is in PK mode
            mPkStarted = response.data.room.pk.state == PKConstant.PK_STATE_PK;
            if (mPkStarted) mPKRoomId = response.data.room.pk.remoteRoom.roomId;

            runOnUiThread(() -> {
                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));

                participants.reset(response.data.room.currentUsers,
                        response.data.room.rankUsers);

                if (!mPkStarted) {
                    boolean audioMuted = config().isAudioMuted();
                    boolean videoMuted = config().isVideoMuted();

                    if (isOwner && !mBroadcastStarted) {
                        // I created this room and I left this room unexpectedly
                        // not long ago.
                        // This time I came from room list as an audience at first,
                        // but from the server response, I know that this is my room.
                        // I can start my broadcasting right now if not muted.
                        audioMuted = response.data.room.owner.enableAudio !=
                                SeatInfo.User.USER_AUDIO_ENABLE;
                        videoMuted = response.data.room.owner.enableVideo !=
                                SeatInfo.User.USER_VIDEO_ENABLE;
                    }

                    setupUIMode(false, isOwner);
                    setupSingleBroadcastBehavior(isOwner, audioMuted, videoMuted);
                    mBroadcastStarted = true;
                } else {
                    mBroadcastStarted = false;
                    mPendingStartPkRequest = true;
                    mPendingPkConfig = response.data.room.pk.relayConfig;
                    setupUIMode(true, isOwner);
                    setupPkBehavior(isOwner, response.data.room.pk.countDown,
                            response.data.room.pk.remoteRoom.owner.userName,
                            response.data.room.pk.relayConfig);
                    updatePkGiftRank(response.data.room.pk.localRank,
                            response.data.room.pk.remoteRank);
                }

                joinRtcChannel();
                joinRtmChannel();
            });
        }
    }

    private void setupUIMode(boolean isPkMode, boolean isOwner) {
        if (isPkMode) {
            mLayout.setBackgroundResource(R.drawable.dark_background);
            mStartPkButton.setVisibility(View.GONE);
            mVideoNormalLayout.setVisibility(View.GONE);
            mPkLayout.removeResult();
            mPkLayout.setVisibility(View.VISIBLE);
            mPkLayout.setHost(isOwner);
        } else {
            mLayout.setBackground(null);
            mStartPkButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            mPkLayout.removeResult();
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.setVisibility(View.GONE);
            mVideoNormalLayout.setVisibility(View.VISIBLE);
        }

        setupMessageListLayout(isPkMode);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER
                : LiveBottomButtonLayout.ROLE_AUDIENCE);
        bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
    }

    /**
     * Must be called after the desirable UI mode is already set up
     */
    private void setupPkBehavior(boolean isOwner, long remaining,
                                 String remoteName, EnterRoomResponse.RelayConfig config) {
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE;
        rtcEngine().setClientRole(myRtcRole);

        mPkLayout.setHost(isOwner);
        mPkLayout.setPKHostName(remoteName);
        mPkLayout.startCountDownTimer(remaining);
        if (!isOwner) {
            mPkLayout.setOnClickGotoPeerChannelListener(view -> enterAnotherPkRoom(mPKRoomId));
        }

        if (isOwner) {
            startCameraCapture();
            CameraTextureView cameraTextureView = new CameraTextureView(this);
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getLeftVideoLayout().addView(cameraTextureView);
            SurfaceView remoteSurfaceView = setupRemoteVideo(config.remote.uid);
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().addView(remoteSurfaceView);
            rtcEngine().muteLocalAudioStream(false);
            rtcEngine().muteLocalVideoStream(false);
            config().setAudioMuted(false);
            config().setVideoMuted(false);
        } else {
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getLeftVideoLayout().addView(surfaceView);
            surfaceView.setZOrderMediaOverlay(true);
            SurfaceView remoteSurfaceView = setupRemoteVideo(config.remote.uid);
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().addView(remoteSurfaceView);
            remoteSurfaceView.setZOrderMediaOverlay(true);
        }
    }

    /**
     * Must be called after the desirable UI mode is already set up
     */
    private void setupSingleBroadcastBehavior(boolean isOwner, boolean audioMuted, boolean videoMuted) {
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER
                : Constants.CLIENT_ROLE_AUDIENCE;
        rtcEngine().setClientRole(myRtcRole);

        if (isOwner) {
            startCameraCapture();
            CameraTextureView cameraTextureView = new CameraTextureView(this);
            mVideoNormalLayout.addView(cameraTextureView);
        } else {
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            mVideoNormalLayout.removeAllViews();
            mVideoNormalLayout.addView(surfaceView);
        }

        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        rtcEngine().muteLocalAudioStream(audioMuted);
        rtcEngine().muteLocalVideoStream(videoMuted);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER : LiveBottomButtonLayout.ROLE_AUDIENCE);
    }

    private void startMediaRelay(EnterRoomResponse.RelayConfig config) {
        ChannelMediaRelayConfiguration relayConfig = new ChannelMediaRelayConfiguration();
        relayConfig.setSrcChannelInfo(toChannelMediaInfo(config.local));
        relayConfig.setDestChannelInfo(config.proxy.channelName, toChannelMediaInfo(config.proxy));
        rtcEngine().startChannelMediaRelay(relayConfig);
    }

    private ChannelMediaInfo toChannelMediaInfo(EnterRoomResponse.RelayInfo proxy) {
        return new ChannelMediaInfo(proxy.channelName, proxy.token, proxy.uid);
    }

    @Override
    public void onRtcJoinChannelSuccess(String channel, int uid, int elapsed) {
        if (isOwner && mPendingStartPkRequest && mPendingPkConfig != null) {
            startMediaRelay(mPendingPkConfig);
            mPendingStartPkRequest = false;
        }
    }

    private void setupMessageListLayout(boolean isPkMode) {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) messageList.getLayoutParams();
        if (isPkMode) {
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            params.addRule(RelativeLayout.BELOW, R.id.pk_host_layout);
        } else {
            params.height = mMessageListHeightInNormalMode;
            params.removeRule(RelativeLayout.BELOW);
        }
        messageList.setLayoutParams(params);
    }

    private void stopPkMode(boolean isOwner) {
        rtcEngine().stopChannelMediaRelay();
        setupUIMode(false, isOwner);
        setupSingleBroadcastBehavior(isOwner,
                config().isAudioMuted(),
                config().isVideoMuted());
    }

    private void enterAnotherPkRoom(String roomId) {
        rtcEngine().leaveChannel();
        leaveRtmChannel(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });

        sendRequest(Request.LEAVE_ROOM, new RoomRequest(
                config().getUserProfile().getToken(), this.roomId));

        enterRoom(roomId);
    }

    private void updatePkGiftRank(int mine, int other) {
        if (mPkStarted && mPkLayout.getVisibility() == View.VISIBLE) {
            mPkLayout.setPoints(mine, other);
        }
    }

    @Override
    public void onRtcChannelMediaRelayStateChanged(int state, int code) {
        if (state == Constants.RELAY_STATE_CONNECTING) {
            XLog.d("channel media relay is connecting");
        } else if (state == Constants.RELAY_STATE_RUNNING) {
            XLog.d("channel media relay is running");
        } else if (state == Constants.RELAY_STATE_FAILURE) {
            XLog.e("channel media relay fails");
        }
    }

    @Override
    public void onRtcChannelMediaRelayEvent(int code) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {
        super.onRoomListResponse(response);
        if (mPkRoomListActionSheet != null && mPkRoomListActionSheet.isShown()) {
            runOnUiThread(() -> {
                filterOutCurrentRoom(response.data);
                mPkRoomListActionSheet.appendUsers(response.data);
            });
        }
    }

    private void filterOutCurrentRoom(RoomListResponse.RoomList list) {
        RoomInfo temp = null;
        for (RoomInfo info : list.list) {
            if (roomId.equals(info.roomId)) {
                temp = info;
                break;
            }
        }

        if (temp != null) list.list.remove(temp);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_bottom_btn_close:
                onBackPressed();
                break;
            case R.id.live_bottom_btn_more:
                LiveRoomToolActionSheet toolSheet = (LiveRoomToolActionSheet) showActionSheetDialog(
                        ACTION_SHEET_TOOL, tabIdToLiveType(tabId), isOwner, true, this);
                toolSheet.setEnableInEarMonitoring(inEarMonitorEnabled);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, tabIdToLiveType(tabId), true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT, tabIdToLiveType(tabId), false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when current user is not host.
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, tabIdToLiveType(tabId), true, true, this);
                }
                break;
            case R.id.start_pk_button:
                if (isOwner) {
                    mPkRoomListActionSheet = (PkRoomListActionSheet)
                            showActionSheetDialog(ACTION_SHEET_PK_ROOM_LIST, tabIdToLiveType(tabId), true, true, this);
                    mPkRoomListActionSheet.setup(proxy(), config().getUserProfile().getToken(),
                            ClientProxy.ROOM_TYPE_PK);
                    mPkRoomListActionSheet.requestMorePkRoom();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPkStarted) {
            String title = getString(R.string.dialog_pk_force_quit_title);
            String message = getString(R.string.dialog_pk_force_quit_message);
            message = String.format(message, mPKRoomUserName != null ? mPKRoomUserName : "");
            curDialog = showDialog(title, message,
                    R.string.dialog_positive_button,
                    R.string.dialog_negative_button,
                    v -> leaveRoom(),
                    v -> closeDialog());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        bottomButtons.clearStates(application());
    }

    @Override
    public void onPkRoomListActionSheetRoomSelected(int position, String roomId, int uid) {
        // The owner sends a request to invite another host for a PK session
        mPKRoomId = roomId;
        mPKManager.invitePK(this.roomId, mPKRoomId);
        dismissActionSheetDialog();
    }

    @Override
    public void onRtmPkReceivedFromAnotherHost(String userId, String userName, String pkRoomId) {
        // Received a pk request from another host,
        // here show a dialog to make a decision.
        String title = getResources().getString(R.string.live_room_pk_room_receive_pk_request_title);
        String messageFormat = getResources().getString(R.string.live_room_pk_room_receive_pk_request_message);
        String message = String.format(messageFormat, userName);

        runOnUiThread(() -> curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    mPKManager.acceptPKInvitation(roomId, pkRoomId);
                    closeDialog();
                },
                view -> {
                    mPKManager.rejectPKInvitation(roomId, pkRoomId);
                    closeDialog();
                }));
    }

    @Override
    public void onRtmPkAcceptedByTargetHost(String userId, String userName, String pkRoomId) {
        runOnUiThread(() -> showShortToast(getResources().getString(R.string.live_room_pk_room_pk_invitation_accepted)));
    }

    @Override
    public void onRtmPkRejectedByTargetHost(String userId, String userName, String pkRoomId) {
        runOnUiThread(() -> showShortToast(getResources().getString(R.string.live_room_pk_room_pk_invitation_rejected)));
    }

    @Override
    public void onRtmReceivePKEvent(PKStateMessage.PKStateMessageBody messageData) {
        runOnUiThread(() -> {
            if (messageData.event == PKConstant.PK_EVENT_START) {
                mPkStarted = true;
                mPKRoomId = messageData.remoteRoom.roomId;
                mPKRoomUserName = messageData.remoteRoom.owner.userName;
                setupUIMode(true, isOwner);
                setupPkBehavior(isOwner, messageData.countDown,
                        mPKRoomUserName,
                        messageData.relayConfig);
                startMediaRelay(messageData.relayConfig);
                updatePkGiftRank(messageData.localRank, messageData.remoteRank);
            } else if (messageData.event == PKConstant.PK_EVENT_RANK_CHANGED) {
                updatePkGiftRank(messageData.localRank, messageData.remoteRank);
            } else if (messageData.event == PKConstant.PK_EVENT_END) {
                mPkLayout.setResult(messageData.result);
                new Handler(getMainLooper()).postDelayed(() -> stopPkMode(isOwner), PK_RESULT_DISPLAY_LAST);
                mPkStarted = false;
                showShortToast(getResources().getString(R.string.pk_ends));
            }
        });
    }
}
