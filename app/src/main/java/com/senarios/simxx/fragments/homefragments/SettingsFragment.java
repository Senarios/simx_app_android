package com.senarios.simxx.fragments.homefragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.hdev.common.datamodels.Events;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.senarios.simxx.callbacks.LogoutCallback;
import com.senarios.simxx.databinding.CardViewSettingsBinding;
import com.senarios.simxx.databinding.FragmentSettingsBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class SettingsFragment extends BaseFragment implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, FragmentTags{
    private ProgressDialog pd;
    private FragmentSettingsBinding binding;
    private FirebaseAuth auth;

    public SettingsFragment() {

    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_settings,container,false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        auth = FirebaseAuth.getInstance();
        binding.toolbar.setNavigationOnClickListener(this);
        binding.navView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {

            getHomeContainer().OnChange(new ProfileFragment(), FragmentTags.PROFILE);



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.blocked:
                getHomeContainer().OnChange(new BlockListFragment(),FragmentTags.BLOCKED);
                break;

            case R.id.faq:
                getHomeContainer().OnChange(new FAQsFragment(),FAQ );
                break;

            case R.id.terms_and_conditon:
                getHomeContainer().OnChange( new TermsAndConditions(), TERMS_CONDITIONS);
                break;

            case R.id.privacy_policy:
                getHomeContainer().OnChange(new PrivacyPolicy(), PRIVACY_POLICY);
                break;

            case R.id.share:
                Intent share_intent = new Intent();
                share_intent.setAction(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_TEXT,"Check this app via\n"+
                        "https://play.google.com/store/apps/details?id="
                        + getContext().getApplicationContext().getPackageName());
                share_intent.setType("text/plain");
                startActivity(Intent.createChooser(share_intent,"Share app via"));
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("text/plain");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download H2Startup");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "h2startup.com");
//                Intent chooserIntent = Intent.createChooser(sharingIntent, "Open With");
//                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(chooserIntent);
                break;

//            case R.id.demo:
//                Utility.makeFilePublic(requireContext(), null, S3Constants.OTHER + "/" + S3Constants.DEMO);
//                break;
            case R.id.linkedin_profile:
                Utility.openBrowser(getActivity(), getString(R.string.linkedin_link) + getViewModel().getLoggedUser().getLink().replace(getString(R.string.linkedin_link),"").trim());
                break;

            case R.id.signout:
                auth.signOut();
                FirebaseAuth.getInstance().signOut();
                EventBus.getDefault().post(Events.Logout);
                break;



        }

        return true;
    }
}
