package com.snail.java.network.upload

import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.URLEncoder

/**
 * 上传执行者
 *
 * date: 2019/2/28 18:21
 * author: zengfansheng
 */
class UploadWorker<T> @JvmOverloads internal constructor(info: UploadInfo<T>, listener: UploadListener<T>? = null) : Disposable {
    private val observer = UploadObserver(info, listener)

    init {
        val builder = Retrofit.Builder()
        if (info.client != null) {
            builder.client(info.client)
        }
        val service = builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(info.baseUrl)
            .build()
            .create(UploadService::class.java)
        val parts = HashMap<String, @JvmSuppressWildcards RequestBody>()
        info.paramParts.entries.forEach { parts[it.key] = RequestBody.create(null, it.value) }
        info.fileParts.entries.forEach {
            MultipartBody.Part.createFormData(it.key, URLEncoder.encode(it.value.name, "utf-8"),
                ProgressRequestBody(MediaType.parse("multipart/form-data"), it.key, it.value, observer))
        }
        service.upload(info.url, parts).subscribe(observer)
    }

    override fun dispose() {
        observer.dispose()
    }

    override fun isDisposed(): Boolean {
        return observer.isDisposed
    }

    fun cancel() {
        dispose()
    }
}