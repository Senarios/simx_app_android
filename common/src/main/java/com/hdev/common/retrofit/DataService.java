package com.hdev.common.retrofit;

import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.PaymentWithdrawal;
import com.hdev.common.datamodels.PaymentwithdrawalResponse;
import com.hdev.common.datamodels.ResponseBlocked;
import com.hdev.common.datamodels.ResponseBroadcast;
import com.hdev.common.datamodels.ResponseFollowers;
import com.hdev.common.datamodels.ReviewRequestBody;
import com.hdev.common.datamodels.Users;
import com.hdev.common.datamodels.paypaldatamodels.BaseRequest;
import com.hdev.common.datamodels.paypaldatamodels.BroadcastRequestBody;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface DataService {


    //linkedin apis
    @POST("accessToken")
    Single<Response<JsonObject>> getAccessToken(@Query("grant_type") String grant,
                                                @Query("code") String code,
                                                @Query("redirect_uri") String re_direct,
                                                @Query("client_id") String c_id,
                                                @Query("client_secret") String c_secret
    );

    @GET("me")
    Single<Response<JsonObject>> getProfile(@Header("Authorization") String Auth);

    @GET("clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))")
    Single<Response<JsonObject>> getEmailAddress(@Header("Authorization") String Auth);

    //dreamfactory apis

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/users/{id}")
    Single<Response<Users>> getUser(@Path("id") String id);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/users/{id}")
    Single<Response<JsonObject>> checkUser(@Path("id") String id, @Query("related") String related);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/users")
    Single<Response<JsonObject>> postUser(@Body HashMap<String, Object> object);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @GET("mysql/_table/broadcasts?filter=isApproved=TRUE")
    Single<Response<ResponseBroadcast>> getBroadcasts(@Query("related") String related, @Query("offset") int offest, @Query("limit") int limit, @Query("order") String order);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @FormUrlEncoded
    @GET("mysql/_table/broadcasts")
    Call<ResponseBroadcast> accountLink(@Field("related") String related,
                                       @Field("offset") int offset,
                                       @Field("limit") int limit,
                                       @Field("order") String order);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @DELETE("mysql/_table/broadcasts/{path}")
    Single<Response<JsonObject>> deleteBroadcast(@Path("path") int id);
    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @DELETE("mysql/_table/tags/{path}")
    Single<Response<JsonObject>> deleteTags(@Path("path") String broadcast);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET(Constants.Endpoints.SEARCH)
    Single<Response<ResponseBroadcast>> getSearch(@Query("search") String q);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/broadcasts?")
    Single<Response<JsonObject>> getBroadcasts(@Query("filter") String q, @Query("order") String orderby, @Query("related") String related);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/ratings")
    Single<Response<JsonObject>> getReviews(@Query("filter") String q, @Query("order") String orderby, @Query("related") String related);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/broadcasts?")
    Single<Response<ResponseBroadcast>> getOtherBroadcasts(@Query("filter") String q, @Query("order") String orderby);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/broadcasts")
    Single<Response<JsonObject>> postBroadcast(@Body HashMap<String, Object> object);
    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/broadcasts")
    Single<Response<JsonObject>> postBroadcast(@Body BroadcastRequestBody requestBody);
    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/ratings")
    Single<Response<JsonObject>> postReview(@Body ReviewRequestBody requestBody);

    @POST("admin/ratings.php")
    Single<Response<JsonObject>> postReviews(@Body ReviewRequestBody requestBody);
    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET("mysql/_table/appointments")
    Single<Response<JsonObject>> getAppointments(@Query("filter") String id);


    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET(Constants.Endpoints.FOLLOWERS)
    Single<Response<JsonObject>> getFollowers(@Query("filter") String id);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET(Constants.Endpoints.FOLLOWERS)
    Single<Response<ResponseFollowers>> getOtherFollowers(@Query("filter") String id);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH("mysql/_table/users")
    Single<Response<JsonObject>> updateUser(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/appointments")
    Single<Response<JsonObject>> postAppointment(@Body HashMap<String, Object> object);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/blockedusers")
    Single<Response<JsonObject>> blockuser(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @GET(Constants.Endpoints.GET_JOB_CANDIDATES)
    Single<Response<JsonObject>> getJobCandidates(@Query("filter") String filter, @Query("related") String related,
                                                  @Query("order") String order
    );

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST(Constants.Endpoints.VIDEOCV)
    Single<Response<JsonObject>> postVideoCV(@Body HashMap<String, Object> object);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @GET(Constants.Endpoints.VIDEOCV)
    Single<Response<JsonObject>> getVideoCVs(@Query("filter") String filter);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @DELETE(Constants.Endpoints.VIDEOCV + "/{id}")
    Single<Response<JsonObject>> deleteVideoCV(@Path("id") int id);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST(Constants.Endpoints.POST_JOB_CANDIDATES)
    Single<Response<JsonObject>> postJobCandidate(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH(Constants.Endpoints.POST_JOB_CANDIDATES)
    Single<Response<JsonObject>> shortlistJobCandidate(@Body HashMap<String, Object> object);

    @Headers("X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90")
    @DELETE(Constants.Endpoints.POST_JOB_CANDIDATES + "/{id}")
    Single<Response<JsonObject>> deleteCandidate(@Path("id") int id);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE(Constants.Endpoints.POST_JOB_CANDIDATES + "/{path}")
    Single<Response<JsonObject>> deleteJobRequest(@Path("path") String path);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE(Constants.Endpoints.POST_JOB_CANDIDATES)
    Single<Response<JsonObject>> deleteJobRequestbyfilter(@Query("filter") String filter);
    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE(Constants.Endpoints.TAGS)
    Single<Response<JsonObject>> deleteTagsByFilter(@Query("filter") String filter);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST("mysql/_table/followers")
    Single<Response<JsonObject>> followuser(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE("mysql/_table/blockedusers?")
    Single<Response<JsonObject>> unblockuser(@Query("filter") String id);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE("mysql/_table/appointments" + "/{id}")
    Single<Response<JsonObject>> deleteAppointment(@Path("id") Integer id);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @DELETE("mysql/_table/followers?")
    Single<Response<JsonObject>> unfollowuser(@Query("filter") String id);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @GET("mysql/_table/blockedusers?")
    Single<Response<ResponseBlocked>> getblockstatus(@Query("filter") String id1);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @GET("mysql/_table/followers?")
    Single<Response<ResponseFollowers>> getfollowtatus(@Query("filter") String id1);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH("mysql/_table/broadcasts")
    Single<Response<JsonObject>> updateBroadcast(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH("mysql/_table/broadcasts")
    Single<Response<JsonObject>> updateBroadcast(@Body BroadcastRequestBody requestBody);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH("mysql/_table/ratings")
    Single<Response<JsonObject>> updateReview(@Body ReviewRequestBody requestBody);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @PATCH("mysql/_table/appointments")
    Single<Response<JsonObject>> updateAppointment(@Body HashMap<String, Object> object);

    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @POST(Constants.Endpoints.TAGS)
    Single<Response<JsonObject>> postTags(@Body HashMap<String, Object> object);


    //twitter apis
    @Headers({Constants.Twitter.AUTH2_TOKEN, "Content-Type: application/x-www-form-urlencoded"})
    @POST("request_token")
    Single<Response<String>> getRequestToken(@Query(Constants.AUTH.AUTH_CALLBACK) String callbackurl,
                                             @Query(Constants.AUTH.AUTH_CONSUMER) String Consumer);

    @POST("access_token")
    Single<Response<String>> getAccess_Token(@Query(Constants.AUTH.AUTH_VERIFIER) String verifier,
                                             @Query(Constants.AUTH.AUTH_TOKEN) String auth_token,
                                             @Query(Constants.AUTH.AUTH_CONSUMER) String auth_consumer);


    //php server calls
    @Multipart
    @POST(Constants.Endpoints.POST_DP)
    Single<Response<JsonObject>> postPicture(@Part("base64") RequestBody image, @Part("ImageName") RequestBody imagename);

    @POST(Constants.Endpoints.DELETE_BROADCAST)
    Single<Response<JsonObject>> deletebroadcast(@Query("id") int id, @Query("image") String image, @Query("broadcast") String broadcast);

    //    Single<Response<JsonObject>> sendPayment(@Body PaymentWithdrawal paymentWithdrawal);
    @Multipart
    @POST("admin/payments.php")
    Call<JsonObject> sendPayment(@PartMap Map<String, RequestBody> params );
    @Multipart
    @POST("admin/ratings.php")
    Call<JsonObject> setRatings(@PartMap Map<String, RequestBody> params );

    @POST(Constants.Endpoints.POST_LIVE_THUMBNAIL)
    Single<Response<JsonObject>> postThumbnail(@Query("videoName") String name);

    @POST(Constants.Endpoints.S3THUMBNAIL)
    Single<Response<JsonObject>> postThumbnail(@Query("imageName") String imageName, @Query("videoLink") String videoLink);


    @POST("social/videosharing.php")
    Single<Response<JsonObject>> shareAPI(@Query("broadcast_title") String broadcast,
                                          @Query("requested_by_user_id") String id,
                                          @Query("linkedin_token") String linked_in,
                                          @Query("twitter_token") String twitter_auth,
                                          @Query("videoName") String videoname,
                                          @Query("twitter_token_secret") String twitter_secret/*,
                                          @Query("fb_token") String fb*/
    );

    //paypal apis
    @FormUrlEncoded
    @POST("oauth2/token")
    Single<Response<JsonObject>> getPaypal_AccessToken(@Field("grant_type") String grant_type);

    @Headers("Content-Type:application/json")
    @POST("payments/payouts")
    Single<Response<JsonObject>> pay(@Header("Authorization") String header, @Body BaseRequest baseRequest);


    @Headers({"Content-Type: application/json", "X-DreamFactory-Api-Key: 88f4e742f60f801b195bb510c533d01bc4470717f4bb5f8195a8429111735a90"})
    @GET("mysql/_table/blockedusers?")
    Single<Response<JsonObject>> getBlockedList(@Query("filter") String query);


}
