package com.alilive.alilivesdk_demo.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * 作者：fanguowei
 * 时间：2018/4/26:15:59
 * 邮箱：fanguowei@hikvision.com.cn
 * TEL： 13967884790
 * 说明：
 */

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    List<Fragment> fragments;

    public FragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
