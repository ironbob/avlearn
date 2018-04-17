package com.wtb.permission;

/**
 * @author zeyqiao
 * @date 2017/9/7
 */

public interface IPermission {

    boolean isPermissionGranted(String permissionName);
    void requestPermission(String permissionName, int requestCode);
    void onPermissionGranted();

    /**
     * 必需的权限没有被允许，关闭当前界面。
     */
    void requiredPermissionNotAllowed();

    /**
     * 跳转到设置页面
     */
    void showAppDetail();
}
