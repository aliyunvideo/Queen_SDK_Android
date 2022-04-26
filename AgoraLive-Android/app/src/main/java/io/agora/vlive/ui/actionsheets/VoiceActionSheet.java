package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import io.agora.vlive.R;

public class VoiceActionSheet extends AbstractActionSheet implements View.OnClickListener {
    public interface VoiceActionSheetListener extends AbsActionSheetListener {
        void onActionSheetAudioRouteSelected(int type);
        void onActionSheetAudioRouteEnabled(boolean enabled);
        void onActionSheetAudioBackPressed();
    }

    public static final int VOICE_UNKNOWN = -1;
    public static final int VOICE_HEADPHONE = 0;
    public static final int VOICE_LOUDSPEAKER = 1;
    public static final int VOICE_OTHERS = 2;

    private VoiceActionSheetListener mListener;

    private View mVoiceHeadphone;
    private View mVoiceLoudspeaker;
    private View mVoiceOthers;

    public VoiceActionSheet(Context context) {
        super(context);
        init();
    }

    public VoiceActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceActionSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.action_voice, this, true);
        findViewById(R.id.live_room_voice_back).setOnClickListener(this);
        findViewById(R.id.voice_switch).setOnClickListener(this);

        mVoiceHeadphone = findViewById(R.id.voice_headphone);
        mVoiceLoudspeaker = findViewById(R.id.voice_loudspeaker);
        mVoiceOthers = findViewById(R.id.voice_others);

        mVoiceHeadphone.setOnClickListener(this);
        mVoiceLoudspeaker.setOnClickListener(this);
        mVoiceOthers.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_room_voice_back:
                if (mListener != null) mListener.onActionSheetAudioBackPressed();
                break;
            case R.id.voice_switch:
                view.setActivated(!view.isActivated());
                if (mListener != null) mListener.onActionSheetAudioRouteEnabled(view.isActivated());
                break;
            case R.id.voice_headphone:
            case R.id.voice_loudspeaker:
            case R.id.voice_others:
                mVoiceHeadphone.setActivated(view == mVoiceHeadphone);
                mVoiceLoudspeaker.setActivated(view == mVoiceLoudspeaker);
                mVoiceOthers.setActivated(view == mVoiceOthers);
                if (mListener != null) mListener.onActionSheetAudioRouteSelected(viewIdToIndex(view.getId()));
                break;
        }
    }

    private int viewIdToIndex(int viewId) {
        switch (viewId) {
            case R.id.voice_headphone:
                return VOICE_HEADPHONE;
            case R.id.voice_loudspeaker:
                return VOICE_LOUDSPEAKER;
            case R.id.voice_others:
                return VOICE_OTHERS;
            default: return VOICE_UNKNOWN;
        }
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof VoiceActionSheetListener) {
            mListener = (VoiceActionSheetListener) listener;
        }
    }
}
