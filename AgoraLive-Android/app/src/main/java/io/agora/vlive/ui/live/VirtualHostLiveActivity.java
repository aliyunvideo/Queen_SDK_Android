package io.agora.vlive.ui.live;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;

import io.agora.capture.video.camera.VideoModule;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.framework.modules.channels.ChannelManager;
import io.agora.rtc.Constants;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.SeatStateMessage;
import io.agora.vlive.protocol.manager.SeatServiceManager;
import io.agora.vlive.protocol.model.model.SeatInfo;
import io.agora.vlive.protocol.model.request.AudienceListRequest;
import io.agora.vlive.protocol.model.request.ModifySeatStateRequest;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.response.AudienceListResponse;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.protocol.model.response.Response;
import io.agora.vlive.ui.actionsheets.InviteUserActionSheet;
import io.agora.vlive.ui.actionsheets.toolactionsheet.LiveRoomToolActionSheet;
import io.agora.vlive.ui.components.LinearLayout9to8;
import io.agora.vlive.ui.components.bottomLayout.LiveBottomButtonLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.UserUtil;

public class VirtualHostLiveActivity extends LiveRoomActivity implements View.OnClickListener,
        InviteUserActionSheet.InviteUserActionSheetListener {
    private static final String TAG = VirtualHostLiveActivity.class.getSimpleName();
    private static final String SAVED_IMAGE = "saved-image";

    private static final int AUDIENCE_SELECT_IMAGE_REQ_CODE = 1;
    private static final int SURFACE_VIEW_DRAW_LATENCY = 600;

    private LiveHostNameLayout mNamePad;

    private RelativeLayout mSingleLayout;
    private FrameLayout mSingleHostVideoLayout;
    private View mSingleHostVideoMask;

    private LinearLayout9to8 mChatVideoLayout;
    private RelativeLayout mOwnerVideoOutLayer;
    private RelativeLayout mHostVideoOutLayer;
    private View mOwnerVideoMask;
    private View mHostVideoMask;
    private AppCompatTextView mFunBtn;
    private boolean mLayoutCalculated;
    private int mVirtualImageSelected;
    private boolean mConnected;
    private String mHostUserId;
    private int mHostUid = -1; // for rtc
    private PreprocessorFaceUnity mPreprocessor;
    private InviteUserActionSheet mInviteUserListActionSheet;
    private boolean mInitAsOwner;
    private boolean mVirtualImageRemoved;

    private SeatServiceManager mSeatManager;

    // To avoid inconsistent video frames, we must
    // insure that the effect bundle loaded and
    // camera preview is started and stable.
    // Thus we wouldn't see a black surface when
    // the camera starting contribute a short
    // latency; if the camera preview is started
    // earlier than the effect bundle is loaded,
    // we will see the original frames from camera.
    private int mVideoInitCount;

    private PreprocessorFaceUnity.OnFuEffectBundleLoadedListener
            mBundleListener = () -> {
                if (mVirtualImageRemoved) {
                    mVirtualImageRemoved = false;
                    return;
                }
                startCameraCapture();
                tryDisplayMyVirtualImage();
            };

    private PreprocessorFaceUnity.OnFirstFrameListener
            mOnFirstFrameListener = this::tryDisplayMyVirtualImage;

    private void tryDisplayMyVirtualImage() {
        if (mVideoInitCount >= 2) {
            return;
        }

        mVideoInitCount++;
        if (mVideoInitCount == 2) {
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            TextureView textureView = new TextureView(this);
            setLocalPreview(textureView);
            runOnUiThread(() -> {
                if (mConnected) {
                    if (isHost) {
                        if (mHostVideoOutLayer.getChildCount() > 1) {
                            mHostVideoOutLayer.removeViewAt(0);
                        }
                        mHostVideoOutLayer.addView(textureView, 0);
                        mHostVideoMask.setVisibility(View.GONE);
                    } else if (isOwner) {
                        if (mOwnerVideoOutLayer.getChildCount() > 1) {
                            mOwnerVideoOutLayer.removeViewAt(0);
                        }
                        mOwnerVideoOutLayer.addView(textureView, 0);
                        mOwnerVideoMask.setVisibility(View.GONE);
                    }
                } else if (isOwner) {
                    if (mSingleHostVideoLayout.getChildCount() > 1) {
                        mSingleHostVideoLayout.removeViewAt(0);
                    }
                    mSingleHostVideoLayout.addView(textureView, 0);
                    mSingleHostVideoMask.setVisibility(View.GONE);
                }
            });
        }
    }

    // Universal handling of the results of sending rtm messages
    private ResultCallback<Void> mMessageResultCallback = new ResultCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(ErrorInfo errorInfo) {
            showLongToast("Message error:" + errorInfo.getErrorDescription());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
    }

    @Override
    protected void onPermissionGranted() {
        initUI();
        isHost = false;
        mSeatManager = new SeatServiceManager(application());
        super.onPermissionGranted();
    }

    private void initUI() {
        mPreprocessor = (PreprocessorFaceUnity) VideoModule.instance().
            getPreprocessor(ChannelManager.ChannelID.CAMERA);
        mPreprocessor.setOnBundleLoadedListener(mBundleListener);
        mPreprocessor.setOnFirstFrameListener(mOnFirstFrameListener);
        mVirtualImageSelected = getIntent().getIntExtra(
                Global.Constants.KEY_VIRTUAL_IMAGE, -1);

        setContentView(R.layout.activity_virtual_host);

        mSingleLayout = findViewById(R.id.virtual_image_single_layout);
        mSingleHostVideoLayout = findViewById(R.id.virtual_image_single_host_video_layout);
        mSingleHostVideoMask = findViewById(R.id.virtual_image_single_video_mask);

        mChatVideoLayout = findViewById(R.id.virtual_live_video_layout);
        mOwnerVideoOutLayer = findViewById(R.id.virtual_live_owner_video_outer_layout);
        mHostVideoOutLayer = findViewById(R.id.virtual_live_host_video_outer_layout);

        mOwnerVideoMask = findViewById(R.id.virtual_live_owner_mask);
        mHostVideoMask = findViewById(R.id.virtual_live_host_mask);

        mFunBtn = findViewById(R.id.virtual_image_function_btn);

        mNamePad = findViewById(R.id.virtual_live_name_pad);
        mNamePad.init(true);

        participants = findViewById(R.id.virtual_live_participant);
        participants.init(true);
        participants.setUserLayoutListener(this);

        bottomButtons = findViewById(R.id.virtual_live_bottom_layout);
        bottomButtons.init(true, true);
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageList = findViewById(R.id.message_list);
        messageList.init(true);
        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        rtcStatsView = findViewById(R.id.virtual_host_rtc_stats);
        rtcStatsView.setCloseListener(view -> rtcStatsView.setVisibility(View.GONE));

        mChatVideoLayout.setVisibility(View.GONE);

        // In case that the UI is not relocated because
        // the permission request dialog consumes the chance
        onGlobalLayoutCompleted();

        if (isOwner) {
            // Comes from prepare activity, the video capture has
            // started and the image bundle has loaded.
            TextureView textureView = new TextureView(this);
            setLocalPreview(textureView);
            mSingleHostVideoLayout.addView(textureView);
            mSingleHostVideoMask.setVisibility(View.GONE);
            mInitAsOwner = true;
        }
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.virtual_live_top_participant_layout);
        if (topLayout != null && !mLayoutCalculated) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
            mLayoutCalculated = true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_bottom_btn_close:
                checkBeforeLeaving();
                break;
            case R.id.live_bottom_btn_more:
                LiveRoomToolActionSheet toolSheet =
                        (LiveRoomToolActionSheet) showActionSheetDialog(
                        ACTION_SHEET_TOOL, tabIdToLiveType(tabId),
                    isOwner || isHost, true, this);
                toolSheet.setVirtualImage(true);
                toolSheet.setEnableInEarMonitoring(inEarMonitorEnabled);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC,
                            tabIdToLiveType(tabId),
                            true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT,
                            tabIdToLiveType(tabId),
                            false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                // this button is hidden when
                // current user is not host.
                if (isHost || isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BEAUTY,
                            tabIdToLiveType(tabId),
                            true, true, this);
                }
                break;
            case R.id.dialog_positive_button:
                if (mConnected) {
                    if (isOwner) {
                        mSeatManager.forceLeave(roomId, mHostUserId, 1);
                    } else {
                        mSeatManager.hostLeave(roomId, ownerId, 1);
                    }
                }
                removeCachedVirtualImage();
                leaveRoom();
                break;
        }
    }

    private void checkBeforeLeaving() {
        curDialog = showDialog(R.string.end_live_streaming_title_owner,
                R.string.end_live_streaming_message_owner, this);
    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        super.onEnterRoomResponse(response);
        if (response.code == Response.SUCCESS) {
            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;

            isHost = false;

            // Determine if I am the owner of a host here because
            // I may leave the room unexpectedly and come once more.
            String myId = config().getUserProfile().getUserId();
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
                myRtcRole = Constants.CLIENT_ROLE_BROADCASTER;
                rtcEngine().setClientRole(myRtcRole);
                mVirtualImageSelected = getCachedVirtualImage();
            }

            // Check if someone is the host
            List<SeatInfo> seatListInfo = response.data.room.coVideoSeats;
            if (seatListInfo.size() > 0) {
                SeatInfo info = seatListInfo.get(0);
                if (info.seat.state == SeatInfo.TAKEN) {
                    mConnected = true;
                    mHostUserId = info.user.userId;
                    mHostUid = info.user.uid;
                }
            }

            boolean audioMutedOwner = response.data.room.owner.enableAudio !=
                    SeatInfo.User.USER_AUDIO_ENABLE;
            boolean videoMutedOwner = response.data.room.owner.enableVideo !=
                    SeatInfo.User.USER_VIDEO_ENABLE;

            runOnUiThread(() -> {
                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));

                if (mConnected) {
                    toChatDisplay();
                    isHost = myId.equals(mHostUserId);
                    mVirtualImageSelected = getCachedVirtualImage();

                    if (isHost) {
                        SeatInfo info = response.data.room.coVideoSeats.get(0);
                        boolean audioMuted = info.user.enableAudio != SeatInfo.User.USER_AUDIO_ENABLE;
                        boolean videoMuted = info.user.enableVideo != SeatInfo.User.USER_VIDEO_ENABLE;
                        becomesHost(audioMuted, videoMuted);
                        setRemoteVideoSurface(mOwnerVideoOutLayer, mOwnerVideoMask, ownerRtcUid, true);
                    } else if (isOwner) {
                        becomesOwner(audioMutedOwner, videoMutedOwner, mInitAsOwner, true);
                        setRemoteVideoSurface(mHostVideoOutLayer, mHostVideoMask, mHostUid, true);
                    } else {
                        setRemoteVideoSurface(mOwnerVideoOutLayer, mOwnerVideoMask, ownerRtcUid, true);
                        setRemoteVideoSurface(mHostVideoOutLayer, mHostVideoMask, mHostUid, true);
                    }
                } else {
                    if (isOwner) {
                        // If mInitAsOwner is true, that is, the user creates
                        // this room and then enters. The video has already
                        // started and the virtual image has been set.
                        // If the user is identified as the owner according
                        // to the information returned from server, he needs to
                        // start video capture here
                        becomesOwner(audioMutedOwner, videoMutedOwner, mInitAsOwner, false);
                    } else {
                        becomeAudience();
                    }
                }
            });
        }
    }

    private void becomesOwner(boolean audioMuted, boolean videoMuted,
                              boolean videoInitialized, boolean connected) {
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        config().setBeautyEnabled(true);
        mFunBtn.setVisibility(View.VISIBLE);
        mFunBtn.setText(R.string.live_virtual_image_invite);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_OWNER);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

        if (!videoInitialized) {
            // Wait the bundle and capture callback to
            // handle the rest work.
            mPreprocessor.onAnimojiSelected(mVirtualImageSelected);
        }

        if (connected) {
            mFunBtn.setText(R.string.live_virtual_image_stop_invite);
        } else {
            mFunBtn.setText(R.string.live_virtual_image_invite);
        }
    }

    private void becomeAudience() {
        isHost = false;
        stopCameraCapture();
        mFunBtn.setVisibility(View.GONE);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        config().setAudioMuted(true);
        config().setVideoMuted(true);
        rtcEngine().muteLocalAudioStream(true);
        rtcEngine().muteLocalVideoStream(true);
        mVirtualImageRemoved = true;
        mPreprocessor.onAnimojiSelected(-1);
    }

    private void becomesHost(boolean audioMuted, boolean videoMuted) {
        isHost = true;
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        rtcEngine().muteLocalAudioStream(audioMuted);
        rtcEngine().muteLocalVideoStream(videoMuted);
        config().setBeautyEnabled(true);
        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
        mFunBtn.setVisibility(View.VISIBLE);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_HOST);
        mPreprocessor.onAnimojiSelected(mVirtualImageSelected);
    }

    @Override
    public void onBackPressed() {
        checkBeforeLeaving();
    }

    @Override
    public void finish() {
        super.finish();
        findViewById(R.id.virtual_live_video_layout).setVisibility(View.GONE);
        bottomButtons.clearStates(application());
        mVirtualImageRemoved = true;
        mPreprocessor.onAnimojiSelected(-1);
    }

    public void onInvite(View view) {
        if (mConnected) {
            curDialog = showDialog(R.string.live_virtual_image_stop_invite,
                    R.string.live_virtual_image_stop_invite_message,
                    R.string.dialog_positive_button,
                    R.string.dialog_negative_button,
                    v -> {
                        Config.UserProfile profile = config().getUserProfile();
                        if (isOwner || isHost) {
                            ModifySeatStateRequest request = new ModifySeatStateRequest(
                                    profile.getToken(), roomId,
                                    profile.getUserId(),
                                    1,   // Only one seat here in virtual image live room
                                    SeatInfo.OPEN);
                            request.setVirtualAvatar(
                                    virtualImageIdToName(mVirtualImageSelected));
                            sendRequest(Request.MODIFY_SEAT_STATE, request);
                            removeCachedVirtualImage();
                            curDialog.dismiss();
                        }
                    },
                    v -> curDialog.dismiss());
        } else {
            mInviteUserListActionSheet = (InviteUserActionSheet)
                    showActionSheetDialog(ACTION_SHEET_INVITE_AUDIENCE,
                            tabIdToLiveType(tabId), isHost, true, this);
            requestAudienceList();
        }
    }

    private void requestAudienceList() {
        sendRequest(Request.AUDIENCE_LIST, new AudienceListRequest(
                config().getUserProfile().getToken(),
                roomId, null, AudienceListRequest.TYPE_AUDIENCE));
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        super.onAudienceListResponse(response);

        if (mInviteUserListActionSheet != null &&
                mInviteUserListActionSheet.isShown()) {
            runOnUiThread(() -> mInviteUserListActionSheet.append(response.data.list));
        }
    }

    @Override
    public void onActionSheetAudienceInvited(int seatId, String userId, String userName) {
        // seat id is no-use here because there is only one seat available.
        if (mInviteUserListActionSheet != null && mInviteUserListActionSheet.isShown()) {
            dismissActionSheetDialog();
        }

        mSeatManager.invite(roomId, userId, seatId);
    }

    @Override
    public void onRtmSeatInvited(String userId, String userName, int index) {
        if (isOwner) return;
        closeDialog();

        String title = getResources().getString(R.string.live_room_host_in_invite_user_list_action_sheet_title);
        String message = getResources().getString(R.string.live_room_virtual_image_invited_message);
        message = String.format(message, userName);
        final Config.UserProfile profile = config().getUserProfile();
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    // If the audience accepts the invitation,
                    // he should first choose a virtual image.
                    Intent intent = new Intent(this,
                            VirtualImageSelectActivity.class);
                    intent.putExtra(Global.Constants.KEY_PEER_ID, userId);
                    intent.putExtra(Global.Constants.KEY_NICKNAME, userName);
                    intent.putExtra(Global.Constants.KEY_AUDIENCE_VIRTUAL_IMAGE, true);
                    startActivityForResult(intent, AUDIENCE_SELECT_IMAGE_REQ_CODE);
                    curDialog.dismiss();
                },
                view -> {
                    mSeatManager.audienceReject(roomId, userId, index);
                    curDialog.dismiss();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;

        String peerId = data.getStringExtra(Global.Constants.KEY_PEER_ID);
        mVirtualImageSelected = data.getIntExtra(
                Global.Constants.KEY_VIRTUAL_IMAGE, -1);
        saveCachedVirtualImage(mVirtualImageSelected);
        mSeatManager.audienceAccept(roomId, peerId, 1);
        mFunBtn.setVisibility(View.VISIBLE);
        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
    }

    @Override
    public void onRtmInvitationAccepted(long processId, String userId, String userName, int index) {
        showShortToast(getResources().getString(R.string.invite_success));
    }

    @Override
    public void onRtmInvitationRejected(long processId, String userId, String userName, int index) {
        closeDialog();
        String title = getResources().getString(R.string.live_room_host_in_invite_rejected);
        String message = getResources().getString(R.string.live_room_host_in_invite_rejected_message);
        message = String.format(message, userName);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
    }

    @Override
    public void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> list) {
        SeatStateMessage.SeatStateMessageDataItem item = list.get(0);
        boolean taken = item.seat.state == SeatInfo.TAKEN;
        mHostUserId = item.user.userId;
        String myUid = config().getUserProfile().getUserId();

        runOnUiThread(() -> {
            if (!mConnected && taken && !TextUtils.isEmpty(mHostUserId)) {
                toChatDisplay();
                mConnected = true;
                mHostUid = item.user.uid;

                if (myUid.equals(mHostUserId)) {
                    becomesHost(false, false);

                    // I am about to be the host, need to
                    // switch to another view to display
                    // the owner's video.
                    setRemoteVideoSurface(mOwnerVideoOutLayer,
                            mOwnerVideoMask, ownerRtcUid, false);
                } else {
                    if (isOwner) {
                        TextureView textureView = new TextureView(this);
                        setLocalPreview(textureView);
                        mOwnerVideoOutLayer.addView(textureView, 0);
                        mOwnerVideoMask.setVisibility(View.GONE);
                        mFunBtn.setVisibility(View.VISIBLE);
                        mFunBtn.setText(R.string.live_virtual_image_stop_invite);
                    } else {
                        // If I am the audience, just move the owner's video
                        setRemoteVideoSurface(mOwnerVideoOutLayer,
                                mOwnerVideoMask, ownerRtcUid, false);
                    }
                }
            } else if (mConnected && !taken) {
                toSingleHostDisplay();
                mConnected = false;
                mHostUserId = null;
                mHostUid = -1;

                if (isOwner) {
                    mFunBtn.setVisibility(View.VISIBLE);
                    mFunBtn.setText(R.string.live_virtual_image_invite);

                    TextureView textureView = new TextureView(this);
                    setLocalPreview(textureView);
                    mSingleHostVideoLayout.addView(textureView);
                    mSingleHostVideoMask.setVisibility(View.GONE);
                } else {
                    if (isHost) {
                        isHost = false;
                        mVideoInitCount = 0;
                        mFunBtn.setVisibility(View.GONE);
                        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
                        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                        mPreprocessor.onAnimojiSelected(-1);
                        stopCameraCapture();
                    }

                    setRemoteVideoSurface(mSingleHostVideoLayout,
                            mSingleHostVideoMask, ownerRtcUid, true);
                }

            }
        });
    }

    private void setRemoteVideoSurface(ViewGroup parent, View mask, int uid, boolean delayed) {
        // delete the video surface which is possibly
        // left by last video displaying
        if (parent.getChildCount() > 1) {
            parent.removeViewAt(0);
        }

        SurfaceView surfaceView = setupRemoteVideo(uid);
        parent.addView(surfaceView, 0);
        if (delayed) {
            // we want to show this view after a short
            // while, in case that the surface might
            // not been initialized too soon.
            new Handler(getMainLooper()).postDelayed(() ->
                    mask.setVisibility(View.GONE),
                    SURFACE_VIEW_DRAW_LATENCY);
        } else {
            mask.setVisibility(View.GONE);
        }
    }

    private void toSingleHostDisplay() {
        mSingleLayout.setVisibility(View.VISIBLE);
        mSingleHostVideoLayout.setVisibility(View.VISIBLE);
        mSingleHostVideoMask.setVisibility(View.VISIBLE);
        mChatVideoLayout.setVisibility(View.GONE);

        removeFirstElement(mOwnerVideoOutLayer);
        removeFirstElement(mHostVideoOutLayer);
    }

    private void removeFirstElement(ViewGroup parent) {
        if (parent.getChildCount() > 1) {
            parent.removeViewAt(0);
        }
    }

    private void toChatDisplay() {
        mSingleHostVideoLayout.removeAllViews();
        mSingleLayout.setVisibility(View.GONE);
        mChatVideoLayout.setVisibility(View.VISIBLE);
        mOwnerVideoMask.setVisibility(View.VISIBLE);
        mHostVideoMask.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        super.onRtcRemoteVideoStateChanged(uid, state, reason, elapsed);
        if (ownerRtcUid == uid) {
            if (!isOwner && isVideoReceivingState(state)) {
                runOnUiThread(() -> {
                    if (mConnected) {
                        setRemoteVideoSurface(mOwnerVideoOutLayer,
                                mOwnerVideoMask, ownerRtcUid, true);
                    } else {
                        setRemoteVideoSurface(mSingleHostVideoLayout,
                                mSingleHostVideoMask, ownerRtcUid, true);
                    }
                });
            }
        } else if (mHostUid == uid) {
            if (!isHost && isVideoReceivingState(state)) {
                runOnUiThread(() -> setRemoteVideoSurface(
                        mHostVideoOutLayer, mHostVideoMask, mHostUid, true));
            }
        }
    }

    private boolean isVideoReceivingState(int state) {
        return state == Constants.REMOTE_VIDEO_STATE_DECODING;
    }

    private int getCachedVirtualImage() {
        return application().getSharedPreferences(SAVED_IMAGE, Context.MODE_PRIVATE).getInt(roomId, 0);
    }

    private void saveCachedVirtualImage(int image) {
        getSharedPreferences(SAVED_IMAGE, Context.MODE_PRIVATE).edit().putInt(roomId, image).apply();
    }

    private void removeCachedVirtualImage() {
        getSharedPreferences(SAVED_IMAGE, Context.MODE_PRIVATE).edit().remove(roomId).apply();
    }
}
