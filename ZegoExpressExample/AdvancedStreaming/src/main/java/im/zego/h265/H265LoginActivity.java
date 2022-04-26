package im.zego.h265;

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
import im.zego.streammonitoring.R;

public class H265LoginActivity extends AppCompatActivity {

    EditText roomIDEdit;
    EditText userIDEdit;
    Button loginRoomButton;

    String roomID;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h265_login);
        bindView();
        requestPermission();
        setDefaultValue();
        setLoginRoomButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void bindView(){
        roomIDEdit = findViewById(R.id.roomIDEditText);
        userIDEdit = findViewById(R.id.userIDEditText);
        loginRoomButton = findViewById(R.id.loginRoomButton);
    }

    public void setDefaultValue(){
        Random random = new Random();
        //set default roomID
        roomID = "H265_Room";
        //set default userID
        userID = KeyCenter.getInstance().getUserID();

        roomIDEdit.setText(roomID);
        userIDEdit.setText(userID);
        userIDEdit.setEnabled(false);
        setTitle(getString(R.string.h265));
    }

    public void setLoginRoomButton(){
        loginRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),H265Activity.class);
                intent.putExtra("roomID",roomIDEdit.getText().toString());
                intent.putExtra("userID",userIDEdit.getText().toString());
                startActivity(intent);
            }
        });
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, H265LoginActivity.class);
        activity.startActivity(intent);
    }

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
}
