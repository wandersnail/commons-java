package cn.wandersnail.common.http;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.wandersnail.common.http.callback.RequestCallback;
import cn.wandersnail.common.http.converter.NothingConverter;
import cn.wandersnail.common.http.download.DownloadInfo;
import cn.wandersnail.common.http.download.DownloadListener;
import cn.wandersnail.common.http.download.DownloadWorker;
import cn.wandersnail.common.http.download.MultiDownloadListener;
import cn.wandersnail.common.http.upload.SyncUploadWorker;
import cn.wandersnail.common.http.upload.UploadInfo;
import cn.wandersnail.common.http.upload.UploadListener;
import cn.wandersnail.common.http.upload.UploadProgressListener;
import cn.wandersnail.common.http.upload.UploadWorker;
import cn.wandersnail.common.http.util.HttpUtils;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * http网络请求，包含普通的get和post、上传、下载
 * <p>
 * date: 2019/8/23 21:08
 * author: zengfansheng
 */
public class EasyHttp {
    static final ExecutorService executorService = Executors.newCachedThreadPool();

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

    private static Configuration getConfiguration(String url, Configuration configuration) {
        String baseUrl = HttpUtils.getBaseUrl(url);
        Configuration config = configuration == null ? new Configuration() : configuration;
        if (config.retrofit == null) {
            OkHttpClient client = HttpUtils.initHttpsClient(config.bypassAuth, new OkHttpClient.Builder())
                    .readTimeout(5, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();
            config.retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
        }
        config.service = config.retrofit.create(HttpService.class);
        return config;
    }

    /**
     * 单个下载
     *
     * @param info     下载信息
     * @param listener 下载监听
     */
    public static <T extends DownloadInfo> DownloadWorker<T> download(T info, DownloadListener<T> listener) {
        return new DownloadWorker<>(info, listener);
    }

    /**
     * 多个同时下载
     *
     * @param infos    下载信息
     * @param listener 下载监听
     */
    public static <T extends DownloadInfo> DownloadWorker<T> download(List<T> infos, MultiDownloadListener<T> listener) {
        return new DownloadWorker<>(infos, listener);
    }

    /**
     * 上传。异步的
     */
    public static <T> UploadWorker<T> enqueueUpload(UploadInfo<T> info, UploadListener<T> listener) {
        return new UploadWorker<>(info, listener);
    }

    /**
     * 上传。同步的
     */
    @NonNull
    public static <T> ConvertedResponse<T> executeUpload(UploadInfo<T> info, UploadProgressListener listener) {
        return new SyncUploadWorker<>(info, listener).convertedResp;
    }

    private static <T> Disposable subscribe(Observable<Response<ResponseBody>> observable, Converter<ResponseBody, T> converter,
                                            Configuration configuration, RequestCallback<T> callback) {
        return new GeneralRequestTask<>(observable, converter, configuration, callback).getDisposable();
    }

    private static <T> ConvertedResponse<T> handleSyncResponse(Call<ResponseBody> call, Converter<ResponseBody, T> converter,
                                                               Configuration configuration) {
        return new SyncGeneralRequestTask<>(call, converter, configuration).convertedResp;
    }

    /**
     * 普通GET请求。异步的
     */
    @NonNull
    public static Disposable enqueueGet(String url, RequestCallback<ResponseBody> callback) {
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.get(url), new NothingConverter(), config, callback);
    }

    /**
     * 普通GET请求。同步的
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executeGet(String url) {
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.getSync(url), new NothingConverter(), config);
    }

    /**
     * 普通GET请求。异步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static Disposable enqueueGet(Configuration configuration, String url, RequestCallback<ResponseBody> callback) {
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.get(url), new NothingConverter(), config, callback);
    }

    /**
     * 普通GET请求。同步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executeGet(Configuration configuration, String url) {
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.getSync(url), new NothingConverter(), config);
    }

    /**
     * 普通GET请求。异步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueueGet(String url, Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.get(url), converter, config, callback);
    }

    /**
     * 普通GET请求。同步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executeGet(String url, Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.getSync(url), converter, config);
    }

    /**
     * 普通GET请求。异步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueueGet(Configuration configuration, String url, Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.get(url), converter, config, callback);
    }

    /**
     * 普通GET请求。同步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executeGet(Configuration configuration, String url, Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.getSync(url), converter, config);
    }

    /**
     * POST请求，body是json。异步的
     */
    @NonNull
    public static Disposable enqueuePostJson(String url, String json, RequestCallback<ResponseBody> callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.postJson(url, requestBody), new NothingConverter(), config, callback);
    }

    /**
     * POST请求，body是json。同步的
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostJson(String url, String json) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postJsonSync(url, requestBody), new NothingConverter(), config);
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static Disposable enqueuePostJson(Configuration configuration, String url, String json, RequestCallback<ResponseBody> callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.postJson(url, requestBody), new NothingConverter(), config, callback);
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostJson(Configuration configuration, String url, String json) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postJsonSync(url, requestBody), new NothingConverter(), config);
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostJson(String url, String json,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.postJson(url, requestBody), converter, config, callback);
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostJson(String url, String json,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postJsonSync(url, requestBody), converter, config);
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostJson(Configuration configuration, String url, String json,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.postJson(url, requestBody), converter, config, callback);
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostJson(Configuration configuration, String url, String json,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postJsonSync(url, requestBody), converter, config);
    }

    /**
     * POST请求，body是字符串。异步的
     */
    @NonNull
    public static Disposable enqueuePostText(String url, String text,
                                      RequestCallback<ResponseBody> callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.post(url, requestBody), new NothingConverter(), config, callback);
    }

    /**
     * POST请求，body是字符串。同步的
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostText(String url, String text) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postSync(url, requestBody), new NothingConverter(), config);
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static Disposable enqueuePostText(Configuration configuration, String url, String text,
                                      RequestCallback<ResponseBody> callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.post(url, requestBody), new NothingConverter(), config, callback);
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostText(Configuration configuration, String url,
                                                           String text) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postSync(url, requestBody), new NothingConverter(), config);
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostText(String url, String text,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.post(url, requestBody), converter, config, callback);
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostText(String url, String text,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postSync(url, requestBody), converter, config);
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostText(Configuration configuration, String url, String text,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.post(url, requestBody), converter, config, callback);
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostText(Configuration configuration, String url, String text,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postSync(url, requestBody), converter, config);
    }

    /**
     * POST提交表单。异步的
     */
    @NonNull
    public static Disposable enqueuePostForm(String url, Map<String, Object> map,
                                      RequestCallback<ResponseBody> callback) {
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.postForm(url, map), new NothingConverter(), config, callback);
    }

    /**
     * POST提交表单。同步的
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostForm(String url, Map<String, Object> map) {
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postFormSync(url, map), new NothingConverter(), config);
    }

    /**
     * POST提交表单。异步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static Disposable enqueuePostForm(Configuration configuration, String url, Map<String, Object> map,
                                      RequestCallback<ResponseBody> callback) {
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.postForm(url, map), new NothingConverter(), config, callback);
    }

    /**
     * POST提交表单。同步的
     *
     * @param configuration 配置
     */
    @NonNull
    public static ConvertedResponse<ResponseBody> executePostForm(Configuration configuration, String url,
                                                           Map<String, Object> map) {
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postFormSync(url, map), new NothingConverter(), config);
    }

    /**
     * POST提交表单。异步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostForm(String url, Map<String, Object> map,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, null);
        return subscribe(config.service.postForm(url, map), converter, config, callback);
    }

    /**
     * POST提交表单。同步的
     *
     * @param converter 响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostForm(String url, Map<String, Object> map,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, null);
        return handleSyncResponse(config.service.postFormSync(url, map), converter, config);
    }

    /**
     * POST提交表单。异步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> Disposable enqueuePostForm(Configuration configuration, String url, Map<String, Object> map,
                                          Converter<ResponseBody, T> converter, RequestCallback<T> callback) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, configuration);
        return subscribe(config.service.postForm(url, map), converter, config, callback);
    }

    /**
     * POST提交表单。同步的
     *
     * @param configuration 配置
     * @param converter     响应体转换器
     */
    @NonNull
    public static <T> ConvertedResponse<T> executePostForm(Configuration configuration, String url, Map<String, Object> map,
                                                    Converter<ResponseBody, T> converter) {
        Objects.requireNonNull(converter, "converter can't be null");
        Configuration config = getConfiguration(url, configuration);
        return handleSyncResponse(config.service.postFormSync(url, map), converter, config);
    }
}
