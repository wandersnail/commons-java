package com.snail.java.network.upload

import com.snail.java.network.TaskInfo

/**
 * 上传监听
 *
 * date: 2019/5/15 11:01
 * author: zengfansheng
 */
interface UploadListener : UploadProgressListener {
    /**
     * 任务上传状态改变
     */
    fun onStateChange(state: TaskInfo.State, t: Throwable?)
}