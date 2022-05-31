package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallRecords {

    @SerializedName("time_stamp")
    @Expose
    private String timeStamp;
    @SerializedName("caller_id")
    @Expose
    private String callerId;
    @SerializedName("caller_name")
    @Expose
    private String callerName;
    @SerializedName("caller_type")
    @Expose
    private String callerType;
    @SerializedName("receiver_id")
    @Expose
    private String receiverId;
    @SerializedName("receiver_name")
    @Expose
    private String receiverName;
    @SerializedName("receiver_type")
    @Expose
    private String receiverType;
    @SerializedName("call_duration")
    @Expose
    private String callDuration;
    @SerializedName("receiver_hour_rate")
    @Expose
    private String receiverHourRate;
    @SerializedName("call_cost")
    @Expose
    private String callCost;
    @SerializedName("receiver_balance_bc")
    @Expose
    private String receiverBalanceBc;
    @SerializedName("receiver_balance_ac")
    @Expose
    private String receiverBalanceAc;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerType() {
        return callerType;
    }

    public void setCallerType(String callerType) {
        this.callerType = callerType;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public String getReceiverHourRate() {
        return receiverHourRate;
    }

    public void setReceiverHourRate(String receiverHourRate) {
        this.receiverHourRate = receiverHourRate;
    }

    public String getCallCost() {
        return callCost;
    }

    public void setCallCost(String callCost) {
        this.callCost = callCost;
    }

    public String getReceiverBalanceBc() {
        return receiverBalanceBc;
    }

    public void setReceiverBalanceBc(String receiverBalanceBc) {
        this.receiverBalanceBc = receiverBalanceBc;
    }

    public String getReceiverBalanceAc() {
        return receiverBalanceAc;
    }

    public void setReceiverBalanceAc(String receiverBalanceAc) {
        this.receiverBalanceAc = receiverBalanceAc;
    }
}
