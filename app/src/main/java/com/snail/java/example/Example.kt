package com.snail.java.example

import com.snail.java.network.NetworkRequester
import com.snail.java.network.TaskInfo
import com.snail.java.network.callback.RequestCallback
import com.snail.java.network.converter.JsonResponseConverter
import com.snail.java.network.converter.StringResponseConverter
import com.snail.java.network.upload.UploadInfo
import com.snail.java.network.upload.UploadListener
import com.snail.java.utils.getMD5Code
import com.snail.java.utils.toDetailMsg
import okhttp3.Response
import java.io.File

/**
 *
 *
 * date: 2019/5/1 18:33
 * author: zengfansheng
 */
object Example {
    @JvmStatic
    fun main(args: Array<String>) {
        val info = UploadInfo("http://120.78.49.23:8080/CM/api?action=1007", 
            JsonResponseConverter(BaseResponse::class.java), mapOf(Pair("mailaddress", "16607735715")), 
            mapOf(Pair("file", File("C:\\Users\\zfs\\Desktop\\horse.png"))))
        NetworkRequester.upload(info, object : UploadListener<BaseResponse> {
            override fun onProgress(name: String, progress: Long, max: Long) {
            }

            override fun onResponseBodyParse(response: Response, convertedBody: BaseResponse?) {
                println("BaseResponse: code=${convertedBody?.code}, msg: ${convertedBody?.message}")
            }

            override fun onStateChange(state: TaskInfo.State, t: Throwable?) {
                if (info.state == TaskInfo.State.ERROR || info.state == TaskInfo.State.CANCEL) {
                    println("uploadUserFigure error: $t")
                } else if (info.state == TaskInfo.State.COMPLETED) {
                    println("上传成功")
                }
            }
        })
    }

    private fun testNetwork() {
        NetworkRequester.get("https://www.baidu.com", StringResponseConverter(), object : RequestCallback<String> {
            override fun onSuccess(response: Response, convertedBody: String?) {
                println(convertedBody)
                System.exit(0)
            }

            override fun onError(t: Throwable) {
                println(t.toDetailMsg())
                System.exit(0)
            }
        })
    }
    
    private fun testFileMD5() {
        println(File("C:\\Users\\zeng_\\Desktop\\血族.The.Strain.S04E04.中英字幕.WEBRip.AAC.720p.x264-人人影视.mp4").getMD5Code())
    }
}