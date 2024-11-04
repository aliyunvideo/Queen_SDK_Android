package com.aliyun.maliang.android.simpleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.EGL14;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aliyun.android.libqueen.QueenConfig;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.QueenResult;
import com.aliyun.android.libqueen.Texture2D;
import com.aliyun.android.libqueen.models.AlgType;
import com.aliyun.android.libqueen.models.Flip;
import com.aliyun.maliang.android.simpleapp.utils.DebugHelper;
import com.aliyunsdk.queen.menu.IParamChangeListener;
import com.aliyunsdk.queen.param.QueenParamHolder;
import com.aliyunsdk.queen.param.QueenRuntime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class BeautyImageTexturePanel implements View.OnClickListener, IParamChangeListener {

    private boolean mIsDebugMode = false;

    private Activity mAttachActivity;

    private RelativeLayout mCameraContainer;

    private ImageView mIvImage;

    private QueenEngine mQueenEngine;

    private Texture2D mBitmapInputTexture;
    private float[] mMockMatrix = null;
    private boolean mKeepDirection = false;
    private Texture2D mOutTexture2D;
    private Bitmap mOriginBmp;
    private volatile boolean isUpdating = false;
    private String mCurBmpPath;
    private boolean mBmpHadChanged;

    private HandlerThread mHandlerThread;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private int runIndex = 0;
    private ArrayList<int[]> mMockInputParams = new ArrayList<>(64);

    private interface IFrameCallback {
        void onFrameCallback(Bitmap inBmp, Bitmap outBmp);
    }

    public BeautyImageTexturePanel(Activity activity) {
        mAttachActivity = activity;

        mUiHandler = new Handler();
        mHandlerThread = new HandlerThread("work-thread", 5);
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
    }

    public View onCreateImagePanel() {
        View panelLayout = LayoutInflater.from(getActivity()).inflate(R.layout.image_panel, null, false);
        panelLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        mCameraContainer = (RelativeLayout)panelLayout;
        mIvImage = mCameraContainer.findViewById(R.id.show_image);
        mCameraContainer.findViewById(R.id.btn_pick_image).setOnClickListener(this);
        return mCameraContainer;
    }

    public void onResume() {
        if (mWorkHandler != null) {
            mWorkHandler.post(mRefreshFrameRunnable);
        }
    }

    public void onPause() {
        mWorkHandler.removeCallbacks(mRefreshFrameRunnable);
        releaseEngineSafely();
    }

    public void onDestroy() {
        exitFrameRefresh();
    }

    private Activity getActivity() { return mAttachActivity; }

    private int mRefreshPeriod = 300;
    private Runnable mRefreshFrameRunnable = new Runnable() {
        @Override
        public void run() {

            if (null != mOriginBmp) {
                runAlgOnBitmap(mOriginBmp, new IFrameCallback() {
                    @Override
                    public void onFrameCallback(Bitmap inBmp, Bitmap outBmp) {
                        mIvImage.setImageBitmap(outBmp);
                        if (QueenRuntime.isRenderSplit) {
                            mIvImage.setVisibility(View.GONE);
                        } else {
                            mIvImage.setVisibility(View.VISIBLE);
                        }
                        isUpdating = false;
                    }
                }, true);
            } else {
                isUpdating = false;
            }

            mWorkHandler.postDelayed(mRefreshFrameRunnable, mRefreshPeriod);
        }
    };


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_pick_image) {
            doActionPickMedia( mAttachActivity, "选图", "image/*",
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MainActivity.REQUEST_CODE_SELECT_IMAGE);
        }
    }

    public void onUpdateNewData(Intent data) {
        if (null != data) {
            Uri imageUri = data.getData();
            if (mCurBmpPath != null && mCurBmpPath.equals(imageUri.getPath())) {
                return;
            }

            updateImage(imageUri);
        }
    }

    private Bitmap getWrapperImage(Uri imageUri) {
        InputStream inputStream = null;
        Bitmap targetBmp = null;
        try {
            inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            int bmpDegree = getBitmapDegree(inputStream);
            inputStream.close();
            inputStream = getActivity().getContentResolver().openInputStream(imageUri);

            byte[] bmpBytes = readStream(inputStream);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length, options);
            options.inSampleSize = calculateInSampleSize(options, 1024, 1820);
            options.inJustDecodeBounds = false;
            Bitmap bmp = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length, options);
            if (bmp != null) {
                targetBmp = bmpDegree != 0 ? rotateBitmap(bmp, bmpDegree) : bmp;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return targetBmp;
    }

    private Bitmap getWrapperImage(String filePath) {
        Bitmap targetBmp = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(filePath));
            int bmpDegree = getBitmapDegree(fileInputStream);
            fileInputStream.close();

            fileInputStream = new FileInputStream(new File(filePath));
            Bitmap bmp = BitmapFactory.decodeStream(fileInputStream);
            if (bmp != null) {
                targetBmp = bmpDegree != 0 ? rotateBitmap(bmp, bmpDegree) : bmp;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return targetBmp;
    }

    private void updateImage(Uri imageUri) {
        Bitmap bitmap = getWrapperImage(imageUri);
        if (bitmap != null) {
            updateOriginalBmp(bitmap);
            mCurBmpPath = imageUri.getPath();
            mBmpHadChanged = true;
        }
    }

    private void updateImage(String filePath) {
        Bitmap bitmap = getWrapperImage(filePath);
        if (bitmap != null) {
            updateOriginalBmp(bitmap);
        }
    }

    public Bitmap rotateBitmap(Bitmap originBmp, int degree) {
        if (originBmp == null)
            return originBmp;

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) >= reqHeight
                    && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    private int getBitmapDegree(InputStream inputStream) {
        int degree = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return degree;
        }

        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(inputStream);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private void runAlgOnBitmapSafely(final Bitmap inBmp, final IFrameCallback callback, boolean callbackInUIThread) {
        runAlgOnBitmap(inBmp, callback, callbackInUIThread);
    }

    private void runAlgOnBitmap(final Bitmap inBmp, final IFrameCallback callback, boolean callbackInUIThread) {
        try {
            Bitmap renderBmp = inBmp;
            ensureEngine();
            ensureInputTexture(inBmp);

            if (mIsDebugMode) {
                mQueenEngine.enableDebugLog();
                mQueenEngine.enableDetectPointDebug(AlgType.kFaceDetect, true);
            }

            int[] parmas = getInputParams();
            mQueenEngine.setInputTexture(mBitmapInputTexture.getTextureId(), parmas[3], parmas[4], false);

            if (null == mOutTexture2D) {
                mOutTexture2D = mQueenEngine.autoGenOutTexture(mKeepDirection);

//                int w = mBitmapInputTexture.getSize().x;
//                int h = mBitmapInputTexture.getSize().y;
//                mQueenEngine.updateOutTexture(mOutTexture2D.getTextureId(), w, h, false);
            }
            QueenParamHolder.writeParamToEngine(mQueenEngine, false);
            mQueenEngine.setSegmentInfoFlipY(true);

            int inputAngle = parmas[0], outputAngle = parmas[1], flipAxis = parmas[2];
            android.util.Log.e("TEST_QUEEN", "==inputAngle=" + inputAngle + ", outAngle=" + outputAngle + ", flip=" + flipAxis);
            mQueenEngine.updateInputTextureBufferAndRunAlg(inputAngle, outputAngle, flipAxis, false);
            int retCode = mMockMatrix != null ? mQueenEngine.renderTexture(mMockMatrix) : mQueenEngine.render();

//            DebugHelper.afterProcessEngine(mQueenEngine, mockInputTextureId, false, w, h);

            if (retCode == QueenResult.QUEEN_OK) {
                renderBmp = mOutTexture2D.readToBitmap();
            } else {
                renderBmp = inBmp;
            }

            final Bitmap outBmp = renderBmp;
            if (null != callback) {
                if (callbackInUIThread) {
                    if (null != mUiHandler) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFrameCallback(inBmp, outBmp);
                            }
                        });
                    }
                } else {
                    callback.onFrameCallback(inBmp, outBmp);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void ensureEngine() {
        if (null == mQueenEngine) {
            try {
                mQueenEngine = createQueenEngine(getActivity(), true, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void ensureInputTexture(Bitmap bitmap) {
        if (mBmpHadChanged) {
            mBmpHadChanged = false;
            if (mBitmapInputTexture != null) {
                mBitmapInputTexture.release();
            }
            mBitmapInputTexture = new Texture2D(mQueenEngine.getEngineHandler());
            mBitmapInputTexture.initWithBitmap(bitmap);
        }
    }

    private int[] getInputParams() {
        int[] algUpdateInputParams;
        int w = mBitmapInputTexture.getSize().x;
        int h = mBitmapInputTexture.getSize().y;
        boolean isNeedRotateInputTextureWH = false;
        int mockTextureType = 270;
        if (mockTextureType == 270) {
            // （头朝向，相对正向，在逆时针270度，即w > h），一般前置摄像头，选择此项
            mKeepDirection = true;
            isNeedRotateInputTextureWH = true;
            algUpdateInputParams = new int[]{270, 0, 2};
            mMockMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f};
        } else if (mockTextureType == 90) {
            // （头朝向，相对正向，逆时针90度，即w > h），一般后置摄像头，选择此项
            mKeepDirection = true;
            isNeedRotateInputTextureWH = true;
            algUpdateInputParams = new int[]{90, 0, 0};
            mMockMatrix = new float[]{0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
        } else if (mockTextureType == 0) {
            // 第三方sdk回调，已经转好方向的，选择此项
            mKeepDirection = false;
            isNeedRotateInputTextureWH = false;
            algUpdateInputParams = new int[] {0, 0, 0};
            float[] matrix = new float[16];
            android.opengl.Matrix.setIdentityM(matrix, 0);  // 初始化一个标准单位矩阵
            // 对X轴进行缩放，以实现上下翻转
            // (sx, sy, sz) 分别为沿 X、Y、Z 轴的缩放因子，
            // 这里我们只需要沿 X 轴缩放 -1 来实现上下翻转
//            android.opengl.Matrix.scaleM(matrix, 0, 1, -1, 1);
            matrix[5] = -1.0f;   // 在矩阵中，matrix[5]代表Y轴上的缩放因子。设置为-1.0f则表示沿Y轴上下翻转。
//            mMockMatrix = matrix;
        } else {
            mKeepDirection = true;
            algUpdateInputParams = getMockInputParams();
            mRefreshPeriod = 2000;      // 延长刷新时间，便于观察
            // 声网-前置摄像头matrix
//            mMockMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f};
        }
        int inputTextureW = isNeedRotateInputTextureWH ? h : w;
        int inputTextureH = isNeedRotateInputTextureWH ? w : h;
        return new int[] {algUpdateInputParams[0], algUpdateInputParams[1], algUpdateInputParams[2], inputTextureW, inputTextureH};
    }

    private int[] getMockInputParams() {
        if (mMockInputParams.size() == 0) {
            int[] in_angles = {0, 90, 180, 270}, out_angles = {0, 90, 180, 270}, flips = {0,1,2,3};
            for (int i = 0; i < in_angles.length; i++) {
                for (int j = 0; j < out_angles.length; j++) {
                    for (int k = 0; k < flips.length; k++) {
                        mMockInputParams.add(new int[]{in_angles[i], out_angles[j], flips[k]});
                    }
                }
            }
        }

        int idx = runIndex % mMockInputParams.size();
        ++runIndex;
        return mMockInputParams.get(idx);
    }

    private void exitFrameRefresh() {
        mWorkHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                releaseEngine();
                if (null != mHandlerThread) {
                    try {
                        mHandlerThread.quit();
                        mHandlerThread.interrupt();
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }
                }
            }
        });
    }

    private void releaseEngineSafely() {
        if (mWorkHandler != null) {
            mWorkHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    releaseEngine();
                }
            });
        }
    }

    private void releaseEngine() {
        if (null != mQueenEngine) {
            releaseEngine(mQueenEngine);
            mQueenEngine = null;
        }
        if (mOutTexture2D != null) {
            mOutTexture2D.release();
            mOutTexture2D = null;
        }
    }

    @Override
    public void onParamChange() {
        if (!isUpdating && mWorkHandler != null) {
            isUpdating = true;
            mWorkHandler.post(mRefreshFrameRunnable);
        }
    }

    private void updateOriginalBmp(Bitmap bmp) {
        mOriginBmp = bmp;

        // 图片尺寸变化,重新加载初始化engine
        releaseEngineSafely();
        onParamChange();
    }

    protected void doActionPickMedia(Activity activity, String title, String type, Uri uri, int requestCode) {
        if (Build.VERSION.SDK_INT >= 30) { // Android 11 = API Level 30
            Intent pickIntent = new Intent(Intent.ACTION_PICK, uri);
            activity.startActivityForResult(pickIntent, requestCode);
        } else {
            Intent intent = new Intent();
            intent.setType(type);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(intent, title), requestCode);
        }
    }

    public static QueenEngine createQueenEngine(Context context, boolean withContext, boolean withNewGlThread) {
        QueenEngine engine = null;
        try {
            QueenConfig config = new QueenConfig();
            config.withContext = withContext;
            config.withNewGlThread = withNewGlThread;
            config.enableDebugLog = true;
//            config.algInputMode = 2;  // 2-手动，0-半自动，仅需输入，1-全自动
//            config.toScreen = true;
            if (withContext || withNewGlThread) {
                if (Build.VERSION.SDK_INT >= 21) {
                    config.shareGlContext = EGL14.eglGetCurrentContext().getNativeHandle();
                } else {
                    config.shareGlContext = EGL14.eglGetCurrentContext().getHandle();
                }
            }
            engine = new QueenEngine(context, config);
        } catch (Exception e) { e.printStackTrace(); }

        return engine;
    }

    public static void releaseEngine(QueenEngine engine) {
        if (engine != null)
            engine.release();
    }
}
