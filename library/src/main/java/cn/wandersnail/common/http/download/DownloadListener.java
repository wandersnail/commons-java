package cn.wandersnail.common.http.download;

/**
 * 下载监听
 * <p>
 * date: 2019/8/23 15:36
 * author: zengfansheng
 */
public interface DownloadListener<T extends DownloadInfo> {
    /**
     * 下载状态改变
     *
     * @param info 当前下载信息
     * @param t    当下载出错时，会有异常
     */
    void onStateChange(T info, Throwable t);

    /**
     * 下载进度变化
     *
     * @param info 当前下载信息
     */
    void onProgress(T info);
}
