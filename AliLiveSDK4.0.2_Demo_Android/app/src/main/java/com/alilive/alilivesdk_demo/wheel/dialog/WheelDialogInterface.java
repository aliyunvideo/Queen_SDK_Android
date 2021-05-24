package com.alilive.alilivesdk_demo.wheel.dialog;


import com.alilive.alilivesdk_demo.wheel.base.IWheel;

public interface WheelDialogInterface<T extends IWheel> {

    boolean onClick(int witch, int selectedIndex, T item);
}