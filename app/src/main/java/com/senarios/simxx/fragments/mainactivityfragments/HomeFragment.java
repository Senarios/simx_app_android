package com.senarios.simxx.fragments.mainactivityfragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;
import com.senarios.simxx.fragments.homefragments.NotificationFragment;
import com.senarios.simxx.fragments.homefragments.ProfileFragment;
import com.senarios.simxx.services.ChatLoginService;

import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BaseFragment implements HomeContainerCallback, Constants.QB,BottomNavigationView.OnNavigationItemSelectedListener {
    public BottomNavigationView nav_view;
    private Integer[] anim= {R.anim.card_flip_left_in,R.anim.card_flip_left_out,R.anim.card_flip_right_in,R.anim.card_flip_right_out,
            R.anim.slide_in_down,R.anim.slide_in_up,R.anim.slide_in_left,R.anim.slide_in_right,
            R.anim.slide_out_down,R.anim.slide_out_up,R.anim.slide_out_left,R.anim.slide_out_right,R.anim.fade_in,R.anim.fade_out,
            R.anim.elastic_up,R.anim.elastic_down
    };
    private Random random=new Random();



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.home_screen_fragment, container, false);
        nav_view=view.findViewById(R.id.nav_view);


        //replace first fragment
        nav_view.getMenu().getItem(1).setChecked(true);
       OnChange(new BroadcastsFragment(), FragmentTags.BROADCAST);

        //


        //service
        Intent login = new Intent(getActivity(), ChatLoginService.class);
        login.putExtra(QB_USER_LOGIN,getViewModel().getSharedPreference().getString(Email,"") );
        login.putExtra(QB_PASSWORD,QB_DEFAULT_PASSWORD);
        login.putExtra(QB_FULL_NAME,getViewModel().getSharedPreference().getString(Fullname,""));
        login.putExtra(QB_ID, getViewModel().getSharedPreference().getString(QUICKB_ID,""));
//        login.putExtra(QB_ID, getViewModel().getLoggedUser().getQbid());
        requireContext().startService(login);


        nav_view.setOnNavigationItemSelectedListener(this);

        return view;
    }


    @Override
    public void OnChange(Fragment fragment, String tag) {
        getChildFragmentManager().popBackStack(tag,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_fade_enter,
                        R.anim.fragment_fade_exit
                        ,R.anim.fragment_fade_enter,
                        R.anim.fragment_fade_exit)
                .replace(R.id.home_container, fragment,tag)
                .addToBackStack(tag)
                .commitAllowingStateLoss();



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId()==R.id.profile){
            OnChange(new ProfileFragment(), FragmentTags.PROFILE);
            return true;

        }
        else if (menuItem.getItemId()==R.id.livestream){
            OnChange(new BroadcastsFragment(), FragmentTags.BROADCAST);
            return true;
        }
        else{
            OnChange(new NotificationFragment(), FragmentTags.NOTIFICATION);
            return true;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
