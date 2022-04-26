package io.agora.vlive.ui.main.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.agora.vlive.Config;
import io.agora.vlive.R;
import io.agora.vlive.protocol.ClientProxy;
import io.agora.vlive.protocol.model.model.RoomInfo;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.RoomListRequest;
import io.agora.vlive.protocol.model.response.RoomListResponse;
import io.agora.vlive.ui.components.SquareRelativeLayout;
import io.agora.vlive.utils.Global;
import io.agora.vlive.utils.UserUtil;

public abstract class AbsPageFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = AbsPageFragment.class.getSimpleName();

    private static final int SPAN_COUNT = 2;
    private static final int REFRESH_DELAY = 1000 * 60;

    // By default, the client asks for 10 more rooms to show in the list
    private static final int REQ_ROOM_COUNT = 10;

    private Handler mHandler;
    private PageRefreshRunnable mPageRefreshRunnable;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RoomListAdapter mAdapter;
    private View mNoDataBg;
    private View mNetworkErrorBg;

    private int mItemSpacing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mPageRefreshRunnable = new PageRefreshRunnable();
        mItemSpacing = getContainer().getResources()
                .getDimensionPixelSize(R.dimen.activity_horizontal_margin);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_room_list, container, false);
        mSwipeRefreshLayout = layout.findViewById(R.id.host_in_swipe);
        mSwipeRefreshLayout.setNestedScrollingEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = layout.findViewById(R.id.host_in_room_list_recycler);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), SPAN_COUNT));
        mAdapter = new RoomListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new RoomListItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopRefreshTimer();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        // The swipe layout is refreshing when
                        // we want to refresh the whole page.
                        // In this case, we'll let the refreshing
                        // listener to handle all the work.
                        return;
                    }

                    startRefreshTimer();
                    int lastItemPosition = recyclerView.getChildAdapterPosition(
                            recyclerView.getChildAt(recyclerView.getChildCount() - 1));
                    if (lastItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        refreshPage(mAdapter.getLast() == null ? null : mAdapter.getLast().roomId);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mNoDataBg = layout.findViewById(R.id.no_data_bg);
        mNetworkErrorBg = layout.findViewById(R.id.network_error_bg);
        mNetworkErrorBg.setVisibility(View.GONE);

        checkRoomListEmpty();

        return layout;
    }

    private void checkRoomListEmpty() {
        mRecyclerView.setVisibility(mAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        mNoDataBg.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void startRefreshTimer() {
        mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
    }

    private void stopRefreshTimer() {
        mHandler.removeCallbacks(mPageRefreshRunnable);
    }

    private class PageRefreshRunnable implements Runnable {
        @Override
        public void run() {
            onPeriodicRefreshTimerTicked();
            mHandler.postDelayed(mPageRefreshRunnable, REFRESH_DELAY);
        }
    }

    private void onPeriodicRefreshTimerTicked() {
        refreshPage(null);
    }

    @Override
    public void onRefresh() {
        refreshPage(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefreshTimer();
        getContainer().proxy().registerProxyListener(this);
        refreshPage(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRefreshTimer();
        getContainer().proxy().removeProxyListener(this);
    }

    /**
     * Refresh the page from after a specific room.
     * @param nextId null if refresh from the beginning of list
     */
    private void refreshPage(String nextId) {
        if (nextId == null) mAdapter.clear(false);
        refreshPage(nextId, REQ_ROOM_COUNT, onGetRoomListType(), null);
    }

    private void refreshPage(String nextId, int count, int type, Integer pkState) {
        RoomListRequest request = new RoomListRequest(
                getContainer().config().getUserProfile().getToken(),
                nextId, count, type, pkState);
        getContainer().proxy().sendRequest(Request.ROOM_LIST, request);
    }

    @Override
    public void onRoomListResponse(RoomListResponse response) {
        final List<RoomInfo> list = response.data.list;
        getContainer().runOnUiThread(() -> {
            mNetworkErrorBg.setVisibility(View.GONE);
            // Next id being empty indicates this is the
            // start of room list and we need to refresh
            // the whole page.
            mAdapter.append(list, TextUtils.isEmpty(response.data.nextId));
            checkRoomListEmpty();
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private class RoomListAdapter extends RecyclerView.Adapter<RoomListItemViewHolder> {
        private List<RoomInfo> mRoomList = new ArrayList<>();

        @NonNull
        @Override
        public RoomListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RoomListItemViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.live_room_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RoomListItemViewHolder holder, final int position) {
            if (mRoomList.size() <= position) return;

            RoomInfo info = mRoomList.get(position);
            holder.name.setText(info.roomName);
            holder.count.setText(String.valueOf(info.currentUsers));
            holder.layout.setBackgroundResource(UserUtil.getUserProfileIcon(info.roomId));
            holder.itemView.setOnClickListener((view) -> {
                if (config().appIdObtained() && position < mRoomList.size()) {
                    goLiveRoom(mRoomList.get(position),
                            serverTypeToTabType(onGetRoomListType()));
                } else {
                    Toast.makeText(getContext(), R.string.agora_app_id_failed,
                            Toast.LENGTH_SHORT).show();
                    checkUpdate();
                }
            });
        }

        private void checkUpdate() {
            if (!config().hasCheckedVersion()) {
                getContainer().proxy().sendRequest(
                        Request.APP_VERSION, getContainer().getAppVersion());
            }
        }

        private int serverTypeToTabType(int serverType) {
            switch (serverType) {
                case ClientProxy.ROOM_TYPE_SINGLE: return Config.LIVE_TYPE_SINGLE_HOST;
                case ClientProxy.ROOM_TYPE_PK: return Config.LIVE_TYPE_PK_HOST;
                case ClientProxy.ROOM_TYPE_VIRTUAL_HOST: return Config.LIVE_TYPE_VIRTUAL_HOST;
                case ClientProxy.ROOM_TYPE_ECOMMERCE: return Config.LIVE_TYPE_ECOMMERCE;
                case ClientProxy.ROOM_TYPE_HOST_IN:
                default: return Config.LIVE_TYPE_MULTI_HOST;
            }
        }

        @Override
        public int getItemCount() {
            return mRoomList.size();
        }

        void append(List<RoomInfo> infoList, boolean reset) {
            if (reset) mRoomList.clear();
            mRoomList.addAll(infoList);
            notifyDataSetChanged();
        }

        void clear(boolean notifyChange) {
            mRoomList.clear();
            if (notifyChange) notifyDataSetChanged();
        }

        RoomInfo getLast() {
            return mRoomList.isEmpty() ? null : mRoomList.get(mRoomList.size() - 1);
        }
    }

    private void goLiveRoom(RoomInfo info, int roomType) {
        Intent intent = new Intent(getActivity(), getLiveActivityClass());
        intent.putExtra(Global.Constants.TAB_KEY, roomType);
        intent.putExtra(Global.Constants.KEY_IS_ROOM_OWNER, false);
        intent.putExtra(Global.Constants.KEY_ROOM_NAME, info.roomName);
        intent.putExtra(Global.Constants.KEY_ROOM_ID, info.roomId);
        startActivity(intent);
    }

    private static class RoomListItemViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView count;
        AppCompatTextView name;
        SquareRelativeLayout layout;

        RoomListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.live_room_list_item_count);
            name = itemView.findViewById(R.id.live_room_list_item_room_name);
            layout = itemView.findViewById(R.id.live_room_list_item_background);
        }
    }

    private class RoomListItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            int position = parent.getChildAdapterPosition(view);
            int total = parent.getAdapter() == null ? 0 : parent.getAdapter().getItemCount();
            int half = mItemSpacing / 2;

            outRect.top = half;
            outRect.bottom = half;

            if (position < SPAN_COUNT) {
                outRect.top = mItemSpacing;
            } else {
                int remain = total % SPAN_COUNT;
                if (remain == 0) remain = SPAN_COUNT;
                if (position + remain >= total) {
                    outRect.bottom = mItemSpacing;
                }
            }

            if (position % SPAN_COUNT == 0) {
                outRect.left = mItemSpacing;
                outRect.right = mItemSpacing / 2;
            } else {
                outRect.left = mItemSpacing / 2;
                outRect.right = mItemSpacing;
            }
        }
    }

    protected abstract int onGetRoomListType();

    protected abstract Class<?> getLiveActivityClass();

    @Override
    public void onResponseError(int requestType, int error, String message) {
        getContainer().runOnUiThread(() -> {
            Toast.makeText(getContext(),
                    "request type: "+ Request.getRequestString(requestType) +
                            ", error message:" + message, Toast.LENGTH_LONG).show();

            if (requestType == Request.ROOM_LIST) {
                if (mAdapter != null) {
                    mAdapter.clear(true);
                }
                mNoDataBg.setVisibility(View.GONE);
                mNetworkErrorBg.setVisibility(View.VISIBLE);
            }
        });
    }
}
