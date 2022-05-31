//package com.senarios.simxx.activities;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import androidx.databinding.DataBindingUtil;
//import androidx.databinding.ViewDataBinding;
//
//import android.content.Intent;
//import android.os.Build;
//import android.text.SpannableString;
//import android.text.Spanned;
//import android.text.TextPaint;
//import android.text.style.ClickableSpan;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//import com.anjlab.android.iab.v3.BillingProcessor;
//import com.anjlab.android.iab.v3.PurchaseInfo;
//import com.anjlab.android.iab.v3.SkuDetails;
//import com.anjlab.android.iab.v3.TransactionDetails;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.wallet.IsReadyToPayRequest;
//import com.google.android.gms.wallet.PaymentData;
//import com.google.android.gms.wallet.PaymentsClient;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.paypal.android.sdk.payments.PayPalConfiguration;
//import com.paypal.android.sdk.payments.PayPalPayment;
//import com.paypal.android.sdk.payments.PaymentConfirmation;
//import com.hdev.common.Constants;
//import com.senarios.simxx.FragmentTags;
//import com.senarios.simxx.PaymentsUtil;
//import com.senarios.simxx.R;
//import com.senarios.simxx.Utility;
//import com.hdev.common.datamodels.Users;
//import com.senarios.simxx.databinding.ActivityPaymentBinding;
//
//import org.json.JSONObject;
//
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Optional;
//
//import io.reactivex.SingleObserver;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//import retrofit2.Response;
//
//public class PaymentActivity extends BaseActivity implements Constants.SharedPreference, Constants.Paypal, Constants.Messages, FragmentTags, Constants.DreamFactory, View.OnClickListener, BillingProcessor.IBillingHandler {
//    private ActivityPaymentBinding binding;
//    private PayPalConfiguration configuration = new PayPalConfiguration()
//            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
//            .clientId(P_CLIENT_ID)
//            .forceDefaultsOnSandbox(false);
//    private final int PAYMENT_CODE = 701;
//    private PaymentsClient paymentsClient;
//    private JsonObject baseCardPaymentMethod;
//    private BillingProcessor billingProcessor;
//    TransactionDetails transactionDetails;
//
//    @Override
//    public ViewDataBinding binding() {
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);
//        return binding;
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    public void init() {
//        binding.firstProduct.setOnClickListener(this);
//        binding.secondProduct.setOnClickListener(this);
//        binding.toolbar.setSubtitleTextColor(getColor(R.color.black));
//        binding.toolbar.setSubtitle("Current Credits : " + getViewModel().getLoggedUser().getCredit());
//        binding.toolbar.setNavigationOnClickListener(v -> {
//            finish();
//        });
//        SpannableString spannableString = new SpannableString("I agree to Terms and Conditions");
//        spannableString.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//
//            }
//
//            @Override
//            public void updateDrawState(@NonNull TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setColor(getResources().getColor(R.color.primary));
//                ds.setUnderlineText(true);
//            }
//        }, 11, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        binding.agreementBox.setText(spannableString);
//        paymentsClient = PaymentsUtil.createPaymentsClient(this);
////        possiblyShowGooglePayButton();
//
//        billingProcessor = new BillingProcessor(this, getResources().getString(R.string.play_console_license), this);
//        billingProcessor.initialize();
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private void possiblyShowGooglePayButton() {
//
//        final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
//        if (!isReadyToPayJson.isPresent()) {
//            return;
//        }
//
//        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
//        // OnCompleteListener to be triggered when the result of the call is known.
//        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
//        Task<Boolean> task = paymentsClient.isReadyToPay(request);
//        task.addOnCompleteListener(this,
//                new OnCompleteListener<Boolean>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Boolean> task) {
//                        if (task.isSuccessful()) {
//                            // setGooglePayAvailable(task.getResult());
//                            Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Log.w("isReadyToPay failed", task.getException());
//                        }
//                    }
//                });
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    public void onClick(View v) {
//        if (!binding.agreementBox.isChecked()) {
//            binding.agreementBox.setError("Agree to Terms and conditions");
//        } else if (Utility.getString(binding.paymentAmount).isEmpty()) {
//            binding.paymentAmount.setError("Enter Amount");
//            binding.paymentAmount.requestFocus();
//        } else {
////            binding.agreementBox.setError(null);
////            PayPalPayment paypal_payment = paypal(Utility.getString(binding.paymentAmount) );
////
////            Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);
////
////            // send the same configuration for restart resiliency
////            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, configuration);
////
////            intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, paypal_payment);
////
////          //  startActivityForResult(intent, PAYMENT_CODE);
////            ;
////            AutoResolveHelper.resolveTask(
////                    paymentsClient.loadPaymentData(PaymentsUtil.getPaymentDataRequest(Utility.getString(binding.paymentAmount))),
////                    this, PAYMENT_CODE);
//        }
//
//    }
//
//    private PayPalPayment paypal(String amount) {
//        BigDecimal bigDecimal = new BigDecimal(amount);
//        return new PayPalPayment(bigDecimal, "USD", "Bill For Premium Account",
//                PayPalPayment.PAYMENT_INTENT_SALE);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PAYMENT_CODE && resultCode == RESULT_OK) {
//
//            if (data != null) {
//                PaymentConfirmation confirm =
//                        data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//
//                PaymentData paymentData = PaymentData.getFromIntent(data);
//                if (paymentData != null) {
//                    Users user = getViewModel().getLoggedUser();
//                    float dummy = getViewModel().getLoggedUser().getCredit() + Float.parseFloat(Utility.getString(binding.paymentAmount));
//                    user.setCredit(dummy);
//                    updateUser(user);
//                }
//            }
//
//        }
//    }
//
//    private void updateUser(Users user) {
//        if (Utility.isNetworkAvailable(this)) {
//            getDialog().show();
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("resource", user);
//            getViewModel().getService(URL).updateUser(map)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new SingleObserver<Response<JsonObject>>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onSuccess(Response<JsonObject> response) {
//                            getDialog().dismiss();
//                            if (response.isSuccessful()) {
//                                if (response.code() == 200) {
//                                    if (response.body() != null) {
//                                        Toast.makeText(PaymentActivity.this, "Credits Updated", Toast.LENGTH_SHORT).show();
//                                        setPreferences(USER, new Gson().toJson(user));
//                                        finish();
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                                    }
//                                } else {
//                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                                }
//
//                            } else {
//                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            getDialog().dismiss();
//                            Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//        } else {
//            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        finish();
//    }
//
//    private void addCredits(float credits) {
//        Users user = getViewModel().getLoggedUser();
//        float dummy = getViewModel().getLoggedUser().getCredit() + credits;
//        user.setCredit(dummy);
//        updateUser(user);
//    }
//
//    @Override
//    public void onProductPurchased(String productId, TransactionDetails details) {
//        Toast.makeText(this, "Spuccessfully Purchased", Toast.LENGTH_SHORT).show();
//        SkuDetails skuDetails = billingProcessor.getPurchaseListingDetails(productId);
//        billingProcessor.consumePurchase(productId);
//            if (productId.equalsIgnoreCase(getString(R.string.product_one))) {
//                addCredits(50);
//            } else if (productId.equalsIgnoreCase(getString(R.string.product_two))) {
//                addCredits(100);
//            } else if (productId.equalsIgnoreCase(getString(R.string.product_three))) {
//                addCredits(150);
//            }
//    }
//
//    @Override
//    public void onPurchaseHistoryRestored() {
//
//    }
//
//    @Override
//    public void onBillingError(int errorCode, Throwable error) {
//        Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onBillingInitialized() {
//        String firstProductId = getString(R.string.product_one);
//        String secondProductId = getString(R.string.product_two);
//        String thirdProductId = getString(R.string.product_three);
//        binding.firstProduct.setOnClickListener(view -> {
//            if (billingProcessor.isOneTimePurchaseSupported()) {
//                billingProcessor.purchase(PaymentActivity.this, firstProductId);
//            } else {
//                Toast.makeText(getApplicationContext(), "One time purchase is not supported!", Toast.LENGTH_SHORT).show();
//            }
//
//        });
//        binding.secondProduct.setOnClickListener(view -> {
//            if (billingProcessor.isOneTimePurchaseSupported()) {
//                billingProcessor.purchase(PaymentActivity.this, secondProductId);
//            } else {
//                Toast.makeText(getApplicationContext(), "One time purchase is not supported!", Toast.LENGTH_SHORT).show();
//            }
//
//        });
//        binding.thirdProduct.setOnClickListener(view -> {
//            if (billingProcessor.isOneTimePurchaseSupported()) {
//                billingProcessor.purchase(PaymentActivity.this, thirdProductId);
//            } else {
//                Toast.makeText(getApplicationContext(), "One time purchase is not supported!", Toast.LENGTH_SHORT).show();
//            }
//
//        });
//    }
//
//    private boolean hasPurchaseDetails() {
//        if (transactionDetails != null) {
//            return transactionDetails.purchaseInfo != null;
//        } else return false;
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (billingProcessor != null) {
//            billingProcessor.release();
//        }
//        super.onDestroy();
//    }
//}
