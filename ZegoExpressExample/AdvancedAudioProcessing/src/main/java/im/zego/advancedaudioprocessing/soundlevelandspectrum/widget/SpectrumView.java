package im.zego.advancedaudioprocessing.soundlevelandspectrum.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

import im.zego.advancedaudioprocessing.R;
import im.zego.commontools.logtools.DeviceInfoManager;

public class SpectrumView extends View {

    private Context context;
    private Paint paint;
    private int paintColor;
    private float strokeWidth;
    private float height, width;
    private float padding;
    private boolean running = true;

    private Thread thread;

    public SpectrumView(Context context) {
        this(context, null);
    }

    public SpectrumView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BeatLoadView);
        paintColor = typedArray.getColor(R.styleable.BeatLoadView_paintColor, Color.GRAY);
        height = typedArray.getDimension(R.styleable.BeatLoadView_itemHeight, dp2px(20));
        strokeWidth = typedArray.getDimension(R.styleable.BeatLoadView_strokeWidth, dp2px(2));
        padding = typedArray.getDimension(R.styleable.BeatLoadView_itemsPadding, dp2px(4));

        width = DeviceInfoManager.getScreenWidth(context);

        strokeWidth = (width / 64);

        typedArray.recycle();
        initPaint();
    }


    private void initPaint() {
        paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.FILL);

        thread = new Thread() {
            @Override
            public void run() {
                while (true) {

                    if (running)
                        postInvalidate();

                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        thread.start();

        for (int i = 0; i < maxLine; i++) {
            color[i] = getColor();
        }
    }

    int[] color = new int[64];
    int maxLine = 64;
    float stopTmp;
    float[] frequencySpectrums = new float[64];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < maxLine; i++) {
            double stopY = (getStopY(frequencySpectrums[i]));
            paint.setColor(color[i]);
            canvas.drawLine(strokeWidth * i, height, strokeWidth * i, (float) (height - (stopY)), paint);
        }
    }

    private int getColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return Color.rgb(r, g, b);
    }

    private double getStopY(double frequencySpectrum) {
        double value = frequencySpectrum < 0 ? 0 : frequencySpectrum;
        value = value >= 0 ? value : -value;
        double itemH;
        if (value > 10) {
            itemH = (double) (Math.log(value) / 20 * height);
        } else {
            itemH = value / 10;
        }
        return itemH;
    }


    private DecelerateInterpolator[] decelerateInterpolator = new DecelerateInterpolator[64];


    private float dp2px(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density * dp;
    }


    public void updateFrequencySpectrum(float[] frequencySpectrumList) {
        frequencySpectrums = frequencySpectrumList;

    }
}
