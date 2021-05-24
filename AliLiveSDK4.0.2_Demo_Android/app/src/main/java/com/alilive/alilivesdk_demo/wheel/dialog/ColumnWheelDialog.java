package com.alilive.alilivesdk_demo.wheel.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.wheel.base.IWheel;
import com.alilive.alilivesdk_demo.wheel.base.WheelItemView;

import java.util.ArrayList;
import java.util.List;


/**
 * date time picker dialog
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * <br><a href="https://github.com/JustinRoom/WheelViewDemo" target="_blank">https://github.com/JustinRoom/WheelViewDemo</a>
 *
 * @author jiangshicheng
 */
public class ColumnWheelDialog<T0 extends IWheel> extends Dialog {

    private TextView tvTitle;
    private TextView tvCancel;
    private TextView tvOK;

    private WheelItemView wheelItemView0;

    private T0[] items0;
    private CharSequence clickTipsWhenIsScrolling = "Scrolling, wait a minute.";

    private OnClickCallBack<T0> cancelCallBack = null;
    private OnClickCallBack<T0> okCallBack = null;

    private boolean isViewInitialized = false;
    private float textSize;
    private int itemVerticalSpace = 70;

    public ColumnWheelDialog(@NonNull Context context) {
        this(context, R.style.WheelDialog);
    }

    private ColumnWheelDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawable(null);
            getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.wheel_dialog_base);
        initView();
    }

    private void initView() {
        isViewInitialized = true;
        LinearLayout lyPickerContainer = findViewById(R.id.wheel_id_picker_container);
        wheelItemView0 = new WheelItemView(lyPickerContainer.getContext());
        lyPickerContainer.addView(wheelItemView0, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        if (textSize > 0) {
            wheelItemView0.setTextSize(textSize);
        }
        if (itemVerticalSpace > 0) {
            wheelItemView0.setItemVerticalSpace(itemVerticalSpace);
        }

        tvTitle = findViewById(R.id.wheel_id_title_bar_title);
        tvCancel = findViewById(R.id.wheel_id_title_bar_cancel);
        tvOK = findViewById(R.id.wheel_id_title_bar_ok);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancelCallBack == null) {
                    dismiss();
                    return;
                }
                if (!cancelCallBack.callBack(
                        v,
                        wheelItemView0.isShown() ? items0[wheelItemView0.getSelectedIndex()] : null
                )) dismiss();
            }
        });
        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okCallBack == null) {
                    dismiss();
                    return;
                }
                if (isScrolling()) {
                    if (!TextUtils.isEmpty(clickTipsWhenIsScrolling))
                        Toast.makeText(v.getContext(), clickTipsWhenIsScrolling, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!okCallBack.callBack(v, wheelItemView0.isShown() ? items0[wheelItemView0.getSelectedIndex()] : null
                )) dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public void setClickTipsWhenIsScrolling(CharSequence clickTipsWhenIsScrolling) {
        this.clickTipsWhenIsScrolling = clickTipsWhenIsScrolling;
    }

    public void setTitle(CharSequence title) {
        ensureIsViewInitialized();
        tvTitle.setText(title);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setItemVerticalSpace(int itemVerticalSpace) {
        this.itemVerticalSpace = itemVerticalSpace;
    }

    public void setOKButton(CharSequence ok, OnClickCallBack<T0> okCallBack) {
        ensureIsViewInitialized();
        tvOK.setText(ok);
        this.okCallBack = okCallBack;
    }

    public void setCancelButton(CharSequence cancel, OnClickCallBack<T0> cancelCallBack) {
        ensureIsViewInitialized();
        tvCancel.setText(cancel);
        this.cancelCallBack = cancelCallBack;
    }

    public void setItems(T0[] items0) {
        setItems(items0, -1);
    }

    /**
     *
     * @param items0 items0
     * @param totalOffsetX the total offset of x axis. The default value is 4dp.
     */
    public void setItems(T0[] items0, int totalOffsetX) {
        ensureIsViewInitialized();
        if (totalOffsetX == -1) {
            totalOffsetX = getContext().getResources().getDimensionPixelSize(R.dimen.wheel_picker_total_offset_x);
        }
        this.items0 = items0;
        updateShowPicker(wheelItemView0, items0);
        updateOffsetX(totalOffsetX);
    }

    public void setSelected(int selected0, int selected1, int selected2, int selected3, int selected4) {
        executeSelected(wheelItemView0, selected0);
    }

    private boolean isScrolling() {
        return isScrolling(wheelItemView0);
    }

    private void ensureIsViewInitialized() {
        if (!isViewInitialized)
            throw new IllegalStateException("View wasn't initialized, call show() first.");
    }

    private void updateShowPicker(WheelItemView wheelItemView, IWheel[] items) {
        boolean hide = (items == null || items.length == 0);
        wheelItemView.setVisibility(hide ? View.GONE : View.VISIBLE);
        if (!hide)
            wheelItemView.setItems(items);
    }

    private void executeSelected(WheelItemView view, int selectedIndex) {
        if (view.isShown())
            view.setSelectedIndex(selectedIndex);
    }

    private void updateOffsetX(int totalOffsetX) {
        List<WheelItemView> views = new ArrayList<>();
        addVisibleView(views, wheelItemView0);
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setTotalOffsetX(0);
        }
        if (views.size() > 2) {
            views.get(0).setTotalOffsetX(totalOffsetX);
            views.get(views.size() - 1).setTotalOffsetX(-totalOffsetX);
        }
    }

    private void addVisibleView(List<WheelItemView> views, WheelItemView v) {
        if (v.isShown())
            views.add(v);
    }

    private boolean isScrolling(WheelItemView view) {
        return view.isShown() && view.isScrolling();
    }

    public interface OnClickCallBack<D0> {
        boolean callBack(View v, @Nullable D0 item0);
    }
}
