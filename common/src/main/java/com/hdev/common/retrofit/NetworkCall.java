package com.hdev.common.retrofit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class NetworkCall {

    public synchronized static void CallAPI(Context context, Single<Response<JsonObject>> api , ApiResponse callback, Boolean isDialog,Class type ,String endpoint) {
        if (CommonUtils.isNetworkAvailable(context)) {
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("Please Wait..");
            dialog.setCancelable(false);
            if (isDialog && !((Activity)context).isFinishing()) {
                dialog.show();
            }
            Log.v("API: ",api.toString());
            api.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        callback.OnSuccess(response, new Gson().fromJson(response.body().toString(),type),endpoint);
                                    }
                                    else {
                                    callback.OnError(response, endpoint);
                                }
                                }
                                else {
                                    callback.OnError(response, endpoint);
                                }

                            } else {
                                callback.OnError(response, endpoint);
                            }
                            if (isDialog && !((Activity)context).isFinishing()) {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            callback.OnException( e , endpoint);
                            if (isDialog && !((Activity)context).isFinishing()) {
                                dialog.dismiss();
                            }
                        }
                    });

        }
        else{
            callback.OnNetWorkError(endpoint,"NetWork Error");
        }
    }

}
