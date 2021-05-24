package com.alilive.alilivesdk_demo.view;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 苹果风格样式单选对话框
 */
public class IosStyleSheetDialog {

	private OnSheetItemClickListener mOnSheetItemClickListener;
	private Activity activity;
	private Dialog dialog;
	private TextView txt_title;
	private TextView txt_cancel;
	private LinearLayout lLayout_content;
	private ScrollView sLayout_content;
	private boolean showTitle = false;
	private List<SheetItem> sheetItemList;
	private OnDialogCancelListener omDialogCancelListener;

	/**是否添加过选项标识*/
	private boolean mAddSheetItemFlag = false;

	public IosStyleSheetDialog(Activity activity, List<String> itemList, String title, OnSheetItemClickListener onSheetItemClickListener) {
		mAddSheetItemFlag = false;
		this.activity = activity;
		sheetItemList = new ArrayList<>(itemList.size());
		for (String itemName : itemList) {
			sheetItemList.add(new SheetItem(itemName));
		}
		mOnSheetItemClickListener = onSheetItemClickListener;
		builder().setTitle(title);
	}

	/**
	 * 构建一个对话框
	 * @return
     */
	public IosStyleSheetDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(activity).inflate(
				R.layout.basis_ios_style_sheet_dialog, null);

		// 设置Dialog最小宽度为屏幕宽度
		view.setMinimumWidth(ScreenUtil.getScreenWidth(activity));

		// 获取自定义Dialog布局中的控件
		sLayout_content = (ScrollView) view.findViewById(R.id.sLayout_content);
		lLayout_content = (LinearLayout) view
				.findViewById(R.id.lLayout_content);
		txt_title = (TextView) view.findViewById(R.id.txt_title);
		txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
		txt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(omDialogCancelListener!=null){
					omDialogCancelListener.onCancelClick();
				}
				dialog.dismiss();
			}
		});

		// 定义Dialog布局和参数
		dialog = new Dialog(activity, R.style.IOSSheetDialogStyle);
		dialog.setContentView(view);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);

		return this;
	}

	public IosStyleSheetDialog setTitle(String title) {
		if (!TextUtils.isEmpty(title)){
			showTitle = true;
			txt_title.setVisibility(View.VISIBLE);
			txt_title.setText(title);
		}
		return this;
	}

	public IosStyleSheetDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public IosStyleSheetDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	/** 设置条目布局 */
	private void setSheetItems() {
		if (sheetItemList == null || sheetItemList.size() <= 0) {
			return;
		}
		if (mAddSheetItemFlag) {
			return;
		}
		int size = sheetItemList.size();

		// TODO 高度控制，非最佳解决办法
		// 添加条目过多的时候控制高度
//		if (size >= 15) {
//			LayoutParams params = (LayoutParams) sLayout_content
//					.getLayoutParams();
////			params.height = (int) (ScreenUtil.getScreenHeight(activity) / 1.3f);
//			sLayout_content.setLayoutParams(params);
//		} else {
//			sLayout_content.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//		}

		// 循环添加条目
		for (int i = 1; i <= size; i++) {
			final int index = i;
			SheetItem sheetItem = sheetItemList.get(i - 1);
			String strItem = sheetItem.name;
			SheetItemColor color = sheetItem.color;

			TextView textView = new TextView(activity);
			textView.setText(strItem);
			textView.setTextSize(18);
			textView.setGravity(Gravity.CENTER);

			// 背景图片
			if (size == 1) {
				if (showTitle) {
					textView.setBackgroundResource(R.drawable.ios_style_sheet_bottom_selector);
				} else {
					textView.setBackgroundResource(R.drawable.ios_style_sheet_single_selector);
				}
			} else {
				if (showTitle) {
					if (i >= 1 && i < size) {
						textView.setBackgroundResource(R.drawable.ios_style_sheet_middle_selector);
					} else {
						textView.setBackgroundResource(R.drawable.ios_style_sheet_bottom_selector);
					}
				} else {
					if (i == 1) {
						textView.setBackgroundResource(R.drawable.ios_style_sheet_top_selector);
					} else if (i < size) {
						textView.setBackgroundResource(R.drawable.ios_style_sheet_middle_selector);
					} else {
						textView.setBackgroundResource(R.drawable.ios_style_sheet_bottom_selector);
					}
				}
			}

			// 字体颜色
			if (color == null) {
				textView.setTextColor(Color.parseColor(SheetItemColor.Blue
						.getName()));
			} else {
				textView.setTextColor(Color.parseColor(color.getName()));
			}

			// 高度
			float scale = activity.getResources().getDisplayMetrics().density;
			int height = (int) (45 * scale + 0.5f);
			textView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, height));

			// 点击事件
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnSheetItemClickListener != null) {
						mOnSheetItemClickListener.onClick(index - 1);
					}
					dialog.dismiss();
				}
			});

			lLayout_content.addView(textView);
		}
		mAddSheetItemFlag = true;
	}

	/**
	 * 显示
	 * @param itemList 列表项
	 * @param title 标题
	 */
	public void show(List<String> itemList, String title) {
		show(itemList, title, null);
	}
	/**
	 * 显示
	 * @param itemList 列表项
	 * @param title 标题
	 */
	public void show(List<String> itemList, String title, OnSheetItemClickListener sheetItemClickListener) {
		if (itemList != null && itemList.size() != 0) {
			mAddSheetItemFlag = false;
			sheetItemList.clear();
			for (String itemName : itemList) {
				sheetItemList.add(new SheetItem(itemName));
			}
			lLayout_content.removeAllViews();
		}
		if (sheetItemClickListener != null) {
			mOnSheetItemClickListener = sheetItemClickListener;
		}
		setTitle(title);
		show();
	}

	public void show() {
		setSheetItems();
		dialog.show();
	}

	public interface OnSheetItemClickListener {
		void onClick(int which);
	}

	/**
	 * 选择项
	 */
	public class SheetItem {

		/**名称*/
		public String name;
		/**颜色*/
		public SheetItemColor color;

		public SheetItem(String name) {
			this.name = name;
		}
	}

	public enum SheetItemColor {
		Blue("#037BFF"), Red("#FD4A2E");

		private String name;

		private SheetItemColor(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * 取消监听
	 */
	public interface OnDialogCancelListener {
		void onCancelClick();
	}
	
	public void setOnDialogCancelListener(OnDialogCancelListener omDialogCancelListener) {
		this.omDialogCancelListener = omDialogCancelListener;
	}
}
