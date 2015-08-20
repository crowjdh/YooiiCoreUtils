package com.yooiistudios.coreutils.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 6. 25.
 *
 * SwishImageLoader
 *  프로필 이미지 url 다운로드&캐시용
 */
public class CachingImageLoader {
    public interface ImageListener {
        void onShowLoadingImage();
        void onLoadImage(Bitmap bitmap);
        void onLoadImageFailed();
    }
    private static final String TAG = CachingImageLoader.class.getSimpleName();

    private volatile static CachingImageLoader instance;

    private ImageLoader mImageLoader;

    public static CachingImageLoader getInstance(FragmentActivity activity) {
        if (instance == null) {
            synchronized (CachingImageLoader.class) {
                if (instance == null) {
                    instance = new CachingImageLoader(activity);
                }
            }
        }
        return instance;
    }

    private CachingImageLoader(FragmentActivity activity) {
        mImageLoader = createImageLoader(activity);
    }

    private static ImageLoader createImageLoader(FragmentActivity activity) {
        Context context = activity.getApplicationContext();
        return new ImageLoader(
                RequestQueue.getInstance(context).get(),
                SimpleImageCache.getInstance().get(activity)
        );
    }

    public void loadImage(String requestUrl, final ImageView imageView,
                          final int loadingResId, final int failedResId) {
        get(requestUrl, new CachingImageLoader.ImageListener() {
            @Override
            public void onShowLoadingImage() {
                imageView.setImageResource(loadingResId);
            }

            @Override
            public void onLoadImage(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadImageFailed() {
                imageView.setImageResource(failedResId);
            }
        });

    }

    public void get(String requestUrl, @NonNull final ImageListener listener) {
        if (requestUrl != null && requestUrl.length() > 0) {
            mImageLoader.get(requestUrl, new ImageLoader.ImageListener() {

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();
                    if (bitmap != null) {
                        listener.onLoadImage(bitmap);
                    } else {
                        // bitmap == null 인 경우 isImmediate(캐시 hit check)라면 로딩중 이미지를 표시한다.
                        // !isImmediate(네트워크에서 받아오는 시도를 한 뒤)라면 실패 이미지를 표시한다.
                        if (isImmediate) {
                            listener.onShowLoadingImage();
                        } else {
                            listener.onLoadImageFailed();
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onLoadImageFailed();
                }
            });
        } else {
            listener.onLoadImageFailed();
        }
    }
}
