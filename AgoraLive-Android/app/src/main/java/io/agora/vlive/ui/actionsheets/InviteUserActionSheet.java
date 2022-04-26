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

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.R;
import io.agora.vlive.protocol.model.response.AudienceListResponse.AudienceInfo;
import io.agora.vlive.utils.UserUtil;

public class InviteUserActionSheet extends AbstractActionSheet {
    public interface InviteUserActionSheetListener extends AbsActionSheetListener {
        void onActionSheetAudienceInvited(int seatId, String userId, String userName);
    }

    private InviteUserActionSheetListener mListener;
    private RoomUserAdapter mAdapter;
    private int mSeatNo;

    public InviteUserActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.action_room_host_in_audience_list, this, true);
        RecyclerView recyclerView = findViewById(R.id.live_room_action_sheet_host_in_audience_list_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        mAdapter = new RoomUserAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {
        if (listener instanceof InviteUserActionSheetListener) {
            mListener = (InviteUserActionSheetListener) listener;
        }
    }

    public void setSeatNo(int seat) {
        mSeatNo = seat;
    }

    public void append(List<AudienceInfo> userList) {
        mAdapter.append(userList);
    }

    private class RoomUserAdapter extends RecyclerView.Adapter<RoomUserViewHolder> {
        private List<AudienceInfo> mUserList = new ArrayList<>();

        void append(List<AudienceInfo> userList) {
            mUserList.addAll(userList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RoomUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomUserViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.action_invite_audience_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RoomUserViewHolder holder, final int position) {
            AudienceInfo info = mUserList.get(position);
            holder.name.setText(UserUtil.getUserText(info.userId, info.userName));
            holder.icon.setImageDrawable(UserUtil.getUserRoundIcon(getResources(), info.userId));
            holder.button.setOnClickListener(view -> {
                if (mListener != null) mListener.onActionSheetAudienceInvited(mSeatNo, info.userId, info.userName);
            });
        }

        @Override
        public int getItemCount() {
            return mUserList == null || mUserList.isEmpty() ? 0 : mUserList.size();
        }
    }

    private static class RoomUserViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        AppCompatTextView button;

        RoomUserViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_name);
            button = itemView.findViewById(R.id.live_room_action_sheet_user_list_item_invite_btn);
        }
    }
}