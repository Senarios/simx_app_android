package com.senarios.simxx.fragments.mainactivityfragments;


import static com.senarios.simxx.fragments.mainactivityfragments.SplashFragment.savedUser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.SignupFragment;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.ForgotPasswordActivity;
import com.senarios.simxx.callbacks.ActivityContainerCallback;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;
import com.senarios.simxx.services.QbSignUpService;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.Objects;
import java.util.regex.Pattern;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginWithLinkedIn extends BaseFragment implements Constants.QB, Constants.SharedPreference {
    private View view;
    private SharedVM sharedVM;
    private ImageView iv_cross;
    private TextView signup,forgot;
    private TextView hyperlink;
    EditText email, password;
    private AppCompatButton btn_login;
    private ActivityContainerCallback callback;
    private ProgressDialog pd;
    String currentUserId;
    private FirebaseAuth auth;
    private Dialog loadingdialog;
    private boolean isShown = false;
    ImageButton showPasswordIcon;

    public LoginWithLinkedIn() {
        // Required empty public constructor
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback=(ActivityContainerCallback)context;
        sharedVM= getViewModel();
    }
    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                // Inflate the layout for this fragment

        if (sharedVM.getSharedPreference().getInt(Constants.SharedPreference.TYPE, -1) == 1) {
            view = inflater.inflate(R.layout.fragment_login_with_linked_in_, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_login_with_linked_in_, container, false);
        }

        initView();

        return view;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        //cross imageview\
        auth = FirebaseAuth.getInstance();
        loadingdialog = new Dialog(getContext());
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        iv_cross = view.findViewById(R.id.iv_cross);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        signup = view.findViewById(R.id.tv_signup);
        forgot = view.findViewById(R.id.forgot_pass);
        hyperlink = view.findViewById(R.id.hyperlink);
        showPasswordIcon = view.findViewById(R.id.showPasswordIcon);
        hyperlink.setMovementMethod(LinkMovementMethod.getInstance());
        hyperlink.setVisibility(View.GONE);
        signup.setOnClickListener(v -> {
            callback.OnFragmentChange(new SignupFragment(), FragmentTags.SIGNUP);
        });
        forgot.setOnClickListener(v->{
            startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
        });
        iv_cross.setOnClickListener(v -> {
            callback.OnFragmentChange(new LoginFragment(), FragmentTags.LOGIN);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        //linkedin
        btn_login = view.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            loginUser();
//                callback.OnFragmentChange(new HomeFragment(),FragmentTags.HOME);
        });
        showPasswordIcon.setOnClickListener(v -> {
            if (isShown) {
                showPasswordIcon.setImageResource(R.drawable.ic_eyeicon_pass);
                password.setTransformationMethod(new PasswordTransformationMethod());
                password.setSelection(password.getText().length());
                isShown = false;
            } else {
                showPasswordIcon.setImageResource(R.drawable.ic_resetpasswordeye);
                password.setTransformationMethod(null);
                password.setSelection(password.getText().length());
                isShown = true;
            }
        });
//        SpannableString Signup_Text = new SpannableString(getString(R.string.sign_up));
//        ClickableSpan span = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View view) {
//                sharedVM.getSharedPreference().edit().putInt(Constants.SharedPreference.TYPE, 1).apply();
//                callback.OnFragmentChange(new WebViewFragment(), FragmentTags.WEBVIEW);
//                view.invalidate();
//            }
//
//            @Override
//            public void updateDrawState(@NonNull TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setColor(Color.parseColor("#0a497a"));
//                ds.setUnderlineText(false);
//
//            }
//        };
//        Signup_Text.setSpan(span, 22, 29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        signup.setText(Signup_Text);
//        signup.setMovementMethod(LinkMovementMethod.getInstance());
//        //intialize pd;
        pd=Utility.setDialogue(getContext());

        //getbundle

        if (getArguments() != null) {
            getAccessToken(getArguments().getString(Constants.SharedPreference.LinkedIn_Code));
        }


    }

    private void loginUser() {
        String user_email = email.getText().toString().trim();
        String user_pass = password.getText().toString().trim();
        sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Email, user_email).apply();

        if (user_email.isEmpty()) {
            Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user_pass.isEmpty()) {
            Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingdialog.show();
            auth.signInWithEmailAndPassword(user_email, user_pass)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if(auth.getCurrentUser().isEmailVerified()) {
                                FirebaseDatabase.getInstance().getReference().child("UserNames")
                                        .orderByChild("email").equalTo(user_email).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        loadingdialog.dismiss();
                                        for (DataSnapshot child: snapshot.getChildren()) {
                                            Log.d("User key", child.getKey());
                                            getUser(child.getKey());

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        loadingdialog.dismiss();
                                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                loadingdialog.dismiss();
                                Toast.makeText(getContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingdialog.dismiss();
                    Toast.makeText(getContext(), "Email or password are incorrect", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private void getAccessToken(String code) {
        if (!Objects.requireNonNull(getActivity()).isFinishing()) {
            pd.show();
        }
        sharedVM.getService(Constants.LinkedIn.Base_Access_Token)
                .getAccessToken(Constants.LinkedIn.grantType, code, Constants.LinkedIn.redirectURL, Constants.LinkedIn.clientId, Constants.LinkedIn.clientSecret).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<Response<JsonObject>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            Log.v("emailtoken", Objects.requireNonNull(response.body()).toString());
                            String Accesscode = Objects.requireNonNull(response.body()).get("access_token").getAsString();
                            Log.v("Access_Token", Accesscode);
                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.ACCESS_TOKEN, Accesscode).apply();
                            getProfile(Accesscode);
                        } else {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    pd.dismiss();
                    Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(Throwable e) {
                pd.dismiss();
                Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getProfile(String access_token) {
        sharedVM.getService(Constants.LinkedIn.V2_API)
                .getProfile("Bearer " + access_token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<JsonObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    pd.dismiss();
                                    Log.v("emailonfetch", Objects.requireNonNull(response.body()).toString());
                                    Toast.makeText(getContext(), "Please Wait while we fetch details..", Toast.LENGTH_SHORT).show();
                                    String id = response.body().get("id").getAsString();
                                    String fullname = response.body().get("localizedFirstName").getAsString()
                                            + response.body().get("localizedLastName").getAsString();
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Linkedin_ID, id).apply();
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Fullname, fullname).apply();
                                    sharedVM.getSharedPreference().edit().putString(SharedPreference.QUICKB_ID, fullname).apply();
//                                    getUser(id, access_token);
                                } else {
                                    pd.dismiss();
                                    Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                pd.dismiss();
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

                    }
                });

    }
    private void getUser(String id){
        pd.show();
        sharedVM.getService(Constants.DreamFactory.URL)
                .getUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<Users>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<Users> response) {
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    pd.dismiss();
                                    FirebaseInstanceId.getInstance().getToken();
                                    Log.v("postuser", new Gson().toJson(response.body()));
                                    savedUser = new Gson().fromJson(String.valueOf(response.body()),Users.class);
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.USER, new Gson().toJson(response.body())).apply();
                                    sharedVM.getSharedPreference().edit().putString(SharedPreference.QUICKB_ID, response.body().getQbid()).apply();
                                    sharedVM.getSharedPreference().edit().putBoolean(Constants.SharedPreference.Login_Boolean, true).apply();

                                    Intent signup = new Intent(getActivity(), QbSignUpService.class);
                                    signup.putExtra(QB_USER_LOGIN, email.getText().toString());
                                    signup.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
                                    signup.putExtra(QB_FULL_NAME, sharedVM.getSharedPreference().getString(Fullname, ""));
                                    requireActivity().startService(signup);

                                    callback.OnFragmentChange(new HomeFragment(), FragmentTags.HOME);
                                } else {
//                                    getEmailAddress(acess_token);
                                }

                            } else {
                                pd.dismiss();
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                        } else if (response.code() == 404) {
                            pd.dismiss();
                        } else {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                    }
                });

    }


}
