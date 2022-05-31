package com.senarios.simxx.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.PaymentResponse;
import com.hdev.common.datamodels.PaymentWithdrawal;
import com.hdev.common.datamodels.PaymentwithdrawalResponse;
import com.hdev.common.datamodels.RatingsAndReview;
import com.hdev.common.datamodels.RatingsReviewsResponse;
import com.hdev.common.datamodels.ReviewRequestBody;
import com.hdev.common.datamodels.ReviewsResponse;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.ApiResponse;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.ReviewsAdapter;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.hdev.common.Constants.DreamFactory.URL;
import static com.hdev.common.Constants.Messages.ERROR;
import static com.hdev.common.Constants.Messages.NETWORK_ERROR;
import static com.hdev.common.Constants.SharedPreference.USER;

public class RatingsAndReviewsActivity extends BaseActivity implements View.OnClickListener, ApiResponse {
    private RecyclerView reviewsRV;
    RelativeLayout filterSearchBar;
    ReviewsAdapter adapter;
    private ImageView back;
    private RatingBar ratingBar;
    private SharedVM sharedVM;
    String ratedValue;
    private Users user, opponentUser;
    private TextView overallRatings, totalReviews, writeReview;
    private List<RatingsAndReview> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings_and_reviews);

        initViews();
        setClickListeners();
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);
        user = sharedVM.getLoggedUser();

        if (getIntent().getSerializableExtra(USER) != null) {
            opponentUser = (Users) getIntent().getSerializableExtra(USER);
            if (opponentUser.getUsername().equals(user.getUsername())) {
                writeReview.setVisibility(View.GONE);
                overallRatings.setText(user.getUserRatings());
                if (user.getUserRatings() == null) {
                    ratingBar.setRating(Float.parseFloat("0"));
                    totalReviews.setText("from " + "0" + " people");
                } else {
                    ratingBar.setRating(Float.parseFloat(user.getUserRatings()));
                    totalReviews.setText("from " + user.getTotalRatings() + " people");
                }

            } else {
                overallRatings.setText(opponentUser.getUserRatings());
                ratingBar.setRating(Float.parseFloat(opponentUser.getUserRatings()));
                totalReviews.setText("from " + opponentUser.getTotalRatings() + " people");
            }
        } else {
            overallRatings.setText(user.getUserRatings());
            ratingBar.setRating(Float.parseFloat(user.getUserRatings()));
            totalReviews.setText("from " + user.getTotalRatings() + " people");
        }

//        setData();
//        setReviewsAdapter();
        getReviews();
    }


    private void initViews() {
        reviewsRV = findViewById(R.id.reviews_rv);
        ratingBar = findViewById(R.id.ratingBar);
        overallRatings = findViewById(R.id.overall_rating);
        totalReviews = findViewById(R.id.total_ratings);
        reviewsRV = findViewById(R.id.reviews_rv);
        writeReview = findViewById(R.id.write_review);
        back = findViewById(R.id.back);

    }

    private void setClickListeners() {
        back.setOnClickListener(this);
        writeReview.setOnClickListener(this);
    }

    private void setReviewsAdapter(List<RatingsAndReview> reviewList) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new ReviewsAdapter(this, reviewList);
        reviewsRV.setLayoutManager(linearLayoutManager);
        reviewsRV.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.write_review:
                ratingDialog();
                break;
        }
    }

    private void ratingDialog() {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.rating_dialog);
        dialog.setCancelable(true);
        Button sendReview = dialog.findViewById(R.id.rate);
        TextView name = dialog.findViewById(R.id.name);
        EditText review = dialog.findViewById(R.id.review);
        RatingBar ratingBar = dialog.findViewById(R.id.ratings);
        name.setText(opponentUser.getName());
        setRatingBar(ratingBar);
        RatingsAndReview ratingsAndReview = new RatingsAndReview();
        ratingsAndReview.setUserId(user.getUsername());
        ratingsAndReview.setToUserId(opponentUser.getUsername());
        sendReview.setOnClickListener(view -> {
            if (!review.getText().toString().isEmpty() && !review.getText().toString().equalsIgnoreCase("")) {
                ratingsAndReview.setReview(review.getText().toString());
            }
            if (!(ratedValue == null) && !ratedValue.isEmpty() && !ratedValue.equalsIgnoreCase("0")) {
                ratingsAndReview.setRating(ratedValue);
                postReview(ratingsAndReview);
                dialog.dismiss();
//
            } else {
                Toast.makeText(getApplicationContext(), "Please give ratings to continue", Toast.LENGTH_LONG).show();
            }

        });
        dialog.show();
    }

    private void postReview(RatingsAndReview ratingsAndReview) {
        if (Utility.isNetworkAvailable(this)) {
            getDialog().show();
            sharedVM.getService(Constants.DEFAULT_URL)
                    .setRatings(ratingsRequestBody(ratingsAndReview))
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            getDialog().dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    getReviews();
                                    Gson gson = new Gson();
                                    RatingsReviewsResponse ratingsReviewsResponse = gson.fromJson(String.valueOf(response.body()), RatingsReviewsResponse.class);
                                    if (ratingsReviewsResponse != null && ratingsReviewsResponse.getRatingsResponse() != null) {
                                        overallRatings.setText(ratingsReviewsResponse.getRatingsResponse().getUserRatings());
                                        totalReviews.setText("from " + ratingsReviewsResponse.getRatingsResponse().getTotalRatings() + " people");                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            getDialog().dismiss();
                            Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }


    private void setRatingBar(RatingBar ratingBar) {
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rate, fromUser) -> {
            int ratings = (int) rate;
            ratedValue = String.valueOf(ratingBar1.getRating());
        });

    }

    private void getReviews() {
        getDialog().show();
        String filter = "(toUserId=" + opponentUser.getUsername() + ")";
        sharedVM.getService(URL)
                .getReviews(filter, Constants.DreamFactory.ORDERBY, Constants.DreamFactory.RATINGS_RELATED)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<Response<JsonObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<JsonObject> response) {
                        getDialog().dismiss();
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                String str = response.body().toString();
                                ReviewsResponse reviewsResponse = convertToReviewList(str);
                                if (reviewsResponse.getRatingsAndReview() != null && !reviewsResponse.getRatingsAndReview().isEmpty()) {
                                    reviewList.clear();
                                    reviewList.addAll(reviewsResponse.getRatingsAndReview());
                                    setReviewsAdapter(reviewsResponse.getRatingsAndReview());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getDialog().dismiss();
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();

                    }
                });

    }

    private void updateUser(Users user) {
        if (Utility.isNetworkAvailable(this)) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", user);
            sharedVM.getService(URL).updateUser(map)
                    .subscribeOn(Schedulers.io())
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
                                        Toast.makeText(RatingsAndReviewsActivity.this, " Updated", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        if (body instanceof ReviewRequestBody) {
            List<RatingsAndReview> reviewArrayList = new ArrayList<>();
            reviewArrayList.add((RatingsAndReview) convertObjectToList(body));
            setReviewsAdapter(reviewArrayList);
        }

    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();

    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();

    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();

    }

    public static List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    private ReviewsResponse convertToReviewList(String roomJsonString) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ReviewsResponse reviewsResponse = gson.fromJson(roomJsonString, ReviewsResponse.class);
        return reviewsResponse;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean containsReview(final List<RatingsAndReview> list, final String id) {
        return list.stream().anyMatch(o -> o.getUserId().equals(id));
    }

    private Map<String, RequestBody> ratingsRequestBody(RatingsAndReview ratingsAndReview) {
        Map<String, RequestBody> map = new HashMap();
        map.put("toUserId", requestBody(ratingsAndReview.getToUserId()));
        map.put("userId", requestBody(ratingsAndReview.getUserId()));
        map.put("review", requestBody(ratingsAndReview.getReview()));
        map.put("rating", requestBody(ratingsAndReview.getRating()));
        return map;
    }

    private RequestBody requestBody(String param) {
        return RequestBody.create(MediaType.parse("text/plain"), String.valueOf(param));
    }
}