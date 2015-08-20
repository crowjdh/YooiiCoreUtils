package com.yooiistudios.coreutils.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dongheyon Jeong in Randombox_Android from Yooii Studios Co., LTD. on 15. 6. 25.
 *
 * ImageDownloadUtils
 *  이미지 다운로드
 */
public class ImageDownloader<T extends ImageDownloader.ImageUrl> {
    public interface OnDownloadImageListener<T extends ImageUrl> {
        void onDownloadImage(T requestedUrl, Bitmap response);
        void onDownloadImageFailed(T requestedUrl, VolleyError error);
    }

    public interface OnDownloadBatchImageListener<T extends ImageUrl>
            extends OnDownloadImageListener<T> {
        void onDownloadAllImages();
    }

    public interface OnDownloadBatchImageIntoFilesListener<T extends ImageUrl>
            extends OnDownloadImageListener<T> {
        void onSaveImage(String fileName, T imageUrl);
        void onSaveAllImages();
    }

    public interface BitmapSaver {
        String saveBitmap(Bitmap bitmap);
    }

    public interface ImageUrl {
        String getImageUrl();
    }

    private final Map<Object, Set<SaveBitmapTask>> mRunningTasks = new HashMap<>();

    public ImageDownloader() { }

    public void downloadFromUrls(Context context, List<T> imageUrls, Object tag,
                                 final OnDownloadBatchImageListener<T> listener) {
        if (imageUrls.isEmpty()) {
            listener.onDownloadAllImages();
            return;
        }
        final List<T> urlsToInspect = new ArrayList<>(imageUrls);
        for (T imageUrl : imageUrls) {
            downloadFromUrl(context, imageUrl, tag, new OnDownloadImageListener<T>() {
                @Override
                public void onDownloadImage(T imageUrl, Bitmap response) {
                    listener.onDownloadImage(imageUrl, response);
                    checkIfDone(imageUrl);
                }

                @Override
                public void onDownloadImageFailed(T imageUrl, VolleyError error) {
                    listener.onDownloadImageFailed(imageUrl, error);
                    checkIfDone(imageUrl);
                }

                private void checkIfDone(T imageUrl) {
                    urlsToInspect.remove(imageUrl);
                    if (urlsToInspect.isEmpty()) {
                        listener.onDownloadAllImages();
                    }
                }
            });
        }
    }

    public void downloadAndSaveFromUrls(final Context context, List<T> imageUrls, Object tag,
                                        final BitmapSaver bitmapSaver,
                                        final OnDownloadBatchImageIntoFilesListener<T> listener) {
        if (imageUrls.isEmpty()) {
            listener.onSaveAllImages();
            return;
        }
        final List<T> urlsToInspect = new ArrayList<>(imageUrls);
        downloadFromUrls(context, imageUrls, tag, new OnDownloadBatchImageListener<T>() {
            @Override public void onDownloadAllImages() { }

            @Override
            public void onDownloadImage(final T requestedUrl, Bitmap response) {
                listener.onDownloadImage(requestedUrl, response);
                Log.d("SwishTimestamp", "downloaded bitmap size: " + response.getByteCount());
                runSaveBitmapTask(context, requestedUrl, response, bitmapSaver,
                        new SaveBitmapTask.OnSaveBitmapListener<T>() {
                            @Override
                            public void onSaveBitmap(String fileName, T tag) {
                                urlsToInspect.remove(requestedUrl);
                                listener.onSaveImage(fileName, tag);
                                if (urlsToInspect.isEmpty()) {
                                    listener.onSaveAllImages();
                                }
                            }
                        });
            }

            @Override
            public void onDownloadImageFailed(T requestedUrl, VolleyError error) {
                urlsToInspect.remove(requestedUrl);
                listener.onDownloadImageFailed(requestedUrl, error);
                if (urlsToInspect.isEmpty()) {
                    listener.onSaveAllImages();
                }
            }
        });
    }

    public void downloadFromUrl(Context context, final T imageUrl, Object tag,
                                final OnDownloadImageListener<T> listener) {
        Request imageRequest = new ImageRequest(imageUrl.getImageUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        listener.onDownloadImage(imageUrl, response);
                    }
                }, 0, 0, null, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onDownloadImageFailed(imageUrl, error);
                    }
                });
        imageRequest.setTag(tag);
        imageRequest.setRetryPolicy(
                new DefaultRetryPolicy(
                        15 * 1000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        RequestQueue.getInstance(context).get().add(imageRequest);
    }

    public void runSaveBitmapTask(Context context, T tag, Bitmap bitmap,
                                  BitmapSaver bitmapSaver,
                                  SaveBitmapTask.OnSaveBitmapListener<T> listener) {
        SaveBitmapTask<T> task = new SaveBitmapTask<>(context, tag, bitmap, bitmapSaver, listener);
        task.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
        addTaskToRunningTaskMap(tag, task);
    }

    public void cancelRequestsByTag(Context context, T tag) {
        RequestQueue.getInstance(context).get().cancelAll(tag);
        cancelAllTasksByTag(tag);
    }

    public void cancelAllTasksByTag(T tag) {
        Set<SaveBitmapTask> tasks = mRunningTasks.get(tag);
        if (tasks != null) {
            for (SaveBitmapTask task : tasks) {
                tasks.remove(task);
                task.cancel(true);
            }
        }
    }

    private void addTaskToRunningTaskMap(Object tag, SaveBitmapTask task) {
        Set<SaveBitmapTask> tasksForTag = mRunningTasks.get(tag);
        if (tasksForTag == null) {
            mRunningTasks.put(tag, tasksForTag = new HashSet<>());
        }
        tasksForTag.add(task);
    }

    private static class SaveBitmapTask<T extends ImageUrl> extends android.os.AsyncTask<Void, Void, String> {
        public interface OnSaveBitmapListener<T extends ImageUrl> {
            void onSaveBitmap(String fileName, T tag);
        }

        private Context mContext;
        private T mTag;
        private WeakReference<Bitmap> mBitmapWeakReference;
        private BitmapSaver mBitmapSaver;
        private OnSaveBitmapListener<T> mListener;

        private SaveBitmapTask(Context context, T tag, Bitmap bitmap, BitmapSaver bitmapSaver,
                               OnSaveBitmapListener<T> listener) {
            mContext = context;
            mTag = tag;
            mBitmapWeakReference = new WeakReference<>(bitmap);
            mBitmapSaver = bitmapSaver;
            mListener = listener;
        }

        @Override
        protected String doInBackground(Void... params) {
            Bitmap bitmap = mBitmapWeakReference.get();
            String receivedFileName = null;
            if (bitmap != null) {
                receivedFileName = mBitmapSaver.saveBitmap(bitmap);
            }
            return receivedFileName;
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);
            if (!isCancelled()) {
                mListener.onSaveBitmap(fileName, mTag);
            }
        }
    }
}
