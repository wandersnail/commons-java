package cn.wandersnail.common.http.upload;

import cn.wandersnail.common.http.TaskInfo;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * date: 2019/8/23 18:08
 * author: zengfansheng
 */
public interface UploadListener<T> extends UploadProgressListener {
    /**
     * 任务上传状态改变
     */
    void onStateChange(TaskInfo.State state, Throwable t);

    /**
     * 响应结果
     *
     * @param response          原始响应
     * @param convertedResponse 经过设置的转换器转换后的结果
     */
    void onResponseBodyParse(Response<ResponseBody> response, T convertedResponse);

    /**
     * 转换错误
     */
    default void onConvertError(Throwable t) {}
}
