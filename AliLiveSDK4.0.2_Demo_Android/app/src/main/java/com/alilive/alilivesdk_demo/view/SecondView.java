package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.SecodListAdapter;

public class SecondView extends RelativeLayout {

    private TextView mback;
    private TextView mTitle;
    private RecyclerView mRecycleview;
    private SecodListAdapter adapter;
    private OnClickListener mListener;
    private String type;
    public void setOnItemClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public SecondView(Context context) {
        super(context);
        init(context);
    }

    public SecondView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SecondView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
         LayoutInflater.from(context).inflate(R.layout.live_second_view, this, true);
         initView();

    }

    private void initView(){
        mback=findViewById(R.id.tv_second_back);
        mTitle=findViewById(R.id.tv_second_title);
        mRecycleview=findViewById(R.id.rv_second_content);
        adapter=new SecodListAdapter();
        mRecycleview.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecycleview.setAdapter(adapter);
        adapter.setOnItemClickListener(new SecodListAdapter.OnClickItemListener() {
            @Override
            public void onClick(String content,int postion) {
                if(mListener!=null){
                    mListener.onClick(content,type,postion);
                }
            }
        });
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null){
                    mListener.onBack();
                }
            }
        });
    }

    public void setmTitle(String title){
        mTitle.setText(title);
    }



    public void setData(String[] datas, String type,int position){
        adapter.setmUserList(datas,position);
        adapter.notifyDataSetChanged();
        this.type=type;
    }

    public interface  OnClickListener{
        public void  onClick(String content, String type,int position);
        public void onBack();
    }
}
