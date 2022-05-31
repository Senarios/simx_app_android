package com.senarios.simxx.activities;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;

import com.senarios.simxx.R;
import com.senarios.simxx.databinding.ActivityFollowerFollowingBinding;
import com.hdev.common.datamodels.Type;
import com.senarios.simxx.fragments.homefragments.FollowersFragment;
import com.senarios.simxx.fragments.homefragments.FollowingsFragment;

public class FollowerFollowingActivity extends BaseActivity {

    private ActivityFollowerFollowingBinding binding;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public ActivityFollowerFollowingBinding binding() {
     binding= DataBindingUtil.setContentView(this,R.layout.activity_follower_following);
        return binding;
    }

    @Override
    public void getData(Intent intent) {
        if (intent!=null){
            String type= getIntent().getStringExtra(DataConstants.TYPE);
           switch (Type.valueOf(type)){
                case FOLLOWER:
                    getViewModel().setFollowers(intent.getParcelableArrayListExtra(DataConstants.FOLLOWER));
                    getSupportFragmentManager().beginTransaction().replace(binding.container.getId(),new FollowersFragment()).commitAllowingStateLoss();
                    break;

                case FOLLOWING:
                    getViewModel().setFollowings(intent.getParcelableArrayListExtra(DataConstants.FOLLOWER));
                    getSupportFragmentManager().beginTransaction().replace(binding.container.getId(),new FollowingsFragment()).commitAllowingStateLoss();
                    break;
            }
        }

    }

}
