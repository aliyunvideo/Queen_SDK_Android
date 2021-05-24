package com.qiniu.pili.droid.streaming.demo.gles;


import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.glGetError;

public class CommonFrameGlDrawer {
    private final FloatBuffer mCubeTextureCoordinates;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private final int TEXTURE_COORDINATE_DATA_SIZE = 2;
    private final float[] IDENTITY_MATRIX = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private final static String VETEX_SHADER_CODE =
            "attribute vec2 a_TexCoordinate;" +
                    "attribute vec4 vPosition;" +
                    "varying vec2 v_TexCoordinate;" +
                    "uniform mat4 uTransformMatrix;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  v_TexCoordinate = (uTransformMatrix * vec4(a_TexCoordinate, 0, 1)).xy;" +
                    "}";

    private final static String FRAGMENT_SHADER_CODE =
                    "precision mediump float;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
                    "}";

    private final int shaderProgram;
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private int mPositionHandle;
    private int mMVPMatrixHandle;

    private static final int COORDINATE_PER_VERTEX = 2;
    private final static float POSITIONS[] = {
            -1.0f, +1.0f,
            +1.0f, +1.0f,
            -1.0f, -1.0f,
            +1.0f, -1.0f,
    };

    private final static float[] TEXTURE_COORDINATE_DATA =
            {
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            };

    private short drawOrder[] = { 0, 1, 2, 1, 3, 2 };
    private final int vertexStride = COORDINATE_PER_VERTEX * 4;

    public CommonFrameGlDrawer()
    {
        // 分配顶点坐标属性
        ByteBuffer bb = ByteBuffer.allocateDirect(POSITIONS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(POSITIONS);
        vertexBuffer.position(0);

        // 分配纹理UV属性

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(TEXTURE_COORDINATE_DATA.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(TEXTURE_COORDINATE_DATA).position(0);

        // 分配索引
        ByteBuffer dlb = ByteBuffer.allocateDirect(POSITIONS.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // 编译program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VETEX_SHADER_CODE);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);

        GLES20.glLinkProgram(shaderProgram);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        checkGLError();

        GLES20.glShaderSource(shader, shaderCode);
        checkGLError();

        GLES20.glCompileShader(shader);
        checkGLError();

        return shader;
    }

    private static void checkGLError() {
        int error = glGetError();
        if (error != 0) {
            throw new RuntimeException("gl error: " + error);
        }
    }

    private float[] matrix = new float[16];
    public void draw(float[] transformMatrix, int textureId, boolean isOes)
    {
        if (null == transformMatrix) {
            transformMatrix = IDENTITY_MATRIX;
        } else {
            // 做一次矩阵逆变换
            Matrix.invertM(matrix, 0, transformMatrix, 0);
            transformMatrix = matrix;
        }

        GLES20.glUseProgram(shaderProgram);

        // 设置坐标信息
        mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, COORDINATE_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // 设置纹理和uv
        mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        if (isOes) {
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }

        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uTransformMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, transformMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }
}