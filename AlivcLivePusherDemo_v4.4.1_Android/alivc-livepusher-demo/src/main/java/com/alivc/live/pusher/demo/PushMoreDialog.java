package com.alivc.live.pusher.demo;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcQualityModeEnum;

public class PushMoreDialog extends DialogFragment implements View.OnClickListener{

    private Button mShare;
    private Button mDynamicAdd;
    private Button mDynamicRemove;
    private EditText mTargetRate = null;
    private EditText mMinRate = null;

    private Switch mAutoFocus;
    private Switch mPushMirror;
    private Switch mPreviewMirror;
    private RadioGroup mPreviewMode;

    private AlivcLivePusher mAlivcLivePusher = null;
    private LivePushFragment.DynamicListern dynamicListern = null;
    private String mPushUrl = "";

    private int mQualityMode = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.push_more, container);
        mShare = (Button) view.findViewById(R.id.share);
        mDynamicAdd = (Button) view.findViewById(R.id.dynamic_add);
        mDynamicRemove = (Button) view.findViewById(R.id.dynamic_remove);
        mShare.setOnClickListener(this);
        mDynamicAdd.setOnClickListener(this);
        mDynamicRemove.setOnClickListener(this);
        mTargetRate = (EditText) view.findViewById(R.id.target_rate_edit);
        mMinRate = (EditText) view.findViewById(R.id.min_rate_edit);

        mPushMirror = (Switch) view.findViewById(R.id.push_mirror_switch);
        mPreviewMirror = (Switch) view.findViewById(R.id.preview_mirror_switch);
        mPreviewMode = (RadioGroup) view.findViewById(R.id.setting_display_mode);
        mAutoFocus = (Switch) view.findViewById(R.id.autofocus_switch);
        mPushMirror.setChecked(SharedPreferenceUtils.isPushMirror(getActivity().getApplicationContext()));
        mPreviewMirror.setChecked(SharedPreferenceUtils.isPreviewMirror(getActivity().getApplicationContext()));
        mAutoFocus.setChecked(SharedPreferenceUtils.isAutoFocus(getActivity().getApplicationContext()));
        mPushMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mPreviewMirror.setOnCheckedChangeListener(onCheckedChangeListener);
        mAutoFocus.setOnCheckedChangeListener(onCheckedChangeListener);

        mPreviewMode.setOnCheckedChangeListener(mDisplayModeListener);
        mTargetRate.setText(String.valueOf(SharedPreferenceUtils.getTargetBit(getActivity().getApplicationContext())));
        mMinRate.setText(String.valueOf(SharedPreferenceUtils.getMinBit(getActivity().getApplicationContext())));

        mTargetRate.setHint(String.valueOf(SharedPreferenceUtils.getHintTargetBit(getActivity().getApplicationContext())));
        mMinRate.setHint(String.valueOf(SharedPreferenceUtils.getHintMinBit(getActivity().getApplicationContext())));
        if(mQualityMode != AlivcQualityModeEnum.QM_CUSTOM.getQualityMode())
        {
            mTargetRate.setFocusable(false);
            mMinRate.setFocusable(false);
            mTargetRate.setFocusableInTouchMode(false);
            mMinRate.setFocusableInTouchMode(false);
            mMinRate.setTextColor(Color.GRAY);
            mTargetRate.setTextColor(Color.GRAY);
        } else {
            mMinRate.setBackgroundColor(Color.WHITE);
            mTargetRate.setBackgroundColor(Color.WHITE);
            mTargetRate.setFocusable(true);
            mMinRate.setFocusable(true);
            mTargetRate.setFocusableInTouchMode(true);
            mMinRate.setFocusableInTouchMode(true);
            mTargetRate.requestFocus();
            mMinRate.requestFocus();
        }
        if(SharedPreferenceUtils.getDisplayFit(getActivity().getApplicationContext()) == AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL

            .getPreviewDisplayMode()) {
            mPreviewMode.check(R.id.full);
        } else if(SharedPreferenceUtils.getDisplayFit(getActivity().getApplicationContext()) == AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT

            .getPreviewDisplayMode()) {
            mPreviewMode.check(R.id.fit);
        } else if(SharedPreferenceUtils.getDisplayFit(getActivity().getApplicationContext()) == AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL

            .getPreviewDisplayMode()) {
            mPreviewMode.check(R.id.cut);
        }
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
//        p.height = dpMetrics.heightPixels/2;
        getDialog().getWindow().setAttributes(p);
    }

    public void setAlivcLivePusher(AlivcLivePusher alivcLivePusher, LivePushFragment.DynamicListern listern) {
        this.mAlivcLivePusher = alivcLivePusher;
        this.dynamicListern = listern;
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (mAlivcLivePusher == null) {
                if(getActivity() != null) {
                    mAlivcLivePusher = ((LivePushActivity)getActivity()).getLivePusher();
                }

                if(mAlivcLivePusher == null) {
                    return;
                }
            }

            try{
                if(id == R.id.push_mirror_switch) {
                    mAlivcLivePusher.setPushMirror(isChecked);
                    SharedPreferenceUtils.setPushMirror(getActivity().getApplicationContext(), isChecked);
                } else if(id == R.id.preview_mirror_switch) {
                    mAlivcLivePusher.setPreviewMirror(isChecked);
                    SharedPreferenceUtils.setPreviewMirror(getActivity().getApplicationContext(), isChecked);
                } else if(id == R.id.autofocus_switch) {
                    mAlivcLivePusher.setAutoFocus(isChecked);
                    SharedPreferenceUtils.setAutofocus(getActivity().getApplicationContext(), isChecked);
                }
            } catch (IllegalStateException e) {
                Common.showDialog(getActivity(), e.getMessage());
            }

        }
    };

    private RadioGroup.OnCheckedChangeListener mDisplayModeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (mAlivcLivePusher == null) {
                if(getActivity() != null) {
                    mAlivcLivePusher = ((LivePushActivity)getActivity()).getLivePusher();
                }

                if(mAlivcLivePusher == null) {
                    return;
                }
            }

            switch (i) {
                case R.id.full:
                    mAlivcLivePusher.setPreviewMode(AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL);
                    SharedPreferenceUtils.setDisplayFit(getActivity().getApplicationContext(),
                        AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL
                        .getPreviewDisplayMode());
                    break;
                case R.id.fit:
                    mAlivcLivePusher.setPreviewMode(AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT);
                    SharedPreferenceUtils.setDisplayFit(getActivity().getApplicationContext(),
                        AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT
                        .getPreviewDisplayMode());
                    break;
                case R.id.cut:
                    mAlivcLivePusher.setPreviewMode(AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL);
                    SharedPreferenceUtils.setDisplayFit(getActivity().getApplicationContext(),
                        AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL
                        .getPreviewDisplayMode());
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            int targetRate = -1;
            int minRate = -1;
            boolean isRight = true;

            if(!mTargetRate.getText().toString().isEmpty()) {
                targetRate = Integer.valueOf(mTargetRate.getText().toString());
            } else {
                targetRate = Integer.valueOf(mTargetRate.getHint().toString());
            }

            SharedPreferenceUtils.setTargetBit(getActivity().getApplicationContext(), targetRate);
            if(!mMinRate.getText().toString().isEmpty()) {
                minRate = Integer.valueOf(mMinRate.getText().toString());
            } else {
                minRate = Integer.valueOf(mMinRate.getHint().toString());
            }

            SharedPreferenceUtils.setMinBit(getActivity().getApplicationContext(), minRate);
            if(targetRate != -1) {
                if(targetRate < 100 || targetRate > 5000) {
                    isRight = false;
                }
            }

            if(minRate != -1) {
                if(minRate < 100 || targetRate > 5000) {
                    isRight = false;
                }
            }

            if(minRate != 1 && targetRate != 1) {
                if(minRate > targetRate) {
                    isRight = false;
                }
            }

            if(isRight) {
                if(targetRate != -1) {
                    mAlivcLivePusher.setTargetVideoBitrate(targetRate);
                }
                if(minRate != -1) {
                    mAlivcLivePusher.setMinVideoBitrate(minRate);
                }
            } else {
                Common.showDialog(getActivity(), getString(R.string.bite_error));
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.dynamic_add:
                if(dynamicListern != null) {
                    dynamicListern.onAddDynamic();
                }
                break;
            case R.id.dynamic_remove:
                if(dynamicListern != null) {
                    dynamicListern.onRemoveDynamic();
                }
                break;
            default:
                break;
        }
    }

    public void setPushUrl(String url) {
        this.mPushUrl = url;
    }

    public void setQualityMode(int mode) {
        this.mQualityMode = mode;
        if(mTargetRate == null || mMinRate == null)
        {
            return;
        }
        if(mQualityMode != AlivcQualityModeEnum.QM_CUSTOM.getQualityMode())
        {
            mTargetRate.setFocusable(false);
            mMinRate.setFocusable(false);
            mTargetRate.setFocusableInTouchMode(false);
            mMinRate.setFocusableInTouchMode(false);
            mMinRate.setBackgroundColor(Color.GRAY);
            mTargetRate.setBackgroundColor(Color.GRAY);
        } else {
            mMinRate.setBackgroundColor(Color.WHITE);
            mTargetRate.setBackgroundColor(Color.WHITE);
            mTargetRate.setFocusable(true);
            mMinRate.setFocusable(true);
            mTargetRate.setFocusableInTouchMode(true);
            mMinRate.setFocusableInTouchMode(true);
            mTargetRate.requestFocus();
            mMinRate.requestFocus();
        }
    }
}
