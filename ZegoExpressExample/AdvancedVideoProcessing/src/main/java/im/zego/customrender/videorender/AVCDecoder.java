package im.zego.customrender.videorender;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * AVCDecoder
 *
 * AVCANNEXB Decoder
 * This class is to decode the encoded video data thrown by SDK
 * Developers may refer to this class in order to  implement receiving and rendering encoded video data thrown by SDK.
 *
 */
@TargetApi(23)
public class AVCDecoder {

    private final static String TAG = "Zego";
    private final static int CONFIGURE_FLAG_DECODE = 0;

    // component of decoder and encoder
    private MediaCodec mediaCodec;
    // Information of media format
    private MediaFormat mediaFormat;
    private Surface surface;
    // The width of render view
    private int viewWidth;
    // The height of render view
    private int viewHeight;

    /** Data ready for decoding
     *  Includes time stamps and data for decoding
     */
    static class DecodeInfo {
        public long timeStmp; // Nanosecond
        public byte[] inOutData;
    }

    private final static ConcurrentLinkedQueue<DecodeInfo> mInputDatasQueue = new ConcurrentLinkedQueue<DecodeInfo>();

    // Decoder callback
    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        // Input buffer call back, waiting for input
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int inputBufferId) {
            try {
                // get the buffer address of MediaCodec input buffer
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                inputBuffer.clear();

                // Collect data from video data queue
                DecodeInfo decodeInfo = mInputDatasQueue.poll();

                if (decodeInfo != null) {
                    // write the data frame into buffer of Media Codec
                    inputBuffer.put(decodeInfo.inOutData, 0, decodeInfo.inOutData.length);
                    // Video frame data enters MediaCodec queue wating for decoding. It requires Linearly increasing time stamps.
                    mediaCodec.queueInputBuffer(inputBufferId, 0, decodeInfo.inOutData.length, decodeInfo.timeStmp * 1000, 0);
                } else {
                    long now = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        now = SystemClock.elapsedRealtimeNanos();
                    } else {
                        now = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
                    }
                    mediaCodec.queueInputBuffer(inputBufferId, 0, 0, now * 1000, 0);
                }
            } catch (IllegalStateException exception) {
                Log.d(TAG, "encoder mediaCodec input exception: " + exception.getMessage());
            }
        }

        /**
         * Callback for completing decoding
         * Render will start after MediaCodec finishes decoding. So the callback does not deal with decoded data.
         */
        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            try {
                // Get the output buffer address of MediaCode based on index of buffer
                ByteBuffer outputBuffer = AVCDecoder.this.mediaCodec.getOutputBuffer(id);
                MediaFormat outputFormat = AVCDecoder.this.mediaCodec.getOutputFormat(id);
                int width = outputFormat.getInteger("width");
                int height = outputFormat.getInteger("height");
//            Log.d(TAG, "decoder OutputBuffer, width: "+width+", height: "+height);
                if(mediaFormat == outputFormat && outputBuffer != null && bufferInfo.size > 0){
                    byte [] buffer = new byte[outputBuffer.remaining()];
                    outputBuffer.get(buffer);
                }

                boolean doRender = (bufferInfo.size != 0);
                // Complete processing, release ByteBuffer data
                AVCDecoder.this.mediaCodec.releaseOutputBuffer(id, doRender);
            } catch (IllegalStateException ex) {
                Log.d(TAG, "encoder mediaCodec output exception: " + ex.getMessage());
            }
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            Log.d(TAG, "decoder onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            Log.d(TAG, "decoder onOutputFormatChanged");
        }


    };

    /**
     * Initialize Decoded
     * @param surface  Surface to display the decoded video
     * @param viewwidth Width of render display view
     * @param viewheight Height of render display view
     */
    public AVCDecoder(Surface surface, int viewwidth, int viewheight){
        try {
            // Choose decoded whose MIME format is AVC to construct MediaCodec
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            mediaCodec = null;
            return;
        }

        if(surface == null){
            return;
        }

        this.viewWidth = viewwidth;
        this.viewHeight = viewheight;
        this.surface = surface;

        // set the MediaFormat of Decoder
        mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, viewWidth, viewHeight);
    }

    // Provide video frame data to Decoder
    public void inputFrameToDecoder(byte[] needEncodeData, long timeStmp){
        if (needEncodeData != null) {
            DecodeInfo decodeInfo = new DecodeInfo();
            decodeInfo.inOutData = needEncodeData;
            decodeInfo.timeStmp = timeStmp;
            boolean inputResult = mInputDatasQueue.offer(decodeInfo);
            if (!inputResult) {
                Log.i(TAG, "decoder inputDecoder queue result = false queue current size = " + mInputDatasQueue.size());
            }
        }
    }

    // start the decoder
    public void startDecoder(){
        if(mediaCodec != null && surface != null){
            // set the callback of decoder
            mediaCodec.setCallback(mCallback);
            // configure the MediaCodec，choose to enable decoder function
            mediaCodec.configure(mediaFormat, surface,null,CONFIGURE_FLAG_DECODE);
            // start the decoder
            mediaCodec.start();
        }else{
            throw new IllegalArgumentException("startDecoder failed, please check the MediaCodec is init correct");
        }
    }

    // release the decoder
    public void stopAndReleaseDecoder(){
        if(mediaCodec != null){
            try {
                mediaCodec.setCallback(null);
                mediaCodec.stop();
                mediaCodec.release();
                mInputDatasQueue.clear();
//            mOutputDatasQueue.clear();
                mediaCodec = null;
            } catch (IllegalStateException e) {
                Log.d(TAG,"MediaCodec decoder stop exception: "+e.getMessage());
            }

        }
    }
}

