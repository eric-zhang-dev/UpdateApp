package com.eric.update_app.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.eric.update_app.dialog.UpdateDialog;


public class UpdateAppHelper {

    private final String TAG = "UpdateAppUtils";
    public static final int CHECK_BY_VERSION_NAME = 1001;
    public static final int CHECK_BY_VERSION_CODE = 1002;
    public static final int DOWNLOAD_BY_APP = 1003;
    public static final int DOWNLOAD_BY_BROWSER = 1004;

    private Activity activity;
    private int checkBy = CHECK_BY_VERSION_CODE;
    private int downloadBy = DOWNLOAD_BY_APP;
    private int serverVersionCode = 0;
    private String apkPath = "";
    private String serverVersionName = "";
    private boolean isForce = false; //是否强制更新
    private boolean isWifi = false;//wifi自动更新
    private int localVersionCode = 0;
    private String localVersionName = "";

    private UpdateAppHelper(Activity activity) {
        this.activity = activity;
        getAPPLocalVersion(activity);
    }

    public static UpdateAppHelper from(Activity activity) {
        return new UpdateAppHelper(activity);
    }

    public UpdateAppHelper checkBy(int checkBy) {
        this.checkBy = checkBy;
        return this;
    }

    public UpdateAppHelper apkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public UpdateAppHelper downloadBy(int downloadBy) {
        this.downloadBy = downloadBy;
        return this;
    }

    public UpdateAppHelper serverVersionCode(int serverVersionCode) {
        this.serverVersionCode = serverVersionCode;
        return this;
    }

    public UpdateAppHelper serverVersionName(String serverVersionName) {
        this.serverVersionName = serverVersionName;
        return this;
    }

    public UpdateAppHelper isWifi(boolean is) {
        if (is) {
            if (1 == getAPNType(activity)) {
                isWifi = true;
            } else {
                isWifi = false;
            }
        } else {
            isWifi = false;
        }
        return this;
    }

    public UpdateAppHelper isForce(boolean isForce) {
        this.isForce = isForce;
        return this;
    }

    private void getAPPLocalVersion(Context ctx) {
        PackageManager manager = ctx.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = info.versionName; // 版本名
            localVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update() {

        switch (checkBy) {
            case CHECK_BY_VERSION_CODE:
                if (serverVersionCode > localVersionCode) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
                }
                break;

            case CHECK_BY_VERSION_NAME:
                if (!serverVersionName.equals(localVersionName)) {
                    toUpdate();
                } else {
                    Log.i(TAG, "当前版本是最新版本" + serverVersionCode + "/" + serverVersionName);
                }
                break;
        }

    }

    private void toUpdate() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            realUpdate();
        } else {//申请权限
            Toast.makeText(activity, "请申请读写SD卡权限", Toast.LENGTH_SHORT).show();
        }

    }

    private void realUpdate() {
        if (isWifi) {
            if (downloadBy == DOWNLOAD_BY_APP) {
                DownloadApp.downloadForAutoInstall(activity, apkPath, "Sign_app-release.apk", serverVersionName);
            } else if (downloadBy == DOWNLOAD_BY_BROWSER) {
                DownloadApp.downloadForWebView(activity, apkPath);
            }
        } else {
            UpdateDialog.showDialog(activity, "检查更新", "发现新版本,是否下载更新?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (downloadBy == DOWNLOAD_BY_APP) {
                        DownloadApp.downloadForAutoInstall(activity, apkPath, "Sign_app-release.apk", serverVersionName);
                    } else if (downloadBy == DOWNLOAD_BY_BROWSER) {
                        DownloadApp.downloadForWebView(activity, apkPath);
                    }
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isForce) activity.finish();
                }
            });
        }
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     * 自定义
     *
     * @param context
     * @return
     */
    public static int getAPNType(Context context) {
        int netType = 0;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
            } else {
                netType = 2;
            }
        }
        return netType;
    }
}
