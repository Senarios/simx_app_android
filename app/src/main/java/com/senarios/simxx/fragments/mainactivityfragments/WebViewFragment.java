package com.senarios.simxx.fragments.mainactivityfragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.LollipopFixedWebView;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.ActivityContainerCallback;
import com.senarios.simxx.viewmodels.SharedVM;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment {
    private SharedVM sharedVM;
    private ActivityContainerCallback callback;

    public WebViewFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback=(ActivityContainerCallback)context;
        sharedVM= new ViewModelProvider(requireActivity()).get(SharedVM.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_web_view, container, false);
        initView(view);


    return view;
    }

    private void initView(View view) {
        //cross icon
        ImageView iv_cross = view.findViewById(R.id.iv_cross);
        iv_cross.setOnClickListener(view1 -> {
            if (getActivity()!=null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


        //webview
        LollipopFixedWebView webView = view.findViewById(R.id.linedin_sign_in);

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webView.clearHistory();
        webView.clearCache(true);
        webView.clearFormData();

        String url=Constants.LinkedIn.Url+"?client_id="+Constants.LinkedIn.clientId
                +"&"
                +"redirect_uri="+Constants.LinkedIn.redirectURL
                +"&"
                +"response_type="+Constants.LinkedIn.responseType
                +"&"
                +"state="+Constants.LinkedIn.state
                +"&"
                +"scope="+Constants.LinkedIn.scope
                ;
        Log.v("LinkedinUrl", url);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.contains("code=")){
                    url=url.split("[?]")[1];
                    url=url.split("=")[1];
                    if (Utility.isNetworkAvailable(requireContext())) {
                        String code = url.split("&")[0];
                        Fragment fragment=new LoginWithLinkedIn();
                        Bundle bundle=new Bundle();
                        bundle.putString(Constants.SharedPreference.LinkedIn_Code,code);
                        fragment.setArguments(bundle);
                        callback.OnFragmentChange(fragment,FragmentTags.LOGINWITHLINKEDIN);
                    }
                    else{
                        Toast.makeText(getContext(), "Enable Internet/Data", Toast.LENGTH_SHORT).show();
                    }
                    Log.v("url", url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });

    }



}
