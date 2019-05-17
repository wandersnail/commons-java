package com.snail.java.network.upload

import com.snail.java.network.TaskInfo
import com.snail.java.network.converter.ResponseConverter
import okhttp3.OkHttpClient
import java.io.File
import java.util.*

/**
 * 上传任务信息
 *
 * date: 2019/2/28 13:00
 * author: zengfansheng
 *
 * @param converter 响应体转换器
 * @param paramParts 携带的参数
 * @param fileParts 多文件，key：为form-data; name=key
 * @param client 使用自定义的OkHttpClient
 */
open class UploadInfo<R>
@JvmOverloads constructor(url: String, internal val converter: ResponseConverter<R>, val paramParts: Map<String, String>, val fileParts: Map<String, File>,
                          internal val client: OkHttpClient? = null, tag: String = UUID.randomUUID().toString()) : TaskInfo(url, tag)