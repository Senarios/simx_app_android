package com.senarios.simxx.models;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("sendmail.php")
    Call<SendMailRes> sendMail(@Field("to") String to,
                               @Field("toName") String toName,
                               @Field("subject") String subject,
                               @Field("body") String body);
}
