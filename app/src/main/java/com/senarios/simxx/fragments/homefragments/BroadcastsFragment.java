package com.senarios.simxx.fragments.homefragments;


import static android.content.Context.MODE_PRIVATE;
import static com.senarios.simxx.Utility.deleteAllfiles;
import static com.senarios.simxx.Utility.getService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.NetworkModel;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.hdev.common.datamodels.ResponseBroadcast;
import com.hdev.common.datamodels.S3UploadRequest;
import com.hdev.common.datamodels.Tags;
import com.hdev.common.datamodels.Users;
import com.hdev.common.datamodels.VideoCv;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.model.QBEvent;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.Info;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MyVideoCVActivity;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.OtherUserProfileActivity;
import com.senarios.simxx.activities.PlayYtBroadcastActivity;
import com.senarios.simxx.activities.ViewStream;
import com.senarios.simxx.adaptors.LiveStreamsAdapter;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.LiveStreamsFragmentBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.services.AmazonS3UploadService;
import com.senarios.simxx.services.ChatLoginService;
import com.senarios.simxx.viewmodels.SharedVM;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class BroadcastsFragment extends BaseFragment implements View.OnTouchListener, QBEntityCallback<QBEvent>, View.OnClickListener, TextWatcher, RecyclerViewCallback, ApiResponse, SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener {
    private LiveStreamsFragmentBinding binding;
    static LiveStreamsAdapter adapter;
    private Broadcasts broadcast;
    private SharedVM sharedVM;
    private ArrayList<Broadcasts> broadcasts = new ArrayList<>();
    private final String MEDIA_PICKER = Environment.getExternalStorageDirectory() + "/mediapicker";
    private final String MEDIA_PICKER_VIDEOS = "/videos";
    private static final int CODE = 772;
    private TextView textView2;
    private ProgressDialog pd;
    private Boolean canApply;


    public BroadcastsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @SuppressLint({"RestrictedApi", "UseRequireInsteadOfGet"})
    @Override
    protected void init() {
        super.init();

        if (getParentFragment() != null) {
            ((HomeFragment) getParentFragment()).nav_view.getMenu().getItem(1).setChecked(true);
        }

        SharedPreferences.Editor editor = getContext().getSharedPreferences("hunter", MODE_PRIVATE).edit();
        editor.putString("jobhunter1", getViewModel().getLoggedUser().getSkills());
        editor.putString("username1", getViewModel().getLoggedUser().getUsername());
        editor.apply();

        binding.rvLiveStreams.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (Utility.isLastItemDisplaying(binding.rvLiveStreams)) {
                    if (binding.searchView.getText().toString().isEmpty()) {
                        getBroadcasts(broadcasts.size());
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        binding.swipe.setOnRefreshListener(this);
        if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equals("Recruiter")) {
            binding.floatingActionButton.setVisibility(View.VISIBLE);
            canApply = false;
        } else if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equals("Job hunter")) {
            binding.floatingActionButton.setVisibility(View.GONE);
            canApply = true;
        }

        //map image view
        binding.mapImageView.setOnClickListener(this);

        binding.searchView.addTextChangedListener(this);
        binding.searchView.setOnEditorActionListener(this);
        binding.searchView.setOnTouchListener(this);

        //fab
        binding.floatingActionButton.setOnClickListener(this);


        adapter = new LiveStreamsAdapter(getContext(), BroadcastsFragment.this, getViewModel(), canApply);
        binding.rvLiveStreams.setAdapter(adapter);
        adapter.setData(broadcasts);
        sharedVM = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SharedVM.class);
        pd = Utility.setDialogue(getContext());


    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.live_streams_fragment, container, false);
        binding = DataBindingUtil.bind(view);
        init();
        return view;
    }


    private void initPreviousVideoTask() {
        try {
            SharedPreferences preferences = getContext().getSharedPreferences("myy", MODE_PRIVATE);
            String post_check = preferences.getString("job_post_popup", "abc");
            if (post_check.startsWith("pop")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Thank you!");
                alertDialog.setMessage("Your job is posted to Administrator and will show on wall as soon as it is approved.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
            if (!AmazonS3UploadService.checkRunning()) {
                if ((boolean) getViewModel().getPreferences(Constants.SharedPreference.ISUPLOADING, false)) {
                    handlePreviousUpload();

                } else {
                    deleteAllfiles(requireActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath());
                    Utility.deleteAllfiles(MEDIA_PICKER + MEDIA_PICKER_VIDEOS);
                    if (textView2 == null || textView2.getText().toString().trim().isEmpty())
                        getBroadcasts(0);
                }
            } else {
                if (!(boolean) getViewModel().getPreferences(Constants.SharedPreference.ISUPLOADING, false)) {
                    deleteAllfiles(requireActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath());
                    Utility.deleteAllfiles(MEDIA_PICKER + MEDIA_PICKER_VIDEOS);
                    if (textView2 == null || textView2.getText().toString().trim().isEmpty())
                        getBroadcasts(0);
                } else {
                    if (textView2 == null || textView2.getText().toString().trim().isEmpty())
                        getBroadcasts(0);
                }
            }
        } catch (Exception e) {
            Utility.showELog(e);
        }
    }

    private void handlePreviousUpload() {
        S3UploadRequest request = new Gson().fromJson(Utility.getPreference(requireContext()).getString(S3_REQUEST, ""), S3UploadRequest.class);
        switch (request.getAction()) {
            case BROADCAST:
                Utility.getAlertDialoge(requireContext(), "Pending Upload", request.getMessage())
                        .setPositiveButton("Yes", (dialog, which) -> {
                            request.setKey(getViewModel().getLoggedUser().getUsername() + System.currentTimeMillis());
                            request.getBroadcast().setBroadcast(request.getKey());
                            Intent intent = new Intent(requireContext(), AmazonS3UploadService.class);
                            intent.putExtra(S3_REQUEST, request);
                            requireContext().startService(intent);
                            getBroadcasts(0);

                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            Utility.getSharedPreference(requireContext()).edit().putBoolean(SharedPreference.ISUPLOADING, false).apply();
                            Utility.getSharedPreference(requireContext()).edit().remove(S3_REQUEST).apply();
                            getBroadcasts(0);
                        })
                        .show();
                break;

            case VIDEOCV:
                Intent intent = new Intent(requireContext(), AmazonS3UploadService.class);
                intent.putExtra(S3_REQUEST, request);
                requireContext().startService(intent);
                getBroadcasts(0);
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //service for again call
        Intent login = new Intent(getActivity(), ChatLoginService.class);
        login.putExtra(QB_USER_LOGIN, getViewModel().getSharedPreference().getString(Email, ""));
        login.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
        login.putExtra(QB_FULL_NAME, getViewModel().getSharedPreference().getString(Fullname, ""));
        login.putExtra(QB_ID, getViewModel().getSharedPreference().getString(QUICKB_ID,""));
//        login.putExtra(QB_ID, getViewModel().getLoggedUser().getQbid());
        requireContext().startService(login);
        initPreviousVideoTask();

    }

    private void getBroadcasts(int offset) {
        if (Utility.isNetworkAvailable(requireContext())) {
            binding.swipe.setRefreshing(true);
            getViewModel().getService(Constants.DreamFactory.URL)
                    .getBroadcasts(BROADCAST_RELATED, offset, 10, Constants.DreamFactory.ORDERBY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBroadcast>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseBroadcast> response) {
                            binding.swipe.setRefreshing(false);
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        response.body().setResource(getBroadCastsList(response.body().getResource()));
                                        binding.noRecTV.setVisibility(View.GONE);
//                                        if (offset == 0) {
//                                            broadcasts.clear();
//                                        }
                                        if (offset == 0) {
                                            broadcasts.clear();
                                            List<Broadcasts> brdcstlist = new ArrayList<>();
                                            for (Broadcasts child : response.body().getResource()) {
                                                if (child.getJobPostStatus().equalsIgnoreCase("Approved"))
                                                    brdcstlist.add(child);
                                            }
                                            broadcasts.addAll(brdcstlist);
                                        } else {
                                            broadcasts.addAll(addBroadCasts(response.body().getResource()));
                                        }
//                                        broadcasts.addAll(offset == 0 ? response.body().getResource() : addBroadCasts(response.body().getResource()));
                                        adapter.setData(broadcasts);


                                    } else {
                                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                binding.swipe.setRefreshing(false);
                            }

                        }
                    });

        } else {
            Toast.makeText(getContext(), "Please Enable Wifi/Data", Toast.LENGTH_SHORT).show();
        }


    }

    private List<Broadcasts> addBroadCasts(List<Broadcasts> resource) {
        List<Broadcasts> brdcstlist = new ArrayList<>();
        for (Broadcasts child : resource) {
            if (!broadcasts.contains(child) && child.getJobPostStatus().equalsIgnoreCase("Approved"))
                brdcstlist.add(child);
        }
        return brdcstlist;
    }

/*    private List<Broadcasts> addAllBroadCasts(List<Broadcasts> resource) {
        List<Broadcasts> brdcstlist = new ArrayList<>();
        for (Broadcasts child : resource) {
            if (!allBroadcasts.contains(child))
                brdcstlist.add(child);
        }
        return brdcstlist;
    }*/

    private List<Broadcasts> getBroadCastsList(List<Broadcasts> resource) {
        List<Broadcasts> broadcastList = new ArrayList<>();

        for (Broadcasts child : resource) {
            if (!getViewModel().compareID(child.getUsername()))
                broadcastList.add(child);

        }
        return broadcastList;
    }

    private void getSearch(String q) {
        if (Utility.isNetworkAvailable(requireContext())) {
            binding.swipe.setRefreshing(true);
            getViewModel().getService(Constants.DEFAULT_URL).getSearch(q)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBroadcast>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<ResponseBroadcast> responseBroadcastResponse) {
                            binding.swipe.setRefreshing(false);
                            if (responseBroadcastResponse.isSuccessful()) {
                                if (responseBroadcastResponse.code() == 200) {
                                    if (responseBroadcastResponse.body() != null) {
                                        if (responseBroadcastResponse.body().getResource() != null) {
                                            responseBroadcastResponse.body().setResource(getBroadCastsList(responseBroadcastResponse.body().getResource()));
                                            List<Broadcasts> brdcstlist = new ArrayList<>();
                                            for (Broadcasts child : responseBroadcastResponse.body().getResource()) {
                                                if (child.getJobPostStatus().equalsIgnoreCase("Approved")) {
                                                    brdcstlist.add(child);
                                                    broadcasts.clear();
                                                    binding.noRecTV.setVisibility(View.GONE);
                                                    broadcasts.addAll(brdcstlist);
                                                } else if (!child.getJobPostStatus().equalsIgnoreCase("Approved")) {
                                                    broadcasts.clear();
                                                    binding.noRecTV.setVisibility(View.VISIBLE);
                                                }
                                            }
//                                        broadcasts.addAll(responseBroadcastResponse.body().getResource());
                                            adapter.setData(broadcasts);
                                        } else {
                                            broadcasts.clear();
                                            adapter.setData(broadcasts);
                                            binding.noRecTV.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                    }


                                } else {

                                    Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                                }


                            } else {

                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.swipe.setRefreshing(false);
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                        }
                    });


        } else {
            Toast.makeText(getContext(), "Please turn on wifi/data", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!textView.getText().toString().isEmpty()) {
                textView2 = textView;
                getSearch(textView.getText().toString().trim());
            }
        }
        return false;
    }

    @Override
    public void onItemPictureClick(int position, Object model) {
        Broadcasts broadcast = (Broadcasts) model;

        if (broadcast.getUsername().equalsIgnoreCase(getViewModel().getLoggedUser().getUsername())) {
            getHomeContainer().OnChange(new ProfileFragment(), FragmentTags.PROFILE);
        } else {
            SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", MODE_PRIVATE).edit();
            if (broadcast.isMessageonly()) {
                editor.putString(SharedPreference.CALL_MSG, "msg");
                editor.apply();
            } else if (broadcast.isCallonly()) {
                editor.putString(SharedPreference.CALL_MSG, "call");
                editor.apply();
            } else if (broadcast.isBothmsgcall()) {
                editor.putString(SharedPreference.CALL_MSG, "both");
                editor.apply();
            }
            startActivity(new Intent(requireContext(), OtherUserProfileActivity.class).putExtra(DataConstants.USER_ID, broadcast.getUsername()));
        }
    }


    @Override
    public void onItemClick(int position, Object model) {
        if (model instanceof Broadcasts) {
            Broadcasts broadcasts = (Broadcasts) model;
            if (broadcasts.getVideourl() != null && broadcasts.getVideourl().startsWith("http")) {
                Intent intent = new Intent(getActivity(), PlayYtBroadcastActivity.class);
                intent.putExtra("ytVideolink", broadcasts.getVideourl());
                startActivity(intent);
            } else if (broadcasts.isOffline()) {
                Utility.makeFilePublic(requireContext(), null, Constants.S3Constants.OFFLINE_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
            } else if (broadcasts.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
                getViewModel().setBroadcast(broadcasts);
                Intent intent = new Intent(getActivity(), ViewStream.class);
                intent.putExtra("b", broadcasts);
                startActivity(intent);
            } else {
                Utility.makeFilePublic(requireContext(), null, S3Constants.RECORDED_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
            }
            Broadcasts broadcasts1 = new Broadcasts();
            broadcasts1 = broadcasts;
            broadcasts1.setViewers(broadcasts.getViewers() == null ? 1 : broadcasts.getViewers() + 1);
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", broadcasts1);
            getViewModel().getService(URL).updateBroadcast(map).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.e("none", "onSubscribe: " + d.toString());
                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            Log.e("none", "onSuccess: " + response.message());

                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.wtf("onDestroy", "yes");
    }

    @Override
    public void onRefresh() {
        //getText().toString().isEmpty()
        if (textView2 != null) {
            getSearch(textView2.getText().toString().trim());
        } else {
            getBroadcasts(0);
        }
    }


    /*
     * get action from upload service to refresh list
     * */
    @Info(callType = Info.CallType.EVENT, description = "service calls this method to refresh api")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(HashMap<String, String> map) {
        getBroadcasts(0);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnNetworkChangeReceiver(NetworkModel networkModel) {
        if (networkModel.isNetwork()) {
            getBroadcasts(0);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = getContext().getSharedPreferences("myy", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("job_post_popup", "abc");
        editor.apply();
        binding.swipe.setRefreshing(false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemButtonClick(int position, Object model) {
        Broadcasts broadcast = (Broadcasts) model;
        this.broadcast = broadcast;
        if (broadcast.isApplyonvideo()) {
            Intent intent = new Intent(requireContext(), MyVideoCVActivity.class);
            intent.putExtra("forApply", "forApply");
            startActivityForResult(intent, CODE);
        } else if (broadcast.isApplyonjobsite()) {
//            String url = "https://h2people.com";
            String appUrl = broadcast.getJobSiteLink();
            if(!appUrl.startsWith("https")) {
                appUrl = "https://" + appUrl;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(appUrl));
            startActivity(i);
        }
    }

    private void applyJob(int id) {
        JobCandidates jobCandidates = new JobCandidates();
        jobCandidates.setBroadcast(broadcast.getBroadcast());
        jobCandidates.setUsername(getViewModel().getLoggedUser().getUsername());
        jobCandidates.setVideocvID(id);
        jobCandidates.setBroadcast_id(broadcast.getId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("resource", jobCandidates);
        NetworkCall.CallAPI(requireContext(), getService(DreamFactory.URL).postJobCandidate(map), this, false, Object.class, Endpoints.POST_JOB_CANDIDATES);

    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        if (endpoint.equalsIgnoreCase(Endpoints.POST_JOB_CANDIDATES)) {
            try {
                adapter.getData().get(broadcast.getPosition()).getJobCandidates().add(new JobCandidates().setUsername(getViewModel().getLoggedUser().getUsername()));
                adapter.notifyDataSetChanged();
                JSONObject object = new JSONObject();
                object.put(NotificationKeys.User.toString(), getViewModel().getLoggedUser().toString());
                object.put(NotificationKeys.message.toString(), "SimpleData");
                object.put(NotificationKeys.Type.toString(), NotificationType.JobRequest);
                Toast.makeText(getContext(), "You have successfully applied with your chosen video", Toast.LENGTH_LONG).show();
                Utility.sendNotification(false, Integer.parseInt(broadcast.getUsers().getQbid()), object, this);
            } catch (JSONException e) {
                Utility.showELog(e);
            }

        }
    }


    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {

    }

    @Override
    public void OnException(Throwable e, String endpoint) {

    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {

            if (s.toString().toLowerCase().startsWith("#")) {
                ArrayList<Broadcasts> itemList = new ArrayList<>();
                for (Broadcasts broadcasts1 : broadcasts) {
                    String ss = s.toString();
                    String listWithoutHashTag = ss.substring(1);
                    if (containsTag(broadcasts1.getTags(), listWithoutHashTag)) {
                        itemList.add(broadcasts1);
                    }
                }
                BroadcastsFragment.adapter.updateSearchList(itemList);
            }

            binding.searchView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.crossblack, 0);
        } else {
            binding.searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_icon_, 0, 0, 0);
//                hideSoftKeyboard();
            getBroadcasts(0);

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean containsTag(final List<Tags> list, final String tag) {
        return list.stream().anyMatch(o -> o.getTag().toLowerCase().contains(tag));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatingActionButton:
                String id = sharedVM.getSharedPreference().getString(SharedPreference.Firebase_Create_Id, "");
//                getUser(id);
                showUploadDialog();

                break;
            case R.id.map_imageView:
                getViewModel().setBroadcast(broadcasts);
                getHomeContainer().OnChange(new MapFragment(), FragmentTags.MAP);
                break;

        }
    }

    @Override
    public void onItemUserAction() {
        getHomeContainer().OnChange(new EditProfileFragment(), EDITPROFILE);
    }

    @Override
    public void onSuccess(QBEvent qbEvent, Bundle bundle) {
        Utility.showLog(qbEvent.toString());
    }

    @Override
    public void onError(QBResponseException e) {
        Utility.showELog(e);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            SharedPreferences preferences = getContext().getSharedPreferences("piccv", MODE_PRIVATE);
            String pic_cv = preferences.getString(SharedPreference.PIC_CV, "");
            Log.wtf("piccv reached", pic_cv);
            if (requestCode == CODE) {
                VideoCv videoCv = (VideoCv) data.getSerializableExtra(MyVideoCVActivity.DATA);
                if (videoCv != null) {
                    applyJob(videoCv.getId());
                }
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;
        if (binding.searchView.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
            int x = Math.round(event.getRawX());
            int y = binding.searchView.getRight() - binding.searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
            if (x >= y) {
                binding.searchView.setText("");
                Utility.showLog("drawable clicked");

                return true;
            }
        }
        return false;
    }

    private void getUser(String id) {
        pd.show();
        sharedVM.getService(Constants.DreamFactory.URL)
                .getUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<Users>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<Users> response) {
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    pd.dismiss();
                                    Log.v("postuser", new Gson().toJson(response.body()));
                                    sharedVM.getSharedPreference().edit().putString(Constants.SharedPreference.USER, new Gson().toJson(response.body())).apply();
                                    showUploadDialog();
                                }

                            } else {
                                pd.dismiss();
                                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            pd.dismiss();
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        pd.dismiss();
                        Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void showUploadDialog() {
        Gson gson = new Gson();
        String json = sharedVM.getSharedPreference().getString(Constants.SharedPreference.USER, "");
        Users user = gson.fromJson(json, Users.class);
//        if (user.getStatus().trim().equalsIgnoreCase("approved")) {
        Utility.getAlertDialoge(getContext(), "Create Your Pitch", "You need to add video for users to find you . It doesn’t need to be a selfie \uD83D\uDE0A")
                .setPositiveButton("Live Stream", (dialog, which) -> {
                    getHomeContainer().OnChange(new CreateStream(), FragmentTags.CREATE_STREAM);
                })
                .setNegativeButton("Offline", (dialog, which) -> {
                    if ((boolean) getViewModel().getPreferences(Constants.SharedPreference.ISUPLOADING, false)) {
                        Toast.makeText(getContext(), "You Already have a pitch uploading in progress, Please wait for it to finish!", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(getActivity(), OfflineStreamActivity.class));
                    }


                })
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
//        } else {
//            Utility.getAlertDialoge(getContext(), "Verification Pending", "A request for approval to upload your videos has been sent to admin.Please wait for approval  \uD83D\uDE0A")
//                    .setPositiveButton("Ok", (dialog, which) -> {
//                        dialog.dismiss();
//                    })
//                    .show();
//        }
    }


}
