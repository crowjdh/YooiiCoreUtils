package com.yooiistudios.coreutils.volley;

import android.content.Context;

import com.android.volley.toolbox.Volley;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 6. 15.
 */
public class RequestQueue {
    /**
     * Singleton
     */
    private volatile static RequestQueue instance;

    private com.android.volley.RequestQueue mRequestQueue;

    private RequestQueue(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueue getInstance(Context context) {
        if (instance == null) {
            synchronized (RequestQueue.class) {
                if (instance == null) {
                    instance = new RequestQueue(context);
                }
            }
        }
        return instance;
    }

    public com.android.volley.RequestQueue get() {
        return mRequestQueue;
    }
}
