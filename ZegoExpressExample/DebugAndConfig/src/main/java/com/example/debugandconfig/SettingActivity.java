package com.example.debugandconfig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoLogConfig;

public class SettingActivity extends AppCompatActivity {

    TextView logPathText;
    EditText logSizeEditText;
    Button setLogConfigButton;
    Button shareLogButton;
    Button saveButton;
    TextView sdkVersionText;
    TextView demoVersionText;
    EditText userIDEditText;
    EditText appIDEditText;
    EditText tokenEditText;

    long appID;
    String userID;
    String token;
    // Path to save logs
    String logPath;
    long logSize;
    ZegoExpressEngine engine = ZegoExpressEngine.getEngine();
    ZegoLogConfig logConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        bindView();
        getAppIDAndUserIDAndToken();
        setDefaultValue();
        setLogConfigButtonEvent();
        setShareLogButtonEvent();
        setSaveButtonEvent();
        setLogComponent();
    }
    public void bindView(){
        logPathText = findViewById(R.id.logPath);
        logSizeEditText = findViewById(R.id.logSizeEdit);
        setLogConfigButton = findViewById(R.id.setLogConfigButton);
        saveButton = findViewById(R.id.saveButton);
        shareLogButton = findViewById(R.id.shareLogButton);
        sdkVersionText = findViewById(R.id.sdkVersion);
        demoVersionText = findViewById(R.id.demoVersion);
        userIDEditText = findViewById(R.id.userID);
        appIDEditText = findViewById(R.id.appID);
        tokenEditText = findViewById(R.id.token);
    }
    //get appID and userID and token
    public void getAppIDAndUserIDAndToken(){
        appID = KeyCenter.getInstance().getAppID();
        userID = KeyCenter.getInstance().getUserID();
        token = KeyCenter.getInstance().getToken();
    }
    public void setDefaultValue() {
        logPath = getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
        logPathText.setText(logPath);
        logConfig = new ZegoLogConfig();
        sdkVersionText.setText(engine.getVersion());
        demoVersionText.setText(getVersion());
        setTitle(getString(R.string.debug_config));
        appIDEditText.setText(String.valueOf(appID));
        userIDEditText.setText(userID);
        tokenEditText.setText(token);
    }
    public void setLogConfigButtonEvent(){
        setLogConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get log size set by users
                if(logSizeEditText.getText().toString().equals("")){
                    Toast.makeText(SettingActivity.this, "Log size cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    logSize = Long.valueOf(logSizeEditText.getText().toString());
                }
                catch (Exception e) {
                    Toast.makeText(SettingActivity.this, "Log size cannot be larger than 9223372036854775807!", Toast.LENGTH_SHORT).show();
                    return;
                }
                logConfig.logPath = logPath;
                logConfig.logSize = logSize;
                engine.setLogConfig(logConfig);
                AppLogger.getInstance().callApi("Set LogConfig, logPath:%s,logSize:%s",logPath,logSize);
                // Initialize ZegoExpressEngine
                ZegoEngineProfile profile = new ZegoEngineProfile();
                profile.appID = appID;
                profile.scenario = ZegoScenario.GENERAL;
                profile.application = getApplication();
                engine = ZegoExpressEngine.createEngine(profile, null);

                AppLogger.getInstance().callApi("Create ZegoExpressEngine");
            }
        });
    }
    public void setShareLogButtonEvent(){
        shareLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File rootFile = new File(logPath);
                File[] files=rootFile.listFiles();
                if (files == null){
                    Log.e("error","Empty File"); }
                ArrayList<File> requestFile = new ArrayList<>();
                ArrayList<Uri> requestFileUri = new ArrayList<>();
                for(int i =0;i<files.length;i++){
                    requestFile.add(new File(files[i].getAbsolutePath()));
                    try {
                        requestFileUri.add(FileProvider.getUriForFile(SettingActivity.this,
                                "im.zego.expresssample.fileprovider", requestFile.get(i)));
                    } catch (IllegalArgumentException e) {
                        Log.e("File Selector",
                                "The selected file can't be shared: " + requestFile.toString());
                        Log.e("File Selector",e.toString());
                    }
                }
                if (!requestFileUri.isEmpty() && requestFileUri.get(0)!= null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    // Grant temporary read permission to the content URI
                    sendIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, requestFileUri);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_log)));
                } else {
                    Log.e("File Selector", "Can not get zego log");
                }
            }
        });
    }
    public void setSaveButtonEvent(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyCenter.getInstance().setAppID(Long.valueOf(appIDEditText.getText().toString()));
                KeyCenter.getInstance().setUserID(userIDEditText.getText().toString());
                KeyCenter.getInstance().setToken(tokenEditText.getText().toString());

                Long a = KeyCenter.getInstance().getAppID();
                String u = KeyCenter.getInstance().getUserID();
                String t = KeyCenter.getInstance().getToken();
            }
        });
    }
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, SettingActivity.class);
        activity.startActivity(intent);
    }

    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionName + "." + info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
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
}