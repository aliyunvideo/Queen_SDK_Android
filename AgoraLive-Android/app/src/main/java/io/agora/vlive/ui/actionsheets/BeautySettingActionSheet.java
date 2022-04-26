package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class BeautySettingActionSheet extends AbstractActionSheet
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public interface BeautyActionSheetListener extends AbsActionSheetListener {
        void onActionSheetBeautyEnabled(boolean enabled);
        void onActionSheetBlurSelected(float blur);
        void onActionSheetWhitenSelected(float whiten);
        void onActionSheetCheekSelected(float cheek);
        void onActionSheetEyeEnlargeSelected(float eye);
    }

    private SeekBar mBlurSeekBar;
    private SeekBar mWhitenSeekBar;
    private SeekBar mCheekSeekBar;
    private SeekBar mEyeSeekBar;

    private AppCompatTextView mBlurValue;
    private AppCompatTextView mWhitenValue;
    private AppCompatTextView mCheekValue;
    private AppCompatTextView mEyeValue;

    private View mBeautySwitch;

    private BeautyActionSheetListener mListener;

    public BeautySettingActionSheet(Context context) {
        super(context);
        init();
    }

    public BeautySettingActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeautySettingActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addView(LayoutInflater.from(getContext()).
                inflate(R.layout.action_beauty, this, false));

        mBlurSeekBar = findViewById(R.id.beauty_blur_progress_bar);
        mWhitenSeekBar = findViewById(R.id.beauty_whiten_progress_bar);
        mCheekSeekBar = findViewById(R.id.beauty_cheek_progress_bar);
        mEyeSeekBar = findViewById(R.id.beauty_eye_progress_bar);

        mBlurSeekBar.setOnSeekBarChangeListener(this);
        mWhitenSeekBar.setOnSeekBarChangeListener(this);
        mCheekSeekBar.setOnSeekBarChangeListener(this);
        mEyeSeekBar.setOnSeekBarChangeListener(this);

        mBlurValue = findViewById(R.id.beauty_value_blur);
        mWhitenValue = findViewById(R.id.beauty_value_whiten);
        mCheekValue = findViewById(R.id.beauty_value_cheek);
        mEyeValue = findViewById(R.id.beauty_value_eye);

        float value = application().config().blurValue();
        mBlurSeekBar.setProgress(valueToProgress(value));
        mBlurValue.setText(String.valueOf(value));

        value = application().config().whitenValue();
        mWhitenSeekBar.setProgress(valueToProgress(value));
        mWhitenValue.setText(String.valueOf(value));

        value = application().config().cheekValue();
        mCheekSeekBar.setProgress(valueToProgress(value));
        mCheekValue.setText(String.valueOf(value));

        value = application().config().eyeValue();
        mEyeSeekBar.setProgress(valueToProgress(value));
        mEyeValue.setText(String.valueOf(value));

        mBeautySwitch = findViewById(R.id.beauty_switch);
        mBeautySwitch.setOnClickListener(this);
        mBeautySwitch.setActivated(application().config().isBeautyEnabled());

        enableOptions(application().config().isBeautyEnabled());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.beauty_switch == id) {
            boolean activated = !mBeautySwitch.isActivated();
            mBeautySwitch.setActivated(activated);
            application().config().setBeautyEnabled(activated);
            if (mListener != null) {
                mListener.onActionSheetBeautyEnabled(activated);

                // If the beauty is disabled, all the options will not
                // be changed using any UI components
                enableOptions(application().config().isBeautyEnabled());
            }
        }
    }

    private void enableOptions(boolean enabled) {
        mBlurSeekBar.setEnabled(enabled);
        mWhitenSeekBar.setEnabled(enabled);
        mCheekSeekBar.setEnabled(enabled);
        mEyeSeekBar.setEnabled(enabled);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float value = progressToValue(seekBar.getProgress());
        switch (seekBar.getId()) {
            case R.id.beauty_blur_progress_bar:
                mBlurValue.setText(String.valueOf(value));
                application().config().setBlurValue(value);
                if (mListener != null) mListener.onActionSheetBlurSelected(value);
                break;
            case R.id.beauty_whiten_progress_bar:
                mWhitenValue.setText(String.valueOf(value));
                application().config().setWhitenValue(value);
                if (mListener != null) mListener.onActionSheetWhitenSelected(value);
                break;
            case R.id.beauty_cheek_progress_bar:
                mCheekValue.setText(String.valueOf(value));
                application().config().setCheekValue(value);
                if (mListener != null) mListener.onActionSheetCheekSelected(value);
                break;
            case R.id.beauty_eye_progress_bar:
                mEyeValue.setText(String.valueOf(value));
                application().config().setEyeValue(value);
                if (mListener != null) mListener.onActionSheetEyeEnlargeSelected(value);
                break;
        }
    }

    private int valueToProgress(float value) {
        return (int) (value * 10);
    }

    private float progressToValue(int progress) {
        return progress / 10.0f;
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof BeautyActionSheetListener) {
            mListener = (BeautyActionSheetListener) listener;
        }
    }
}
