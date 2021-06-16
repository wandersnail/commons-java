package cn.wandersnail.common.http.callback;


/**
 * date: 2021/6/11 11:41
 * author: zengfansheng
 */
public interface JsonParser<T> {
    T parse(String jsonStr);
}
