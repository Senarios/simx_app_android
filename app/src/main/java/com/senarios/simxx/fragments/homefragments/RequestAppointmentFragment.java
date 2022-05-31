package com.senarios.simxx.fragments.homefragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.model.QBEvent;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.databinding.RequestAppointmentBinding;
import com.hdev.common.datamodels.Appointment;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.fragments.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RequestAppointmentFragment extends BaseFragment implements Observer<Users>, View.OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, QBEntityCallback<QBEvent>, AdapterView.OnItemSelectedListener {
    private RequestAppointmentBinding binding;
    private Users user;
    private String date;
    private String time, duration;
    private Float cost;
    private DatePickerDialog datePickerDialog;


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.request_appointment, container, false);
        binding = DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        binding.dateTextview.setOnClickListener(this);
//        binding.timeTextview.setOnClickListener(this);
        binding.tenMin.setOnClickListener(this);
        binding.twentyMin.setOnClickListener(this);
        binding.thirtyMin.setOnClickListener(this);
        binding.fourtyMin.setOnClickListener(this);
        binding.fiftyMin.setOnClickListener(this);
        binding.sixtyMin.setOnClickListener(this);
        binding.eightAM.setOnClickListener(this);
        binding.tenAM.setOnClickListener(this);
        binding.twoPM.setOnClickListener(this);
        binding.twelvePM.setOnClickListener(this);
        binding.fourPM.setOnClickListener(this);
        binding.sixPM.setOnClickListener(this);
        binding.back.setOnClickListener(this);
//        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                requireActivity().finish();
//            }
//        });
        getViewModel().getOpponent_user().observe(this, this);

        binding.requestAppointmentBtn.setOnClickListener(this);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        date = df.format(c);
        binding.dateTextview.setText(date);

//        binding.spinner.setOnItemSelectedListener(this);
    }

    private void selectedTime(int time) {
        if (time == 6) {
            this.time = "18:00";
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.sixPM, R.color.white);
            setTextViewDrawableColor(binding.fourPM, R.color.black);
            setTextViewDrawableColor(binding.twoPM, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);
            setTextViewDrawableColor(binding.tenAM, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);

        } else if (time == 4) {
            this.time = "16:00";
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.fourPM, R.color.white);
            setTextViewDrawableColor(binding.sixPM, R.color.black);
            setTextViewDrawableColor(binding.twoPM, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);
            setTextViewDrawableColor(binding.tenAM, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);

        } else if (time == 2) {
            this.time = "14:00";
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.twoPM, R.color.white);
            setTextViewDrawableColor(binding.sixPM, R.color.black);
            setTextViewDrawableColor(binding.fourPM, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);
            setTextViewDrawableColor(binding.tenAM, R.color.black);
            setTextViewDrawableColor(binding.eightAM, R.color.black);

        } else if (time == 12) {
            this.time = "12:00";
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.twelvePM, R.color.white);
            setTextViewDrawableColor(binding.sixPM, R.color.black);
            setTextViewDrawableColor(binding.twoPM, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.tenAM, R.color.black);
            setTextViewDrawableColor(binding.eightAM, R.color.black);

        } else if (time == 10) {
            this.time = "10:00";
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.tenAM, R.color.white);
            setTextViewDrawableColor(binding.sixPM, R.color.black);
            setTextViewDrawableColor(binding.twoPM, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);
            setTextViewDrawableColor(binding.eightAM, R.color.black);

        } else if (time == 8) {
            this.time = "8:00";
            binding.eightAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.tenAM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twelvePM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.twoPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixPM.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.eightAM.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenAM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twelvePM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twoPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourPM.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixPM.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.eightAM, R.color.white);
            setTextViewDrawableColor(binding.sixPM, R.color.black);
            setTextViewDrawableColor(binding.twoPM, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.twelvePM, R.color.black);
            setTextViewDrawableColor(binding.tenAM, R.color.black);

        }
    }

    private void selectedDuration(int duration) {
        if (duration == 10) {
            this.duration = "10";
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.tenMin, R.color.white);
            setTextViewDrawableColor(binding.twentyMin, R.color.black);
            setTextViewDrawableColor(binding.thirtyMin, R.color.black);
            setTextViewDrawableColor(binding.fourtyMin, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.sixtyMin, R.color.black);

        } else if (duration == 20) {
            this.duration = "20";
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.twentyMin, R.color.white);
            setTextViewDrawableColor(binding.tenMin, R.color.black);
            setTextViewDrawableColor(binding.thirtyMin, R.color.black);
            setTextViewDrawableColor(binding.fourtyMin, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.sixtyMin, R.color.black);

        } else if (duration == 30) {
            this.duration = "30";
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.thirtyMin, R.color.white);
            setTextViewDrawableColor(binding.twentyMin, R.color.black);
            setTextViewDrawableColor(binding.tenMin, R.color.black);
            setTextViewDrawableColor(binding.fourtyMin, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.sixtyMin, R.color.black);

        } else if (duration == 40) {
            this.duration = "40";
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.fourtyMin, R.color.white);
            setTextViewDrawableColor(binding.twentyMin, R.color.black);
            setTextViewDrawableColor(binding.tenMin, R.color.black);
            setTextViewDrawableColor(binding.thirtyMin, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.sixtyMin, R.color.black);

        } else if (duration == 50) {
            this.duration = "50";
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.fiftyMin, R.color.white);
            setTextViewDrawableColor(binding.twentyMin, R.color.black);
            setTextViewDrawableColor(binding.tenMin, R.color.black);
            setTextViewDrawableColor(binding.fourtyMin, R.color.black);
            setTextViewDrawableColor(binding.thirtyMin, R.color.black);
            setTextViewDrawableColor(binding.sixtyMin, R.color.black);

        } else if (duration == 60) {
            this.duration = "60";
            binding.sixtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_slected));
            binding.twentyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.thirtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fourtyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.fiftyMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));
            binding.tenMin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_backgroud_white));

            binding.sixtyMin.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.tenMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.twentyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.thirtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fourtyMin.setTextColor(getContext().getResources().getColor(R.color.black));
            binding.fiftyMin.setTextColor(getContext().getResources().getColor(R.color.black));

            setTextViewDrawableColor(binding.sixtyMin, R.color.white);
            setTextViewDrawableColor(binding.twentyMin, R.color.black);
            setTextViewDrawableColor(binding.tenMin, R.color.black);
            setTextViewDrawableColor(binding.fourtyMin, R.color.black);
            setTextViewDrawableColor(binding.fiftyMin, R.color.black);
            setTextViewDrawableColor(binding.thirtyMin, R.color.black);
        }
    }


    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void postAppointment() {
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        if (!date.equals(formattedDate)) {
            if (Utility.isNetworkAvailable(requireContext())) {
                getDialog().show();
                Appointment appointment = new Appointment();
                appointment.setTime(time);
                appointment.setDate(date);
                appointment.setMessage("");
                appointment.setStatus("pending");
                appointment.setPatientId(getViewModel().getLoggedUser().getUsername());
                appointment.setPatientName(getViewModel().getLoggedUser().getName());
                appointment.setPatientQbId(getViewModel().getLoggedUser().getQbid());
                appointment.setDoctorId(user.getUsername());
                appointment.setDoctorName(user.getName());
                appointment.setDoctorQbId(user.getQbid());
                HashMap<String, Object> map = new HashMap<>();
                map.put("resource", appointment);
                getViewModel().getService(Constants.DreamFactory.URL).postAppointment(map)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Response<JsonObject>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Response<JsonObject> response) {
                                getDialog().dismiss();
                                if (response.isSuccessful()) {
                                    if (response.code() == 200) {
                                        if (response.body() != null) {
                                            Toast.makeText(requireContext(), "Appointment Successfull", Toast.LENGTH_SHORT).show();
                                            JSONObject object = new JSONObject();
                                            try {
                                                object.put(NotificationKeys.User.toString(), getViewModel().getLoggedUser().toString());
                                                object.put(NotificationKeys.message.toString(), "SimpleData");
                                                object.put(NotificationKeys.Type.toString(), NotificationType.Appointment);
                                                Utility.sendNotification(false, Integer.parseInt(user.getQbid()), object, RequestAppointmentFragment.this);
                                            } catch (JSONException e) {
                                                Utility.showELog(e);
                                            }
                                            requireActivity().finish();
                                        }


                                    } else {

                                        Toast.makeText(getContext(), "Something ..", Toast.LENGTH_SHORT).show();
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went ..", Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                getDialog().dismiss();
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                        });


            } else {
                Toast.makeText(getContext(), "Please enable wifi/data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Unable to set appointment today", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onChanged(Users users) {
        this.user = users;
        binding.broadcastFeeHour.setText("£" + user.getRate() + "/hour");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getViewModel().getOpponent_user().removeObservers(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.date_textview) {
            datePickerDialog = new DatePickerDialog(getContext(), this, Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        } else if (id == R.id.time_textview) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this, Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE), false);
            timePickerDialog.show();
        } else if (id == R.id.eight_AM) {
            selectedTime(8);
        } else if (id == R.id.ten_AM) {
            selectedTime(10);
        } else if (id == R.id.twelve_PM) {
            selectedTime(12);
        } else if (id == R.id.two_PM) {
            selectedTime(2);
        } else if (id == R.id.four_PM) {
            selectedTime(4);
        } else if (id == R.id.six_PM) {
            selectedTime(6);
        } else if (id == R.id.ten_min) {
            selectedDuration(10);
            setRates(10);
        } else if (id == R.id.twenty_min) {
            selectedDuration(20);
            setRates(20);
        } else if (id == R.id.thirty_min) {
            selectedDuration(30);
            setRates(30);
        } else if (id == R.id.fourty_min) {
            selectedDuration(40);
            setRates(40);
        } else if (id == R.id.fifty_min) {
            selectedDuration(50);
            setRates(50);
        } else if (id == R.id.sixty_min) {
            selectedDuration(60);
            setRates(60);
        } else if (id == R.id.request_appointment_btn) {
            if (date == null) {
                binding.dateTextview.setError("Select Date");
            } else if (time == null) {
                binding.timeTextview.setError("Select Time");
            } else if (duration == null) {
                binding.durationTv.setError("Select duration");
            } else {
                postAppointment();
            }

        } else if (id == R.id.back) {
            getActivity().onBackPressed();
        }
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = (month + 1) + "/" + dayOfMonth + "/" + year;
        binding.dateTextview.setText(date);
        binding.dateTextview.setError(null);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        time = hourOfDay + ":" + minute;
        binding.timeTextview.setText(time);
        binding.timeTextview.setError(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
//            cost = (Float.valueOf(binding.spinner.getSelectedItem().toString().split(" ")[0]) / 60) * Integer.valueOf(user.getRate());
            binding.costEstimateRate.setText("" + cost);
        } catch (NumberFormatException e) {

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSuccess(QBEvent qbEvent, Bundle bundle) {
        Utility.showLog(qbEvent.toString());
    }

    @Override
    public void onError(QBResponseException e) {
        Utility.showELog(e);
    }

    private void setRates(int duration) {
        try {
            Float rate= Float.valueOf(user.getRate());
            Float costOfOneMin = rate/60;
            cost = (costOfOneMin * duration);
            binding.costEstimateRate.setText("£"+String.format("%.1f", cost));
        } catch (NumberFormatException e) {

        }
    }
}
