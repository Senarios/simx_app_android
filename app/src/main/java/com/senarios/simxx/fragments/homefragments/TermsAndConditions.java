package com.senarios.simxx.fragments.homefragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.senarios.simxx.callbacks.LogoutCallback;
import com.senarios.simxx.databinding.FragmentTermsAndConditionsBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import org.w3c.dom.Text;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class TermsAndConditions extends BaseFragment implements View.OnClickListener, FragmentTags {
    private FragmentTermsAndConditionsBinding binding;

    public TermsAndConditions() {
        // Required empty public constructor
    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_terms_and_conditions, container, false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        binding.toolbar.setNavigationOnClickListener(this);
        binding.tvTc.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onClick(View v) {
//        homeContainerCallback.OnChange(new SettingsFragment(),SETTINGS);
        if (getParentFragment()!=null) {
            getParentFragment().getChildFragmentManager().popBackStack();
        }
    }
}
