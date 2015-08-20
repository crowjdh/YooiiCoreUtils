package com.yooiistudios.coreutils.volley.http.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 8.
 *
 * PutHttpRequest
 *  PUT 요청 객체
 */
public class PutHttpRequest extends BodyHttpRequest {
    protected PutHttpRequest(Builder builder) {
        super(builder);
    }

    public static class Builder extends BodyHttpRequest.Builder<PutHttpRequest, Builder> {
        public Builder(@NonNull String url, @NonNull JSONObject body) {
            this(url, body, null);
        }

        public Builder(@NonNull String url, @NonNull JSONObject body,
                       @Nullable OnResponseListener<JSONObject> listener) {
            super(Method.PUT, url, body, listener);
        }

        @Override
        protected Builder getSelf() {
            return this;
        }

        @Override
        public PutHttpRequest build() {
            return new PutHttpRequest(this);
        }
    }
}
