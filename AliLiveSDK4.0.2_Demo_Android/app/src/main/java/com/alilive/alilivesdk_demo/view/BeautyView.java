package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.FragmentPagerAdapter;
import com.alilive.alilivesdk_demo.bean.BeautyItemData;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.fragment.SkinFragment;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;
import com.alilive.alilivesdk_demo.listener.OnTabSelectListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class BeautyView extends LinearLayout {
    private String[] mTitles = {"美颜"};
    private SegmentTabLayout mSegmentTabLayout;
    private AppCompatActivity activity;
    private ViewPager mViewPager;
    private SkinFragment mSkinFragment;
    private BeautyClickAndSlideListener mClickAndSlideListener;
    private Button mCloseButton;
    private TextView mMopi;
    private TextView mWhite;
    private boolean isMopi;
    private boolean isWhite;
    private ImageView mClose;

    public BeautyView(@NonNull Context context) {
        this(context, null);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (AppCompatActivity) context;
        initView(context);
    }
    private void initView(Context context) {
//        setBackgroundResource(R.color.color_background_white_alpha_30);
        View view = LayoutInflater.from(context).inflate(R.layout.live_beauty_view, this, true);
        mCloseButton = findViewById(R.id.live_close);
        mSegmentTabLayout = findViewById(R.id.live_tab);
        mSegmentTabLayout.setTabData(mTitles);
        mViewPager =findViewById(R.id.live_viewpager);
        mClose=findViewById(R.id.iv_close);

        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(GONE);
            }
        });
        mSkinFragment = new SkinFragment();
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(mSkinFragment);
        mViewPager.setAdapter(new FragmentPagerAdapter(activity.getSupportFragmentManager(),fragments));
        mSegmentTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
        mViewPager.setCurrentItem(0);
        BeautyClickAndSlideListener beautyClickAndSlideListener = new BeautyClickAndSlideListener() {
            @Override
            public void onButtonClick(String pageName,int pageIndex,String message, int position) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onButtonClick(pageName,pageIndex,message,position);
                }
            }

            @Override
            public void onProgressChanged(String pageName,int pageIndex,String message, float position) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onProgressChanged(pageName,pageIndex,message,position);
                }
            }

            @Override
            public void onSwitchChanged(String pageName,int pageIndex,String message, boolean isCheck) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onSwitchChanged(pageName,pageIndex,message,isCheck);
                }
            }

            @Override
            public void onPageSwitch(String pageName,int pageIndex, boolean isCheck) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onPageSwitch(pageName,pageIndex,isCheck);
                }
            }
        };
        mCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onButtonClick("",0,"关闭页面",0);
                }
//                BeautyView.this.setVisibility(GONE);
            }
        });
        mSkinFragment.setClickListener(beautyClickAndSlideListener);
        mMopi= (TextView)findViewById(R.id.tv_mopi);
        mWhite=(TextView)findViewById(R.id.tv_white);

        mMopi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isMopi=!isMopi;
                isWhite=false;
                mWhite.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_white), null, null);
                if(isMopi){
                    mViewPager.setVisibility(VISIBLE);
                    mMopi.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_mopichoose), null, null);
                    Constants.getBeautySkinNameList().clear();
                    Constants.getBeautySkinNameList().add(new BeautyItemData("磨皮",false,80));
                    mSkinFragment.setData();
                }else {
                    mViewPager.setVisibility(GONE);
                    mMopi.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_mopi), null, null);
                }
            }
        });

        mWhite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isWhite=!isWhite;
                isMopi=false;
                mMopi.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_mopi), null, null);
                if(isWhite){
                    mViewPager.setVisibility(VISIBLE);
                    Constants.getBeautySkinNameList().clear();
                    Constants.getBeautySkinNameList().add(new BeautyItemData("美白",false,80));
                    mWhite.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_whitechoose), null, null);
                    mSkinFragment.setData();
                }else {
                    mViewPager.setVisibility(GONE);
                    mWhite.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.live_white), null, null);
                }
            }
        });

    }

    public void setClickListener(BeautyClickAndSlideListener clickListener) {
        this.mClickAndSlideListener = clickListener;
    }

}
