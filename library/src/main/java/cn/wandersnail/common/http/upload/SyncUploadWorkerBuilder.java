package cn.wandersnail.common.http.upload;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2021/4/8 14:40
 * author: zengfansheng
 */
public class SyncUploadWorkerBuilder<T> {
    private Converter<ResponseBody, T> converter;
    private Map<String, String> paramParts;
    private OkHttpClient client;
    private Map<String, String> headers;
    private List<FileInfo> fileInfos;
    private String tag;
    private String url;
    private UploadProgressListener listener;

    public SyncUploadWorkerBuilder<T> setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public SyncUploadWorkerBuilder<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public SyncUploadWorkerBuilder<T> setListener(UploadProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public SyncUploadWorkerBuilder<T> setFileParts(List<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
        return this;
    }

    public SyncUploadWorkerBuilder<T> setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 设置响应体转换器
     */
    public SyncUploadWorkerBuilder<T> setConverter(Converter<ResponseBody, T> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * 设置携带的参数
     */
    public SyncUploadWorkerBuilder<T> setParamParts(Map<String, String> paramParts) {
        this.paramParts = paramParts;
        return this;
    }

    /**
     * 设置自定义的OkHttpClient
     */
    public SyncUploadWorkerBuilder<T> setClient(OkHttpClient client) {
        this.client = client;
        return this;
    }

    public SyncUploadWorker<T> build() {
        UploadInfo<T> info;
        if (tag == null || tag.length() == 0) {
            info = new UploadInfo<>(url);
        } else {
            info = new UploadInfo<>(tag, url);
        }
        info.setClient(client)
                .setConverter(converter)
                .setFileParts(fileInfos)
                .setHeaders(headers)
                .setParamParts(paramParts);
        return new SyncUploadWorker<>(info, listener);
    }
}
