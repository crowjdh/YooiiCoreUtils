package com.yooiistudios.coreutils.volley.http.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 7. 7.
 *
 * GetHttpRequest
 *  GET 요청 객체
 */
public class GetHttpRequest extends VolleyHttpRequest<GetHttpRequest.Builder, JSONObject> {
    @Nullable
    private final Map<String, Object> parameters;

    protected GetHttpRequest(Builder builder) {
        super(builder);
        parameters = builder.parameters;
    }

    @NonNull
    @Override
    public String getUrl() {
        return appendParametersToUrl(super.getUrl(), parameters);
    }

    @Nullable
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Request convertToRequest() {
        // TODO: possible improvement
        Request request = new JsonObjectRequest(getMethod(), getUrl(), null, this, this) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createHeaderParams(getAdditionalHeader());
            }
        };
        request = applyRetryPolicy(request, getTimeout());
        return request;
    }

    private static String appendParametersToUrl(String url, @Nullable Map<String, Object> values) {
        if (values != null && values.size() > 0) {
            url = appendQuestionMarkToUrl(url);
            StringBuilder parameters = new StringBuilder(url);
            Iterator<Map.Entry<String, Object>> iterator = values.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                parameters.append(entry.getKey()).append('=').append(entry.getValue());
                if (iterator.hasNext()) {
                    parameters.append('&');
                }
            }
            return parameters.toString();
        } else {
            return url;
        }
    }

    @NonNull
    private static String appendQuestionMarkToUrl(String getUrl) {
        if (!getUrl.endsWith("?")) {
            getUrl += '?';
        }
        return getUrl;
    }

    public static class Builder extends VolleyHttpRequest.Builder<GetHttpRequest, JSONObject, Builder> {
        @Nullable
        public Map<String, Object> parameters;

        public Builder(@NonNull String url) {
            this(url, null);
        }

        public Builder(@NonNull String url,
                       @Nullable OnResponseListener<JSONObject> listener) {
            super(Method.GET, url, listener);
        }

        public Builder setParameters(@Nullable Map<String, Object> parameters) {
            this.parameters = parameters;

            return this;
        }

        @Override
        protected Builder getSelf() {
            return this;
        }

        @Override
        public GetHttpRequest build() {
            return new GetHttpRequest(this);
        }

    }
}
