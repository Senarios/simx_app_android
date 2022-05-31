package com.senarios.simxx.fragments.homefragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.ResponseFollowers;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MainActivity;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.adaptors.FollowersAdapter;
import com.senarios.simxx.adaptors.FollowingsAdapter;
import com.senarios.simxx.callbacks.FollowCallBack;
import com.hdev.common.datamodels.Followers;
import com.senarios.simxx.databinding.FragmentFollowersBinding;
import com.senarios.simxx.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Response;

import static com.senarios.simxx.FragmentTags.PROFILE;

public class FollowersFragment extends BaseFragment implements ApiResponse,View.OnClickListener,Observer<List<Followers>>, FollowCallBack {
    private FollowersAdapter followersAdapter;
    private FragmentFollowersBinding binding;
    private ArrayList<Followers> loggeduser_followings_list;
    private List<Followers> followers;

    public FollowersFragment() {

        // Required empty public constructor
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_followers, container, false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        binding.swipe.setEnabled(false);
        loggeduser_followings_list = new ArrayList<>();
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentFragment()!=null){
                    getHomeContainer().OnChange(new ProfileFragment(),PROFILE);
                }
                else{
                    requireActivity().finish();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().getFollowers().observe(this,this);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onChanged(List<Followers> followers) {
        this.followers = followers;
        getFollowers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getViewModel().getFollowers().removeObservers(this);
    }

    @Override
    public void OnClick(String username) {
        if (username.equalsIgnoreCase(getViewModel().getLoggedUser().getUsername())){
            startActivity(new Intent(requireContext(), MainActivity.class).putExtra(USER,""));
            getActivity().finish();


        }
        else{
            startActivity(new Intent(requireContext(), OtherUserProfileActivity.class).putExtra(DataConstants.USER_ID,username));

        }
    }

    private void getFollowers() {
        binding.swipe.setRefreshing(true);
        NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                        .getFollowers(("(followerid like %" + getViewModel().getLoggedUser().getUsername() + "%) or (userid like %" + getViewModel().getLoggedUser().getUsername() + "%)"))
                , this, false, ResponseFollowers.class, Endpoints.FOLLOWERS);
    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        binding.swipe.setRefreshing(false);
        if (body instanceof ResponseFollowers) {
            ArrayList<Followers> followers_list = new ArrayList<>();
            ArrayList<Followers> followings_list = new ArrayList<>();
            ArrayList<Followers> followers_followings = new ArrayList<>();
            followers_followings.addAll(((ResponseFollowers) body).getResource());
            for (int i = 0; i < followers_followings.size(); i++) {
                if (followers_followings.get(i).getUserid().equals(getViewModel().getLoggedUser().getUsername())) {
                    followings_list.add(followers_followings.get(i));
                } else {
                    followers_list.add(followers_followings.get(i));
                }
            }
            loggeduser_followings_list = followings_list;
            setAdapter();
        }
    }

    private void setAdapter() {
        followersAdapter = new FollowersAdapter(loggeduser_followings_list,(ArrayList<Followers>) followers,getContext(),this);
        binding.rcFollowers.setAdapter(followersAdapter);
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
}
