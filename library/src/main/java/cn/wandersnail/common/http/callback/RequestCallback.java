package cn.wandersnail.common.http.callback;

import okhttp3.Response;

/**
 * 请求结果回调
 * <p>
 * date: 2019/8/23 15:22
 * author: zengfansheng
 */
public interface RequestCallback<T> {
    /**
     * 请求成功
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    void onSuccess(Response response, T convertedResponse);

    void onError(Throwable t);
}
