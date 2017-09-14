package com.yfcloud.shortvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yfcloud.shortvideo.R;

import java.util.List;


/**
 * Created by yunfan on 2017/7/25.
 */

public class MusicAdapter extends BaseAdapter {
    private List<String> mItemList;
    private Context mContext;
    private ItemClickListener mItemClickListener;

    public MusicAdapter(List<String> itemList, Context context) {
        this.mItemList = itemList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public String getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    @SuppressWarnings("deprecation")
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_music, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final String item = mItemList.get(position);
        holder.mNameTextView.setText(item.substring(item.lastIndexOf("/") + 1));
        holder.mNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null)
                    mItemClickListener.onItemClickListener(mItemList.get(position));
            }
        });
        return view;
    }

    private class ViewHolder {
        TextView mNameTextView;

        ViewHolder(View itemView) {
            mNameTextView = (TextView) itemView.findViewById(R.id.tv_audio_name);
        }
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClickListener(String path);
    }
}
