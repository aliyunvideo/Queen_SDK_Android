package com.alivc.live.pusher.demo;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import com.alivc.live.pusher.AlivcLivePusher;

public class MusicDialog extends DialogFragment implements View.OnClickListener {

    private Button mPause;
    private Button mStop;
    private Button mLoop;
    private Button mMute;
    private Switch mEarsBack;
    private Switch mAudioDenoise;

    private TextView mAccompanimentText;
    private SeekBar mAccompanimentBar;

    private TextView mVoiceText;
    private SeekBar mVoiceBar;

    private RecyclerView mMusicRecyclerView;
    private MusicAdapter mMusicAdapter;

    private AlivcLivePusher mAlivcLivePusher = null;
    private boolean isPause = true;
    private boolean isStop = true;
    private boolean isLoop = true;
    private boolean isMute = false;
    private boolean isEarsBack = false;
    private boolean isAudioDenoise = false;

    private int mPosition = 0;
    private MusicAdapter.MusicInfo mMusicInfo = null;

    public static MusicDialog newInstance() {
        MusicDialog pushBeautyDialog = new MusicDialog();
        return pushBeautyDialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.push_music, container);
        mPause = (Button) view.findViewById(R.id.pause);
        mPause.setSelected(isPause);
        mPause.setText(isPause ? getString(R.string.pause) : getString(R.string.resume));
        mStop = (Button) view.findViewById(R.id.stop);
        mStop.setSelected(isStop);
        mStop.setText(isStop ? getString(R.string.stop) : getString(R.string.start));
        mLoop = (Button) view.findViewById(R.id.loop);
        mLoop.setSelected(isLoop);
        mLoop.setText(isLoop ? getString(R.string.close_loop) : getString(R.string.open_loop));
        mMute = (Button) view.findViewById(R.id.mute);
        mMute.setSelected(isMute);
        mMute.setText(isMute ? getString(R.string.close_mute) : getString(R.string.open_mute));
        mEarsBack = (Switch) view.findViewById(R.id.ears_back);
        mEarsBack.setOnCheckedChangeListener(onCheckedChangeListener);
        mAudioDenoise = (Switch) view.findViewById(R.id.audio_denoise);
        mPause.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mLoop.setOnClickListener(this);
        mMute.setOnClickListener(this);
        mAudioDenoise.setOnCheckedChangeListener(onCheckedChangeListener);
        mAccompanimentText = (TextView) view.findViewById(R.id.accompaniment_text);
        mAccompanimentBar = (SeekBar) view.findViewById(R.id.accompaniment_seekbar);
        mVoiceText = (TextView) view.findViewById(R.id.voice_text);
        mVoiceBar = (SeekBar) view.findViewById(R.id.voice_seekbar);

        mMusicRecyclerView = (RecyclerView) view.findViewById(R.id.music_list);
        if (mMusicAdapter == null) {
            mMusicAdapter = new MusicAdapter(getActivity());
            mMusicAdapter.setOnItemClick(mOnItemClick);
            mMusicAdapter.startDefaultMusic();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mMusicRecyclerView.setLayoutManager(layoutManager);
        mMusicRecyclerView.setAdapter(mMusicAdapter);
        mMusicRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.HORIZONTAL));

        mAccompanimentBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mVoiceBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        updateButton(mPosition > 0 ? true : false);

        return view;
    }

    @Override
    public void onResume() {
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();

        DisplayMetrics dpMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dpMetrics);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();

        p.width = dpMetrics.widthPixels;
        p.height = (int) ((dpMetrics.heightPixels / 2)*1.5);
        getDialog().getWindow().setAttributes(p);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (mAlivcLivePusher == null) {
            return;
        }
        try {
            switch (id) {
                case R.id.pause:
                    boolean pauseSelected = mPause.isSelected();
                    mPause.setText(pauseSelected ? getString(R.string.resume) : getString(R.string.pause));
                    mPause.setSelected(!pauseSelected);
                    isPause = !pauseSelected;
                    if (!pauseSelected) {
                        mAlivcLivePusher.resumeBGM();
                    } else {
                        mAlivcLivePusher.pauseBGM();
                    }

                    break;
                case R.id.stop:
                    boolean stopSelected = mStop.isSelected();
                    mStop.setText(stopSelected ? getString(R.string.start) : getString(R.string.stop));
                    mStop.setSelected(!stopSelected);
                    isStop = !stopSelected;
                    mPause.setEnabled(isStop);
                    mPause.setText(getString(R.string.pause));
                    mPause.setSelected(true);
                    if (!stopSelected) {
                        mAlivcLivePusher.startBGMAsync(mMusicInfo.getPath());
                    } else {
                        mAlivcLivePusher.stopBGMAsync();
                    }
                    break;
                case R.id.loop:
                    boolean loopSelected = mLoop.isSelected();
                    mAlivcLivePusher.setBGMLoop(!loopSelected);
                    mLoop.setText(loopSelected ? getString(R.string.open_loop) : getString(R.string.close_loop));
                    mLoop.setSelected(!loopSelected);
                    isLoop = !loopSelected;
                    if (mMusicAdapter != null) {
                        mMusicAdapter.updateItemView((MusicAdapter.MusicViewHolder) mMusicRecyclerView.findViewHolderForAdapterPosition(mPosition), mPosition, !loopSelected);
                    }
                    break;
                case R.id.mute:
                    boolean isSelect = mMute.isSelected();
                    mAlivcLivePusher.setMute(!isSelect);
                    if (mPosition == 0) {
                        mAccompanimentBar.setEnabled(false);
                        mVoiceBar.setEnabled(true);
                    } else {
                        mAccompanimentBar.setEnabled(isSelect);
                        mVoiceBar.setEnabled(isSelect);
                    }

                    mMute.setText(isSelect ? getString(R.string.open_mute) : getString(R.string.close_mute));
                    mMute.setSelected(!isSelect);
                    isMute = !isSelect;
                    break;
                case R.id.ears_back:
                    boolean erasSelected = mEarsBack.isSelected();
                    mAlivcLivePusher.setBGMEarsBack(!erasSelected);
                    mEarsBack.setText(erasSelected ? getString(R.string.open_ears_back) : getString(R.string.close_ears_back));
                    mEarsBack.setSelected(!erasSelected);
                    isEarsBack = !erasSelected;
                    break;
                case R.id.audio_denoise:
                    boolean audioSelected = mAudioDenoise.isSelected();
                    mAlivcLivePusher.setAudioDenoise(!audioSelected);
                    mAudioDenoise.setText(audioSelected ? getString(R.string.open_audio_denoise) : getString(R.string.close_audio_denoise));
                    mAudioDenoise.setSelected(!audioSelected);
                    isAudioDenoise = !audioSelected;
                    break;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                int seekBarId = seekBar.getId();

                if (mAccompanimentBar.getId() == seekBarId) {
                    mAccompanimentText.setText(String.valueOf(progress));
                    mAlivcLivePusher.setBGMVolume(progress);
                } else if (mVoiceBar.getId() == seekBarId) {
                    mVoiceText.setText(String.valueOf(progress));
                    mAlivcLivePusher.setCaptureVolume(progress);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher) {
        this.mAlivcLivePusher = alivcLivePusher;
    }

    public interface OnItemClick {
        void onItemClick(MusicAdapter.MusicInfo musicInfo, int position);
    }

    private OnItemClick mOnItemClick = new OnItemClick() {
        @Override
        public void onItemClick(MusicAdapter.MusicInfo musicInfo, int position) {
            mMusicInfo = musicInfo;
            mPosition = position;
            updateButtonState(position > 0 ? true : false);
            if (musicInfo.getPath() != null && !musicInfo.getPath().isEmpty()) {
                startBGMAsync(musicInfo.getPath());
            } else {
                try {
                    mAlivcLivePusher.stopBGMAsync();
                } catch (IllegalStateException e) {

                }
            }
        }
    };

    private void updateButton(boolean bool) {
        mPause.setEnabled(bool);
        mStop.setEnabled(bool);
        mLoop.setEnabled(bool);
//        mMute.setEnabled(bool);

        if (mPosition == 0) {
            mAccompanimentBar.setEnabled(mMute.isSelected());
            mVoiceBar.setEnabled(!mMute.isSelected());
        } else {
            mAccompanimentBar.setEnabled(!mMute.isSelected());
            mVoiceBar.setEnabled(!mMute.isSelected());
        }
    }

    private void updateButtonState(boolean bool) {
        updateButton(bool);
        mPause.setText(getString(R.string.pause));
        mStop.setText(getString(R.string.stop));
        mPause.setSelected(true);
        mStop.setSelected(true);
    }

    public void updateProgress(long progress, long totalTime) {
        if (mMusicAdapter != null) {
            mMusicAdapter.updateProgress((MusicAdapter.MusicViewHolder) mMusicRecyclerView.findViewHolderForAdapterPosition(mPosition), progress, totalTime);
        }
    }

    private void startBGMAsync(String path) {
        if (mAlivcLivePusher != null) {
            try {
                mAlivcLivePusher.startBGMAsync(path);
            } catch (IllegalStateException e) {

            }
        }
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.ears_back) {
                mAlivcLivePusher.setBGMEarsBack(isChecked);
            } else if (id == R.id.audio_denoise) {
                mAlivcLivePusher.setAudioDenoise(isChecked);
            }
        }
    };
}
