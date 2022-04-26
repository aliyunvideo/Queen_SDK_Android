package io.agora.vlive.ui.main.fragments;

import androidx.fragment.app.Fragment;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.Config;
import io.agora.vlive.protocol.ClientProxyListener;
import io.agora.vlive.protocol.model.response.AppVersionResponse;
import io.agora.vlive.protocol.model.response.AudienceListResponse;
import io.agora.vlive.protocol.model.response.CreateRoomResponse;
import io.agora.vlive.protocol.model.response.CreateUserResponse;
import io.agora.vlive.protocol.model.response.EditUserResponse;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.protocol.model.response.GiftListResponse;
import io.agora.vlive.protocol.model.response.GiftRankResponse;
import io.agora.vlive.protocol.model.response.LeaveRoomResponse;
import io.agora.vlive.protocol.model.response.LoginResponse;
import io.agora.vlive.protocol.model.response.ModifyUserStateResponse;
import io.agora.vlive.protocol.model.response.MusicListResponse;
import io.agora.vlive.protocol.model.response.OssPolicyResponse;
import io.agora.vlive.protocol.model.response.ProductListResponse;
import io.agora.vlive.protocol.model.response.RefreshTokenResponse;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.protocol.model.response.SeatStateResponse;
import io.agora.vlive.protocol.model.response.SendGiftResponse;
import io.agora.vlive.ui.main.MainActivity;

public abstract class AbstractFragment extends Fragment implements ClientProxyListener {
    protected AgoraLiveApplication application() {
        return (AgoraLiveApplication) getContext().getApplicationContext();
    }

    MainActivity getContainer() {
        return (MainActivity) getActivity();
    }

    protected Config config() {
        return application().config();
    }

    @Override
    public void onAppVersionResponse(AppVersionResponse response) {

    }

    @Override
    public void onRefreshTokenResponse(RefreshTokenResponse refreshTokenResponse) {

    }

    @Override
    public void onOssPolicyResponse(OssPolicyResponse response) {

    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {

    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {

    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {

    }

    @Override
    public void onCreateUserResponse(CreateUserResponse response) {

    }

    @Override
    public void onLoginResponse(LoginResponse response) {

    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {

    }

    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {

    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {

    }

    @Override
    public void onLeaveRoomResponse(LeaveRoomResponse response) {

    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {

    }

    @Override
    public void onRequestSeatStateResponse(SeatStateResponse response) {

    }

    @Override
    public void onModifyUserStateResponse(ModifyUserStateResponse response) {

    }

    @Override
    public void onSendGiftResponse(SendGiftResponse response) {

    }

    @Override
    public void onGiftRankResponse(GiftRankResponse response) {

    }

    @Override
    public void onGetProductListResponse(ProductListResponse response) {

    }

    @Override
    public void onProductStateChangedResponse(String productId, int state, boolean success) {

    }

    @Override
    public void onProductPurchasedResponse(boolean success) {

    }

    @Override
    public void onSeatInteractionResponse(long processId, String userId, int seatNo, int type) {

    }

    @Override
    public void onResponseError(int requestType, int error, String message) {

    }
}