package cn.wandersnail.common.http;


import java.util.concurrent.TimeoutException;

import cn.wandersnail.common.http.callback.RequestCallback;
import cn.wandersnail.common.http.util.SchedulerUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * 一般的请求任务
 * <p>
 * date: 2019/8/23 21:19
 * author: zengfansheng
 */
class GeneralRequestTask<T> {
    Disposable disposable;

    GeneralRequestTask(Observable<Response<ResponseBody>> observable, Converter<ResponseBody, T> converter,
                              Configuration configuration, RequestCallback<T> callback) {
        //只有设置过超时才计
        if (configuration.callTimeout > 0) {
            EasyHttp.executorService.execute(() -> {
                try {
                    Thread.sleep(configuration.callTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (GeneralRequestTask.this) {
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                    disposable = null;
                    if (callback != null) {
                        callback.onError(new TimeoutException("Http request timeout!"));
                    }
                }
            });
        }
        disposable = observable.compose(SchedulerUtils.applyGeneralObservableSchedulers())
                .subscribe(response -> {
                    disposable = null;
                    if (callback != null) {
                        T successBody = null;
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            successBody = body == null ? null : (converter == null ? (T) body : converter.convert(body));
                            callback.onSuccess(response, successBody);
                        }
                        callback.onResponse(response, successBody, response.errorBody());
                    }
                }, throwable -> {
                    disposable = null;
                    if (callback != null) {
                        callback.onError(throwable);
                    }
                }, () -> disposable = null);
    }

    Disposable getDisposable() {
        return disposable;
    }
}
