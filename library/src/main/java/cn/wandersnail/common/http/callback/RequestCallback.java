package cn.wandersnail.common.http.callback;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 请求结果回调
 * <p>
 * date: 2019/8/23 15:22
 * author: zengfansheng
 */
public interface RequestCallback<T> {
    /**
     * 请求成功。已废弃！请使用{@link #onSuccess(retrofit2.Response, Object)}
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    @Deprecated
    default void onSuccess(Response response, T convertedResponse) {
    }

    void onError(Throwable t);

    /**
     * 请求成功
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    @Deprecated
    default void onSuccess(retrofit2.Response<ResponseBody> response, T convertedResponse) {
    }

    /**
     * 响应回调
     *
     * @param response    原始响应
     * @param successBody 请求成功时，经过设置的转换器转换后的结果
     * @param errorBody   错误响应的body
     */
    void onResponse(retrofit2.Response<ResponseBody> response, T successBody, ResponseBody errorBody);
}
