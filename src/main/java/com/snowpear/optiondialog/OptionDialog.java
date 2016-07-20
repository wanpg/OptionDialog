package com.snowpear.optiondialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * Created by wangjinpeng on 16/1/28.
 * Dialog的自己实现，用于在4.4以上机器，此dialog的content区域是不包含statusbar和navigationbar区域的
 * 因此可以做一些上下边缘进入退出的动画，并且不会覆盖statusbar和navigationbar
 *
 * OptionDialog与系统的Dialog混用时要注意层次，
 * 系统的Dialog会始终在OptionDialog的上一层，
 * 也就是OptionDialog会被系统的Dialog覆盖
 */
public class OptionDialog extends RelativeLayout {

    public OptionDialog(Context context) {
        super(context);
        initView(context);
    }

    public OptionDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OptionDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OptionDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    /**
     * dialog的状态变化时候的回调
     */
    public interface OnDialogStateChangeListener{
        void onShow();
        void onDismiss();
        void onCancel();
    }

    /**
     * backpress点击，相关联的Activity会调用
     * @return
     */
    protected boolean onBackPressed() {
        if(isShown() && mCancelable){
            cancel();
            return true;
        }
        return false;
    }

    public static int SCOPE_FULLSCREEN = 1;//全屏半透明
    public static int SCOPE_CONTENT = 2;//偏移以外的半透明

    /**
     * 操作按钮的容器
     */
    private OptionContainerView mOptionContainer;

    /**
     * content的重力
     */
    private int mGravity = Gravity.TOP;

    /**
     * 是否可以取消，返回键或者周围区域
     */
    private boolean mCancelable = true;

    /**
     * content周围区域是否接收触摸事件，如果此事件为true，则{@link #mCancelable} 为false
     */
    private boolean mOutsideTouchable = false;

    /**
     * 是否可以拖拽
     */
    private boolean mDragable = false;

    /**
     * 半透明区域
     */
    private int scope = SCOPE_FULLSCREEN;

    /**
     * 背景是否半透明，如果此为false，则{@link #scope} 无效
     */
    private boolean isTranslucentWork = false;

    private int leftMargin = 0;
    private int topMargin = 0;
    private int rightMargin = 0;
    private int bottomMargin = 0;

    private int enterAnimationRes = -1;
    private int exitAnimationRes = -1;

    private OnDialogStateChangeListener onDialogStateChangeListener;

    private OptionDialogManagerImpl mOptionDialogManagerImpl;
    private View mContentView;
    private OptionSystemConfig optionSystemConfig;

    private String tag = null;

    private boolean visible;

    private OptionContainerView.OptionContainerChangeListener mOptionContainerListener = new OptionContainerView.OptionContainerChangeListener() {
        @Override
        public void enterOver() {

        }

        @Override
        public void exitOver() {
            dismissImmediate(true);
        }
    };

    /**
     * 初始化view
     * @param context
     */
    private void initView(Context context) {
        boolean isInit = false;
        if(isInit){
            return;
        }
        if(context != null && context instanceof Activity) {
            optionSystemConfig = new OptionSystemConfig((Activity) context);
        }
        if (Build.VERSION.SDK_INT <= 15) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * 设置contentView
     * @param contentView
     */
    public void setContentView(View contentView){
        if(mOptionContainer != null && mOptionContainer.getChildCount() > 0){
            return;
        }
        mContentView = contentView;
    }

    /**
     * 子类可重写此方法进行contentview设置
     * @param inflater
     * @return
     */
    protected View createContent(LayoutInflater inflater){
        return null;
    }

    /**
     * 获取tag
     * @return
     */
    public String getAddTag(){
        return tag;
    }

    public boolean isVisible() {
        return isShown() || visible;
    }

    /**
     * 取消dialog
     */
    public void cancel(){
        mOptionContainer.exit(new Runnable() {
            @Override
            public void run() {
                dismissImmediate(true);
            }
        });
    }

    /**
     * 销毁dialog，会调用动画
     */
    public void dismiss() {
        mOptionContainer.exit(new Runnable() {
            @Override
            public void run() {
                dismissImmediate(false);
            }
        });
    }

    /**
     * 直接dismiss，调用此方法，无动画效果
     */
    public void dismissImmediate(){
        dismissImmediate(false);
    }

    /**
     * 设置是否能返回键或者空白区域点击销毁
     * @param cancelable
     * @return
     */
    public OptionDialog cancelable(boolean cancelable){
        this.mCancelable = cancelable;
        return this;
    }

    public OptionDialog outsideTouchable(boolean outsideTouchable){
        mOutsideTouchable = outsideTouchable;
        return this;
    }

    /**
     * 设置content的重力
     * @param gravity
     * @return
     */
    public OptionDialog gravity(int gravity){
        this.mGravity = gravity;
        return this;
    }


    /**
     * 设置content出现区域的四边的边距
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public OptionDialog margin(int left, int top, int right, int bottom){
        leftMargin = left;
        topMargin = top;
        rightMargin = right;
        bottomMargin = bottom;
        return this;
    }

    @Override
    public void draw(Canvas canvas) {
        try{
            super.draw(canvas);
        }catch(java.lang.NullPointerException e){
            //you can log if you want to you can leave your friends behind
            //Log.e("mytag","MyCustomView::draw():"+e);
        }
    }

    /**
     * 设置背景透明度
     * @param work
     * @param scopeType
     * @return
     */
    public OptionDialog translucent(boolean work, int scopeType){
        isTranslucentWork = work;
        scope = scopeType;
        return this;
    }

    /**
     * 设置是否能够拖拽
     * @param dragable
     * @return
     */
    public OptionDialog dragable(boolean dragable){
        this.mDragable = dragable;
        return this;
    }

    /**
     * 设置动画
     * @param enterRes
     * @param exitRes
     * @return
     */
    public OptionDialog animation(int enterRes, int exitRes){
        enterAnimationRes = enterRes;
        exitAnimationRes = exitRes;
        return this;
    }

    /**
     * 设置状态回调
     * @param onDialogStateChangeListener
     * @return
     */
    public OptionDialog setDialogStateChangeListener(OnDialogStateChangeListener onDialogStateChangeListener) {
        this.onDialogStateChangeListener = onDialogStateChangeListener;
        return this;
    }

    /**
     * 获取当前持有的Activity
     * @return
     */
    public Activity getActivity() {
        if(mOptionDialogManagerImpl != null){
            return mOptionDialogManagerImpl.getActivity();
        }else if(getContext() != null && getContext() instanceof Activity){
            return (Activity) getContext();
        }
        return null;
    }

    public final String getString(int res) {
        return getResources().getString(res);
    }
    public final String getString(int res, Object... formatArgs) {
        return getResources().getString(res, formatArgs);
    }

    /**
     * 取消在返回键返回/空白处点击/滑动页面返回时调用
     * 退出动画执行完
     */
    protected void onCancel(){
        if(onDialogStateChangeListener!=null){
            onDialogStateChangeListener.onCancel();
        }
    }

    /**
     * 销毁时会调用此方法，退出动画执行以后
     */
    protected void onDismiss(){
        if(onDialogStateChangeListener!=null){
            onDialogStateChangeListener.onDismiss();
        }
    }

    /**
     * 完全显示时候会调用此方法
     * 进入动画结束以后
     */
    protected void onShow(){
        if(onDialogStateChangeListener!=null){
            onDialogStateChangeListener.onShow();
        }
    }

    /**
     * 显示dialog并执行动画
     * @param optionDialogManagerImpl
     * @param tag
     */
    public void show(OptionDialogManagerImpl optionDialogManagerImpl, String tag){
        if(isVisible()){
            return;
        }
        this.tag = tag;
        mOptionDialogManagerImpl = optionDialogManagerImpl;
        if(optionSystemConfig == null){
            optionSystemConfig = new OptionSystemConfig(getActivity());
        }
        mOptionDialogManagerImpl.getOptionDialogManager().putDialog(tag, this);
        //添加自己到window
        Window window = getActivity().getWindow();
        View decorView = window.getDecorView();

        // 检测是否重复加载
        int i = ((ViewGroup) decorView).indexOfChild(this);
        if(i >= 0){
            return;
        }
        ((ViewGroup) decorView).addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        visible = true;

        //创建容器
        mOptionContainer = new OptionContainerView(getContext());
        View mShadowView = new View(getContext());
        RelativeLayout.LayoutParams lpShadow = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams lpContent = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //创建content
        if(mContentView == null){
            mContentView = createContent(LayoutInflater.from(getContext()));
        }
        int orientation = getResources().getConfiguration().orientation;
        int statusBarHeight = decorView.getTop() < optionSystemConfig.getStatusBarHeight() ? optionSystemConfig.getStatusBarHeight() : 0;
        int navigationBarHeight = 0;
        int navigationBarWidth = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            float[] size = OptionSystemConfig.getDisplaySize(getContext());
            navigationBarHeight = (orientation == Configuration.ORIENTATION_PORTRAIT && decorView.getBottom() >= size[1]) ? optionSystemConfig.getNavigationBarHeight() : 0;
            navigationBarWidth = (orientation != Configuration.ORIENTATION_PORTRAIT && decorView.getRight() >= size[0]) ? optionSystemConfig.getNavigationBarWidth() : 0;
        }
        if(scope == SCOPE_CONTENT) {
            if (mGravity == Gravity.TOP) {
                lpShadow.topMargin = topMargin + statusBarHeight;
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                } else {
                    lpShadow.rightMargin = navigationBarWidth;
                }
            } else {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    lpShadow.bottomMargin = bottomMargin + navigationBarHeight;
                } else {
                    lpShadow.bottomMargin = bottomMargin;
                    lpShadow.rightMargin = navigationBarWidth;
                }
            }
        }
        if(mGravity == Gravity.TOP){
            lpContent.topMargin = topMargin + statusBarHeight;
            lpContent.leftMargin = leftMargin;
            lpContent.rightMargin = rightMargin;
            lpContent.bottomMargin = bottomMargin;
            if(orientation == Configuration.ORIENTATION_PORTRAIT) {
                lpContent.bottomMargin += navigationBarHeight;
            } else {
                lpContent.rightMargin += navigationBarWidth;
            }
            lpContent.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }else{
            lpContent.topMargin = topMargin + statusBarHeight;
            lpContent.leftMargin = leftMargin;
            lpContent.rightMargin = rightMargin;
            lpContent.bottomMargin = bottomMargin;
            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                lpContent.bottomMargin += navigationBarHeight;
            }else{
                lpContent.rightMargin += navigationBarWidth;
            }
            lpContent.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        addView(mShadowView, lpShadow);
        addView(mOptionContainer, lpContent);
        mOptionContainer.addView(mContentView);
        mOptionContainer.updateContainer(mGravity, mDragable, enterAnimationRes, exitAnimationRes, mShadowView, isTranslucentWork, mOptionContainerListener);
        if(!mOutsideTouchable) {
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCancelable) {
                        cancel();
                    }
                }
            });
        } else {
            mCancelable = false;
        }

        mContentView.setVisibility(View.VISIBLE);

        if(isTranslucentWork) {
            int color = Color.parseColor("#88000000");
            mShadowView.setBackgroundColor(color);
        }else{
            mShadowView.setVisibility(View.GONE);
        }
        mOptionContainer.enter(new Runnable() {
            @Override
            public void run() {
                onShow();
            }
        });
    }

    private void dismissImmediate(boolean isCancel){
        if(mOptionContainer != null){
            mOptionContainer.clearAnimation();
            mOptionContainer.removeAllViews();
        }
        Window window = getActivity().getWindow();
        ((ViewGroup)window.getDecorView()).removeView(this);
        visible = false;
        mOptionDialogManagerImpl.getOptionDialogManager().removeDialog(this);
        //此处调用onDismiss，为了回调stateListener的onDismiss或者让子类知道dismiss触发
        if(isCancel){
            onCancel();
        }
        onDismiss();
    }
}
