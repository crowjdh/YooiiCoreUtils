package com.yooiistudios.coreutils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 8. 20.
 *
 * UnitConverter
 *  Converts pixel, dp, etc...
 */
public class UnitConverter {
    private UnitConverter() {
        throw new AssertionError("You MUST NOT create the instance of this class!!");
    }

    public static int convertDpToPixel(Context context, float dipValue) {
        return convertDpToPixel(context.getResources(), dipValue);
    }

    public static int convertDpToPixel(Resources resources, float dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
