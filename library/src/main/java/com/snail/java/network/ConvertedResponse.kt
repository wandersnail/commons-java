package com.snail.java.network

import okhttp3.Response

/**
 * 转换过的响应数据
 *
 * date: 2019/5/16 20:08
 * author: zengfansheng
 * 
 * @param raw 原始响应
 */
class ConvertedResponse<T>(val raw: Response) {
    var convertedBody: T? = null
    /**
     * 转换异常
     */
    var convertRrror: Throwable? = null
}