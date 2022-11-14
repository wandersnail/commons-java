package cn.wandersnail.common.http.converter;

import com.alibaba.fastjson2.JSON;

import cn.wandersnail.common.http.EasyHttp;
import cn.wandersnail.common.http.callback.JsonParser;
import cn.wandersnail.common.http.exception.ConvertException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 响应体为json字符串
 * 
 * date: 2019/8/23 15:13
 * author: zengfansheng
 */
public class JsonResponseConverter<T> implements Converter<ResponseBody, T> {
    private final Class<T> cls;
    private final JsonParser<T> parser;
    private JsonParserType parserType = JsonParserType.FASTJSON2;

    public JsonResponseConverter(Class<T> cls) {
        this.cls = cls;
        parser = null;
    }

    public JsonResponseConverter(Class<T> cls, JsonParserType parserType) {
        this.cls = cls;
        this.parserType = parserType;
        parser = null;
    }

    public JsonResponseConverter(JsonParser<T> parser) {
        this.parser = parser;
        cls = null;
    }

    @Override
    public T convert(ResponseBody value) throws ConvertException {
        try {
            if (parser != null) {
                return parser.parse(value.string());
            } else if (parserType == JsonParserType.FASTJSON2 && isFastjson2Supported()) {
                return JSON.parseObject(value.string(), cls);
            } else if (parserType == JsonParserType.FASTJSON && isFastjsonSupported()) {
                return com.alibaba.fastjson.JSON.parseObject(value.string(), cls);
            } else if (parserType == JsonParserType.GSON && isGsonSupported()) {
                return EasyHttp.getGson().fromJson(value.string(), cls);
            } else {
                throw new ConvertException("没有可用的Body转换器");
            }
        } catch (Throwable e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
    
    private boolean isFastjsonSupported() {
        try {
            Class.forName("com.alibaba.fastjson.JSON");
            return true;
        } catch (ClassNotFoundException ignore) {
        }
        return false;
    }
    
    private boolean isFastjson2Supported() {
        try {
            Class.forName("com.alibaba.fastjson2.JSON");
            return true;
        } catch (ClassNotFoundException ignore) {
        }
        return false;
    }
    
    private boolean isGsonSupported() {
        try {
            Class.forName("com.google.gson.Gson");
            return true;
        } catch (ClassNotFoundException ignore) {
        }
        return false;
    }
}
