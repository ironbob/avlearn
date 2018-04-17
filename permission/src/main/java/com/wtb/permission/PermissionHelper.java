package com.wtb.permission;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;


import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zeyqiao
 * @date 2017/9/7
 */

public class PermissionHelper {

    private static final String TAG = "PermissionHelper";

    private boolean mIsFinishActivity = true;
    private boolean mIsAllRequiredGranted = true;

    private static List<PermissionWrapper> permissionList = new ArrayList<PermissionWrapper>();
    private List<PermissionWrapper> needPermissionList = new ArrayList<>();


    //权限列表，如果需要加权限，一定要清楚的知道，在哪个页面加，是否依赖前面Activity的权限
    static {
        permissionList.add(new PermissionWrapper(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0, "读写存储卡"));
        permissionList.add(new PermissionWrapper(Manifest.permission.READ_PHONE_STATE, 1, "读取手机状态"));
        permissionList.add(new PermissionWrapper(Manifest.permission.CAMERA, 2, "摄像头"));
        permissionList.add(new PermissionWrapper(Manifest.permission.RECORD_AUDIO, 3, "录音"));
        permissionList.add(new PermissionWrapper(Manifest.permission.ACCESS_COARSE_LOCATION, 4, "地理位置", false));
        permissionList.add(new PermissionWrapper(Manifest.permission.READ_EXTERNAL_STORAGE, 5, "存储卡"));
    }

    private IPermission mIPermission;

    public PermissionHelper(IPermission iPermission) {
        this.mIPermission = iPermission;
    }

    public void checkPermissionState(Class cls) {
        Annotation permissionAnnotation = cls.getAnnotation(PermissionRequired.class);
        if (permissionAnnotation != null) {
            PermissionRequired permissionRequired = (PermissionRequired) permissionAnnotation;
            String[] permissions = permissionRequired.permession();
            mIsFinishActivity = permissionRequired.isFinishActivity();
            if (permissions == null || permissions.length == 0) {
                return;
            }

            needPermissionList.clear();
            for (String permission : permissions) {
                for (PermissionWrapper permissionWrapper : permissionList) {
                    if (permissionWrapper.getName().equals(permission)) {
                        needPermissionList.add(permissionWrapper);
                    }
                }
            }

            for (PermissionWrapper permissionWrapper : needPermissionList) {
                if (!mIPermission.isPermissionGranted(permissionWrapper.getName()) && permissionWrapper.isShow()) {
                    mIsAllRequiredGranted = false;
                    requestPermission(permissionWrapper.getLevel());
                    break;
                }
            }

            if (mIsAllRequiredGranted) {
                mIPermission.onPermissionGranted();
            }
        } else {
            mIPermission.onPermissionGranted();
        }
    }

    public void checkPermissionState(String[] permissions,boolean isFinishActivity){
        mIsFinishActivity = isFinishActivity;
        if (permissions == null || permissions.length == 0) {
            mIPermission.onPermissionGranted();
            return;
        }

        needPermissionList.clear();
        for (String permission : permissions) {
            for (PermissionWrapper permissionWrapper : permissionList) {
                if (permissionWrapper.getName().equals(permission)) {
                    needPermissionList.add(permissionWrapper);
                }
            }
        }

        for (PermissionWrapper permissionWrapper : needPermissionList) {
            if (!mIPermission.isPermissionGranted(permissionWrapper.getName()) && permissionWrapper.isShow()) {
                mIsAllRequiredGranted = false;
                requestPermission(permissionWrapper.getLevel());
                break;
            }
        }

        if (mIsAllRequiredGranted) {
            mIPermission.onPermissionGranted();
        }
    }

    private void requestPermission(int level) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mIPermission.requestPermission(permissionList.get(level).getName(), level);
        }
    }


    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults, Activity activity) {
        if (activity == null) {
            return;
        }

        String tip = "";
        if (requestCode >= 0 && requestCode < permissionList.size()) {
            tip = permissionList.get(requestCode).getTip();
        } else {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "用户拒绝了打开" + tip + "权限");

            boolean shouldShowRationale = false;

            shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, permissionList.get(requestCode).getName());

//            QQCustomDialog QQCustomDialog = NowDialogUtil.createDialog(activity, "", "你已拒绝读取" + tip + "权限，请到应用权限中打开", activity.getString(R.string.permission_request_cancel), activity.getString(R.string.permission_request_goto_setting), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (dialog != null) {
//                                dialog.dismiss();
//                            }
//
//                            if (permissionList.get(requestCode).isRequired()) {
//                                //finish
//                                requiredPermissionNotAllowed();
//                            } else {
//                                //continue
//                                checkRestPermission(requestCode);
//                            }
//                        }
//                    },
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            showAppDetail();
//                            dialog.dismiss();
//
//                            if (permissionList.get(requestCode).isRequired()) {
//                                //finish
//                                requiredPermissionNotAllowed();
//                            } else {
//                                //continue
//                                checkRestPermission(requestCode);
//                            }
//                        }
//                    });
//            QQCustomDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//                        if (permissionList.get(requestCode).isRequired()) {
//                            //finish
//                            requiredPermissionNotAllowed();
//                        } else {
//                            //continue
//                            checkRestPermission(requestCode);
//                        }
//                    }
//                    return false;
//                }
//            });
//
//            QQCustomDialog.show();
        } else {
            checkRestPermission(requestCode);
        }

    }

    /**
     * 依次检查后面的权限有没有获得。
     * @param requestCode 当前申请的权限
     */
    private void checkRestPermission(int requestCode) {
        mIsAllRequiredGranted = true;
        PermissionWrapper nextNeededPermission = getNextNeededPermission(permissionList.get(requestCode));
        while (nextNeededPermission != null) {
            if (!mIPermission.isPermissionGranted(nextNeededPermission.getName())) {
                requestPermission(nextNeededPermission.getLevel());
                mIsAllRequiredGranted = false;
                break;
            }
            nextNeededPermission = getNextNeededPermission(nextNeededPermission);
        }

        if (mIsAllRequiredGranted) {
            mIPermission.onPermissionGranted();
        }
    }

    private PermissionWrapper getNextNeededPermission(PermissionWrapper permissionWrapper) {
        for (int i = 0; i < needPermissionList.size(); i ++) {
            if (needPermissionList.get(i).equals(permissionWrapper)) {
                if (i + 1 < needPermissionList.size()) {
                    return needPermissionList.get(i + 1);
                }
                break;
            }
        }
        return null;
    }

    /**
     * 必需的权限没有被允许，关闭当前界面。
     */
    private void requiredPermissionNotAllowed() {
        if (mIsFinishActivity) {
            mIPermission.requiredPermissionNotAllowed();
        }
    }

    private void showAppDetail() {
        mIPermission.showAppDetail();
    }

    public boolean isAllRequiredGranted() {
        return mIsAllRequiredGranted;
    }


    public static void showNotifyPermissionWarnigDialog(Activity activity){
    }

}
