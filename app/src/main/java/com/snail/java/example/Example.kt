package com.snail.java.example

import com.snail.java.network.NetworkRequester
import com.snail.java.network.callback.RequestCallback
import com.snail.java.network.converter.StringResponseConverter
import com.snail.java.utils.toDetailMsg

/**
 *
 *
 * date: 2019/5/1 18:33
 * author: zengfansheng
 */
class Example {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            NetworkRequester.get("https://www.baidu.com", StringResponseConverter(), object : RequestCallback<String> {
                override fun onSuccess(parsedResp: String) {
                    println(parsedResp)
                    System.exit(0)
                }

                override fun onError(t: Throwable) {
                    println(t.toDetailMsg())
                    System.exit(0)
                }
            })
        }
    }
}