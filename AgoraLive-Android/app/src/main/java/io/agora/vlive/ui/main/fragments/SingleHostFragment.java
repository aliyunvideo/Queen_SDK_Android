package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.ui.live.SingleHostLiveActivity;

public class SingleHostFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_SINGLE;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return SingleHostLiveActivity.class;
    }
}
