package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.R;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.ClientProxyListener;
import io.agora.vlive.protocol.model.model.UserProfile;
import io.agora.vlive.protocol.model.request.AudienceListRequest;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.utils.UserUtil;

public class LiveRoomUserListActionSheet extends AbstractActionSheet {
    private static final String TAG = LiveRoomUserListActionSheet.class.getSimpleName();

    public interface OnUserSelectedListener extends AbsActionSheetListener {
        void onActionSheetUserListItemSelected(String userId, String userName);
    }

    private OnUserSelectedListener mOnUserSelectedListener;
    private RoomUserAdapter mAdapter;

    private ClientProxy mProxy;
    private String mRoomId;
    private String mToken;

    public LiveRoomUserListActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.action_room_all_user_list, this, true);
        RecyclerView recyclerView = findViewById(R.id.live_room_action_sheet_all_user_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new RoomUserAdapter();
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastItemPosition = recyclerView.getChildAdapterPosition(
                            recyclerView.getChildAt(recyclerView.getChildCount() - 1));
                    if (lastItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        requestMoreAudience();
                    }
                }
            }
        });
    }

    public void setup(ClientProxy proxy, ClientProxyListener listener,
                      String roomId, String token) {
        mProxy = proxy;
        mRoomId = roomId;
        mToken = token;
    }

    public void requestMoreAudience() {
        AudienceListRequest request = new AudienceListRequest(
                mToken, mRoomId, mAdapter.getLastUserId(),
                AudienceListRequest.TYPE_ALL);
        mProxy.sendRequest(Request.AUDIENCE_LIST, request);
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof OnUserSelectedListener) {
            mOnUserSelectedListener = (OnUserSelectedListener) listener;
        }
    }

    public void appendUsers(List<UserProfile> userList) {
        mAdapter.append(userList);
    }

    private class RoomUserAdapter extends RecyclerView.Adapter {
        private List<UserProfile> mUserList = new ArrayList<>();

        public void append(List<UserProfile> userList) {
            mUserList.addAll(userList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomUserViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.action_room_user_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            UserProfile profile = mUserList.get(position);
            RoomUserViewHolder viewHolder = (RoomUserViewHolder) holder;
            viewHolder.name.setText(getUserText(profile));
            viewHolder.icon.setImageDrawable(UserUtil.getUserRoundIcon(getResources(), profile.getUserId()));
            viewHolder.itemView.setOnClickListener(view -> {
                if (mOnUserSelectedListener != null) mOnUserSelectedListener
                        .onActionSheetUserListItemSelected(profile.getUserId(), profile.getUserName());
            });
        }

        private String getUserText(UserProfile profile) {
            return !TextUtils.isEmpty(profile.getUserName()) ? profile.getUserName() : profile.getUserId();
        }

        @Override
        public int getItemCount() {
            return mUserList == null || mUserList.isEmpty() ? 0 : mUserList.size();
        }

        String getLastUserId() {
            return mUserList.isEmpty() ? null : mUserList.get(mUserList.size() - 1).getUserId();
        }
    }

    private class RoomUserViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;

        RoomUserViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_name);
        }
    }
}
