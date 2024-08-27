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
import android.os.Bundle;
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
import com.aliyunsdk.queen.menu.IParamChangeListener;
import com.aliyunsdk.queen.param.QueenParamHolder;
import com.aliyunsdk.queen.param.QueenRuntime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BeautyImagePanel implements View.OnClickListener, IParamChangeListener {

    private Activity mAttachActivity;

    private RelativeLayout mCameraContainer;

    private ImageView mIvImage;

    private QueenEngine mQueenEngine;
    private Texture2D mOutTexture2D;
    private Bitmap mOriginBmp;
    private volatile boolean isUpdating = false;

    private HandlerThread mHandlerThread;
    private Handler mWorkHandler;
    private Handler mUiHandler;

    private interface IFrameCallback {
        void onFrameCallback(Bitmap inBmp, Bitmap outBmp);
    }

    public BeautyImagePanel(Activity activity) {
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
        releaseEngine();
    }

    public void onDestroy() {
        exitFrameRefresh();
    }

    private Activity getActivity() { return mAttachActivity; }

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

            mWorkHandler.postDelayed(mRefreshFrameRunnable, 1000 / 30);
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
            updateImage(imageUri);

            releaseEngineSafely();
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
            if(QueenRuntime.isEnableQueen) {
                ensureEngine();
                mQueenEngine.setInputBitMap(inBmp);
                if (null == mOutTexture2D) {
                    mOutTexture2D = mQueenEngine.autoGenOutTexture();
                }
                mQueenEngine.setSegmentInfoFlipY(false);
                QueenParamHolder.writeParamToEngine(mQueenEngine, false);
                mQueenEngine.updateInputDataAndRunAlg(inBmp);

                @QueenResult int retCode = mQueenEngine.render();
                if (retCode == QueenResult.QUEEN_OK) {
                    renderBmp = mOutTexture2D.readToBitmap();
                } else {
                    renderBmp = inBmp;
                }
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
//            engine = new QueenEngine(context, withContext, toScreen);
            QueenConfig config = new QueenConfig();
            config.withContext = withContext;
            config.withNewGlThread = withNewGlThread;
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
