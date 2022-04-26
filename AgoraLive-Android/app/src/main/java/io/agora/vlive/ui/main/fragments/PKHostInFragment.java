package io.agora.vlive.ui.main.fragments;

import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.ui.live.HostPKLiveActivity;

public class PKHostInFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_PK;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return HostPKLiveActivity.class;
    }
}
