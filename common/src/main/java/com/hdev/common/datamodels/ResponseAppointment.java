package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseAppointment {
    @SerializedName("resource")
    @Expose
    private List<Appointment> resource = null;

    public List<Appointment> getResource() {
        return resource;
    }

    public void setResource(List<Appointment> resource) {
        this.resource = resource;
    }


}
