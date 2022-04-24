package com.alivc.live.queenbeauty;

import com.alivc.live.beautyui.BeautyUIDataInjectorInterface;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.queenbeauty.injector.QueenBeautyDataInjector;
import com.alivc.live.queenbeauty.injector.QueenLookupDataInjector;
import com.alivc.live.queenbeauty.injector.QueenMakeupDataInjector;
import com.alivc.live.queenbeauty.injector.QueenStickerDataInjector;

import java.util.ArrayList;

import androidx.annotation.Keep;

@Keep
public class QueenBeautyUIDataInjector implements BeautyUIDataInjectorInterface {

    private final ArrayList<BeautyTabBean> tabBeans = new ArrayList<BeautyTabBean>();

    public QueenBeautyUIDataInjector() {
        tabBeans.add(QueenBeautyDataInjector.getBeautyTabBean());
        tabBeans.add(QueenMakeupDataInjector.getBeautyMakeupTabBean());
        tabBeans.add(QueenLookupDataInjector.getBeautyLookupTabBean());
        tabBeans.add(QueenStickerDataInjector.getBeautyStickerTabBean());
    }

    @Override
    public ArrayList<BeautyTabBean> getBeautyTabBeans() {
        return tabBeans;
    }

}
