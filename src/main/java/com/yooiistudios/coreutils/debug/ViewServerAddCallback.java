package com.yooiistudios.coreutils.debug;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 8. 12.
 *
 * ViewServerAddCallback
 *  Hierarchy viewer 를 사용하기 위해 ViewServer 를 액티비티 라이프사이클에 맞춰 등록 및 해제
 *
 *  Usage: Add this callback on onCreate of Application class
 *   -
 *  @Override
 *  public void onCreate() {
 *      super.onCreate();
 *      if (BuildConfig.DEBUG_MODE) {
 *          registerActivityLifecycleCallbacks(new ViewServerAddCallback());
 *      }
 *  }
 */
public class ViewServerAddCallback implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ViewServer.get(activity).addWindow(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ViewServer.get(activity).setFocusedWindow(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ViewServer.get(activity).removeWindow(activity);
    }

    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
}
