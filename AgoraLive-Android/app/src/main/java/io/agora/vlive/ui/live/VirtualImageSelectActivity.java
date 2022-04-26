package io.agora.vlive.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import io.agora.vlive.R;
import io.agora.vlive.ui.BaseActivity;
import io.agora.vlive.utils.Global;

public class VirtualImageSelectActivity extends BaseActivity implements View.OnClickListener {
    public static final int PREPARE_REQUEST_CODE = 1;
    public static final String KEY_FROM_VIRTUAL_IMAGE = "from-virtual-image";

    private static final int AUDIENCE_RESULT_CODE = 2;

    private AppCompatImageView mSelectedImage;
    private RelativeLayout mDogLayout;
    private RelativeLayout mGirlLayout;

    private int mSelected;
    private boolean mFromAudience;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.virtual_image_activity);
        mFromAudience = getIntent().getBooleanExtra(
                Global.Constants.KEY_AUDIENCE_VIRTUAL_IMAGE, false);
        mSelectedImage = findViewById(R.id.selected_virtual_image_view);
        mDogLayout = findViewById(R.id.virtual_image_dog_layout);
        mGirlLayout = findViewById(R.id.virtual_image_girl_layout);
        mDogLayout.setOnClickListener(this);
        mGirlLayout.setOnClickListener(this);
        setSelectedImage();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout topLayout = findViewById(R.id.select_virtual_image_top_layout);
        RelativeLayout.LayoutParams params;
        params = (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);

        int contentTop = params.topMargin + params.height;
        int contentHeight = displayHeight - contentTop;
        int base = contentHeight / 8;

        contentHeight = base * 6;
        contentTop = contentHeight / 8;
        int optionHeight = (contentHeight * 3 / 4 - 112) / 3;
        int selectedImageHeight = optionHeight * 2;

        int idealMaxWidth = displayWidth * 3 / 4;
        int finalSelectedImageWidth = Math.min(selectedImageHeight, idealMaxWidth);
        int finalSelectedImageHeight = finalSelectedImageWidth;

        params = (RelativeLayout.LayoutParams) mSelectedImage.getLayoutParams();
        params.width = finalSelectedImageWidth;
        params.height = finalSelectedImageHeight;
        params.topMargin += contentTop;
        mSelectedImage.setLayoutParams(params);

        LinearLayout optionLayout = findViewById(R.id.virtual_selected_option_layout);
        params = (RelativeLayout.LayoutParams) optionLayout.getLayoutParams();
        params.height = optionHeight;
        optionLayout.setLayoutParams(params);

        LinearLayout.LayoutParams linearParams =
                (LinearLayout.LayoutParams) mDogLayout.getLayoutParams();
        linearParams.width = optionHeight;
        linearParams.height = optionHeight;
        mDogLayout.setLayoutParams(linearParams);

        linearParams = (LinearLayout.LayoutParams) mGirlLayout.getLayoutParams();
        linearParams.width = optionHeight;
        linearParams.height = optionHeight;
        mGirlLayout.setLayoutParams(linearParams);

        int optionImageSize = optionHeight * 3 / 4;
        AppCompatImageView optionDog = findViewById(R.id.virtual_image_dog);
        params = (RelativeLayout.LayoutParams) optionDog.getLayoutParams();
        params.width = optionImageSize;
        params.height = optionImageSize;
        optionDog.setLayoutParams(params);

        AppCompatImageView optionGirl = findViewById(R.id.virtual_image_girl);
        params = (RelativeLayout.LayoutParams) optionGirl.getLayoutParams();
        params.width = optionImageSize;
        params.height = optionImageSize;
        optionGirl.setLayoutParams(params);
    }

    public void onImageSelected(View view) {
        Intent intent = new Intent(getIntent());
        intent.putExtra(Global.Constants.KEY_VIRTUAL_IMAGE, mSelected);
        if (intent.getBooleanExtra(Global.Constants.KEY_CREATE_ROOM, false)) {
            intent.setClass(getApplicationContext(), LivePrepareActivity.class);
            intent.putExtra(KEY_FROM_VIRTUAL_IMAGE, true);
            startActivityForResult(intent, PREPARE_REQUEST_CODE);
        } else if (mFromAudience) {
            setResult(AUDIENCE_RESULT_CODE, intent);
            finish();
        }
    }

    public void onCloseClicked(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.virtual_image_dog_layout) {
            mSelected = 0;
            setSelectedImage();
        } else if (id == R.id.virtual_image_girl_layout) {
            mSelected = 1;
            setSelectedImage();
        }
    }

    private void setSelectedImage() {
        if (mSelected == 0) {
            mSelectedImage.setImageResource(R.drawable.virtual_image_dog);
            mDogLayout.setBackgroundResource(R.drawable.virtual_image_option_bg_selected);
            mGirlLayout.setBackgroundResource(R.drawable.virtual_image_option_bg_normal);
        } else if (mSelected == 1) {
            mSelectedImage.setImageResource(R.drawable.virtual_image_girl);
            mDogLayout.setBackgroundResource(R.drawable.virtual_image_option_bg_normal);
            mGirlLayout.setBackgroundResource(R.drawable.virtual_image_option_bg_selected);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREPARE_REQUEST_CODE &&
            resultCode == LivePrepareActivity.RESULT_GO_LIVE) {
            finish();
        }
    }
}
