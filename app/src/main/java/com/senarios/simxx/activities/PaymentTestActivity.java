package com.senarios.simxx.activities;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.senarios.simxx.R;
import com.senarios.simxx.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaymentTestActivity extends AppCompatActivity implements PurchasesUpdatedListener,View.OnClickListener {
    private BillingClient billingClient;
    public static final String PRODUCT_ID1 = "product_01";
    public static final String PRODUCT_ID2 = "product_02";
    public static final String PRODUCT_ID3 = "product_03";
    Button firstBtn,secondBtn,thirdBtn;
    public static final String PREF_FILE= "MyPref";
    public static final String PURCHASE_KEY= "purchase";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_test);

        firstBtn = findViewById(R.id.first_product);
        secondBtn = findViewById(R.id.second_product);
        thirdBtn = findViewById(R.id.third_product);
        firstBtn.setOnClickListener(this);
        secondBtn.setOnClickListener(this);
        thirdBtn.setOnClickListener(this);
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> list) {
        if (firstBtn.isPressed()) {
            //if item newly purchased
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                handlePurchases(list,PRODUCT_ID1);
            }
            //if item already purchased then check and reflect changes
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
                List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if(alreadyPurchases!=null){
                    handlePurchases(alreadyPurchases,PRODUCT_ID1);
                }
            }
            //if purchase cancelled
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(getApplicationContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
            }
            // Handle any other error msgs
            else {
//            Toast.makeText(getApplicationContext(),"Error y"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
            }
        } else if (secondBtn.isPressed()) {
            //if item newly purchased
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                handlePurchases(list,PRODUCT_ID2);
            }
            //if item already purchased then check and reflect changes
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
                List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if(alreadyPurchases!=null){
                    handlePurchases(alreadyPurchases,PRODUCT_ID2);
                }
            }
            //if purchase cancelled
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(getApplicationContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
            }
            // Handle any other error msgs
            else {
//            Toast.makeText(getApplicationContext(),"Error y"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
            }
        } else if (thirdBtn.isPressed()) {
            //if item newly purchased
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                handlePurchases(list,PRODUCT_ID3);
            }
            //if item already purchased then check and reflect changes
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
                List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
                if(alreadyPurchases!=null){
                    handlePurchases(alreadyPurchases,PRODUCT_ID3);
                }
            }
            //if purchase cancelled
            else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(getApplicationContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
            }
            // Handle any other error msgs
            else {
//            Toast.makeText(getApplicationContext(),"Error y"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    void handlePurchases(List<Purchase>  purchases,String prodId) {
        for(Purchase purchase:purchases) {
            //if item is purchased
            if (prodId.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
            {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
//                    Toast.makeText(getApplicationContext(), "Error : Invalid Purchase", Toast.LENGTH_SHORT).show();
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if(!getPurchaseValueFromPref()){
                        savePurchaseValueToPref(true);
                        Toast.makeText(getApplicationContext(), "Item Purchased", Toast.LENGTH_SHORT).show();
                        this.recreate();
                    }
                }
            }
            //if purchase is pending
            else if(prodId.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING)
            {
                Toast.makeText(getApplicationContext(),
                        "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
            }
            //if purchase is unknown
            else if(prodId.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE)
            {
                savePurchaseValueToPref(false);
//                purchaseStatus.setText("Purchase Status : Not Purchased");
//                purchaseButton.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Purchase Status Unknown", Toast.LENGTH_SHORT).show();
            }
        }
    }

    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                savePurchaseValueToPref(true);
                Toast.makeText(getApplicationContext(), "Item Purchased", Toast.LENGTH_SHORT).show();
                PaymentTestActivity.this.recreate();
            }
        }
    };
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArVKU6FIo3mOxY7szY6G94xp+Abti/+iOndUZq2TvUIJSsj+VKyw3wcut3dWUtr9JqvpOMXvjg4IcfEx5dBs0u/1rMnUYP9m47ByFATVzW0agPDUvfh/DRDPGuHiTxgrrt4jVwSC4ABi9F3/wYg7ZG20nh7bYYZwgxHbomhV8UKMXZBqB6TC729zqAgQOgMFVs+y0GPhusdFni1iItqgODB8dLJViFBY6W8caUVxhRk58blbUF4htvaVruwReEQVYFc+WaLacs1I83qa183Y70RM3VKzb20I+fttl3IXkgOMdmsV5EbdtcZ+aiEs2vSg6uaCW/4No58HFtjLahhUttwIDAQAB";
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    private void initiatePurchase(String productId) {
        List<String> skuList = new ArrayList<>();
        skuList.add(productId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(PaymentTestActivity.this, flowParams);
                            }
                            else{
                                //try to add item/product id "purchase" inside managed product in google play console
                                Toast.makeText(getApplicationContext(),"Purchase Item not Found",Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    " Error "+billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void connectService1() {
        if (billingClient.isReady()) {
            initiatePurchase(PRODUCT_ID1);
        }
        //else reconnect service
        else{
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(PRODUCT_ID1);
                    } else {
//                        Toast.makeText(getApplicationContext(),"Error b"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }
    public void connectService2() {
        if (billingClient.isReady()) {
            initiatePurchase(PRODUCT_ID2);
        }
        //else reconnect service
        else{
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(PRODUCT_ID2);
                    } else {
//                        Toast.makeText(getApplicationContext(),"Error b"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }
    public void connectService3() {
        if (billingClient.isReady()) {
            initiatePurchase(PRODUCT_ID3);
        }
        //else reconnect service
        else{
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase(PRODUCT_ID3);
                    } else {
//                        Toast.makeText(getApplicationContext(),"Error b"+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.first_product:
                connectService1();
                break;
            case R.id.second_product:
                connectService2();
                break;
            case R.id.third_product:
                connectService3();
                break;
        }
    }
    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }
    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }
    private boolean getPurchaseValueFromPref(){
        return getPreferenceObject().getBoolean( PURCHASE_KEY,false);
    }
    private void savePurchaseValueToPref(boolean value){
        getPreferenceEditObject().putBoolean(PURCHASE_KEY,value).commit();
    }
}