package com.senarios.simxx.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.LocationHelper;
import com.hdev.common.databinding.LayoutEditextBinding;
import com.hdev.common.datamodels.Tags;
import com.hdev.common.datamodels.UserType;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.DataService;
import com.hdev.common.retrofit.NetworkCall;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.senarios.simxx.Info;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.databinding.ActivityCreateOfflineStreamBinding;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.S3UploadRequest;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;
import com.senarios.simxx.models.ApiService;
import com.senarios.simxx.models.SendMailRes;
import com.senarios.simxx.services.AmazonS3UploadService;
import com.squareup.picasso.Picasso;
import com.video_trim.TrimmerActivity;


import net.alhazmy13.mediapicker.Video.VideoPicker;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OfflineStreamActivity extends BaseActivity implements LocationHelper.LocationHelperCallback, Observer<Location>, OnSuccessListener<Location>, ApiResponse {
    private ActivityCreateOfflineStreamBinding binding;
    public static final String EXT = ".mp4";
    public static final String EXTPIC = ".png";
    private static final int PERMISSION_CODE = 386;
    private String BROADCAST, PATH = null;
    private Location mLastlocation;
    private boolean isUpload = false;
    private ProgressDialog pd;
    private List<Tags> tags = new ArrayList<>();
    private LocationHelper locationHelper;
    Context context;
    private Dialog loadingdialog;
    String appUrl;
    RadioButton apply_on_video, apply_on_site;


    @Override
    public void init() {

        context = this;
        locationHelper = new LocationHelper(this, this);
        locationHelper.getLocation().observe(this, this);
        getLifecycle().addObserver(locationHelper);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_offline_stream);
        pd = Utility.setDialogue(this);

       /* if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equalsIgnoreCase(UserType.RemoteWorker.toString())) {
            binding.isJob.setVisibility(View.GONE);
        }*/
        binding.selectVideo.setEnabled(false);
        binding.selectVideo.setClickable(false);
        binding.jobDes.setClickable(true);
        binding.jobDes.setEnabled(true);
        binding.jobSiteLink.setClickable(false);
        binding.jobSiteLink.setEnabled(false);
        binding.jobSiteView.setClickable(false);
        binding.jobSiteView.setEnabled(false);
        BROADCAST = getViewModel().getLoggedUser().getUsername() + System.currentTimeMillis();

        binding.etTitle.setFilters(new InputFilter[]{EMOJI_FILTER});

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
        loadingdialog = new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.setCancelable(false);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, this);
        }

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
                binding.jobSiteView.setOnClickListener(v->{
                    appUrl = binding.jobSiteLink.getText().toString();
                    if(!appUrl.startsWith("https")) {
                        appUrl = "https://" + appUrl;
                    }
                    EditText edttxt = new EditText(context);
                    int type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    edttxt.setInputType(type);
                    edttxt.setText(appUrl);
                    new AlertDialog.Builder(context)
                            .setTitle("URL")
                            .setMessage("Add a job site link")
                            .setView(edttxt)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    binding.jobSiteLink.setText(appUrl+ edttxt.getText().toString().trim());
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.cancel, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
            }
        });
        binding.isVideoLink.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.isSelectVideo.setChecked(false);
                binding.selectVideo.setEnabled(false);
                binding.selectVideo.setClickable(false);
                binding.jobDes.setClickable(true);
                binding.jobDes.setEnabled(true);
                binding.jobDesView.setClickable(true);
                binding.jobDesView.setEnabled(true);

                PATH = null;
                binding.group.setVisibility(View.GONE);
            }
        });

        binding.isSelectVideo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.isVideoLink.setChecked(false);
                binding.selectVideo.setEnabled(true);
                binding.selectVideo.setClickable(true);
                binding.jobDes.setClickable(false);
                binding.jobDes.setEnabled(false);
                binding.jobDesView.setClickable(false);
                binding.jobDesView.setEnabled(false);

                binding.jobDes.setText("");
                binding.ytThumbnail.setVisibility(View.GONE);
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
        });binding.bothMsgCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.messagesOnly.setChecked(false);
                binding.callOnly.setChecked(false);
            }
        });
        binding.btnLocation.setOnClickListener(v->{
            Intent intent = new Intent(OfflineStreamActivity.this, PicLocationActivity.class);
            intent.putExtra("itsOffline","offline");
            startActivity(intent);
        });


//        binding.jobDes.setMovementMethod(null);
//        binding.jobDes.setEnabled(false);

        binding.jobDesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edttxt = new EditText(context);
                int type = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                edttxt.setInputType(type);
                edttxt.setText(binding.jobDes.getText().toString());
                new AlertDialog.Builder(context)
                        .setTitle("URL")
                        .setMessage("Add a youtube link")
                        .setView(edttxt)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                binding.jobDes.setText(edttxt.getText().toString());
                                if (binding.jobDes.getText().toString().startsWith("https://youtu")) {
                                    String fullsize_path_img = "https://img.youtube.com/vi/" + getYouTubeId(binding.jobDes.getText().toString()) + "/0.jpg";
                                    binding.ytThumbnail.setVisibility(View.VISIBLE);
                                    binding.ytProgress.setVisibility(View.VISIBLE);
                                    binding.group.setVisibility(View.GONE);

//                                    Picasso.get().load(fullsize_path_img).into(binding.ytThumbnail);

                                    Glide.with(context).load(fullsize_path_img).optionalCenterCrop()
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .placeholder(R.drawable.h2pay2)
                                            .error(R.drawable.h2pay2)
                                            .addListener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    binding.ytThumbnail.setVisibility(View.GONE);
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    binding.ytProgress.setVisibility(View.GONE);
                                                    return false;
                                                }
                                            })
                                            .into(binding.ytThumbnail);
                                } else {
                                    binding.ytThumbnail.setVisibility(View.GONE);
                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

//        binding.isJob.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b == true) {
//                    binding.jobDes.setVisibility(View.VISIBLE);
//                } else {
//                    binding.jobDes.setVisibility(View.GONE);
//                }
//            }
//        });
    }

    private void initTagDialog() {
        AlertDialog.Builder builder = Utility.getAlertDialoge(OfflineStreamActivity.this, "", "");
        View view = LayoutInflater.from(this).inflate(com.hdev.common.R.layout.layout_editext, null);
        LayoutEditextBinding binding = DataBindingUtil.bind(view);
        binding.et.setFilters(new InputFilter[]{EMOJI_FILTER});
        builder.setView(binding.getRoot());
        builder.setPositiveButton("Add", (d, which) -> {
            if (!Utility.getString(binding.et).isEmpty()) {
                tags.add(new Tags(BROADCAST, Utility.getString(binding.et).trim()));
                OfflineStreamActivity.this.binding.tagsView.setData(getAddedTags(tags));
                d.dismiss();
            } else {
                Snackbar.make(binding.getRoot(), "Enter Tag", 3000).show();
            }
        }).show();
    }

    private List<String> getAddedTags(List<Tags> tags) {
        List<String> tagsList = new ArrayList<>();

        for (Tags tag : tags) {
            tagsList.add(tag.getTag());

        }
        return tagsList;
    }

    public void upload(View view) {
        if (binding.cityName.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Select location", Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.isVideoLink.isChecked() && binding.jobDes.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter youtube video link", Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.isSelectVideo.isChecked() && PATH==null) {
            Toast.makeText(getApplicationContext(), "Select Gallery Video", Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.rgApplyOnJobsite.isChecked() && binding.jobSiteLink.getText().toString().trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter job site link", Toast.LENGTH_SHORT).show();
            return;
        } else if (binding.rgApplyOnJobsite.isChecked() && !(binding.jobSiteLink.getText().toString().trim().startsWith("http")||binding.jobSiteLink.getText().toString().trim().startsWith("www."))) {
            Toast.makeText(getApplicationContext(), "Enter valid job site link", Toast.LENGTH_SHORT).show();
            return;
        }

        if (PATH != null && BROADCAST != null) {
            if (!AmazonS3UploadService.checkRunning()) {
                if (!Utility.getString(binding.etTitle).isEmpty()) {
                    if (tags.size() == 1) {
                        Snackbar.make(binding.getRoot(), "Add Some tags", 3000).show();
                    }
//                    else if (binding.isVideoLink.isChecked() && !android.util.Patterns.WEB_URL.matcher(binding.jobDes.getText().toString()).matches()) {
//                        Snackbar.make(binding.getRoot(), "Please enter valid URL", 3000).show();
//                    }
                    else {
                        Utility.getAlertDialoge(this, "Confirmation", "You Really want to upload this video?")
                                .setPositiveButton("Lets Go!", (dialog, which) -> {
                                    Utility.show(this);
                                    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://web.scottishhealth.live/")
                                            .addConverterFactory(GsonConverterFactory.create()).build();
                                    ApiService apiPost = retrofit.create(ApiService.class);
                                    Call<SendMailRes> call = apiPost.sendMail(getString(R.string.admin_mail),getString(R.string.admin_name)
                                            ,getString(R.string.mail_title),getString(R.string.mail_body1)+" "+binding.etTitle.getText().toString().trim()+" "+getString(R.string.mail_body2));
                                    call.enqueue(new Callback<SendMailRes>() {
                                        @Override
                                        public void onResponse(Call<SendMailRes> call, Response<SendMailRes> response) {
                                            if (response.isSuccessful()) {
                                                Utility.dismiss();
                                                dialog.dismiss();
                                                S3UploadRequest();
                                                SharedPreferences preferences = getSharedPreferences("myy", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("job_post_popup", "popup");
                                                editor.putString("latt", "");
                                                editor.putString("lonn", "");
                                                editor.apply();
                                            } else {
                                                Utility.dismiss();
                                                Toast.makeText(OfflineStreamActivity.this, "Process Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<SendMailRes> call, Throwable t) {
                                            Utility.dismiss();
                                            if (t instanceof SocketTimeoutException) {
                                                Toast.makeText(OfflineStreamActivity.this, "Time out, Please try again", Toast.LENGTH_SHORT).show();
                                            } else if (t instanceof IOException) {
                                                Toast.makeText(OfflineStreamActivity.this, "Check you internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                })
                                .setNegativeButton("Naah", (dialog, which) -> dialog.dismiss())
                                .show();
                    }


                } else {
                    Toast.makeText(OfflineStreamActivity.this, "Please Enter Pitch title", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(OfflineStreamActivity.this, "Please wait for current video to finish uploading.", Toast.LENGTH_SHORT).show();
            }
        } else {
//            Toast.makeText(OfflineStreamActivity.this, "n Please select a Video", Toast.LENGTH_SHORT).show();
            if (!Utility.getString(binding.etTitle).isEmpty()) {
                if (tags.size() == 1) {
                    Snackbar.make(binding.getRoot(), "Add Some tags", 3000).show();
                } else if (!binding.jobDes.getText().toString().startsWith("https://youtu")) {
                    Toast.makeText(getApplicationContext(), "Please Enter valid youtube link", Toast.LENGTH_SHORT).show();
                }
//                else if (binding.isVideoLink.isChecked() && !android.util.Patterns.WEB_URL.matcher(binding.jobDes.getText().toString()).matches()) {
//                    Snackbar.make(binding.getRoot(), "Please enter valid URL", 3000).show();
//                }
                else {
//                    SharedPreferences.Editor editorr = getSharedPreferences(YT, MODE_PRIVATE).edit();
//                    editorr.putString("linkBack", "");
//                    editorr.apply();
                    Utility.getAlertDialoge(this, "Confirmation", "You Really want to upload this video?")
                            .setPositiveButton("Lets Go!", (dialog, which) -> {
                                Utility.show(this);
                                Retrofit retrofit = new Retrofit.Builder().baseUrl("https://web.scottishhealth.live/")
                                        .addConverterFactory(GsonConverterFactory.create()).build();
                                ApiService apiPost = retrofit.create(ApiService.class);
                                Call<SendMailRes> call = apiPost.sendMail(getString(R.string.admin_mail),getString(R.string.admin_name),
                                        getString(R.string.mail_title),getString(R.string.mail_body1)+" "+binding.etTitle.getText().toString().trim()+" "+getString(R.string.mail_body2));
                                call.enqueue(new Callback<SendMailRes>() {
                                    @Override
                                    public void onResponse(Call<SendMailRes> call, Response<SendMailRes> response) {
                                        if (response.isSuccessful()) {
                                            Utility.dismiss();
                                            dialog.dismiss();
                                            ytUploadRequest();
                                            SharedPreferences preferences = getSharedPreferences("myy", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("job_post_popup", "popup");
                                            editor.putString("latt", "");
                                            editor.putString("lonn", "");
                                            editor.apply();
                                        } else {
                                            Utility.dismiss();
                                            Toast.makeText(OfflineStreamActivity.this, "Process Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<SendMailRes> call, Throwable t) {
                                        Utility.dismiss();
                                        if (t instanceof SocketTimeoutException) {
                                            Toast.makeText(OfflineStreamActivity.this, "Time out, Please try again", Toast.LENGTH_SHORT).show();
                                        } else if (t instanceof IOException) {
                                            Toast.makeText(OfflineStreamActivity.this, "Check you internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            })
                            .setNegativeButton("Naah", (dialog, which) -> dialog.dismiss())
                            .show();
                }


            } else {
                Toast.makeText(OfflineStreamActivity.this, "Please Enter Pitch title", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void selectVideo(View view) {

      /* Intent intent=new Intent(this,Class.forName("com."));
        startActivity(intent);*/


        Utility.showVidepPicker(OfflineStreamActivity.this);
    }

    public void playVideo(View view) {
        if (PATH != null) {
            Utility.openVideoIntent(this, PATH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreferences preferences = getSharedPreferences(YT, MODE_PRIVATE);
//        String getYtLink = preferences.getString("linkBack", "");
//
////        String linkBack = getIntent().getStringExtra("linkBack");
//        if (getYtLink != null) {
//            binding.jobDes.setText(getYtLink);
//        }else {
//            binding.jobDes.setText("");
//        }
        SharedPreferences prefs = getSharedPreferences("myy", MODE_PRIVATE);
        String cityLoc = prefs.getString("city", "");
        if (cityLoc != null) {
            binding.cityName.setVisibility(View.VISIBLE);
            binding.cityName.setText(cityLoc);
        }
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                if (mPaths != null && mPaths.size() > 0) {
                    Utility.showLog("Video Success" + mPaths.get(0));
                    PATH = mPaths.get(0);
                    String duration = Utility.convertMillieToHMmSs(Utility.getVideoDuration(PATH));
                    if (duration != null && !duration.isEmpty()) {
                        startActivityForResult(new Intent(this, TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, mPaths.get(0)), TrimmerActivity.CODE);
                    } else {
                        Toast.makeText(getApplicationContext(), "You must select video", Toast.LENGTH_SHORT).show();
                    }
                    //  startActivityForResult(new Intent(this,EditorActivity.class).putExtra(EditorActivity.EXTRA_VIDEO_PATH,mPaths.get(0)),EditorActivity.CODE);
                }
            }
        } else if (requestCode == TrimmerActivity.CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String path = data.getStringExtra(TrimmerActivity.EXTRA_VIDEO_PATH);
                String duration = Utility.convertMillieToHMmSs(Utility.getVideoDuration(path));
                Utility.showLog("Video Duration" + duration);//use this duration
                if (isVideoDuration(duration)) {
                    Utility.showLog("trimmed video " + path);
                    if (path != null) {
                        PATH = path;
                        binding.videoView.setImageBitmap(Utility.getThumbnail(PATH));
                        binding.group.setVisibility(View.VISIBLE);
                    }
                } else {
                    Utility.getAlertDialoge(this, "Video Not Supported", "Your offline pitch duration must be not more than 5 minutes.")
                            .setPositiveButton("Trim Previous Video Again", (dialog, which) -> {
                                dialog.dismiss();
                                startActivityForResult(new Intent(this, TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, PATH), TrimmerActivity.CODE);
                            })
                            .setNegativeButton("Select New", (dialog, which) -> {
                                dialog.dismiss();
                                Utility.showVidepPicker(OfflineStreamActivity.this);

                            })
                            .show();
                }


            }
        } else if (resultCode == locationHelper.CODE) {
            locationHelper.onRequestApproved();
        }
    }


    private boolean isVideoDuration(String duration) {
        return (Integer.parseInt(duration.split(":")[0]) == 5 && Integer.parseInt(duration.split(":")[1]) == 0) ||
                (Integer.parseInt(duration.split(":")[0]) < 5);
    }


    @Info(classname = AmazonS3UploadService.class, callType = Info.CallType.EVENT, method = "S3UploadRequest()")
    private void S3UploadRequest() {
        isUpload = true;
        S3UploadRequest s3UploadRequest = new S3UploadRequest()
                .setPath(PATH)
                .setKey(BROADCAST)
                .setS3_PATH(S3Constants.OFFLINE_VIDEO_FOLDER + "/" + BROADCAST + EXT)
                .setAction(S3UploadRequest.UploadActions.BROADCAST)
                .setBroadcast(getBroadcast())
                .setMessage("You have a pending offline pitch, do you want to upload to now? if you cancel it, it will be cleared from system");
        Intent intent = new Intent(this, AmazonS3UploadService.class);
        intent.putExtra(S3_REQUEST, s3UploadRequest);
        startService(intent);
        finish();
    }

    @Info(classname = AmazonS3UploadService.class, callType = Info.CallType.EVENT, method = "S3UploadRequest()")
    private void ytUploadRequest() {
        isUpload = true;
        postBroadcast();
        onBackPressed();
        finish();
    }

    @Info(callType = Info.CallType.API_CALL, description = "post broadcast on simx server")
    private void postBroadcast() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("resource", getBroadcast());
        NetworkCall.CallAPI(OfflineStreamActivity.this, Utility.getService(Constants.DreamFactory.URL).postBroadcast(map), this, false, Broadcasts.class, Constants.Endpoints.BROADCASTS);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        SharedPreferences.Editor editor = getSharedPreferences(YT, MODE_PRIVATE).edit();
//        editor.putString("linkBack", "");
//        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        SharedPreferences.Editor editor = getSharedPreferences(YT, MODE_PRIVATE).edit();
//        editor.putString("linkBack", "");
//        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd.isShowing()) {
            pd.dismiss();
        }

        if (!isUpload) {
            stopService(new Intent(this, AmazonS3UploadService.class));
            if (PATH != null) {
                Utility.deleteFile(PATH);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("myy", MODE_PRIVATE).edit();
        editor.putString("city", "");
        editor.apply();
    }

    @Override
    public void onSuccess(Location location) {
        mLastlocation = location;
    }


    @Info(callType = Info.CallType.CUSTOM)
    private Broadcasts getBroadcast() {
        Broadcasts broadcast = new Broadcasts();
        broadcast.setUsername(getViewModel().getLoggedUser().getUsername());
        broadcast.setId(0);
        broadcast.setViewers(0);
        broadcast.setVideourl(binding.jobDes.getText().toString());
        broadcast.setStatus(GoCoder.OFFLINE);
        broadcast.setIsjob(true);
        broadcast.setTitle(Utility.getString(binding.etTitle));
        broadcast.setTime(Utility.getDateString());
        SharedPreferences prefs = getSharedPreferences("myy", MODE_PRIVATE);
        String lat = prefs.getString("latt", "");
        String lng = prefs.getString("lonn", "");
        if (mLastlocation != null) {
            broadcast.setLocation(Utility.getLocationFromLatLng(this, Double.parseDouble(lat), Double.parseDouble(lng)));
            broadcast.setLatti(lat);
            broadcast.setLongi(lng);
        } else {
            broadcast.setLocation(Utility.getLocationFromLatLng(this, 2.0943, 57.1497));
            broadcast.setLatti("2.0943");
            broadcast.setLongi("57.1497");
        }
        tags.remove(0);

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
        }
        else if (binding.bothMsgCall.isChecked()) {
            broadcast.setBothmsgcall(true);
            broadcast.setMessageonly(false);
            broadcast.setCallonly(false);
        }

        broadcast.setTags(tags);
        broadcast.setName(getViewModel().getLoggedUser().getName());
        broadcast.setBroadcast(BROADCAST);
        broadcast.setJobSiteLink(binding.jobSiteLink.getText().toString().trim());
        broadcast.setSkill(getViewModel().getLoggedUser().getSkills());
        broadcast.setImglink(broadcast.getBroadcast());
        broadcast.setOffline(true);
        broadcast.setApproved(true);
        broadcast.setJobPostStatus("Pending");
//        if (binding.isJob.isChecked()) {
//        broadcast.setIsjob(binding.isVideoLink.isChecked());
//        broadcast.setJobDes(binding.jobDes.getText().toString());
//        }
        return broadcast;
    }


    @Override
    public void onChanged(Location location) {
        mLastlocation = location;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (!hasPermissions(this, permissions)) {
                requestPermissions(locationHelper.PERMISSIONS, LocationHelper.CODE);
            } else if (CommonUtils.shouldShowRationalPermission(this, locationHelper.PERMISSIONS)) {
                CommonUtils.showSettingDialog(this);
            } else {
                locationHelper.onRequestApproved();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onPermissionNeeded() {
        requestPermissions(locationHelper.PERMISSIONS, LocationHelper.CODE);
    }

    @Override
    public void onPermissionDenied() {

    }


    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
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

    private String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }
}
