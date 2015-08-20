package com.yooiistudios.coreutils.volley.http.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 8.
 *
 * BodyfulHttpRequest
 *  Body 를 가지는 request
 */
public abstract class BodyHttpRequest extends VolleyHttpRequest
        <BodyHttpRequest.Builder
                <? extends BodyHttpRequest, ? extends BodyHttpRequest.Builder>, JSONObject> {
    @NonNull
    private final JSONObject body;

    protected BodyHttpRequest(Builder<? extends BodyHttpRequest,
            ? extends BodyHttpRequest.Builder> builder) {
        super(builder);
        body = builder.body;
    }

    @NonNull
    public JSONObject getBody() {
        return body;
    }

    @Override
    public Request convertToRequest() {
        Request request = new JsonObjectRequest(getMethod(), getUrl(),
                getBody(), this, this) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createHeaderParams(getAdditionalHeader());
            }
        };
        request = applyRetryPolicy(request, getTimeout());
        return request;
    }

    public static abstract class Builder<T extends BodyHttpRequest, W extends Builder>
            extends VolleyHttpRequest.Builder<T, JSONObject, W> {
        @NonNull
        public final JSONObject body;

        public Builder(int method, @NonNull String url, @NonNull JSONObject body) {
            this(method, url, body, null);
        }

        public Builder(int method, @NonNull String url, @NonNull JSONObject body,
                       @Nullable OnResponseListener<JSONObject> listener) {
            super(method, url, listener);
            this.body = body;
        }

        @Override
        public abstract T build();
    }
}
