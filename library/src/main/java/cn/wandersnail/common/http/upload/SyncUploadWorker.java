package cn.wandersnail.common.http.upload;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import cn.wandersnail.common.http.ConvertedResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 同步上传任务
 * <p>
 * date: 2019/8/23 20:38
 * author: zengfansheng
 */
public class SyncUploadWorker<T> {
    public ConvertedResponse<T> convertedResp;

    public SyncUploadWorker(UploadInfo<T> info, UploadProgressListener listener) {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (info.client != null) {
            builder.client(info.client);
        }
        UploadService service = builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(info.getBaseUrl())
                .build()
                .create(UploadService.class);
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        if (info.paramParts != null) {
            for (Map.Entry<String, String> entry : info.paramParts.entrySet()) {
                bodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        UploadProgressListener localListener = (name, progress, max) -> {
            if (listener != null) {
                listener.onProgress(name, progress, max);
            }
        };
        for (Map.Entry<String, List<File>> entry : info.fileParts.entrySet()) {
            try {
                List<File> files = entry.getValue();
                for (File file : files) {
                    MultipartBody.Part part = MultipartBody.Part.createFormData(entry.getKey(),
                            URLEncoder.encode(file.getName(), "utf-8"),
                            new ProgressRequestBody(MediaType.parse("multipart/form-data"), entry.getKey(),
                                    file, localListener));
                    bodyBuilder.addPart(part);
                }                
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Call<ResponseBody> call = service.uploadSync(info.url, bodyBuilder.build());
        convertedResp = new ConvertedResponse<>(call);
        try {
            Response<ResponseBody> response = call.execute();
            convertedResp.raw = response.raw();
            if (response.isSuccessful() && info.converter != null) {
                try {
                    convertedResp.convertedResponse = info.converter.convert(response.body());
                } catch (Throwable t) {
                    convertedResp.convertError = t;
                }
            }
        } catch (Exception e) {
            //取消任务会抛异常
        }
    }
}
