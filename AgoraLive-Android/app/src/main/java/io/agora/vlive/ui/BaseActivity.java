package io.agora.vlive.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Stack;

import io.agora.capture.video.camera.CameraManager;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmClient;
import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.Config;
import io.agora.vlive.agora.rtc.RtcEventHandler;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.ClientProxyListener;
import io.agora.vlive.protocol.model.response.AppVersionResponse;
import io.agora.vlive.protocol.model.response.AudienceListResponse;
import io.agora.vlive.protocol.model.response.CreateRoomResponse;
import io.agora.vlive.protocol.model.response.CreateUserResponse;
import io.agora.vlive.protocol.model.response.EditUserResponse;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.protocol.model.response.GiftListResponse;
import io.agora.vlive.protocol.model.response.GiftRankResponse;
import io.agora.vlive.protocol.model.response.LeaveRoomResponse;
import io.agora.vlive.protocol.model.response.LoginResponse;
import io.agora.vlive.protocol.model.response.ModifyUserStateResponse;
import io.agora.vlive.protocol.model.response.MusicListResponse;
import io.agora.vlive.protocol.model.response.OssPolicyResponse;
import io.agora.vlive.protocol.model.response.ProductListResponse;
import io.agora.vlive.protocol.model.response.RefreshTokenResponse;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.protocol.model.response.SeatStateResponse;
import io.agora.vlive.protocol.model.response.SendGiftResponse;
import io.agora.vlive.ui.actionsheets.AbstractActionSheet;
import io.agora.vlive.ui.actionsheets.GiftActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomUserListActionSheet;
import io.agora.vlive.ui.actionsheets.OnlineUserInviteCallActionSheet;
import io.agora.vlive.ui.actionsheets.ProductActionSheet;
import io.agora.vlive.ui.actionsheets.toolactionsheet.LiveRoomToolActionSheet;
import io.agora.vlive.ui.actionsheets.InviteUserActionSheet;
import io.agora.vlive.ui.actionsheets.PkRoomListActionSheet;
import io.agora.vlive.ui.actionsheets.VoiceActionSheet;
import io.agora.vlive.R;
import io.agora.vlive.ui.actionsheets.BackgroundMusicActionSheet;
import io.agora.vlive.ui.actionsheets.BeautySettingActionSheet;
import io.agora.vlive.ui.actionsheets.LiveRoomSettingActionSheet;

/**
 * Capabilities that are shared by all activity, such as
 * messaging, action sheets, dialogs, server requests and so on.
 */
public abstract class BaseActivity extends AppCompatActivity implements ClientProxyListener {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int ACTION_SHEET_VIDEO = 0;
    protected static final int ACTION_SHEET_BEAUTY = 1;
    protected static final int ACTION_SHEET_BG_MUSIC = 2;
    protected static final int ACTION_SHEET_GIFT = 3;
    protected static final int ACTION_SHEET_TOOL = 4;
    protected static final int ACTION_SHEET_VOICE = 5;
    protected static final int ACTION_SHEET_INVITE_AUDIENCE = 6;
    protected static final int ACTION_SHEET_ROOM_USER = 7;
    protected static final int ACTION_SHEET_PK_ROOM_LIST = 8;
    protected static final int ACTION_SHEET_PRODUCT_LIST = 9;
    protected static final int ACTION_SHEET_PRODUCT_INVITE_ONLINE_SHOP = 10;

    private static final int ACTION_SHEET_DIALOG_STYLE_RES = R.style.live_room_dialog;
    private static final int TOAST_SHORT_INTERVAL = 2000;

    protected int systemBarHeight;
    protected int displayHeight;
    protected int displayWidth;

    private Stack<AbstractActionSheet> mActionSheetStack = new Stack<>();
    private BottomSheetDialog mSheetDialog;
    private long mLastToastTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setGlobalLayoutListener();
        systemBarHeight = getStatusBarHeight();
        getDisplaySize();
    }

    private void setGlobalLayoutListener() {
        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                onGlobalLayoutCompleted();
            }
        });
    }

    /**
     * Give a chance to obtain view layout attributes when the
     * content view layout process is completed.
     * Some layout attributes will be available here but not
     * in onCreate(), like measured width/height.
     * This callback will be called ONLY ONCE before the whole
     * window content is ready to be displayed for first time.
     */
    protected void onGlobalLayoutCompleted() {

    }

    protected void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    protected void hideStatusBar(boolean darkText) {
        hideStatusBar(getWindow(), darkText);
    }

    private int getStatusBarHeight() {
        int id = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : id;
    }

    protected void keepScreenOn(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void getDisplaySize() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        displayWidth = metric.widthPixels;
        displayHeight = metric.heightPixels;
    }

    protected void showActionSheetDialog(final AbstractActionSheet sheet) {
        dismissActionSheetDialog();

        mSheetDialog = new BottomSheetDialog(this, ACTION_SHEET_DIALOG_STYLE_RES);
        mSheetDialog.setCanceledOnTouchOutside(true);
        mSheetDialog.setContentView(sheet);
        hideStatusBar(mSheetDialog.getWindow(), false);

        mSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mActionSheetStack.isEmpty()) {
                    // Happens only in case of errors.
                    return;
                }

                if (sheet != mActionSheetStack.peek()) {
                    // When this action sheet is not at the top of
                    // stack, it means that a new action sheet
                    // is about to be shown and it needs a fallback
                    // history, and this sheet needs to be retained.
                    return;
                }

                // At this moment, we want to fallback to
                // the previous action sheet if exists.
                mActionSheetStack.pop();
                if (!mActionSheetStack.isEmpty()) {
                    AbstractActionSheet sheet = mActionSheetStack.peek();
                    ((ViewGroup) sheet.getParent()).removeAllViews();
                    showActionSheetDialog(mActionSheetStack.peek());
                }
            }
        });

        mSheetDialog.show();
    }

    protected AbstractActionSheet showActionSheetDialog(int sheetType, int liveType, boolean isHost, boolean newStack,
                                         AbstractActionSheet.AbsActionSheetListener listener) {
        AbstractActionSheet actionSheet;
        switch (sheetType) {
            case ACTION_SHEET_BEAUTY:
                actionSheet = new BeautySettingActionSheet(this);
                break;
            case ACTION_SHEET_BG_MUSIC:
                actionSheet = new BackgroundMusicActionSheet(this);
                break;
            case ACTION_SHEET_GIFT:
                actionSheet = new GiftActionSheet(this);
                break;
            case ACTION_SHEET_TOOL:
                actionSheet = new LiveRoomToolActionSheet(this);
                ((LiveRoomToolActionSheet) actionSheet).setHost(isHost);
                break;
            case ACTION_SHEET_VOICE:
                actionSheet = new VoiceActionSheet(this);
                break;
            case ACTION_SHEET_INVITE_AUDIENCE:
                actionSheet = new InviteUserActionSheet(this);
                break;
            case ACTION_SHEET_ROOM_USER:
                actionSheet = new LiveRoomUserListActionSheet(this);
                break;
            case ACTION_SHEET_PK_ROOM_LIST:
                actionSheet = new PkRoomListActionSheet(this);
                break;
            case ACTION_SHEET_PRODUCT_LIST:
                actionSheet = new ProductActionSheet(this);
                break;
            case ACTION_SHEET_PRODUCT_INVITE_ONLINE_SHOP:
                actionSheet = new OnlineUserInviteCallActionSheet(this);
                break;
            default:
                actionSheet = new LiveRoomSettingActionSheet(this);
                ((LiveRoomSettingActionSheet) actionSheet).setFallback(!newStack);
                ((LiveRoomSettingActionSheet) actionSheet).setLiveType(liveType);
        }

        actionSheet.setActionSheetListener(listener);
        if (newStack) mActionSheetStack.clear();
        mActionSheetStack.push(actionSheet);
        showActionSheetDialog(actionSheet);
        return actionSheet;
    }

    protected void showCustomActionSheetDialog(boolean newStack, AbstractActionSheet sheet) {
        if (newStack) mActionSheetStack.clear();
        mActionSheetStack.push(sheet);
        showActionSheetDialog(sheet);
    }

    protected void dismissActionSheetDialog() {
        if (mSheetDialog != null && mSheetDialog.isShowing()) {
            mSheetDialog.dismiss();
        }
    }

    protected boolean actionSheetShowing() {
        return mSheetDialog != null && mSheetDialog.isShowing();
    }

    protected Dialog showDialog(int title, int message,
                              final View.OnClickListener positiveClickListener) {
        final Dialog dialog = new Dialog(this,
                R.style.live_room_dialog_center_in_window);
        dialog.setContentView(R.layout.live_room_dialog);
        AppCompatTextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(title);
        AppCompatTextView msgTextView = dialog.findViewById(R.id.dialog_message);
        msgTextView.setText(message);
        dialog.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(positiveClickListener);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
        return dialog;
    }

    protected Dialog showDialog(int title, int message,
                                int positiveText, int negativeText,
                                final View.OnClickListener positiveClickListener,
                                final View.OnClickListener negativeClickListener) {
        String titleStr = getResources().getString(title);
        String messageStr = getResources().getString(message);
        return showDialog(titleStr, messageStr, positiveText, negativeText,
                positiveClickListener, negativeClickListener);
    }

    protected Dialog showDialog(String title, String message,
                                int positiveText, int negativeText,
                                final View.OnClickListener positiveClickListener,
                                final View.OnClickListener negativeClickListener) {
        final Dialog dialog = new Dialog(this,
                R.style.live_room_dialog_center_in_window);
        dialog.setContentView(R.layout.live_room_dialog);

        AppCompatTextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(title);

        AppCompatTextView msgTextView = dialog.findViewById(R.id.dialog_message);
        msgTextView.setText(message);

        AppCompatTextView negativeButton = dialog.findViewById(R.id.dialog_negative_button);
        negativeButton.setText(negativeText);
        negativeButton.setOnClickListener(negativeClickListener);

        AppCompatTextView positiveButton = dialog.findViewById(R.id.dialog_positive_button);
        positiveButton.setText(positiveText);
        positiveButton.setOnClickListener(positiveClickListener);

        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
        return dialog;
    }

    protected Dialog showSingleButtonConfirmDialog(String title, String message,
                                                   final View.OnClickListener listener) {
        final Dialog dialog = new Dialog(this,
                R.style.live_room_dialog_center_in_window);
        dialog.setContentView(R.layout.live_room_dialog_single_button);
        AppCompatTextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(title);
        AppCompatTextView msgTextView = dialog.findViewById(R.id.dialog_message);
        msgTextView.setText(message);
        dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(listener);
        hideStatusBar(dialog.getWindow(), false);
        dialog.show();
        return dialog;
    }

    protected Dialog showSingleButtonConfirmDialog(int title, int message,
                                                   final View.OnClickListener listener) {
        String titleStr = getResources().getString(title);
        String messageStr = getResources().getString(message);
        return showSingleButtonConfirmDialog(titleStr, messageStr, listener);
    }

    public AgoraLiveApplication application() {
        return (AgoraLiveApplication)  getApplication();
    }

    public Config config() {
        return application().config();
    }

    public SharedPreferences preferences() {
        return application().preferences();
    }

    public ClientProxy proxy() {
        return application().proxy();
    }

    public RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    public RtmClient rtmClient() {
        return application().rtmClient();
    }

    public long sendRequest(int req, Object params) {
        return proxy().sendRequest(req, params);
    }

    public void registerRtcHandler(RtcEventHandler handler) {
        application().registerRtcHandler(handler);
    }

    public void removeRtcHandler(RtcEventHandler handler) {
        application().removeRtcHandler(handler);
    }

    protected void showShortToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    protected void showLongToast(String message) {
        showToast(message, Toast.LENGTH_LONG);
    }

    protected CameraManager cameraVideoManager() {
        return application().cameraVideoManager();
    }

    protected void showToast(String message, int length) {
        long current = System.currentTimeMillis();
        if (current - mLastToastTime > TOAST_SHORT_INTERVAL) {
            // avoid showing the toast too frequently
            Toast.makeText(this, message, length).show();
            mLastToastTime = current;
        }
    }

    public String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        proxy().registerProxyListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        proxy().removeProxyListener(this);
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {

    }

    @Override
    public void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse) {

    }

    @Override
    public void onOssPolicyResponse(OssPolicyResponse response) {

    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {

    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {

    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {

    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {

    }

    @Override
    public void onLoginResponse(LoginResponse response) {

    }

    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {

    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {

    }

    @Override
    public void onLeaveRoomResponse(LeaveRoomResponse response) {

    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {

    }

    @Override
    public void onRequestSeatStateResponse(SeatStateResponse response) {

    }

    @Override
    public void onModifyUserStateResponse(ModifyUserStateResponse response) {

    }

    @Override
    public void onSendGiftResponse(SendGiftResponse response) {

    }

    @Override
    public void onGiftRankResponse(GiftRankResponse response) {

    }

    @Override
    public void onGetProductListResponse(ProductListResponse response) {

    }

    @Override
    public void onProductStateChangedResponse(String productId, int state, boolean success) {

    }

    @Override
    public void onProductPurchasedResponse(boolean success) {

    }

    @Override
    public void onSeatInteractionResponse(long processId, String userId, int seatNo, int type) {

    }

    @Override
    public void onResponseError(int requestType, int error, String message) {

    }
}
