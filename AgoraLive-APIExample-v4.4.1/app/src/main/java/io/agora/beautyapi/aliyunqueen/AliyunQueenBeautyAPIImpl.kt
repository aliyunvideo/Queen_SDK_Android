/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.beautyapi.aliyunqueen

import android.content.Context
import android.graphics.Matrix
import android.view.OrientationEventListener
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.aliyun.android.libqueen.ImageFormat
import com.aliyun.android.libqueen.QueenConfig
import com.aliyun.android.libqueen.QueenEngine
import com.aliyun.android.libqueen.QueenResult
import com.aliyun.android.libqueen.Texture2D
import com.aliyun.android.libqueen.models.AlgType
import com.aliyun.android.libqueen.models.Flip
import com.aliyunsdk.queen.param.QueenParamHolder
import io.agora.base.TextureBufferHelper
import io.agora.base.VideoFrame
import io.agora.base.VideoFrame.I420Buffer
import io.agora.base.VideoFrame.TextureBuffer
import io.agora.base.internal.video.YuvHelper
import io.agora.beautyapi.aliyunqueen.utils.LogUtils
import io.agora.rtc2.Constants
import io.agora.rtc2.gl.EglBaseProvider
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import java.nio.ByteBuffer
import java.util.Collections
import java.util.concurrent.Callable


class AliyunQueenBeautyAPIImpl : AliyunQueenBeautyAPI, IVideoFrameObserver {
    private val TAG = "AliyunQueenBeautyAPIImpl"
    private val reportId = "scenarioAPI"
    private val reportCategory = "beauty_android_$VERSION"
    private var beautyMode = 0 // 0: 自动根据buffer类型切换，1：固定使用OES纹理，2：固定使用i420

    private var textureBufferHelper: TextureBufferHelper? = null
    private var nv21ByteBuffer: ByteBuffer? = null
    private var config: Config? = null
    private var enable: Boolean = false
    private var isReleased: Boolean = false
    private var captureMirror = true
    private var renderMirror = true
    private var skipFrame = 0
    private var currBeautyProcessType = BeautyProcessType.UNKNOWN
    private var isFrontCamera = true
    private var cameraConfig = CameraConfig()
    private var localVideoRenderMode = Constants.RENDER_MODE_HIDDEN
    private val pendingProcessRunList = Collections.synchronizedList(mutableListOf<()->Unit>())
    private var frameWidth = 0
    private var frameHeight = 0
    private var mContext: Context? = null
    private var queenEngine: QueenEngine? = null
    private var mOutTexture2D: Texture2D? = null
    private var mMockMatrix: FloatArray? = null

    private var orientationListener: SimpleOrientationListener? = null
    private var deviceOrientation: Int = 0

    private var isDebugQueenEngine = true          // 调试engine，打开此开关，会显示log日志及人脸点位信息

    private enum class BeautyProcessType{
        UNKNOWN, TEXTURE_OES, TEXTURE_2D, I420
    }

    open class SimpleOrientationListener(context: Context?) : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
        }
    }

    override fun initialize(config: Config): Int {
        if (this.config != null) {
            LogUtils.e(TAG, "initialize >> The beauty api has been initialized!")
            return ErrorCode.ERROR_HAS_INITIALIZED.value
        }
        this.mContext = config.context
        this.config = config
        this.cameraConfig = config.cameraConfig
        if (config.captureMode == CaptureMode.Agora) {
            config.rtcEngine.registerVideoFrameObserver(this)
        }

        LogUtils.i(TAG, "initialize >> config = $config")
        config.rtcEngine.sendCustomReportMessage(reportId, reportCategory, "initialize", "$config", 0)
        return ErrorCode.ERROR_OK.value
    }

    override fun enable(enable: Boolean): Int {
        LogUtils.i(TAG, "enable >> enable = $enable")
        if (config == null) {
            LogUtils.e(TAG, "enable >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "enable >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (config?.captureMode == CaptureMode.Custom) {
            skipFrame = 2
            LogUtils.i(TAG, "enable >> skipFrame = $skipFrame")
        }
        this.enable = enable
        config?.rtcEngine?.sendCustomReportMessage(reportId, reportCategory, "enable", "$enable", 0)
        return ErrorCode.ERROR_OK.value
    }

    override fun setupLocalVideo(view: View, renderMode: Int): Int {
        val rtcEngine = config?.rtcEngine
        if(rtcEngine == null){
            LogUtils.e(TAG, "setupLocalVideo >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        LogUtils.i(TAG, "setupLocalVideo >> view=$view, renderMode=$renderMode")
        rtcEngine.sendCustomReportMessage(reportId, reportCategory, "enable", "view=$view, renderMode=$renderMode", 0)
        if (view is TextureView || view is SurfaceView) {
            val canvas = VideoCanvas(view, renderMode, 0)
            canvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
            rtcEngine.setupLocalVideo(canvas)
            return ErrorCode.ERROR_OK.value
        }
        return ErrorCode.ERROR_VIEW_TYPE_ERROR.value
    }

    override fun onFrame(videoFrame: VideoFrame): Int {
        val conf = config
        if (conf == null) {
            LogUtils.e(TAG, "onFrame >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "onFrame >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (conf.captureMode != CaptureMode.Custom) {
            LogUtils.e(TAG, "onFrame >> The capture mode is not Custom!")
            return ErrorCode.ERROR_PROCESS_NOT_CUSTOM.value
        }
        if (processBeauty(videoFrame)) {
            return ErrorCode.ERROR_OK.value
        }
        LogUtils.i(TAG, "onFrame >> Skip Frame.")
        return ErrorCode.ERROR_FRAME_SKIPPED.value
    }

    override fun setBeautyPreset(
        preset: BeautyPreset,
        beautyNodePath: String,
        beauty4ItemNodePath: String,
        reSharpNodePath: String
    ): Int {
        TODO("Not yet implemented")
    }

    override fun setParameters(key: String, value: String) {
        when (key) {
            "beauty_mode" -> beautyMode = value.toInt()
        }
    }

    override fun runOnProcessThread(run: () -> Unit) {
        if (config == null) {
            LogUtils.e(TAG, "runOnProcessThread >> The beauty api has not been initialized!")
            return
        }
        if (isReleased) {
            LogUtils.e(TAG, "runOnProcessThread >> The beauty api has been released!")
            return
        }
        if (textureBufferHelper?.handler?.looper?.thread == Thread.currentThread()) {
            run.invoke()
        } else if (textureBufferHelper != null) {
            textureBufferHelper?.handler?.post(run)
        } else {
            pendingProcessRunList.add(run)
        }
    }

    override fun updateCameraConfig(config: CameraConfig): Int {
        LogUtils.i(TAG, "updateCameraConfig >> oldCameraConfig=$cameraConfig, newCameraConfig=$config")
        cameraConfig = CameraConfig(config.frontMirror, config.backMirror)
        this.config?.rtcEngine?.sendCustomReportMessage(reportId, reportCategory, "updateCameraConfig", "config=$config", 0)

        return ErrorCode.ERROR_OK.value
    }

    override fun isFrontCamera() = isFrontCamera

    override fun release(): Int {
        val conf = config
        if(conf == null){
            LogUtils.e(TAG, "release >> The beauty api has not been initialized!")
            return ErrorCode.ERROR_HAS_NOT_INITIALIZED.value
        }
        if (isReleased) {
            LogUtils.e(TAG, "setBeautyPreset >> The beauty api has been released!")
            return ErrorCode.ERROR_HAS_RELEASED.value
        }
        if (conf.captureMode == CaptureMode.Agora) {
            conf.rtcEngine.registerVideoFrameObserver(null)
        }
        conf.rtcEngine.sendCustomReportMessage(reportId, reportCategory, "release", "", 0)
        LogUtils.i(TAG, "release")
        isReleased = true

        textureBufferHelper?.invoke(Callable {
            queenEngine?.release()
            queenEngine = null
            mOutTexture2D?.release()
            mOutTexture2D = null
            orientationListener?.disable()
            orientationListener = null
        })
        textureBufferHelper?.let {
            textureBufferHelper = null
            it.handler.removeCallbacksAndMessages(null)
            it.invoke {
                config?.eventCallback?.onEffectDestroyed?.invoke()
                null
            }
            it.dispose()
        }
        pendingProcessRunList.clear()
        return ErrorCode.ERROR_OK.value
    }

    private fun processBeauty(videoFrame: VideoFrame): Boolean {
        if (isReleased) {
            LogUtils.e(TAG, "processBeauty >> The beauty api has been released!")
            return false
        }

        val cMirror =
            if (isFrontCamera) {
                when (cameraConfig.frontMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> true
                    MirrorMode.MIRROR_LOCAL_ONLY -> false
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            } else {
                when (cameraConfig.backMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> true
                    MirrorMode.MIRROR_LOCAL_ONLY -> false
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            }
        val rMirror =
            if (isFrontCamera) {
                when (cameraConfig.frontMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> false
                    MirrorMode.MIRROR_LOCAL_ONLY -> true
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            } else {
                when (cameraConfig.backMirror) {
                    MirrorMode.MIRROR_LOCAL_REMOTE -> false
                    MirrorMode.MIRROR_LOCAL_ONLY -> true
                    MirrorMode.MIRROR_REMOTE_ONLY -> true
                    MirrorMode.MIRROR_NONE -> false
                }
            }
        if (captureMirror != cMirror || renderMirror != rMirror) {
            LogUtils.w(TAG, "processBeauty >> enable=$enable, captureMirror=$captureMirror->$cMirror, renderMirror=$renderMirror->$rMirror")
            captureMirror = cMirror
            if(renderMirror != rMirror){
                renderMirror = rMirror
                config?.rtcEngine?.setLocalRenderMode(
                    localVideoRenderMode,
                    if(renderMirror) Constants.VIDEO_MIRROR_MODE_ENABLED else Constants.VIDEO_MIRROR_MODE_DISABLED
                )
            }
            skipFrame = 2
            return false
        }

        val oldIsFrontCamera = isFrontCamera
        isFrontCamera = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
        if(oldIsFrontCamera != isFrontCamera){
            LogUtils.w(TAG, "processBeauty >> oldIsFrontCamera=$oldIsFrontCamera, isFrontCamera=$isFrontCamera")
            return false
        }

        val oldFrameWidth = frameWidth
        val oldFrameHeight = frameHeight
        frameWidth = videoFrame.rotatedWidth
        frameHeight = videoFrame.rotatedHeight
        if (oldFrameWidth > 0 || oldFrameHeight > 0) {
            if(oldFrameWidth != frameWidth || oldFrameHeight != frameHeight){
                skipFrame = 2
                return false
            }
        }

        if(!enable){
            return true
        }

        if (textureBufferHelper == null) {
            textureBufferHelper = TextureBufferHelper.create(
                "AliyunQueenRender",
                EglBaseProvider.instance().rootEglBase.eglBaseContext
            )
            textureBufferHelper?.invoke {
                config?.eventCallback?.onEffectInitialized?.invoke()
                synchronized(pendingProcessRunList){
                    val iterator = pendingProcessRunList.iterator()
                    while (iterator.hasNext()){
                        iterator.next().invoke()
                        iterator.remove()
                    }
                }
            }
            LogUtils.i(TAG, "processBeauty >> create texture buffer, beautyMode=$beautyMode")
        }

        val textureBuffer = videoFrame.buffer as TextureBuffer
        var textWidth = textureBuffer.width
        var textHeight = textureBuffer.height
//        var rotation = videoFrame.rotation            // 在渲染视频前设置该帧的顺时针旋转角度，目前支持 0 度、90 度、180 度，和 270 度。
//        var rotatedWidth = videoFrame.rotatedWidth    // 获取旋转后的视频帧宽度
//        var rotatedHeight = videoFrame.rotatedHeight  // 获取旋转后的视频帧高度

        val processTexId = processBeautyAuto(videoFrame)

        if (processTexId < 0) {
            LogUtils.w(TAG, "processBeauty >> processTexId < 0")
            return false
        }

        if (skipFrame > 0) {
            skipFrame--
            return false
        }

        // 不要作转换，为保障最大适用场景，此处采取方案为，保证输出画面尺寸方向，与输入尺寸方向一致，继续沿用原有的绘制流程。
        // 因此，此处wrapTextureBuffer时采用标准matrix，且replaceBuffer时的rotation继续使用原rotation。
        // textureBuffer.transformMatrix的作用是y轴翻转，也就是左右反向，其他不动;若在wrapTextureBuffer()将其设置为标准matrix，则预览为正常。
        val processBuffer: TextureBuffer = textureBufferHelper?.wrapTextureBuffer(
            textWidth,
            textHeight,
            TextureBuffer.Type.RGB,
            processTexId,
            Matrix()
        ) ?: return false

        videoFrame.replaceBuffer(processBuffer, videoFrame.rotation, videoFrame.timestampNs)

        return true
    }

    private fun processBeautyAuto(videoFrame: VideoFrame): Int {
        return processBeautySingleBuffer(videoFrame)
//        return processBeautySingleTexture(videoFrame)     // 纹理模式在横放置时，仍有角度问题，采用buffer模式即可，避免纹理读取buffer，性能更高
    }

    private fun processBeautySingleBuffer(videoFrame: VideoFrame): Int {
        val texBufferHelper = textureBufferHelper ?: return -1
        val nv21Buffer = getNV21Buffer(videoFrame) ?: return -1

        return texBufferHelper.invoke(Callable {
            val width = videoFrame.buffer.width
            val height = videoFrame.buffer.height
            val srcTextureId = (videoFrame.buffer as TextureBuffer).textureId

            ensureEngine()

            var inputParams = getInputParamsInBuffer(width, height, isFrontCamera, deviceOrientation)
            queenEngine?.setInputTexture(srcTextureId, inputParams[0], inputParams[1],false)
            if (null == mOutTexture2D) {
                mOutTexture2D = queenEngine?.autoGenOutTexture(inputParams[2] == 1)
            }
            QueenParamHolder.writeParamToEngine(queenEngine, false)
            queenEngine?.setSegmentInfoFlipY(true)
            queenEngine?.updateInputDataAndRunAlg(nv21Buffer, ImageFormat.NV21, width, height, 0, inputParams[3], 0, 0)

            val retCode: Int = if (mMockMatrix != null) {
                queenEngine?.renderTexture(mMockMatrix) ?: 0 // 如果为null，返回默认值0
            } else {
                queenEngine?.render() ?: 0 // 如果为null，返回默认值0
            }

            if (retCode == QueenResult.QUEEN_OK) {
                return@Callable mOutTexture2D?.textureId ?: srcTextureId
            } else {
                return@Callable srcTextureId
            }
        })
    }


    private fun processBeautySingleTexture(videoFrame: VideoFrame): Int {
        val texBufferHelper = textureBufferHelper ?: return -1
        val buffer = videoFrame.buffer as? TextureBuffer ?: return -1

        when(buffer.type){
            TextureBuffer.Type.OES -> {
                if(currBeautyProcessType != BeautyProcessType.TEXTURE_OES){
                    LogUtils.i(TAG, "processBeauty >> process source type change old=$currBeautyProcessType, new=${BeautyProcessType.TEXTURE_OES}")
                    currBeautyProcessType = BeautyProcessType.TEXTURE_OES
                    return -1
                }
            }
            else -> {
                if(currBeautyProcessType != BeautyProcessType.TEXTURE_2D){
                    LogUtils.i(TAG, "processBeauty >> process source type change old=$currBeautyProcessType, new=${BeautyProcessType.TEXTURE_2D}")
                    currBeautyProcessType = BeautyProcessType.TEXTURE_2D
                    return -1
                }
            }
        }

        val srcTextureId = (videoFrame.buffer as TextureBuffer).textureId
        return texBufferHelper.invoke(Callable {
            val textureBuffer = videoFrame.buffer as TextureBuffer
            val width = textureBuffer.width
            val height = textureBuffer.height

            ensureEngine()

            var inputParams = getInputParamsInTexture(width, height, isFrontCamera, deviceOrientation)
            queenEngine?.setInputTexture(srcTextureId, inputParams[0], inputParams[1],false)
            if (null == mOutTexture2D) {
                mOutTexture2D = queenEngine?.autoGenOutTexture(inputParams[2] == 1)
            }
            QueenParamHolder.writeParamToEngine(queenEngine, false);
            queenEngine?.setSegmentInfoFlipY(true);

            if (isDebugQueenEngine) {
                queenEngine?.enableDebugLog()
                queenEngine?.enableDetectPointDebug(AlgType.kFaceDetect, true)
            }

            queenEngine?.updateInputTextureBufferAndRunAlg(inputParams[3], 0, 0, false)


            val retCode: Int = if (mMockMatrix != null) {
                queenEngine?.renderTexture(mMockMatrix) ?: 0 // 如果为null，返回默认值0
            } else {
                queenEngine?.render() ?: 0 // 如果为null，返回默认值0
            }

            val resultTexture2D = mOutTexture2D?.textureId ?: srcTextureId
            if (retCode == QueenResult.QUEEN_OK) {
                return@Callable resultTexture2D
            } else {
                return@Callable srcTextureId
            }
        })
    }

    private fun ensureEngine(): Int {
        if (queenEngine == null) {
            var queenConfig = QueenConfig()
            queenEngine = QueenEngine(mContext, queenConfig)

            ensureInitOrientationListener()
        }
        return 0
    }

    private fun ensureInitOrientationListener() {
        if (orientationListener == null) {
            orientationListener = object : SimpleOrientationListener(mContext) {
                override fun onOrientationChanged(orientation: Int) {
                    deviceOrientation = if (orientation >= 315 || orientation <= 45) {
                        0
                    } else if (orientation > 45 && orientation <= 135) {
                        90 // 设备方向为右横屏
                    } else if (orientation > 135 && orientation <= 225) {
                        180 // 设备方向为倒置竖直
                    } else {
                        270 // 设备方向为左横屏
                    }
                }
            }
        }

        if (orientationListener?.canDetectOrientation() == true) orientationListener?.enable()
    }

    /**
     * 返回输入所需的参数信息，数组顺序为：[]setInputWidth, setInputHeight, autoGenOutTextureKeepDirection, algUpdateInputAngle]
     */
    private fun getInputParamsInTexture(w:Int, h:Int, isFront: Boolean, orientation: Int): IntArray {
        var setInputTextureWidth: Int
        var setInputTextureHeight: Int
        var setGenOutTextureKeepDirection: Int
        var setAlgUpdateInputAngle: Int

        var mockCameraAngle = if (isFront) { orientation }
                                else { (orientation + 180) % 360 }

        if (mockCameraAngle == 0) {
            // 手机前置相机+摆放正向 以及 后置相机+摆放倒立，适用本规则
            // 输入为横屏，（头朝向右，相对正向，在逆时针270度，即w > h），一般前置摄像头，选择此项
//            setGenOutTextureKeepDirection = 1
//            setInputTextureWidth = h
//            setInputTextureHeight = w
//            setAlgUpdateInputAngle = 270
//            mMockMatrix = floatArrayOf(
//                0.0f, -1.0f, 0.0f, 0.0f,
//                1.0f, 0.0f, 0.0f, 0.0f,
//                0.0f, 0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f, 0.0f, 1.0f
//            )

            setGenOutTextureKeepDirection = 1
            setInputTextureWidth = h
            setInputTextureHeight = w
            setAlgUpdateInputAngle = 270
            mMockMatrix = floatArrayOf(
                0.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
            )

        } else if (mockCameraAngle == 180) {
            // 手机前置相机+倒立 以及 后置相机+正向，适用本规则
            // 横屏，（头朝向左，相对正向，逆时针90度，即w > h），一般后置摄像头，选择此项
            setGenOutTextureKeepDirection = 1
            setInputTextureWidth = h
            setInputTextureHeight = w
            setAlgUpdateInputAngle = 90
            mMockMatrix = floatArrayOf(
                0.0f, -1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f,0.0f,1.0f
            )
        } else if (mockCameraAngle == 90) {
            // 输入画面为横屏模式下，头顶正向下。前置相机+手机摆放摄像头在右方，后置相机+手机摆放摄像头在左方
            setGenOutTextureKeepDirection = 1
            setInputTextureWidth = h
            setInputTextureHeight = w
            setAlgUpdateInputAngle = 180
            queenEngine?.setRenderAndFaceFlip(Flip.kNone, Flip.kFlipY)  // NOTE, 如果需要贴纸反过来，则设置此处的值为Y
            mMockMatrix = null
//            mMockMatrix = floatArrayOf(
//                0.0f, -1.0f, 0.0f, 0.0f,
//                1.0f, 0.0f, 0.0f, 0.0f,
//                0.0f, 0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f,0.0f,1.0f
//            )

//            setGenOutTextureKeepDirection = 1
//            setInputTextureWidth = h
//            setInputTextureHeight = w
//            setAlgUpdateInputAngle = 90
//            var mRotationMatrixX = floatArrayOf(
//                1.0f, 0.0f,  0.0f, 0.0f,
//                0.0f, 0.0f, -1.0f, 0.0f,
//                0.0f, 1.0f,  0.0f, 0.0f,
//                0.0f, 0.0f,  0.0f, 1.0f
//            )
//            var mRotationMatrixY = floatArrayOf(
//                0.0f, 0.0f, 1.0f, 0.0f,
//                0.0f, 1.0f, 0.0f, 0.0f,
//                -1.0f, 0.0f, 0.0f, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f
//            )
//            var mRotationMatrix = floatArrayOf(
//                0.0f, -1.0f, 0.0f, 0.0f,
//                1.0f,  0.0f, 0.0f, 0.0f,
//                0.0f,  0.0f, 1.0f, 0.0f,
//                0.0f,  0.0f, 0.0f, 1.0f
//            )
////            queenEngine?.updateInputMatrix(mRotationMatrix)
//            queenEngine?.setRenderAndFaceFlip(Flip.kNone, Flip.kFlipY)  // NOTE, 如果需要贴纸反过来，则设置此处的值为Y
////            mMockMatrix = null
////            mMockMatrix = floatArrayOf(
////                0.0f, -1.0f, 0.0f, 0.0f,
////                1.0f, 0.0f, 0.0f, 0.0f,
////                0.0f, 0.0f, 1.0f, 0.0f,
////                0.0f, 1.0f, 0.0f, 1.0f
////            )

        } else {
            // 输入画面为横屏模式下，头顶正向上。前置相机+手机摆放摄像头在左方，后置相机+手机摆放摄像头在右方
            setGenOutTextureKeepDirection = 1
            setInputTextureWidth = h
            setInputTextureHeight = w
            setAlgUpdateInputAngle = 0
//            queenEngine?.setRenderAndFaceFlip(Flip.kNone, Flip.kFlipY)  // NOTE, 如果需要贴纸反过来，则设置此处的值为Y
            mMockMatrix = null
        }

        return intArrayOf(
            setInputTextureWidth, setInputTextureHeight, setGenOutTextureKeepDirection, setAlgUpdateInputAngle
        )
    }

    private fun getInputParamsInBuffer(w:Int, h:Int, isFront: Boolean, orientation: Int): IntArray {
        var setInputTextureWidth = h
        var setInputTextureHeight = w
        var setGenOutTextureKeepDirection = 1

        if (isFront) {
            mMockMatrix = floatArrayOf(
                0.0f, -1.0f, 0.0f, 0.0f,    // 表示把原图像的 x 轴转变为 -y 轴。
                1.0f, 0.0f, 0.0f, 0.0f,     // 表示把原图像的 y 轴转变为 x 轴。
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
            )   // 逆时针90度旋转，同时保持 z 坐标不变

        } else {
            mMockMatrix = floatArrayOf(
                0.0f, 1.0f, 0.0f, 0.0f,  // 把原图像的 x 轴转变为 y 轴。
                -1.0f, 0.0f, 0.0f, 0.0f,  //把原图像的 y 轴转变为 -x 轴。
                0.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f
            )
        }


        // 摄像头正向摆放，实际采集到画面是逆时针270度的，也就是说，手机摆放角度与实际采集画面角度，实际是相差270度。
        var setAlgUpdateInputAngle = if(isFront) {
                                        270 - orientation
                                    } else {
                                        (90 + orientation) % 360
                                    }
        return intArrayOf(
            setInputTextureWidth, setInputTextureHeight, setGenOutTextureKeepDirection, setAlgUpdateInputAngle,
        )
    }

    private fun getNV21Buffer(videoFrame: VideoFrame): ByteArray? {
        val buffer = videoFrame.buffer
        val i420Buffer = buffer as? I420Buffer ?: buffer.toI420()
        val width = i420Buffer.width
        val height = i420Buffer.height
        val nv21Size = (width * height * 3.0f / 2.0f + 0.5f).toInt()
        if (nv21ByteBuffer == null || nv21ByteBuffer?.capacity() != nv21Size) {
            nv21ByteBuffer?.clear()
            nv21ByteBuffer = ByteBuffer.allocateDirect(nv21Size)
            return null
        }
        val nv21ByteArray = ByteArray(nv21Size)

        YuvHelper.I420ToNV12(
            i420Buffer.dataY, i420Buffer.strideY,
            i420Buffer.dataV, i420Buffer.strideV,
            i420Buffer.dataU, i420Buffer.strideU,
            nv21ByteBuffer, width, height
        )
        nv21ByteBuffer?.position(0)
        nv21ByteBuffer?.get(nv21ByteArray)
        if (buffer !is I420Buffer) {
            i420Buffer.release()
        }
        return nv21ByteArray
    }

    // IVideoFrameObserver implements

    override fun onCaptureVideoFrame(sourceType: Int, videoFrame: VideoFrame?): Boolean {
        videoFrame ?: return false
        return processBeauty(videoFrame)
    }

    override fun onPreEncodeVideoFrame(sourceType: Int, videoFrame: VideoFrame?) = false

    override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int) = false

    override fun onRenderVideoFrame(
        channelId: String?,
        uid: Int,
        videoFrame: VideoFrame?
    ) = false

    override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

    override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

    override fun getRotationApplied() = false

    override fun getMirrorApplied() = captureMirror && !enable

    override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

}