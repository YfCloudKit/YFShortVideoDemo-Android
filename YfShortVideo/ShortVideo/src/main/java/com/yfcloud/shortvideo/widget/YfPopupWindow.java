package com.yfcloud.shortvideo.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.adapter.ItemSelectAdapter;


public class YfPopupWindow extends PopupWindow {

    private final Context mContext;
    private final String[] mItemNames;
    private final boolean mSingleSelection;

    public static final int TYPE_FILTER = 0x100;
    public static final int TYPE_LOGO = 0x101;
    public static final int TYPE_GIF = 0x110;
    public static final int TYPE_TIME = 0x111;
    public static final int TYPE_FILTER_STYLE = 0x1000;
    private final int mType;

    public YfPopupWindow(Context context, String[] itemNames, int type, boolean singleSelection) {
        super(context);
        mType = type;
        mContext = context;
        mSingleSelection = singleSelection;
        mItemNames = itemNames;
        // 设置可以获得焦点
        this.setFocusable(true);
        // 设置弹窗内可点击
        setTouchable(true);
        // 设置弹窗外可点击
        setOutsideTouchable(true);
        // 设置弹窗的宽度和高度
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        setWidth(displayMetrics.widthPixels);

        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.colorCommonHalfTransparentBg)));
        // 设置弹窗的布局界面
        View view = LayoutInflater.from(context).inflate(R.layout.popup_faceu_item, null);
        setContentView(view);
        initView(view);

    }

    ItemSelectAdapter mAdapter;

    private void initView(View view) {

        TextView tvTip = (TextView) view.findViewById(R.id.tv_tip);
        TextView tvBottom = (TextView) view.findViewById(R.id.tv_function);
        tvTip.setText(getEffectTip(mType));
        tvBottom.setText(getEffectBottomTitle(mType));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_faceu);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ItemSelectAdapter(mContext, mType, mItemNames, mSingleSelection);
        recyclerView.setAdapter(mAdapter);
    }

    private String getEffectTip(int type) {
        String tip = "";
        switch (type) {
            case TYPE_FILTER:
                tip = mContext.getResources().getString(R.string.tip_filter);
                break;
            case TYPE_LOGO:
                tip = mContext.getResources().getString(R.string.tip_logo);
                break;
            case TYPE_GIF:
                tip = mContext.getResources().getString(R.string.tip_gif);
                break;
            case TYPE_TIME:
                tip = mContext.getResources().getString(R.string.tip_time);
                break;
            case TYPE_FILTER_STYLE:
                tip = mContext.getResources().getString(R.string.tip_filter_style);
                break;
        }
        return tip;
    }

    private String getEffectBottomTitle(int type) {
        String bottomName = "";
        switch (type) {
            case TYPE_FILTER:
                bottomName = mContext.getResources().getString(R.string.bottom_filter);
                break;
            case TYPE_LOGO:
                bottomName = mContext.getResources().getString(R.string.bottom_logo);
                break;
            case TYPE_GIF:
                bottomName = mContext.getResources().getString(R.string.bottom_gif);
                break;
            case TYPE_TIME:
                bottomName = mContext.getResources().getString(R.string.bottom_time);
                break;
            case TYPE_FILTER_STYLE:
                bottomName = mContext.getResources().getString(R.string.bottom_fiter_style);
                break;
        }
        return bottomName;
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mAdapter.setOnSelectedListener(listener);
    }

    public void setOnPressedListener(OnPressedListener listener) {
        mAdapter.setOnPressedListener(listener);
    }

    public interface OnSelectedListener {
        void onSelected(int position);
    }

    public interface OnPressedListener {
        void onPressed(int position, boolean pressed);
    }
}