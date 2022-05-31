package com.senarios.simxx.fragments.homefragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.hdev.common.datamodels.RealPathUtil;
import com.hdev.common.datamodels.S3UploadRequest;
import com.hdev.common.datamodels.UserType;
import com.hdev.common.datamodels.Users;
import com.hdev.common.exoplayer.VideoPlayerActivity;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.model.QBEvent;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.ImageUtil;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MyVideoCVActivity;
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.PaymentTestActivity;
import com.senarios.simxx.activities.WithdrawActivity;
import com.senarios.simxx.databinding.FragmentEditProfileBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.services.AmazonS3UploadService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.video_trim.TrimmerActivity;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

public class EditProfileFragment extends BaseFragment implements Constants.AUTH, View.OnClickListener, Constants, Constants.Messages, Constants.DreamFactory, FragmentTags, QBEntityCallback<QBEvent> {
    private ProgressDialog pd;
    private String[] permissions = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final int CODE = 1000;
    private final int SETTING_CODE = 101;
    private final int GALLERY_PICTURE = 2;
    private final int TAKE_PICTURE = 1;
    private String image, image_name;
    private Users user;
    private String auth, verifier;
    private S3UploadRequest s3UploadRequest = new S3UploadRequest();
    private FragmentEditProfileBinding binding;
    String regex = "http(s)?:\\/\\/([\\w]+\\.)?linkedin\\.com\\/in\\/[A-z0-9_-]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri uri;
    private Activity mActivity;

    @Override
    public void onResume() {
        super.onResume();

        if (getViewModel() != null && binding.currentBalanceText != null) {
            binding.currentBalanceText.setText("Balance \n £ " + getViewModel().getLoggedUser().getCredit());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        binding = DataBindingUtil.bind(view);


        //
        pd = Utility.setDialogue(getContext());

        storage = FirebaseStorage.getInstance();
//        binding.setLinkeldnProfileLink.setEnabled(false);
        //clicklistners
        binding.backIcon.setOnClickListener(this);
        binding.profilePicture.setOnClickListener(this);
        binding.saveBtn.setOnClickListener(this);
        binding.withdrawBtn.setOnClickListener(this);
        binding.addPaymentBtn.setOnClickListener(this);
//        binding.infoGreyButton.setOnClickListener(this);
        binding.btnWatchVideo.setOnClickListener(this);
//        binding.btnUpload.setOnClickListener(this);
//        binding.typeSelector.setLifecycleOwner(this);
        binding.note.setOnClickListener(this);

        //set values

        binding.name.setText("" + getViewModel().getLoggedUser().getName());
        binding.broadcasterRate.setText("" + getViewModel().getLoggedUser().getRate());

      /*  if (getViewModel().getLoggedUser().getSkills().equals(UserType.RemoteWorker.toString()))
        {
            binding.broadcasterRate.setText("0");
            binding.broadcasterRate.setEnabled(false);
        }*/
        if (getViewModel().getLoggedUser().getSkills() != null && getViewModel().getLoggedUser().getSkills().equalsIgnoreCase(UserType.RemoteWorker.toString())) {
//            binding.typeSelector.selectItemByIndex(1);
//            binding.groupCv.setVisibility(View.VISIBLE);
            binding.btnWatchVideo.setVisibility(View.VISIBLE);

        } else {
//            binding.typeSelector.selectItemByIndex(0);
        }

        binding.editProfileProgress.setVisibility(View.VISIBLE);
        Glide.with(this).load(DreamFactory.GET_IMAGE_URL + getViewModel().getLoggedUser().getEmail() + ".png")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.h2pay2)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        binding.editProfileProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.editProfileProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(binding.profilePicture);

        //profile image
//        SharedPreferences editor = getContext().getSharedPreferences("myProfilee", Context.MODE_PRIVATE);
//        String profileid = editor.getString("userProfile", "");
//        database = FirebaseDatabase.getInstance();
//        database.getReference().child("profileimages/" + profileid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (mActivity == null) {
//                            return;
//                        }
//                        String imagee = snapshot.getValue(String.class);
//                        Glide.with(mActivity).load(imagee)
//                                .into(binding.profilePicture);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });


// +getViewModel().getLoggedUser().getLink() was removed from below line
        binding.setLinkeldnProfileLink.setText(getViewModel().getLoggedUser().getLink());
//        binding.setLinkeldnProfileLink.setText(getString(R.string.linkedin_link));

//        binding.typeSelector.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, o) -> {
//                    if ( o.equalsIgnoreCase(UserType.RemoteWorker.toString())){
//                binding.groupCv.setVisibility(View.VISIBLE);
//            }
//            else{
//                binding.groupCv.setVisibility(View.GONE);
//            }
//
//        });

        if (getArguments() != null) {
            auth = getArguments().getString(AUTH.AUTH_TOKEN);
            verifier = getArguments().getString(AUTH.AUTH_VERIFIER);
            getuserAccessToken(auth, verifier);
        }

        user = getViewModel().getLoggedUser();

        return view;
    }

    private void getuserAccessToken(String auth, String verifier) {
        pd.show();
        getViewModel().getServiceforTwitter(Twitter.BASE_URL)
                .getAccess_Token(verifier, auth, Twitter.consumerKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        pd.dismiss();
                        if (response.isSuccessful()) {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    Toast.makeText(getContext(), "Successful", Toast.LENGTH_SHORT).show();
                                    String s = response.body();
                                    String user_Auth = s.split("&")[0].split("=")[1];
                                    String user_secret = s.split("&")[1].split("=")[1];
                                    getViewModel().getSharedPreference().edit().putString(USER_AUTH_TOKEN, user_Auth).apply();
                                    getViewModel().getSharedPreference().edit().putString(USER_AUTH_SECRET, user_secret).apply();


                                } else {
                                    Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        pd.dismiss();
                        Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public void onClick(View v) {
        int id = v.getId();


        if (id == R.id.back_icon) {
            if (getParentFragment() != null) {
                getParentFragment().getChildFragmentManager().popBackStack();
            }
        } else if (id == R.id.note) {
            Utility.makeFilePublic(requireContext(), null, S3Constants.OTHER + "/" + S3Constants.LINK);
        } else if (id == binding.profilePicture.getId()) {
            com.github.drjacky.imagepicker.ImagePicker.Companion.with(getActivity())
                    .crop()
                    .cropOval()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(101);

//            if (hasPermissions(getContext(),permissions)){
//                Utility.showImagePicker(requireContext());
//            }
//            else{
//                requestPermissions(permissions,CODE);
//            }
        } else if (id == binding.saveBtn.getId()) {
            checks();
        }
//        else if (id==binding.twitterBtn.getId()){
//            if (getViewModel().getSharedPreference().getString(USER_AUTH_TOKEN,null)!=null){
//                Toast.makeText(getContext(), "Twitter Account Already Connected!", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                getTwitterAccesstoken();
//            }
//
//        }
//        else if (id==binding.linkedinBtn.getId()){
//            Toast.makeText(getContext(), "Linked Connected Successfully!", Toast.LENGTH_SHORT).show();
//        }
        else if (id == binding.btnWatchVideo.getId()) {
            startActivity(new Intent(requireContext(), MyVideoCVActivity.class));
//            if (getViewModel().getLoggedUser().getVideocv()!=null ) {
//                Utility.makeFilePublic(requireContext(),null,S3Constants.VIDEO_CV_FOLDER+"/"+getViewModel().getLoggedUser().getUsername()+ OfflineStreamActivity.EXT);
//            }
//            else{
//                Toast.makeText(requireContext(), "Please upload a Video CV first!", Toast.LENGTH_SHORT).show();
//            }
        }
//        else if (id==binding.btnUpload.getId()){
//            Utility.showVidepPicker(requireActivity());
//        }

        else if (id == binding.withdrawBtn.getId()) {
//            Toast.makeText(getContext(), "Coming soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), WithdrawActivity.class));
        } else if (id == binding.addPaymentBtn.getId()) {
//            Toast.makeText(getContext(), "Coming soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), PaymentTestActivity.class));
        }
//        else if (binding.infoGreyButton.getId()==id){
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LinkedIn.FindProfile));
//            startActivity(browserIntent);
//        }

    }

    private void checks() {
//    if ( binding.typeSelector.getSpinnerAdapter().getSpinnerView().getText().toString().equalsIgnoreCase(BROADCASTER) || Utility.getString(binding.broadcasterRate).isEmpty() || Integer.parseInt(Utility.getString(binding.broadcasterRate)) < 0 || Integer.parseInt(Utility.getString(binding.broadcasterRate)) < 20){
//    if (  Utility.getString(binding.broadcasterRate).isEmpty() || Integer.parseInt(Utility.getString(binding.broadcasterRate)) < 0 || Integer.parseInt(Utility.getString(binding.broadcasterRate)) < 20){
//        binding.broadcasterRate.setError("Please enter Broadcast Rate and should be atleast 20");
//        binding.broadcasterRate.requestFocus();
//    }
//    else
//    if (Utility.getString(binding.setLinkeldnProfileLink).isEmpty() || !Utility.getString(binding.setLinkeldnProfileLink).startsWith("https://www.linkedin.com/in") || !Utility.getString(binding.setLinkeldnProfileLink).matches(regex)) {
//
//                binding.setLinkeldnProfileLink.setError("Please Enter Proper Link");
//                binding.setLinkeldnProfileLink.requestFocus();
//    } else
        if (Utility.getString(binding.name).isEmpty()) {
            binding.name.setError("Please Enter Profile Name");
            binding.name.requestFocus();
        } else {

//        updatePic();

            user.setName(Utility.getString(binding.name).trim());
            user.setRate("1");
//        user.setSkills( binding.typeSelector.getSpinnerAdapter().getSpinnerView().getText().toString());
            String[] arr = Utility.getString(binding.setLinkeldnProfileLink).split("/");
            user.setLink(arr[arr.length - 1]);
            updateUser(user);

        }


    }

    private void updatePic() {
        SharedPreferences editor = getContext().getSharedPreferences("myProfilee", Context.MODE_PRIVATE);
        String profileid = editor.getString("userProfile", "");
        final StorageReference reference = storage.getReference().child("profileimages/" + profileid);
        reference.putFile(Uri.parse(String.valueOf(uri))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.getReference().child("profileimages/" + profileid)
                                .setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                            }
                        });
                    }
                });
            }
        });
    }


    private void updateUser(Users user) {
        if (Utility.isNetworkAvailable(requireContext())) {
            pd.show();
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", user);
            getViewModel().getService(URL).updateUser(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        getViewModel().setPreferences(SharedPreference.USER, user);
                                        getHomeContainer().OnChange(new ProfileFragment(), PROFILE);
                                    } else {
                                        Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(getContext(), NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }


    //functions related to image upload
    private void startDialog() {
        AlertDialog.Builder myAlertDialog;
        myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    try {
                        Intent i = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, GALLERY_PICTURE);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Please Install appropriate app to handle this action", Toast.LENGTH_SHORT).show();
                    }

                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> {
                    try {
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivityForResult(intent, TAKE_PICTURE);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Please Install appropriate app to handle this action", Toast.LENGTH_SHORT).show();
                    }

                });
        myAlertDialog.show();
    }

    private void postImage(String name, String image) {

        if (Utility.isNetworkAvailable(requireContext())) {
            pd.show();
            RequestBody b_image = RequestBody.create(MediaType.parse("text/plain"), image);
            RequestBody b_image_name = RequestBody.create(MediaType.parse("text/plain"), name);
            getViewModel().getService(Constants.POST_IMAGE_URL)
                    .postPicture(b_image, b_image_name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        if (response.body().get("boolean").getAsBoolean())
                                            Log.v("image_response", response.toString());
                                        JSONObject object = new JSONObject();
                                        try {
                                            object.put(NotificationKeys.User.toString(), getViewModel().getLoggedUser().toString());
                                            object.put(NotificationKeys.message.toString(), "SimpleData");
                                            object.put(NotificationKeys.Type.toString(), NotificationType.PROFILEPICTURE);
                                            Utility.sendNotification(false, Integer.parseInt(user.getQbid()), object, EditProfileFragment.this);
                                        } catch (JSONException e) {
                                            Utility.showELog(e);
                                        }
                                      /*  Utility.sendNotification(true,0, Utility.sendRefreshNotification(), new QBEntityCallback<QBEvent>() {
                                            @Override
                                            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                                                Utility.showLog("notification sent");
                                            }

                                            @Override
                                            public void onError(QBResponseException e) {
                                                Utility.showELog( e);
                                            }

                                        });*/

                                        Toast.makeText(getContext(), "Image Posted Successfully", Toast.LENGTH_SHORT).show();
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
                            pd.dismiss();
                            Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {
            Toast.makeText(getContext(), "Please Enable Data/Wifi", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (requireContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            Context context = getActivity();
            String path = RealPathUtil.getRealPath(context, uri);
            Log.wtf("pathimage", path);
//            isImage=true;
            binding.profilePicture.setImageURI(Uri.parse(path));
            image_name = getViewModel().getLoggedUser().getEmail() + ".png";
            try {
                postImage(image_name, ImageUtil.convert(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                binding.profilePicture.setImageBitmap(photo);
                photo = resize(photo, 75, 75);
                Uri uri = getImageUri(requireContext(), photo);
                image_name = getViewModel().getLoggedUser().getUsername() + ".png";
                /*new File(getRealPathFromURI(uri)).getName();*/
                image = ImageUtil.convert(photo);
                postImage(image_name, image);
            }

        } else if (requestCode == GALLERY_PICTURE && data != null && data.getData() != null) {
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                photo = resize(photo, 75, 75);
                binding.profilePicture.setImageBitmap(photo);
                image_name = getViewModel().getLoggedUser().getUsername() + ".png";
                /*getFileName(data.getData());*/
                image = ImageUtil.convert(photo);
                postImage(image_name, image);

            } catch (IOException e) {
                Toast.makeText(getContext(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        if (resultCode == SETTING_CODE) {
            if (hasPermissions(getContext(), permissions)) {
                startDialog();
            }
        }


        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && mPaths.size() > 0) {
                CommonUtils.cropImage(requireContext(), new File(mPaths.get(0)), this);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    Uri resultUri = result.getUri();
                    binding.profilePicture.setImageURI(resultUri);
                    try {
                        postImage(image_name = getViewModel().getLoggedUser().getUsername() + ".png", ImageUtil.convert(resize(MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), resultUri), 200, 200)));

                    } catch (Exception e) {
                        //handle exception
                    }


                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
                if (mPaths != null && mPaths.size() > 0) {
                    Utility.showLog("Video Success" + mPaths.get(0));
                    s3UploadRequest.setPath(mPaths.get(0));
                    startActivityForResult(new Intent(requireContext(), TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, mPaths.get(0)), TrimmerActivity.CODE);

                }
            }
        } else if (requestCode == TrimmerActivity.CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String path = data.getStringExtra(TrimmerActivity.EXTRA_VIDEO_PATH);
                s3UploadRequest.setPath(path);
                String duration = Utility.convertMillieToHMmSs(Utility.getVideoDuration(path));
                Utility.showLog("Video Duration" + duration);//use this duration
                if (isVideoDuration(duration)) {
                    Utility.showLog("trimmed video " + path);
                    if (path != null) {
                        Utility.getAlertDialoge(requireContext(), "Upload Video", "You sure you want to upload this video?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    dialog.dismiss();
                                    s3UploadRequest.setKey(user.getUsername());
                                    s3UploadRequest.setS3_PATH(Constants.S3Constants.VIDEO_CV_FOLDER + "/" + s3UploadRequest.getKey() + OfflineStreamActivity.EXT);
                                    s3UploadRequest.setAction(S3UploadRequest.UploadActions.VIDEOCV);
                                    Intent intent = new Intent(requireContext(), AmazonS3UploadService.class);
                                    intent.putExtra(S3_REQUEST, s3UploadRequest);
                                    requireContext().startService(intent);

                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    dialog.dismiss();

                                })
                                .show();

                    }
                } else {
                    Utility.getAlertDialoge(requireContext(), "Video Not Supported", "Your offline pitch duration must be not more than 5 minutes.")
                            .setPositiveButton("Trim Previous Video Again", (dialog, which) -> {
                                dialog.dismiss();
                                startActivityForResult(new Intent(requireContext(), TrimmerActivity.class).putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, s3UploadRequest.getPath()), TrimmerActivity.CODE);
                            })
                            .setNegativeButton("Select New", (dialog, which) -> {
                                dialog.dismiss();
                                Utility.showVidepPicker(requireActivity());

                            })
                            .show();
                }


            }
        }

    }


    private void getTwitterAccesstoken() {
        if (Utility.isNetworkAvailable(getContext())) {
            pd.show();
            getViewModel().getServiceforTwitter(Twitter.BASE_URL)
                    .getRequestToken(Twitter.CallBackURL, Twitter.consumerKey)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new SingleObserver<Response<String>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        Log.v("requesttoken", response.body());
                                        String s = response.body();
                                        String auth = s.split("&")[0].split("=")[1];
                                        Bundle bundle = new Bundle();
                                        bundle.putString(AUTH.AUTH_TOKEN, auth);
                                        Fragment fragment = new TwitterWebView();
                                        fragment.setArguments(bundle);
                                        getHomeContainer().OnChange(fragment, FragmentTags.TWITTERWEBVIEW);


                                    } else {
                                        Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(getContext(), NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }


    }

    private void makeFilePublic(String file_id) {
        Single.fromCallable(() -> {
                    GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.S3Constants.S3_BUCKET, S3Constants.VIDEO_CV_FOLDER + "/" + file_id + OfflineStreamActivity.EXT);
                    return Utility.getS3Client(requireContext()).generatePresignedUrl(urlRequest);

                }
        ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<java.net.URL>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(URL s) {
                        Utility.showLog("url" + s.toString());
                        try {
                            startActivity(VideoPlayerActivity.newInstance(requireActivity(), s.toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utility.showLog("errorObject");

                    }
                });

    }


    private boolean isVideoDuration(String duration) {
        return (Integer.parseInt(duration.split(":")[0]) == 5 && Integer.parseInt(duration.split(":")[1]) == 0) ||
                (Integer.parseInt(duration.split(":")[0]) < 5);
    }

    @Override
    public void onSuccess(QBEvent qbEvent, Bundle bundle) {

    }

    @Override
    public void onError(QBResponseException e) {

    }


}
