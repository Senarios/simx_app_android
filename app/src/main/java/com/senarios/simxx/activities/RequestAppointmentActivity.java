package com.senarios.simxx.activities;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;

import com.senarios.simxx.R;
import com.senarios.simxx.databinding.ActivityRequestAppointmentBinding;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.fragments.homefragments.RequestAppointmentFragment;

public class RequestAppointmentActivity extends BaseActivity {
    private ActivityRequestAppointmentBinding binding;

    @Override
    public ActivityRequestAppointmentBinding binding() {
        binding= DataBindingUtil.setContentView(this,R.layout.activity_request_appointment);
        return binding;
    }



    @Override
    public void getData(Intent intent) {
     if (intent!=null && intent.hasExtra(USER)){
         getViewModel().setOpponent_user((Users) getIntent().getSerializableExtra(USER));
         getSupportFragmentManager().beginTransaction().replace(binding.container.getId(),new RequestAppointmentFragment())
                 .commitAllowingStateLoss();
     }

    }

    @Override
    public void init() {

    }


}
