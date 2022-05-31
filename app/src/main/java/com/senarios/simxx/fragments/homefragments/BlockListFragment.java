package com.senarios.simxx.fragments.homefragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.adaptors.BlockListAdapter;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentBlockedBinding;
import com.hdev.common.datamodels.Blocked;
import com.senarios.simxx.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class BlockListFragment extends BaseFragment implements Observer<List<Blocked>>,View.OnClickListener , RecyclerViewCallback {
    private FragmentBlockedBinding binding;
    private BlockListAdapter adapter;

    public BlockListFragment() {
    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_blocked, container, false);
        binding= DataBindingUtil.bind(view);

        init();

        return view;
    }

    @Override
    protected void init() {
        super.init();
      binding.toolbar.setNavigationOnClickListener(this);


    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().getBlockedUsersList().observe(this,this);
    }

    @Override
    public void onClick(View v) {
        Utility.showLog("");
        if (getParentFragment()!=null) {
            getParentFragment().getChildFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onChanged(List<Blocked> blockedList) {
        if (blockedList!=null && blockedList.size()>0){
            adapter=new BlockListAdapter((ArrayList<Blocked>) blockedList,getContext(),this);
            binding.rcFollowers.setAdapter(adapter);
        }
        else{
            if (adapter!=null){
                adapter.reset();
            }
        }
    }



    @Override
    public void onItemClick(int position, Object model) {
        if (model instanceof Blocked)
        if (((Blocked) model).getBlockedid().equalsIgnoreCase(getViewModel().getLoggedUser().getUsername())){
            getHomeContainer().OnChange(new ProfileFragment(),FragmentTags.PROFILE);
        }
        else{
            startActivity(new Intent(requireContext(), OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID,((Blocked) model).getBlockedid()));

        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
