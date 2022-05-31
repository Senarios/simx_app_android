package com.senarios.simxx.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.datamodels.Events;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.model.QBEvent;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.OtherUserBroadcastsAdapter;
import com.senarios.simxx.callbacks.BroadcastCallback;
import com.senarios.simxx.databinding.ProfileFragmentBinding;
import com.hdev.common.datamodels.Blocked;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Followers;
import com.hdev.common.datamodels.OtherUserModel;
import com.hdev.common.datamodels.ResponseBlocked;
import com.hdev.common.datamodels.ResponseBroadcast;
import com.hdev.common.datamodels.ResponseFollowers;
import com.hdev.common.datamodels.Type;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.viewmodels.SharedVM;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class OtherUserProfileActivity extends BaseActivity implements BroadcastCallback, AppBarLayout.OnOffsetChangedListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, QBEntityCallback<QBEvent> {
    private ProfileFragmentBinding binding;
    private OtherUserModel otherUserModel;
    private OtherUserBroadcastsAdapter adapter;
    private SharedVM sharedVM;
    private Users user;
    private CompositeDisposable disposable = new CompositeDisposable();


    @Override
    public ProfileFragmentBinding binding() {
        binding = DataBindingUtil.setContentView(this, R.layout.profile_fragment);
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);
        user = sharedVM.getLoggedUser();
        return binding;
    }

    @Override
    public void getData(Intent intent) {
        if (intent != null && intent.hasExtra(DataConstants.USER_ID)) {
            getUser(intent.getStringExtra(DataConstants.USER_ID));
        }
    }

    @Override
    public void init() {
        binding.swipe.setEnabled(false);
        binding.swipe.setOnRefreshListener(this);
        binding.ProfilePicture.setOnClickListener(this);
        // binding.followButton.setOnClickListener(this);
        //binding.blockBtn.setOnClickListener(this);
        binding.followersNumber.setOnClickListener(this);
        binding.followingNumber.setOnClickListener(this);
        binding.callButton.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.messageButton.setOnClickListener(this);
        binding.appointmentButton.setOnClickListener(this);
        binding.ivLinkedin.setOnClickListener(this);
        binding.ratingLayout.setOnClickListener(this);
        binding.rcForBroadcasts.setLayoutManager(new LinearLayoutManager(this));

        binding.appbar.addOnOffsetChangedListener(this);

        SharedPreferences preferences = getSharedPreferences("notificationString", MODE_PRIVATE);
        String sendNotificationString = preferences.getString(SharedPreference.CALL_MSG, "");

        switch (sendNotificationString) {
            case "msg":
                binding.callButton.setVisibility(View.GONE);
                break;
            case "call":
                binding.messageButton.setVisibility(View.GONE);
                break;
            case "both":
                binding.callButton.setVisibility(View.VISIBLE);
                binding.messageButton.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void onRefresh() {
        getData(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("notificationString", MODE_PRIVATE);
        String returnMessage = preferences.getString("callreturn", "");

        if (returnMessage.equals("yes")) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", Context.MODE_PRIVATE).edit();
            editor.putString("callreturn", "no");
            editor.apply();
            finish();
        } else {
            Log.wtf("return", "not");
        }
    }

    @Override
    public void onClick(View v) {
        if (otherUserModel == null) {
            Toast.makeText(this, "loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.Profile_picture:
                Utility.loadFullScreenImageView(this, Constants.DreamFactory.GET_IMAGE_URL + otherUserModel.getUsers().getEmail() + ".png");
                break;

            case R.id.iv_linkedin:
                Log.d("hellocheckthis", otherUserModel.getUsers().toString());
                Utility.openBrowser(this, getString(R.string.linkedin_link) + otherUserModel.getUsers().getLink().replace(getString(R.string.linkedin_link), "").trim());
                break;

            case R.id.followers_number:
                startActivity(new Intent(this, FollowerFollowingActivity.class).putExtra(DataConstants.TYPE, Type.FOLLOWER.toString()).putExtra(DataConstants.FOLLOWER, otherUserModel.getFollowers()));
                break;

            case R.id.following_number:
                startActivity(new Intent(this, FollowerFollowingActivity.class).putExtra(DataConstants.TYPE, Type.FOLLOWING.toString()).putExtra(DataConstants.FOLLOWER, otherUserModel.getFollowing()));
                break;

            case R.id.block_btn:
                if (binding.blockBtn.getText().equals("Block User")) {
                    block();
                } else {
                    unblock();
                }
                break;
            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.rating_layout:
                startActivity(new Intent(this, RatingsAndReviewsActivity.class)
                        .putExtra(USER, binding.getUser()));
                break;
            case R.id.follow_button:
                if (otherUserModel.getBlocked() != null) {
                    Toast.makeText(getContext(), "Unblock user first", Toast.LENGTH_SHORT).show();
                } else {
                    if (binding.followButton.getText().equals("Follow")) {
                        follow();
                    } else {
                        unfollow();
                    }

                }
                break;

            case R.id.appointment_button:
                if (otherUserModel.getBlocked() != null) {
                    Toast.makeText(getContext(), "Unblock user first", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(this, RequestAppointmentActivity.class)
                        .putExtra(USER, binding.getUser())
                );
                break;
            case R.id.call_button:
                if (otherUserModel.getBlocked() != null) {
                    Toast.makeText(getContext(), "Unblock user first", Toast.LENGTH_SHORT).show();
                    return;
                }
                //getRate())>0 this change
                if (binding.getUser() != null && !binding.getUser().getQbid().equalsIgnoreCase("NA")) {
//                    if (binding.getUser().getRate().isEmpty() || Integer.parseInt(binding.getUser().getRate()) == 0) {
                        getViewModel().getSharedPreference().edit().putInt(QB_OPPONENT_USER, Integer.parseInt(binding.getUser().getQbid())).apply();
                        Intent intent = new Intent(getContext(), CallActivity.class);
                        intent.putExtra(QB_OPPONENT_USER, binding.getUser());
                        startActivity(intent);
//                    }
//                    else if (Math.round(getViewModel().getLoggedUser().getCredit()) > 0) {
//                        getViewModel().getSharedPreference().edit().putInt(QB_OPPONENT_USER, Integer.parseInt(binding.getUser().getQbid())).apply();
//                        Intent intent = new Intent(getContext(), CallActivity.class);
//                        intent.putExtra(QB_OPPONENT_USER, binding.getUser());
//                        startActivity(intent);
//                    } else {
//                        Utility.getAlertDialoge(this, "Insufficient Balance", getString(R.string.message_low_balance))
//                                .setPositiveButton("Yes", (dialog, which) ->
//                                        startActivity(new Intent(OtherUserProfileActivity.this, PaymentTestActivity.class)))
//                                .setNegativeButton("No", (dialog, which) -> {
//                                    dialog.dismiss();
//                                }).show();
//                    }
                } else {
                    Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.message_button:
                if (otherUserModel.getBlocked() != null) {
                    Toast.makeText(getContext(), "Unblock user first", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessage();
                break;


        }

    }


    @Override
    public void OnBroadcastClicked(Broadcasts broadcasts) {
        if (broadcasts.getVideourl() != null && !broadcasts.getVideourl().isEmpty() && broadcasts.getVideourl().startsWith("https://youtu")) {
            Intent intent = new Intent(OtherUserProfileActivity.this, PlayYtBroadcastActivity.class);
            intent.putExtra("ytVideolink", broadcasts.getVideourl());
            startActivity(intent);
        }else if (broadcasts.isOffline()&&broadcasts.getVideourl().isEmpty()&&broadcasts.getVideourl()==null) {
            Utility.makeFilePublic(this, null, Constants.S3Constants.OFFLINE_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
        } else if (broadcasts.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
            Intent intent = new Intent(this, ViewStream.class);
            intent.putExtra("b", broadcasts);
            startActivity(intent);
        } else {
            Utility.makeFilePublic(this, null, Constants.S3Constants.RECORDED_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
        }
    }

    private void getUser(String id) {
        toggleClick(false);
//        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
        if (Utility.isNetworkAvailable(this)) {
            binding.swipe.setRefreshing(true);
            binding.blockBtn.setVisibility(View.INVISIBLE);
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getUser(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<Users>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onSuccess(Response<Users> response) {
                            binding.swipe.setRefreshing(false);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        SharedPreferences preferences = getSharedPreferences("notificationString", MODE_PRIVATE);
                                        String sendNotificationString = preferences.getString(SharedPreference.CALL_MSG, "");
                                        if (sendNotificationString.equals("msg")) {
                                            binding.callButton.setVisibility(View.GONE);
                                        }else binding.callButton.setVisibility(View.VISIBLE);

                                        binding.blockBtn.setVisibility(View.VISIBLE);
                                        otherUserModel = new OtherUserModel();
                                        otherUserModel.setUsers(response.body());
                                        binding.setUser(otherUserModel.getUsers());
                                        binding.root.setVisibility(View.VISIBLE);
                                        Users user=otherUserModel.getUsers();
                                        binding.ratingBar.setRating(Float.parseFloat(user.getUserRatings()));
                                        binding.ratingText.setText(user.getUserRatings()+"("+user.getTotalRatings()+")");
                                        callApis(getViewModel().getLoggedUser().getUsername(), binding.getUser().getUsername());

//                                        getBlockStatus(getViewModel().getLoggedUser().getUsername(), binding.getUser().getUsername());
//                                        getFollowStatus(getViewModel().getLoggedUser().getUsername(), binding.getUser().getUsername());
//                                        getFollowers(binding.getUser().getUsername());
//                                        getBroadcasts(id);
                                    }
                                } else {
                                    Toast.makeText(OtherUserProfileActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(OtherUserProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.swipe.setRefreshing(false);
                            Toast.makeText(OtherUserProfileActivity.this, "Something went wrong." + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } else {
            Toast.makeText(this, "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }

    }

    private void callApis(String loggedUser, String opponentUser) {

        binding.swipe.setRefreshing(true);
        disposable.add(
                Single.mergeDelayError(getViewModel().getService(Constants.DreamFactory.URL)
                                .getblockstatus("(userid = " + loggedUser + ") and (blockedid = " + opponentUser + ")"),
                        getViewModel().getService(Constants.DreamFactory.URL)
                                .getfollowtatus("(userid = " + loggedUser + ") and (followerid = " + opponentUser + ")"),
                        getViewModel().getService(Constants.DreamFactory.URL)
                                .getFollowers(("(followerid like %" + opponentUser + "%) or (userid like %" + opponentUser + "%)")),
                        getViewModel().getService(Constants.DreamFactory.URL)
                                .getOtherBroadcasts("(username=" + opponentUser + ")", Constants.DreamFactory.ORDERBY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .doOnComplete(() -> {
                            binding.swipe.setRefreshing(false);
                            toggleClick(true);
                            Utility.showLog("doOnComplete");
                        })
                        .doOnNext(response -> {
                            if (response.body() instanceof ResponseBroadcast) {
                                otherUserModel.setBroadcasts((ArrayList<Broadcasts>) ((ResponseBroadcast) response.body()).getResource());
                                if (((ResponseBroadcast) response.body()).getResource().size() > 0) {
                                    binding.setBroadcasts("" + otherUserModel.getBroadcasts().size());
                                    adapter = new OtherUserBroadcastsAdapter(otherUserModel.getBroadcasts(), OtherUserProfileActivity.this, OtherUserProfileActivity.this);
                                    binding.rcForBroadcasts.setAdapter(adapter);
                                } else {
                                    binding.setBroadcasts("0");
                                }
                            } else if (response.body() instanceof ResponseBlocked) {
                                if (!(((ResponseBlocked) response.body()).getResource().isEmpty())) {
                                    otherUserModel.setBlocked((ArrayList<Blocked>) ((ResponseBlocked) response.body()).getResource());
                                    binding.blockBtn.setText("Unblock");
                                } else {
                                    otherUserModel.setBlocked(null);
                                    binding.blockBtn.setText("Block User");
                                }
                                binding.blockBtn.setClickable(true);
                            } else if (response.body() instanceof ResponseFollowers) {
                                if (!(((ResponseFollowers) response.body()).getResource().isEmpty())) {
                                    binding.followButton.setText("Unfollow");
                                } else {
                                    binding.followButton.setText("Follow");
                                }
                                binding.followButton.setClickable(true);
                            } else if (response.body() instanceof JsonObject) {
                                ArrayList<Followers> followers_followings = new ArrayList<>();
                                ResponseFollowers model = new Gson().fromJson(((JsonObject) response.body()).toString(), ResponseFollowers.class);
                                followers_followings.addAll(model.getResource());/*
                                otherUserModel.setFollowers((ArrayList<Followers>) getFollowersList(followers_followings));
                                otherUserModel.setFollowing((ArrayList<Followers>) getFollowingList(followers_followings));*/

                                otherUserModel.setFollowers((ArrayList<Followers>) getFollowingList(followers_followings));
                                otherUserModel.setFollowing((ArrayList<Followers>) getFollowersList(followers_followings));
                                binding.followersNumber.setText(String.valueOf(otherUserModel.getFollowers().size()));
                                binding.followersNumber.setClickable(true);
                                binding.followingNumber.setText(String.valueOf(otherUserModel.getFollowing().size()));
                                binding.followingNumber.setClickable(true);
                                binding.blockBtn.setOnClickListener(this);
                                binding.followButton.setOnClickListener(this);
                            }
                            Utility.showLog("doOnNext");
                        })
                        .doOnError(throwable -> {
                            Utility.showLog("doOnError");
                        })
                        .subscribe());


    }

    private List<Followers> getFollowersList(List<Followers> resource) {
        List<Followers> followerList = new ArrayList<>();

        for (Followers child : resource) {
            if (child.getUserid().equalsIgnoreCase(binding.getUser().getUsername()))
                followerList.add(child);

        }
        return followerList;
    }

    private List<Followers> getFollowingList(List<Followers> resource) {
        List<Followers> followingList = new ArrayList<>();

        for (Followers child : resource) {
            if (!child.getUserid().equalsIgnoreCase(binding.getUser().getUsername()))
                followingList.add(child);

        }
        return followingList;
    }

    private void getBroadcasts(String id) {
        if (Utility.isNetworkAvailable(this)) {
            binding.swipe.setRefreshing(true);
            String filter = "(username=" + id + ")";
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getOtherBroadcasts(filter, Constants.DreamFactory.ORDERBY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBroadcast>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseBroadcast> response) {
                            binding.swipe.setRefreshing(false);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        otherUserModel.setBroadcasts((ArrayList<Broadcasts>) response.body().getResource());
                                        if (response.body().getResource().size() > 0) {
                                            binding.setBroadcasts("" + otherUserModel.getBroadcasts().size());
                                            adapter = new OtherUserBroadcastsAdapter(otherUserModel.getBroadcasts(), OtherUserProfileActivity.this, OtherUserProfileActivity.this);
                                            binding.rcForBroadcasts.setAdapter(adapter);
                                        } else {
                                            binding.setBroadcasts("0");
                                        }
                                    } else {
                                        Toast.makeText(OtherUserProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(OtherUserProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {

                                Toast.makeText(OtherUserProfileActivity.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.swipe.setRefreshing(false);

                        }
                    });
        } else {
            Toast.makeText(this, "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }

    }

    private void getBlockStatus(String id1, String id2) {
        if (Utility.isNetworkAvailable(OtherUserProfileActivity.this)) {
            binding.blockBtn.setClickable(false);
            binding.swipe.setRefreshing(true);
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getblockstatus("(userid = " + id1 + ") and (blockedid = " + id2 + ")")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBlocked>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseBlocked> responseBlockedResponse) {
                            binding.swipe.setRefreshing(false);
                            binding.blockBtn.setClickable(true);
                            if (responseBlockedResponse.isSuccessful()) {
                                if (responseBlockedResponse.code() == 200) {
                                    if (!(responseBlockedResponse.body().getResource().isEmpty())) {
                                        otherUserModel.setBlocked((ArrayList<Blocked>) responseBlockedResponse.body().getResource());
                                        binding.blockBtn.setText("Unblock");
                                    } else {
                                        otherUserModel.setBlocked(null);
                                        binding.blockBtn.setText("Block User");
                                    }
                                    binding.blockBtn.setClickable(true);


                                } else {

                                    Toast.makeText(OtherUserProfileActivity.this, "Something went ..", Toast.LENGTH_SHORT).show();
                                }

                                ;
                            } else {

                                Toast.makeText(OtherUserProfileActivity.this, "Something wlkkent wrong..", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.swipe.setRefreshing(false);

                        }
                    });

        } else {
            Toast.makeText(OtherUserProfileActivity.this, "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }
    }

    private void block() {
        long millis = System.currentTimeMillis();
        millis = millis / 1000;
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            binding.blockBtn.setClickable(false);
            Blocked blocked = new Blocked();
            blocked.setId(getViewModel().getLoggedUser().getUsername() + Math.floor(millis));
            blocked.setUserid(getViewModel().getLoggedUser().getUsername());
            blocked.setUsername(getViewModel().getLoggedUser().getName());
            blocked.setBlockedid(binding.getUser().getUsername());
            blocked.setBlockedname(binding.getUser().getName());
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", blocked);
            getViewModel().getService(Constants.DreamFactory.URL).blockuser(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            EventBus.getDefault().post(Events.BLOCKED);
                            binding.blockBtn.setClickable(true);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        otherUserModel.setBlocked(new ArrayList<>());
                                        binding.blockBtn.setText("Unblock");
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {

                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.blockBtn.setClickable(true);
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getContext(), "Please enable wifi/data", Toast.LENGTH_SHORT).show();
        }
    }

    private void unblock() {
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            binding.blockBtn.setClickable(false);
            getViewModel().getService(Constants.DreamFactory.URL).unblockuser("(userid = " + getViewModel().getLoggedUser().getUsername() + ") and (blockedid = " + binding.getUser().getUsername() + ")")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            EventBus.getDefault().post(Events.BLOCKED);
                            binding.blockBtn.setClickable(true);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        otherUserModel.setBlocked(null);
                                        binding.blockBtn.setText("Block User");

                                    } else {

                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {

                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.blockBtn.setClickable(true);
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getContext(), "Please enable wifi/data", Toast.LENGTH_SHORT).show();
        }
    }

    private void unfollow() {
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            binding.followButton.setClickable(false);
            getViewModel().getService(Constants.DreamFactory.URL).unfollowuser("(userid = " + getViewModel().getLoggedUser().getUsername() + ") and (followerid = " + binding.getUser().getUsername() + ")")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            EventBus.getDefault().post(Events.FOLLOWER);
                            binding.followButton.setClickable(true);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        binding.followButton.setText("Follow");
                                        getFollowers(binding.getUser().getUsername());
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.followButton.setClickable(true);
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getContext(), "Please enable wifi/data", Toast.LENGTH_SHORT).show();
        }
    }

    private void follow() {
        long millis = System.currentTimeMillis();
        millis = millis / 1000;
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            binding.followButton.setClickable(false);
            Followers followers = new Followers();
            followers.setUserid(getViewModel().getLoggedUser().getUsername());
            followers.setFollowerid(binding.getUser().getUsername());
            followers.setUsername(getViewModel().getLoggedUser().getName());
            followers.setUserEmail(getViewModel().getLoggedUser().getEmail());
            followers.setFollowername(binding.getUser().getName());
            followers.setFollowerEmail(binding.getUser().getEmail());
            followers.setId(getViewModel().getLoggedUser().getUsername() + Math.floor(millis));
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", followers);
            getViewModel().getService(Constants.DreamFactory.URL).followuser(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            EventBus.getDefault().post(Events.FOLLOWER);
                            binding.followButton.setClickable(true);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        binding.followButton.setText("Unfollow");
                                        getFollowers(binding.getUser().getUsername());
                                    }
                                    JSONObject object = new JSONObject();
                                    try {
                                        object.put(NotificationKeys.User.toString(), getViewModel().getLoggedUser().toString());
                                        object.put(NotificationKeys.message.toString(), "SimpleData");
                                        object.put(NotificationKeys.Type.toString(), NotificationType.Follower);
                                        Utility.sendNotification(false, Integer.parseInt(otherUserModel.getUsers().getQbid()), object, OtherUserProfileActivity.this);
                                    } catch (JSONException e) {
                                        Utility.showELog(e);
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {

                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.followButton.setClickable(true);
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getContext(), "Please enable wifi/data", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFollowStatus(String id1, String id2) {
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getfollowtatus("(userid = " + id1 + ") and (followerid = " + id2 + ")")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseFollowers>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseFollowers> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (!(response.body().getResource().isEmpty())) {
                                        binding.followButton.setText("Unfollow");
                                    } else {
                                        binding.followButton.setText("Follow");
                                    }
                                    binding.followButton.setClickable(true);
                                } else {
                                    Toast.makeText(getContext(), "Something went ..", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Something wlkkent wrong..", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {


                        }
                    });

        } else {
            Toast.makeText(getContext(), "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFollowers(String id) {
        if (Utility.isNetworkAvailable(Objects.requireNonNull(getContext()))) {
            binding.followersNumber.setClickable(false);
            binding.followingNumber.setClickable(false);
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getOtherFollowers(("(followerid like %" + id + "%) or (userid like %" + id + "%)"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseFollowers>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseFollowers> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        ArrayList<Followers> followers_followings = new ArrayList<>();
                                        ArrayList<Followers> followings_list = new ArrayList<>();
                                        ArrayList<Followers> followers_list = new ArrayList<>();
                                        followers_followings.addAll(response.body().getResource());
                                        for (int i = 0; i < followers_followings.size(); i++) {
                                            if (followers_followings.get(i).getUserid().equals(id)) {
                                                followings_list.add(followers_followings.get(i));
                                            } else {
                                                followers_list.add(followers_followings.get(i));
                                            }

                                        }
                                        otherUserModel.setFollowers(followers_list);
                                        otherUserModel.setFollowing(followings_list);
                                        binding.followersNumber.setText(String.valueOf(followers_list.size()));
                                        binding.followersNumber.setClickable(true);
                                        binding.followingNumber.setText(String.valueOf(followings_list.size()));
                                        binding.followingNumber.setClickable(true);

                                    } else {
                                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {
                                binding.followersNumber.setClickable(true);
                                binding.followingNumber.setClickable(true);
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.followersNumber.setClickable(true);
                            binding.followingNumber.setClickable(true);
                        }
                    });

        } else {
            Toast.makeText(getContext(), "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }


    }

    private void sendMessage() {
        binding.swipe.setRefreshing(true);
        binding.messageButton.setClickable(false);
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        occupantIdsList.add(Integer.valueOf(binding.getUser().getQbid()));
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName("Chat");
        dialog.setPhoto("1786");
        dialog.setType(QBDialogType.PRIVATE);
        dialog.setOccupantsIds(occupantIdsList);
        //   QBUser qbUser = new Gson().fromJson(getViewModel().getSharedPreference().getString(Constants.SharedPreference.QB_USER, ""), QBUser.class);
        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle params) {
                binding.messageButton.setClickable(true);
                binding.swipe.setRefreshing(false);
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("d", result);
                intent.putExtra(DataConstants.USER, otherUserModel.getUsers());
                startActivity(intent);

            }

            @Override
            public void onError(QBResponseException e) {
                binding.messageButton.setClickable(true);
                binding.swipe.setRefreshing(false);
                Utility.showELog(e);
                Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private Context getContext() {
        return OtherUserProfileActivity.this;
    }

    @Override
    public void onSuccess(QBEvent qbEvent, Bundle bundle) {
        Utility.showLog(qbEvent.toString());
    }

    @Override
    public void onError(QBResponseException e) {
        Utility.showELog(e);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (Math.abs(i) == appBarLayout.getTotalScrollRange()) {
            binding.actionButtons.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            binding.actionButtons.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        binding.swipe.setRefreshing(false);
    }

    private void toggleClick(Boolean isClick) {
        binding.ProfilePicture.setClickable(isClick);
        binding.followButton.setClickable(isClick);
        binding.blockBtn.setClickable(isClick);
        binding.followersNumber.setClickable(isClick);
        binding.followingNumber.setClickable(isClick);
        binding.callButton.setClickable(isClick);
        binding.messageButton.setClickable(isClick);
        binding.appointmentButton.setClickable(isClick);
        binding.ivLinkedin.setClickable(isClick);
    }
}

