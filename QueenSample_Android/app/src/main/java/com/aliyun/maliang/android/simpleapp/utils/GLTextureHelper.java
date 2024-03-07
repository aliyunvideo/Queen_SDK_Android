package com.aliyun.maliang.android.simpleapp.utils;

import static android.opengl.GLES20.GL_FRAMEBUFFER;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class GLTextureHelper {

    // 定义一个方法，用于将数据装载进纹理
    public static int loadRgbaBuf2Texture(byte[] rgbBuffer, int textureWidth, int textureHeight, int reusedTextureId) {
        int textureId = reusedTextureId;
        if (reusedTextureId < 0) {
            int[] textureIds = new int[1];
            // 生成纹理
            GLES20.glGenTextures(1, textureIds, 0);
            textureId = textureIds[0];
        }

        // 缓存旧fbo
        int[] oldTexId = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, IntBuffer.wrap(oldTexId));

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // 设置纹理参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // 将数据装载进纹理
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ByteBuffer.wrap(rgbBuffer));

        // 解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, oldTexId[0]);

        return textureId;
    }

    public static byte[] nv21toRGBA(byte[] data, int width, int height) {
        int size = width * height;
        byte[] bytes = new byte[size * 4];
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
        return bytes;
    }

    public static void saveRgbaToFile(byte[] rgbaData, int width, int height, String filePath) {
        try {
            Bitmap bmp = rgba2Bitmap(
                    rgbaData,
                    width,
                    height
            );
            FileOutputStream outputStream = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Bitmap rgba2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertRgbaByteArrayToColor(data);    //取RGB值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }

    public static int[] convertRgbaByteArrayToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 4 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size/4 + arg];
        int red, green, blue, alpha;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 4]);
                green = convertByteToInt(data[i * 4 + 1]);
                blue = convertByteToInt(data[i * 4 + 2]);
                alpha = convertByteToInt(data[i * 4 + 3]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (alpha << 24) | (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 4]);
                green = convertByteToInt(data[i * 4 + 1]);
                blue = convertByteToInt(data[i * 4 + 2]);
                alpha = convertByteToInt(data[i * 4 + 3]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (alpha << 24) | (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }
    private static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }
}
