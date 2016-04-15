package com.snowpear.optiondialog;

import android.app.Activity;

/**
 * 建议Activity实现此接口，并实现下面方法
 */
public interface OptionDialogManagerImpl {



//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//            if(backOptionDialog()){
//                return true;
//            }
//        }
//        return super.dispatchKeyEvent(event);
//    }

    boolean backOptionDialog();

    Activity getActivity();

    OptionDialogManager getOptionDialogManager();


}
