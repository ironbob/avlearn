package com.wtb.permission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by koudleren on 2016/12/7.
 *
 */
public class BasePermissionActivity extends FragmentActivity implements IPermission{
    private static final String TAG = BasePermissionActivity.class.getSimpleName();
    public Set<PermissionWrapper> mInterceptSet =new HashSet<PermissionWrapper>();

    protected IPermissionResultInterface mPermissionResultInterface;

    private PermissionHelper mPermissionHelper;

    protected boolean mIsNeedPreLogin = true;

    public interface IPermissionResultInterface {
        void onPermissionGranted(boolean isPermissionGranted);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int apiLevel = Build.VERSION.SDK_INT;
        if(apiLevel >= 23) {
            mPermissionHelper = new PermissionHelper(this);
            checkPermissionState(this.getClass());
        } else {
            onPermissionGranted();
        }
    }

    public void addPermissionResultListener(IPermissionResultInterface permissionResultInterface){
        mPermissionResultInterface = permissionResultInterface;
    }

    public void checkPermissionState(Class cls) {
        if (mPermissionHelper != null) {
            mPermissionHelper.checkPermissionState(cls);
        }
    }

    public void checkPermissionState(String[] permissions,boolean isFinishActivity){
        if(mPermissionHelper != null){
            mPermissionHelper.checkPermissionState(permissions,isFinishActivity);
        }
    }

    /**
     * If the permission is granted.
     * @param permissionName the name of the permission.
     * @return true, is granted ; otherwise  is denied.
     */
    @TargetApi(23)
    public boolean isPermissionGranted(String permissionName) {
        return checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isAllRequiredGranted() {
        return mPermissionHelper == null || mPermissionHelper.isAllRequiredGranted();
    }

    /**
     * You may override it in sub-activity.
     * You SHOULD do everything related on permission.
     */
    public void onPermissionGranted(){
        if(mPermissionResultInterface != null){
            mPermissionResultInterface.onPermissionGranted(true);
        }
    }

    public void setPermissionIntercept(String permission) {
        mInterceptSet.add(new PermissionWrapper(permission));
    }
    public boolean isIntercepted(String permission) {
        return mInterceptSet.contains(new PermissionWrapper(permission));
    }

    @Override
    public void requestPermission(String permissionName, int requestCode) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{permissionName}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length == 0) {
            return;
        }
        if(isIntercepted(permissions[0])){
            return;
        }

        if(mCallback != null) {
            mCallback.onPermissionResult(permissions, grantResults);
            mCallback = null;
        }

        if (mPermissionHelper != null) {
            mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public final void requiredPermissionNotAllowed() {
        finish();
    }

    @Override
    public final void showAppDetail() {
//        AppUtil.showInstalledAppDetails(this, getPackageName());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean isSuc = true;
        try{
            isSuc = super.onKeyUp(keyCode, event);
        }catch (IllegalStateException e){
        }
        return isSuc;
    }

    private PermissionResult mCallback;

    public void requestPerms(String[] perms, PermissionResult callback) {
        mCallback = callback;
        if(mPermissionHelper != null) {
            mPermissionHelper.checkPermissionState(perms, false);
        }
    }

    public interface PermissionResult {
        void onPermissionResult(@NonNull final String[] permissions, @NonNull int[] grantResults);
    }
}
