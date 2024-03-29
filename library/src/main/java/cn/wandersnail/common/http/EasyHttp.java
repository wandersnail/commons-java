package cn.wandersnail.common.http;


import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.wandersnail.common.http.download.DownloadWorkerBuilder;
import cn.wandersnail.common.http.download.MultiDownloadWorkerBuilder;
import cn.wandersnail.common.http.upload.SyncUploadWorkerBuilder;
import cn.wandersnail.common.http.upload.UploadWorkerBuilder;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * http网络请求，包含普通的get和post、上传、下载
 * <p>
 * date: 2019/8/23 21:08
 * author: zengfansheng
 */
public class EasyHttp {
    static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static Gson gson;

    static {
        RxJavaPlugins.setErrorHandler(t -> {
            if (t instanceof UndeliverableException) {
                t = t.getCause() == null ? new Throwable(t) : t.getCause();
            }
            if (t instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (t instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if (t instanceof NullPointerException || t instanceof IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
                return;
            }
            if (t instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
            }
        });
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static void setGson(Gson gson) {
        EasyHttp.gson = gson;
    }

    /**
     * 单文件下载
     */
    public static DownloadWorkerBuilder singleDownloadWorkerBuilder() {
        return new DownloadWorkerBuilder();
    }

    /**
     * 多文件下载
     */
    public static MultiDownloadWorkerBuilder multiDownloadWorkerBuilder() {
        return new MultiDownloadWorkerBuilder();
    }

    /**
     * 异步上传
     */
    public static <T> UploadWorkerBuilder<T> uploadWorkerBuilder() {
        return new UploadWorkerBuilder<>();
    }

    /**
     * 异步上传
     */
    public static <T> UploadWorkerBuilder<T> uploadWorkerBuilder(Class<T> cls) {
        return new UploadWorkerBuilder<>();
    }

    /**
     * 同步上传
     */
    public static <T> SyncUploadWorkerBuilder<T> syncUploadWorkerBuilder() {
        return new SyncUploadWorkerBuilder<>();
    }
    
    /**
     * 同步上传
     */
    public static <T> SyncUploadWorkerBuilder<T> syncUploadWorkerBuilder(Class<T> cls) {
        return new SyncUploadWorkerBuilder<>();
    }

    /**
     * GET请求器
     */
    public static <T> GetRequester<T> getRequester() {
        return new GetRequester<>();
    }

    /**
     * GET请求器
     */
    public static <T> GetRequester<T> getRequester(Class<T> cls) {
        return new GetRequester<>();
    }

    /**
     * POST请求器
     */
    public static <T> PostRequester<T> postRequester() {
        return new PostRequester<>();
    }

    /**
     * POST请求器
     */
    public static <T> PostRequester<T> postRequester(Class<T> cls) {
        return new PostRequester<>();
    }

    /**
     * DELETE请求
     */
    public static <T> DeleteRequester<T> deleteRequester() {
        return new DeleteRequester<>();
    }

    /**
     * DELETE请求
     */
    public static <T> DeleteRequester<T> deleteRequester(Class<T> cls) {
        return new DeleteRequester<>();
    }
}
