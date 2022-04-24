package com.tencent.mlvb.thirdbeauty;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aliyun.queen.IQueenRender;
import com.aliyun.queen.QueenRender;
import com.aliyun.queen.utils.LicenseHelper;
import com.nama.FURenderer;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.mlvb.common.MLVBBaseActivity;
import com.tencent.mlvb.common.URLUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.Random;

import static com.tencent.live2.V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeTexture;
import static com.tencent.live2.V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D;

/**
 * TRTC 第三方美颜页面
 *
 */
public class ThirdQueenBeautyActivity extends MLVBBaseActivity implements View.OnClickListener {
    private static final String TAG = "ThirdBeautyActivity";

    private TXCloudVideoView    mPushRenderView;
    private V2TXLivePusher      mLivePusher;
    private SeekBar             mSeekBlurLevel;
    private TextView            mTextBlurLevel;
    private EditText            mEditStreamId;
    private Button              mButtonPush;
    private TextView            mTextTitle;
    private IQueenRender                mQueenRenderer;
    // Texture transformation matrix, the image will be displayed upright
    private float flipMatrix[] = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thirdbeauty_activity_third_beauty);
        FURenderer.setup(getApplicationContext());

        mQueenRenderer = new QueenRender.Builder()
                .build();
        if (checkPermission()) {
            initView();
        }

        String hashCode = LicenseHelper.getPackageSignature(this);
        android.util.Log.e("TEST_QUEEN", "pkgName=" + this.getPackageName() + ", hashd=" + hashCode);
    }

    @Override
    protected void onPermissionGranted() {
        initView();
    }


    private void initView() {
        mPushRenderView = findViewById(R.id.pusher_tx_cloud_view);
        mSeekBlurLevel  = findViewById(R.id.sb_blur_level);
        mTextBlurLevel  = findViewById(R.id.tv_blur_level);
        mButtonPush     = findViewById(R.id.btn_push);
        mEditStreamId   = findViewById(R.id.et_stream_id);
        mTextTitle      = findViewById(R.id.tv_title);

        mEditStreamId.setText(generateStreamId());
        findViewById(R.id.iv_back).setOnClickListener(this);

        mSeekBlurLevel.setVisibility(View.GONE);

        mButtonPush.setOnClickListener(this);
        if(!TextUtils.isEmpty(mEditStreamId.getText().toString())){
            mTextTitle.setText(mEditStreamId.getText().toString());
        }

        // 添加底部菜单栏
        com.aliyunsdk.queen.menu.BeautyMenuPanel beautyMenuPanel = new com.aliyunsdk.queen.menu.BeautyMenuPanel(this);
        final FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        menuParams.gravity = Gravity.BOTTOM;

        LinearLayout contentView = (LinearLayout)findViewById(R.id.bottom_linearlayout_container);
        contentView.addView(beautyMenuPanel, menuParams);
    }

    private void startPush() {
        String streamId = mEditStreamId.getText().toString();
        if(TextUtils.isEmpty(streamId)){
            Toast.makeText(ThirdQueenBeautyActivity.this, getString(R.string.thirdbeauty_please_input_streamd), Toast.LENGTH_SHORT).show();
            return;
        }
        mTextTitle.setText(streamId);
        String userId = String.valueOf(new Random().nextInt(10000));
        String pushUrl = URLUtils.generatePushUrl(streamId, userId, 0);
        mLivePusher = new V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);

        mLivePusher.enableCustomVideoProcess(true, V2TXLivePixelFormatTexture2D, V2TXLiveBufferTypeTexture);
        mLivePusher.setObserver(new V2TXLivePusherObserver() {
            @Override
            public void onGLContextCreated() {
                mQueenRenderer.onTextureCreate(getApplicationContext());
            }

            @Override
            public int onProcessVideoFrame(V2TXLiveDef.V2TXLiveVideoFrame srcFrame, V2TXLiveDef.V2TXLiveVideoFrame dstFrame) {
                dstFrame.texture.textureId = mQueenRenderer.onTextureProcess(srcFrame.texture.textureId, flipMatrix, srcFrame.width, srcFrame.height);
                return 0;
            }

            @Override
            public void onGLContextDestroyed() {
                mQueenRenderer.onTextureDestroy();
            }
        });
        mLivePusher.setRenderView(mPushRenderView);
        mLivePusher.startCamera(true);
        int ret = mLivePusher.startPush(pushUrl);
        Log.i(TAG, "startPush return: " + ret);
        mLivePusher.startMicrophone();
        mButtonPush.setText(R.string.thirdbeauty_stop_push);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLivePusher != null){
            mLivePusher.stopCamera();
            mLivePusher.stopMicrophone();
            if(mLivePusher.isPushing() == 1){
                mLivePusher.stopPush();
            }
            mLivePusher = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.btn_push){
            push();
        }else if(id == R.id.iv_back){
            finish();
        }
    }

    private void push() {
        if(mLivePusher != null && mLivePusher.isPushing() == 1){
            stopPush();
        }else{
            startPush();
        }
    }

    private void stopPush() {
        if(mLivePusher != null){
            mLivePusher.stopCamera();
            mLivePusher.stopMicrophone();
            if(mLivePusher.isPushing() == 1){
                mLivePusher.stopPush();
            }
            mLivePusher = null;
        }
        mButtonPush.setText(R.string.thirdbeauty_start_push);
    }
}