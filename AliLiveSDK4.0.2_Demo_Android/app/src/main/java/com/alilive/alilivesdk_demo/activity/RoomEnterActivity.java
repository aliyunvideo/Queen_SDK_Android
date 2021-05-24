package com.alilive.alilivesdk_demo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.utils.SPUtils;

/**
 * 房间设置界面
 */
public class RoomEnterActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_ROOM_IP = "roomIp";
    public static final String EXTRA_ROOM_ID = "roomId";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_NAME = "userName";
    public static final String EXTRA_ALLOW_MIC = "allowMic";
    private RelativeLayout mRlInteractiveRole;
    private RelativeLayout mRlLiveRole;
    private EditText mTvRoomId;
    private EditText mTvUserId;
    private EditText mEtUserName;
    private Switch mSwitchLinkRoom;
    private RelativeLayout mBack;
    private String roomIp = "";
    private Button mEnterRoomBtn;//进入房间
    private Boolean isAnchorSelected = true;//是否点击主播频道

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room_enter_activity);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView() {
        mRlInteractiveRole = findViewById(R.id.liveroom_rl_interactive_role);
        mRlInteractiveRole.setOnClickListener(this);
        mRlLiveRole = findViewById(R.id.liveroom_rl_live_role);
        mRlLiveRole.setOnClickListener(this);
        mTvRoomId = findViewById(R.id.liveroom_et_channel_id);
        mTvRoomId.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mBack = findViewById(R.id.liveroom_back_layout);
        mBack.setOnClickListener(this);
        mTvUserId = findViewById(R.id.liveroom_et_user_id);
        mTvUserId.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mEtUserName = (EditText) findViewById(R.id.liveroom_et_username);
        mSwitchLinkRoom = findViewById(R.id.switch_link_room);
        mSwitchLinkRoom.setChecked(true);
        mEnterRoomBtn = findViewById(R.id.btn_enter_room);
        mEnterRoomBtn.setOnClickListener(this);
        SharedPreferences sharedPreferences = SPUtils.getPreferences();
        roomIp = sharedPreferences.getString(EXTRA_ROOM_IP, "live-room-server-sh.myalicdn.com:443");//线上环境
        int roomId = sharedPreferences.getInt(EXTRA_ROOM_ID, 100855);
        int userId = sharedPreferences.getInt(EXTRA_USER_ID, 6666);
        String userName = sharedPreferences.getString(EXTRA_USER_NAME, "songwei");

        boolean isAllowLink = sharedPreferences.getBoolean(EXTRA_ALLOW_MIC, true);
        if (roomId != -1) {
            mTvRoomId.setText("" + roomId);
        }
        if (userId != -1) {
            mTvUserId.setText("" + userId);
        }
        if (!TextUtils.isEmpty(userName)) {
            mEtUserName.setText(userName);
        }
        mSwitchLinkRoom.setChecked(isAllowLink);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.liveroom_rl_interactive_role://主播点击
                mRlInteractiveRole.setBackgroundResource(R.drawable.shape_bg_enter_green);
                mRlLiveRole.setBackgroundResource(R.drawable.shape_bg_btn_oval);
                isAnchorSelected = true;
                break;
            case R.id.liveroom_rl_live_role://观众点击
                mRlInteractiveRole.setBackgroundResource(R.drawable.shape_bg_btn_oval);
                mRlLiveRole.setBackgroundResource(R.drawable.shape_bg_enter_green);
                isAnchorSelected = false;
                break;
            case R.id.btn_enter_room://进入房间
                if(getUserId() == -1 || getRoomId() == -1){//房间号&userid都不能为空
                    return;
                }
                if(TextUtils.isEmpty(mEtUserName.getText().toString())){//用户昵称不能为空
                    Toast.makeText(this, getString(R.string.liveroom_enter_usernick_forbid_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                //步骤1：创建一个SharedPreferences对象
                SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
                //步骤2： 实例化SharedPreferences.Editor对象
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //步骤3：将获取过来的值放入文件
                editor.putString(EXTRA_USER_NAME, mEtUserName.getText().toString());
                editor.putInt(EXTRA_USER_ID, getUserId());
                editor.putInt(EXTRA_ROOM_ID, getRoomId());
                editor.putBoolean(EXTRA_ALLOW_MIC, mSwitchLinkRoom.isChecked());
                //步骤4：提交
                editor.commit();
                if (isAnchorSelected) {
                    intent = new Intent(RoomEnterActivity.this, AnchorActivity.class);
                    intentArgs(intent);
                } else {
                    // 跳转到观众页面
                    intent = new Intent(RoomEnterActivity.this, AudienceActivity.class);
                    intentArgs(intent);
                }
                break;
            case R.id.liveroom_back_layout:
                finish();
                break;
            default:
                break;

        }
    }

    /**
     * roomid
     * @return
     */
    private int getRoomId() {
        String roomIdStr = mTvRoomId.getText().toString();
        if(TextUtils.isEmpty(roomIdStr)){
            Toast.makeText(this, getString(R.string.liveroom_enter_roomno_forbid_empty), Toast.LENGTH_SHORT).show();
            return -1;
        }else {
            Integer roomId = Integer.parseInt(roomIdStr);
            return roomId;
        }
    }
    /**
     * getUserId
     * @return
     */
    private int getUserId() {
        String userIdStr = mTvUserId.getText().toString();
        if(TextUtils.isEmpty(userIdStr)){
            Toast.makeText(this, getString(R.string.liveroom_enter_userid_forbid_empty), Toast.LENGTH_SHORT).show();
            return -1;
        }else {
            Integer userId = Integer.parseInt(userIdStr);
            return userId;
        }
    }

    /**
     * intent args
     * @param intent
     */
    private void intentArgs(Intent intent) {
        String url = "";
        if (!TextUtils.isEmpty(roomIp)) {
            url = "wss://" + roomIp + "/live";

        }
        intent.putExtra(EXTRA_ROOM_IP, url);
        intent.putExtra(EXTRA_ROOM_ID, getRoomId());
        intent.putExtra(EXTRA_USER_ID, getUserId());
        intent.putExtra(EXTRA_USER_NAME, mEtUserName.getText().toString());
        intent.putExtra(EXTRA_ALLOW_MIC, mSwitchLinkRoom.isChecked());
        startActivity(intent);
    }
}
