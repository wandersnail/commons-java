package cn.wandersnail.common.http.download;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * date: 2021/4/8 14:20
 * author: zengfansheng
 */
public class MultiDownloadWorkerBuilder {
    private final List<DownloadInfo> infoList = new ArrayList<>();
    private MultiDownloadListener<DownloadInfo> listener;

    /**
     * 添加下载文件信息
     *
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public MultiDownloadWorkerBuilder addFileInfo(String url, String savePath) {
        return addFileInfo(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * 添加下载文件信息
     *
     * @param tag      下载任务标识
     * @param url      下载地址
     * @param savePath 文件保存路径
     */
    public MultiDownloadWorkerBuilder addFileInfo(String tag, String url, String savePath) {
        DownloadInfo info = new DownloadInfo(tag, url, savePath);
        if (!infoList.contains(info)) {
            infoList.add(info);
        }
        return this;
    }

    /**
     * 设置下载进度监听器
     */
    public MultiDownloadWorkerBuilder setListener(MultiDownloadListener<DownloadInfo> listener) {
        this.listener = listener;
        return this;
    }

    public DownloadWorker<DownloadInfo> build() {
        return new DownloadWorker<>(infoList, listener);
    }
}
