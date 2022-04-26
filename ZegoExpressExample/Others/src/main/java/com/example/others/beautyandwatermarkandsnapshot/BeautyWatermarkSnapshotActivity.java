package com.example.others.beautyandwatermarkandsnapshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.example.others.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import im.zego.commontools.logtools.AppLogger;
import im.zego.commontools.logtools.LogView;
import im.zego.commontools.logtools.logLinearLayout;
import im.zego.keycenter.KeyCenter;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoPlayerTakeSnapshotCallback;
import im.zego.zegoexpress.callback.IZegoPublisherTakeSnapshotCallback;
import im.zego.zegoexpress.constants.ZegoBeautifyFeature;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoBeautifyOption;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoWatermark;

public class BeautyWatermarkSnapshotActivity extends AppCompatActivity {

    TextureView preview;
    TextureView playView;
    Button startPublishingButton;
    Button startPlayingButton;
    SwitchMaterial watermarkButton;
    Button watermarkFile;
    SwitchMaterial isPreviewVisibleButton;
    Button publisherSnapshotButton;
    Button playerSnapshotButton;
    TextView roomState;

    public static final int PICK_IMAGE = 1;

    final List<String> fileNames = new ArrayList<>();

    String userID;
    String streamID;
    String roomID;
    ZegoExpressEngine engine;
    Long appID;
    String token;
    ZegoUser user;

    //Store whether the user is playing the stream
    Boolean isPlay = false;
    //Store whether the user is publishing the stream
    Boolean isPublish = false;
    //Store whether the added watermark is visible in preview.
    Boolean isPreviewVisible = false;

    ZegoWatermark watermark;

    // Unicode of Emoji
    int checkEmoji = 0x2705;
    int crossEmoji = 0x274c;
    int roomConnectedEmoji = 0x1F7E2;
    int roomDisconnectedEmoji = 0x1F534;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty_watermark_snapshot);
        initData();
        bindView();
        setDefaultValue();
        getAppIDAndUserIDAndToken();
        initEngineAndUser();
        loginRoom();
        requestPermission();
        setStartPublishButtonEvent();
        setStartPlayButtonEvent();

        setEnableWaterMarkButtonEvent();
        setChosenFileButtonEvent();
        setIsPreviewVisibleButtonEvent();
        setSnapshotButton();
        setApiCalledResult();
        setLogComponent();
        setEventHandler();
    }
    public void bindView(){
        preview = findViewById(R.id.PreviewView);
        playView = findViewById(R.id.PlayView);
        startPublishingButton = findViewById(R.id.startPublishButton);
        startPlayingButton = findViewById(R.id.startPlayButton);
        startPublishingButton = findViewById(R.id.startPublishButton);

        watermarkButton = findViewById(R.id.waterMark);
        watermarkFile = findViewById(R.id.chooseButton);
        isPreviewVisibleButton = findViewById(R.id.isPreviewVisible);
        publisherSnapshotButton = findViewById(R.id.snapshotPublisherButton);
        playerSnapshotButton = findViewById(R.id.snapshotPlayerButton);
        roomState = findViewById(R.id.roomState);
    }
    public void setDefaultValue(){
        roomID = "0024";
        streamID = "0024";
        setTitle(getString(R.string.beauty_watermark_snapshot));
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
    public void setStartPublishButtonEvent(){
        startPublishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is publishing the stream, this button is used to stop publishing. Otherwise, this button is used to start publishing.
                if (isPublish){
                    engine.stopPreview();
                    engine.stopPublishingStream();
                    AppLogger.getInstance().callApi("Stop Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.start_publishing));
                    isPublish = false;
                } else {
                    engine.startPreview(new ZegoCanvas(preview));
                    engine.startPublishingStream(streamID);
                    AppLogger.getInstance().callApi("Start Publishing Stream:%s",streamID);
                    startPublishingButton.setText(getString(R.string.stop_publishing));
                    isPublish = true;
                }
            }
        });
    }
    public void setStartPlayButtonEvent(){
        startPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the user is playing the stream, this button is used to stop playing. Otherwise, this button is used to start publishing.
                if (isPlay){
                    engine.stopPlayingStream(streamID);
                    AppLogger.getInstance().callApi("Stop Playing Stream:%s",streamID);
                    startPlayingButton.setText(getString(R.string.start_playing));
                    isPlay = false;
                } else {
                    engine.startPlayingStream(streamID, new ZegoCanvas(playView));
                    startPlayingButton.setText(getString(R.string.stop_playing));
                    AppLogger.getInstance().callApi("Start Playing Stream:%s",streamID);
                    isPlay = true;
                }
            }
        });
    }

    public void setEnableWaterMarkButtonEvent(){
        watermarkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (watermark == null)
                {
                    setDefaultWatermark();
                }
                //enable the watermark
                if (isChecked) {
                    engine.setPublishWatermark(watermark,isPreviewVisible);
                } else {
                    engine.setPublishWatermark(null,isPreviewVisible);
                }
            }
        });
    }
    public void setDefaultWatermark(){
        String path = getExternalFilesDir("").getPath()+"/logo.png";
        Rect layout = new Rect(0,0,200,200);
        watermark = new ZegoWatermark("file:/"+path, layout);
    }
    public void setChosenFileButtonEvent(){
        watermarkFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //choose the watermark from the file.
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data!=null) {
            // Get chosen file path
            String realPath = ImageFilePath.getPath(getApplicationContext(), data.getData());
            Rect layout = new Rect(0,0,200,200);
            watermark = new ZegoWatermark("file:/"+realPath, layout);
            watermarkButton.setChecked(false);
        }
    }
    public void setIsPreviewVisibleButtonEvent(){
        isPreviewVisibleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (watermark == null)
                {
                    setDefaultWatermark();
                }
                if (isChecked){
                    isPreviewVisible = true;
                } else {
                    isPreviewVisible = false;
                }
                if (watermarkButton.isChecked()) {
                    engine.setPublishWatermark(watermark, isPreviewVisible);
                }
            }
        });
    }
    public void setSnapshotButton(){
        publisherSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.takePublishStreamSnapshot(new IZegoPublisherTakeSnapshotCallback() {
                    @Override
                    public void onPublisherTakeSnapshotResult(int errorCode, Bitmap image) {
                        saveScreenShot(image);
                    }
                });
            }
        });
        playerSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.takePlayStreamSnapshot(streamID,new IZegoPlayerTakeSnapshotCallback() {
                    @Override
                    public void onPlayerTakeSnapshotResult(int errorCode, Bitmap image) {
                        saveScreenShot(image);
                    }
                });
            }
        });
    }

    //save the screenshot from bitmap
    private void saveScreenShot(Bitmap bitmap)  {
        if (bitmap != null) {
            String filename; // declaration file name
            // filename to save time
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            filename = sdf.format(date) + ".jpeg";
            BitmapUtils.saveBitmap(filename, bitmap, getApplicationContext());
        } else {
            Toast.makeText(this, "Please start publishing firstly", Toast.LENGTH_SHORT).show();
        }
    }
    public void setEventHandler(){
        engine.setEventHandler(new IZegoEventHandler() {
            // The callback triggered when the room connection state changes.
            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
                // Update room state
                if (state.equals(ZegoRoomState.CONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomConnectedEmoji));
                } else if (state.equals(ZegoRoomState.DISCONNECTED)){
                    roomState.setText(getEmojiStringByUnicode(roomDisconnectedEmoji));
                }
            }
            // The callback triggered when the state of stream publishing changes.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PUBLISHER_STATE_NO_PUBLISH and the errcode is not 0, it means that stream publishing has failed
                // and no more retry will be attempted by the engine. At this point, the failure of stream publishing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPublisherState.NO_PUBLISH)) {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_publishing));
                    }
                } else {
                    if (isPublish) {
                        startPublishingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_publishing));
                    }
                }
            }
            // The callback triggered when the state of stream playing changes.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                // If the state is PLAYER_STATE_NO_PLAY and the errcode is not 0, it means that stream playing has failed and
                // no more retry will be attempted by the engine. At this point, the failure of stream playing can be indicated
                // on the UI of the App.
                if(errorCode != 0 && state.equals(ZegoPlayerState.NO_PLAY)) {
                    if (isPlay) {
                        startPlayingButton.setText(getEmojiStringByUnicode(crossEmoji) + getString(R.string.stop_playing));
                    }
                } else {
                    if (isPlay) {
                        startPlayingButton.setText(getEmojiStringByUnicode(checkEmoji) + getString(R.string.stop_playing));
                    }
                }
            }

        });
    }
    public void setApiCalledResult(){
        // Update log with api called results
        ZegoExpressEngine.setApiCalledCallback(new IZegoApiCalledEventHandler() {
            @Override
            public void onApiCalledResult(int errorCode, String funcName, String info) {
                super.onApiCalledResult(errorCode, funcName, info);
                if (errorCode == 0){
                    AppLogger.getInstance().success("[%s]:%s", funcName, info);
                } else {
                    AppLogger.getInstance().fail("[%d]%s:%s", errorCode, funcName, info);
                }
            }
        });
    }
    private String getEmojiStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
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
    public void requestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
            }
        }
    }

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, BeautyWatermarkSnapshotActivity.class);
        activity.startActivity(intent);
    }



    static class BitmapUtils {

        /**
         * Save Bitmap
         *
         * @param name file name
         * @param bm  picture to save
         */
        static void saveBitmap(String name, Bitmap bm, Context mContext) {
            Log.d("Save Bitmap", "Ready to save picture");
            // Specify the path to store the file
            // 指定我们想要存储文件的地址
            String TargetPath = mContext.getExternalFilesDir(null) + "/images/";
            Log.d("Save Bitmap", "Save Path=" + TargetPath);
            if (!FileUtils.fileIsExist(TargetPath)) {
                Log.d("Save Bitmap", "TargetPath isn't exist");
            } else {
                File saveFile = new File(TargetPath, name);

                try {
                    FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                    saveImgOut.flush();
                    saveImgOut.close();
                    Log.d("Save Bitmap", "The picture has been saved to your phone!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
    static class FileUtils {
        /**
         * Determine whether the folder of the specified directory exists,
         * if it does not exist, you need to create a new folder
         * 判断指定目录的文件夹是否存在，如果不存在则需要创建新的文件夹
         * @param fileName 指定目录  Specified directory
         * @return 返回创建结果 Return result: TRUE or FALSE
         */
        static boolean fileIsExist(String fileName)
        {
            // Check whether the file exists
            File file=new File(fileName);
            if (file.exists())
                return true;
            else{
                // Make directory if it doesn't exist
                return file.mkdirs();
            }
        }
    }
    private void initData() {
        fileNames.add("logo.png");

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
            // Get Assets
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
        //logout and destroy the engine.
        engine.logoutRoom(roomID);
        ZegoExpressEngine.destroyEngine(null);
        super.onDestroy();
    }
}