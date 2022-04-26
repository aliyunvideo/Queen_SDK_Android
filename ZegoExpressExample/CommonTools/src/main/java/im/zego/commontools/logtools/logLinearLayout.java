package im.zego.commontools.logtools;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import im.zego.commontools.R;

public class logLinearLayout extends LinearLayout {

    LogView logView;
    Context context;
    RecyclerView recyclerView;
    LogAdapter logAdapter = LogAdapter.get();
    View view;
    LinearLayoutManager linearLayoutManager;

    public logLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        view = inflate(context, R.layout.log_layout_whole, this);
        logView = new LogView(context.getApplicationContext());
        setAdapter();
    }
    public void setAdapter(){
        recyclerView = view.findViewById(R.id.logListViewRecycle);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        logAdapter = LogAdapter.get();
        recyclerView.setAdapter(logAdapter);
        recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scrollToBottom();
            }
        });
        scrollToBottom();
    }
    public void scrollToBottom() {
        if (recyclerView != null) {
            int itemCount = logAdapter.getItemCount();
            if (itemCount > 1){
                linearLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), itemCount - 1);
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
