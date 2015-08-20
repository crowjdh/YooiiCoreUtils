package com.yooiistudios.coreutils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by Dongheyon Jeong in ActivityLifecycleCallbacksTest on July 9th, 2015
 *
 * AppActiveStateObserver
 *  Supplies information of app's active state(foreground / background)
 *
 * Usage:
 * 1. Add below in onCreate of your Application class.
 *      registerActivityLifecycleCallbacks(AppActiveStateObserver.getInstance());
 * 2. Use isInForeground() and isInBackground() method to determine active state of the application.
 *
 *
 * Additional function:
 * Implement "OnStateChangeCallback" and register it by using registerCallback(OnStateChangeCallback callback) method
 * to get notified when the active state changes.
 */
public class AppActiveStateObserver implements Application.ActivityLifecycleCallbacks {
    public interface OnStateChangeCallback {
        void onStateForeground(Activity activity);
        void onStateBackground(Activity activity);
    }

    private volatile static AppActiveStateObserver instance;

    private final ArrayList<OnStateChangeCallback> mCallbacks = new ArrayList<>();
    private int mStartedActivityCount = 0;
    private boolean mIsInForeground = false;

    private AppActiveStateObserver() { }

    public static AppActiveStateObserver getInstance() {
        if (instance == null) {
            synchronized (AppActiveStateObserver.class) {
                if (instance == null) {
                    instance = new AppActiveStateObserver();
                }
            }
        }
        return instance;
    }

    public boolean isInForeground() {
        return mIsInForeground;
    }

    public boolean isInBackground() {
        return !isInForeground();
    }

    public void registerCallback(OnStateChangeCallback callback) {
        synchronized (mCallbacks) {
            mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(OnStateChangeCallback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    private void increaseStartedActivityCount(Activity activity) {
        adjustStartedActivityCount(activity, true);
    }

    private void decreaseStartedActivityCount(Activity activity) {
        adjustStartedActivityCount(activity, false);
    }

    private void adjustStartedActivityCount(Activity activity, boolean hasActivityStarted) {
        boolean wasInForeground = mIsInForeground;
        if (hasActivityStarted) {
            mStartedActivityCount++;
        } else {
            mStartedActivityCount--;
        }
        mIsInForeground = mStartedActivityCount > 0;

        boolean activityStateChanged = wasInForeground != mIsInForeground;
        if (activityStateChanged) {
            if (isInForeground()) {
                dispatchStateForeground(activity);
            } else {
                dispatchStateBackground(activity);
            }
        }
    }

    private Object[] collectCallbacks() {
        Object[] callbacks = null;
        synchronized (mCallbacks) {
            if (mCallbacks.size() > 0) {
                callbacks = mCallbacks.toArray();
            }
        }
        return callbacks;
    }

    private void dispatchStateForeground(Activity activity) {
        Object[] callbacks = collectCallbacks();

        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((OnStateChangeCallback)callback).onStateForeground(activity);
            }
        }
    }

    private void dispatchStateBackground(Activity activity) {
        Object[] callbacks = collectCallbacks();

        if (callbacks != null) {
            for (Object callback : callbacks) {
                ((OnStateChangeCallback)callback).onStateBackground(activity);
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        increaseStartedActivityCount(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        decreaseStartedActivityCount(activity);
    }

    // region Unused callbacks
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

    @Override
    public void onActivityResumed(Activity activity) { }

    @Override
    public void onActivityPaused(Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }
    // endregion
}