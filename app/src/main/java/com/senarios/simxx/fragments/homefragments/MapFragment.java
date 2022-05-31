package com.senarios.simxx.fragments.homefragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.PlayYtBroadcastActivity;
import com.senarios.simxx.activities.ViewStream;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.ResponseBroadcast;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback , EditText.OnEditorActionListener, GoogleMap.OnMarkerClickListener {
   private GoogleMap googleMap;
   private SharedVM sharedVM;
   private ImageView back;
    private EditText search;
    private HomeContainerCallback callback;
    private ProgressDialog pd;
    private List<Broadcasts> temp_broadcasts=new ArrayList<>();
    private Bitmap online;
    private Bitmap offline;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sharedVM= ViewModelProviders.of(requireActivity()).get(SharedVM.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //
        back=view.findViewById(R.id.back);

        search=view.findViewById(R.id.searchView);



        init();


        //
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }





        return view;
    }

    private void init() {
        BitmapDrawable on=(BitmapDrawable)getResources().getDrawable(R.drawable.map_online);
        Bitmap o=on.getBitmap();
        online = Bitmap.createScaledBitmap(o, 75, 90, false);
        BitmapDrawable of=(BitmapDrawable)getResources().getDrawable(R.drawable.map_offline);
        Bitmap f=of.getBitmap();
        offline = Bitmap.createScaledBitmap(f, 75, 90, false);

        callback=(HomeContainerCallback)getParentFragment();

        search.setOnEditorActionListener(this);
        pd=Utility.setDialogue(getContext());
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i2==0){
                   /* sharedVM.getBroadcsats().observe(getViewLifecycleOwner(), broadcasts -> {
                        if (googleMap!=null){
                            if (broadcasts!=null && broadcasts.size()>0) {
                                temp_broadcasts.clear();
                                temp_broadcasts=broadcasts;
                                googleMap.clear();
                                getMarkerAndMove(broadcasts);
                            }
                        }


                    });*/
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        back.setOnClickListener(view -> {
//            callback.OnChange(new BroadcastsFragment(), FragmentTags.BROADCAST)
            if (getParentFragment()!=null) {
                getParentFragment().getChildFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)  {
        this.googleMap=googleMap;
        sharedVM.getBroadcsats().observe(getViewLifecycleOwner(), broadcasts -> {
            if (broadcasts!=null && broadcasts.size()>0) {
                temp_broadcasts=broadcasts;
                getMarkerAndMove(broadcasts);
            }
        });

        this.googleMap.setOnMarkerClickListener(this);

    }

    private void getSearch(String q){
        if (Utility.isNetworkAvailable(requireContext())){
            pd.show();
            sharedVM.getService(Constants.DEFAULT_URL).getSearch(q)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBroadcast>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseBroadcast> response) {

                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body()!=null){
                                        googleMap.clear();

                                        //response.body().getResource().size()>0
                                        if (response.body().getResource() != null ) {
                                            temp_broadcasts.clear();
                                            temp_broadcasts=response.body().getResource();
                                            getMarkerAndMove(response.body().getResource());
                                        }
                                        else {
                                            pd.dismiss();
                                            Toast.makeText(getContext(), "No Result", Toast.LENGTH_SHORT).show();
                                            search.setText("");
                                           onMapReady(googleMap);
                                        }


                                    }
                                    else{
                                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }



                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }
                            pd.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                        }
                    });


        }
        else {
            Toast.makeText(getContext(), "Please turn on wifi/data", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLngBounds getBounds(List<Broadcasts> broadcasts){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Broadcasts marker : broadcasts) {
            builder.include(new LatLng(Double.valueOf(marker.getLatti()), Double.valueOf(marker.getLongi())));
        }
            return builder.build();
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!textView.getText().toString().isEmpty()) {
                getSearch(textView.getText().toString().trim());
            }
        }
        return false;
    }

    private void getMarkerAndMove(List<Broadcasts> broadcasts){

     //
        for (int i=0;i<broadcasts.size();i++){

            if (broadcasts.get(i).getStatus()!=null && broadcasts.get(i).getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)){

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.valueOf(broadcasts.get(i).getLatti()), Double.valueOf(broadcasts.get(i).getLongi())))
                            .icon(BitmapDescriptorFactory.fromBitmap(online))
//                            .snippet(broadcasts.get(i).getBroadcast())
                            .title(broadcasts.get(i).getTitle())
                            .alpha(0.7f)

                    ).setTag(broadcasts.get(i).getImglink());

            }
            else{
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(broadcasts.get(i).getLatti()), Double.valueOf(broadcasts.get(i).getLongi())))
                        .icon(BitmapDescriptorFactory.fromBitmap(offline))
//                        .snippet(broadcasts.get(i).getBroadcast())
                        .title(broadcasts.get(i).getTitle())
                        .alpha(0.7f)

                ).setTag(broadcasts.get(i).getImglink());
            }

        }
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getBounds(broadcasts), 0);
        googleMap.animateCamera(cu);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for (Broadcasts broadcast :temp_broadcasts){
            if (broadcast.getTitle().equalsIgnoreCase(marker.getTitle())&&broadcast.getImglink().equalsIgnoreCase(marker.getTag().toString())) {
                if (broadcast.isOffline()){
                    Utility.makeFilePublic(requireContext(),broadcast ,Constants.S3Constants.OFFLINE_VIDEO_FOLDER+"/"+broadcast.getBroadcast()+OfflineStreamActivity.EXT);
                }
                else if (broadcast.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
                    sharedVM.setBroadcast(broadcast);
                    Intent intent=new Intent(getActivity(), ViewStream.class);
                    intent.putExtra("b", broadcast);
                    startActivity(intent);
                }
                else {
                    Utility.makeFilePublic(requireContext(), broadcast,Constants.S3Constants.RECORDED_VIDEO_FOLDER+"/"+broadcast.getBroadcast()+OfflineStreamActivity.EXT);
                }
            }
        }
        return false;
    }
}
