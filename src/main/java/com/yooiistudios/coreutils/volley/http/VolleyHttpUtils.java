package com.yooiistudios.coreutils.volley.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.yooiistudios.randombox.utils.volley.RequestQueue;
import com.yooiistudios.randombox.utils.volley.http.request.BodyHttpRequest;
import com.yooiistudios.randombox.utils.volley.http.request.GetHttpRequest;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 6. 15.
 *
 * VolleyHttpUtils
 *  Volley 를 사용한 http 통신을 래핑한 클래스
 */
public class VolleyHttpUtils {
    public static JSONObject execute(Context context, GetHttpRequest httpRequest) throws VolleyHttpException {
        // TODO: RequestFuture may be null.
        return performRequest(context, httpRequest.toRequest(), httpRequest.getRequestFuture());
    }

    public static JSONObject execute(Context context, BodyHttpRequest httpRequest) throws VolleyHttpException {
        return performRequest(context, httpRequest.toRequest(), httpRequest.getRequestFuture());
    }

    private static <T> T performRequest(Context context, Request request, RequestFuture<T> requestFuture) throws VolleyHttpException {
        getRequestQueue(context.getApplicationContext()).add(request);

        try {
            return requestFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new VolleyHttpException(e);
        }
    }

    public static void executeAsync(Context context, GetHttpRequest httpRequest) {
        getRequestQueue(context).add(httpRequest.toRequest());
    }

    public static void executeAsync(Context context, BodyHttpRequest httpRequest) {
        getRequestQueue(context).add(httpRequest.toRequest());
    }

    public static void cancelRequestByTag(Context context, @NonNull Object tag) {
        getRequestQueue(context).cancelAll(tag);
    }

    private static com.android.volley.RequestQueue getRequestQueue(Context context) {
        return RequestQueue.getInstance(context).get();
    }

    public static class VolleyHttpException extends Exception {
        public VolleyHttpException() {
            this("Something went wrong during Volley HTTP execution!!");
        }

        public VolleyHttpException(String detailMessage) {
            super(detailMessage);
        }

        public VolleyHttpException(Throwable throwable) {
            super(throwable);
        }

        public VolleyHttpException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }
}
