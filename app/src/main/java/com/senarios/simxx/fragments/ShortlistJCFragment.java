package com.senarios.simxx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.ResponseJobCandidate;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.activities.ShowPicCvActivity;
import com.senarios.simxx.adaptors.JobCandidatesAdaptor;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentJobCandidatesBinding;
import com.senarios.simxx.databinding.FragmentShortlistJcBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import retrofit2.Response;


public class ShortlistJCFragment extends BaseFragment implements ApiResponse , RecyclerViewCallback  {
    private FragmentShortlistJcBinding binding;
    private boolean isShortlist=false;
    public static final String SHORTILIST="shortlistCandidates";
    public static final String DATA="broadcast";
    private JobCandidatesAdaptor adaptor;
    private Broadcasts broadcast;
    private JobCandidates jobCandidate;
    FirebaseDatabase database;


    public ShortlistJCFragment() {
        // Required empty public constructor
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_shortlist_jc,container,false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        callApi();
    }


    private void callApi(){
        if (getArguments()!=null){
            binding.view.progressBar.setVisibility(View.VISIBLE);
            binding.view.recyclerview.setVisibility(View.GONE);
            isShortlist=getArguments().getBoolean(SHORTILIST);
            broadcast=(Broadcasts) getArguments().getSerializable(DATA);
            NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                            .getJobCandidates(getJobCandidatesWithShortList(broadcast.getBroadcast(),isShortlist),
                                    Constants.DreamFactory.USERNAME_RELATED,
                                    Constants.DreamFactory.ORDERBY),
                    this,false, ResponseJobCandidate.class,Constants.Endpoints.GET_JOB_CANDIDATES);
        }

    }



    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
        if (body instanceof ResponseJobCandidate){
            binding.view.recyclerview.setVisibility(View.VISIBLE);
            adaptor=new JobCandidatesAdaptor(requireContext(),((ResponseJobCandidate) body).getResource(),this);
            binding.view.recyclerview.setAdapter(adaptor);
        }
        else if (endpoint.equalsIgnoreCase(Endpoints.POST_JOB_CANDIDATES)){
            EventBus.getDefault().post(jobCandidate);
        }
    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        binding.view.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onItemOptions(int position, Object model, View container) {
        this.jobCandidate=(JobCandidates) model;

        PopupMenu popupMenu=new PopupMenu(requireContext(),container,Gravity.RIGHT);

        popupMenu.inflate(R.menu.menu_shortlist_job_candidates);

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.remove_shortlist:
                    jobCandidate.setIsshortlisted(false);
                    shortList(jobCandidate);
                    break;

                case R.id.videoCV:
                    if (((JobCandidates) model).getVideoCv() != null && ((JobCandidates) model).getVideoCv().getTitle() != null) {
                        String title = ((JobCandidates) model).getVideoCv().getTitle();
                        if (title.startsWith("Pic")) {
                            database = FirebaseDatabase.getInstance();
                            database.getReference().child("images/" + ((JobCandidates) model).getVideoCv().getVideocv())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String imagee = snapshot.getValue(String.class);
                                            Intent intent = new Intent(getActivity(), ShowPicCvActivity.class);
                                            intent.putExtra("picPathh", imagee);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else if (!title.startsWith("Pic")) {
                            if (model != null && model instanceof JobCandidates) {
                                Utility.makeFilePublic(requireContext(), null, S3Constants.VIDEO_CV_FOLDER + "/" + ((JobCandidates) model).getVideoCv().getVideocv() + OfflineStreamActivity.EXT);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Cv deleted by Job Hunter", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.share:
                    if (((JobCandidates) model).getVideoCv() != null && ((JobCandidates) model).getVideoCv().getTitle() != null) {
                        String titlee = ((JobCandidates) model).getVideoCv().getTitle();
                        if (titlee.startsWith("Pic")) {
                            database = FirebaseDatabase.getInstance();
                            database.getReference().child("images/" + ((JobCandidates) model).getVideoCv().getVideocv())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String imagee = snapshot.getValue(String.class);
                                            Utility.sharePic(requireContext(), imagee, broadcast.getJobDes());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        } else if (!titlee.startsWith("Pic")) {
                            //String link=Constants.DreamFactory.WEB_SHARE_URL + ((JobCandidates) model).getVideoCv().getVideocv();
                            String link = "https://simx.s3.us-west-2.amazonaws.com/" + S3Constants.VIDEO_CV_FOLDER + "/" + ((JobCandidates) model).getVideoCv().getVideocv() + OfflineStreamActivity.EXT;
                            Utility.share(requireContext(), link, broadcast.getJobDes());
                        }

                    } else {
                        Toast.makeText(getContext(), "Cv deleted by Job Hunter", Toast.LENGTH_SHORT).show();
                    }
//                    String link="https://simx.s3.us-west-2.amazonaws.com/"+S3Constants.VIDEO_CV_FOLDER+"/"+((JobCandidates) model).getVideoCv().getVideocv()+OfflineStreamActivity.EXT;
//                    Utility.share(requireContext(),link,broadcast.getJobDes());
//                    String link=Constants.DreamFactory.WEB_SHARE_URL + ((JobCandidates) model).getVideoCv().getVideocv();
//                    Utility.share(requireContext(),link,broadcast.getJobDes());
                    break;
            }


            return true;
        });


    }

    @Override
    public void onItemButtonClick(int position, Object model) {
        if (model!=null && model instanceof JobCandidates) {
            startActivity(new Intent(requireActivity(), OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID,((JobCandidates) model).getUser().getUsername()));
        }
    }

    private void shortList(JobCandidates jobCandidates){
        HashMap<String,Object> map=new HashMap<>();
        map.put("resource",jobCandidates);
        NetworkCall.CallAPI(requireContext(),getViewModel().getService(DreamFactory.URL).shortlistJobCandidate(map),this,false
                ,Object.class,Endpoints.POST_JOB_CANDIDATES);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void external(JobCandidates jobCandidate){
        if (adaptor.getData().contains(jobCandidate)){
            adaptor.getData().remove(jobCandidate);
            adaptor.notifyDataSetChanged();
        }
        else{
            adaptor.getData().add(jobCandidate);
            adaptor.notifyDataSetChanged();
        }
    }

}
