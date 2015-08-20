/*
 * Copyright 2015 Mike Penz All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yooiistudios.coreutils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by mikepenz on 14.03.15.
 * This class implements a hack to change the layout padding on bottom if the keyboard is shown
 * to allow long lists with editTextViews
 * Basic idea for this solution found here: http://stackoverflow.com/a/9108219/325479
 */
public class KeyboardUtils {
    private View mDecorView;
    private View mContentView;
    private View mTargetView;
    private int mTargetViewId = -1;
    private float mInitialDpDiff = -1;

    public KeyboardUtils(Activity act, View contentView) {
        this.mDecorView = act.getWindow().getDecorView();
        this.mContentView = contentView;
    }

    public void enable() {
        if (shouldEnable()) {
            mDecorView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    public void disable() {
        if (shouldEnable()) {
            mDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    public void setTargetView(View targetView) {
        mTargetView = targetView;
        mTargetViewId = -1;
    }

    public void setTargetViewId(int targetViewId) {
        mTargetView = null;
        mTargetViewId = targetViewId;
    }

    public static void hideKeyboard(Activity act) {
        if (act != null && act.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private static boolean shouldEnable() {
        return Build.VERSION.SDK_INT >= 19;
    }

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    mDecorView.getWindowVisibleDisplayFrame(r);

                    float heightDiffDp = UnitConverter.convertPixelsToDp(
                            mDecorView.getRootView().getHeight() - (r.bottom - r.top),
                            mDecorView.getContext());

                    if (mInitialDpDiff == -1) {
                        mInitialDpDiff = heightDiffDp;
                    }

                    if (heightDiffDp - mInitialDpDiff > 100) {
                        int heightAdditive =
                                UnitConverter.convertDpToPixel(
                                        mDecorView.getContext(), (heightDiffDp - mInitialDpDiff));
                        View targetView = null;
                        if (mTargetViewId != -1) {
                            targetView = mContentView.findViewById(mTargetViewId);
                        } else if (mTargetView != null) {
                            targetView = mTargetView;
                        }
                        if (targetView != null) {
                            Rect rect = new Rect();
                            targetView.getGlobalVisibleRect(rect);
                            heightAdditive -= (mContentView.getHeight() - rect.bottom);
                            mContentView.setTranslationY(-heightAdditive);
                        }
                    } else {
                        mContentView.setTranslationY(0);
                    }
                }
            };
}
