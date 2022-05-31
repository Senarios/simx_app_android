package com.hdev.common.datamodels.paypaldatamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hdev.common.datamodels.Broadcasts;

public class BroadcastRequestBody {
    @SerializedName("resource")
    @Expose
    private Broadcasts broadcasts;

    public Broadcasts getBroadcasts() {
        return broadcasts;
    }

    public void setBroadcasts(Broadcasts broadcasts) {
        this.broadcasts = broadcasts;
    }
}
