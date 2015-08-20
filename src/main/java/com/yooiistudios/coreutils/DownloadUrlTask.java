package com.yooiistudios.coreutils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dongheyon Jeong in DownloadLargeFileTest from Yooii Studios Co., LTD. on 15. 7. 10.
 *
 * DownloadUrlTask
 *  url 을 File 로 다운로드
 */
public class DownloadUrlTask extends android.os.AsyncTask<Void, Integer, DownloadUrlTask.State> {
    private static final String TAG = DownloadUrlTask.class.getSimpleName();

    public interface OnDownloadListener {
        void onDownloadProgressUpdate(int current, int total);
        void onDownloadSuccess();
        void onDownloadFail();
        void onDownloadCancel();
    }

    protected enum State { SUCCESS, FAIL, CANCELLED }

    private static final int TIMEOUT_MILLI = 7 * 1000;

    private String mUrl;
    private File mDestFile;
    private OnDownloadListener mOnDownloadListener;

    public DownloadUrlTask(String url, File destFile, OnDownloadListener onDownloadListener) {
        mUrl = url;
        mDestFile = destFile;
        mOnDownloadListener = onDownloadListener;
    }

    @Override
    protected State doInBackground(Void... params) {
        deleteFileIfExists();
        return download(mUrl, mDestFile);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int amount = values[0];
        int total = values[1];
        mOnDownloadListener.onDownloadProgressUpdate(amount, total);
    }

    @Override
    protected void onPostExecute(State state) {
        super.onPostExecute(state);
        switch (state) {
            case SUCCESS:
                mOnDownloadListener.onDownloadSuccess();
                break;
            case FAIL:
                mOnDownloadListener.onDownloadFail();
                break;
            case CANCELLED:
                notifyCancelled();
                break;
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        notifyCancelled();
    }

    private void notifyCancelled() {
        mOnDownloadListener.onDownloadCancel();
    }

    private void publish(int amount, int total) {
        publishProgress(amount, total);
    }

    private void deleteFileIfExists() {
        if (mDestFile.exists()) {
            mDestFile.delete();
        }
    }

    private State download(String urlString, File outputFile) {
        BufferedInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(TIMEOUT_MILLI);
            conn.setReadTimeout(TIMEOUT_MILLI);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3)" +
                    " AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");

            int responseCode = conn.getResponseCode();
            Log.d(TAG, String.valueOf(responseCode));
            inputStream = new BufferedInputStream(conn.getInputStream());
            int totalBytes = conn.getContentLength();

            byte[] buffer = new byte[4096];

            int totalBytesRead = 0;
            int bytesRead;
            outputStream = new FileOutputStream(outputFile);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (isCancelled()) {
                    return State.CANCELLED;
                }

                // 다운로드 느리게 테스트할 경우 아래 코드 주석을 풀면 됨
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                outputStream.write(buffer, 0, bytesRead);
                publish(totalBytesRead, totalBytes);
            }

            return State.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(inputStream);
            CloseableUtils.closeQuietly(outputStream);
        }

        return State.FAIL;
    }
}
