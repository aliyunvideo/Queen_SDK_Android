package com.alilive.alilivesdk_demo.view;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.BGMListAdapter;
import com.alilive.alilivesdk_demo.bean.MusicBean;
import com.alilive.alilivesdk_demo.listener.BGMClickListener;
import com.alilive.alilivesdk_demo.utils.CopyFileUtil;
import com.alilive.alilivesdk_demo.utils.TimeFormater;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BGMView extends RelativeLayout {

    private Context mContext;
    private BGMClickListener mBgmClickListener;
    /**
     * BGM当前播放进度
     */
    private TextView mBgmPositionTextView;
    /**
     * BGM总时长
     */
    private TextView mBgmDurationTextView;
    /**
     * 播放、暂停、恢复、停止
     */
    private Button mBgmPlayButton,mBgmResumeButton,mBgmPauseButton,mBgmStopButton;
    /**
     * BGM是否推流、循环
     */
    private Switch mEnablePushSwitch,mEnableLoopSwitch;
    /**
     * BGM 路径
     */
    private String mCurrentBGMPath;
    /**
     * BGM name
     */
    private String mCurrentBGMName;
    private SeekBar mBgmSeekBar;
    private SeekBar mVolumeSeekBar;
    private TextView mBgmTitleTextView;
    private RecyclerView mBgmRecyclerView;

    private List<MusicBean> mPathList = new ArrayList<>();
    private BGMListAdapter mBgmListAdapter;
    private String mBgmFile;

    private boolean mUpdateBGMPosition = true;
//    private TextView mBgmVolumeTextView;

    private ContentResolver mContentResolver;
    private TextView mBack;
    private ImageView mPlayAndPause;
    private boolean isPlay;

    public BGMView(Context context) {
        super(context);
        init(context);
    }

    public BGMView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BGMView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        View inflateView = LayoutInflater.from(context).inflate(R.layout.live_bgm_view, this, true);

        initView();
        initRecyclerView();
        initListener();
    }

    private void initView(){
        mBgmSeekBar = findViewById(R.id.seekbar_bgm);
        mVolumeSeekBar = findViewById(R.id.seekbar_volume);
        mBgmPositionTextView = findViewById(R.id.tv_bgm_position);
        mBgmDurationTextView = findViewById(R.id.tv_bgm_duration);
        mBgmTitleTextView = findViewById(R.id.tv_bgm_title);
//        mBgmVolumeTextView = findViewById(R.id.tv_bgm_volume);

        mBgmPlayButton = findViewById(R.id.btn_bgm_play);
        mBgmResumeButton = findViewById(R.id.btn_bgm_resume);
        mBgmPauseButton = findViewById(R.id.btn_bgm_pause);
        mBgmStopButton = findViewById(R.id.btn_bgm_stop);

        mEnablePushSwitch = findViewById(R.id.switch_push);
        mEnableLoopSwitch = findViewById(R.id.switch_loop);

        mBgmRecyclerView = findViewById(R.id.bgm_recyclerview);

        mPlayAndPause=findViewById(R.id.iv_play);

    }

    private void initRecyclerView(){
        mBgmRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        mBgmFile = CopyFileUtil.getBGMFile(mContext.getApplicationContext());
        if(!TextUtils.isEmpty(mBgmFile)){
            File file = new File(mBgmFile);
            if(file.exists()){
                String[] list = file.list();
                if(list == null){
                    return;
                }
                for (String item : list) {
                    MusicBean musicBean = new MusicBean();
                    musicBean.setName(item);
                    musicBean.setPath(mBgmFile + item);
                    musicBean.setLocal(false);
                    mPathList.add(musicBean);
                }
            }
        }
        mBgmListAdapter = new BGMListAdapter();
        getMusics();
        mBgmListAdapter.setData(mPathList);
        mBgmRecyclerView.setAdapter(mBgmListAdapter);
    }

    private void initListener(){
        mBgmListAdapter.setOnItemClickListener(new BGMListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position) {
                MusicBean musicBean = mPathList.get(position);
                mCurrentBGMName = musicBean.getName();
                mCurrentBGMPath = musicBean.getPath();
                mBgmTitleTextView.setText(mCurrentBGMName);
                if(mBgmClickListener != null){
                    isPlay=true;
                    mPlayAndPause.setImageResource(R.drawable.iv_pause);
                    mBgmClickListener.onPlayClick(mCurrentBGMPath,mEnablePushSwitch.isChecked(),mEnableLoopSwitch.isChecked());
                }
            }
        });


        //推流开关
        mEnablePushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(mBgmClickListener != null){
                    mBgmClickListener.onPushSwitchChanged(mCurrentBGMPath,checked,mEnableLoopSwitch.isChecked());
                }
            }
        });

        //循环开关
        mEnableLoopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(mBgmClickListener != null){
                    mBgmClickListener.onLoopSwitchChanged(mCurrentBGMPath,mEnablePushSwitch.isChecked(),checked);
                }
            }
        });

        //播放
        mBgmPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mPlayAndPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlay=!isPlay;
                if(isPlay){
                    mPlayAndPause.setImageResource(R.drawable.iv_pause);
                    if(mBgmClickListener != null){
                        mBgmClickListener.onResumeClick();
                    }
                }else {
                    mPlayAndPause.setImageResource(R.drawable.iv_play);
                    if(mBgmClickListener != null){
                        mBgmClickListener.onPauseClick();
                    }
                }
            }
        });

        //恢复
        mBgmResumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBgmClickListener != null){
                    mBgmClickListener.onResumeClick();
                }
            }
        });

        //暂停
        mBgmPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBgmClickListener != null){
                    mBgmClickListener.onPauseClick();
                }
            }
        });

        //停止
        mBgmStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBgmClickListener != null){
                    mBgmClickListener.onStopClick();
                }
            }
        });

        //seek
        mBgmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mUpdateBGMPosition = false;
                    mBgmPositionTextView.setText(TimeFormater.formatMs(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUpdateBGMPosition = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mUpdateBGMPosition = true;
                if(mBgmClickListener != null){
                    mBgmClickListener.onSeek(seekBar.getProgress());
                }
            }
        });

        //volume
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(mBgmClickListener != null){
//                        mBgmVolumeTextView.setText(progress+"");
                        mBgmClickListener.onVolume(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 设置BGM总时长
     * 单位是毫秒
     * >=0 时长,  <0 失败
     */
    public void setBGMDuration(int duration){
        if(duration >= 0){
            mBgmDurationTextView.setText(TimeFormater.formatMs(duration));
            mBgmSeekBar.setMax(duration / 1000 * 1000);
        }
    }

    public void setBGMCurrentPosition(int bgmCurrentPosition) {
        if(bgmCurrentPosition >= 0 && mUpdateBGMPosition){
            mBgmPositionTextView.setText(TimeFormater.formatMs(bgmCurrentPosition));
            mBgmSeekBar.setProgress(bgmCurrentPosition / 1000 * 1000);
        }
    }

    public void setBGMClickListener(BGMClickListener listener){
        this.mBgmClickListener = listener;
    }


    /**
     * 获取本机音乐列表
     */
    public void getMusics() {
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径
                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // 歌曲名
                MusicBean music = new MusicBean();
                music.setPath(path);
                music.setName(name);
                music.setLocal(true);
                mPathList.add(music);
            }
            mBgmListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
