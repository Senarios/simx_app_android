package com.hdev.common.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Response;

public interface ApiResponse {
    void OnSuccess(Response<JsonObject> response , Object body ,String endpoint);
    void OnError(Response<JsonObject> response,String endpoint);
    void OnException(Throwable e,String endpoint);
    void OnNetWorkError(String endpoint, String message);

}
