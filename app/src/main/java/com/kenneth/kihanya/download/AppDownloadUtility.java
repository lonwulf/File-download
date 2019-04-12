package com.kenneth.kihanya.download;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.functions.Func1;

public class AppDownloadUtility {

    public static final String BASE_URL = "url here";
    public static final String FILE_URL = "path here";
    public static final String TAG = "Activity";

    public void downloadApk() {
        DownloadService downloadService = createService(DownloadService.class);
        downloadService.initiateDownloadService(BASE_URL + FILE_URL)
                .flatMap(handleResponse())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileObserver());

        Log.d(TAG, "downloadApk: initiated");
    }

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL + FILE_URL)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(serviceClass);
    }

    private Observable<File> saveFile(final Response<ResponseBody> response) {
        return io.reactivex.Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                if (response.isSuccessful()) {
                    try {
                        String header = response.headers().get("Content-Disposition");
                        String filename = header.replace("your apk", "");
                        File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                        BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                        bufferedSink.writeAll(response.body().source());
                        bufferedSink.close();
                        emitter.onNext(destinationFile);
                        emitter.onComplete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        emitter.onError(ex);
                    }

                }
            }
        });
    }

    private Function<Response<ResponseBody>, Observable<File>> handleResponse() {
        return new Function<Response<ResponseBody>, Observable<File>>() {
            @Override
            public Observable<File> apply(Response<ResponseBody> responseBodyResponse) throws Exception {
                return saveFile(responseBodyResponse);
            }
        };
    }

    private Observer<File> fileObserver() {
        return new Observer<File>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(File file) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "downloadApk: completed");
            }
        };
    }
}
