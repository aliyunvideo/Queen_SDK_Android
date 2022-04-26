package io.agora.vlive.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.vlive.utils.Global;
import io.agora.vlive.R;

public class HomeFragment extends AbstractFragment implements View.OnClickListener {
    private static final int CATEGORY_IMAGE_WIDTH = 690;
    private static final int CATEGORY_IMAGE_HEIGHT = 299;

    private ScrollView mContentScrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container,false);
        mContentScrollView = view.findViewById(R.id.home_category_content_layout);

        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                adjustScreenSize((ViewGroup) view);
            }
        });

        view.findViewById(R.id.home_category_multi_layout).setOnClickListener(this);
        view.findViewById(R.id.home_category_single_layout).setOnClickListener(this);
        view.findViewById(R.id.home_category_pk_layout).setOnClickListener(this);
        view.findViewById(R.id.home_category_virtual_host_layout).setOnClickListener(this);
        view.findViewById(R.id.home_category_ecommerce_layout).setOnClickListener(this);
        return view;
    }

    private void adjustScreenSize(ViewGroup container) {
        float ratio = CATEGORY_IMAGE_HEIGHT / (float) CATEGORY_IMAGE_WIDTH;
        ImageView image = container.findViewById(R.id.home_category_multi_image);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) (image.getMeasuredWidth() * ratio);
        image.setLayoutParams(params);

        image = container.findViewById(R.id.home_category_single_image);
        params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) (image.getMeasuredWidth() * ratio);
        image.setLayoutParams(params);

        image = container.findViewById(R.id.home_category_pk_image);
        params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) (image.getMeasuredWidth() * ratio);
        image.setLayoutParams(params);

        image = container.findViewById(R.id.home_category_virtual_host_image);
        params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) (image.getMeasuredWidth() * ratio);
        image.setLayoutParams(params);

        image = container.findViewById(R.id.home_category_ecommerce_image);
        params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.height = (int) (image.getMeasuredWidth() * ratio);
        image.setLayoutParams(params);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mContentScrollView != null) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mContentScrollView.getLayoutParams();
            int systemBarHeight = getContainer().getSystemBarHeight();
            params.topMargin += systemBarHeight;
            mContentScrollView.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View view) {
        int tabId;
        switch (view.getId()) {
            case R.id.home_category_single_layout:
                tabId = Global.Constants.TAB_ID_SINGLE;
                break;
            case R.id.home_category_pk_layout:
                tabId = Global.Constants.TAB_ID_PK;
                break;
            case R.id.home_category_virtual_host_layout:
                tabId = Global.Constants.TAB_ID_VIRTUAL;
                break;
            case R.id.home_category_ecommerce_layout:
                tabId = Global.Constants.TAB_ECOMMERCE;
                break;
            default:
                tabId = Global.Constants.TAB_ID_MULTI;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Global.Constants.TAB_KEY, tabId);
        if (getContainer() != null) {
            getContainer().setNavigationSelected(R.id.navigation_rooms, bundle);
        }
    }
}
