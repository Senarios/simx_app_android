package com.hdev.common.datamodels.paypaldatamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("recipient_type")
    @Expose
    private String recipientType;
    @SerializedName("amount")
    @Expose
    private Amount amount;
    @SerializedName("receiver")
    @Expose
    private String receiver;

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

}
