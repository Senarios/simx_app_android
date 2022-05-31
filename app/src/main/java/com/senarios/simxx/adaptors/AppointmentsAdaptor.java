package com.senarios.simxx.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.hdev.common.datamodels.AppointmentList;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.List;

public class AppointmentsAdaptor extends RecyclerView.Adapter<AppointmentsAdaptor.holder> {
    private List<AppointmentList> appointmentLists;
    private Context context;
    private SharedVM sharedVM;
    private RecyclerViewCallback listener;

    public AppointmentsAdaptor(List<AppointmentList> appointmentLists, Context context, SharedVM sharedVM, RecyclerViewCallback listener) {
        this.appointmentLists = appointmentLists;
        this.context = context;
        this.sharedVM = sharedVM;
        this.listener = listener;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointmentslist, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        if (appointmentLists.get(position).getAppointments() != null) {
            if (appointmentLists.get(position).getTitle().equalsIgnoreCase(Constants.SELF_APPOINTMENT)) {
                holder.rv.setAdapter(new SelfAppointmentsAdapter(appointmentLists.get(position).getAppointments(), context, sharedVM, listener));
            } else {
                holder.rv.setAdapter(new OtherAppointmentsAdapter(appointmentLists.get(position).getAppointments(), context, sharedVM, listener));
            }
        }
        if (appointmentLists.get(position).getTitle() != null) {
            holder.textView.setText(appointmentLists.get(position).getTitle());
        }

    }

    @Override
    public int getItemCount() {
        return appointmentLists.size();
    }


    class holder extends RecyclerView.ViewHolder {
        private RecyclerView rv;
        private TextView textView;

        public holder(@NonNull View view) {
            super(view);
            rv = view.findViewById(R.id.rv);
            textView = view.findViewById(R.id.textview);
            rv.setLayoutManager(new LinearLayoutManager(context));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (rv.getVisibility() == View.VISIBLE) {
                        rv.setVisibility(View.GONE);
                    } else rv.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}
