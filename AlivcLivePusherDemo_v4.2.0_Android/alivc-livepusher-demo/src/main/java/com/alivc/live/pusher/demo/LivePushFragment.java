package com.alivc.live.pusher.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alivc.component.custom.AlivcLivePushCustomFilter;
import com.alivc.live.pusher.AlivcLivePushBGMListener;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcSnapshotListener;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;

import com.alivc.live.beautyui.BeautyContainerView;
import com.alivc.live.beautyui.BeautyUIDataInjectorFactory;
import com.alivc.live.beautyui.bean.BeautyItemBean;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.beautyui.listener.BeautyTabListener;
import com.alivc.live.beautyui.manager.BeautyUIManager;
import com.alivc.live.beauty.BeautyFactory;
import com.alivc.live.beauty.BeautyInterface;
import com.alivc.live.beauty.constant.BeautyImageFormat;
import com.alivc.live.beauty.constant.BeautySDKType;
import com.alivc.component.custom.AlivcLivePushCustomDetect;
import com.alivc.live.pusher.LogUtil;
import com.alivc.live.pusher.widget.CommonDialog;
import com.alivc.live.pusher.widget.DataView;
import com.alivc.live.pusher.widget.TextFormatUtil;

public class LivePushFragment extends Fragment implements Runnable {
    public static final String TAG = "LivePushFragment";

    private static final String URL_KEY = "url_key";
    private static final String ASYNC_KEY = "async_key";
    private static final String AUDIO_ONLY_KEY = "audio_only_key";
    private static final String VIDEO_ONLY_KEY = "video_only_key";
    private static final String QUALITY_MODE_KEY = "quality_mode_key";
    private static final String CAMERA_ID = "camera_id";
    private static final String FLASH_ON = "flash_on";
    private static final String AUTH_TIME = "auth_time";
    private static final String PRIVACY_KEY = "privacy_key";
    private static final String MIX_EXTERN = "mix_extern";
    private static final String MIX_MAIN = "mix_main";
    private static final String BEAUTY_CHECKED = "beauty_checked";
    private static final String FPS = "fps";
    private final long REFRESH_INTERVAL = 2000;
    private ImageView mExit;
    private TextView mMusic;
    private TextView mFlash;
    private TextView mCamera;
    private TextView mSnapshot;
    private TextView mBeautyButton;
    private TextView mUrl;
    private TextView mIsPushing;
    private LinearLayout mGuide;

    private TextView mPreviewButton;
    private TextView mPushButton;
    private TextView mOperaButton;
    private TextView mMore;
    private TextView mRestartButton;
    private TextView mDataButton;
    private AlivcLivePusher mAlivcLivePusher = null;
    private String mPushUrl = null;
    private SurfaceView mSurfaceView = null;
    private boolean mAsync = false;

    private boolean mAudio = false;
    private boolean mVideoOnly = false;
    private boolean isPushing = false;
    private Handler mHandler = new Handler();

    private LivePushActivity.PauseState mStateListener = null;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isFlash = false;
    private boolean mMixExtern = false;
    private boolean mMixMain = false;
    private boolean flashState = true;

    private int snapshotCount = 0;

    private int mQualityMode = 0;

    ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(5,
            new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
    private boolean audioThreadOn = false;
    private boolean mIsStartAsnycPushing = false;

    private MusicDialog mMusicDialog = null;

    private String mAuthString = "?auth_key=%1$d-%2$d-%3$d-%4$s";
    private String mMd5String = "%1$s-%2$d-%3$d-%4$d-%5$s";
    private String mTempUrl = null;
    private String mAuthTime = "";
    private String mPrivacyKey = "";
    private TextView mStatusTV;
    private LinearLayout mActionBar;
    Vector<Integer> mDynamicals = new Vector<>();
    private BeautyContainerView mBeautyContainerView;
    // 高级美颜管理类
    private BeautyInterface mBeautyManager;
    private boolean isBeautyEnable = true;
    private DataView mDataView;
    private int mCurBr;
    private int mTargetBr;
    private TextView mCurFpsLayout;
    private TextView mCurBrLayout;
    private boolean mBeautyOn = true;
    private int mFps;
    private CommonDialog mDialog;
    private AlivcLivePushStatsInfo mPushStatsInfo;

    public static LivePushFragment newInstance(String url, boolean async, boolean mAudio, boolean mVideoOnly, int cameraId, boolean isFlash, int mode, String authTime, String privacyKey, boolean mixExtern, boolean mixMain, boolean beautyOn, int fps) {
        LivePushFragment livePushFragment = new LivePushFragment();
        Bundle bundle = new Bundle();
        bundle.putString(URL_KEY, url);
        bundle.putBoolean(ASYNC_KEY, async);
        bundle.putBoolean(AUDIO_ONLY_KEY, mAudio);
        bundle.putBoolean(VIDEO_ONLY_KEY, mVideoOnly);
        bundle.putInt(QUALITY_MODE_KEY, mode);
        bundle.putInt(CAMERA_ID, cameraId);
        bundle.putBoolean(FLASH_ON, isFlash);
        bundle.putString(AUTH_TIME, authTime);
        bundle.putString(PRIVACY_KEY, privacyKey);
        bundle.putBoolean(MIX_EXTERN, mixExtern);
        bundle.putBoolean(MIX_MAIN, mixMain);
        bundle.putBoolean(BEAUTY_CHECKED, beautyOn);
        bundle.putInt(FPS, fps);
        livePushFragment.setArguments(bundle);
        return livePushFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPushUrl = getArguments().getString(URL_KEY);
            mTempUrl = mPushUrl;
            mAsync = getArguments().getBoolean(ASYNC_KEY, false);
            mAudio = getArguments().getBoolean(AUDIO_ONLY_KEY, false);
            mVideoOnly = getArguments().getBoolean(VIDEO_ONLY_KEY, false);
            mCameraId = getArguments().getInt(CAMERA_ID);
            isFlash = getArguments().getBoolean(FLASH_ON, false);
            mMixExtern = getArguments().getBoolean(MIX_EXTERN, false);
            mMixMain = getArguments().getBoolean(MIX_MAIN, false);
            mQualityMode = getArguments().getInt(QUALITY_MODE_KEY);
            mAuthTime = getArguments().getString(AUTH_TIME);
            mPrivacyKey = getArguments().getString(PRIVACY_KEY);
            mBeautyOn = getArguments().getBoolean(BEAUTY_CHECKED);
            mFps = getArguments().getInt(FPS);
            flashState = isFlash;
        }
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.setLivePushInfoListener(mPushInfoListener);
            mAlivcLivePusher.setLivePushErrorListener(mPushErrorListener);
            mAlivcLivePusher.setLivePushNetworkListener(mPushNetworkListener);
            mAlivcLivePusher.setLivePushBGMListener(mPushBGMListener);
            isPushing = mAlivcLivePusher.isPushing();
        }

        if (mAlivcLivePusher != null && mBeautyOn) {
            mAlivcLivePusher.setCustomDetect(new AlivcLivePushCustomDetect() {
                @Override
                public void customDetectCreate() {
                    Log.d(TAG, "customDetectCreate start");
                    initBeautyManager();
                    Log.d(TAG, "customDetectCreate end");
                }

                @Override
                public long customDetectProcess(long data, int width, int height, int rotation, int format, long extra) {
                    Log.d(TAG, "customDetectProcess start: data ptr:" + data + ",width:" + width + ",height:" + height + "," + format + "," + rotation);

                    if (mBeautyManager != null) {
                        mBeautyManager.onDrawFrame(data, BeautyImageFormat.kNV21, width, height, 0, mCameraId);
                        Log.d(TAG, "keria: " + mCameraId);
                    }
                    Log.d(TAG, "customDetectProcess end");

                    return 0;
                }

                @Override
                public void customDetectDestroy() {
                    Log.d(TAG, "customDetectDestroy start");
                    destroyBeautyManager();
                    Log.d(TAG, "customDetectDestroy end");
                }
            });

            mAlivcLivePusher.setCustomFilter(new AlivcLivePushCustomFilter() {
                @Override
                public void customFilterCreate() {
                    Log.d(TAG, "customFilterCreate start");

                    initBeautyManager();

                    Log.d(TAG, "customFilterCreate end");
                }

                @Override
                public void customFilterUpdateParam(float fSkinSmooth, float fWhiten, float fWholeFacePink, float fThinFaceHorizontal, float fCheekPink, float fShortenFaceVertical, float fBigEye) {

                }

                @Override
                public void customFilterSwitch(boolean on) {

                }

                @Override
                public int customFilterProcess(int inputTexture, int textureWidth, int textureHeight, long extra) {
                    Log.d(TAG, "customFilterProcess start: textureId" + inputTexture + ",width:" + textureWidth + ",height:" + textureHeight);

                    int ret = mBeautyManager != null ? mBeautyManager.onTextureInput(inputTexture, textureWidth, textureHeight) : inputTexture;

                    Log.d(TAG, "customFilterProcess end, textureId:" + ret);
                    Log.d(TAG, "keria1: " + mCameraId);

                    return ret;
                }

                @Override
                public void customFilterDestroy() {
                    destroyBeautyManager();
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.push_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBeautyContainerView = view.findViewById(R.id.beauty_container_view);
        mBeautyContainerView.initData(BeautyUIDataInjectorFactory.generateDataInjector(BeautySDKType.QUEEN));
        mBeautyContainerView.setBeautyTabListener(new BeautyTabListener() {
            @Override
            public void onTabItemDataChanged(BeautyTabBean tabBean, BeautyItemBean beautyItemBean) {
                BeautyUIManager.onTabItemDataChanged(mBeautyManager, beautyItemBean);
            }

            @Override
            public void onTabPutAway(BeautyTabBean tabBean) {
                changeBeautyContainerVisibility();
            }

            @Override
            public void onTabReset(BeautyTabBean tabBean) {

            }
        });
        mDataButton = (TextView) view.findViewById(R.id.data);
        mDataView = (DataView) view.findViewById(R.id.ll_data);
        mDataView.setVisibility(View.GONE);
        mStatusTV = (TextView) view.findViewById(R.id.tv_status);
        mExit = (ImageView) view.findViewById(R.id.exit);
        mMusic = (TextView) view.findViewById(R.id.music);
        mFlash = (TextView) view.findViewById(R.id.flash);
        mFlash.setSelected(isFlash);
        mCamera = (TextView) view.findViewById(R.id.camera);
        mSnapshot = (TextView) view.findViewById(R.id.snapshot);
        mActionBar = (LinearLayout) view.findViewById(R.id.action_bar);
        mCamera.setSelected(true);
        mSnapshot.setSelected(true);
        mPreviewButton = (TextView) view.findViewById(R.id.preview_button);
        mPreviewButton.setSelected(false);
        mPushButton = (TextView) view.findViewById(R.id.push_button);
        mPushButton.setSelected(true);
        mOperaButton = (TextView) view.findViewById(R.id.opera_button);
        mOperaButton.setSelected(false);
        mMore = (TextView) view.findViewById(R.id.more);
        mBeautyButton = (TextView) view.findViewById(R.id.beauty_button);
        mBeautyButton.setSelected(SharedPreferenceUtils.isBeautyOn(getActivity().getApplicationContext()));
        mRestartButton = (TextView) view.findViewById(R.id.restart_button);
        mUrl = (TextView) view.findViewById(R.id.push_url);
        mUrl.setText(mPushUrl);
        mIsPushing = (TextView) view.findViewById(R.id.isPushing);
        mCurFpsLayout = ((TextView) mDataView.findViewById(R.id.tv_data1));
        mCurBrLayout = ((TextView) mDataView.findViewById(R.id.tv_data2));
        mIsPushing.setText(String.valueOf(isPushing));
        mGuide = (LinearLayout) view.findViewById(R.id.guide);
        mExit.setOnClickListener(onClickListener);
        mMusic.setOnClickListener(onClickListener);
        mFlash.setOnClickListener(onClickListener);
        mCamera.setOnClickListener(onClickListener);
        mSnapshot.setOnClickListener(onClickListener);
        mPreviewButton.setOnClickListener(onClickListener);
        mPushButton.setOnClickListener(onClickListener);
        mOperaButton.setOnClickListener(onClickListener);
        mBeautyButton.setOnClickListener(onClickListener);
        mRestartButton.setOnClickListener(onClickListener);
        mMore.setOnClickListener(onClickListener);
        mDataButton.setOnClickListener(onClickListener);

        if (mVideoOnly) {
            mMusic.setVisibility(View.GONE);
        }
        if (mAudio) {
            mPreviewButton.setVisibility(View.GONE);
        }
        if (mMixMain) {
            mBeautyButton.setVisibility(View.GONE);
            mMusic.setVisibility(View.GONE);
            mFlash.setVisibility(View.GONE);
            mCamera.setVisibility(View.GONE);
        }
        mMore.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mBeautyButton.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mFlash.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mCamera.setVisibility(mAudio ? View.GONE : View.VISIBLE);
        mFlash.setClickable(mCameraId != CAMERA_TYPE_FRONT.getCameraId());
        mBeautyContainerView.reset();
        mBeautyContainerView.setVisibility(View.INVISIBLE);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int id = view.getId();

            if (mAlivcLivePusher == null) {
                if (getActivity() != null) {
                    mAlivcLivePusher = ((LivePushActivity) getActivity()).getLivePusher();
                }

                if (mAlivcLivePusher == null) {
                    return;
                }
            }

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (id) {
                            case R.id.exit:
                                getActivity().finish();
                                break;
                            case R.id.music:
                                if (mMusicDialog == null) {
                                    mMusicDialog = MusicDialog.newInstance();
                                    mMusicDialog.setAlivcLivePusher(mAlivcLivePusher);
                                }
                                mMusicDialog.show(getFragmentManager(), "beautyDialog");

                                break;
                            case R.id.flash:
                                mAlivcLivePusher.setFlash(!mFlash.isSelected());
                                flashState = !mFlash.isSelected();
                                mFlash.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFlash.setSelected(!mFlash.isSelected());
                                    }
                                });
                                break;
                            case R.id.camera:
                                if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                                    mCameraId = CAMERA_TYPE_BACK.getCameraId();
                                } else {
                                    mCameraId = CAMERA_TYPE_FRONT.getCameraId();
                                }
                                mAlivcLivePusher.switchCamera();
                                mFlash.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFlash.setClickable(mCameraId == CAMERA_TYPE_FRONT.getCameraId() ? false : true);
                                        if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
                                            mFlash.setSelected(false);
                                        } else {
                                            mFlash.setSelected(flashState);
                                        }
                                    }
                                });

                                break;
                            case R.id.preview_button:
                                final boolean isPreview = mPreviewButton.isSelected();
                                if (mSurfaceView == null && getActivity() != null) {
                                    mSurfaceView = ((LivePushActivity) getActivity()).getPreviewView();
                                }
                                if (!isPreview) {
                                    mAlivcLivePusher.stopPreview();
                                } else {
                                    if (mAsync) {
                                        mAlivcLivePusher.startPreviewAysnc(mSurfaceView);
                                    } else {
                                        mAlivcLivePusher.startPreview(mSurfaceView);
                                    }
                                }

                                mPreviewButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPreviewButton.setText(isPreview ? getString(R.string.stop_preview_button) : getString(R.string.start_preview_button));
                                        mPreviewButton.setSelected(!isPreview);
                                    }
                                });

                                break;
                            case R.id.push_button:
                                final boolean isPush = mPushButton.isSelected();
                                if (isPush) {
                                    if (mAsync) {
                                        mAlivcLivePusher.startPushAysnc(mPushUrl);
                                    } else {
                                        mAlivcLivePusher.startPush(mPushUrl);
                                    }
                                } else {
                                    mAlivcLivePusher.stopPush();
                                    stopPcm();
                                    mOperaButton.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mOperaButton.setText(getString(R.string.pause_button));
                                            mOperaButton.setSelected(false);
                                        }
                                    });
                                    if (mStateListener != null) {
                                        mStateListener.updatePause(false);
                                    }
                                }

                                mPushButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mStatusTV.setText(isPush ? getString(R.string.pushing) : getString(R.string.wating_push));
                                        mPushButton.setText(isPush ? getString(R.string.stop_button) : getString(R.string.start_push));
                                        mPushButton.setSelected(!isPush);
                                    }
                                });

                                break;
                            case R.id.opera_button:
                                final boolean isPause = mOperaButton.isSelected();
                                if (!isPause) {
                                    mAlivcLivePusher.pause();
                                } else {
                                    if (mAsync) {
                                        mAlivcLivePusher.resumeAsync();
                                    } else {
                                        mAlivcLivePusher.resume();
                                    }
                                }

                                if (mStateListener != null) {
                                    mStateListener.updatePause(!isPause);
                                }
                                mOperaButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOperaButton.setText(isPause ? getString(R.string.pause_button) : getString(R.string.resume_button));
                                        mOperaButton.setSelected(!isPause);
                                    }
                                });

                                break;
                            case R.id.beauty_button:
                                if (!mBeautyOn) {
                                    ToastUtils.show(getString(R.string.beauty_off_tips));
                                    return;
                                }
                                mBeautyButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeBeautyContainerVisibility();
                                    }
                                });
                                break;
                            case R.id.restart_button:
                                if (mAsync) {
                                    if (!mIsStartAsnycPushing) {
                                        mIsStartAsnycPushing = true;
                                        mAlivcLivePusher.restartPushAync();
                                    }
                                } else {
                                    mAlivcLivePusher.restartPush();
                                }
                                break;
                            case R.id.more:
                                PushMoreDialog pushMoreDialog = new PushMoreDialog();
                                pushMoreDialog.setAlivcLivePusher(mAlivcLivePusher, new DynamicListern() {
                                    @Override
                                    public void onAddDynamic() {
                                        if (mAlivcLivePusher != null && mDynamicals.size() < 5) {
                                            float startX = 0.1f + mDynamicals.size() * 0.2f;
                                            float startY = 0.1f + mDynamicals.size() * 0.2f;
                                            int id = mAlivcLivePusher.addDynamicsAddons(getActivity().getFilesDir().getPath() + File.separator + "alivc_resource/qizi/", startX, startY, 0.2f, 0.2f);
                                            if (id > 0) {
                                                mDynamicals.add(id);
                                            } else {
                                                ToastUtils.show(getString(R.string.add_dynamic_failed) + id);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onRemoveDynamic() {
                                        if (mDynamicals.size() > 0) {
                                            int index = mDynamicals.size() - 1;
                                            int id = mDynamicals.get(index);
                                            mAlivcLivePusher.removeDynamicsAddons(id);
                                            mDynamicals.remove(index);
                                        }
                                    }
                                });
                                pushMoreDialog.setQualityMode(mQualityMode);
                                pushMoreDialog.setPushUrl(mPushUrl);
                                pushMoreDialog.show(getFragmentManager(), "moreDialog");
                                break;
                            case R.id.snapshot:
                                mAlivcLivePusher.snapshot(1, 0, new AlivcSnapshotListener() {
                                    @Override
                                    public void onSnapshot(Bitmap bmp) {
                                        String dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS").format(new Date());
                                        File f = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "snapshot-" + dateFormat + ".png");
                                        if (f.exists()) {
                                            f.delete();
                                        }
                                        try {
                                            FileOutputStream out = new FileOutputStream(f);
                                            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                                            out.flush();
                                            out.close();
                                        } catch (FileNotFoundException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        showDialog("截图已保存：" + getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/snapshot-" + dateFormat + ".png");
                                    }
                                });
                                break;
                            case R.id.data:
                                mDataView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDataView.getVisibility() == View.VISIBLE) {
                                            mDataView.setVisibility(View.INVISIBLE);
                                        } else {
                                            mDataView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        showDialog(e.getMessage());
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        showDialog(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

        }
    };


    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher) {
        this.mAlivcLivePusher = alivcLivePusher;
    }

    public void setStateListener(LivePushActivity.PauseState listener) {
        this.mStateListener = listener;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }


    AlivcLivePushInfoListener mPushInfoListener = new AlivcLivePushInfoListener() {
        @Override
        public void onPreviewStarted(AlivcLivePusher pusher) {
            showToast(getString(R.string.start_preview));
        }

        @Override
        public void onPreviewStoped(AlivcLivePusher pusher) {
            showToast(getString(R.string.stop_preview));
        }

        @Override
        public void onPushStarted(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showToast(getString(R.string.start_push));
        }

        @Override
        public void onFirstAVFramePushed(AlivcLivePusher pusher) {
        }

        @Override
        public void onPushPauesed(AlivcLivePusher pusher) {
            showToast(getString(R.string.pause_push));
        }

        @Override
        public void onPushResumed(AlivcLivePusher pusher) {
            showToast(getString(R.string.resume_push));
        }

        @Override
        public void onPushStoped(AlivcLivePusher pusher) {
            showToast(getString(R.string.stop_push));
        }

        /**
         * 推流重启通知
         *
         * @param pusher AlivcLivePusher实例
         */
        @Override
        public void onPushRestarted(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showToast(getString(R.string.restart_success));
        }

        @Override
        public void onFirstFramePreviewed(AlivcLivePusher pusher) {

        }

        @Override
        public void onDropFrame(AlivcLivePusher pusher, int countBef, int countAft) {
        }

        @Override
        public void onAdjustBitRate(AlivcLivePusher pusher, int curBr, int targetBr) {
        }

        @Override
        public void onAdjustFps(AlivcLivePusher pusher, int curFps, int targetFps) {
        }

        @Override
        public void onPushStatistics(AlivcLivePusher pusher, AlivcLivePushStatsInfo statistics) {
            if (mAlivcLivePusher != null) {
                if (mCurBrLayout != null) {
                    mCurBrLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if(statistics!=null&&statistics.getVideoEncodeBitrate()!=0) {
                                mCurBrLayout.setText("编码码率:" + statistics.getVideoEncodeBitrate() + "kbps");
                            }
                        }
                    });
                }
                if (mCurFpsLayout != null) {
                    mCurFpsLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if(statistics!=null&&statistics.getVideoEncodeFps()!=0) {
                                mCurFpsLayout.setText("发送帧率:" + statistics.getVideoEncodeFps() + "fps");
                            }
                        }
                    });
                }
            }
        }
    };

    AlivcLivePushErrorListener mPushErrorListener = new AlivcLivePushErrorListener() {

        @Override
        public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            mIsStartAsnycPushing = false;
            showDialog(getString(R.string.system_error) + error.toString());
        }

        @Override
        public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            if (error != null) {
                mIsStartAsnycPushing = false;
                showDialog(getString(R.string.sdk_error) + error.toString());
            }
        }
    };

    AlivcLivePushNetworkListener mPushNetworkListener = new AlivcLivePushNetworkListener() {
        @Override
        public void onNetworkPoor(AlivcLivePusher pusher) {
            showNetWorkDialog(getString(R.string.network_poor));
        }

        @Override
        public void onNetworkRecovery(AlivcLivePusher pusher) {
            showToast(getString(R.string.network_recovery));
        }

        @Override
        public void onReconnectStart(AlivcLivePusher pusher) {

            showToastShort(getString(R.string.reconnect_start));
        }

        @Override
        public void onReconnectFail(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showDialog(getString(R.string.reconnect_fail));
        }

        @Override
        public void onReconnectSucceed(AlivcLivePusher pusher) {
            showToast(getString(R.string.reconnect_success));
        }

        @Override
        public void onSendDataTimeout(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showDialog(getString(R.string.senddata_timeout));
        }

        @Override
        public void onConnectFail(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showDialog(getString(R.string.connect_fail));
        }

        @Override
        public void onConnectionLost(AlivcLivePusher pusher) {
            mIsStartAsnycPushing = false;
            showToast("推流已断开");
        }

        @Override
        public String onPushURLAuthenticationOverdue(AlivcLivePusher pusher) {
            return "";
        }

        @Override
        public void onSendMessage(AlivcLivePusher pusher) {
            showToast(getString(R.string.send_message));
        }

        @Override
        public void onPacketsLost(AlivcLivePusher pusher) {
            showToast("推流丢包通知");
        }
    };

    private AlivcLivePushBGMListener mPushBGMListener = new AlivcLivePushBGMListener() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onStoped() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onResumed() {

        }

        @Override
        public void onProgress(final long progress, final long duration) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMusicDialog != null) {
                        mMusicDialog.updateProgress(progress, duration);
                    }
                }
            });
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onDownloadTimeout() {

        }

        @Override
        public void onOpenFailed() {
            showDialog(getString(R.string.bgm_open_failed));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
        }
    }

    private void showToast(final String text) {
        if (getActivity() == null || text == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void showToastShort(final String text) {
        if (getActivity() == null || text == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void showDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        if (mDialog == null || !mDialog.isShowing()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        mDialog = new CommonDialog(getActivity());
                        mDialog.setDialogTitle(getString(R.string.dialog_title));
                        mDialog.setDialogContent(message);
                        mDialog.setConfirmButton(TextFormatUtil.getTextFormat(getActivity(), R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        mDialog.show();
                    }
                }
            });
        }
    }

    private void showDialog(final String title, final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setMessage(message);
                    dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void showNetWorkDialog(final String message) {
        if (getActivity() == null || message == null) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.setMessage(message);
                    dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.setNeutralButton(getString(R.string.reconnect), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                mAlivcLivePusher.reconnectPushAsync(null);
                            } catch (IllegalStateException e) {

                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void run() {
        if (mIsPushing != null && mAlivcLivePusher != null) {
            try {
                isPushing = mAlivcLivePusher.isNetworkPushing();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            AlivcLivePushError error = mAlivcLivePusher.getLastError();
            if (!error.equals(AlivcLivePushError.ALIVC_COMMON_RETURN_SUCCESS)) {
                mIsPushing.setText(String.valueOf(isPushing) + ", error code : " + error.getCode());
            } else {
                mIsPushing.setText(String.valueOf(isPushing));
            }
        }
        mHandler.postDelayed(this, REFRESH_INTERVAL);

    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(this);
    }

    public interface BeautyListener {
        void onBeautySwitch(boolean beauty);
    }

    private BeautyListener mBeautyListener = new BeautyListener() {
        @Override
        public void onBeautySwitch(boolean beauty) {
            if (mBeautyButton != null) {
                mBeautyButton.setSelected(beauty);
            }
        }
    };

    private String getMD5(String string) {

        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private String getUri(String url) {
        String result = "";
        String temp = url.substring(7);
        if (temp != null && !temp.isEmpty()) {
            result = temp.substring(temp.indexOf("/"));
        }
        return result;
    }

    private String getAuthString(String time) {
        if (!time.isEmpty() && !mPrivacyKey.isEmpty()) {
            long tempTime = (System.currentTimeMillis() + Integer.valueOf(time)) / 1000;
            String tempprivacyKey = String.format(mMd5String, getUri(mPushUrl), tempTime, 0, 0, mPrivacyKey);
            String auth = String.format(mAuthString, tempTime, 0, 0, getMD5(tempprivacyKey));
            mTempUrl = mPushUrl + auth;
        } else {
            mTempUrl = mPushUrl;
        }
        return mTempUrl;
    }

    private void startPCM(final Context context) {
        new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private AtomicInteger atoInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("LivePushActivity-readPCM-Thread" + atoInteger.getAndIncrement());
                return t;
            }
        }).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                audioThreadOn = true;
                byte[] pcm;
                int allSended = 0;
                int sizePerSecond = 44100 * 2;
                InputStream myInput = null;
                OutputStream myOutput = null;
                boolean reUse = false;
                long startPts = System.nanoTime() / 1000;
                try {
                    File f = new File(getActivity().getFilesDir().getPath() + File.separator+"alivc_resource/441.pcm");
                    myInput = new FileInputStream(f);
                   // File f = new File("/sdcard/alivc_resource/441.pcm");
                    byte[] buffer = new byte[2048];
                    int length = myInput.read(buffer, 0, 2048);
                    while (length > 0 && audioThreadOn) {
                        long pts = System.nanoTime() / 1000;
                        mAlivcLivePusher.inputStreamAudioData(buffer, length, 44100, 1, pts);
                        allSended += length;
                        if ((allSended * 1000000L / sizePerSecond - 50000) > (pts - startPts)) {
                            try {
                                Thread.sleep(45);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        length = myInput.read(buffer);
                        if (length < 2048) {
                            myInput.close();
                            myInput = new FileInputStream(f);
                            length = myInput.read(buffer);
                        }
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    myInput.close();
                    audioThreadOn = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void stopPcm() {
        audioThreadOn = false;
    }

    public interface DynamicListern {
        void onAddDynamic();

        void onRemoveDynamic();
    }

    private void initBeautyManager() {
        if (mBeautyManager == null) {
            Log.d(TAG, "initBeautyManager start");
            mBeautyManager = BeautyFactory.createBeauty(BeautySDKType.QUEEN, LivePushFragment.this.getActivity());
            // initialize in texture thread.
            mBeautyManager.init();
            mBeautyManager.setBeautyEnable(isBeautyEnable);
            Log.d(TAG, "initBeautyManager end");
        }
    }

    private void destroyBeautyManager() {
        if (mBeautyManager != null) {
            mBeautyManager.release();
            mBeautyManager = null;
        }
    }

    private void changeBeautyContainerVisibility() {
        if (mBeautyContainerView.getVisibility() == View.VISIBLE) {
            mBeautyContainerView.setVisibilityWithAnimation(false);
            mActionBar.setVisibility(View.VISIBLE);
        } else {
            mBeautyContainerView.setVisibilityWithAnimation(true);
            mActionBar.setVisibility(View.INVISIBLE);
        }
    }

}
