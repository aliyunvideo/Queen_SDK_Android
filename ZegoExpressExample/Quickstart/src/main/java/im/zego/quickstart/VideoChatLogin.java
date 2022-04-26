package im.zego.quickstart;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

import im.zego.keycenter.KeyCenter;

public class VideoChatLogin extends AppCompatActivity {

    EditText roomIDEdit;
    EditText userIDEdit;
    EditText publishStreamIDEdit;
    Button loginRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_login);
        bindView();
        requestPermission();
        setDefaultValue();
        setLoginRoomButtonEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, VideoChatLogin.class);
        activity.startActivity(intent);
    }

    public void bindView() {
        roomIDEdit = findViewById(R.id.editRoomID);
        userIDEdit = findViewById(R.id.editUserID);
        publishStreamIDEdit = findViewById(R.id.editStreamID);
        loginRoomButton = findViewById(R.id.loginRoomButton);
    }

    // Request for permission
    public void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
            }
        }
    }

    public void setDefaultValue() {
        setTitle(R.string.video_chat_login);

        String userID = KeyCenter.getInstance().getUserID();
        roomIDEdit.setText("0001");
        userIDEdit.setText(userID);
        userIDEdit.setEnabled(false);
        publishStreamIDEdit.setText("s_" + userID);
    }

    public void setLoginRoomButtonEvent() {
        loginRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),VideoChat.class);
                intent.putExtra("roomID", roomIDEdit.getText().toString());
                intent.putExtra("userID", userIDEdit.getText().toString());
                intent.putExtra("publishStreamID", publishStreamIDEdit.getText().toString());
                startActivity(intent);
            }
        });
    }
}
