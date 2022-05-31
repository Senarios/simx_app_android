package com.senarios.simxx.fragments.homefragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Followers;
import com.hdev.common.datamodels.ResponseFollowers;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.FullScreenActivity;
import com.senarios.simxx.activities.RatingsAndReviewsActivity;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.MyProfileFragmentBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.MyBroadcastFragment;
import com.senarios.simxx.fragments.MyJobRequests;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment implements View.OnClickListener, ApiResponse, RecyclerViewCallback {
    private MyProfileFragmentBinding binding;
    private SharedVM sharedVM;
    private Users user;
    FirebaseDatabase database;
    private Activity mActivity;
    String type;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_profile_fragment, container, false);
        binding = DataBindingUtil.bind(view);
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);
        user = sharedVM.getLoggedUser();
        init();
        return view;
    }

    @Override
    protected void init() {
        if (getParentFragment() != null) {
            ((HomeFragment) getParentFragment()).nav_view.getMenu().getItem(0).setChecked(true);
        }

        binding.setUser(getViewModel().getLoggedUser());

        binding.swipe.setEnabled(false);

        binding.ProfilePicture.setOnClickListener(this);

        binding.settingsBtn.setOnClickListener(this);

        binding.EditProfile.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);

        binding.followersNumber.setOnClickListener(this);

        binding.followingNumber.setOnClickListener(this);
        binding.gallery.setOnClickListener(this);
        binding.ratingLayout.setOnClickListener(this);
        binding.applications.setOnClickListener(this);
        binding.ratingBar.setRating(0);
//        binding.ratingText.setText("0"+"("+"0"+")");
        if (user.getUserRatings()==null) {
            binding.ratingText.setText("0" + "(" + "0" + ")");
        } else {
            binding.ratingText.setText(user.getUserRatings()+"("+user.getTotalRatings()+")");
        }
        initViewPager();
        if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equals("Recruiter")) {
            binding.fragmentTitleTV.setText(getString(R.string.title_gallery));
            loadGalleryFragment();
//            creator.add("My Applications", new MyJobRequests().getClass());
        } else {
            loadMyApplications();
            binding.fragmentTitleTV.setText(getString(R.string.title_my_applications));
        }

        getFollowers();

        showProfilePic();

    }

    private void showProfilePic() {
        if (mActivity == null) {
            return;
        }
//        SharedPreferences editor = getContext().getSharedPreferences("myProfilee", Context.MODE_PRIVATE);
//        String profileid = editor.getString("userProfile", "");
//        database = FirebaseDatabase.getInstance();
//        database.getReference().child("profileimages/" + profileid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (mActivity == null) {
//                            return;
//                        }
//                        String imagee = snapshot.getValue(String.class);
//                        Glide.with(mActivity).load(imagee)
//                                .into(binding.ProfilePicture);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });

    }

    private void initViewPager() {

        FragmentPagerItems.Creator creator;

//        if (getViewModel().getLoggedUser().getSkills().equalsIgnoreCase(UserType.RemoteWorker.toString())) {
        if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equals("Recruiter")) {
            creator  = FragmentPagerItems.with(requireContext()).add("My Gallery", new MyBroadcastFragment().getClass());
//            creator.add("My Applications", new MyJobRequests().getClass());
        } else {
            binding.broadcastsCountLL.setVisibility(View.GONE);
            binding.broadcastsCountView.setVisibility(View.GONE);
            creator  = FragmentPagerItems.with(requireContext()).add("My Applications", new MyJobRequests().getClass());
        }
        binding.view.viewPager.setAdapter(new FragmentPagerItemAdapter(getChildFragmentManager(), creator.create()
        ));
        binding.view.tabs.setViewPager(binding.view.viewPager);
        binding.view.viewPager.setPagingEnabled(false);

    }

    private void getFollowers() {
        binding.swipe.setRefreshing(true);
        NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                        .getFollowers(("(followerid like %" + getViewModel().getLoggedUser().getUsername() + "%) or (userid like %" + getViewModel().getLoggedUser().getUsername() + "%)"))
                , this, false, ResponseFollowers.class, Endpoints.FOLLOWERS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Profile_picture:
                Utility.showLog(Constants.DreamFactory.GET_IMAGE_URL + getViewModel().getLoggedUser().getEmail() + ".png");
                Intent fullImageIntent = new Intent(getContext(), FullScreenActivity.class);
                fullImageIntent.putExtra(FullScreenActivity.IMAGE, Constants.DreamFactory.GET_IMAGE_URL + getViewModel().getLoggedUser().getEmail() + ".png");
                startActivity(fullImageIntent);

//                Intent fullImageIntent = new Intent(getContext(), FullScreenActivity.class);
////                fullImageIntent.putExtra(FullScreenActivity.IMAGE, Constants.DreamFactory.GET_IMAGE_URL + getViewModel().getLoggedUser().getUsername() + ".png");
//                startActivity(fullImageIntent);
                break;
            case R.id.settings_btn:
                getHomeContainer().OnChange(new SettingsFragment(), FragmentTags.SETTINGS);
                break;
            case R.id.followers_number:
                getHomeContainer().OnChange(new FollowersFragment(), FragmentTags.FOLLOWERS);
                break;

            case R.id.following_number:
                getHomeContainer().OnChange(new FollowingsFragment(), FragmentTags.FOLLOWINGS);
                break;
            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
            case R.id.rating_layout:
                startActivity(new Intent(getContext(), RatingsAndReviewsActivity.class)
                        .putExtra(USER, binding.getUser()));
                break;

            case R.id.Edit_profile:
                getHomeContainer().OnChange(new EditProfileFragment(), FragmentTags.EDITPROFILE);
                break;
            case R.id.gallery:
                loadGalleryFragment();
                break;
            case R.id.applications:
//                if (getViewModel().getLoggedUser().getSkills().equalsIgnoreCase(UserType.RemoteWorker.toString())) {
                if (getViewModel().getLoggedUser().getSkills()!=null) {

                    getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container, new MyJobRequests()).commitAllowingStateLoss();
                    binding.applications.setTextColor(getResources().getColor(R.color.white));
                    binding.applications.setBackground(getResources().getDrawable(R.drawable.appointments_selected));
                    binding.gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
                    binding.gallery.setBackground(getResources().getDrawable(R.drawable.message_button_unselected));
                }
                break;


        }
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

            binding.followingNumber.setText("" + followings_list.size());
            binding.followersNumber.setText("" + followers_list.size());

            getViewModel().setFollowers(followers_list);
            getViewModel().setFollowings(followings_list);
        }


    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        binding.swipe.setRefreshing(false);

    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        binding.swipe.setRefreshing(false);
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        binding.swipe.setRefreshing(false);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getBroadcast(List<Broadcasts> broadcasts) {
        if (broadcasts != null) {
            binding.broadcastsNumber.setText("" + broadcasts.size());
        }
    }

    private void loadGalleryFragment() {
        getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container, new MyBroadcastFragment()).commitAllowingStateLoss();
        binding.gallery.setTextColor(getResources().getColor(R.color.white));
        binding.gallery.setBackground(getResources().getDrawable(R.drawable.messages_selected));
        binding.applications.setTextColor(getResources().getColor(R.color.colorPrimary));
        binding.applications.setBackground(getResources().getDrawable(R.drawable.appointments_button_unselected));
    }

    private void loadMyApplications(){
        if (getViewModel().getLoggedUser().getSkills()!=null) {

            getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container, new MyJobRequests()).commitAllowingStateLoss();
            binding.applications.setTextColor(getResources().getColor(R.color.white));
            binding.applications.setBackground(getResources().getDrawable(R.drawable.appointments_selected));
            binding.gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
            binding.gallery.setBackground(getResources().getDrawable(R.drawable.message_button_unselected));
        }
    }

}
