package com.hdev.common.datamodels.paypaldatamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BaseRequest {
    @SerializedName("sender_batch_header")
    @Expose
    private SenderBatchHeader senderBatchHeader;
    @SerializedName("items")
    @Expose
    private List<Item> items = null;



    public SenderBatchHeader getSenderBatchHeader() {
        return senderBatchHeader;
    }

    public void setSenderBatchHeader(SenderBatchHeader senderBatchHeader) {
        this.senderBatchHeader = senderBatchHeader;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
