package com.hdev.common.datamodels.paypaldatamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SenderBatchHeader {
    @SerializedName("sender_batch_id")
    @Expose
    private String senderBatchId;

    public String getSenderBatchId() {
        return senderBatchId;
    }

    public void setSenderBatchId(String senderBatchId) {
        this.senderBatchId = senderBatchId;
    }
}
