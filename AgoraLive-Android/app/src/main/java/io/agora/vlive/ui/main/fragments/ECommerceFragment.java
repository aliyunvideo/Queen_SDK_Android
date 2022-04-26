package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.ui.live.ECommerceLiveActivity;

public class ECommerceFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_ECOMMERCE;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return ECommerceLiveActivity.class;
    }
}
