package com.example.others.mediaplayer.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.others.R;
import com.example.others.mediaplayer.adapter.MainAdapter;
import com.example.others.mediaplayer.entity.ModuleInfo;
import com.example.others.recording.RecordingActivity;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;

public class MediaPlayerSelectionActivity extends AppCompatActivity {

    RecyclerView mediaList;
    TextView userIDEdit;
    EditText urlEidt;
    Button enterButton;

    MainAdapter mainAdapter = new MainAdapter();

    String userID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    final List<String> fileNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_selection_player);
        bindView();
        initData();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        initEngineAndUser();
        loginRoom();
        setUI();
        setItem();
        setLogComponent();
    }
    public void bindView(){
        mediaList = findViewById(R.id.mediaList);
        userIDEdit = findViewById(R.id.userIDEditText);
        urlEidt = findViewById(R.id.urlInput);
        enterButton = findViewById(R.id.enterButton);
    }
    public void setDefaultValue(){
        roomID = "0027";
        userIDEdit.setText(userID);
        userIDEdit.setEnabled(false);
        setTitle(getString(R.string.media_player));
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void initEngineAndUser(){
        // Initialize ZegoExpressEngine
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.GENERAL;
        profile.application = getApplication();
        engine = ZegoExpressEngine.createEngine(profile, null);

        AppLogger.getInstance().callApi("Create ZegoExpressEngine");
        //create the user
        user = new ZegoUser(userID);
    }
    public void loginRoom(){
        //login room
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        engine.loginRoom(roomID, user, config);
        AppLogger.getInstance().callApi("LoginRoom: %s",roomID);
        //enable the camera
        engine.enableCamera(true);
        //enable the microphone
        engine.muteMicrophone(false);
        //enable the speaker
        engine.muteSpeaker(false);
    }
    public void setUI(){
        mainAdapter.setOnItemClickListener((view, position) -> {
            ModuleInfo moduleInfo = (ModuleInfo) view.getTag();
            String module = moduleInfo.getModule();
            if (module.equals(getString(R.string.sample))) {
                startMediaPlayer( getExternalFilesDir("").getPath()+"/sample.mp3", false);
            } else if (module.equals(getString(R.string.test))){
                startMediaPlayer(getExternalFilesDir("").getPath()+"/test.wav", false);
            } else  if (module.equals(getString(R.string.ad))){
                startMediaPlayer(getExternalFilesDir("").getPath()+"/ad.mp4", true);
            } else if (module.equals(getString(R.string.sample_bgm))){
                startMediaPlayer("https://storage.zego.im/demo/sample_astrix.mp3", false);
            } else if (module.equals(getString(R.string.sample_network))){
                startMediaPlayer("https://storage.zego.im/demo/201808270915.mp4", true);
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resourceURL = urlEidt.getText().toString();
                startMediaPlayer(resourceURL, true);
            }
        });
    }
    public void setItem(){
        mainAdapter.addModuleInfo(new ModuleInfo().moduleName(getString(R.string.sample_bgm)).titleName(getString(R.string.network_resource)));
        mainAdapter.addModuleInfo(new ModuleInfo().moduleName(getString(R.string.sample_network)));
        mainAdapter.addModuleInfo(new ModuleInfo().moduleName(getString(R.string.sample)).titleName(getString(R.string.local_resource)));
        mainAdapter.addModuleInfo(new ModuleInfo().moduleName(getString(R.string.ad)));
        mainAdapter.addModuleInfo(new ModuleInfo().moduleName(getString(R.string.test)));
        mediaList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mediaList.setAdapter(mainAdapter);
        mediaList.setItemAnimator(new DefaultItemAnimator());
    }
    public void startMediaPlayer(String path, Boolean isVideo){
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.putExtra("path",path);
        intent.putExtra("type",isVideo);
        this.startActivity(intent);
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MediaPlayerSelectionActivity.class);
        activity.startActivity(intent);
    }

    // Set log component. It includes a pop-up dialog.
    public void setLogComponent(){
        logLinearLayout logHiddenView = findViewById(R.id.logView);
        logHiddenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogView logview = new LogView(getApplicationContext());
                logview.show(getSupportFragmentManager(),null);
            }
        });
    }
    private void initData() {
        fileNames.add("sample.mp3");
        fileNames.add("test.wav");
        fileNames.add("ad.mp4");

        copyAssetsFiles(fileNames);
    }


    private void copyAssetsFiles(final List<String> fileNames) {
        new Thread() {
            public void run() {
                for (String fileName : fileNames) {
                    copyAssetsFile(fileName);
                }
            }
        }.start();
    }

    private void copyAssetsFile(String fileName) {
        final File file = new File(getExternalFilesDir(""), fileName);
        System.out.println("File Path---->" + file.getAbsolutePath());
        if (file.exists()) {
            System.out.println("File exists");
            return;
        }
        try {
            // Get Assets.
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}