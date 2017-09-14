package com.yfcloud.shortvideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.yfcloud.shortvideo.R;
import com.yunfan.encoder.filter.YfAntiqueFilter;
import com.yunfan.encoder.filter.YfBlackMagicFilter;
import com.yunfan.encoder.filter.YfBlackWhiteFilter;
import com.yunfan.encoder.filter.YfCrayonFilter;
import com.yunfan.encoder.filter.YfFilterFactory;
import com.yunfan.encoder.filter.YfGifFilter;
import com.yunfan.encoder.filter.YfLatteFilter;
import com.yunfan.encoder.filter.YfLookupFilter;
import com.yunfan.encoder.filter.YfMirrorFilter;
import com.yunfan.encoder.filter.YfMultiWindowFilter;
import com.yunfan.encoder.filter.YfRomanticFilter;
import com.yunfan.encoder.filter.YfShakeFilter;
import com.yunfan.encoder.filter.YfSketchFilter;
import com.yunfan.encoder.filter.YfSkinWhiteFilter;
import com.yunfan.encoder.filter.YfSoulDazzleFilter;
import com.yunfan.encoder.filter.YfTartanFilter;
import com.yunfan.encoder.filter.YfTenderFilter;
import com.yunfan.encoder.filter.YfWaterMarkFilter;
import com.yunfan.encoder.filter.YfWaveFilter;
import com.yunfan.encoder.filter.YfWhirlpoolFilter;
import com.yunfan.encoder.filter.YfWhiteCatFilter;

import java.io.IOException;

/**
 * Created by 37917 on 2017/7/27 0027.
 */

public class FilterFactory {
    Context mContext;
    YfFilterFactory.Factory mFactory;

    public FilterFactory(Context context) throws IllegalAccessException {
        mContext = context;
        mFactory = new YfFilterFactory.Factory(context);
    }

    public YfCrayonFilter createCrayonFilter(int filterIndex) {
        YfCrayonFilter temp = mFactory.createCrayonFilter();
        temp.setIndex(filterIndex);
        temp.setRenderInSpecificPts(true);
        return temp;
    }

    public YfSketchFilter createSketchFilter(int filterIndex) {
        YfSketchFilter temp = mFactory.createSketchFilter();
        temp.setIndex(filterIndex);
        temp.setRenderInSpecificPts(true);
        return temp;
    }

    public YfWaveFilter createWaveFilter(int filterIndex) {
        YfWaveFilter yfWaveFilter = mFactory.createWaveFilter();
        yfWaveFilter.setIndex(filterIndex);
        yfWaveFilter.setAutoMotion(0.5f);
        yfWaveFilter.setRenderInSpecificPts(true);
        return yfWaveFilter;
    }

    public YfWhirlpoolFilter createWhirlpoolFilter(int filterIndex) {
        YfWhirlpoolFilter filter = mFactory.createWhirlpoolFilter();
        filter.setIndex(filterIndex);
        filter.setRenderInSpecificPts(true);
        return filter;
    }

    public YfTartanFilter createTartanFilter(Context context, int filterIndex) {
        YfTartanFilter filter = mFactory.createTartanFilter();
        if (filter == null) return null;
        filter.setIndex(filterIndex);
        filter.setRenderInSpecificPts(true);
        return filter;
    }

    public YfSoulDazzleFilter createSoulDazzleFilter(int filterIndex) {
        YfSoulDazzleFilter yfSoulDazzleFilter = mFactory.createSoulDazzleFilter();
        if (yfSoulDazzleFilter == null) return null;
        yfSoulDazzleFilter.setIndex(filterIndex);
        yfSoulDazzleFilter.setAutoMotion(0.04f);
        yfSoulDazzleFilter.setRenderInSpecificPts(true);
        return yfSoulDazzleFilter;
    }

    public YfMirrorFilter createMirrorFilter(int filterIndex) {
        YfMirrorFilter yfMirrorFilter = mFactory.createMirrorFilter();
        if (yfMirrorFilter == null) return null;
        yfMirrorFilter.setIndex(filterIndex);
        yfMirrorFilter.setRenderInSpecificPts(true);
        return yfMirrorFilter;
    }

    public YfBlackMagicFilter createEdgeFilter(int filterIndex) {
        YfBlackMagicFilter yfEdgeFilter = mFactory.createEdgeFilter();
        if (yfEdgeFilter == null) return null;
        yfEdgeFilter.setIndex(filterIndex);
        yfEdgeFilter.setRenderInSpecificPts(true);
        return yfEdgeFilter;
    }

    public YfShakeFilter createShakeFilter(int filterIndex) {
        YfShakeFilter yfShakeFilter = mFactory.createShakeFilter();
        if (yfShakeFilter == null) return null;
        yfShakeFilter.setIndex(filterIndex);
        yfShakeFilter.setAutoMotion(0.03f);
        yfShakeFilter.setRenderInSpecificPts(true);
        return yfShakeFilter;
    }

    public YfMultiWindowFilter createMultiWindowFilter(int size, int filterIndex) {
        YfMultiWindowFilter filter = mFactory.createMultiWindowFilter();
        if (filter == null) return null;
        filter.setIndex(filterIndex);
        filter.setWindowSize((int) Math.sqrt(size), (int) Math.sqrt(size));
        filter.setRenderInSpecificPts(true);
        return filter;
    }

    public YfGifFilter createGifFilter(Context context, int filterIndex, int displayWidth, String gifFileName) {
        YfGifFilter gifFilter = mFactory.createGifFilter();
        gifFilter.setIndex(filterIndex);
        try {
            gifFilter.setGifSource(context.getAssets().open(gifFileName));
            int percent = 40;
            int padding = displayWidth * 10 / 100;
            float logoWidth = 240, logoHeight = 144;
            int width = displayWidth * percent / 100;
            int height = (int) (width * logoHeight / logoWidth);
            gifFilter.setPosition(padding, padding, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gifFilter;
    }

    public YfWaterMarkFilter createWaterMarkFilter(Context context, int filterIndex, int displayWidth, int logo) {
        YfWaterMarkFilter waterMarkFilter = mFactory.createWaterMarkFilter();
        waterMarkFilter.setIndex(filterIndex);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), logo);
        waterMarkFilter.setWaterMark(bitmap);
        int percent = 50;
        int padding = 50;
        float logoWidth = bitmap.getWidth(), logoHeight = bitmap.getHeight();
        int width = displayWidth * percent / 100;
        int height = (int) (width * logoHeight / logoWidth);
        waterMarkFilter.setPosition(displayWidth - width - padding, padding, width, height);
        return waterMarkFilter;
    }

    //===================风格滤镜====================

    public YfSkinWhiteFilter createSkinWhiteFilter(int filterIndex) {
        YfSkinWhiteFilter temp = mFactory.createSkinWhiteFilter();
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfRomanticFilter createRomanticFilter(int filterIndex) {
        YfRomanticFilter temp = mFactory.createRomanticFilter();
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfTenderFilter createTenderFilter(Context context, int filterIndex) {
        YfTenderFilter temp = mFactory.createTenderFilter(context);
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfLookupFilter createFairyTaleFilter(Context context, int filterIndex) {
        YfLookupFilter temp = mFactory.createLookupFilter(context,"filter/fairy_tale.png");
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfAntiqueFilter createAntiqueFilter(int filterIndex) {
        YfAntiqueFilter temp = mFactory.createAntiqueFilter();
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfWhiteCatFilter createWhiteCatFilter(int filterIndex) {
        YfWhiteCatFilter temp = mFactory.createWhiteCatFilter();
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfLatteFilter createLatteFilter(int filterIndex) {
        YfLatteFilter temp = mFactory.createLatteFilter();
        temp.setIndex(filterIndex);
        return temp;
    }

    public YfBlackWhiteFilter createBlackWhiteFilter(int filterIndex) {
        YfBlackWhiteFilter temp = mFactory.createBlackWhiteFilter();
        temp.setIndex(filterIndex);
        return temp;
    }


}
