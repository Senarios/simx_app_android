package com.senarios.simxx.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.HttpMethod;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.ResponseJobCandidate;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.activities.PlayYtBroadcastActivity;
import com.senarios.simxx.activities.ViewStream;
import com.senarios.simxx.adaptors.JobRequestsAdapter;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentMyJobRequestsBinding;

import java.util.ArrayList;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyJobRequests extends BaseFragment implements ApiResponse , RecyclerViewCallback {
    private FragmentMyJobRequestsBinding binding;
    private JobRequestsAdapter adapter;
    private int position=-1;

    public MyJobRequests() {
        // Required empty public constructor
    }



    @Override
    protected void init() {
        super.init();
        callApi();
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_job_requests, container, false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    private void callApi(){
            binding.view.progressBar.setVisibility(View.VISIBLE);
            binding.view.recyclerview.setVisibility(View.GONE);
            NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                            .getJobCandidates("username="+getViewModel().getLoggedUser().getUsername(),
                                    Constants.DreamFactory.USERNAME_RELATED,
                                    Constants.DreamFactory.ORDERBY),
                    this,false, ResponseJobCandidate.class,Constants.Endpoints.GET_JOB_CANDIDATES);

    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
        binding.view.recyclerview.setVisibility(View.VISIBLE);
        if(body instanceof ResponseJobCandidate) {
            if (((ResponseJobCandidate) body).getResource().size() > 0) {
                adapter=new JobRequestsAdapter(((ResponseJobCandidate) body).getResource(), requireContext(), this);
                binding.view.recyclerview.setAdapter(adapter);
            }
            else{
                adapter.reset();
            }
        }
        else if (response.raw().request().method().equalsIgnoreCase(HttpMethod.DELETE.toString())){
            adapter.getData().remove(position);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
        binding.view.recyclerview.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
        binding.view.recyclerview.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        binding.view.progressBar.setVisibility(View.GONE);
        binding.view.recyclerview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemButtonClick(int position, Object model) {
        if (model instanceof JobCandidates){
            this.position=position;
            deleteRequest(String.valueOf(((JobCandidates) model).getId()));
        }

    }

    @Override
    public void onItemClick(int position, Object model) {
        Broadcasts broadcasts=((JobCandidates) model).getBroadcasts();
        if(broadcasts != null) {
            Toast.makeText(getActivity(),"Job Removed",Toast.LENGTH_SHORT);
            if (broadcasts.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
                Intent intent = new Intent(requireContext(), ViewStream.class);
                intent.putExtra("b", broadcasts);
                startActivity(intent);
            } else {
                if (broadcasts.getVideourl() != null && broadcasts.getVideourl().startsWith("https://youtu")) {
                    Intent intent = new Intent(getActivity(), PlayYtBroadcastActivity.class);
                    intent.putExtra("ytVideolink", broadcasts.getVideourl());
                    startActivity(intent);
                }
                else if (broadcasts.isOffline()&&broadcasts.getVideourl().isEmpty()) {
                    Utility.makeFilePublic(requireContext(), null, Constants.S3Constants.OFFLINE_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
                } else {
                    Utility.makeFilePublic(requireContext(), null, Constants.S3Constants.RECORDED_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
                }
            }
        }
    }

    @Override
    public void onItemPictureClick(int position, Object model) {
        if (((JobCandidates) model).getBroadcasts() != null && ((JobCandidates) model).getBroadcasts().getUsername() != null)
            startActivity(new Intent(requireActivity(), OtherUserProfileActivity.class).putExtra(Constants.DataConstants.USER_ID, ((JobCandidates) model).getBroadcasts().getUsername()));
        else
            Toast.makeText(requireActivity(), requireActivity().getString(R.string.not_exist_post), Toast.LENGTH_SHORT).show();    }

    private void deleteRequest(String id) {
        NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                        .deleteJobRequest(id),
                this,false, Object.class, Endpoints.POST_JOB_CANDIDATES);
    }
}
