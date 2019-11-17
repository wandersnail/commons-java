package cn.wandersnail.common.http.download;


import java.io.File;
import java.util.Objects;
import java.util.UUID;

import cn.wandersnail.common.http.TaskInfo;


/**
 * 下载信息类，包含下载状态及进度监听
 * <p>
 * date: 2019/8/23 15:28
 * author: zengfansheng
 */
public class DownloadInfo extends TaskInfo {
    public final String savePath;
    long contentLength;//总长度
    long completionLength;//已完成长度
    private final String tempFilename;

    /**
     * @param url      请求地址
     * @param savePath 文件保存路径
     */
    public DownloadInfo(String url, String savePath) {
        this(UUID.randomUUID().toString(), url, savePath);
    }

    /**
     * @param tag      唯一标识
     * @param url      请求地址
     * @param savePath 文件保存路径
     */
    public DownloadInfo(String tag, String url, String savePath) {
        super(tag, url);
        this.savePath = savePath;
        tempFilename = UUID.randomUUID().toString();
    }

    /**
     * 进度：已完成长度
     */
    public long getCompletionLength() {
        return completionLength;
    }

    /**
     * 进度：总长度
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * 获取下载的临时文件，下载完成后再重命名
     */
    public File getTemporaryFile() {
        return new File(System.getProperty("java.io.tmpdir"), tempFilename);
    }
    
    @Override
    public void reset() {
        completionLength = 0;
        contentLength = 0;
        getTemporaryFile().delete();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadInfo)) return false;
        DownloadInfo that = (DownloadInfo) o;
        return savePath.equals(that.savePath) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(savePath, url);
    }
}
