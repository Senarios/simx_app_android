package com.senarios.simxx.fragments.homefragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Events;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.adaptors.AppointmentsAdaptor;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentAppointmentsBinding;
import com.hdev.common.datamodels.Appointment;
import com.hdev.common.datamodels.AppointmentList;
import com.hdev.common.datamodels.ResponseAppointment;
import com.senarios.simxx.fragments.BaseFragment;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Appointments extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerViewCallback, ApiResponse {
    private FragmentAppointmentsBinding binding;



    public Appointments() {
        // Required empty public constructor
    }



    @Override
    protected void init() {
        super.init();
        getAppointments();
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_appointments, container, false);
        binding= DataBindingUtil.bind(view);
        binding.swipe.setOnRefreshListener(this);
        init();
        return view;
    }

    private void getAppointments() {
        binding.swipe.setRefreshing(true);
        String filter="((patientId="+getViewModel().getLoggedUser().getUsername()+") OR (doctorId="+getViewModel().getLoggedUser().getUsername()+"))";
        NetworkCall.CallAPI(requireContext(),Utility.getService(DreamFactory.URL).getAppointments(filter),this,false,ResponseAppointment.class,"");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (binding.swipe.isRefreshing()){
            binding.swipe.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        getAppointments();
    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        binding.swipe.setRefreshing(false);
         ArrayList<Appointment> other_appointments=new ArrayList<>();
         ArrayList<Appointment> self_appointments=new ArrayList<>();
        if (body instanceof ResponseAppointment){
            for (Appointment appointment : ((ResponseAppointment) body).getResource()) {
                if (appointment.getDoctorId().equalsIgnoreCase(getViewModel().getLoggedUser().getUsername())) {
                    self_appointments.add(appointment);
                } else {
                    other_appointments.add(appointment);
                }


            }
            List<AppointmentList> appointmentLists=new ArrayList<>();
            appointmentLists.add(new AppointmentList(Constants.SELF_APPOINTMENT,self_appointments));
            appointmentLists.add(new AppointmentList(Constants.OTHER_APPOINTMENT,other_appointments));
            binding.recyclerview.setAdapter(new AppointmentsAdaptor(appointmentLists,getContext(),getViewModel(),this));

        }
    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        binding.swipe.setRefreshing(false);
    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        binding.swipe.setRefreshing(false);
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        binding.swipe.setRefreshing(false);
    }

    @Override
    public void onItemPictureClick(int position, Object model) {
        startActivity(new Intent(requireContext(), OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID,model.toString()));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void logout(Events events){
        switch (events){
            case APPOINTMENT:
                getAppointments();
                break;


        }

    }


}
