package com.senarios.simxx.fragments.homefragments;

import static com.hdev.common.CommonUtils.hasPermissions;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.LocationHelper;
import com.hdev.common.databinding.LayoutEditextBinding;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Tags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.LiveStreamActivity;
import com.senarios.simxx.activities.MainActivity;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.PicLocationActivity;
import com.senarios.simxx.databinding.CreateStreamFragmentBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.models.ApiService;
import com.senarios.simxx.models.SendMailRes;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateStream extends BaseFragment implements LocationHelper.LocationHelperCallback, Observer<Location>, View.OnClickListener, CompoundButton.OnCheckedChangeListener, Constants.SharedPreference, Constants.Messages {
    private CreateStreamFragmentBinding binding;
    private static final int PERMISSION_CODE = 386;
    private List<Tags> tags = new ArrayList<>();
    private String BROADCAST;
    private String jobDes;
    String appUrl;
    private Location mLastlocation;
    private LocationHelper locationHelper;
    Context context;

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.create_stream_fragment, container, false);
        binding = DataBindingUtil.bind(rootView);
        init();
        return rootView;
    }

    @Override
    protected void init() {
        super.init();
        //ask permission
        context = this.getContext();

        locationHelper = new LocationHelper(requireContext(), this);
        locationHelper.getLocation().observe(this, this);
        getLifecycle().addObserver(locationHelper);


        binding.etTitle.setFilters(new InputFilter[]{EMOJI_FILTER});

        binding.checkFrontCamera.setOnCheckedChangeListener(this);
        binding.checkRearCamera.setOnCheckedChangeListener(this);
        binding.jobSiteLink.setClickable(false);
        binding.jobSiteLink.setEnabled(false);
        binding.jobSiteView.setClickable(false);
        binding.jobSiteView.setEnabled(false);

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragment() != null) {
                getParentFragment().getChildFragmentManager().popBackStack();
            }
        });
        binding.btnLive.setOnClickListener(this);
        binding.btnLocation.setOnClickListener(this);
        BROADCAST = getViewModel().getLoggedUser().getUsername() + System.currentTimeMillis();
        tags.add(new Tags(null, "Add Tag"));
        binding.tagsView.setData(getAddedTags(tags));
        binding.tagsView.addOnTagSelectListener((item, selected) -> {
            if (selected && !item.toString().equalsIgnoreCase("Add Tag")) {
                tags.remove(new Tags(BROADCAST, item.toString()));
                binding.tagsView.setData(getAddedTags(tags));
            } else if (tags.size() == 6) {
                Snackbar.make(binding.getRoot(), "Tags Limit Reached!", 3000).show();
            } else {
                if (item.toString().equalsIgnoreCase("Add Tag")) {
                    initTagDialog();
                }
            }
        });
        binding.rgApplyOnVideo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rgApplyOnJobsite.setChecked(false);
                binding.jobSiteLink.setText("");
                binding.jobSiteLink.setClickable(false);
                binding.jobSiteLink.setEnabled(false);
                binding.jobSiteView.setClickable(false);
                binding.jobSiteView.setEnabled(false);
            }
        });

        binding.rgApplyOnJobsite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.rgApplyOnVideo.setChecked(false);
                binding.jobSiteLink.setClickable(true);
                binding.jobSiteLink.setEnabled(true);
                binding.jobSiteView.setClickable(true);
                binding.jobSiteView.setEnabled(true);
                binding.jobSiteView.setOnClickListener(v -> {
                    appUrl = binding.jobSiteLink.getText().toString();
                    if(!appUrl.startsWith("https")) {
                        appUrl = "https://" + appUrl;
                    }
                    EditText edttxt = new EditText(context);
                    int type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    edttxt.setInputType(type);
                    edttxt.setText(binding.jobSiteLink.getText().toString());
                    new AlertDialog.Builder(context)
                            .setTitle("URL")
                            .setMessage("Add a job site link")
                            .setView(edttxt)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    binding.jobSiteLink.setText(appUrl + edttxt.getText().toString().trim());
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.cancel, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
            }
        });
        binding.messagesOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.callOnly.setChecked(false);
                binding.bothMsgCall.setChecked(false);
            }
        });
        binding.callOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.messagesOnly.setChecked(false);
                binding.bothMsgCall.setChecked(false);
            }
        });
        binding.bothMsgCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.messagesOnly.setChecked(false);
                binding.callOnly.setChecked(false);
            }
        });

//        binding.jobDes.setVisibility(View.VISIBLE);
//        binding.jobDes.setMovementMethod(null);
//        binding.jobDes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditText edttxt = new EditText(context);
//                edttxt.setText(binding.jobDes.getText());
//                new AlertDialog.Builder(context)
//                        .setTitle("URL")
//                        .setMessage("Add a link")
//                        .setView(edttxt)
//                        .setIcon(R.drawable.simx_logo4)
//                        // Specifying a listener allows you to take an action before dismissing the dialog.
//
//                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                binding.jobDes.setText(edttxt.getText());
//                            }
//                        })
//
//                        // A null listener allows the button to dismiss the dialog and take no further action.
//                        .setNegativeButton(android.R.string.cancel, null)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//        });

        binding.isVideoLink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b == true) {
                    binding.jobDes.setVisibility(View.VISIBLE);
                } else {
                    binding.jobDes.setVisibility(View.GONE);
                }
            }
        });

    }

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.NON_SPACING_MARK
                        || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };


    private List<String> getAddedTags(List<Tags> tags) {
        List<String> tagsList = new ArrayList<>();

        for (Tags tag : tags) {
            tagsList.add(tag.getTag());

        }
        return tagsList;
    }

    private void initTagDialog() {
        AlertDialog.Builder builder = Utility.getAlertDialoge(requireContext(), "", "");
        View view = LayoutInflater.from(requireContext()).inflate(com.hdev.common.R.layout.layout_editext, null);
        LayoutEditextBinding binding = DataBindingUtil.bind(view);
        binding.et.setFilters(new InputFilter[]{EMOJI_FILTER});
        builder.setView(binding.getRoot());
        builder.setPositiveButton("Add", (dialog, which) -> {
            if (!Utility.getString(binding.et).isEmpty()) {
                tags.add(new Tags(BROADCAST, Utility.getString(binding.et).trim()));
                CreateStream.this.binding.tagsView.setData(getAddedTags(tags));
                dialog.dismiss();
            } else {
                Snackbar.make(binding.getRoot(), "Enter Tag", 3000).show();
            }
        }).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cross:

                break;
            case R.id.btn_live:
                validations();
                break;
            case R.id.btn_location:
                Intent intent = new Intent(getActivity(), PicLocationActivity.class);
                intent.putExtra("itsStream", "stream");
                startActivity(intent);
                break;

        }

    }


    private void validations() {
        if (Utility.getString(binding.etTitle).isEmpty()) {
            binding.etTitle.setError("Enter title");
            binding.etTitle.requestFocus();
        } else if (binding.cityName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please Select location", Toast.LENGTH_SHORT).show();
            return;
        } else if (!Utility.isGooglePlayServicesAvailable(getContext())) {
            AlertDialog dialog = Utility.getAlertDialoge(getContext(), PLAY_SERVICE_ERROR_TITLE, PLAY_SERVICES_ERROR).create();
            dialog.show();
        } else if (!hasPermissions(getContext(), locationHelper.PERMISSIONS)) {
            requestPermissions(locationHelper.PERMISSIONS, PERMISSION_CODE);
        } else if (mLastlocation == null) {
            locationHelper.onRequestApproved();
        } else if (tags.size() == 1) {
            Snackbar.make(binding.getRoot(), "Add Some tags", 3000).show();
        } else if (binding.rgApplyOnJobsite.isChecked() && binding.jobSiteLink.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Enter job site link", Toast.LENGTH_SHORT).show();
        } else if (binding.rgApplyOnJobsite.isChecked() && !(binding.jobSiteLink.getText().toString().trim().startsWith("http")||binding.jobSiteLink.getText().toString().trim().startsWith("www."))) {
            Toast.makeText(getContext(), "Enter valid job site link", Toast.LENGTH_SHORT).show();
        } else {
            String[] broadcastTypes = getResources().getStringArray(R.array.broadcast_type);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setSingleChoiceItems(broadcastTypes, 0, (dialog, which) -> {
                dialog.dismiss();
                startBroadCasting(which == 0 ? "00:30" : "15:00");
            });
            alertDialog.setNegativeButton("cancel", (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.setCancelable(false);
            AlertDialog durationDialog = alertDialog.create();
            durationDialog.show();

        }
    }

    private void startBroadCasting(String duration) {
        Broadcasts broadcast=getBroadcast();
        broadcast.setDuration(duration);

//        startActivity(StreamActivity.newInstance(requireActivity(),broadcast));
        SharedPreferences preferences = getContext().getSharedPreferences("myy", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("job_post_popup", "popup");
        editor.putString("latt", "");
        editor.putString("lonn", "");
        editor.apply();
        Intent intent = new Intent(getContext(), LiveStreamActivity.class);
        intent.putExtra("b", broadcast);
        intent.putExtra("duration",duration);
        startActivity(intent);

//        Intent intent = new Intent(getContext(), StreamActivity.class);
//        intent.putExtra(StreamActivity.DATA, broadcast);
//        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (!hasPermissions(getContext(), permissions)) {
                requestPermissions(locationHelper.PERMISSIONS, LocationHelper.CODE);
            } else if (CommonUtils.shouldShowRationalPermission(requireActivity(), locationHelper.PERMISSIONS)) {
                CommonUtils.showSettingDialog(requireActivity());
            } else {
                locationHelper.onRequestApproved();
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    private Broadcasts getBroadcast() {
        Broadcasts broadcast = new Broadcasts();
        broadcast.setUsername(getViewModel().getLoggedUser().getUsername());
        broadcast.setId(0);
        broadcast.setViewers(0);
        broadcast.setIsjob(true);
        broadcast.setStatus(Constants.GoCoder.ONLINE);
        broadcast.setTitle(Utility.getString(binding.etTitle));
        broadcast.setTime(Utility.getDateString());
        SharedPreferences prefs = getContext().getSharedPreferences("myy", Context.MODE_PRIVATE);
        String lat = prefs.getString("latt", "");
        String lng = prefs.getString("lonn", "");
        if (mLastlocation != null) {
            broadcast.setLocation(Utility.getLocationFromLatLng(requireContext(), Double.parseDouble(lat), Double.parseDouble(lng)));
            broadcast.setLatti(lat);
            broadcast.setLongi(lng);
        } else {
            broadcast.setLocation(Utility.getLocationFromLatLng(requireContext(), 2.0943, 57.1497));
            broadcast.setLatti("2.0943");
            broadcast.setLongi("57.1497");
        }
        if (binding.rgApplyOnVideo.isChecked()) {
            broadcast.setApplyonvideo(true);
            broadcast.setApplyonjobsite(false);
        } else if (binding.rgApplyOnJobsite.isChecked()) {
            broadcast.setApplyonvideo(false);
            broadcast.setApplyonjobsite(true);
        }
        if (binding.messagesOnly.isChecked()) {
            broadcast.setMessageonly(true);
            broadcast.setCallonly(false);
            broadcast.setBothmsgcall(false);
        } else if (binding.callOnly.isChecked()) {
            broadcast.setCallonly(true);
            broadcast.setMessageonly(false);
            broadcast.setBothmsgcall(false);
        } else if (binding.bothMsgCall.isChecked()) {
            broadcast.setBothmsgcall(true);
            broadcast.setMessageonly(false);
            broadcast.setCallonly(false);
        }
        broadcast.setName(getViewModel().getLoggedUser().getName());
        broadcast.setBroadcast(BROADCAST);
        broadcast.setJobSiteLink(binding.jobSiteLink.getText().toString().trim());
        broadcast.setSkill(Constants.BROADCASTER);
        broadcast.setImglink(broadcast.getBroadcast());
        broadcast.setOffline(false);
        broadcast.setApproved(true);
        broadcast.setJobPostStatus("Approved");
        tags.remove(0);
        broadcast.setTags(tags);
//       if (binding.isVideoLink.isChecked())
//       {
//           broadcast.setIsjob(binding.isVideoLink.isChecked());
//           broadcast.setJobDes(binding.jobDes.getText().toString());
//       }
        if (binding.checkFrontCamera.isChecked()) {
            broadcast.setCamera(1);
        } else if (binding.checkRearCamera.isChecked()) {
            broadcast.setCamera(0);
        }
        return broadcast;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == locationHelper.CODE) {
            locationHelper.onRequestApproved();
        }
    }


    @Override
    public void onChanged(Location location) {
        mLastlocation = location;
    }

    @Override
    public void onPermissionNeeded() {
        requestPermissions(locationHelper.PERMISSIONS, LocationHelper.CODE);
    }

    @Override
    public void onPermissionDenied() {
        CommonUtils.showSettingDialog(requireActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    getActivity().onBackPressed();
                    requireActivity().startActivity(new Intent(requireActivity(), MainActivity.class).putExtra("main", true));
                    requireActivity().finish();
                }
            }, 1000);
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getContext().getSharedPreferences("myy", Context.MODE_PRIVATE).edit();
        editor.putString("city", "");
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getContext().getSharedPreferences("myy", Context.MODE_PRIVATE);
        String cityLoc = prefs.getString("city", "");
        if (cityLoc != null) {
            binding.cityName.setVisibility(View.VISIBLE);
            binding.cityName.setText(cityLoc);
        }
    }
}
