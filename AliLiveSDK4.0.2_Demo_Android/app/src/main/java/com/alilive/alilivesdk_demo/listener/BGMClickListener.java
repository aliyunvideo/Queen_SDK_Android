package com.alilive.alilivesdk_demo.listener;

public interface BGMClickListener {

    /**
     * 播放
     * @param push  推流
     * @param loop  循环
     * @param path  BGM路径
     */
    void onPlayClick(String path,boolean push,boolean loop);

    /**
     * 恢复
     */
    void onResumeClick();

    /**
     * 暂停
     */
    void onPauseClick();

    /**
     * 停止
     */
    void onStopClick();

    /**
     * seek
     */
    void onSeek(int position);

    void onVolume(int volume);

    void onPushSwitchChanged(String path,boolean push,boolean loop);

    void onLoopSwitchChanged(String path,boolean push,boolean loop);
}
