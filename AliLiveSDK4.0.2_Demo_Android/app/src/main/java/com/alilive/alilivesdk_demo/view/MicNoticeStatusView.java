package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;

/**
 * 连麦状态布局
 */
public class MicNoticeStatusView extends RelativeLayout {

    public static final int STATUS_PREPARE = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_CONNECTED = 2;
    public static final int STATUS_REFUSED = 3;

    LinearLayout ll_mic_status;
    TextView tv_mic_status;
    Button mBtnRefuse;
    Button mBtnAccept;

    NoticeItemClickListener onItemClickListener;

    private int mStatus = STATUS_PREPARE;

    public MicNoticeStatusView(Context context) {
        this(context,null);
    }

    public MicNoticeStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MicNoticeStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView(context);
    }

    private void inflateView(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.item_notice_status_view,this);
        ll_mic_status = contentView.findViewById(R.id.ll_mic_status);
        tv_mic_status = contentView.findViewById(R.id.tv_mic_status);
        mBtnAccept = contentView.findViewById(R.id.btn_accept);
        mBtnRefuse = contentView.findViewById(R.id.btn_refuse);
        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(true);

                }
            }
        });
        mBtnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(false);
                }
            }
        });
    }

    /**
     * 准备阶段
     */
    public void setPrepare(){
        mStatus = STATUS_PREPARE;
        ll_mic_status.setVisibility(View.VISIBLE);
        tv_mic_status.setVisibility(View.GONE);
        mBtnAccept.setEnabled(true);
    }

    /**
     * 连接中
     */
    public void setConnecting(){
        mStatus = STATUS_CONNECTING;
        ll_mic_status.setVisibility(View.GONE);
        tv_mic_status.setVisibility(View.VISIBLE);
        tv_mic_status.setText("正在连麦");
        mBtnAccept.setEnabled(false);
    }

    /**
     * 连接成功
     */
    public void setConnected(){
        mStatus = STATUS_CONNECTED;
        ll_mic_status.setVisibility(View.GONE);
        tv_mic_status.setVisibility(View.VISIBLE);
        tv_mic_status.setText("已连麦");
        //按钮置灰
        mBtnAccept.setEnabled(false);
    }

    /**
     * 已拒绝
     */
    public void setRefused(){
        mStatus = STATUS_REFUSED;
        ll_mic_status.setVisibility(View.GONE);
        tv_mic_status.setVisibility(View.VISIBLE);
        tv_mic_status.setText("已拒绝连麦");
        mBtnAccept.setEnabled(false);
    }

    public void setOnItemClickListener(NoticeItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface NoticeItemClickListener{
        void onItemClick(boolean isAccept);
    }
}
