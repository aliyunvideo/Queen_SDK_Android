package io.agora.vlive.ui.main;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.elvishew.xlog.XLog;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.UserRequest;
import io.agora.vlive.protocol.model.response.AppVersionResponse;
import io.agora.vlive.protocol.model.response.CreateUserResponse;
import io.agora.vlive.protocol.model.response.GiftListResponse;
import io.agora.vlive.protocol.model.response.LoginResponse;
import io.agora.vlive.protocol.model.response.MusicListResponse;
import io.agora.vlive.protocol.model.response.Response;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.ui.components.PrivacyTermsDialog;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.RandomUtil;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NETWORK_CHECK_INTERVAL = 10000;
    private static final int MAX_PERIODIC_APP_ID_TRY_COUNT = 5;

    private BottomNavigationView mNavView;
    private NavController mNavController;
    private int mAppIdTryCount;
    private PrivacyTermsDialog termsDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_main);
        initPrivacy();
        initUI();
        initAsync();
    }

    private void initPrivacy() {
        if (!preferences().getBoolean(Global.Constants.KEY_SHOW_PRIVACY, false)) {
            termsDialog = new PrivacyTermsDialog(this);
            termsDialog.setPrivacyTermsDialogListener(new PrivacyTermsDialog.OnPrivacyTermsDialogListener() {
                @Override
                public void onPositiveClick() {
                    preferences().edit().putBoolean(Global.Constants.KEY_SHOW_PRIVACY, true).apply();
                }

                @Override
                public void onNegativeClick() {
                    finish();
                }
            });
            termsDialog.show();
        }
    }

    private void initUI() {
        initNavigation();
    }

    private void initNavigation() {
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavView = findViewById(R.id.nav_view);
        mNavView.setItemIconTintList(null);
        changeItemHeight(mNavView);
        mNavView.setOnNavigationItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            int currentId = mNavController.getCurrentDestination() == null ?
                    0 : mNavController.getCurrentDestination().getId();

            // Do not respond to this click event because
            // we do not want to refresh this fragment
            // by repeatedly selecting the same menu item.
            if (selectedId == currentId) return false;
            NavigationUI.onNavDestinationSelected(item, mNavController);
            hideStatusBar(getWindow(), true);
            return true;
        });
    }

    public int getSystemBarHeight() {
        return systemBarHeight;
    }

    public void setNavigationSelected(int resId, Bundle bundle) {
        mNavView.setSelectedItemId(resId);
        mNavController.navigate(resId, bundle);
    }

    private void changeItemHeight(@NonNull BottomNavigationView navView) {
        // Bottom navigation menu uses a hardcode menu item
        // height which cannot be changed by a layout attribute.
        // Change the item height using reflection for
        // a comfortable padding between icon and label.
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.nav_bar_height);
        BottomNavigationMenuView menu =
                (BottomNavigationMenuView) navView.getChildAt(0);
        try {
            Field itemHeightField = BottomNavigationMenuView.class.getDeclaredField("itemHeight");
            itemHeightField.setAccessible(true);
            itemHeightField.set(menu, itemHeight);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void initAsync() {
        new Thread(() -> {
            checkUpdate();
            getGiftList();
            getMusicList();
        }).start();
    }

    private void checkUpdate() {
        if (!config().hasCheckedVersion()) {
            sendRequest(Request.APP_VERSION, getAppVersion());
        }
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {
        config().setVersionInfo(response.data);
        config().setAppId(response.data.config.appId);
        application().initEngine(response.data.config.appId);
        mAppIdTryCount = 0;
        login();
    }

    private void login() {
        Config.UserProfile profile = config().getUserProfile();
        initUserFromStorage(profile);
        if (!profile.isValid()) {
            createUser();
        } else {
            loginToServer();
        }
    }

    private void initUserFromStorage(Config.UserProfile profile) {
        profile.setUserId(preferences().getString(Global.Constants.KEY_PROFILE_UID, null));
        profile.setUserName(preferences().getString(Global.Constants.KEY_USER_NAME, null));
        profile.setImageUrl(preferences().getString(Global.Constants.KEY_IMAGE_URL, null));
        profile.setToken(preferences().getString(Global.Constants.KEY_TOKEN, null));
    }

    private void createUser() {
        String userName = RandomUtil.randomUserName(this);
        config().getUserProfile().setUserName(userName);
        preferences().edit().putString(Global.Constants.KEY_USER_NAME, userName).apply();
        sendRequest(Request.CREATE_USER, new UserRequest(userName));
    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {
        createUserFromResponse(response);
        loginToServer();
    }

    private void createUserFromResponse(CreateUserResponse response) {
        Config.UserProfile profile = config().getUserProfile();
        profile.setUserId(response.data.userId);
        preferences().edit().putString(Global.Constants.KEY_PROFILE_UID, profile.getUserId()).apply();
    }

    private void loginToServer() {
        sendRequest(Request.USER_LOGIN, config().getUserProfile().getUserId());
    }

    @Override
    public void onLoginResponse(LoginResponse response) {
        if (response != null && response.code == Response.SUCCESS) {
            Config.UserProfile profile = config().getUserProfile();
            profile.setToken(response.data.userToken);
            profile.setRtmToken(response.data.rtmToken);
            profile.setAgoraUid(response.data.uid);
            preferences().edit().putString(Global.Constants.KEY_TOKEN, response.data.userToken).apply();
            joinRtmServer();
        }
    }

    private void joinRtmServer() {
        Config.UserProfile profile = config().getUserProfile();
        rtmClient().login(profile.getRtmToken(), String.valueOf(profile.getAgoraUid()), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                XLog.d("rtm client login success:" + config().getUserProfile().getRtmToken());
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    private void getGiftList() {
        sendRequest(Request.GIFT_LIST, null);
    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {
        config().initGiftList(this);
    }

    private void getMusicList() {
        sendRequest(Request.MUSIC_LIST, null);
    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {
        config().setMusicList(response.data);
    }

    @Override
    public void onResponseError(int requestType, int error, String message) {
        XLog.e("request:" + requestType + " error:" + error + " msg:" + message);

        switch (requestType) {
            case Request.APP_VERSION:
                if (mAppIdTryCount <= MAX_PERIODIC_APP_ID_TRY_COUNT) {
                    new Handler(getMainLooper()).postDelayed(
                            MainActivity.this::checkUpdate, NETWORK_CHECK_INTERVAL);
                    mAppIdTryCount++;
                }
                break;
            default: runOnUiThread(() -> showLongToast("Request type: "+
                    Request.getRequestString(requestType) + " " + message));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (termsDialog != null) {
            termsDialog.dismiss();
        }
    }
}
