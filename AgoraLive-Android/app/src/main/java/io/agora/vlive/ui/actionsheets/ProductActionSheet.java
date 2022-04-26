package io.agora.vlive.ui.actionsheets;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import io.agora.vlive.protocol.manager.ProductServiceManager;
import io.agora.vlive.protocol.model.model.Product;
import io.agora.vlive.utils.Global;

public class ProductActionSheet extends AbstractActionSheet implements View.OnClickListener {
    public interface OnProductActionListener {
        void onProductDetail(Product product);
        void onProductListed(String productId);
        void onProductUnlisted(String productId);
    }

    private OnProductActionListener mListener;

    private RecyclerView mRecycler;
    private ProductListAdapter mAdapter;
    private TextView mUnlistedText;
    private TextView mListedText;
    private View mIndicatorUnlisted;
    private View mIndicatorListed;
    private RelativeLayout mTypeLayout;

    // Whether the current displayed list is the "listed" page
    private boolean mCurrentListed;

    // The audience should only see the "listed" page
    private int mRole = Global.Constants.ROLE_AUDIENCE;

    private String mRoomId;

    private ProductServiceManager mProductManager;
    private ArrayList<Product> mProductListed = new ArrayList<>();
    private ArrayList<Product> mProductUnlisted = new ArrayList<>();
    private String mProductPriceFormat;
    private int mProductItemMargin;

    @Override
    public void setActionSheetListener(AbsActionSheetListener listener) {

    }

    public ProductActionSheet(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.action_room_product_list, this, true);
        mProductPriceFormat = getContext().getString(R.string.live_room_action_sheet_product_price_format);
        mProductItemMargin = getResources().getDimensionPixelSize(
                R.dimen.live_room_product_action_sheet_product_item_margin);

        mRecycler = findViewById(R.id.live_room_action_sheet_product_list_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.addItemDecoration(new ProductItemDecoration());
        mAdapter = new ProductListAdapter();
        mRecycler.setAdapter(mAdapter);

        mUnlistedText = findViewById(R.id.live_room_product_sheet_text_unlisted);
        mListedText = findViewById(R.id.live_room_product_sheet_text_listed);
        mIndicatorUnlisted = findViewById(R.id.live_room_product_sheet_indicator_unlisted);
        mIndicatorListed = findViewById(R.id.live_room_product_sheet_indicator_listed);
        mTypeLayout = findViewById(R.id.live_room_action_sheet_product_list_type_layout);

        mUnlistedText.setOnClickListener(this);
        mListedText.setOnClickListener(this);
    }

    public void setRole(int role) {
        mRole = role;

        if (mRole == Global.Constants.ROLE_OWNER) {
            mTypeLayout.setVisibility(View.VISIBLE);
            if (mCurrentListed) {
                switchType();
            } else {
                setHighlightType();
            }
        } else if (mRole == Global.Constants.ROLE_AUDIENCE) {
            mTypeLayout.setVisibility(View.GONE);
            mCurrentListed = true;
        }

        if (mProductManager != null) {
            mProductManager.requestProductList(mRoomId);
        }
    }

    public void switchType() {
        if (mRole == Global.Constants.ROLE_OWNER) {
            mCurrentListed = !mCurrentListed;
            setHighlightType();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setHighlightType() {
        if (mCurrentListed) {
            setBoldText(mListedText, true);
            setBoldText(mUnlistedText, false);
            mIndicatorListed.setVisibility(View.VISIBLE);
            mIndicatorUnlisted.setVisibility(View.GONE);
        } else {
            setBoldText(mListedText, false);
            setBoldText(mUnlistedText, true);
            mIndicatorListed.setVisibility(View.GONE);
            mIndicatorUnlisted.setVisibility(View.VISIBLE);
        }
    }

    private void setBoldText(TextView text, boolean bold) {
        text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public void setProductManager(ProductServiceManager manager) {
        mProductManager = manager;
    }

    public void setListener(OnProductActionListener listener) {
        mListener = listener;
    }

    public void updateList(List<Product> list) {
        mProductListed.clear();
        mProductUnlisted.clear();

        for (Product product : list) {
            Product p = new Product(product);
            if (p.state == Product.PRODUCT_LAUNCHED) {
                mProductListed.add(p);
            } else if (p.state == Product.PRODUCT_UNAVAILABLE) {
                mProductUnlisted.add(p);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.live_room_product_sheet_text_listed:
            case R.id.live_room_product_sheet_text_unlisted:
                switchType();
                break;
        }
    }

    private class ProductListAdapter extends RecyclerView.Adapter<ProductListViewHolder> {
        @NonNull
        @Override
        public ProductListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProductListViewHolder(LayoutInflater.
                    from(getContext()).inflate(R.layout.product_list_item_layout, null));
        }

        @Override
        public void onBindViewHolder(@NonNull ProductListViewHolder holder, int position) {
            Product product = null;
            if (mCurrentListed && position < mProductListed.size()) {
                product = mProductListed.get(position);
            } else if (!mCurrentListed && position < mProductUnlisted.size()) {
                product = mProductUnlisted.get(position);
            }

            if (product == null) return;

            holder.icon.setImageResource(productIdToResId(product.productId));
            holder.description.setText(productDescription(product.productId));
            holder.price.setText(String.format(mProductPriceFormat, product.price));
            holder.action.setText(getProductActionStringRes());
            holder.action.setTypeface(Typeface.DEFAULT_BOLD);
            final Product p = product;
            holder.action.setOnClickListener(view -> handleProductAction(p));
        }

        @Override
        public int getItemCount() {
            return mCurrentListed ? mProductListed.size() : mProductUnlisted.size();
        }
    }

    private int getProductActionStringRes() {
        if (mRole == Global.Constants.ROLE_OWNER) {
            return mCurrentListed
                    ? R.string.live_room_action_sheet_product_action_unlisted
                    : R.string.live_room_action_sheet_product_action_list;
        } else {
            return R.string.live_room_action_sheet_product_action_detail;
        }
    }

    private int productIdToResId(String productId) {
        switch (productId) {
            case "2": return R.drawable.icon_product_2;
            case "3": return R.drawable.icon_product_3;
            case "4": return R.drawable.icon_product_4;
            default: return R.drawable.icon_product_1;
        }
    }

    private int productDescription(String productId) {
        switch(productId) {
            case "2": return R.string.product_desp_2;
            case "3": return R.string.product_desp_3;
            case "4": return R.string.product_desp_4;
            default: return R.string.product_desp_1;
        }
    }

    private void handleProductAction(Product product) {
        if (mListener != null) {
            if (mRole == Global.Constants.ROLE_OWNER) {
                if (mCurrentListed) {
                    mListener.onProductUnlisted(product.productId);
                } else {
                    mListener.onProductListed(product.productId);
                }
            } else {
                mListener.onProductDetail(product);
            }

            mAdapter.notifyDataSetChanged();
        }
    }

    private static class ProductListViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView icon;
        AppCompatTextView description;
        AppCompatTextView price;
        AppCompatTextView action;

        public ProductListViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.product_list_item_icon);
            description = itemView.findViewById(R.id.product_list_item_description);
            price = itemView.findViewById(R.id.product_list_item_price);
            action = itemView.findViewById(R.id.product_list_item_action_btn);
        }
    }

    private class ProductItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.top = mProductItemMargin;
            outRect.bottom = mProductItemMargin;
        }
    }
}
