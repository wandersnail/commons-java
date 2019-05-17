package com.snail.java.network

import com.snail.java.network.callback.RequestCallback
import com.snail.java.network.converter.OriginalResponseConverter
import com.snail.java.network.converter.ResponseConverter
import com.snail.java.network.download.DownloadInfo
import com.snail.java.network.download.DownloadListener
import com.snail.java.network.download.DownloadWorker
import com.snail.java.network.download.MultiDownloadListener
import com.snail.java.network.upload.*
import com.snail.java.network.utils.HttpUtils
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.Executors


/**
 * http网络请求，包含普通的get和post、上传、下载
 *
 * date: 2019/2/23 16:37
 * author: zengfansheng
 */
object NetworkRequester {
    internal val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    
    private fun getConfiguration(baseUrl: String, configuration: Configuration?): Configuration {
        val url = HttpUtils.getBaseUrl(baseUrl)
        val config = configuration ?: Configuration()
        if (config.retrofit == null) {
            config.retrofit = Retrofit.Builder().baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
        config.service = config.retrofit!!.create(HttpService::class.java)
        return config
    }

    /**
     * 单个下载
     *
     * @param info 下载信息
     * @param listener 下载监听
     */
    @JvmStatic
    fun <T : DownloadInfo> download(info: T, listener: DownloadListener<T>?): DownloadWorker<T> {
        return DownloadWorker(info, listener)
    }

    /**
     * 多个同时下载
     *
     * @param infos 下载信息
     * @param listener 下载监听
     */
    @JvmStatic
    fun <T : DownloadInfo> download(infos: List<T>, listener: MultiDownloadListener<T>?): DownloadWorker<T> {
        return DownloadWorker(infos, listener)
    }

    /**
     * 上传。异步的
     */
    @JvmStatic
    @JvmOverloads
    fun <T> upload(info: UploadInfo<T>, listener: UploadListener<T>? = null): UploadWorker<T> {
        return UploadWorker(info, listener)
    }

    /**
     * 上传。同步的
     */
    @JvmStatic
    @JvmOverloads
    fun <T> uploadSync(info: UploadInfo<T>, listener: UploadProgressListener? = null): ConvertedResponse<T> {
        return SyncUploadWorker(info, listener).convertedResponse
    }

    private fun <T> subscribe(observable: Observable<Response<ResponseBody>>, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        return observable.subscribeOn(Schedulers.from(executor)).subscribe({
            try {
                callback?.onSuccess(it.raw(), converter.convert(it.body()))
            } catch (t: Throwable) {
                callback?.onError(t)
            }
        }, {
            callback?.onError(it)
        })
    }
    
    private fun <T> handleSyncResponse(call: Call<ResponseBody>, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val convertedResponse = ConvertedResponse<T>(call)
        try {
            val response = call.execute()
            convertedResponse.raw = response.raw()
            if (response.isSuccessful) {
                try {
                    convertedResponse.convertedBody = converter.convert(response.body())
                } catch (t: Throwable) {
                    convertedResponse.convertRrror = t
                }
            }
        } catch (e: Exception) {
            //取消任务会抛异常
        }        
        return convertedResponse
    }
    
    /**
     * 普通GET请求。异步的
     */
    @JvmStatic
    fun get(url: String, callback: RequestCallback<ResponseBody>?): Disposable {
        return subscribe(getConfiguration(url, null).service!!.get(url), OriginalResponseConverter(), callback)
    }

    /**
     * 普通GET请求。同步的
     */
    @JvmStatic
    fun get(url: String): ConvertedResponse<ResponseBody> {
        val call = getConfiguration(url, null).service!!.getSync(url)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * 普通GET请求。异步的
     */
    @JvmStatic
    fun get(configuration: Configuration, url: String, callback: RequestCallback<ResponseBody>?): Disposable {
        return subscribe(getConfiguration(url, configuration).service!!.get(url), OriginalResponseConverter(), callback)
    }

    /**
     * 普通GET请求。同步的
     */
    @JvmStatic
    fun get(configuration: Configuration, url: String): ConvertedResponse<ResponseBody> {
        val call = getConfiguration(url, configuration).service!!.getSync(url)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * 普通GET请求。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(url: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        return subscribe(getConfiguration(url, null).service!!.get(url), converter, callback)
    }

    /**
     * 普通GET请求。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(url: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val call = getConfiguration(url, null).service!!.getSync(url)
        return handleSyncResponse(call, converter)
    }

    /**
     * 普通GET请求。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(configuration: Configuration, url: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        return subscribe(getConfiguration(url, configuration).service!!.get(url), converter, callback)
    }

    /**
     * 普通GET请求。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> get(configuration: Configuration, url: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val call = getConfiguration(url, configuration).service!!.getSync(url)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param url 请求的url
     */
    @JvmStatic
    fun postJson(url: String, json: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        return subscribe(getConfiguration(url, null).service!!.postJson(url, requestBody), OriginalResponseConverter(), callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param url 请求的url
     */
    @JvmStatic
    fun postJson(url: String, json: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val call = getConfiguration(url, null).service!!.postJsonSync(url, requestBody)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param url 请求的url
     */
    fun postJson(configuration: Configuration, url: String, json: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        return subscribe(getConfiguration(url, configuration).service!!.postJson(url, requestBody), OriginalResponseConverter(), callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param url 请求的url
     */
    fun postJson(configuration: Configuration, url: String, json: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val call = getConfiguration(url, configuration).service!!.postJsonSync(url, requestBody)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(url: String, json: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = getConfiguration(url, null).service!!.postJson(url, requestBody)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(url: String, json: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val call = getConfiguration(url, null).service!!.postJsonSync(url, requestBody)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST请求，body是json。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(configuration: Configuration, url: String, json: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val observable = getConfiguration(url, configuration).service!!.postJson(url, requestBody)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST请求，body是json。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postJson(configuration: Configuration, url: String, json: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
        val call = getConfiguration(url, configuration).service!!.postJsonSync(url, requestBody)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST请求，body是字符串。异步的
     */
    @JvmStatic
    fun postText(url: String, text: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        return subscribe(getConfiguration(url, null).service!!.post(url, requestBody), OriginalResponseConverter(), callback)
    }

    /**
     * POST请求，body是字符串。同步的
     */
    @JvmStatic
    fun postText(url: String, text: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val call = getConfiguration(url, null).service!!.postSync(url, requestBody)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST请求，body是字符串。异步的
     */
    @JvmStatic
    fun postText(configuration: Configuration, url: String, text: String, callback: RequestCallback<ResponseBody>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        return subscribe(getConfiguration(url, configuration).service!!.post(url, requestBody), OriginalResponseConverter(), callback)
    }

    /**
     * POST请求，body是字符串。同步的
     */
    @JvmStatic
    fun postText(configuration: Configuration, url: String, text: String): ConvertedResponse<ResponseBody> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val call = getConfiguration(url, configuration).service!!.postSync(url, requestBody)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(url: String, text: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = getConfiguration(url, null).service!!.post(url, requestBody)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(url: String, text: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val call = getConfiguration(url, null).service!!.postSync(url, requestBody)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST请求，body是字符串。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(configuration: Configuration, url: String, text: String, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val observable = getConfiguration(url, configuration).service!!.post(url, requestBody)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST请求，body是字符串。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postText(configuration: Configuration, url: String, text: String, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), text)
        val call = getConfiguration(url, configuration).service!!.postSync(url, requestBody)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST提交表单。异步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(url: String, map: Map<String, Any>, callback: RequestCallback<ResponseBody>?): Disposable {
        return subscribe(getConfiguration(url, null).service!!.postForm(url, map), OriginalResponseConverter(), callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(url: String, map: Map<String, Any>): ConvertedResponse<ResponseBody> {
        val call = getConfiguration(url, null).service!!.postFormSync(url, map)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST提交表单。异步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(configuration: Configuration, url: String, map: Map<String, Any>, callback: RequestCallback<ResponseBody>?): Disposable {
        return subscribe(getConfiguration(url, configuration).service!!.postForm(url, map), OriginalResponseConverter(), callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param map 参数集合
     */
    @JvmStatic
    fun postForm(configuration: Configuration, url: String, map: Map<String, Any>): ConvertedResponse<ResponseBody> {
        val call = getConfiguration(url, configuration).service!!.postFormSync(url, map)
        return handleSyncResponse(call, OriginalResponseConverter())
    }

    /**
     * POST提交表单。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(url: String, map: Map<String, Any>, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val observable = getConfiguration(url, null).service!!.postForm(url, map)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(url: String, map: Map<String, Any>, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val call = getConfiguration(url, null).service!!.postFormSync(url, map)
        return handleSyncResponse(call, converter)
    }

    /**
     * POST提交表单。异步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(configuration: Configuration, url: String, map: Map<String, Any>, converter: ResponseConverter<T>, callback: RequestCallback<T>?): Disposable {
        val observable = getConfiguration(url, configuration).service!!.postForm(url, map)
        return subscribe(observable, converter, callback)
    }

    /**
     * POST提交表单。同步的
     *
     * @param converter 响应体转换器
     * @param T 转到成的对象类
     */
    @JvmStatic
    fun <T> postForm(configuration: Configuration, url: String, map: Map<String, Any>, converter: ResponseConverter<T>): ConvertedResponse<T> {
        val call = getConfiguration(url, configuration).service!!.postFormSync(url, map)
        return handleSyncResponse(call, converter)
    }
}