package com.yfcloud.shortvideo.utils;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by 37917 on 2017/7/29 0029.
 */

public class AnimationUtil {
    private static ScaleAnimation becomeLarge, becomeSmall;

    public static ScaleAnimation getSurfaceScaleAnimation(boolean scaleLarge, Animation.AnimationListener listener) {
        if (scaleLarge) {
            if (becomeLarge == null) {
                becomeLarge = new ScaleAnimation(0.5f, 1f, 0.5f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.2f);
                becomeLarge.setFillAfter(true);
                becomeLarge.setDuration(1200);
            }
            becomeLarge.setAnimationListener(listener);
            return becomeLarge;
        } else {
            if (becomeSmall == null) {
                becomeSmall = new ScaleAnimation(1, 0.5f, 1, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.2f);
                becomeSmall.setFillAfter(true);
                becomeSmall.setDuration(1200);
            }
            becomeSmall.setAnimationListener(listener);
            return becomeSmall;
        }
    }


}
