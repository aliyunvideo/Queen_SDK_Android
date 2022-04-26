package io.agora.vlive.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;
import io.agora.vlive.ui.BaseActivity;

public class AboutActivity extends BaseActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        hideStatusBar(true);

        findViewById(R.id.about_privacy_layout).setOnClickListener(this);
        findViewById(R.id.about_terms).setOnClickListener(this);
        findViewById(R.id.about_disclaimer_layout).setOnClickListener(this);
        findViewById(R.id.about_sign_up_layout).setOnClickListener(this);
        findViewById(R.id.about_activity_close).setOnClickListener(this);

        setVersionText();
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.activity_about_title_layout);
        if (topLayout != null) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams)
                            topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
        }
    }

    private void setVersionText() {
        AppCompatTextView agoraLiveVersionText = findViewById(R.id.agoralive_version_text);
        String versionText = "Ver " + getAppVersion();
        agoraLiveVersionText.setText(versionText);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.about_privacy_layout:
                String link = getString(R.string.privacy_website_link);
                Uri uri = Uri.parse(link);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case R.id.about_terms:
                link = getString(R.string.terms_service_link);
                uri = Uri.parse(link);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case R.id.about_disclaimer_layout:
                intent = new Intent(this, DisclaimerActivity.class);
                break;
            case R.id.about_sign_up_layout:
                link = getString(R.string.sign_up_website_link);
                uri = Uri.parse(link);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case R.id.about_activity_close:
                finish();
                return;
        }

        startActivity(intent);
    }
}
