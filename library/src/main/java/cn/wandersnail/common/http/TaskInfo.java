package cn.wandersnail.common.http;

import java.util.UUID;

import cn.wandersnail.common.http.util.HttpUtils;

/**
 * 任务信息
 * <p>
 * date: 2019/8/23 13:43
 * author: zengfansheng
 */
public class TaskInfo {
    /**
     * 唯一标识
     */
    public final String tag;
    /**
     * 请求地址
     */
    public final String url;
    /**
     * 任务状态
     */
    public State state = State.IDLE;

    public TaskInfo(String url) {
        this(UUID.randomUUID().toString(), url);
    }

    /**
     * @param tag 唯一标识
     * @param url 请求地址
     */
    public TaskInfo(String tag, String url) {
        this.tag = tag;
        this.url = url;
    }

    public String getBaseUrl() {
        return HttpUtils.getBaseUrl(url);
    }

    public void reset() {
    }

    public enum State {
        IDLE, START, ONGOING, COMPLETED, CANCEL, ERROR, PAUSE
    }
}
