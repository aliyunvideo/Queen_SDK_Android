package im.zego.Scenes.VideoForMultipleUsers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import im.zego.R;
import im.zego.commontools.logtools.AppLogger;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;

public class VideoForMultipleUsersLogin extends AppCompatActivity {

    EditText editRoomID;
    EditText editUserID;
    EditText editUserName;
    Spinner encodeResolutionSpinner;
    EditText editFps;
    Spinner bitrateSpinner;
    Button loginRoomButton;

    ZegoExpressEngine engine;
    ZegoVideoConfig config;
    long appID;
    String userID;
    String token;
    String roomID = "0004";
    String userName;
    ZegoUser user;
    int fps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_for_multiple_users_login);

        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        requestPermission();
        setEncodeResolutionSpinnerEvent();
        setBitrateSpinnerEvent();
        setLoginRoomButtonEvent();
    }
    public void bindView(){
        editRoomID = findViewById(R.id.editRoomID);
        editUserID = findViewById(R.id.editUserID);
        editUserName = findViewById(R.id.editUserName);
        encodeResolutionSpinner = findViewById(R.id.encodeResolutionSpinner);
        editFps = findViewById(R.id.editFps);
        bitrateSpinner = findViewById(R.id.bitrateSpinner);
        loginRoomButton = findViewById(R.id.loginRoomButton);
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
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void setDefaultValue(){
        userName = ("Android_" + Build.MODEL).replaceAll(" ", "_");
        //create the user
        user = new ZegoUser(userID, userName);
        // set default configuration
        config = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_180P);
        editUserID.setText(userID);
        editUserID.setEnabled(false);
        editUserName.setText(userName);
        setTitle(getString(R.string.video_for_multiple_users));
    }
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
    }
    public void setEncodeResolutionSpinnerEvent(){
        encodeResolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] option = getResources().getStringArray(R.array.resolution);
                switch (option[position]){
                    case "180x320":
                        config.setEncodeResolution(180,320);
                        break;
                    case "270x480":
                        config.setEncodeResolution(270,480);
                        break;
                    case "360x640":
                        config.setEncodeResolution(360,640);
                        break;
                    case "540x960":
                        config.setEncodeResolution(540,960);
                        break;
                    case "720x1280":
                        config.setEncodeResolution(720,1280);
                        break;
                    case "1080x1920":
                        config.setEncodeResolution(1080,1920);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setBitrateSpinnerEvent(){
        bitrateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] option = getResources().getStringArray(R.array.bitrate);
                switch (option[position]){
                    case "300kbps":
                        config.setVideoBitrate(300);
                        break;
                    case "400kbps":
                        config.setVideoBitrate(400);
                        break;
                    case "600kbps":
                        config.setVideoBitrate(600);
                        break;
                    case "1200kbps":
                        config.setVideoBitrate(1200);
                        break;
                    case "1500kbps":
                        config.setVideoBitrate(1500);
                        break;
                    case "3000kbps":
                        config.setVideoBitrate(3000);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void setLoginRoomButtonEvent(){
        loginRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = editUserID.getText().toString();
                roomID = editRoomID.getText().toString();
                userName = editUserName.getText().toString();
                if (editFps.getText().toString().equals(""))
                {
                    Toast.makeText(VideoForMultipleUsersLogin.this, "FPS cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    fps = Integer.parseInt(editFps.getText().toString());
                } catch (NumberFormatException e)
                {
                    Toast.makeText(VideoForMultipleUsersLogin.this, "FPS is too large", Toast.LENGTH_SHORT).show();
                    return;
                }
                initEngineAndUser();

                config.setVideoFPS(fps);
                engine.setVideoConfig(config);
                loginRoom();

                Intent temp= new Intent(VideoForMultipleUsersLogin.this,VideoForMultipleUsersActivity.class);
                temp.putExtra("userName",userName);
                temp.putExtra("userID",userID);
                temp.putExtra("roomID",roomID);
                startActivity(temp);
            }
        });
    }
    public void loginRoom(){
        ZegoRoomConfig RoomConfig = new ZegoRoomConfig();
        //enable the user status notification
        RoomConfig.isUserStatusNotify = true;
        RoomConfig.token = token;
        user.userID = userID;
        user.userName = userName;
        //login room
        engine.loginRoom(roomID, user, RoomConfig);
        AppLogger.getInstance().callApi("Login Room:%s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity,VideoForMultipleUsersLogin.class);
        activity.startActivity(intent);
    }
}