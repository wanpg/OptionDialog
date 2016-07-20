package com.snowpear.optiondialog;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Created by wangjinpeng on 16/1/29.
 */
public class OptionDialogManager {

    ArrayList<OptionDialog> optionDialogList;

    /**
     * 私有此构造方法，避免外部使用
     */
    private OptionDialogManager(){

    }

    public OptionDialogManager(Activity activity){
        optionDialogList = new ArrayList<>();
    }

    public void putDialog(String tag, OptionDialog dialog){
        optionDialogList.add(0, dialog);
    }

    public void removeDialog(OptionDialog dialog){
        int index = optionDialogList.indexOf(dialog);
        if(index >= 0) {
            optionDialogList.remove(index);
        }
    }

    public OptionDialog findDialogByTag(String tag){
        for(OptionDialog dialog : optionDialogList) {
            if(dialog != null && dialog.getAddTag() != null && dialog.getAddTag().equals(tag)){
                return dialog;
            }
        }
        return null;
    }

    public boolean backDialog(){
        if(optionDialogList.size() > 0){
            OptionDialog dialog = optionDialogList.get(0);
            if(dialog != null) {
                return dialog.onBackPressed();
            }
        }
        return false;
    }

    private void trim(){
    }

    public void removeAll(){
        optionDialogList.clear();
    }
}
