package com.aliyun.maliang.android.simpleapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.aliyun.maliang.android.simpleapp.image.SimpleImageGLSurfaceView;
import com.aliyun.maliang.android.simpleapp.image.SimpleImageRenderer;
import com.aliyunsdk.queen.menu.IParamChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BeautyImageTextureV2Panel implements View.OnClickListener, IParamChangeListener {

    private Activity mAttachActivity;

    private String mCurBmpPath;
    private SimpleImageGLSurfaceView mGLSurfaceView;

    public BeautyImageTextureV2Panel(Activity activity) {
        mAttachActivity = activity;
    }

    public View onCreateImagePanel() {
        SimpleImageRenderer renderer = new SimpleImageRenderer();
        mGLSurfaceView = new SimpleImageGLSurfaceView(mAttachActivity);
        mGLSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mGLSurfaceView.init(renderer, mAttachActivity);

        RelativeLayout imageSurfaceLayout = new RelativeLayout(this.getActivity());
        imageSurfaceLayout.addView(mGLSurfaceView);

        Button button = new Button(this.getActivity());
        button.setText("Pick Image");
        button.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageSurfaceLayout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doActionPickMedia( mAttachActivity, "选图", "image/*",
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MainActivity.REQUEST_CODE_SELECT_IMAGE);
            }
        });

        return imageSurfaceLayout;
//        return mGLSurfaceView;
    }

    public void onPause() {
        // DO NOTHING
    }
    public void onResume() {
        // DO NOTHING
    }

    public void onDestroy() {
        mGLSurfaceView.release();
    }

    private Activity getActivity() { return mAttachActivity; }


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

    @Override
    public void onParamChange() {
        mGLSurfaceView.requestUpdateRender();
    }

    private void updateOriginalBmp(Bitmap bmp) {
        // TODO：更新内容
        mGLSurfaceView.updateInputBmp(bmp);
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

}
