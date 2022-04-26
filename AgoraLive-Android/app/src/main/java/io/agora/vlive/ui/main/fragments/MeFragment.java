package io.agora.vlive.ui.main.fragments;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.ui.profile.AboutActivity;
import io.agora.vlive.ui.profile.ModifyUserNameActivity;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.UserUtil;

public class MeFragment extends AbstractFragment implements View.OnClickListener {
    private static final String TAG = MeFragment.class.getSimpleName();
    private static final int USER_NAME_REQUEST = 1;

    private View mLayout;
    private AppCompatTextView mNameText;
    private AppCompatTextView mProfileTitleNameText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Config.UserProfile profile = config().getUserProfile();
        mLayout = inflater.inflate(R.layout.fragment_me, container, false);
        setUserIcon(mLayout.findViewById(R.id.user_profile_icon));

        mNameText = mLayout.findViewById(R.id.edit_profile_nickname);
        mNameText.setText(profile.getUserName());

        mProfileTitleNameText = mLayout.findViewById(R.id.user_profile_nickname);
        mProfileTitleNameText.setText(profile.getUserName());

        mLayout.findViewById(R.id.user_profile_nickname_setting_layout).setOnClickListener(this);
        mLayout.findViewById(R.id.user_profile_icon_setting_layout).setOnClickListener(this);
        mLayout.findViewById(R.id.user_profile_about_layout).setOnClickListener(this);
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RelativeLayout titleLayout = mLayout.findViewById(R.id.profile_title_layout);
        if (titleLayout != null) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) titleLayout.getLayoutParams();
            int systemBarHeight = getContainer().getSystemBarHeight();
            params.topMargin += systemBarHeight;
            titleLayout.setLayoutParams(params);
        }
    }

    private void setUserIcon(AppCompatImageView imageView) {
        Config.UserProfile profile = getContainer().config().getUserProfile();
        Drawable saved = profile.getProfileIcon();
        RoundedBitmapDrawable drawable =
                saved instanceof RoundedBitmapDrawable ? (RoundedBitmapDrawable) saved : null;

        if (drawable == null) {
            drawable = RoundedBitmapDrawableFactory.create(getResources(),
                    BitmapFactory.decodeResource(getResources(),
                    UserUtil.getUserProfileIcon(profile.getUserId())));
            drawable.setCircular(true);
            profile.setProfileIcon(drawable);
        }

        imageView.setImageDrawable(drawable);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_profile_nickname_setting_layout:
                goEditUserNameActivity();
                break;
            case R.id.user_profile_icon_setting_layout:
                break;
            case R.id.user_profile_about_layout:
                gotoAboutActivity();
                break;
        }
    }

    private void goEditUserNameActivity() {
        Intent intent = new Intent(getContext(), ModifyUserNameActivity.class);
        intent.putExtra(Global.Constants.KEY_USER_NAME, config().getUserProfile().getUserName());
        startActivityForResult(intent, USER_NAME_REQUEST);
    }

    private void gotoAboutActivity() {
        Intent intent = new Intent(getContext(), AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == USER_NAME_REQUEST) {
            if (resultCode == Global.Constants.EDIT_USER_NAME_RESULT_DONE) {
                String name = data == null ? "" : data.getStringExtra(Global.Constants.KEY_USER_NAME);
                config().getUserProfile().setUserName(name);
                mNameText.setText(name);
                mProfileTitleNameText.setText(name);
            }
        }
    }
}
