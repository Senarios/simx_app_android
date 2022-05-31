package com.hdev.common.datamodels;

import java.util.List;

public class AppointmentList {
    private String title;
    private List<Appointment>appointments;


    public AppointmentList(String title, List<Appointment> appointments) {
        this.title = title;
        this.appointments = appointments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
