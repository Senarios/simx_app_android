package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.HttpMethod;
import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.hdev.common.datamodels.Appointment;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class SelfAppointmentsAdapter extends RecyclerView.Adapter<SelfAppointmentsAdapter.ViewHolder> {
    private List<Appointment> arrayList;
    private Context context;
    private SharedVM sharedVM;
    private ProgressDialog pd;
    private RecyclerViewCallback listener;

    public SelfAppointmentsAdapter(List<Appointment> arrayList, Context context, SharedVM sharedVM, RecyclerViewCallback listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.sharedVM = sharedVM;
        this.listener = listener;
        pd=Utility.setDialogue(context);
    }

    @NonNull
    @Override
    public SelfAppointmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_appointments_have,parent,false);
        return new ViewHolder(view);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SelfAppointmentsAdapter.ViewHolder holder, int position) {
        holder.time.setText(arrayList.get(position).getTime());
        holder.date.setText(arrayList.get(position).getDate());
        holder.status.setText(arrayList.get(position).getStatus());
        if (holder.status.getText().toString().trim().equalsIgnoreCase("pending")){
            holder.status.setBackgroundColor(context.getResources().getColor( R.color.color_grey));
            holder.status.setTextColor(context.getResources().getColor(R.color.black));
        }else if (holder.status.getText().toString().trim().equalsIgnoreCase("accepted")){
            holder.status.setBackgroundColor(context.getResources().getColor( R.color.Green));
            holder.status.setTextColor(context.getResources().getColor(R.color.white));
        }
        holder.name.setText(arrayList.get(position).getPatientName());


        if (!arrayList.get(position).getStatus().equalsIgnoreCase("pending")){
            holder.swipeRevealLayout.setLockDrag(true);
        }

        if (arrayList.get(position).getStatus().equalsIgnoreCase("accepted")){
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.Green));
            holder.swipeHere.setVisibility(View.GONE);

        }
        else if (arrayList.get(position).getStatus().equalsIgnoreCase("rejected")) {
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.Red));
            holder.swipeHere.setVisibility(View.GONE);
        }


        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getPatientId()+".png")
                .into(holder.image);

//            holder.viewers.setText(""+ 30);




    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView time;
        private TextView date;
        private TextView status;
        private CircleImageView circle_profile_picture;
        private TextView viewers;
        private SwipeRevealLayout swipeRevealLayout;
        private TextView doctorsname;
        private TextView tv_Delete,tv_edit;
        private TextView name , swipeHere;
        private ImageView image,delete;



        private ViewHolder(View itemView) {
            super(itemView);
            time=itemView.findViewById(R.id.time);
            date=itemView.findViewById(R.id.Date);
            status=itemView.findViewById(R.id.pending);
            swipeHere=itemView.findViewById(R.id.swipeHere);
            swipeHere.setVisibility(View.VISIBLE);
            circle_profile_picture=itemView.findViewById(R.id.circle_profile_picture);
            viewers=itemView.findViewById(R.id.viewers_number);
            doctorsname=itemView.findViewById(R.id.doctor_name);
            swipeRevealLayout=itemView.findViewById(R.id.cardView);
            tv_Delete= itemView.findViewById(R.id.tv_Delete);
            delete= itemView.findViewById(R.id.delete);
            tv_edit=itemView.findViewById(R.id.tv_edit);
            name=itemView.findViewById(R.id.name);
            image=itemView.findViewById(R.id.image);
            tv_edit.setOnClickListener(this);
            tv_Delete.setOnClickListener(this);
            delete.setOnClickListener(this);
            name.setOnClickListener(this);
            circle_profile_picture.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id=v.getId();
            if (id==R.id.tv_edit){
                Appointment appointment=  arrayList.get(getAdapterPosition());
              appointment.setStatus(Constants.ACCEPTED);
              updateAppointment(appointment,getAdapterPosition());
              swipeRevealLayout.close(true);
              swipeRevealLayout.setLockDrag(true);

            }
            else if (v.getId()==R.id.name){
                listener.onItemPictureClick(getAdapterPosition(),arrayList.get(getAdapterPosition()).getPatientId());
            }else if (v.getId()==R.id.delete){
                Appointment appointment=  arrayList.get(getAdapterPosition());
                appointment.setStatus(Constants.REJECTED);
                updateAppointment(appointment,getAdapterPosition());
                swipeRevealLayout.close(true);
                swipeRevealLayout.setLockDrag(true);            }
            else{
                Appointment appointment=  arrayList.get(getAdapterPosition());
                appointment.setStatus(Constants.REJECTED);
                updateAppointment(appointment,getAdapterPosition());
                swipeRevealLayout.close(true);
                swipeRevealLayout.setLockDrag(true);
            }
        }
    }

    private void updateAppointment(Appointment appointment,int position){
        if (Utility.isNetworkAvailable(context)){
            pd=Utility.setDialogue(context);
            pd.show();
            HashMap<String,Object> map=new HashMap<>();
            map.put("resource", appointment);
            sharedVM.getService(Constants.DreamFactory.URL)
                    .updateAppointment(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            pd.dismiss();
                            if (response.isSuccessful()){
                                if (response.code()==200){
                                    if (response.body()!=null){
                                        arrayList.get(position).setStatus(appointment.getStatus());
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Appointment "+appointment.getStatus(), Toast.LENGTH_SHORT).show();

                                    }
                                    else{
                                        Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else{
                                    Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                                {
                                    Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                            Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });


        }
        else{
            Toast.makeText(context, Constants.Messages.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }

    }
}
