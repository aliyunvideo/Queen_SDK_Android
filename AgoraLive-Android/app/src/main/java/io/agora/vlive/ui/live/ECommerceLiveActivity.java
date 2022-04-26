package io.agora.vlive.ui.live;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.video.ChannelMediaInfo;
import io.agora.rtc.video.ChannelMediaRelayConfiguration;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.R;
import io.agora.vlive.agora.rtm.model.PKStateMessage;
import io.agora.vlive.agora.rtm.model.SeatStateMessage;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.manager.PKServiceManager;
import io.agora.vlive.protocol.manager.ProductServiceManager;
import io.agora.vlive.protocol.manager.SeatServiceManager;
import io.agora.vlive.protocol.model.model.Product;
import io.agora.vlive.protocol.model.model.SeatInfo;
import io.agora.vlive.protocol.model.model.UserProfile;
import io.agora.vlive.protocol.model.request.AudienceListRequest;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.RoomRequest;
import io.agora.vlive.protocol.model.response.AudienceListResponse;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.protocol.model.response.ProductListResponse;
import io.agora.vlive.protocol.model.response.Response;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.protocol.model.types.PKConstant;
import io.agora.vlive.protocol.model.types.SeatInteraction;
import io.agora.vlive.ui.actionsheets.InviteUserActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomUserListActionSheet;
import io.agora.vlive.ui.actionsheets.OnlineUserInviteCallActionSheet;
import io.agora.vlive.ui.actionsheets.PkRoomListActionSheet;
import io.agora.vlive.ui.actionsheets.ProductActionSheet;
import io.agora.vlive.ui.actionsheets.toolactionsheet.AbsToolActionSheet;
import io.agora.vlive.ui.actionsheets.toolactionsheet.ECommerceToolActionSheet;
import io.agora.vlive.ui.components.CameraTextureView;
import io.agora.vlive.ui.components.PkLayout;
import io.agora.vlive.ui.components.bottomLayout.AbsBottomLayout;
import io.agora.vlive.ui.components.bottomLayout.ECommerceBottomLayout;
import io.agora.vlive.ui.components.LiveHostNameLayout;
import io.agora.vlive.ui.components.LiveMessageEditLayout;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.UserUtil;

public class ECommerceLiveActivity extends LiveRoomActivity
        implements View.OnClickListener, PkRoomListActionSheet.OnPkRoomSelectedListener,
        InviteUserActionSheet.InviteUserActionSheetListener {
    private static final String TAG = ECommerceLiveActivity.class.getSimpleName();

    private static final int PK_RESULT_DISPLAY_LAST = 2000;

    private String mOwnerName;

    private LiveHostNameLayout mNamePad;
    private FrameLayout mBigVideoLayout;
    private boolean mTopLayoutCalculated;

    private ECommerceBottomLayout mBottomLayout;

    private boolean mAudioMuted;
    private boolean mVideoMuted;
    private boolean mInEarMonitoring = false;

    private SeatServiceManager mSeatManager;
    private CallController mCallController;
    private String mCallRemoteUserId;
    private String mCallRemoteUserName;
    private int mCallRemoteRtcUid;

    private PkRoomListActionSheet mPKRoomListAction;
    private PKServiceManager mPKManager;
    private PkLayout mPkLayout;
    private boolean mIsInPkMode;
    private String mPKRoomId;
    private String mPKRoomUserName;

    private ProductServiceManager mProductManager;

    private ProductActionSheet mProductListActionSheet;
    private InviteUserActionSheet mInviteAudienceActionSheet;
    private LiveRoomUserListActionSheet mRoomUserListActionSheet;
    private OnlineUserInviteCallActionSheet mOnlineUserInviteActionSheet;

    private ProductDetailWindow mProductDetailWindow;

    private AbsBottomLayout.BottomButtonListener mBottomListener = new AbsBottomLayout.BottomButtonListener() {
        @Override
        public void onLiveBottomLayoutShowMessageEditor() {
            if (messageEditLayout != null) {
                messageEditLayout.setVisibility(View.VISIBLE);
                messageEditText.requestFocus();
                inputMethodManager.showSoftInput(messageEditText, 0);
            }
        }

        @Override
        public void onFun1ButtonClicked(int role) {
            if (isOwner) {
                if (mCallController.isCalling()) {
                    showShortToast(getString(R.string.cannot_pk_while_calling));
                } else if (mIsInPkMode) {
                    showShortToast(getString(R.string.cannot_pk_while_already_pk_with_someone));
                } else {
                    mPKRoomListAction = (PkRoomListActionSheet)
                            showActionSheetDialog(ACTION_SHEET_PK_ROOM_LIST, tabIdToLiveType(tabId),
                                    true, true, ECommerceLiveActivity.this);
                    mPKRoomListAction.setup(proxy(), config().getUserProfile().getToken(),
                            ClientProxy.ROOM_TYPE_ECOMMERCE);
                    mPKRoomListAction.requestMorePkRoom();
                }
            } else {
                showActionSheetDialog(ACTION_SHEET_GIFT, tabIdToLiveType(tabId),
                        false, true, ECommerceLiveActivity.this);
            }
        }

        @Override
        public void onFun2ButtonClicked(int role) {
            mProductListActionSheet = (ProductActionSheet)
                    showActionSheetDialog(ACTION_SHEET_PRODUCT_LIST, tabIdToLiveType(tabId),
                            isOwner, true, null);
            mProductListActionSheet.setProductManager(mProductManager);
            mProductListActionSheet.setRoomId(roomId);
            mProductListActionSheet.setRole(isOwner ?
                    Global.Constants.ROLE_OWNER : Global.Constants.ROLE_AUDIENCE);
            mProductListActionSheet.setListener(mProductActionListener);
        }

        @Override
        public void onMoreButtonClicked() {
            ECommerceToolActionSheet actionSheet = new ECommerceToolActionSheet(ECommerceLiveActivity.this);
            actionSheet.setOnToolActionSheetItemClickedListener(mToolActionSheetItemListener);
            actionSheet.setRole(roomRoleToToolActionRole());
            showCustomActionSheetDialog(true, actionSheet);
        }

        private int roomRoleToToolActionRole() {
            if (isOwner) return AbsToolActionSheet.ROLE_OWNER;
            else return AbsToolActionSheet.ROLE_AUDIENCE;
        }

        @Override
        public void onCloseButtonClicked() {
            checkBeforeLeavingRoom();
        }
    };

    private void checkBeforeLeavingRoom() {
        if (mCallController.isCalling()) {
            String title = getResources().getString(R.string.dialog_ecommerce_end_call_title);
            String message = getResources().getString(R.string.dialog_ecommerce_end_call_message_and_quit);
            message = String.format(message, mCallRemoteUserName);
            curDialog = showDialog(title, message,
                    R.string.dialog_positive_button,
                    R.string.dialog_negative_button,
                    v -> {
                        if (isOwner) {
                            mSeatManager.forceLeave(roomId, mCallRemoteUserId, 1);
                        } else {
                            mSeatManager.hostLeave(roomId, ownerId, 1);
                        }
                        closeDialog();
                        finish();
                    },
                    v -> closeDialog());
        } else if (mIsInPkMode && isOwner) {
            String message = getResources().getString(R.string.dialog_pk_force_quit_message);
            message = String.format(message, mPKRoomUserName);
            curDialog = showDialog(getResources().getString(R.string.dialog_pk_force_quit_title), message,
                    R.string.dialog_positive_button,
                    R.string.dialog_negative_button,
                    v -> leaveRoom(),
                    v -> closeDialog());
        } else {
            int messageRes = isOwner
                    ? R.string.end_live_streaming_message_owner
                    : R.string.finish_broadcast_message_audience;
            curDialog = showDialog(R.string.end_live_streaming_title_owner,
                    messageRes, ECommerceLiveActivity.this);
        }
    }

    private ProductActionSheet.OnProductActionListener mProductActionListener
            = new ProductActionSheet.OnProductActionListener() {
        @Override
        public void onProductDetail(Product product) {
            showProductDetailWindow(product);
        }

        private void showProductDetailWindow(Product product) {
            mProductDetailWindow = new ProductDetailWindow(
                    ECommerceLiveActivity.this,
                    R.style.product_detail_window, product);
            mProductDetailWindow.show();
        }

        @Override
        public void onProductListed(String productId) {
            XLog.d("onProductListed " + productId);
            mProductManager.requestChangeProductState(roomId, productId, Product.PRODUCT_LAUNCHED);
        }

        @Override
        public void onProductUnlisted(String productId) {
            XLog.d("onProductUnlisted " + productId);
            mProductManager.requestChangeProductState(roomId, productId, Product.PRODUCT_UNAVAILABLE);
        }
    };

    private OnlineUserInviteCallActionSheet.OnlineUserActionListener
            mOnlineUserInviteActionListener = new OnlineUserInviteCallActionSheet.OnlineUserActionListener() {
        @Override
        public void onUserInvited(String userId, String userName) {
            mSeatManager.invite(roomId, userId, 1);
            dismissActionSheetDialog();
        }

        @Override
        public void onUserApplicationAccepted(String userId, String userName) {
            mSeatManager.ownerAccept(roomId, userId, 1);
            dismissActionSheetDialog();
        }

        @Override
        public void onUserApplicationRejected(String userId, String userName) {
            mSeatManager.ownerReject(roomId, userId, 1);
            dismissActionSheetDialog();
        }

        @Override
        public void onUserListTabChanged(boolean showAll) {
            if (!showAll) {
                participants.showNotification(false);
            }
        }
    };

    @Override
    public void onProductStateChangedResponse(String productId, int state, boolean success) {
        runOnUiThread(() -> {
            if (state == Product.PRODUCT_LAUNCHED && success) {
                showShortToast(getString(R.string.product_list_success));
            } else if (state == Product.PRODUCT_UNAVAILABLE && success) {
                showShortToast(getString(R.string.product_unlist_success));
            }

            mProductManager.requestProductList(roomId);
        });
    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {
        super.onRoomListResponse(response);
        if (mPKRoomListAction != null && mPKRoomListAction.isShown()) {
            runOnUiThread(() -> mPKRoomListAction.appendUsers(response.data));
        }
    }

    private AbsToolActionSheet.OnToolActionSheetItemClickedListener
            mToolActionSheetItemListener = new AbsToolActionSheet.OnToolActionSheetItemClickedListener() {
        public void onToolActionSheetItemClicked(int position, View view) {
            switch (position) {
                case 0:
                    if (isOwner) {
                        onActionSheetSettingClicked();
                    } else {
                        if (mCallController.isCalling()) {
                            showShortToast(getString(R.string.dialog_ecommerce_duplicate_call_request));
                        } else {
                            requestToConnect();
                        }
                        dismissActionSheetDialog();
                    }
                    break;
                case 1:
                    onActionSheetRealDataClicked();
                    break;
                case 2:
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, tabIdToLiveType(tabId),
                            true, true, ECommerceLiveActivity.this);
                    break;
                case 3:
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, tabIdToLiveType(tabId),
                            true, true, ECommerceLiveActivity.this);
                    break;
                case 4:
                    onActionSheetRotateClicked();
                    break;
                case 5:
                    mVideoMuted = !mVideoMuted;
                    rtcEngine().muteLocalVideoStream(mVideoMuted);
                    config().setVideoMuted(mVideoMuted);
                    break;
                case 6:
                    mAudioMuted = !mAudioMuted;
                    rtcEngine().muteLocalAudioStream(mAudioMuted);
                    config().setAudioMuted(mAudioMuted);
                    break;
                case 7:
                    if (onActionSheetEarMonitoringClicked(!mInEarMonitoring)) {
                        mInEarMonitoring = !mInEarMonitoring;
                    }
                    break;
            }
        }

        public void onToolActionSheetItemViewBind(int position, View view) {
            switch (position) {
                case 5:
                    view.setActivated(!config().isVideoMuted());
                    break;
                case 6:
                    view.setActivated(!config().isAudioMuted());
                    break;
                case 7:
                    view.setActivated(mInEarMonitoring);
                    break;
            }
        }
    };

    private void requestToConnect() {
        curDialog = showDialog(R.string.live_room_host_in_audience_apply_title,
                R.string.live_room_host_in_audience_apply_message,
                v -> {
                    mSeatManager.apply(roomId, ownerId, 1);
                    closeDialog();
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(false);
    }

    @Override
    protected void onPermissionGranted() {
        initUI();
        mSeatManager = new SeatServiceManager(application());
        mPKManager = new PKServiceManager(application());
        mProductManager = new ProductServiceManager(application());
        super.onPermissionGranted();
    }

    private void initUI() {
        setContentView(R.layout.activity_ecommerce);

        mNamePad = findViewById(R.id.ecommerce_name_pad);
        mNamePad.init();

        participants = findViewById(R.id.ecommerce_participant);
        participants.init();
        participants.setUserLayoutListener(this);

        mBottomLayout = findViewById(R.id.ecommerce_bottom_layout);
        mBottomLayout.setBottomLayoutListener(mBottomListener);

        mBigVideoLayout = findViewById(R.id.ecommerce_big_video_layout);

        if (isOwner) {
            mAudioMuted = false;
            mVideoMuted = false;
            becomesOwner(false, false);
        }

        messageList = findViewById(R.id.message_list);
        messageList.init();
        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        rtcStatsView = findViewById(R.id.single_host_rtc_stats);
        rtcStatsView.setCloseListener(view -> rtcStatsView.setVisibility(View.GONE));

        // In case that the UI is not relocated because
        // the permission request dialog consumes the chance
        onGlobalLayoutCompleted();

        mCallController = new CallController(findViewById(R.id.remote_call_layout), this);

        mPkLayout = findViewById(R.id.pk_host_layout);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.ecommerce_participant_layout);
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
        super.onEnterRoomResponse(response);
        if (response.code == Response.SUCCESS) {
            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;
            mOwnerName = response.data.room.owner.userName;

            // Determine if I am the owner of a host here because
            // I may leave the room unexpectedly and come once more.
            String myId = config().getUserProfile().getUserId();
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
            }

            if (isOwner) {
                mAudioMuted = response.data.room.owner.enableAudio !=
                        SeatInfo.User.USER_AUDIO_ENABLE;
                mVideoMuted = response.data.room.owner.enableVideo !=
                        SeatInfo.User.USER_VIDEO_ENABLE;
            }

            mIsInPkMode = response.data.room.pk.state == PKConstant.PK_STATE_PK;
            if (mIsInPkMode) mPKRoomId = response.data.room.pk.remoteRoom.roomId;

            // Check if someone is the host
            boolean callConnected = false;
            boolean iamHost = false;
            List<SeatInfo> seatListInfo = response.data.room.coVideoSeats;
            if (seatListInfo.size() > 0) {
                SeatInfo info = seatListInfo.get(0);
                if (info.seat.state == SeatInfo.TAKEN) {
                    callConnected = true;
                    mCallRemoteUserId = info.user.userId;
                    mCallRemoteRtcUid = info.user.uid;
                    mCallRemoteUserName = info.user.userName;
                    iamHost = myId.equals(info.user.userId);

                    if (iamHost) {
                        mAudioMuted = info.user.enableAudio !=
                                SeatInfo.User.USER_AUDIO_ENABLE;
                        mVideoMuted = info.user.enableVideo !=
                                SeatInfo.User.USER_VIDEO_ENABLE;
                    }
                }
            }

            final boolean inCall = callConnected;
            final boolean callHost = iamHost;

            if (!isOwner && !callHost) {
                mAudioMuted = true;
                mVideoMuted = true;
            }

            runOnUiThread(() -> {
                if (mIsInPkMode) {
                    mBottomLayout.setRole(bottomLayoutRole());
                    setupUIMode(true, isOwner);
                    setupPkBehavior(isOwner, response.data.room.pk.countDown,
                            response.data.room.pk.remoteRoom.owner.userName,
                            response.data.room.pk.relayConfig);
                    updatePkGiftRank(response.data.room.pk.localRank,
                            response.data.room.pk.remoteRank);
                }  else {
                    if (isOwner) {
                        becomesOwner(mAudioMuted, mVideoMuted);
                    } else {
                        becomeAudience();
                    }

                    if (inCall && !mCallController.isCalling()) {
                        if (isOwner || callHost) {
                            mCallController.startCall(mCallRemoteRtcUid);
                        } else {
                            mCallController.viewOtherAudienceCall(mCallRemoteRtcUid);
                        }
                    }
                }

                mNamePad.setName(response.data.room.owner.userName);
                mNamePad.setIcon(UserUtil.getUserRoundIcon(getResources(),
                        response.data.room.owner.userId));
            });
        }
    }

    private void becomesOwner(boolean audioMuted, boolean videoMuted) {
        if (!videoMuted) startCameraCapture();
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mBottomLayout.setRole(bottomLayoutRole());
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        initLocalPreview();
    }

    private void initLocalPreview() {
        CameraTextureView textureView = new CameraTextureView(this);
        mBigVideoLayout.addView(textureView);
    }

    private void becomeAudience() {
        isHost = false;
        stopCameraCapture();
        mBottomLayout.setRole(bottomLayoutRole());
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        config().setAudioMuted(true);
        config().setVideoMuted(true);
        setupRemotePreview();
    }

    private void setupRemotePreview() {
        SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
        mBigVideoLayout.addView(surfaceView);
    }

    private int bottomLayoutRole() {
        if (isOwner) return AbsBottomLayout.ROLE_OWNER;
        else if (isHost) return AbsBottomLayout.ROLE_HOST;
        else return AbsBottomLayout.ROLE_AUDIENCE;
    }

    @Override
    public void onBackPressed() {
        checkBeforeLeavingRoom();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_positive_button:
                leaveRoom();
                finish();
                break;
            case R.id.remote_call_close_btn:
                if (mCallController.isCalling()) {
                    String message = getResources().getString(R.string.dialog_ecommerce_end_call_message);
                    if (isOwner) {
                        message = String.format(message, mCallRemoteUserName);
                        curDialog = showDialog(getResources().getString(R.string.dialog_ecommerce_end_call_title), message,
                                R.string.dialog_positive_button,
                                R.string.dialog_negative_button,
                                v -> {
                                    mSeatManager.forceLeave(roomId, mCallRemoteUserId, 1);
                                    closeDialog();
                                },
                                v -> closeDialog());
                    } else {
                        message = String.format(message, mOwnerName);
                        curDialog = showDialog(getResources().getString(R.string.dialog_ecommerce_end_call_title), message,
                                R.string.dialog_positive_button,
                                R.string.dialog_negative_button,
                                v -> {
                                    mSeatManager.hostLeave(roomId, ownerId, 1);
                                    closeDialog();
                                },
                                v -> closeDialog());
                    }
                }
                break;
        }
    }

    @Override
    public void onPkRoomListActionSheetRoomSelected(int position, String roomId, int uid) {
        mPKRoomId = roomId;
        mPKManager.invitePK(this.roomId, mPKRoomId);
        dismissActionSheetDialog();
    }

    @Override
    public void onUserLayoutShowUserList(View view) {
        // Show invite user list
        if (isOwner) {
            mOnlineUserInviteActionSheet = (OnlineUserInviteCallActionSheet)
                    showActionSheetDialog(ACTION_SHEET_PRODUCT_INVITE_ONLINE_SHOP,
                            tabIdToLiveType(tabId), isHost, true, this);
            mOnlineUserInviteActionSheet.setup(proxy(), this, roomId, config().getUserProfile().getToken());
            mOnlineUserInviteActionSheet.setSeatManager(mSeatManager);
            mOnlineUserInviteActionSheet.setOnlineUserListener(mOnlineUserInviteActionListener);
            mOnlineUserInviteActionSheet.setOwnerUserId(ownerId);
            mOnlineUserInviteActionSheet.showNotification(participants.notificationShown());
            requestAudienceList();
        } else {
            mRoomUserListActionSheet = (LiveRoomUserListActionSheet)
                    showActionSheetDialog(ACTION_SHEET_ROOM_USER, tabIdToLiveType(tabId), isHost, true, this);
            mRoomUserListActionSheet.setup(proxy(), this, roomId, config().getUserProfile().getToken());
            mRoomUserListActionSheet.requestMoreAudience();
        }
    }

    private void requestAudienceList() {
        sendRequest(Request.AUDIENCE_LIST, new AudienceListRequest(
                config().getUserProfile().getToken(),
                roomId, null, AudienceListRequest.TYPE_ALL));
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        if (mInviteAudienceActionSheet != null &&
                mInviteAudienceActionSheet.isShown()) {
            runOnUiThread(() -> mInviteAudienceActionSheet.append(response.data.list));
        } else {
            List<UserProfile> userList = new ArrayList<>();
            for (AudienceListResponse.AudienceInfo info : response.data.list) {
                UserProfile profile = new UserProfile();
                profile.setUserId(info.userId);
                profile.setUserName(info.userName);
                profile.setAvatar(info.avatar);
                userList.add(profile);
            }

            if (isOwner && onlineUserActionSheetIsShown()) {
                runOnUiThread(() -> mOnlineUserInviteActionSheet.append(userList));
            } else if (mRoomUserListActionSheet != null &&
                    mRoomUserListActionSheet.isShown()) {
                runOnUiThread(() -> mRoomUserListActionSheet.appendUsers(userList));
            }
        }
    }

    private boolean onlineUserActionSheetIsShown() {
        return mOnlineUserInviteActionSheet != null &&
                mOnlineUserInviteActionSheet.isShown();
    }

    private void refreshOnlineUserState() {
        if (onlineUserActionSheetIsShown()) {
            runOnUiThread(() -> mOnlineUserInviteActionSheet.notifyDataSetChanged());
        }
    }

    @Override
    public void onActionSheetAudienceInvited(int seatId, String userId, String userName) {
        if (mIsInPkMode) {
            showShortToast(getString(R.string.cannot_call_while_pk_with_someone));
            return;
        }

        mSeatManager.invite(roomId, userId, seatId);
        if (mInviteAudienceActionSheet != null &&
                mInviteAudienceActionSheet.isShown()) {
            dismissActionSheetDialog();
        }
    }

    @Override
    public void onSeatInteractionResponse(long processId, String userId, int seatNo, int type) {
        Log.i(TAG, "SeatInteractionResponse " + type);
        // Tell seat manager that the response has been sent out
        if (type == SeatInteraction.OWNER_INVITE) {
            mSeatManager.addToInvitingList(userId, processId);
        } else if (type == SeatInteraction.OWNER_ACCEPT ||
                type == SeatInteraction.OWNER_REJECT) {
            mSeatManager.removeFromApplicationList(userId);
        }

        refreshOnlineUserState();
    }

    @Override
    public void onRtmSeatInvited(String userId, String userName, int index) {
        if (isOwner) return;
        String title = getResources().getString(R.string.live_room_host_in_invite_user_list_action_sheet_title);
        String message = getResources().getString(R.string.live_room_host_in_invited_by_owner_simple);
        message = String.format(message, userName);
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    mSeatManager.audienceAccept(roomId, userId, index);
                    curDialog.dismiss();
                },
                view -> {
                    mSeatManager.audienceReject(roomId, userId, index);
                    curDialog.dismiss();
                });
    }

    @Override
    public void onRtmSeatApplied(String userId, String userName, int seatId) {
        if (!isOwner) return;
        mSeatManager.addToApplicationList(userId, userName);
        refreshOnlineUserState();

        if (onlineUserActionSheetIsShown()) {
            participants.showNotification(mOnlineUserInviteActionSheet.checkIfShowNotification());
        } else {
            participants.showNotification(true);
        }

    }

    @Override
    public void onRtmApplicationAccepted(long processId, String userId, String userName, int index) {
        showShortToast(getResources().getString(R.string.apply_seat_success));
    }

    @Override
    public void onRtmInvitationAccepted(long processId, String userId, String userName, int index) {
        showShortToast(getResources().getString(R.string.invite_success));
        mSeatManager.removeFromInvitingList(userId);
        refreshOnlineUserState();
    }

    @Override
    public void onRtmApplicationRejected(long processId, String userId, String nickname, int index) {
        String title = getResources().getString(R.string.live_room_host_in_apply_rejected);
        String message = getResources().getString(R.string.live_room_host_in_apply_rejected_message);
        message = String.format(message, nickname);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
    }

    @Override
    public void onRtmInvitationRejected(long processId, String userId, String nickname, int index) {
        String title = getResources().getString(R.string.live_room_host_in_invite_rejected);
        String message = getResources().getString(R.string.live_room_host_in_invite_rejected_message);
        message = String.format(message, nickname);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
        mSeatManager.removeFromInvitingList(userId);
        refreshOnlineUserState();
    }

    @Override
    public void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> list) {
        if (list != null && list.size() >= 1) {
            SeatStateMessage.SeatStateMessageDataItem item = list.get(0);
            if (item.seat.no == 1) {
                if (item.seat.state == SeatInfo.TAKEN &&
                        !mCallController.isCalling()) {
                    mCallRemoteUserId = item.user.userId;
                    mCallRemoteRtcUid = item.user.uid;
                    mCallRemoteUserName = item.user.userName;
                    if (config().getUserProfile().getUserId()
                            .equals(mCallRemoteUserId) || isOwner) {
                        mCallController.startCall(mCallRemoteRtcUid);
                    } else {
                        mCallController.viewOtherAudienceCall(mCallRemoteRtcUid);
                    }
                } else if (item.seat.state != SeatInfo.TAKEN &&
                        mCallController.isCalling()) {
                    mCallController.endCall(mCallRemoteRtcUid);
                }
            }
        }
    }

    @Override
    public void onGetProductListResponse(ProductListResponse response) {
        runOnUiThread(() -> {
            if (mProductListActionSheet != null
                    && mProductListActionSheet.isShown()) {
                mProductListActionSheet.updateList(response.data);
            }
        });
    }

    private class CallController {
        private RelativeLayout mLayout;
        private RelativeLayout mVideoLayout;
        private AppCompatImageView mCloseBtn;
        private AppCompatTextView mNameTextView;
        private boolean mIsCalling;

        CallController(RelativeLayout baseLayout, View.OnClickListener closeListener) {
            mLayout = baseLayout;
            mVideoLayout = findViewById(R.id.remote_call_video_layout);
            mCloseBtn = findViewById(R.id.remote_call_close_btn);
            mCloseBtn.setOnClickListener(closeListener);
            mNameTextView = findViewById(R.id.remote_call_peer_name);
        }

        void startCall(int rtcUid) {
            mLayout.setVisibility(View.VISIBLE);
            mCloseBtn.setVisibility(View.VISIBLE);
            if (isOwner) {
                SurfaceView surfaceView = setupRemoteVideo(rtcUid);
                surfaceView.setZOrderMediaOverlay(false);
                mVideoLayout.addView(surfaceView);
                mNameTextView.setText(mCallRemoteUserName);
            } else {
                CameraTextureView textureView = new
                        CameraTextureView(ECommerceLiveActivity.this);
                mVideoLayout.addView(textureView);
                startCameraCapture();
                rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                mNameTextView.setText(mOwnerName);
            }

            mIsCalling = true;
            messageList.setNarrow(true);
        }

        void endCall(int rtcUid) {
            if (isOwner) {
                removeRemoteVideo(rtcUid);
            } else {
                stopCameraCapture();
                rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
            }

            mVideoLayout.removeAllViews();
            mLayout.setVisibility(View.GONE);
            messageList.setNarrow(false);
            mIsCalling = false;
        }

        void viewOtherAudienceCall(int rtcUid) {
            mCloseBtn.setVisibility(View.GONE);
            mLayout.setVisibility(View.VISIBLE);
            mIsCalling = true;
            SurfaceView surfaceView = setupRemoteVideo(rtcUid);
            surfaceView.setZOrderMediaOverlay(true);
            mVideoLayout.removeAllViews();
            mVideoLayout.addView(surfaceView);
            messageList.setNarrow(true);
        }

        public boolean isCalling() {
            return mIsCalling;
        }
    }

    @Override
    public void onRtmProductPurchased(String productId, int count) {
        mProductManager.requestProductList(roomId);
    }

    @Override
    public void onRtmProductStateChanged(String productId, int state) {
        if (!isOwner && state == Product.PRODUCT_LAUNCHED) {
            if (!isCurDialogShowing() && !actionSheetShowing()) {
                showShortToast("有新商品上架");
            }
        }
    }

    @Override
    public void onProductPurchasedResponse(boolean success) {
        int toastRes = success ? R.string.product_purchase_success : R.string.product_purchase_fail;
        runOnUiThread(() -> showShortToast(getString(toastRes)));
    }

    @Override
    public void onRtmPkReceivedFromAnotherHost(String userId, String userName, String pkRoomId) {
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
                mIsInPkMode = true;
                mPKRoomId = messageData.remoteRoom.roomId;
                mPKRoomUserName = messageData.remoteRoom.owner.userName;
                setupUIMode(true, isOwner);
                setupPkBehavior(isOwner,
                        messageData.countDown, mPKRoomUserName,
                        messageData.relayConfig);
                startMediaRelay(messageData.relayConfig);
                updatePkGiftRank(messageData.localRank, messageData.remoteRank);
            } else if (messageData.event == PKConstant.PK_EVENT_RANK_CHANGED) {
                updatePkGiftRank(messageData.localRank, messageData.remoteRank);
            } else if (messageData.event == PKConstant.PK_EVENT_END) {
                mPkLayout.setResult(messageData.result);
                new Handler(getMainLooper()).postDelayed(() -> stopPkMode(isOwner), PK_RESULT_DISPLAY_LAST);
                mIsInPkMode = false;
                showShortToast(getResources().getString(R.string.pk_ends));
            }
        });
    }

    private void updatePkGiftRank(int mine, int other) {
        if (mIsInPkMode && mPkLayout.getVisibility() == View.VISIBLE) {
            mPkLayout.setPoints(mine, other);
        }
    }

    private void setupUIMode(boolean isPkMode, boolean isOwner) {
        if (isPkMode) {
            mBigVideoLayout.setVisibility(View.GONE);
            mPkLayout.removeResult();
            mPkLayout.setVisibility(View.VISIBLE);
            mPkLayout.setHost(isOwner);
        } else {
            mPkLayout.removeResult();
            mPkLayout.getLeftVideoLayout().removeAllViews();
            mPkLayout.getRightVideoLayout().removeAllViews();
            mPkLayout.setVisibility(View.GONE);
            mBigVideoLayout.setVisibility(View.VISIBLE);
        }
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

        rtcEngine().muteLocalAudioStream(mAudioMuted);
        rtcEngine().muteLocalVideoStream(mVideoMuted);
        config().setAudioMuted(mAudioMuted);
        config().setVideoMuted(mVideoMuted);
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

    private void startMediaRelay(EnterRoomResponse.RelayConfig config) {
        ChannelMediaRelayConfiguration relayConfig = new ChannelMediaRelayConfiguration();
        relayConfig.setSrcChannelInfo(toChannelMediaInfo(config.local));
        relayConfig.setDestChannelInfo(config.proxy.channelName, toChannelMediaInfo(config.proxy));
        rtcEngine().startChannelMediaRelay(relayConfig);
    }

    private ChannelMediaInfo toChannelMediaInfo(EnterRoomResponse.RelayInfo proxy) {
        return new ChannelMediaInfo(proxy.channelName, proxy.token, proxy.uid);
    }

    private void stopPkMode(boolean isOwner) {
        rtcEngine().stopChannelMediaRelay();
        setupUIMode(false, isOwner);
        setupSingleBroadcastBehavior(isOwner,
                config().isAudioMuted(),
                config().isVideoMuted());
    }

    private void setupSingleBroadcastBehavior(boolean isOwner, boolean audioMuted, boolean videoMuted) {
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER
                : Constants.CLIENT_ROLE_AUDIENCE;
        rtcEngine().setClientRole(myRtcRole);

        if (isOwner) {
            startCameraCapture();
            CameraTextureView cameraTextureView = new CameraTextureView(this);
            mBigVideoLayout.addView(cameraTextureView);
        } else {
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            mBigVideoLayout.removeAllViews();
            mBigVideoLayout.addView(surfaceView);
        }

        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
        rtcEngine().muteLocalAudioStream(audioMuted);
        rtcEngine().muteLocalVideoStream(videoMuted);
    }

    private class ProductDetailWindow extends Dialog {
        private Product mProduct;
        private RelativeLayout mVideo;
        private RelativeLayout mOwnerVideoLayout;
        private int mPictureRes;

        public ProductDetailWindow(@NonNull Context context, int styleRes,
                                   Product product) {
            super(context, styleRes);
            mProduct = product;
            mPictureRes = productIdToPictureResource(mProduct.productId);
        }

        @Override
        public void show() {
            setContentView(R.layout.product_detail_layout);
            hideStatusBar(getWindow(), true);
            setCancelable(true);
            AppCompatTextView buyBtn = findViewById(R.id.product_buy_now_btn);
            buyBtn.setOnClickListener(view -> {
                if (mProductManager != null) {
                    mProductManager.requestPurchaseProduct(roomId, mProduct.productId, 1);
                }
                dismiss();
            });

            findViewById(R.id.product_detail_back).setOnClickListener(view -> dismissProductDetailWindow());
            findViewById(R.id.product_detail_video_close_btn).setOnClickListener(
                    view -> {
                        if (mOwnerVideoLayout != null) {
                            ViewGroup parent = (ViewGroup) mOwnerVideoLayout.getParent();
                            parent.removeView(mOwnerVideoLayout);
                        }
                    });

            AppCompatTextView productDescription = findViewById(R.id.product_window_description_text);
            productDescription.setText(parseDescription(mProduct.productId));

            AppCompatImageView pictureImageView = findViewById(R.id.product_detail_big_picture);
            pictureImageView.setImageResource(mPictureRes);

            mOwnerVideoLayout = findViewById(R.id.product_detail_owner_video_layout);
            mOwnerVideoLayout.setVisibility(View.VISIBLE);

            mVideo = findViewById(R.id.owner_video);
            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            mVideo.removeAllViews();
            mVideo.addView(surfaceView);

            if (mIsInPkMode) {
                mPkLayout.getLeftVideoLayout().removeAllViews();
            } else {
                mBigVideoLayout.removeAllViews();
            }

            super.show();
        }

        private int parseDescription(String productId) {
            switch(productId) {
                case "2": return R.string.product_desp_2;
                case "3": return R.string.product_desp_3;
                case "4": return R.string.product_desp_4;
                default: return R.string.product_desp_1;
            }
        }

        @Override
        public void dismiss() {
            if (mOwnerVideoLayout != null) {
                mOwnerVideoLayout.removeAllViews();
            }

            SurfaceView surfaceView = setupRemoteVideo(ownerRtcUid);
            if (mIsInPkMode) {
                mPkLayout.getLeftVideoLayout().addView(surfaceView);
            } else {
                mBigVideoLayout.addView(surfaceView);
            }

            super.dismiss();
        }

        int productIdToPictureResource(String id) {
            switch (id) {
                case "1": return R.drawable.product_picture_1;
                case "2": return R.drawable.product_picture_2;
                case "3": return R.drawable.product_picture_3;
                case "4":
                default: return R.drawable.product_picture_4;
            }
        }
    }

    @Override
    public void onRtmLeaveMessage() {
        runOnUiThread(() -> {
            dismissProductDetailWindow();
            leaveRoom();
        });
    }

    private void dismissProductDetailWindow() {
        Log.d(TAG, "dismiss product");
        if (mProductDetailWindow != null &&
                mProductDetailWindow.isShowing()) {
            mProductDetailWindow.dismiss();
        }
    }
}
