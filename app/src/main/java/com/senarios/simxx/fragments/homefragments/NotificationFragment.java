package com.senarios.simxx.fragments.homefragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.senarios.simxx.callbacks.NotificationContainerCallback;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.viewmodels.SharedVM;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements View.OnClickListener,NotificationContainerCallback {
    private Button appointments,messages;
    private SharedVM sharedVM;
    private HomeContainerCallback callback;




    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedVM= new ViewModelProvider(requireActivity()).get(SharedVM.class);
        callback=(HomeContainerCallback) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_notification,container,false);
        //
        appointments=view.findViewById(R.id.appointments);
        messages=view.findViewById(R.id.messages);

        init();

        if (getParentFragment()!=null) {
            ((HomeFragment) getParentFragment()).nav_view.getMenu().getItem(2).setChecked(true);
        }
        getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container_message,new MessageFragment()).commitAllowingStateLoss();
        return view;
    }

    private void init() {
        appointments.setOnClickListener(this);
        messages.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.messages){
            getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container_message,new MessageFragment()).commitAllowingStateLoss();
            messages.setTextColor(getResources().getColor(R.color.white));
            messages.setBackground(getResources().getDrawable(R.drawable.messages_selected));
            appointments.setTextColor(getResources().getColor(R.color.colorPrimary));
            appointments.setBackground(getResources().getDrawable(R.drawable.appointments_button_unselected));


        }
        else{
            getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container_message,new Appointments()).commitAllowingStateLoss();
           appointments.setTextColor(getResources().getColor(R.color.white));
            appointments.setBackground(getResources().getDrawable(R.drawable.appointments_selected));
            messages.setTextColor(getResources().getColor(R.color.colorPrimary));
            messages.setBackground(getResources().getDrawable(R.drawable.message_button_unselected));
        }

    }

    @Override
    public void OnChange(Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit).replace(R.id.container_message,new Appointments()).commitAllowingStateLoss();

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedVM.getUsername().removeObservers(this);
    }
}
