package cn.wandersnail.common.http.converter;

import com.alibaba.fastjson.JSON;

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
    private JsonParserType parserType = JsonParserType.FASTJSON;

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
        if (value == null) {
            throw new ConvertException("ResponseBody is null");
        }
        try {
            if (parser != null) {
                return parser.parse(value.string());
            } else if (parserType == JsonParserType.FASTJSON) {
                return JSON.parseObject(value.string(), cls);
            } else {
                return EasyHttp.getGson().fromJson(value.string(), cls);
            }
        } catch (Throwable e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
}
