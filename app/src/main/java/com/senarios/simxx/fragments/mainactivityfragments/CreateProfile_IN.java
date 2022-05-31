package com.senarios.simxx.fragments.mainactivityfragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.RealPathUtil;
import com.hdev.common.datamodels.UserType;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.ImageUtil;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MyVideoCVActivity;
import com.senarios.simxx.databinding.CreateProfileFragment2Binding;
import com.senarios.simxx.fragments.BaseFragment;
import com.theartofdev.edmodo.cropper.CropImage;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.File;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateProfile_IN extends BaseFragment implements ApiResponse,View.OnClickListener{
    private static final int SETTING_CODE = 1000;
    private CreateProfileFragment2Binding binding;
    private static final int CODE = 41;
    private String skills=null;
    private boolean isImage=false;
    private String[] permissions={"android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};
    Uri uri;



    public CreateProfile_IN() {
        // Required empty public constructor
    }




    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.create_profile_fragment2,container,false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();

        binding.nameEditText.setText(getViewModel().getSharedPreference().getString(Fullname,""));

        binding.uploadPicture.setOnClickListener(this);
        binding.recruiter.setOnClickListener(this);
        binding.jobHunter.setOnClickListener(this);

        binding.doneButton.setOnClickListener(this);

        binding.broadcaster.setOnClickListener(this);
        binding.Viewer.setOnClickListener(this);


    }

    private void checks() {
        SharedPreferences editor = getContext().getSharedPreferences("my", Context.MODE_PRIVATE);
        String acc_type_check = editor.getString(Constants.SharedPreference.ACCOUNT_TYPE, "");
        if (!isImage){
            Toast.makeText(getContext(), "Please Upload a cheeky snap of yourself", Toast.LENGTH_SHORT).show();
        }
        else if (Utility.getString(binding.nameEditText).isEmpty()){
            binding.nameEditText.setError("Please enter your name");
        } else if (acc_type_check.equals("")) {
            Toast.makeText(getContext(), "Please Select your account type", Toast.LENGTH_SHORT).show();
        }
        // Single user type so dont need the check
       /* else if (skills==null){
            Toast.makeText(getContext(), "Choose your User Type!", Toast.LENGTH_SHORT).show();
        }*/
        else {
            getViewModel().getSharedPreference().edit().putString(Fullname, binding.nameEditText.getText().toString().trim()).apply();
            getViewModel().getSharedPreference().edit().putString(USER_TYPE, "RemoteWorker").apply();
            getActivityContainer().OnFragmentChange(new CreateProfile(), CREATE_PROFILE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Utility.hasPermissions(requireContext(),permissions)){
            Utility.showImagePicker(requireContext());
        }
        else if (Utility.shouldShowRationalPermission(requireActivity(),permissions)){
            showSettingDialogue();
        }
        else{
            requestPermissions(permissions,CODE);
        }


    }

    private void showSettingDialogue() {
        AlertDialog.Builder myAlertDialog;
        myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle("Permission Denied");
        myAlertDialog.setMessage(getResources().getString(R.string.setting_diaoge));
        myAlertDialog.setPositiveButton("Go to Setting",
                (arg0, arg1) -> openSettings());

        myAlertDialog.setNegativeButton("Nah, Im good",
                (arg0, arg1) -> {
            arg0.dismiss();


        });
        myAlertDialog.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTING_CODE);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && mPaths.size() > 0) {
                CommonUtils.cropImage(requireContext(), new File(mPaths.get(0)), this);
            }
        }

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            Context context = getActivity();
            String path = RealPathUtil.getRealPath(context, uri);
            Log.wtf("pathimage", path);
            isImage=true;
            binding.uploadPicture.setImageURI(Uri.parse(path));
            SharedPreferences.Editor editor = getContext().getSharedPreferences("myProfile", Context.MODE_PRIVATE).edit();
            editor.putString("uriProfile", String.valueOf(uri));
            editor.apply();

            try {
                postImage(getViewModel().getSharedPreference().getString(Email, "")+".png",ImageUtil.convert( MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver() , uri)));
            }
            catch (Exception e) {
                Utility.showELog(e);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result!=null) {
                    Uri resultUri = result.getUri();
                    binding.uploadPicture.setImageURI(resultUri);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("myProfile", Context.MODE_PRIVATE).edit();
                    editor.putString("uriProfile", String.valueOf(resultUri));
                    editor.apply();
                    try {
                        postImage(getViewModel().getSharedPreference().getString(Linkedin_ID, "")+".png",ImageUtil.convert( MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver() ,resultUri)));
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


    private void postImage(String name,String image){
        isImage=true;
        RequestBody b_image=RequestBody.create(MediaType.parse("text/plain"),image);
        RequestBody b_image_name=RequestBody.create(MediaType.parse("text/plain"),name);
        getDialog().show();
        NetworkCall.CallAPI(requireActivity(),getViewModel().getService(Constants.POST_IMAGE_URL).postPicture(b_image,b_image_name),this,false,Object.class,Endpoints.POST_DP);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // Single user type
            case R.id.broadcaster:
                skills= UserType.Trainer.toString();
                binding.broadcaster.setBackground(getResources().getDrawable(R.drawable.background_border_blue));
                binding.broadcasterImage.setImageDrawable(getResources().getDrawable(R.drawable.broadcast_blue_2));
                binding.broadcasterText.setTextColor(getResources().getColor(R.color.colorPrimary));
                binding.Viewer.setBackground(getResources().getDrawable(R.drawable.background_border_grey));
                binding.ViewerImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_viewer_grey,requireContext().getTheme()));
                binding.ViewerImageText.setTextColor(getResources().getColor(R.color.DarkGray));
                binding.usernameWarn.setText("I would like to share videos to promote training or traineeships");
                break;

            case R.id.Viewer:
                skills= UserType.RemoteWorker.toString();
                binding.broadcaster.setBackground(getResources().getDrawable(R.drawable.background_border_grey));
                binding.broadcasterImage.setImageDrawable(getResources().getDrawable(R.drawable.broadcasterimagegrey));
                binding.broadcasterText.setTextColor(getResources().getColor(R.color.DarkGray));
                binding.Viewer.setBackground(getResources().getDrawable(R.drawable.background_border_blue));
                binding.ViewerImage.setImageDrawable(getResources().getDrawable(R.drawable.group_));
                binding.ViewerImageText.setTextColor(getResources().getColor(R.color.colorPrimary));
                binding.usernameWarn.setText("I am looking for remote training or coaching");
                binding.usernameWarn.setMovementMethod(new ScrollingMovementMethod());
                break;

            case R.id.upload_picture:
                com.github.drjacky.imagepicker.ImagePicker.Companion.with(getActivity())
                        .crop()
                        .cropOval()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start(101);
//                if (Utility.hasPermissions(requireContext(),permissions)){
//                    Utility.showImagePicker(requireContext());
//                }
//                else{
//                    requestPermissions(permissions,CODE);
//                }
                break;
            case R.id.done_button:
                checks();
                break;
            case R.id.recruiter:
                if (binding.jobHunter.getCurrentTextColor()==Color.GREEN) {
                    binding.jobHunter.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                SharedPreferences.Editor editor = getContext().getSharedPreferences("my", Context.MODE_PRIVATE).edit();
                editor.putString(Constants.SharedPreference.ACCOUNT_TYPE, "Recruiter");
                editor.apply();
//                getViewModel().getSharedPreference().edit().putString(Constants.SharedPreference.ACCOUNT_TYPE, "Recruiter");
                binding.recruiter.setTextColor(Color.GREEN);
                break;
            case R.id.job_hunter:
                if (binding.recruiter.getCurrentTextColor()==Color.GREEN) {
                    binding.recruiter.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                SharedPreferences.Editor editor1 = getContext().getSharedPreferences("my", Context.MODE_PRIVATE).edit();
                editor1.putString(Constants.SharedPreference.ACCOUNT_TYPE, "Job hunter");
                editor1.apply();
//                getViewModel().getSharedPreference().edit().putString(SharedPreference.ACCOUNT_TYPE, "Job hunter");
                binding.jobHunter.setTextColor(Color.GREEN);
                break;
        }
    }


    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        if (endpoint.equalsIgnoreCase(Endpoints.POST_DP)){
            getDialog().dismiss();
           Toast.makeText(requireActivity(), "Image Uploaded", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        getDialog().dismiss();
    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        getDialog().dismiss();
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        getDialog().dismiss();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (getDialog().isShowing()){
            getDialog().dismiss();
        }
    }
}
