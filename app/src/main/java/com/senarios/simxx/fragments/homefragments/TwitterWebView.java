package com.senarios.simxx.fragments.homefragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.HomeContainerCallback;

import static com.senarios.simxx.FragmentTags.EDITPROFILE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TwitterWebView extends Fragment {
    private HomeContainerCallback homeContainerCallback;
    private WebView webView;

    private String auth_token="";

    public TwitterWebView() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        homeContainerCallback =(HomeContainerCallback)getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_twitter_web_view, container, false);
        webView=view.findViewById(R.id.linedin_sign_in);

        if (getArguments()!=null){
            auth_token=getArguments().getString(Constants.AUTH.AUTH_TOKEN);
        }

        webView.loadUrl(Constants.Twitter.AUTH_WEB_URL+ Constants.AUTH.AUTH_TOKEN+"=" +auth_token);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
                try {
                    if (url.contains("cyberjobscope")){
                        String url_auth_token=url.split("&")[1].split("=")[1];
                        if(url_auth_token.equalsIgnoreCase(auth_token)){
                            String verifier=url.split("&")[2].split("=")[1];
                            Bundle bundle=new Bundle();
                            bundle.putString(Constants.AUTH.AUTH_VERIFIER, verifier);
                            bundle.putString(Constants.AUTH.AUTH_TOKEN, auth_token);
                            Fragment fragment=new EditProfileFragment();
                            fragment.setArguments(bundle);
                            homeContainerCallback.OnChange(fragment, EDITPROFILE);

                        }

                    }
                }
                catch (Exception e){
                    homeContainerCallback.OnChange(new EditProfileFragment(),EDITPROFILE);
                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });


        return view;
    }

}
