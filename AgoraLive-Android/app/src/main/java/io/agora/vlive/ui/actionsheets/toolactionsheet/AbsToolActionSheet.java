package io.agora.vlive.ui.actionsheets.toolactionsheet;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.vlive.R;
import io.agora.vlive.ui.actionsheets.AbstractActionSheet;

public abstract class AbsToolActionSheet extends AbstractActionSheet {
    public static final int ROLE_AUDIENCE = 0;
    public static final int ROLE_HOST = 1;
    public static final int ROLE_OWNER = 2;

    private static final int DEFAULT_GRID_SPAN = 4;

    public interface OnToolActionSheetItemClickedListener {
        void onToolActionSheetItemClicked(int position, View view);
        void onToolActionSheetItemViewBind(int position, View view);
    }

    protected ToolAdapter mAdapter;
    protected int role = ROLE_AUDIENCE;

    private int mGridSpan = DEFAULT_GRID_SPAN;

    private AppCompatTextView mTitleText;
    private RecyclerView mRecyclerView;
    private int mItemPadding;

    private OnToolActionSheetItemClickedListener mListener;

    public AbsToolActionSheet(Context context) {
        super(context);
        init();
    }

    public AbsToolActionSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mItemPadding = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);

        int layoutRes = onGetLayoutResource();
        LayoutInflater.from(getContext()).inflate(layoutRes, this, true);

        mTitleText = findViewById(R.id.live_room_action_sheet_tool_title);
        mRecyclerView = findViewById(R.id.live_room_action_sheet_tool_recycler);

        if (mTitleText == null || mRecyclerView == null) {
            throw new RuntimeException("No necessary view elements found");
        }

        mAdapter = new ToolAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(application(), mGridSpan));
        mRecyclerView.addItemDecoration(new PaddingDecoration());
    }

    public abstract int onGetLayoutResource();

    public abstract int getItemCount(int role);

    public abstract int[] getItemTitleResource(int role);

    public abstract int[] getItemIconResource(int role);

    public void setRole(int role) {
        this.role = role;
        mAdapter.notifyDataSetChanged();
    }

    public void setTile(String title) {
        mTitleText.setText(title);
    }

    private class ToolAdapter extends RecyclerView.Adapter<ToolViewHolder> {
        @NonNull
        @Override
        public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ToolViewHolder(LayoutInflater.from(
                    getContext()).inflate(R.layout.action_tool_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
            holder.setPosition(position);
            holder.name.setText(getItemTitleResource(role)[position]);
            holder.icon.setImageResource(getItemIconResource(role)[position]);
            if (mListener != null) mListener.onToolActionSheetItemViewBind(position, holder.icon);
        }

        @Override
        public int getItemCount() {
            return AbsToolActionSheet.this.getItemCount(role);
        }
    }

    class ToolViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView name;
        int position;

        ToolViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.live_room_action_sheet_tool_item_name);
            icon = itemView.findViewById(R.id.live_room_action_sheet_tool_item_icon);
            icon.setOnClickListener(view ->  {
                handleItemClicked(position, view);
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            });
        }

        void setPosition(int position) {
            this.position = position;
        }
    }

    private void handleItemClicked(int position, View view) {
        if (mListener != null) {
            mListener.onToolActionSheetItemClicked(position, view);
        }
    }

    private class PaddingDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.top = mItemPadding;
            outRect.bottom = mItemPadding;
        }
    }

    public void setOnToolActionSheetItemClickedListener(
            OnToolActionSheetItemClickedListener listener) {
        mListener = listener;
    }

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {

    }
}
