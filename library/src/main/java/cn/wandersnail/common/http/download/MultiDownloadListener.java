package cn.wandersnail.common.http.download;

import cn.wandersnail.common.http.callback.MultiProgressListener;

/**
 * 多任务下载监听器
 * <p>
 * date: 2019/8/23 15:40
 * author: zengfansheng
 */
public interface MultiDownloadListener<T extends DownloadInfo> extends DownloadListener<T>, MultiProgressListener {
}
