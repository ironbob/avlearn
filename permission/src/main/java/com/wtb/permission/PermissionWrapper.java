package com.wtb.permission;

/**
 * Created by Administrator on 2016/11/9.
 */
public class PermissionWrapper {
    //the name of the permission
    private String name;
    //indicate the priority of the permission.
    //high level permissions depends on low permissions.
    private int level;
    //提示的中文字，如“读写SD卡”
    private String tip;

    private boolean isRequired = true;

    public PermissionWrapper(String name, int level, String tip, boolean isRequired){
        this.name = name;
        this.level = level;
        this.tip = tip;
        this.isRequired = isRequired;
    }

    public PermissionWrapper(String name, int level, String tip){
        this(name, level, tip, true);
    }

    public PermissionWrapper(String name){
        this.name = name;
    }


    public String getName(){
        return this.name;
    }

    public int getLevel(){
        return this.level;
    }

    public String getTip(){
        return this.tip;
    }

    public boolean isRequired() {
        return this.isRequired;
    }

    public boolean isShow(){
        if(isRequired){
            return true;
        }else {
            //非必要的权限只弹一次
//            boolean isShow = StorageCenter.getBoolean(name,true);
//            if(isShow) {
//                StorageCenter.putBoolean(name, false);
//            }
            return true;
        }
    }


    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermissionWrapper))
            return false;
        if (obj == this)
            return true;
        return this.name.equals(((PermissionWrapper) obj).name);
    }
}
