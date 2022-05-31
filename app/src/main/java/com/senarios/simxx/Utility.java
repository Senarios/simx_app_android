package com.senarios.simxx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.bumptech.glide.Glide;
//import com.facebook.share.Share;
//import com.facebook.share.model.ShareLinkContent;
//import com.facebook.share.widget.ShareDialog;
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.NotificationUtils;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.hdev.common.exoplayer.VideoPlayerActivity;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;
import com.senarios.simxx.activities.JobVideoPlayerActivity;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.DataService;
import com.hdev.common.retrofit.RetrofitClientInstance;
import com.senarios.simxx.activities.MainActivity;
import com.senarios.simxx.adaptors.MyBroadcastsAdapter;
import com.senarios.simxx.fragments.MyBroadcastFragment;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import kotlin.text.Regex;


public abstract class Utility implements Constants.QB, Constants.SharedPreference {
    private static final String TAG = "Utility";
    private static ProgressDialog dialog;
    public static Dialog loadingDialog;


    public static QBEnvironment getDefaultEnvironment() {
        return QBEnvironment.PRODUCTION;
    }


    public static boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean shouldShowRationalPermission(Activity context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static InputFilter[] getFilter()
    {
        InputFilter EMOJI_FILTER = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int index = start; index < end; index++) {

                    int type = Character.getType(source.charAt(index));

                    if (type == Character.SURROGATE || type==Character.NON_SPACING_MARK
                            || type==Character.OTHER_SYMBOL) {
                        return "";
                    }
                }
                return null;
            }
        };
        return new InputFilter[]{EMOJI_FILTER};
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static ProgressDialog setDialogue(Context context) {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait..");
        return dialog;
    }


    public static String getString(EditText editText) {
        if (editText == null) {
            throw new NullPointerException("Given Edit Text is null");
        }
        return editText.getText().toString().trim();
    }

    public static void displayLocationSettingsRequest(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("asd", "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("asd", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult((Activity) context, 101);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("asd", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("asd", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(Preference, Preference_Mode);

    }

    public static Users getUser(Context context) {
        return new Gson().fromJson(context.getSharedPreferences(Preference, Preference_Mode).getString(USER, ""), Users.class);

    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }


    public static Integer getColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }


    public static AlertDialog.Builder getAlertDialoge(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        return alertDialog;

    }

    public static AlertDialog.Builder getStyledAlertDialog(Context context, int resource, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, resource);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        return alertDialog;

    }

    public static void showVidepPicker(Activity context) {
        new VideoPicker.Builder(context)
                .mode(VideoPicker.Mode.GALLERY)
                .directory(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)))
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();

    }

    public static void showImagePicker(Context context) {
        new ImagePicker.Builder((Activity) context)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .compressLevel(ImagePicker.ComperesLevel.HARD)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowMultipleImages(false)
                .enableDebuggingMode(true)
                .build();
    }

    public static void showLog(String text) {
        Log.v(TAG, text);
    }

    public static void showELog(Exception e) {
        Log.v(TAG, "" + e.getLocalizedMessage());
    }

    public static String convertMillieToHMmSs(Long millie) {
        if(millie != null){
            long seconds = (millie / 1000);
            long second = seconds % 60;
            long minute = (seconds / 60) % 60;
            long hour = (seconds / (60 * 60)) % 24;

            String result = "";
            if (hour > 0) {
                return String.format("%02d:%02d:%02d", hour, minute, second);
            } else {
                return String.format("%02d:%02d", minute, second);
            }
        } else {
            return null;
        }

    }

    public static Long getVideoDuration(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            retriever.getFrameAtTime(0);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);
            retriever.release();
            return timeInMillisec;
        } catch (Exception e) {
            return null;
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getThumbnail(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        return retriever.getFrameAtTime(0);
    }

    public static AmazonS3Client getS3Client(Context context) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(context.getResources().getString(R.string.AWSAppAccessKey), context.getResources().getString(R.string.AWSAppAccessSecretKey));
        return new AmazonS3Client(credentials, Region.getRegion(Regions.US_WEST_2), getclientconfiguration());
    }

    private static ClientConfiguration getclientconfiguration() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(300000);
        clientConfiguration.setSocketTimeout(300000);
        return clientConfiguration;
    }

    public static TransferUtility getTransferUtility(Context context, AmazonS3Client s3client) {
        return TransferUtility.builder().context(context).defaultBucket(Constants.S3Constants.S3_BUCKET).s3Client(s3client).build();

    }

    public static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(Constants.SharedPreference.Preference, Constants.SharedPreference.Preference_Mode);
    }

    public static Users getloggedUser(Context context) {
        return new Gson().fromJson(getSharedPreference(context).getString(Constants.SharedPreference.USER, ""), Users.class);
    }


    public static String getDateString() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(c);
    }

    public static void openVideoIntent(Context context, String link) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(link), "video/mp4");
        context.startActivity(Intent.createChooser(intent, "Complete action using"));
    }

    public static String getLocationFromLatLng(Context context, Double lat, Double lng) {
        Geocoder geocoder;
        String address = "";
        List<Address> yourAddresses;
        try {
            geocoder = new Geocoder(context, Locale.getDefault());
            yourAddresses = geocoder.getFromLocation(lat, lng, 1);
            address = yourAddresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static DataService getService(String url) {
        return RetrofitClientInstance.getinstance(url).getRetrofitInstance().create(DataService.class);

    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public static void showDefaultNotification(Context context, String title, String message) {
        NotificationUtils utilBuilder = new NotificationUtils(context);
        utilBuilder.initNotification()
                .setTitle(title)
                .setContent(message)
                .setPending(new Intent(context, MainActivity.class))
                .buildAndNotify();


    }


    private void getFramesCount(String path) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(path);
        metadataRetriever.getFrameAtTime();

    }

    public static void loadFullScreenImageView(@NotNull Context context, @NotNull String uri) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(uri);
        Intent fullImageIntent = new Intent(context, FullScreenImageViewActivity.class);
        fullImageIntent.putStringArrayListExtra(FullScreenImageViewActivity.URI_LIST_DATA, arrayList);
        fullImageIntent.putExtra(FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, 0);
        context.startActivity(fullImageIntent);
    }

    public static void deleteAllfiles(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            if (directory.listFiles().length > 0) {
                for (File file : directory.listFiles()) {
                    showLog("file deleted" + file.getPath());
                    file.delete();
                }
            }
        }
    }

    public static void makeFilePublic(Context context, Broadcasts broadcast, String key) {
        Single.fromCallable(() -> {
                    GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.S3Constants.S3_BUCKET, key);
                    return Utility.getS3Client(context).generatePresignedUrl(urlRequest);
                }
        ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<URL>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(URL s) {
                        Utility.showLog("urlhere" + s.toString());
                        try {
                            if (broadcast != null && broadcast.isjob()) {
                                context.startActivity(JobVideoPlayerActivity.newInstance(context, broadcast, s.toString()));
                            } else {
                                context.startActivity(VideoPlayerActivity.newInstance(context, s.toString()));
                            }
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

    public static boolean isLastItemDisplaying(RecyclerView recyclerView) {
        if (recyclerView.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            return lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1;
        }
        return false;
    }

    public static boolean checkLocationServices(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void deleteFileFromS3(Context context, String key) {
        Completable.fromAction(() -> getS3Client(context).deleteObject(Constants.S3Constants.S3_BUCKET, key)
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

    }

    public static String normalizeString(String s) {
        return s.replaceAll("_", "");
    }

    public static boolean normalizeStringEqual(String s1, String s2) {
        s1 = s1.replaceAll("_", "").toLowerCase();
        s2 = s2.replaceAll("_", "").toLowerCase();
        return s1.equalsIgnoreCase(s2);
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

    public static void sendNotification(Boolean isUniversal, int receiver, JSONObject object, QBEntityCallback<QBEvent> callback) {
        QBEvent event = new QBEvent();
        event.setEnvironment(getDefaultEnvironment());
        Integer[] ids = new Integer[1];
        ids[0] = receiver;
        StringifyArrayList<String> arrayList = new StringifyArrayList<>();
        arrayList.add("android");
        if (isUniversal) {
            event.setUserTagsAll(arrayList);
        } else {
            event.addUserIds(ids);
        }
        event.setNotificationType(QBNotificationType.PUSH);
        event.setMessage(object.toString());
        try {
            QBPushNotifications.createEvent(event).performAsync(callback);
        } catch (Exception e) {
            Log.d(TAG, "sendNotification: error here" + e.getMessage());
        }
        return;
    }

    public static void share(Context context, String link, String jobDescription) {
        String link2;
        link2 = "Please watch my video in the link, and download Scottish Health to video chat. \n" + link;
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, link2);
        Intent chooserIntent = Intent.createChooser(sharingIntent, "Open With");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        showLog(link2);
        context.startActivity(chooserIntent);
    }

    public static void sharePic(Context context, String link, String jobDescription) {
        String link2;
        link2 = "Please watch my picture cv in the link, and download Scottish Health to video chat. \n" + link;
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, link2);
        Intent chooserIntent = Intent.createChooser(sharingIntent, "Open With");
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        showLog(link2);
        context.startActivity(chooserIntent);
    }

    public static JSONObject sendRefreshNotification() {
        JSONObject object = new JSONObject();
        try {
            object.put(NotificationKeys.Type.toString(), NotificationType.PROFILEPICTURE.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static void showSnackBar(View view, String text) {
        Snackbar.make(view, text, 3000).show();
    }

    public static void refreshImages(Context context) {
        Single.fromCallable(() -> {
            Glide.get(context).clearDiskCache();
            Glide.get(context).clearMemory();
            return true;
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public static void openBrowser(Context context, String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(browserIntent);
    }

    public static boolean isIBANValid(String iban) {

        int IBAN_MIN_SIZE = 15;
        int IBAN_MAX_SIZE = 34;
        long IBAN_MAX = 999999999;
        long IBAN_MODULUS = 97;
        if (iban == null || iban.isEmpty()) {
            return false;
        }
        String trimmed = iban.trim();

        if (trimmed.length() < IBAN_MIN_SIZE || trimmed.length() > IBAN_MAX_SIZE) {
            return false;
        }

        String reformat = trimmed.substring(4) + trimmed.substring(0, 4);
        long total = 0;

        for (int i = 0; i < reformat.length(); i++) {

            int charValue = Character.getNumericValue(reformat.charAt(i));

            if (charValue < 0 || charValue > 35) {
                return false;
            }

            total = (charValue > 9 ? total * 100 : total * 10) + charValue;

            if (total > IBAN_MAX) {
                total = (total % IBAN_MODULUS);
            }
        }

        return (total % IBAN_MODULUS) == 1;
    }

    public static boolean isSortCodeValid(String sortCode) {
        Pattern pattern = Pattern.compile("^\\d\\d-\\d\\d-\\d\\d$");
        if (pattern.matcher(sortCode).matches()) {
            return true;
        } else
            return false;
    }

    public static String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }

    public static void show(Activity activity) {
        loadingDialog = new Dialog(activity);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    public static void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
