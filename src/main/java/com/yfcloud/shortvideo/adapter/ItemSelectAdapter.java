package com.yfcloud.shortvideo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.utils.Const;
import com.yfcloud.shortvideo.widget.YfPopupWindow;

import java.io.File;

import static com.yfcloud.shortvideo.widget.YfPopupWindow.TYPE_FILTER;


public class ItemSelectAdapter extends RecyclerView.Adapter<ItemSelectAdapter.SelectViewHolder> {
    private final String TAG = "ItemSelectAdapter";
    private final int mType;
    Context mContext;
    private String[] mItemNames;
    private boolean mSingleSelection;
    private final int[] mDrawables;
    private String[] gifPaths;

    public ItemSelectAdapter(Context context, int type, String[] itemNames, boolean singleSelection) {
        mContext = context;
        mItemNames = itemNames;
        mSingleSelection = singleSelection;
        mType = type;
        mDrawables = getDrawables(mType);
        gifPaths = getGifPaths(mType);
        File file = new File(Const.PATH_GIF);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length == 0) {
                gifPaths = null;
            }
        }
    }

    private int[] getDrawables(int mType) {
        int[] drawables = new int[0];
        switch (mType) {
            case TYPE_FILTER:
                break;
            case YfPopupWindow.TYPE_LOGO:
                break;
            case YfPopupWindow.TYPE_GIF:
                break;
            case YfPopupWindow.TYPE_TIME:
                break;
            case YfPopupWindow.TYPE_FILTER_STYLE:
                drawables = new int[]{
                        R.drawable.ic_filter_0_nothing,
                        R.drawable.ic_filter_1_beauty,
                        R.drawable.ic_filter_2_romantic,
                        R.drawable.ic_filter_3_tender,
                        R.drawable.ic_filter_4_fairytale,
                        R.drawable.ic_filter_5_antique,
                        R.drawable.ic_filter_6_white_cate,
                        R.drawable.ic_filter_7_latte,
                        R.drawable.ic_filter_8_black_white,
                };
                break;
        }
        return drawables;
    }

    private String[] getGifPaths(int mType) {
        String[] gifPaths = new String[0];
        switch (mType) {
            case YfPopupWindow.TYPE_FILTER:
                gifPaths = new String[]{
                        Const.PATH_GIF + "/" + "origin.gif",
                        Const.PATH_GIF + "/" + "soul_obe.gif",
                        Const.PATH_GIF + "/" + "nine_window.gif",
                        Const.PATH_GIF + "/" + "70s.gif",
                        Const.PATH_GIF + "/" + "mirror.gif",
                        Const.PATH_GIF + "/" + "black_magic.gif",
                        Const.PATH_GIF + "/" + "hallucination.gif",

                };
                break;
            case YfPopupWindow.TYPE_LOGO:
                break;
            case YfPopupWindow.TYPE_GIF:
                break;
            case YfPopupWindow.TYPE_TIME:
                gifPaths = new String[]{
                        Const.PATH_GIF + "/" + "origin.gif",
                        Const.PATH_GIF + "/" + "time_back.gif",
                        Const.PATH_GIF + "/" + "slow_action.gif",
                        Const.PATH_GIF + "/" + "origin.gif",

                };
                break;
        }
        return gifPaths;
    }


    @Override
    public ItemSelectAdapter.SelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_select_filter, null);
        SelectViewHolder viewHolder = new SelectViewHolder(inflate);
        viewHolder.mImageView = (ImageView) inflate.findViewById(R.id.iv_faceu_item);
        viewHolder.mNames = (TextView) inflate.findViewById(R.id.txt);
        viewHolder.mLinearLayout = (LinearLayout) inflate.findViewById(R.id.ll_filter_item);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ItemSelectAdapter.SelectViewHolder holder, final int position) {
        holder.mNames.setText(mType == TYPE_FILTER ? mItemNames[position] :
                position == 0 ? "无" : mItemNames[position - 1]);
        //风格滤镜图标
        if (mType == YfPopupWindow.TYPE_FILTER_STYLE && mDrawables != null && mDrawables.length > 0) {
            holder.mImageView.setBackground(mContext.getResources().getDrawable(mDrawables[position]));
        } else if (mType == YfPopupWindow.TYPE_TIME||mType == YfPopupWindow.TYPE_FILTER) {
            holder.mImageView.setBackground(mContext.getResources().getDrawable(R.drawable.origin_1));
            if (gifPaths != null && position <= gifPaths.length - 1) {
                //视觉特效动图
                Glide.with(mContext).load(gifPaths[position]).asGif().into(holder.mImageView);
            }
        } else if (mType  ==YfPopupWindow.TYPE_GIF) {
            holder.mImageView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_red_round));
        }
        if (mSelectedListener != null) {
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSingleSelection) {
                        holder.mImageView.setSelected(!holder.mImageView.isSelected());
                        holder.mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bg_yellow_round));
                        int lastSelected = mCurrentSelectPosition;
                        if (lastSelected != position)
                            notifyItemChanged(lastSelected);
                        Log.d(TAG, "position: " + mCurrentSelectPosition + "," + holder.mImageView.isSelected());
                        mCurrentSelectPosition = position;
                        notifyItemChanged(position);
                    } else {
                        holder.mImageView.setSelected(!holder.mImageView.isSelected());
                    }

                    if (mSelectedListener != null) {
                        mSelectedListener.onSelected(position);
                    }

                }
            });

            if (position == mCurrentSelectPosition) {
                holder.mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bg_yellow_round));
            } else {
                holder.mLinearLayout.setBackground(null);
            }


            Log.d(TAG, "on bind view holder:" + position + "," + mCurrentSelectPosition + "," + holder.mImageView.isSelected());
            if (mSingleSelection)
                holder.mImageView.setSelected(position == mCurrentSelectPosition ? holder.mImageView.isSelected() : false);
        }

        if (mOnPressedListener != null) {
            holder.mImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
//                    Log.d(TAG, "onTouch: " + event.getAction());
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mOnPressedListener.onPressed(position, true);
                            holder.mImageView.setSelected(true);
                            holder.mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bg_yellow_round));
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            holder.mLinearLayout.setBackground(null);
                            mOnPressedListener.onPressed(position, false);
                            holder.mImageView.setSelected(false);
                            break;

                    }
                    //imageview需要返回true才会触发touchup等事件
                    return true;
                }
            });
        }

    }

    public void setOnSelectedListener(YfPopupWindow.OnSelectedListener listener) {
        mSelectedListener = listener;
    }

    public void setOnPressedListener(YfPopupWindow.OnPressedListener listener) {
        mOnPressedListener = listener;
    }

    @Override
    public int getItemCount() {
        return mType == TYPE_FILTER ? mItemNames.length : mItemNames.length + 1;
    }

    private int mCurrentSelectPosition = 0;

    class SelectViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mNames;
        LinearLayout mLinearLayout;

        SelectViewHolder(View itemView) {
            super(itemView);
        }
    }

    YfPopupWindow.OnSelectedListener mSelectedListener;
    YfPopupWindow.OnPressedListener mOnPressedListener;


}