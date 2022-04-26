package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.model.model.RoomInfo;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.RoomListRequest;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.utils.UserUtil;

public class PkRoomListActionSheet extends AbstractActionSheet {
    private static final int ROOM_REQUEST_COUNT = 10;

    public interface OnPkRoomSelectedListener extends AbsActionSheetListener {
        /**
         *
         * @param position
         * @param roomId target pk room id
         * @param uid as the target room owner's rtm uid
         */
        void onPkRoomListActionSheetRoomSelected(int position, String roomId, int uid);
    }

    private OnPkRoomSelectedListener mListener;
    private PkRoomListAdapter mAdapter;
    private String mToken;
    private ClientProxy mProxy;
    private int mRoomType;
    private AppCompatTextView mTitle;
    private String mTitleFormat;

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof OnPkRoomSelectedListener) {
            mListener = (OnPkRoomSelectedListener) listener;
        }
    }

    public PkRoomListActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        mTitleFormat = getResources().getString(R.string.live_room_pk_room_list_title_format);
        LayoutInflater.from(getContext()).inflate(R.layout.action_room_all_pk_room_list, this, true);
        mTitle = findViewById(R.id.live_room_action_sheet_pk_room_list_title);
        RecyclerView recyclerView = findViewById(R.id.live_room_action_sheet_pk_room_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new PkRoomListAdapter();
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastItemPosition = recyclerView.getChildAdapterPosition(
                            recyclerView.getChildAt(recyclerView.getChildCount() - 1));
                    if (lastItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        requestMorePkRoom();
                    }
                }
            }
        });
    }

    public void setup(ClientProxy proxy, String token, int roomType) {
        mProxy = proxy;
        mToken = token;
        mRoomType = roomType;
    }

    public void requestMorePkRoom() {
        RoomListRequest request = new RoomListRequest(mToken, mAdapter.getLastRoomId(),
                ROOM_REQUEST_COUNT, mRoomType, ClientProxy.PK_WAIT);
        mProxy.sendRequest(Request.ROOM_LIST, request);
    }

    public void appendUsers(RoomListResponse.RoomList list) {
        mAdapter.append(list);
    }

    private class PkRoomListAdapter extends RecyclerView.Adapter {
        private RoomListResponse.RoomList mRoomList;

        void append(@NonNull RoomListResponse.RoomList list) {
            if (mRoomList == null) {
                mRoomList = list;
            } else {
                mRoomList.nextId = list.nextId;
                mRoomList.total = list.total;
                if (mRoomList.list != null) {
                    mRoomList.list.addAll(list.list);
                } else {
                    mRoomList.list = list.list;
                }
            }

            notifyDataSetChanged();
            String title = String.format(mTitleFormat, list.total);
            mTitle.setText(title);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PkRoomListViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.action_room_all_pk_room_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (mRoomList.list == null) {
                return;
            }

            RoomInfo info = mRoomList.list.get(position);
            PkRoomListViewHolder viewHolder = (PkRoomListViewHolder) holder;
            viewHolder.name.setText(info.roomName);
            viewHolder.icon.setImageDrawable(UserUtil.getUserRoundIcon(getResources(), info.roomId));
            viewHolder.pkButton.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.onPkRoomListActionSheetRoomSelected(position, info.roomId, info.ownerUid);
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mRoomList == null || mRoomList.list == null) ? 0 : mRoomList.list.size();
        }

        String getLastRoomId() {
            if (mRoomList == null || mRoomList.list == null) {
                return null;
            } else if (mRoomList.list.size() == 0) {
                return null;
            } else {
                int size = mRoomList.list.size();
                return mRoomList.list.get(size - 1).roomId;
            }
        }
    }

    private class PkRoomListViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        AppCompatTextView pkButton;

        PkRoomListViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.live_room_action_sheet_pk_room_list_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_pk_room_list_item_name);
            pkButton = itemView.findViewById(R.id.live_room_action_sheet_pk_room_list_item_invite_btn);
        }
    }
}
