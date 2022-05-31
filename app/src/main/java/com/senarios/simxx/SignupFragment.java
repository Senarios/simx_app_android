package com.senarios.simxx;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.callbacks.ActivityContainerCallback;
import com.senarios.simxx.fragments.mainactivityfragments.CreateProfile_IN;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.fragments.mainactivityfragments.LoginWithLinkedIn;
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
public class SignupFragment extends Fragment implements Constants.QB, Constants.SharedPreference {
    private View view;
    private SharedVM sharedVM;
    private EditText nameid, emailid, passwordid, conpassid;
    private Button signupbtn;
    private CheckBox acceptTerms;
    String name ,email , password, conpassword;

    private TextView alreadyregister;
    private String emailpattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])" +
            "(?=.*[a-z])" +
            "(?=.*[A-Z])" +
            "(?=\\S+$)." +
            "{8,15}" +
            "$");
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private TextView hyperlink, chl;
    private AppCompatButton btn_next;
    private ActivityContainerCallback callback;
    private ProgressDialog pd;
    String path;
    private Dialog loadingdialog;
    ImageButton showPassIcon, showConPassIcon;
    //    private FirebaseAuth auth;
    private boolean isShown = false;

    public SignupFragment() {
        // Required empty public constructor
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (ActivityContainerCallback) context;
        sharedVM = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SharedVM.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (sharedVM.getSharedPreference().getInt(Constants.SharedPreference.TYPE, -1) == 1) {
            view = inflater.inflate(R.layout.fragment_signup, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_signup, container, false);
        }

        initView();

        return view;
    }


    private void initView() {
        //cross imageview\
        auth = FirebaseAuth.getInstance();
        loadingdialog = new Dialog(getContext());
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
//        iv_cross = view.findViewById(R.id.iv_cross);
//        btn_next = view.findViewById(R.id.btn_signup_and_next);
        acceptTerms = view.findViewById(R.id.terms_and_condition_cb);
        auth = FirebaseAuth.getInstance();
        nameid = view.findViewById(R.id.name);
        emailid = view.findViewById(R.id.email);
        passwordid = view.findViewById(R.id.password);
        conpassid = view.findViewById(R.id.con_password);
        showPassIcon = view.findViewById(R.id.showPasswordIcon);
        showConPassIcon = view.findViewById(R.id.showConPasswordIcon);
        btn_next = view.findViewById(R.id.btn_signup_and_next);
        alreadyregister = view.findViewById(R.id.tv_already_register);
        hyperlink = view.findViewById(R.id.hyperlink);
//        hyperlink.setMovementMethod(LinkMovementMethod.getInstance());

//        signup.setOnClickListener(v -> {
//            startActivity(new Intent(getActivity(), SignupActivity.class));
//        });
//        iv_cross.setOnClickListener(v -> {
//            callback.OnFragmentChange(new LoginFragment(), FragmentTags.LOGIN);
//            if (getActivity() != null) {
//                getActivity().getSupportFragmentManager().popBackStack();
//            }
//        });

        hyperlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });

        //linkedin
        alreadyregister.setOnClickListener(v -> {
            callback.OnFragmentChange(new LoginWithLinkedIn(), FragmentTags.LOGINWITHLINKEDIN);
        });
        btn_next.setOnClickListener(v -> {
            registerUser();
        });

        showPassIcon.setOnClickListener(v -> {
            if (isShown) {
                showPassIcon.setImageResource(R.drawable.ic_eyeicon_pass);
                passwordid.setTransformationMethod(new PasswordTransformationMethod());
                passwordid.setSelection(passwordid.getText().length());
                isShown = false;
            } else {
                showPassIcon.setImageResource(R.drawable.ic_resetpasswordeye);
                passwordid.setTransformationMethod(null);
                passwordid.setSelection(passwordid.getText().length());
                isShown = true;
            }
        });
        showConPassIcon.setOnClickListener(v -> {
            if (isShown) {
                showConPassIcon.setImageResource(R.drawable.ic_eyeicon_pass);
                conpassid.setTransformationMethod(new PasswordTransformationMethod());
                conpassid.setSelection(conpassid.getText().length());
                isShown = false;
            } else {
                showConPassIcon.setImageResource(R.drawable.ic_resetpasswordeye);
                conpassid.setTransformationMethod(null);
                conpassid.setSelection(conpassid.getText().length());
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
//        pd=Utility.setDialogue(getContext());

        //getbundle

//        if (getArguments() != null) {
//            getAccessToken(getArguments().getString(Constants.SharedPreference.LinkedIn_Code));
//        }


    }

    private void registerUser() {
        name = nameid.getText().toString().trim();
        email = emailid.getText().toString().trim();
        password = passwordid.getText().toString().trim();
        conpassword = conpassid.getText().toString().trim();

        if(name.isEmpty()){
            Toast.makeText(getContext(), "Enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(name.length()>25){
            Toast.makeText(getContext(), "Name too long ", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.isEmpty()){
            Toast.makeText(getContext(), "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!email.matches(emailpattern)){
            Toast.makeText(getContext(), "invalid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.isEmpty()){
            Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(getContext(), "Password must have minimum 8 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number", Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length()<8){
            Toast.makeText(getContext(), "Password too short", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()>15) {
            Toast.makeText(getContext(), "Password too long", Toast.LENGTH_SHORT).show();
            return;
        }

        if(conpassword.isEmpty()){
            Toast.makeText(getContext(), "enter password again", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.matches(conpassword)) {
            conpassid.setError(null);
        } else {
            Toast.makeText(getContext(), "password and confirm password must be same", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!acceptTerms.isChecked()) {
            Toast.makeText(getContext(), "Accept Terms and Conditions to continue", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingdialog.show();
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                        loadingdialog.dismiss();
                        if (isNewUser) {
                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.PASSWORD, password).apply();
                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.CONPASSWORD, conpassword).apply();

                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Fullname, name).apply();
                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Email, email).apply();
//                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Firebase_Create_Id, auth.getCurrentUser().getUid()).apply();
                            Intent signup = new Intent(getActivity(), QbSignUpService.class);
                            signup.putExtra(QB_USER_LOGIN, email);
                            signup.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
                            signup.putExtra(QB_FULL_NAME, sharedVM.getSharedPreference().getString(Fullname, ""));
                            requireActivity().startService(signup);
                            callback.OnFragmentChange(new CreateProfile_IN(), FragmentTags.CREATE_PROFILE_IN);
                            Log.e("TAG", "Is New User!");
                        } else {
                            emailid.setError("email already in use");
                        }

                    }
                });

//        loadingdialog.show();
//        auth.createUserWithEmailAndPassword(email, password)
//                .addOnSuccessListener(authResult -> {
//
////                    database = FirebaseDatabase.getInstance();
////                    GetUserName getUserName = new GetUserName(name);
////                    loadingdialog.dismiss();
//
//                    database = FirebaseDatabase.getInstance();
//                    SignupUserDetails signupUserDetails = new SignupUserDetails(name,path,email);
//                    database.getReference("UserNames").child(auth.getCurrentUser().getUid())
//                            .setValue(signupUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
////                                loadingdialog.dismiss();
//                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Fullname, name).apply();
//                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Email, email).apply();
//                            sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Firebase_Create_Id, auth.getCurrentUser().getUid()).apply();
//
//                            Intent signup = new Intent(getActivity(), QbSignUpService.class);
//                            signup.putExtra(QB_USER_LOGIN, email);
//                            signup.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
//                            signup.putExtra(QB_FULL_NAME, sharedVM.getSharedPreference().getString(Fullname, ""));
//                            requireActivity().startService(signup);
//
//                            loadingdialog.dismiss();
//                            callback.OnFragmentChange(new CreateProfile_IN(), FragmentTags.CREATE_PROFILE_IN);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                                loadingdialog.dismiss();
//                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                loadingdialog.dismiss();
//                emailid.setError("email already in use");
//            }
//        });
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
                                    getUser(id, access_token);
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

    private void getEmailAddress(String access_token) {
        sharedVM.getService(Constants.LinkedIn.V2_API)
                .getEmailAddress("Bearer " + access_token)
                .subscribeOn(Schedulers.io())
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
                                    Log.v("email", Objects.requireNonNull(response.body()).toString());
                                    String email = response.body().get("elements").getAsJsonArray().get(0).getAsJsonObject().get("handle~").getAsJsonObject().get("emailAddress").getAsString();
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.Email, email).apply();
                                    Intent signup = new Intent(getActivity(), QbSignUpService.class);
                                    signup.putExtra(QB_USER_LOGIN, sharedVM.getSharedPreference().getString(Linkedin_ID, ""));
                                    signup.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
                                    signup.putExtra(QB_FULL_NAME, sharedVM.getSharedPreference().getString(Fullname, ""));
                                    requireActivity().startService(signup);
                                    callback.OnFragmentChange(new CreateProfile_IN(), FragmentTags.CREATE_PROFILE_IN);
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

    private void getUser(String id, String acess_token) {
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
                                    Log.v("postuser", new Gson().toJson(response.body()));
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.USER, new Gson().toJson(response.body())).apply();
                                    sharedVM.getSharedPreference().edit().putBoolean(Constants.SharedPreference.Login_Boolean, true).apply();
                                    callback.OnFragmentChange(new HomeFragment(), FragmentTags.HOME);
                                } else {
                                    getEmailAddress(acess_token);
                                }

                            } else {
                                pd.dismiss();
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                        } else if (response.code() == 404) {
                            pd.dismiss();
                            getEmailAddress(acess_token);
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

    private void closeDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View inflatedView = layoutInflater.inflate(R.layout.terms_condition_itemview, null, false);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        final PopupWindow filterPopup = new PopupWindow(inflatedView, width, height, true);
        filterPopup.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        filterPopup.setOutsideTouchable(false);
        filterPopup.setOutsideTouchable(true);
        filterPopup.showAtLocation(inflatedView, Gravity.CENTER, 0, 0);
        filterPopup.getContentView().setFocusableInTouchMode(true);
        filterPopup.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    filterPopup.dismiss();
                    return true;
                }
                return false;
            }
        });

        ImageView cancelBtn = inflatedView.findViewById(R.id.close);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPopup.dismiss();
            }
        });
    }


}
