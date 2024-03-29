package cn.wandersnail.common.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * date: 2019/8/23 21:03
 * author: zengfansheng
 */
class SyncGeneralRequestTask<T> {
    private boolean complete;
    final ConvertedResponse<T> convertedResp;

    SyncGeneralRequestTask(Call<ResponseBody> call, Converter<ResponseBody, T> converter, Configuration configuration) {
        convertedResp = new ConvertedResponse<>(call);
        if (configuration.callTimeout > 0) {
            EasyHttp.executorService.execute(() -> {
                int secondCount = 0;
                while (secondCount++ < configuration.callTimeout) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                    if (complete) {
                        return;
                    }
                }
                //如果到了这里，说明超时了，取消请求
                convertedResp.isCallTimeout = true;
                call.cancel();
            });
        }
        try {
            Response<ResponseBody> response = call.execute();
            complete = true;
            convertedResp.response = response;
            if (response.isSuccessful()) {
                try {
                    ResponseBody body = response.body();
                    if (body != null) {
                        convertedResp.convertedResponse = converter == null ? (T) body : converter.convert(body);                   
                    }
                } catch (Throwable t) {
                    convertedResp.convertError = t;
                }
            }
        } catch (Exception e) {
            //取消任务会抛异常
        }
    }
}
