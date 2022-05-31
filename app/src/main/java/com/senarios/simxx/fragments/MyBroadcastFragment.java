package com.senarios.simxx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.ResponseBroadcast;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.adaptors.MyBroadcastsAdapter;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.FragmentMyBroadcastBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyBroadcastFragment extends BaseFragment implements ApiResponse, RecyclerViewCallback {
    private FragmentMyBroadcastBinding binding;
    private Broadcasts broadcast;
    private int position = -1;
    private MyBroadcastsAdapter adapter;

    public MyBroadcastFragment() {
        // Required empty public constructor
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_broadcast, container, false);
        binding = DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        getBroadcasts();
    }

    private void getBroadcasts() {
        binding.view.progressBar.setVisibility(View.VISIBLE);
        binding.view.recyclerview.setVisibility(View.GONE);
        String filter = "(username=" + getViewModel().getLoggedUser().getUsername() + ")";
        NetworkCall.CallAPI(requireContext(), Utility.getService(Constants.DreamFactory.URL)
                        .getBroadcasts(filter, Constants.DreamFactory.ORDERBY, DreamFactory.BROADCAST_RELATED), this,
                false, ResponseBroadcast.class, Endpoints.BROADCASTS);
    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        binding.view.progressBar.setVisibility(View.GONE);
        binding.view.recyclerview.setVisibility(View.VISIBLE);
        if (body instanceof ResponseBroadcast) {
            EventBus.getDefault().post(((ResponseBroadcast) body).getResource());
            adapter = new MyBroadcastsAdapter((ArrayList<Broadcasts>) ((ResponseBroadcast) body).getResource(), requireContext(), this);
            binding.view.recyclerview.setAdapter(adapter);
        } else if (endpoint.equalsIgnoreCase(Endpoints.DELETE_BROADCAST)) {
            if (broadcast != null) {
                if (broadcast.isjob()) {
                    if (broadcast.getJobCandidates().size() > 0) {
                        deleteJobRequests(broadcast);
                    }
                }

                adapter.getData().remove(position);
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post((List<Broadcasts>) adapter.getData());
                if (broadcast.isOffline()) {
                    Utility.deleteFileFromS3(requireContext(), S3Constants.OFFLINE_VIDEO_FOLDER + "/" + broadcast.getBroadcast() + OfflineStreamActivity.EXT);
                } else {
                    Utility.deleteFileFromS3(requireContext(), S3Constants.RECORDED_VIDEO_FOLDER + "/" + broadcast.getBroadcast() + OfflineStreamActivity.EXT);
                }
            }
        } else if (endpoint.equalsIgnoreCase(Endpoints.TAGS)) {
            if (response.code() == 200) {
                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
            }
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
    public void onItemDelete(int position, Object model) {
        CommonUtils.getAlertDialoge(requireContext(), "User Action Required", "Do you want to delete this live Stream?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    if (model instanceof Broadcasts) {
                        broadcast = (Broadcasts) model;
                        this.position = position;
                        deleteBroadcast(broadcast);
                        deleteTags(broadcast);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                }).show();

    }

    private void deleteBroadcast(Broadcasts broadcast) {
        NetworkCall.CallAPI(requireContext(), Utility.getService(DreamFactory.URL)
                        .deleteBroadcast(broadcast.getId())
                , this, false, Object.class, Endpoints.DELETE_BROADCAST);
    }

    private void deleteTags(Broadcasts broadcast) {
        NetworkCall.CallAPI(requireContext(), Utility.getService(DreamFactory.URL)
                        .deleteTagsByFilter("broadcast=" + broadcast.getBroadcast())
                , this, false, Object.class, Endpoints.TAGS);
    }

    private void deleteJobRequests(Broadcasts broadcast) {
        NetworkCall.CallAPI(requireContext(), Utility.getService(DreamFactory.URL)
                        .deleteJobRequestbyfilter("broadcast=" + broadcast.getBroadcast())
                , this, false, Object.class, Endpoints.POST_JOB_CANDIDATES);
    }
}
