package com.senarios.simxx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.senarios.simxx.R;
import com.senarios.simxx.fragments.mainactivityfragments.LoginWithLinkedIn;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText registered_email;
    TextView goBack;
    private Dialog loadingdialog;
    AppCompatButton btn_forgot;
    private FirebaseAuth auth;
    private String emailpattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        auth = FirebaseAuth.getInstance();
        registered_email = findViewById(R.id.registered_email);
        btn_forgot = findViewById(R.id.btn_forgot);
        goBack = findViewById(R.id.go_back);
        goBack.setOnClickListener(v -> {
            onBackPressed();
            finish();
        });
        btn_forgot.setOnClickListener(v -> {
            final String email = registered_email.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                registered_email.setError("enter your email");
                return;
            } else {
                registered_email.setError(null);
            }
            if (!email.matches(emailpattern)) {
                registered_email.setError("invalid email");
                return;
            }
            loadingdialog.show();
            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            loadingdialog.dismiss();
                            Toast.makeText(ForgotPasswordActivity.this, "Recovery email sent! check your inbox", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingdialog.dismiss();
                    registered_email.setError("email not found");
                }
            });
        });
    }
}