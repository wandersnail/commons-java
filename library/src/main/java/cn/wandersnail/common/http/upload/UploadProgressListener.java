package cn.wandersnail.common.http.upload;

/**
 * date: 2019/8/23 18:12
 * author: zengfansheng
 */
public interface UploadProgressListener {
    /**
     * 进度更新
     *
     * @param filename 文件名
     * @param progress 已完成的大小
     * @param max      总大小
     */
    void onProgress(String filename, long progress, long max);
}
