package com.senarios.simxx.viewmodels;

import static com.senarios.simxx.fragments.mainactivityfragments.SplashFragment.savedUser;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Blocked;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Followers;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.DataService;
import com.hdev.common.retrofit.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

public class SharedVM extends AndroidViewModel {
     private static SharedPreferences preferences;
     private Users user;
     private MutableLiveData<List<Broadcasts>> lv_broadcasts=new MutableLiveData<>();
     private Broadcasts broadcast;
     private MutableLiveData<Integer> id=new MutableLiveData<>();
     private MutableLiveData<List<Followers>> followers=new MutableLiveData<>();
     private MutableLiveData<List<Followers>> followings=new MutableLiveData<>();
     private MutableLiveData<Users> opponent_user=new MutableLiveData<>();
     private MutableLiveData<String> username=new MutableLiveData<>();
     private MutableLiveData<List<Blocked>> blockedUsersList=new MutableLiveData<>();
     private MutableLiveData<List<String>> getBlockedListIds=new MutableLiveData<>();

    public SharedVM(@NonNull Application application) {
        super(application);

    }

    public SharedPreferences getSharedPreference(){
        if (preferences==null){
            preferences=getApplication().getSharedPreferences(Constants.SharedPreference.Preference,Constants.SharedPreference.Preference_Mode );
        }
        return preferences;
    }

    public void setPreferences(String key,Object object){
        if (getSharedPreference()!=null) {
            if (object instanceof String) {
                getSharedPreference().edit().putString(key,(String)object).apply();
            } else if (object instanceof Integer) {
                getSharedPreference().edit().putInt(key,(Integer)object).apply();
            } else if (object instanceof Boolean) {
                getSharedPreference().edit().putBoolean(key,(Boolean)object).apply();
            } else if (object instanceof Users) {
                getSharedPreference().edit().putString(key,new Gson().toJson(object)).apply();
            }
        }
    }

    public Object getPreferences(String key,Object object) {
        if (getSharedPreference() != null) {
            if (object instanceof String) {
                return getSharedPreference().getString(key, "");
            } else if (object instanceof Integer) {
                return getSharedPreference().getInt(key, -1);
            } else if (object instanceof Boolean) {
                return getSharedPreference().getBoolean(key, false);
            }else if (object instanceof Users){
                return new Gson().fromJson(getSharedPreference().getString(Constants.SharedPreference.USER, ""),Users.class);
            }

        }
        return "";
    }


    public DataService getService(String url){
        return RetrofitClientInstance.getinstance(url).getRetrofitInstance().create(DataService.class);
    }
    public DataService getServiceforPaypal(String url){
        return RetrofitClientInstance.getinstance(url).getRetroforPaypal().create(DataService.class);
    }
    public DataService getServiceforTwitter(String url){
        return RetrofitClientInstance.getinstance(url).getRetroforTwitter().create(DataService.class);
    }

    public Users getLoggedUser(){
            user=new Gson().fromJson(getSharedPreference().getString(Constants.SharedPreference.USER,""), Users.class);
            if((user == null || user.getUsername() == null) && savedUser != null){
                user = savedUser;
                getSharedPreference().edit().putString(Constants.SharedPreference.USER,new Gson().toJson(savedUser)).commit();
            }

        return user;
    }

    public void setBroadcast (List<Broadcasts> broadcast){
        lv_broadcasts.setValue(broadcast);
    }
    public LiveData<List<Broadcasts>> getBroadcsats (){
        return lv_broadcasts;
    }

    public void setBroadcast(Broadcasts broadcast){
        this.broadcast=broadcast;
    }
    public Broadcasts getBroadcast(){
        return broadcast;
    }

    public LiveData<Integer> getid (){
        return id;
    }


    public LiveData<List<Followers>> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Followers> followers) {
        this.followers.setValue(followers);
    }



    public LiveData<Users> getOpponent_user() {
        return opponent_user;
    }

    public void setOpponent_user(Users opponent) {
        opponent_user.setValue(opponent);
    }


    //change notification fragment to profile fragment on tag of usericon using these two functions
    public LiveData<String> getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public LiveData<List<Blocked>> getBlockedUsersList() {
        return blockedUsersList;
    }

    public MutableLiveData<List<String>> getGetBlockedListIds() {
        return getBlockedListIds;
    }

    public void setBlockedUsersList(List<Blocked> blockedUsersList) {
        List<String> ids=new ArrayList<>();
        for (Blocked item : blockedUsersList){
            ids.add(item.getBlockedid());
        }
        getBlockedListIds.setValue(ids);
        this.blockedUsersList.setValue(blockedUsersList);
    }

    public Boolean compareID(String item){
        return getBlockedListIds.getValue()!=null && getBlockedListIds.getValue().contains(item);
    }

    public void setUser(Users user) {
        this.user = user;
    }


    public void setFollowings(List<Followers> followings) {
        this.followings.setValue(followings);
    }

    public LiveData<List<Followers>> getFollowings() {
        return followings;
    }
}
