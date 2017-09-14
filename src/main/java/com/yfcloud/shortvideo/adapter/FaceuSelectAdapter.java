package com.yfcloud.shortvideo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yfcloud.shortvideo.R;
import com.yfcloud.shortvideo.widget.CircleImageView;


/**
 * Created by yunfan on 2017/3/15.
 */

public class FaceuSelectAdapter extends RecyclerView.Adapter<FaceuSelectAdapter.SelectViewHolder> {
    private final String TAG = "FaceuSelectAdapter";
    Context mContext;
    private String[] mFaceuItems;
    private final String[] mArNames;
    private int[] mFaceuMipmaps = {
            R.mipmap.ic_delete_all,
            R.mipmap.tiara,
            R.mipmap.item0208,
            R.mipmap.einstein,
            R.mipmap.princesscrown,
            R.mipmap.mood,
            R.mipmap.deer,
            R.mipmap.beagledog,
            R.mipmap.item0501,
            R.mipmap.colorcrown,
            R.mipmap.item0210,
            R.mipmap.happyrabbi,
            R.mipmap.item0204,
            R.mipmap.hartshorn
    } ;

    public FaceuSelectAdapter(Context context, String[] faceuItems) {
        mContext = context;
        mFaceuItems = faceuItems;
        mArNames = mContext.getResources().getStringArray(R.array.ar_name);
    }


    @Override
    public FaceuSelectAdapter.SelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_select, null);
        SelectViewHolder viewHolder = new SelectViewHolder(inflate);
        viewHolder.mButton = (CircleImageView) inflate.findViewById(R.id.circle_image_view);
        viewHolder.mTextView = (TextView) inflate.findViewById(R.id.tv_effect_name);
        viewHolder.mLinearLayout = (LinearLayout) inflate.findViewById(R.id.ll_faceu_item);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final FaceuSelectAdapter.SelectViewHolder holder, final int position) {
        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bg_yellow_round));
                int lastSelected = mCurrentSelectPosition;
                notifyItemChanged(lastSelected);
                mCurrentSelectPosition = position;
                Log.d(TAG, "position: " + mCurrentSelectPosition);
                notifyItemChanged(position);
                if (mOnSelectListener != null) {
                    mOnSelectListener.onSelect(position);
                }
            }
        });
        holder.mButton.setBackground(mContext.getResources().getDrawable(mFaceuMipmaps[position]));
        holder.mTextView.setText(position == 0 ? "无" : mArNames[position - 1]);
        if (position == mCurrentSelectPosition) {
            holder.mLinearLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bg_yellow_round));
        } else {
            holder.mLinearLayout.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return mFaceuItems.length + 1;//position为0时，不开启faceu
    }

    private int mCurrentSelectPosition = 0;

    class SelectViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mButton;
        TextView mTextView;
        LinearLayout mLinearLayout;

        SelectViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setSelectListener(OnSelectListener selectListener) {
        mOnSelectListener = selectListener;
    }

    OnSelectListener mOnSelectListener;

    public interface OnSelectListener {
        void onSelect(int position);
    }
}
