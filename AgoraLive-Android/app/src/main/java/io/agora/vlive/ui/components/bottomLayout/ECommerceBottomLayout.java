package io.agora.vlive.ui.components.bottomLayout;

import android.content.Context;
import android.util.AttributeSet;

import io.agora.vlive.R;

public class ECommerceBottomLayout extends AbsBottomLayout {
    public ECommerceBottomLayout(Context context) {
        super(context);
    }

    public ECommerceBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.live_bottom_button_layout;
    }

    @Override
    public void setRole(int role) {
        fun2Btn.setImageResource(R.drawable.live_bottom_btn_shopcart);

        switch (role) {
            case ROLE_AUDIENCE:
            case ROLE_HOST:
                fun1Btn.setImageResource(R.drawable.live_bottom_btn_gift);
                break;
            case ROLE_OWNER:
                fun1Btn.setImageResource(R.drawable.live_bottom_btn_pk);
                break;
        }
    }
}
