package com.senarios.simxx.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.HttpMethod;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.RealPathUtil;
import com.hdev.common.datamodels.ResponseVideoCv;
import com.hdev.common.datamodels.S3UploadRequest;
import com.hdev.common.datamodels.VideoCv;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.ImageUtil;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.MyVideoCvAdaptor;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.ActivityMyVideoCVBinding;
import com.senarios.simxx.services.AmazonS3UploadService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.video_trim.TrimmerActivity;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

import static com.senarios.simxx.activities.OfflineStreamActivity.EXT;
import static com.senarios.simxx.activities.OfflineStreamActivity.EXTPIC;

public class MyVideoCVActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, Toolbar.OnMenuItemClickListener,ApiResponse,RecyclerViewCallback {
    private MyVideoCvAdaptor adaptor;
    private ActivityMyVideoCVBinding binding;
    private VideoCv videoCv;
    private BottomSheetBehavior behavior;
    public static final String DATA="Video CV";
    public static final String DATA_PIC="Picture CV";
    boolean forApply=false;
    private boolean isImage = false;
    FirebaseStorage storage;
    FirebaseDatabase database;
    Uri resultUri;
    Uri uri;


    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(HashMap<String,String> map){
        getVideoCVs();
    }

    @Override
    public ActivityMyVideoCVBinding binding() {
        binding= DataBindingUtil.setContentView(this,R.layout.activity_my_video_c_v);
        return binding;
    }

    private void upload(){
        if (AmazonS3UploadService.checkRunning()){
            Utility.showSnackBar(binding.root,"Please wait for current video to upload");
        }
        else{
            validateInputs();
        }


    }

    public void selectVideo(View view){
        Utility.showVidepPicker(MyVideoCVActivity.this);
    }

    public void playVideo(View view){
        if (videoCv.getPath()!=null){
            Utility.openVideoIntent(this,videoCv.getPath());
        }
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (behavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
                Rect outRect = new Rect();
                binding.bottomsheet.root.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void init() {
        if (getIntent().hasExtra("forApply")){
            forApply=true;
        }
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        getVideoCVs();
        binding.swipe.setEnabled(false);
        binding.swipe.setOnRefreshListener(this);
        binding.toolbar.setOnMenuItemClickListener(this);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        behavior=BottomSheetBehavior.from(binding.bottomsheet.root);
        toggleSheet(BottomSheetBehavior.STATE_HIDDEN);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState==BottomSheetBehavior.STATE_DRAGGING){
                    toggleSheet(BottomSheetBehavior.STATE_HIDDEN);
                }
                else if (newState==BottomSheetBehavior.STATE_EXPANDED){
                    binding.root.setOnTouchListener((v, event) -> false);
                }
                else if (newState==BottomSheetBehavior.STATE_HIDDEN){
                    if (isKeyboardShowing)hideSoftKeyboard();
                    binding.bottomsheet.group.setVisibility(View.GONE);
                    binding.bottomsheet.btnUpload.setVisibility(View.GONE);
                    binding.bottomsheet.edittext.et.setText("");
                    videoCv=null;
                    binding.root.setOnTouchListener(null);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        binding.bottomsheet.edittext.inputlayout.setHintEnabled(true);
        binding.bottomsheet.edittext.inputlayout.setHint("Upload Video");
        binding.bottomsheet.edittext.inputlayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        binding.content.applyByPicCv.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.content.applyByVidCv.setChecked(false);
            }
        });

        binding.content.applyByVidCv.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.content.applyByPicCv.setChecked(false);
            }
        });
    }

    private void getVideoCVs() {
        binding.content.progressBar.setVisibility(View.VISIBLE);
        binding.content.recyclerview.setVisibility(View.GONE);
        NetworkCall.CallAPI(this, Utility.getService(Constants.DreamFactory.URL).getVideoCVs("username="+getViewModel().getLoggedUser().getUsername())
        ,this,false, ResponseVideoCv.class,Endpoints.VIDEOCV);
    }

    private void deleteCV() {
        NetworkCall.CallAPI(this, Utility.getService(Constants.DreamFactory.URL).deleteVideoCV(videoCv.getId())
                ,this,false, Object.class,Endpoints.VIDEOCV);
    }

    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        hideLoading();
        if (body instanceof ResponseVideoCv){
            ResponseVideoCv responseVideoCv=(ResponseVideoCv)body;
            if (responseVideoCv.getVideoCvs().isEmpty()){

            }
            if (forApply){
                adaptor=new MyVideoCvAdaptor(this,((ResponseVideoCv) body).getVideoCvs(),this,true);
            }else {
                adaptor=new MyVideoCvAdaptor(this,((ResponseVideoCv) body).getVideoCvs(),this);
            }
            binding.content.recyclerview.setAdapter(adaptor);
        }
        else if (response.raw().request().method().equalsIgnoreCase(HttpMethod.DELETE.toString())){
            if (adaptor!=null){
                adaptor.getData().remove(videoCv);
                adaptor.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        hideLoading();
        Utility.showSnackBar(binding.getRoot(),"Something went wrong!");
    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        hideLoading();
        Utility.showSnackBar(binding.getRoot(),"Something went wrong!");
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        hideLoading();
        Utility.showSnackBar(binding.getRoot(),"Network Error");
    }

    private void hideLoading(){
        binding.swipe.setRefreshing(false);
        binding.content.progressBar.setVisibility(View.GONE);
        binding.content.recyclerview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        getVideoCVs();
    }

    @Override
    public void onItemClick(int position, Object model) {
       if (getCallingActivity()!=null){
           Intent intent=new Intent();
           intent.putExtra(DATA,(VideoCv)model);
           setResult(Activity.RESULT_OK,intent);
           finish();
       }
    }

    @Override
    public void onItemPictureClick(int position, Object model) {
        Utility.makeFilePublic(this, null, S3Constants.VIDEO_CV_FOLDER + "/" +((VideoCv) model).getVideocv()+ OfflineStreamActivity.EXT);

    }

    @Override
    public void onItemDelete(int position, Object model) {
        CommonUtils.getAlertDialoge(this,"User Action Required","Do you want to delete this Video Cv?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    videoCv=(VideoCv)model;
                    deleteCV();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                }).show();



    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                if (binding.content.applyByPicCv.isChecked()) {
                    videoCv=new VideoCv();
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    startActivityForResult(intent, 21);
                    com.github.drjacky.imagepicker.ImagePicker.Companion.with(MyVideoCVActivity.this)
                            .crop()
                            .cropOval()
                            .compress(1024)
                            .maxResultSize(1080, 1080)
                            .start(101);
//                    videoCv.setVideocv("abc");
//                    Utility.showImagePicker(MyVideoCVActivity.this);
                } else if (binding.content.applyByVidCv.isChecked()) {
                    videoCv=new VideoCv();
                    videoCv.setVideocv(getViewModel().getLoggedUser().getUsername()+System.currentTimeMillis());
                    toggleSheet(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;

        }

        return true;
    }

    private void toggleSheet(int state){
        behavior.setState(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                if (mPaths!=null && mPaths.size()>0) {
                    Utility.showLog("Video Success" + mPaths.get(0));
                    videoCv.setPath(mPaths.get(0));
                    startActivityForResult(new Intent(this, TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH,mPaths.get(0)),TrimmerActivity.CODE);

                }
            }
        }
        else if (requestCode == TrimmerActivity.CODE && resultCode == RESULT_OK){
            if (data!=null){
                String path=data.getStringExtra(TrimmerActivity.EXTRA_VIDEO_PATH);
                String duration = Utility.convertMillieToHMmSs(Utility.getVideoDuration(path));
                Utility.showLog("Video Duration" + duration);//use this duration
                if (isVideoDuration(duration)){
                    Utility.showLog("trimmed video "+path);
                    if (path!=null) {
                        videoCv.setPath(path);
                        binding.bottomsheet.videoView.setImageBitmap(Utility.getThumbnail(path));
                        binding.bottomsheet.group.setVisibility(View.VISIBLE);
                        binding.bottomsheet.btnUpload.setVisibility(View.VISIBLE);
                        binding.bottomsheet.btnUpload.setOnClickListener(v -> upload());

                    }
                } else {
                    Utility.getAlertDialoge(this, "Video Not Supported", "Your offline pitch duration must be not more than 5 minutes.")
                            .setPositiveButton("Trim Previous Video Again", (dialog, which) -> {
                                dialog.dismiss();
                                startActivityForResult(new Intent(this,TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH,videoCv.getPath()),TrimmerActivity.CODE);
                            })
                            .setNegativeButton("Select New", (dialog, which) -> {
                                dialog.dismiss();
                                Utility.showVidepPicker(MyVideoCVActivity.this);

                            })
                            .show();
                }



            }
        }
//        if (requestCode == 21 && resultCode == Activity.RESULT_OK&&data!=null&&data.getData()!=null) {
//            uri = data.getData();
//            binding.content.picCvImg.setImageURI(uri);
//            Context context = MyVideoCVActivity.this;
//            String path = RealPathUtil.getRealPath(context, uri);
//            Log.wtf("pathimage", path);
//            uploadImage();
//            S3UploadRequestPic();
//            try {
//                postImage(getViewModel().getSharedPreference().getString(Linkedin_ID, "")+".png", ImageUtil.convert( MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() ,resultUri)));
//            }
//            catch (Exception e) {
//                Utility.showELog(e);
//            }
//        }
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            Context context = MyVideoCVActivity.this;
            String path = RealPathUtil.getRealPath(context, uri);
            Log.wtf("pathimage", path);
            uploadImage();
            S3UploadRequestPic();
            try {
                postImage(getViewModel().getSharedPreference().getString(Linkedin_ID, "")+".png", ImageUtil.convert( MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() ,resultUri)));
            }
            catch (Exception e) {
                Utility.showELog(e);
            }
        }
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && mPaths.size() > 0) {
                CommonUtils.cropImagee(this, new File(mPaths.get(0)));
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result!=null) {
                    resultUri = result.getUri();

                    SharedPreferences preferences = getSharedPreferences("my", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("imagePath", resultUri.toString());
                    editor.apply();
                    uploadImage();

//                    videoCv.setVideocv(resultUri.getPath());

//                    binding.content.picCvImg.setVisibility(View.VISIBLE);
//                    binding.content.picCvImg.setImageURI(resultUri);
//                    try {
//                        postImage(getViewModel().getSharedPreference().getString(Firebase_Create_Id, "")+".png",ImageUtil.convert( MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() ,resultUri)));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    S3UploadRequestPic();
                    Log.wtf("picupload","reached");
//                    SharedPreferences.Editor editor = getSharedPreferences("piccv", Context.MODE_PRIVATE).edit();
//                    editor.putString(SharedPreference.PIC_CV, "picturecv");
//                    editor.apply();
//                    binding.uploadPicture.setImageURI(resultUri);
                    try {
                        postImage(getViewModel().getSharedPreference().getString(Linkedin_ID, "")+".png", ImageUtil.convert( MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() ,resultUri)));
                    }
                    catch (Exception e) {
                        Utility.showELog(e);
                    }


                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private boolean isVideoDuration(String duration) {
        return (Integer.parseInt(duration.split(":")[0])==5 && Integer.parseInt(duration.split(":")[1])==0) ||
                (Integer.parseInt(duration.split(":")[0])<5);
    }

    private void S3UploadRequest() {
        S3UploadRequest s3UploadRequest=new S3UploadRequest()
                .setPath(videoCv.getPath())
                .setKey(videoCv.getVideocv())
                .setVideoCv(videoCv)
                .setS3_PATH(S3Constants.VIDEO_CV_FOLDER + "/" + videoCv.getVideocv() + EXT)
                .setAction(S3UploadRequest.UploadActions.VIDEOCV)
                .setMessage("You have a pending offline pitch, do you want to upload to now? if you cancel it, it will be cleared from system");
        Intent intent=new Intent(this, AmazonS3UploadService.class);
        intent.putExtra(S3_REQUEST,s3UploadRequest);
        startService(intent);
        toggleSheet(BottomSheetBehavior.STATE_HIDDEN);
    }
    private void S3UploadRequestPic() {
        forApply=true;
        S3UploadRequest s3UploadRequest=new S3UploadRequest()
                .setPath(videoCv.getPath())
                .setKey(videoCv.getVideocv())
                .setVideoCv(videoCv)
                .setS3_PATH(S3Constants.VIDEO_CV_FOLDER + "/" + videoCv.getVideocv() + EXTPIC)
                .setAction(S3UploadRequest.UploadActions.VIDEOCV)
                .setMessage("You have a pending offline pitch, do you want to upload to now? if you cancel it, it will be cleared from system");
        HashMap <String,Object> map=new HashMap<>();
        map.put("resource", s3UploadRequest.getVideoCv());
        NetworkCall.CallAPI(this,Utility.getService(Constants.DreamFactory.URL).postVideoCV(map),this,false,
                    Object.class, Constants.Endpoints.VIDEOCV);
        getVideoCVs();
//            onBackPressed();
    }
    private void postImage(String name,String image){
        isImage=true;
        RequestBody b_image=RequestBody.create(MediaType.parse("text/plain"),image);
        RequestBody b_image_name=RequestBody.create(MediaType.parse("text/plain"),name);
//        getDialog().show();
        NetworkCall.CallAPI(getApplicationContext(),getViewModel().getService(Constants.POST_IMAGE_URL).postPicture(b_image,b_image_name),this,false,Object.class,Endpoints.POST_DP);
    }

    @Override
    public void onPicClick(String videocvpath) {
//        if (arraylist.get(position).getTitle().startsWith("Pic")) {
//            storageReference = FirebaseStorage.getInstance().getReference().child(videocvpath + ".jpg");
//            try {
//                File localFile = File.createTempFile("tempfile", ".jpg");
//                storageReference.getFile(localFile)
//                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
////                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                                Toast.makeText(MyVideoCVActivity.this, "successuyt", Toast.LENGTH_SHORT).show();
//
////                                        Uri abc = Uri.parse(arraylist.get(position).getVideocv());
////                                        ImageView image = new ImageView(context);
////                                        image.setImageBitmap(bitmap);
////
////                                        AlertDialog.Builder builder =
////                                                new AlertDialog.Builder(context).
////                                                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
////                                                            @Override
////                                                            public void onClick(DialogInterface dialog, int which) {
////                                                                dialog.dismiss();
////                                                            }
////                                                        }).
////                                                        setView(image);
////                                        builder.create().show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MyVideoCVActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//        }
    }

    private void validateInputs() {
        if (videoCv.getPath()==null){
            Utility.showSnackBar(binding.root,"Select a Video to upload");
        }
        else if (Utility.getString(binding.bottomsheet.edittext.et).trim().isEmpty()){
            Utility.showSnackBar(binding.root,"Create a Cv Title");
        }
        else {
            videoCv.setTitle(Utility.getString(binding.bottomsheet.edittext.et).trim());
            videoCv.setUsername(getViewModel().getLoggedUser().getUsername());
            S3UploadRequest();
        }
        binding.root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        binding.root.getWindowVisibleDisplayFrame(r);
                        int screenHeight =   binding.root.getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;

                        Log.d("TAG", "keypadHeight = " + keypadHeight);

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            if (!isKeyboardShowing) {
                                isKeyboardShowing = true;
                                onKeyboardVisibilityChanged(true);
                            }
                        }
                        else {
                            // keyboard is closed
                            if (isKeyboardShowing) {
                                isKeyboardShowing = false;
                                onKeyboardVisibilityChanged(false);
                            }
                        }
                    }
                });
    }
    boolean isKeyboardShowing = false;
    void onKeyboardVisibilityChanged(boolean opened) {
    }
    // UploadImage method
    private void uploadImage() {

        String randomId = UUID.randomUUID().toString();
        videoCv.setPath(uri.getPath());
        videoCv.setTitle("Picture CV");
        videoCv.setVideocv(randomId);
        videoCv.setUsername(getViewModel().getLoggedUser().getUsername());
        final StorageReference reference = storage.getReference().child("images/"+randomId);
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.getReference().child("images/"+randomId)
                                .setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });


    }
}