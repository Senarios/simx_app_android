package com.senarios.simxx.fragments.login_image_fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.hdev.common.Constants;
import com.senarios.simxx.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginPagerFragment extends Fragment {

    private ImageView iv_pager;

    public LoginPagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_image_, container, false);
        iv_pager = view.findViewById(R.id.iv_login_pager);
        if (getArguments() != null) {
            iv_pager.setImageResource(getArguments().getInt(Constants.SharedPreference.Resource));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
