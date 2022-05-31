package com.senarios.simxx.activities;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.PaymentResponse;
import com.hdev.common.datamodels.PaymentWithdrawal;
import com.hdev.common.datamodels.PaymentwithdrawalResponse;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.databinding.ActivityWithdrawBinding;
import com.senarios.simxx.viewmodels.SharedVM;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WithdrawActivity extends BaseActivity implements View.OnClickListener, Constants.Paypal, Constants.Messages {
    private ActivityWithdrawBinding binding;
    private SharedVM sharedVM;



    @Override
    public void init() {
        binding.btnSendPayment.setOnClickListener(this);
        binding.toolbar.setSubtitle("Current Credits : " + getViewModel().getLoggedUser().getCredit());
        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        sharedVM = ViewModelProviders.of(Objects.requireNonNull(this)).get(SharedVM.class);

    }

    @Override
    public ViewDataBinding binding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw);
        return binding;
    }

    @Override
    public void onClick(View v) {

        if (getViewModel().getLoggedUser().getCredit().intValue() < 1) {
            getAlertDialoge(this, "Error", "You Don't have Enough Credits.")
                    .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (Utility.getString(binding.bankName).isEmpty()) {
            setError(binding.inputBankName, "Enter bank name");
        }  else if (Utility.getString(binding.payeeName).isEmpty()) {
            setError(binding.inputPayeeName, "Enter payee name");
        } else if (!(Utility.getString(binding.bankName).length() >= 3)) {
            setError(binding.inputBankName, "Enter valid bank name");
        } else if (Utility.getString(binding.accountNo).isEmpty()) {
            setError(binding.inputAccountNo, "Enter account number");
        } else if (!(Utility.getString(binding.accountNo).length() >= 9) || !(Utility.getString(binding.accountNo).length() <= 18)) {
            setError(binding.inputAccountNo, "Enter valid account number");
        }
        else if (Utility.getString(binding.iban).isEmpty()|| !Utility.isIBANValid(binding.iban.getText().toString())) {
            setError(binding.inputIbanNumber, "Enter valid IBAN number");
        }
        else if (Utility.getString(binding.sortCode).isEmpty()|| !Utility.isSortCodeValid(binding.sortCode.getText().toString())) {
            setError(binding.inputSortCode, "Enter valid sort code");
        } else if (Utility.getString(binding.phoneNo).isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
        }else if ((binding.countryCodePicker.getSelectedCountryCodeWithPlus() + binding.phoneNo.getText().toString()).isEmpty()) {
            Toast.makeText(this, "Please select country code and enter number", Toast.LENGTH_SHORT).show();
        }
//        else if (Utility.getString(binding.confirmEmail).isEmpty()){
//            setError(binding.inputConfirmEmail,"Verify Email");
//        }
//        else if (!Utility.getString(binding.email).equalsIgnoreCase(Utility.getString(binding.confirmEmail))){
//            Toast.makeText(this, "Email And Confirm Email didnt match...", Toast.LENGTH_SHORT).show();
//        }
        else {
            String id = sharedVM.getSharedPreference().getString(SharedPreference.Email, "");
            getUser(id);
//            getAccessToken();
        }

    }

    private void getAccessToken() {
        if (Utility.isNetworkAvailable(this)) {
            getDialog().show();
            getViewModel().getServiceforPaypal(Paypal.BASE_URL)
                    .getPaypal_AccessToken(GRANT_TYPE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            getDialog().dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        String token = response.body().get("access_token").getAsString();
//                                        sendPayment(token);

                                    } else {
                                        Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }


                        }

                        @Override
                        public void onError(Throwable e) {
                            getDialog().dismiss();
                        }
                    });
        } else {
            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendPayment(PaymentWithdrawal paymentWithdrawal) {
        if (Utility.isNetworkAvailable(this)) {
            getDialog().show();
            sharedVM.getService(Constants.DEFAULT_URL)
                    .sendPayment(paymentRequestBody(paymentWithdrawal))
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            getDialog().dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        Gson gson = new Gson();
                                        PaymentwithdrawalResponse paymentwithdrawalResponse = gson.fromJson(String.valueOf(response.body()), PaymentwithdrawalResponse.class);
                                        if (paymentwithdrawalResponse.getPaymentResponse() != null) {
                                            PaymentResponse paymentResponse = paymentwithdrawalResponse.getPaymentResponse();
                                            if (paymentResponse.getPayment_status().equalsIgnoreCase("pending")) {
                                                showDialog(paymentResponse.getMessage());
                                                resetFields();
                                            } else {
                                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            getDialog().dismiss();
                        }
                    });
        }

    }
    private void resetFields(){
        binding.accountNo.setText("");
        binding.bankName.setText("");
        binding.iban.setText("");
        binding.sortCode.setText("");
        binding.phoneNo.setText("");
        binding.bic.setText("");
        binding.payeeName.setText("");
    }

//    private void sendPayment(String token) {
//        if (Utility.isNetworkAvailable(this)) {
//            getDialog().show();
//            BaseRequest baseRequest = new BaseRequest();
//            Item item = new Item();
//            SenderBatchHeader senderBatchHeader = new SenderBatchHeader();
//            senderBatchHeader.setSenderBatchId(getString());
//            Amount amount = new Amount(round(getViewModel().getLoggedUser().getCredit(), 2) + "", "USD");
//            item.setAmount(amount);
//            item.setReceiver(Utility.getString(binding.email).trim());
//            item.setRecipientType("EMAIL");
//            List<Item> items = new ArrayList<>();
//            items.add(item);
//            baseRequest.setItems(items);
//            baseRequest.setSenderBatchHeader(senderBatchHeader);
//            Log.v("Data", new Gson().toJson(baseRequest));
//            getViewModel().getService(Paypal.BASE_URL)
//                    .pay("Bearer " + token, baseRequest)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
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
//                                if (response.code() == 201) {
//                                    if (response.body() != null) {
//                                        Users users = getViewModel().getLoggedUser();
//                                        users.setCredit(0.0f);
//                                        getViewModel().getSharedPreference().edit().putString(Constants.SharedPreference.USER, new Gson().toJson(users)).commit();
//                                        EventBus.getDefault().post(Events.UPDATE);
//                                        finish();
//                                        Toast.makeText(WithdrawActivity.this, "Payment Transferred", Toast.LENGTH_SHORT).show();
//
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                                    }
//                                } else {
//                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
//                            }
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

    private void showDialog(String message) {
        Utility.getAlertDialoge(this, "Payment", message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    protected String getString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private void setError(TextInputLayout layout, String error) {
        layout.setErrorEnabled(true);
        layout.setError(error);
        layout.requestFocus();
    }

    private void getUser(String id) {
        getDialog().show();
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
                                    getDialog().dismiss();
                                    Log.v("postuser", new Gson().toJson(response.body()));
                                    Gson gson = new Gson();
                                    String gsonString = gson.toJson(response.body());
                                    Users user = gson.fromJson(gsonString, Users.class);
                                    if (user.getCredit() > 0) {
                                        PaymentWithdrawal paymentWithdrawal = new PaymentWithdrawal();
                                        paymentWithdrawal.setUsername(user.getUsername());
                                        paymentWithdrawal.setBankName(binding.bankName.getText().toString());
                                        paymentWithdrawal.setPayeeName(binding.payeeName.getText().toString());
                                        paymentWithdrawal.setBankAccountNo(binding.accountNo.getText().toString());
                                        paymentWithdrawal.setIbanNo(binding.iban.getText().toString());
                                        if (binding.bic.getText().toString() != null) {
                                            paymentWithdrawal.setBicCode(binding.bic.getText().toString());
                                        }
                                        paymentWithdrawal.setSortCode(binding.sortCode.getText().toString());
                                        paymentWithdrawal.setPhoneNo(binding.countryCodePicker.getSelectedCountryCodeWithPlus()+ binding.phoneNo.getText().toString());
                                        paymentWithdrawal.setCredits(user.getCredit());
                                        sendPayment(paymentWithdrawal);
                                    } else {
                                        showDialog("You do not have enough credits");
                                    }
                                }

                            } else {
                                getDialog().dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            getDialog().dismiss();
                            Toast.makeText(getApplicationContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        getDialog().dismiss();
                        Toast.makeText(getApplicationContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public static Map<String, RequestBody> paymentRequestBody(PaymentWithdrawal paymentWithdrawal) {
        Map<String, RequestBody> map = new HashMap();
        map.put("username", requestBody(paymentWithdrawal.getUsername()));
        map.put("bank_name", requestBody(paymentWithdrawal.getBankName()));
        map.put("payee_name", requestBody(paymentWithdrawal.getPayeeName()));
        map.put("account_no", requestBody(paymentWithdrawal.getBankAccountNo()));
        map.put("iban", requestBody(paymentWithdrawal.getIbanNo()));
        map.put("bic", requestBody(paymentWithdrawal.getBicCode()));
        map.put("sort_code", requestBody(paymentWithdrawal.getSortCode()));
        map.put("phone_no", requestBody(paymentWithdrawal.getPhoneNo()));
        map.put("pending_credit", requestBody(String.valueOf(paymentWithdrawal.getCredits())));
        return map;
    }

    public static RequestBody requestBody(String param) {
        return RequestBody.create(MediaType.parse("text/plain"), String.valueOf(param));
    }

}
