package com.snail.java.example

/**
 * 描述:
 * 时间: 2018/9/12 10:44
 * 作者: zengfansheng
 */
open class BaseResponse {
    var code: Int = 0
    var msg: String? = null
    
    override fun toString(): String {
        return "BaseResponse(code=$code, msg=$msg)"
    }
}
