package cn.wandersnail.common.http;

import java.util.HashMap;
import java.util.Map;

import cn.wandersnail.common.http.callback.RequestCallback;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2021/4/8 16:39
 * author: zengfansheng
 */
public class DeleteRequester<T> extends Requester<T> {
    private RequestBody body;
    private Map<String, Object> params;
    private boolean isJsonBody;

    /**
     * 自定义配置
     */
    public DeleteRequester<T> setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * 请求路径
     */
    public DeleteRequester<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 响应体转换器
     */
    public DeleteRequester<T> setConverter(Converter<ResponseBody, T> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * 文本请求体
     */
    public DeleteRequester<T> setTextBody(String text) {
        body = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text);
        return this;
    }

    /**
     * json请求体
     */
    public DeleteRequester<T> setJsonBody(String json) {
        body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json);
        isJsonBody = true;
        return this;
    }

    /**
     * 自定义请求体
     */
    public DeleteRequester<T> setBody(RequestBody body) {
        this.body = body;
        return this;
    }

    /**
     * 请求参数
     */
    public DeleteRequester<T> setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    @Override
    public ConvertedResponse<T> execute() {
        handleConfiguration(url, configuration);
        if (isJsonBody) {
            if (configuration.headers == null) {
                configuration.headers = new HashMap<>();
            }
            Map<String, String> headers = configuration.headers;
            if (headers.get("Content-Type") == null) {
                headers.put("Content-Type", "application/json");
            }
        }
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            if (params != null) {
                if (body != null) {
                    return execute(configuration.service.deleteSync(url, configuration.headers, params, body));
                } else {
                    return execute(configuration.service.deleteParamsSync(url, configuration.headers, params));
                }
            } else if (body != null) {
                return execute(configuration.service.deleteSync(url, configuration.headers, body));
            } else {
                return execute(configuration.service.deleteSync(url, configuration.headers));
            }
        } else {
            if (params != null) {
                if (body != null) {
                    return execute(configuration.service.deleteParamsAndBodySync(url, params, body));
                } else {
                    return execute(configuration.service.deleteParamsSync(url, params));
                }
            } else if (body != null) {
                return execute(configuration.service.deleteSync(url, body));
            } else {
                return execute(configuration.service.deleteSync(url));
            }
        }
    }

    @Override
    public Disposable enqueue(RequestCallback<T> callback) {
        handleConfiguration(url, configuration);
        if (isJsonBody) {
            if (configuration.headers == null) {
                configuration.headers = new HashMap<>();
            }
            Map<String, String> headers = configuration.headers;
            if (headers.get("Content-Type") == null) {
                headers.put("Content-Type", "application/json");
            }
        }
        if (configuration.headers != null && !configuration.headers.isEmpty()) {
            if (params != null) {
                if (body != null) {
                    return enqueue(configuration.service.deleteParamsAndBody(url, configuration.headers, params, body), callback);
                } else {
                    return enqueue(configuration.service.deleteParams(url, configuration.headers, params), callback);
                }
            } else if (body != null) {
                return enqueue(configuration.service.delete(url, configuration.headers, body), callback);
            } else {
                return enqueue(configuration.service.delete(url, configuration.headers), callback);
            }
        } else {
            if (params != null) {
                if (body != null) {
                    return enqueue(configuration.service.deleteParamsAndBody(url, params, body), callback);
                } else {
                    return enqueue(configuration.service.deleteParams(url, params), callback);
                }
            } else if (body != null) {
                return enqueue(configuration.service.delete(url, body), callback);
            } else {
                return enqueue(configuration.service.delete(url), callback);
            }
        }
    }
}
