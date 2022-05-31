package com.senarios.simxx.fragments.homefragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.senarios.simxx.databinding.FragmentPrivacyPolicyBinding;
import com.senarios.simxx.fragments.BaseFragment;

import static com.senarios.simxx.FragmentTags.SETTINGS;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivacyPolicy extends BaseFragment implements View.OnClickListener{
    private FragmentPrivacyPolicyBinding binding;

    public PrivacyPolicy() {
        // Required empty public constructor
    }




    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        binding= DataBindingUtil.bind(view);
        init();

        return view ;
    }

    @Override
    protected void init() {
        super.init();
        binding.tvTc.setMovementMethod(new ScrollingMovementMethod());
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentFragment()!=null) {
                    getParentFragment().getChildFragmentManager().popBackStack();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
//        homeContainerCallback.OnChange(new SettingsFragment(),SETTINGS);

    }
}
