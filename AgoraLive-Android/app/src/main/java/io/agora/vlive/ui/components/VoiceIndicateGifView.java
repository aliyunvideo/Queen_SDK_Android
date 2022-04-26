package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.io.IOException;

public class VoiceIndicateGifView extends AppCompatImageView {
    private static final String VOICE_INDICATE_NAME = "voice.gif";

    private Movie mMovie;
    private boolean mStarted;
    private long mMovieStartTimeStamp;
    private long mMovieStopTimeStamp = -1;

    public VoiceIndicateGifView(Context context) {
        super(context);
        setDefaultImage();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public VoiceIndicateGifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setDefaultImage();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void setDefaultImage() {
        try {
            mMovie = Movie.decodeStream(getContext().getAssets().open(VOICE_INDICATE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mStarted) {
            mMovieStartTimeStamp = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (-1 < mMovieStopTimeStamp &&
                mMovieStopTimeStamp < now) {
            stop();
            return;
        }

        if (mMovieStartTimeStamp == 0) {
            mMovieStartTimeStamp = now;
        }

        if (mMovie != null) {
            int duration = mMovie.duration();
            int time = (int) ((now - mMovieStartTimeStamp) % duration);
            mMovie.setTime(time);
            canvas.save();
            mMovie.draw(canvas, 0, 0);
            canvas.restore();
            postInvalidateOnAnimation();
        }
    }

    /**
     * Start gif animation repeatedly until stopped
     */
    public void start() {
        if (!mStarted) {
            mStarted = true;
            invalidate();
        }
    }

    /**
     * Start gif animation for a specific amount of time.
     * @param duration
     */
    public void start(long duration) {
        if (!mStarted) {
            mStarted = true;
            mMovieStopTimeStamp = System.currentTimeMillis() + duration;
            invalidate();
        }
    }

    public void stop() {
        if (mStarted) {
            mStarted = false;
            mMovieStartTimeStamp = 0;
            mMovieStopTimeStamp = -1;
            invalidate();
        }
    }
}
