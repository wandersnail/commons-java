package cn.wandersnail.common.http;

import java.util.concurrent.TimeUnit;

import cn.wandersnail.common.http.callback.RequestCallback;
import cn.wandersnail.common.http.util.HttpUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * date: 2021/4/8 15:42
 * author: zengfansheng
 */
abstract class Requester<T> {
    protected Configuration configuration;
    protected String url;
    protected Converter<ResponseBody, T> converter;

    public abstract ConvertedResponse<T> execute();
    
    public abstract Disposable enqueue(RequestCallback<T> callback);

    protected void handleConfiguration(String url, Configuration configuration) {
        String baseUrl = HttpUtils.getBaseUrl(url);
        Configuration config = configuration == null ? new Configuration() : configuration;
        if (config.retrofit == null) {
            int timeout = config.callTimeout > 0 ? config.callTimeout : 5;
            OkHttpClient client = HttpUtils.initHttpsClient(config.bypassAuth, new OkHttpClient.Builder())
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .build();
            config.retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
        }
        config.service = config.retrofit.create(HttpService.class);
        this.configuration = config;
    }
    
    protected ConvertedResponse<T> execute(Call<ResponseBody> call) {
        return new SyncGeneralRequestTask<>(call, converter, configuration).convertedResp;
    }

    protected Disposable enqueue(Observable<Response<ResponseBody>> observable, RequestCallback<T> callback) {
        return new GeneralRequestTask<>(observable, converter, configuration, callback).disposable;
    }
}
