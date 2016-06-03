package com.snowpear.optiondialog;

import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Adapter;

/**
 * Created by wangjinpeng on 16/6/3.
 */
public class OptionUtils {

    private static final boolean DEBUG = true;

    public static void debug(String info){
        if(DEBUG){
            Log.d("OptionDialog", info);
        }
    }

    public static float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    public static DisplayMetrics getScreenDisplay(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     *         scroll up. Override this if the child view is a custom view.
     */
    public static boolean canChildScrollUp(ViewGroup viewGroup) {
        if(viewGroup != null) {
            View mTarget = viewGroup.getChildAt(0);
            if (mTarget != null) {
                if (Build.VERSION.SDK_INT < 14) {
                    if (mTarget instanceof AbsListView) {
                        final AbsListView absListView = (AbsListView) mTarget;
                        return absListView.getChildCount() > 0
                                && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                                .getTop() < absListView.getPaddingTop());
                    } else {
                        return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
                    }
                } else {
                    return ViewCompat.canScrollVertically(mTarget, -1);
                }
            }
        }
        return false;
    }

    public static boolean canChildScrollDown(ViewGroup viewGroup) {
        if(viewGroup != null) {
            View mListView = viewGroup.getChildAt(0);
            if (mListView != null) {
                if (Build.VERSION.SDK_INT < 14) {
                    if (mListView instanceof AbsListView) {
                        int childCount = ((AbsListView) mListView).getChildCount();
                        if (childCount <= 0) {
                            return false;
                        }
                        Adapter adapter = ((AbsListView) mListView).getAdapter();
                        if (adapter == null || adapter.getCount() <= 0) {
                            return false;
                        }

                        if (((AbsListView) mListView).getLastVisiblePosition() < adapter.getCount() - 1) {
                            return true;
                        }

                        int lastBottom = ((AbsListView) mListView).getChildAt(childCount - 1).getBottom();
                        int listBottom = mListView.getBottom();
                        return (lastBottom - listBottom) > mListView.getPaddingBottom();
                    } else {
                        return ViewCompat.canScrollVertically(mListView, 1) || mListView.getScrollY() > 0;
                    }
                } else {
                    return ViewCompat.canScrollVertically(mListView, 1);
                }
            }
        }
        return false;
    }
}
