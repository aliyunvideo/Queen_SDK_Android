package io.agora.vlive;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import io.agora.capture.video.camera.Constant;
import io.agora.framework.PreprocessorFaceUnity;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.vlive.protocol.model.model.AppVersionInfo;
import io.agora.vlive.protocol.model.model.GiftInfo;
import io.agora.vlive.protocol.model.model.MusicInfo;
import io.agora.vlive.utils.GiftUtil;
import io.agora.vlive.utils.Global;

public class Config {
    public static class UserProfile {
        private String userId;
        private String userName;
        private String imageUrl;
        private String token;
        private String rtcToken;
        private String rtmToken;
        private long agoraUid;
        private SoftReference<Drawable> userIcon;

        public boolean isValid() {
            return userId != null;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName != null ? userName : userId;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRtcToken() {
            return rtcToken;
        }

        public void setRtcToken(String rtcToken) {
            this.rtcToken = rtcToken;
        }

        public String getRtmToken() {
            return rtmToken;
        }

        public void setRtmToken(String rtmToken) {
            this.rtmToken = rtmToken;
        }

        public long getAgoraUid() {
            return agoraUid;
        }

        public void setAgoraUid(long agoraUid) {
            this.agoraUid = agoraUid;
        }

        public Drawable getProfileIcon() {
            return userIcon == null ? null : userIcon.get();
        }

        public void setProfileIcon(Drawable userProfileDrawable) {
            this.userIcon = new SoftReference<>(userProfileDrawable);
        }
    }

    public static final int LIVE_TYPE_MULTI_HOST = 1;
    public static final int LIVE_TYPE_SINGLE_HOST = 2;
    public static final int LIVE_TYPE_PK_HOST = 3;
    public static final int LIVE_TYPE_VIRTUAL_HOST = 4;
    public static final int LIVE_TYPE_ECOMMERCE = 5;

    private AgoraLiveApplication mApplication;

    Config(AgoraLiveApplication application) {
        mApplication = application;
        mUserProfile = new UserProfile();
        SharedPreferences sp = mApplication.preferences();

        mBeautyEnabled = sp.getBoolean(Global.Constants.KEY_BEAUTY_ENABLED, true);

        mBlurValue = sp.getFloat(Global.Constants.KEY_BLUR, PreprocessorFaceUnity.DEFAULT_BLUR_VALUE);
        mWhitenValue = sp.getFloat(Global.Constants.KEY_WHITEN, PreprocessorFaceUnity.DEFAULT_WHITEN_VALUE);
        mCheekValue = sp.getFloat(Global.Constants.KEY_CHEEK, PreprocessorFaceUnity.DEFAULT_CHEEK_VALUE);
        mEyeValue = sp.getFloat(Global.Constants.KEY_EYE, PreprocessorFaceUnity.DEFAULT_EYE_VALUE);

        mResolutionIndex = sp.getInt(Global.Constants.KEY_RESOLUTION, Global.Constants.VIDEO_DEFAULT_RESOLUTION_INDEX);
        mFrameRateIndex = sp.getInt(Global.Constants.KEY_FRAME_RATE, Global.Constants.VIDEO_DEFAULT_FRAME_RATE_INDEX);
        mBitrate = sp.getInt(Global.Constants.KEY_BITRATE, Global.Constants.VIDEO_DEFAULT_BITRATE);
    }

    private UserProfile mUserProfile;
    private AppVersionInfo mVersionInfo;
    private String mAppId;
    private List<GiftInfo> mGiftInfoList = new ArrayList<>();
    private List<MusicInfo> mMusicInfoList = new ArrayList<>();
    private int mLastTabPosition = Global.Constants.TAB_ID_MULTI;

    // Camera capture configurations
    private int mCameraFacing = Constant.CAMERA_FACING_FRONT;

    // Beautification configs
    private boolean mBeautyEnabled;
    private float mBlurValue;
    private float mWhitenValue;
    private float mCheekValue;
    private float mEyeValue;

    // Video configs
    private int mResolutionIndex;
    private int mFrameRateIndex;
    private int mBitrate;

    private int mCurrentPlayedMusicIndex = -1;

    // rtc configurations
    private boolean mVideoMuted;
    private boolean mAudioMuted;

    public UserProfile getUserProfile() {
        return mUserProfile;
    }

    public AppVersionInfo getVersionInfo() {
        return mVersionInfo;
    }

    public void setVersionInfo(AppVersionInfo mVersionInfo) {
        this.mVersionInfo = mVersionInfo;
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }

    public boolean appIdObtained() {
        return !TextUtils.isEmpty(mAppId);
    }

    public boolean hasCheckedVersion() {
        return mVersionInfo != null;
    }

    public boolean isBeautyEnabled() {
        return mBeautyEnabled;
    }

    public void setBeautyEnabled(boolean enabled) {
        mBeautyEnabled = enabled;
        mApplication.preferences().edit()
                .putBoolean(Global.Constants.KEY_BEAUTY_ENABLED, enabled).apply();
    }

    public int lastTabPosition() {
        return mLastTabPosition;
    }

    public void setLastTabPosition(int position) {
        mLastTabPosition = position;
    }

    public int getCameraFacing() {
        return mCameraFacing;
    }

    public void setCameraFacing(int facing) {
        this.mCameraFacing = facing;
    }

    public float blurValue() {
        return mBlurValue;
    }

    public void setBlurValue(float blur) {
        mBlurValue = blur;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_BLUR, blur).apply();
    }

    public float whitenValue() {
        return mWhitenValue;
    }

    public void setWhitenValue(float whiten) {
        mWhitenValue = whiten;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_WHITEN, whiten).apply();
    }

    public float cheekValue() {
        return mCheekValue;
    }

    public void setCheekValue(float cheek) {
        mCheekValue = cheek;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_CHEEK, cheek).apply();
    }

    public float eyeValue() {
        return mEyeValue;
    }

    public void setEyeValue(float eye) {
        mEyeValue = eye;
        mApplication.preferences().edit()
                .putFloat(Global.Constants.KEY_EYE, eye).apply();
    }

    public int resolutionIndex() {
        return mResolutionIndex;
    }

    public void setResolutionIndex(int index) {
        mResolutionIndex = index;
    }

    public int frameRateIndex() {
        return mFrameRateIndex;
    }

    public void setFrameRateIndex(int index) {
        mFrameRateIndex = index;
    }

    public int videoBitrate() {
        return mBitrate;
    }

    public void setVideoBitrate(int bitrate) {
        mBitrate = bitrate;
    }

    public VideoEncoderConfiguration createVideoEncoderConfig(int type) {
        switch (type) {
            case LIVE_TYPE_MULTI_HOST:
                return new VideoEncoderConfiguration(
                        Global.Constants.RESOLUTIONS_MULTI_HOST[0],
                        Global.Constants.FRAME_RATES[0],
                        VideoEncoderConfiguration.STANDARD_BITRATE,

                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);
            case LIVE_TYPE_SINGLE_HOST:
                return new VideoEncoderConfiguration(
                        Global.Constants.RESOLUTIONS_SINGLE_HOST[mResolutionIndex],
                        Global.Constants.FRAME_RATES[0],
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);
            case LIVE_TYPE_PK_HOST:
                return new VideoEncoderConfiguration(
                        Global.Constants.RESOLUTIONS_PK_HOST[0],
                        Global.Constants.FRAME_RATES[0],
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);
            case LIVE_TYPE_VIRTUAL_HOST:
                return new VideoEncoderConfiguration(
                        Global.Constants.RESOLUTIONS_VIRTUAL_IMAGE[0],
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT);
            case LIVE_TYPE_ECOMMERCE:
                return new VideoEncoderConfiguration(
                        Global.Constants.RESOLUTIONS_ECOMMERCE[0],
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT);
            default: return null;
        }
    }

    public int currentMusicIndex() {
        return mCurrentPlayedMusicIndex;
    }

    public void setCurrentMusicIndex(int index) {
        mCurrentPlayedMusicIndex = index;
    }

    public void setVideoMuted(boolean muted) {
        mVideoMuted = muted;
    }

    public boolean isVideoMuted() {
        return mVideoMuted;
    }

    public void setAudioMuted(boolean muted) {
        mAudioMuted = muted;
    }

    public boolean isAudioMuted() {
        return mAudioMuted;
    }

    public void initGiftList(Context context) {
        String[] mGiftNames = context.getResources().getStringArray(R.array.gift_names);
        int[] mGiftValues = context.getResources().getIntArray(R.array.gift_values);
        mGiftInfoList = new ArrayList<>();
        for (int i = 0; i < mGiftNames.length; i++) {
            GiftInfo info = new GiftInfo(i, mGiftNames[i],
                    GiftUtil.GIFT_ICON_RES[i], mGiftValues[i]);
            mGiftInfoList.add(i, info);
        }
    }

    public List<GiftInfo> getGiftList() {
        return mGiftInfoList;
    }

    public void setMusicList(List<MusicInfo> list) {
        mMusicInfoList.clear();
        mMusicInfoList.addAll(list);
    }

    public List<MusicInfo> getMusicList() {
        return mMusicInfoList;
    }
}
