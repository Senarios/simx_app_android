package com.senarios.simxx.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.transition.Fade;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.BoundService;
import com.hdev.common.CommonUtils;
import com.hdev.common.LocationHelper;
import com.hdev.common.datamodels.Events;
import com.hdev.common.datamodels.Followers;
import com.hdev.common.datamodels.Tags;
import com.hdev.common.datamodels.Users;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.QBUsers;
import com.hdev.common.Constants;
import com.quickblox.users.model.QBUser;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.ActivityContainerCallback;
import com.senarios.simxx.callbacks.LogoutCallback;
import com.hdev.common.datamodels.ResponseBlocked;
import com.hdev.common.datamodels.ResponseFollowers;
import com.senarios.simxx.databinding.ActivityMainBinding;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;
import com.senarios.simxx.fragments.homefragments.ProfileFragment;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.fragments.mainactivityfragments.LoginFragment;
import com.senarios.simxx.fragments.mainactivityfragments.LoginWithLinkedIn;
import com.senarios.simxx.fragments.mainactivityfragments.SplashFragment;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.viewmodels.SharedVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdev.common.Constants.DreamFactory.URL;
import static com.senarios.simxx.fragments.mainactivityfragments.SplashFragment.savedUser;

public class MainActivity extends BaseActivity implements Constants.QB,
        OnCompleteListener<InstanceIdResult>, ApiResponse, ActivityContainerCallback, FragmentTags, QBPushManager.QBSubscribeListener {
    private final int CODE = 0;
    private String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE
    };
    private List<String> denied = new ArrayList<>();
    private Fragment home;
    public static String STATIC_TOKEN = "";

    @Override
    public ActivityMainBinding binding() {
        return DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            if (intent.hasExtra(USER)) {
                if (findFragment(HOME) != null) {
                    ((HomeFragment) findFragment(HOME)).OnChange(new ProfileFragment(), PROFILE);
                }

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        postTags();

        //keep screen oN
//        keepScreenOn();

        getViewModel().getSharedPreference().edit().putBoolean(SUB_AWS, false).apply();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(this);

        if (!hasPermissions(this, PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, CODE);
            }
        } else if(getIntent().getBooleanExtra("main",false)) {
            OnFragmentChange(new HomeFragment(), FragmentTags.HOME);
        } else {
            OnFragmentChange(new SplashFragment(), FragmentTags.SPLASH);
        }


//        //check if location is enabled.
        displayLocationSettingsRequest(this);

        try {
            getBlockList();
        } catch (Exception e) {
            Utility.showELog(e);
        }
        QBPushManager.getInstance().addListener(this);

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Fragment fragment = findFragment(FragmentTags.SPLASH);
        if (fragment != null && fragment.isVisible()) {
            OnFragmentChange(new SplashFragment(), SPLASH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment home = getSupportFragmentManager().findFragmentByTag(FragmentTags.HOME);
        if (home != null && home.isVisible()) {
            Fragment createStream = home.getChildFragmentManager().findFragmentByTag(CREATE_STREAM);
            if (createStream != null && createStream.isVisible()) {
                createStream.onActivityResult(requestCode, resultCode, data);
            }
            Fragment editprofile = home.getChildFragmentManager().findFragmentByTag(FragmentTags.EDITPROFILE);
            if (editprofile != null && editprofile.isVisible()) {
                editprofile.onActivityResult(requestCode, resultCode, data);
            }

            Fragment profile = home.getChildFragmentManager().findFragmentByTag(PROFILE);
            if (profile != null && profile.isVisible()) {
                profile.onActivityResult(requestCode, resultCode, data);
            }

            Fragment broadcasts = home.getChildFragmentManager().findFragmentByTag(BROADCAST);
            if (broadcasts != null && broadcasts.isVisible()) {
                broadcasts.onActivityResult(requestCode, resultCode, data);
            }


        }


        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.CREATE_PROFILE_IN);
        if (fragment != null && fragment.isVisible()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(FragmentTags.CREATE_PROFILE);
        if (fragment1 != null && fragment1.isVisible()) {
            fragment1.onActivityResult(requestCode, resultCode, data);
        }

    }


    @Override
    public void OnFragmentChange(Fragment fragment, String tag) {
        try {
            FragmentManager manager = getSupportFragmentManager();
            getSupportFragmentManager().popBackStack(tag, 0);
            FragmentTransaction fm = manager.beginTransaction();
            fm.replace(R.id.fragment_container, fragment, tag);
            fm.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
            if (!tag.equalsIgnoreCase(SPLASH)) {
                fm.addToBackStack(tag);
            }
            fm.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            finish();
        } else if (findFragment(LOGIN) != null && findFragment(LOGIN).isVisible()) {
            finish();
        } else if (findFragment(HOME) != null && findFragment(HOME).isVisible()) {
            home = findFragment(HOME);
            if (home != null) {
                if (home.getChildFragmentManager().getBackStackEntryCount() == 1) {
                    finish();
                } else {
                    home.getChildFragmentManager().popBackStack();
                }
            }
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    private Fragment findFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        denied.clear();
        OnFragmentChange(new SplashFragment(), SPLASH);
        if (requestCode == CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied.add(permission);
                }
            }
            if (denied.size() > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("SimX")
                        .setMessage(PERMISSION)
                        .setPositiveButton("Continue To Grant Permissions", (dialog, which) -> requestPermissions(permissions, CODE))
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .show();
            }

        }
    }


    @Override
    public void onComplete(@NonNull Task<InstanceIdResult> task) {
        if (task.isSuccessful()) {
            STATIC_TOKEN = Objects.requireNonNull(task.getResult()).getToken();
            getViewModel().getSharedPreference().edit().putString(Constants.SharedPreference.FCM, Objects.requireNonNull(task.getResult()).getToken()).apply();
        }
    }


    //unused fragments functions
    public void removeFragment(String tag) {
        Fragment removefragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (removefragment != null) {
            getSupportFragmentManager().beginTransaction().remove(removefragment).commit();
        }
    }


    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        try {
            Utility.showLog(response.toString());
            if (body instanceof ResponseBlocked) {
                getViewModel().setBlockedUsersList(((ResponseBlocked) body).getResource());
            } else if (body instanceof ResponseFollowers) {
                List<Followers> followers_list;
                List<Followers> followings_list;
                List<Followers> followers_followings;
                followers_followings = ((ResponseFollowers) body).getResource();
                followers_list = getFollowersList(followers_followings);
                followings_list = getFollowingList(followers_followings);
                getViewModel().setFollowers(followers_list);
                getViewModel().setFollowings(followings_list);
            }

        } catch (Exception e) {
            OnException(e, endpoint);
        }
    }

    private List<Followers> getFollowersList(List<Followers> resource) {
        List<Followers> followerList = new ArrayList<>();

        for (Followers child : resource) {
            if (child.getUserid().equalsIgnoreCase(getViewModel().getLoggedUser().getUsername()))
                followerList.add(child);

        }
        return followerList;
    }

    private List<Followers> getFollowingList(List<Followers> resource) {
        List<Followers> followingList = new ArrayList<>();

        for (Followers child : resource) {
            if (!child.getUserid().equalsIgnoreCase(getViewModel().getLoggedUser().getUsername()))
                followingList.add(child);

        }
        return followingList;
    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {

    }

    @Override
    public void OnException(Throwable e, String endpoint) {

    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {

    }


    @Override
    public void onSubscriptionCreated() {
        Utility.showLog("Subscribed");
    }

    @Override
    public void onSubscriptionError(Exception e, int i) {
        Utility.showELog(e);
    }

    @Override
    public void onSubscriptionDeleted(boolean b) {
        Utility.showLog("Subscription Deleted");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void logout(Events events) {
        switch (events) {
            case Logout:
                OnLogout();
                break;
            case FOLLOWER:
                getFollowersList();
                break;
            case BLOCKED:
                getBlockList();
                break;
            case UPDATE:
                updateUser();
                break;


        }

    }

    private void updateUser() {
        if (Utility.isNetworkAvailable(this)) {
            Users user = Utility.getloggedUser(this);
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", user);
            Utility.getService(URL).updateUser(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {


                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

        }
    }

    private void getFollowersList() {
        if (getViewModel().getLoggedUser() != null) {
            NetworkCall.CallAPI(this, getViewModel().getService(Constants.DreamFactory.URL).getFollowers(("(followerid like %" + getViewModel().getLoggedUser().getUsername() + "%) or (userid like %" + getViewModel().getLoggedUser().getUsername() + "%)")), this, false, ResponseFollowers.class, Constants.Endpoints.FOLLOWERS);
        }
    }

    public void getBlockList() {
        if (getViewModel().getLoggedUser() != null) {
            NetworkCall.CallAPI(this, getViewModel().getService(Constants.DreamFactory.URL).getBlockedList("userid = " + getViewModel().getLoggedUser().getUsername()), this, false, ResponseBlocked.class, Constants.DreamFactory.GET_BlOCKED);
        }
    }

    private void OnLogout() {
        QBUser qbUser = new Gson().fromJson(getViewModel().getSharedPreference().getString(Constants.SharedPreference.QB_USER, ""), QBUser.class);
        SubscribeService.unSubscribeFromPushes(this);
      //  qbUser=null;
        QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                if(savedUser != null )
                    savedUser = null;
                getViewModel().getSharedPreference().edit().clear().apply();
                OnFragmentChange(new LoginWithLinkedIn(), LOGIN);
                clearImages();
            }

            @Override
            public void onError(QBResponseException e) {
                Utility.showELog(e);
                getViewModel().getSharedPreference().edit().clear().apply();
                OnFragmentChange(new LoginWithLinkedIn(), LOGIN);
                clearImages();
            }
        });
    }

    private void clearImages() {
        Single.fromCallable(() -> {
            Glide.get(this).clearDiskCache();
            return true;
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Utility.showLog("image Cache Clear");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }


}
