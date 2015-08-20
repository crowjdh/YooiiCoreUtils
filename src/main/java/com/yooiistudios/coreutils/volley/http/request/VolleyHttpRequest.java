package com.yooiistudios.coreutils.volley.http.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 7.
 *
 * VolleyHttpRequest
 *  VolleyHttpUtils 에 사용될 요청 객체
 */
public abstract class VolleyHttpRequest<V extends VolleyHttpRequest.Builder<
        ? extends VolleyHttpRequest, S, ? extends VolleyHttpRequest.Builder>, S>
        implements Response.Listener<S>, Response.ErrorListener {
    public interface OnResponseListener<T> {
        void onSuccess(T responseJson);
        void onFail(VolleyError error);
    }
    protected enum Method implements Request.Method { }

    // TODO: 타임아웃 조절, 테스트 시 1 사용할 것
    private static final int DEFAULT_TIMEOUT_MS = 7 * 1000;
//    private static final int DEFAULT_TIMEOUT_MS = 1;

    private final int method;
    @NonNull
    private final String url;
    @Nullable
    private final OnResponseListener<S> listener;
    @Nullable
    private final RequestFuture<S> requestFuture;
    @Nullable
    private final Object tag;
    @Nullable
    private final Map<String, String> additionalHeader;
    private final int timeout;

    protected VolleyHttpRequest(V builder) {
        method = builder.method;
        url = builder.url;
        listener = builder.listener;
        this.requestFuture = listener == null ? RequestFuture.<S>newFuture() : null;
        tag = builder.tag;
        additionalHeader = builder.additionalHeader;
        timeout = builder.timeout;
    }

    public int getMethod() {
        return method;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @Nullable
    public Map<String, String> getAdditionalHeader() {
        return additionalHeader;
    }

    public int getTimeout() {
        return timeout;
    }

    @Nullable
    public RequestFuture<S> getRequestFuture() {
        return requestFuture;
    }

    @Override
    public void onResponse(S response) {
        if (listener != null) {
            listener.onSuccess(response);
        } else if (requestFuture != null) {
            requestFuture.onResponse(response);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (listener != null) {
            listener.onFail(error);
        } else if (requestFuture != null) {
            requestFuture.onErrorResponse(error);
        }
    }

    public final Request toRequest() {
        Request request = convertToRequest();
        request.setTag(tag);
        return request;
    }

    protected abstract Request convertToRequest();

    protected static Map<String, String> createHeaderParams(Map<String, String> additionalHeader) {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("Accept", "application/json");
        headerParams.put("Content-type", "application/json");

        if (additionalHeader != null) {
            headerParams.putAll(additionalHeader);
        }

        return headerParams;
    }

    protected static Request applyRetryPolicy(Request request, int timeout) {
        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        timeout,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        return request;
    }

    protected abstract static class Builder<
            T extends VolleyHttpRequest<? extends VolleyHttpRequest.Builder, U>,
            U, W extends Builder> {
        public final int method;
        @NonNull
        public final String url;
        @Nullable
        public final OnResponseListener<U> listener;

        public Object tag;
        public Map<String, String> additionalHeader = null;
        public int timeout = DEFAULT_TIMEOUT_MS;

        public Builder(int method, @NonNull String url,
                       @Nullable OnResponseListener<U> listener) {
            this.method = method;
            this.url = url;
            this.listener = listener;
        }

        public W setTag(Object tag) {
            this.tag = tag;
            return getSelf();
        }

        public W setAdditionalHeader(Map<String, String> additionalHeader) {
            this.additionalHeader = additionalHeader;
            return getSelf();
        }

        public W setTimeout(int timeout) {
            this.timeout = timeout;
            return getSelf();
        }

        protected abstract W getSelf();

        public abstract T build();
    }
}
