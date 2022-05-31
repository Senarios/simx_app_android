package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.senarios.simxx.DateUtils;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.hdev.common.datamodels.Appointment;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class OtherAppointmentsAdapter extends RecyclerView.Adapter<OtherAppointmentsAdapter.ViewHolder> {
    private List<Appointment> arrayList;
    private Context context;
    private SharedVM sharedVM;
    private ProgressDialog pd;
    private RecyclerViewCallback listener;

    public OtherAppointmentsAdapter(List<Appointment> arrayList, Context context, SharedVM sharedVM, RecyclerViewCallback listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.sharedVM = sharedVM;
        this.listener = listener;
        pd=Utility.setDialogue(context);
    }




    @NonNull
    @Override
    public OtherAppointmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_appointments,parent,false);
        return new ViewHolder(view);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OtherAppointmentsAdapter.ViewHolder holder, int position) {
        holder.time.setText(arrayList.get(position).getTime());
        holder.date.setText(arrayList.get(position).getDate());
        holder.status.setText(arrayList.get(position).getStatus());
        if (holder.status.getText().toString().trim().equalsIgnoreCase("pending")){
            holder.status.setBackgroundColor(context.getResources().getColor( R.color.color_grey));
            holder.status.setTextColor(context.getResources().getColor(R.color.black));
        }else if (holder.status.getText().toString().trim().equalsIgnoreCase("accepted")){
            holder.status.setBackgroundColor(context.getResources().getColor( R.color.Green));
            holder.status.setTextColor(context.getResources().getColor(R.color.white));
        }else if (holder.status.getText().toString().trim().equalsIgnoreCase("rejected")){
            holder.status.setBackgroundColor(context.getResources().getColor( R.color.red));
            holder.status.setTextColor(context.getResources().getColor(R.color.white));
        }
        holder.name.setText(arrayList.get(position).getDoctorName());



        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getDoctorId()+".png")
                .into(holder.image);

            holder.viewers.setText(""+30); //+30

        if (arrayList.get(position).getStatus().equalsIgnoreCase("accepted")){
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.Green));

        }
        else if (arrayList.get(position).getStatus().equalsIgnoreCase("rejected")) {
            holder.image.setBackgroundColor(context.getResources().getColor(R.color.Red));
        }
        Date date=new Date(arrayList.get(position).getDate());
        boolean d=DateUtils.isAfterDay(new Date(),date );
        Utility.showLog(""+new Date());
        if (d || arrayList.get(position).getStatus().equalsIgnoreCase("rejected")){
            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setOnClickListener(v -> deleteAppointment(arrayList.get(position),position));
        }

    }

    private void deleteAppointment(Appointment appointment,int position) {
        if (Utility.isNetworkAvailable(context)){
            pd.show();
            sharedVM.getService(Constants.DreamFactory.URL)
                    //.deleteAppointment("(patientId = "+appointment.getPatientId()+") and (doctorId = "+appointment.getDoctorId()+")")
                    .deleteAppointment(appointment.getId())
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
                                       // notifyItemRemoved(position);
                                        arrayList.remove(position);
                                        Toast.makeText(context, "Appointment Deleted!", Toast.LENGTH_SHORT).show();

                                    }
                                    else{
                                        Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();

                                    }

                                }
                                else{
                                    Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();
                                }

                            }
                            else{
                                Toast.makeText(context, Constants.Messages.ERROR, Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                        }
                    });


        }
        else{
            Toast.makeText(context, Constants.Messages.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView time;
        private TextView date;
        private TextView status;
        private CircleImageView circle_profile_picture;
        private TextView viewers;
        private TextView doctorsname;
        private TextView name , swipeHere;
        private ImageView image,delete;



        private ViewHolder(View itemView) {
            super(itemView);
            time=itemView.findViewById(R.id.time);
            date=itemView.findViewById(R.id.Date);
            status=itemView.findViewById(R.id.pending);
            circle_profile_picture=itemView.findViewById(R.id.circle_profile_picture);
            viewers=itemView.findViewById(R.id.viewers_number);
            image=itemView.findViewById(R.id.image);
            doctorsname=itemView.findViewById(R.id.doctor_name);
            name=itemView.findViewById(R.id.name);
            delete=itemView.findViewById(R.id.delete);
            circle_profile_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemPictureClick(getAdapterPosition(),arrayList.get(getAdapterPosition()).getDoctorId());
                }
            });



        }
    }
}
