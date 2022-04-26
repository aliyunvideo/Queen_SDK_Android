package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import io.agora.vlive.protocol.manager.SeatServiceManager;
import io.agora.vlive.protocol.model.model.UserProfile;
import io.agora.vlive.protocol.model.request.AudienceListRequest;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.utils.UserUtil;

public class OnlineUserInviteCallActionSheet extends AbstractActionSheet implements View.OnClickListener {
    private RecyclerView mUserListRecycler;
    private OnlineUserInviteCallAdapter mInviteAdapter = new OnlineUserInviteCallAdapter();
    private OnlineUserApplicationAdapter mApplicationAdapter = new OnlineUserApplicationAdapter();
    private SeatServiceManager mSeatManager;
    private OnlineUserActionListener mOnlineUserListener;
    private List<UserProfile> mUserList = new ArrayList<>();
    private String mOwnerId;

    private RelativeLayout mLeftTitleLayout;
    private RelativeLayout mRightTitleLayout;
    private TextView mAllUserTitle;
    private TextView mApplicationTitle;
    private View mAllUserTitleIndicator;
    private View mApplicationTitleIndicator;
    private View mNotification;
    private boolean mDefaultShowAll = true;

    private ClientProxy mProxy;
    private String mRoomId;
    private String mToken;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_room_online_user_type_title_layout_left:
                if (mDefaultShowAll) return;
                changeTab();
                break;
            case R.id.live_room_online_user_type_title_layout_right:
                if (!mDefaultShowAll) return;
                changeTab();
                break;
        }
    }

    public interface OnlineUserActionListener {
        void onUserInvited(String userId, String userName);
        void onUserApplicationAccepted(String userId, String userName);
        void onUserApplicationRejected(String userId, String userName);
        void onUserListTabChanged(boolean showAll);
    }

    public OnlineUserInviteCallActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.online_user_call_invite_layout, this, true);
        mUserListRecycler = findViewById(R.id.live_room_action_sheet_online_user);
        mUserListRecycler.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));

        mUserListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        mLeftTitleLayout = findViewById(R.id.live_room_online_user_type_title_layout_left);
        mLeftTitleLayout.setOnClickListener(this);
        mRightTitleLayout = findViewById(R.id.live_room_online_user_type_title_layout_right);
        mRightTitleLayout.setOnClickListener(this);

        mAllUserTitle = findViewById(R.id.live_room_online_user_text_all);
        mAllUserTitleIndicator = findViewById(R.id.live_room_online_user_tab_all_indicator);
        mApplicationTitle = findViewById(R.id.live_room_online_user_text_application);
        mApplicationTitleIndicator = findViewById(R.id.live_room_online_user_text_application_indicator);

        mNotification = findViewById(R.id.notification_point);
        mNotification.setVisibility(View.GONE);
        showTab();
    }

    public void setup(ClientProxy proxy, ClientProxyListener listener,
                      String roomId, String token) {
        mProxy = proxy;
        mRoomId = roomId;
        mToken = token;
    }

    public void requestMoreAudience() {
        AudienceListRequest request = new AudienceListRequest(
                mToken, mRoomId, getLastUserId(),
                AudienceListRequest.TYPE_ALL);
        mProxy.sendRequest(Request.AUDIENCE_LIST, request);
    }

    public void setSeatManager(SeatServiceManager manager) {
        mSeatManager = manager;
    }

    public void setOnlineUserListener(OnlineUserActionListener listener) {
        mOnlineUserListener = listener;
    }

    public void setOwnerUserId(String userId) {
        mOwnerId = userId;
    }

    private void showTabHighlight() {
        if (mDefaultShowAll) {
            setBoldText(mAllUserTitle, true);
            setBoldText(mApplicationTitle, false);
            mAllUserTitleIndicator.setVisibility(VISIBLE);
            mApplicationTitleIndicator.setVisibility(GONE);
        } else {
            setBoldText(mAllUserTitle, false);
            setBoldText(mApplicationTitle, true);
            mAllUserTitleIndicator.setVisibility(GONE);
            mApplicationTitleIndicator.setVisibility(VISIBLE);
            mNotification.setVisibility(GONE);
        }
    }

    private void showTab() {
        showTabHighlight();
        mUserListRecycler.setAdapter(getCurrentAdapter());
    }

    public void changeTab() {
        mDefaultShowAll = !mDefaultShowAll;
        if (mOnlineUserListener != null) mOnlineUserListener.onUserListTabChanged(mDefaultShowAll);
        showTab();
    }

    private void setBoldText(TextView text, boolean bold) {
        text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    public void append(List<UserProfile> userList) {
        mUserList.addAll(userList);
        notifyDataSetChanged();
    }

    private String getLastUserId() {
        return mUserList.isEmpty() ? null : mUserList.get(mUserList.size() - 1).getUserId();
    }

    private RecyclerView.Adapter getCurrentAdapter() {
        return mDefaultShowAll ? mInviteAdapter : mApplicationAdapter;
    }

    public void notifyDataSetChanged() {
        getCurrentAdapter().notifyDataSetChanged();
    }

    private class OnlineUserInviteCallAdapter extends RecyclerView.Adapter<OnlineUserInviteCallViewHolder> {
        @NonNull
        @Override
        public OnlineUserInviteCallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnlineUserInviteCallViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.action_room_online_user_invite_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OnlineUserInviteCallViewHolder holder, final int position) {
            UserProfile profile = mUserList.get(position);
            holder.name.setText(getUserText(profile));
            holder.icon.setImageDrawable(UserUtil.getUserRoundIcon(getResources(), profile.getUserId()));

            if (mOwnerId != null && mOwnerId.equals(profile.getUserId())) {
                holder.status.setVisibility(View.GONE);
            } else {
                holder.status.setVisibility(View.VISIBLE);

                if (userInviting(profile.getUserId())) {
                    holder.status.setText(R.string.live_room_online_user_invited_btn);
                    holder.status.setActivated(false);
                } else {
                    holder.status.setText(R.string.live_room_online_user_invite_call_btn);
                    holder.status.setActivated(true);
                }

                holder.status.setOnClickListener(view -> {
                    if (mOnlineUserListener != null) mOnlineUserListener
                            .onUserInvited(profile.getUserId(), profile.getUserName());
                });
            }
        }

        private boolean userInviting(String userId) {
            return mSeatManager != null && mSeatManager.userIsInvited(userId);
        }

        @Override
        public int getItemCount() {
            return mUserList == null || mUserList.isEmpty() ? 0 : mUserList.size();
        }
    }

    private String getUserText(UserProfile profile) {
        return !TextUtils.isEmpty(profile.getUserName()) ? profile.getUserName() : profile.getUserId();
    }

    private String getUserText(String name, String replace) {
        return !TextUtils.isEmpty(name) ? name : replace;
    }

    private static class OnlineUserInviteCallViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        AppCompatTextView status;

        OnlineUserInviteCallViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_name);
            status = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_status);
        }
    }

    private class OnlineUserApplicationAdapter extends RecyclerView.Adapter<OnlineUserCallApplicationViewHolder> {
        @NonNull
        @Override
        public OnlineUserCallApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OnlineUserCallApplicationViewHolder(LayoutInflater.from(getContext())
                    .inflate(R.layout.action_room_online_user_audience_application_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OnlineUserCallApplicationViewHolder holder, int position) {
            if (position < mSeatManager.getAudienceApplication().size()) {
                SeatServiceManager.SeatApplicationUserInfo info = mSeatManager.getAudienceApplication().get(position);
                holder.name.setText(getUserText(info.userName, info.userId));
                holder.icon.setImageDrawable(UserUtil.getUserRoundIcon(getResources(), info.userId));
                holder.acceptBtn.setOnClickListener(view -> {
                    if (mOnlineUserListener != null) mOnlineUserListener
                            .onUserApplicationAccepted(info.userId, info.userName);
                });

                holder.rejectBtn.setOnClickListener(view -> {
                    if (mOnlineUserListener != null) mOnlineUserListener
                            .onUserApplicationRejected(info.userId, info.userName);
                });
            }
        }

        @Override
        public int getItemCount() {
            return mSeatManager == null ? 0 : mSeatManager.getAudienceApplication().size();
        }
    }

    private static class OnlineUserCallApplicationViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        AppCompatTextView acceptBtn;
        AppCompatTextView rejectBtn;

        public OnlineUserCallApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_icon);
            name = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_name);
            acceptBtn = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_accept);
            rejectBtn = itemView.findViewById(R.id.live_room_action_sheet_online_user_item_reject);
        }
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {

    }

    public boolean checkIfShowNotification() {
        mNotification.setVisibility(mDefaultShowAll ? VISIBLE : GONE);
        return mDefaultShowAll;
    }

    public void showNotification(boolean show) {
        mNotification.setVisibility(show ? VISIBLE : GONE);
    }
}
