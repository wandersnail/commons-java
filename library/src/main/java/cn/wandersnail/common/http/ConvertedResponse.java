package cn.wandersnail.common.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 转换过的响应数据
 * 
 * date: 2019/8/23 20:51
 * author: zengfansheng
 */
public class ConvertedResponse<T> {
    private final Call<ResponseBody> call;
    /**
     * 转换过的响应数据
     */
    public T convertedResponse;
    /**
     * 转换异常
     */
    public Throwable convertError;
    /**
     * 原始响应
     */
    public Response<ResponseBody> response;
    /**
     * 是否请求超时了
     */
    public boolean isCallTimeout;

    public ConvertedResponse(Call<ResponseBody> call) {
        this.call = call;
    }

    /**
     * 取消
     */
    public void cancel() {
        if (call.isExecuted() && !call.isCanceled()) {
            call.cancel();
        }
    }

    /**
     * 是否被取消了
     */
    public boolean isCanceled() {
        return call.isCanceled();
    }
}
