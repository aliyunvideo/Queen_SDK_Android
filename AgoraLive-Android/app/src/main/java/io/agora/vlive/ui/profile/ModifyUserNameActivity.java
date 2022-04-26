package io.agora.vlive.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.UserRequest;
import io.agora.vlive.protocol.model.response.EditUserResponse;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.utils.Global;

public class ModifyUserNameActivity extends BaseActivity
        implements View.OnClickListener, TextWatcher {
    private static final int MAX_NAME_LENGTH = 15;

    private AppCompatTextView mDoneBtn;
    private AppCompatEditText mNameEditText;
    private String mNewName;
    private String mOldName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.modify_user_name_activity);
        init();
    }

    private void init() {
        findViewById(R.id.modify_user_name_cancel_btn).setOnClickListener(this);
        mDoneBtn = findViewById(R.id.modify_user_name_confirm_btn);
        mNameEditText = findViewById(R.id.modify_user_name_edit_text);
        mDoneBtn.setOnClickListener(this);
        mNameEditText.addTextChangedListener(this);

        mOldName = getIntent().getStringExtra(Global.Constants.KEY_USER_NAME);
        mNameEditText.setText(mOldName);
        mDoneBtn.setEnabled(mNameEditText.length() > 0);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.modify_user_name_title_layout);
        if (topLayout != null) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.modify_user_name_confirm_btn) {
            if (TextUtils.isEmpty(mNameEditText.getText())) {
                showShortToast(getResources().getString(R.string.modify_user_name_empty));
                return;
            }

            mNewName = mNameEditText.getText().toString();
            if (!mNewName.equals(mOldName)) {
                UserRequest request = new UserRequest(config().getUserProfile().getToken(),
                        config().getUserProfile().getUserId(), mNewName);
                sendRequest(Request.EDIT_USER, request);
            } else {
                finish();
            }
        } else if (v.getId() == R.id.modify_user_name_cancel_btn) {
            setResult(Global.Constants.EDIT_USER_NAME_RESULT_CANCEL);
            finish();
        }
    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {
        if (response.data) {
            Intent data = new Intent();
            data.putExtra(Global.Constants.KEY_USER_NAME, mNewName);
            setResult(Global.Constants.EDIT_USER_NAME_RESULT_DONE, data);
            config().getUserProfile().setUserName(mNewName);
            preferences().edit().putString(Global.Constants.KEY_USER_NAME, mNewName).apply();
            finish();
        } else {
            showShortToast(getResources().getString(R.string.modify_user_name_error));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(@NonNull Editable s) {
        if (s.length() <= 0) {
            mDoneBtn.setEnabled(false);
        } else if (s.length() > MAX_NAME_LENGTH) {
            mNameEditText.setText(s.subSequence(0, MAX_NAME_LENGTH));
            mNameEditText.setSelection(MAX_NAME_LENGTH);
            showShortToast(getResources().getString(R.string.modify_user_name_too_long));
        } else {
            mDoneBtn.setEnabled(true);
        }
    }
}
