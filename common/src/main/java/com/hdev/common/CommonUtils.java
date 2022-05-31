package com.hdev.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.hdev.common.retrofit.DataService;
import com.hdev.common.retrofit.RetrofitClientInstance;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CommonUtils {
    private static final String TAG="Utility";

    public static boolean hasPermissions(Context context, String... permissions) {
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context,permission)){
                    return true;
                }
            }
        }
        return false;
    }

    public  static void cropImage(Context context, File imagePath, Fragment fragment){
        CropImage.activity(Uri.fromFile(imagePath))
                .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                .start(context, fragment);
    }
    public  static void cropImagee(Context context, File imagePath){
        CropImage.activity(Uri.fromFile(imagePath))
                .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                .start((Activity) context);
    }

    public static DataService getService(String url){
        return RetrofitClientInstance.getinstance(url).getRetrofitInstance().create(DataService.class);
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

    public static void showLog(String text) {
        Log.v(TAG,text);
    }

    public static NotificationManager getNotificationManager(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getApplicationContext().getSystemService(NotificationManager.class);
        }
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public static void showELog(Exception e) {
        Log.v(TAG,"" + e.getLocalizedMessage());
    }

    public static void openSettings(Activity context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivityForResult(intent, 386);
    }

    public static AlertDialog.Builder getAlertDialoge(Context context, String title, String message){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialog;

    }

    public static void showSettingDialog(Activity activity){
        getAlertDialoge(activity,"Permissions Required","You have disabled Permissions required by app to work properly, please go to setting and allow them")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSettings(activity);
                    }
                }).show();
    }

}
