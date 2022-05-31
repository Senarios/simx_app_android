package com.senarios.simxx.fragments.mainactivityfragments;


import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;

import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.adaptors.LoginPagerAdaptor;
import com.senarios.simxx.databinding.FragmentLoginBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.login_image_fragment.LoginPagerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment {
   private FragmentLoginBinding binding;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_login, container, false);
        binding= DataBindingUtil.bind(view);
        initView(view);

        return  view;
    }

    private void initView(View view) {
        binding.pager.setAdapter(new LoginPagerAdaptor(getChildFragmentManager(), PagerAdapter.POSITION_UNCHANGED,pager_images()));


       binding.circleIndicator.setViewPager(binding.pager);


        binding.btnLogin.setOnClickListener(view1 -> {
            getViewModel().getSharedPreference().edit().putInt(Constants.SharedPreference.TYPE,0).apply();
            getActivityContainer().OnFragmentChange(new LoginWithLinkedIn(), FragmentTags.LOGINWITHLINKEDIN);
        });




        SpannableString Signup_Text =new SpannableString(getString(R.string.sign_up));
        ClickableSpan span=new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                getViewModel().getSharedPreference().edit().putInt(Constants.SharedPreference.TYPE,1).apply();
                getActivityContainer().OnFragmentChange(new LoginWithLinkedIn(), FragmentTags.LOGINWITHLINKEDIN);
                view.invalidate();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#0a497a"));
                ds.setUnderlineText(false);

            }
        };
        Signup_Text.setSpan(span, 19, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvSignup.setText(Signup_Text);
        binding.tvSignup.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private List<Fragment> pager_images(){
        List<Fragment> fragments=new ArrayList<>();
        fragments.add(returnFragment(R.drawable.login_pager_1));
        fragments.add(returnFragment(R.drawable.login_pager_2));
        fragments.add(returnFragment(R.drawable.login_pager_3));
      return fragments;
    }



    private Fragment returnFragment(int id){
        Fragment fragment=new LoginPagerFragment();
        Bundle b=new Bundle();
        b.putInt(Constants.SharedPreference.Resource,id);
        fragment.setArguments(b);
        return fragment;
    }

}
