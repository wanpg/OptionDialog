package com.snowpear.optiondialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BaseInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by wangjinpeng on 16/3/14.
 */
class OptionContainerView extends RelativeLayout {

    public OptionContainerView(Context context) {
        super(context);
        init(context);
    }

    public OptionContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OptionContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OptionContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    interface OptionContainerChangeListener{
        void enterOver();
        void exitOver();
    }

    private static long DEFAULT_ANIMATION_DUATION = 250;

    private boolean mDragable;
    private int mGravity;

    private float mDragDownY;
    private float mDragLastY;
    private int mTouchSlop = 0;

    private int enterAnimationRes = -1;
    private int exitAnimationRes = -1;

    private OptionContainerChangeListener mListener;

    private View mShadowView;
    private boolean isTranslucentWork;

    protected VelocityTracker mVelocityTracker;

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    /** 最小滑动速率，超过这个速率就可以翻页 **/
    private static final int mSlideVelocity = 600;
    protected int mMaximumVelocity;
    private boolean isBeingDragged = false;

    private Scroller scroller;

    public void updateContainer(int gravity, boolean dragable, int animResEnter, int animResExit, View shadowView, boolean isTranslucentWork, OptionContainerChangeListener listener){
        mGravity = gravity;
        mDragable = dragable;
        mListener = listener;
        enterAnimationRes = animResEnter;
        exitAnimationRes = animResExit;
        mShadowView = shadowView;
        this.isTranslucentWork = isTranslucentWork;
    }

    public void enter(Runnable runnable){
        doAnimation(true, runnable);
    }

    public void exit(Runnable runnable){
        doAnimation(false, runnable);
    }

    private void init(Context context){
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        scroller = new Scroller(context, sInterpolator);
    }

    @Override
    public void clearAnimation() {
        super.clearAnimation();
        View view = getChildAt(0);
        if(view != null){
            view.clearAnimation();
        }
    }

    private void determineDrag(MotionEvent event){
        final int activePointerId = mActivePointerId;
        final int pointerIndex = getPointerIndex(event, activePointerId);
        if (activePointerId == INVALID_POINTER)
            return;
        final float x = MotionEventCompat.getX(event, pointerIndex);
        final float deltaY = mDragDownY - x;
        final float absDeltaY = Math.abs(deltaY);
        isBeingDragged = false;
        if(absDeltaY > mTouchSlop) {
            if (mGravity == Gravity.TOP && deltaY > 0) {
                isBeingDragged = true;
            }else if (mGravity == Gravity.BOTTOM && deltaY < 0) {
                isBeingDragged = true;
            }
        }
        if(isBeingDragged){
            //此处记录开始的一些参数
        }
    }

    private static final int INVALID_POINTER = -1;
    /**
     * 我的活动的触摸点的id
     */
    private int mActivePointerId = -1;
    private int getPointerIndex(MotionEvent ev, int id) {
        int activePointerIndex = MotionEventCompat.findPointerIndex(ev, id);
        if (activePointerIndex == -1)
            mActivePointerId = INVALID_POINTER;
        return activePointerIndex;
    }

    public void recordStartPos(MotionEvent ev){
        // 记录开始坐标的index 和  ID
        int index = MotionEventCompat.getActionIndex(ev);
        mActivePointerId = MotionEventCompat.getPointerId(ev, index);
        mDragDownY = mDragLastY = ev.getY();
    }

    private void finishScroll(){

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!mDragable){
            return false;
        }
        if(mGravity == Gravity.TOP && canChildScrollDown()){
            return false;
        }
        if(mGravity == Gravity.BOTTOM && canChildScrollUp()){
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        if(action == MotionEvent.ACTION_DOWN){
            recordStartPos(ev);
        }else if(action == MotionEvent.ACTION_MOVE){
            if(mActivePointerId == INVALID_POINTER){
                recordStartPos(ev);
            }
            determineDrag(ev);
        }

        recordStartPos(ev);
        return isBeingDragged;
    }

    private void recordMotionToTracker(MotionEvent event){
        if(isBeingDragged) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mDragable){
            return true;
        }

        recordMotionToTracker(event);

        final int action = MotionEventCompat.getActionMasked(event);
        float curY = event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mDragDownY = curY;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isBeingDragged){
                    determineDrag(event);
                }
                if(isBeingDragged){
                    final int oldScrollY = this.getScrollY();
                    final float deltaY = mDragLastY - curY;
                    mDragLastY = curY;
                    int newScrollY = (int) (oldScrollY + deltaY);
                    if(mGravity == Gravity.TOP){
                        if(newScrollY < 0){
                            newScrollY = 0;
                        }
                    }else{
                        if(newScrollY > 0){
                            newScrollY = 0;
                        }
                    }
                    this.scrollTo(0, newScrollY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isBeingDragged){
                    final int scrollY = Math.abs(this.getScrollY());
                    final int totalHeight = getHeight();
                    if(scrollY > totalHeight){
                        if(mListener != null){
                            mListener.exitOver();
                        }
                    }else {
                        final boolean enter;
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
                        Log.d("wanpg", "onTouchEvent---action-up--initialVelocity:" + initialVelocity);
                        final int activePointerIndex = getPointerIndex(event, mActivePointerId);
                        if (mActivePointerId == INVALID_POINTER) {
                            //回到最初状态
                            enter = true;
                        } else {
                            final float y = MotionEventCompat.getY(event, activePointerIndex);
                            final int totalDelta = (int) (y - mDragDownY);
                            //根据滑动的距离判断是否进入上一页或者下一页
                            if (initialVelocity > mSlideVelocity) {
                                //大于这个速率  继续完成动画
                                enter = true;
                            } else if (initialVelocity < -mSlideVelocity) {
                                //回到最初状态
                                enter = false;
                            } else {
                                int screenHeight = getScreenDisplay().heightPixels;
                                int absDelta = Math.abs(totalDelta);
                                if (absDelta > screenHeight / 6 || absDelta > totalHeight / 3) {
                                    //结束此dialog,继续完成动画
                                    enter = false;
                                } else {
                                    //回到最初状态
                                    enter = true;
                                }
                            }
                        }
                        smoothScroll(enter, initialVelocity);
                    }
                }
                break;
        }
        return true;
    }

    private void smoothScroll(boolean enter, int velocity){
        final int scrollY = Math.abs(this.getScrollY());
        final int totalHeight = getHeight();
        scrollTo(0, 0);
        final float percent = ((float) scrollY) / ((float) totalHeight);
        float leftPercent = 0f;
        if(enter){
            //回到最初
            enter = true;
            leftPercent = percent;
        }else{
            //结束此dialog
            enter = false;
            leftPercent = 1f - percent;
        }
        doAnimationByProgress(enter, leftPercent);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     *         scroll up. Override this if the child view is a custom view.
     */
    private boolean canChildScrollUp() {
        View mTarget = getChildAt(0);
        if(mTarget != null) {
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
        return false;
    }

    public boolean canChildScrollDown() {
        View mListView = getChildAt(0);
        if(mListView != null) {
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
        return false;
    }

    /**
     * 执行动画，dismiss和show的时候调用
     * @param enter
     * @param runnable
     */
    private void doAnimation(final boolean enter, final Runnable runnable){
        View view = getChildAt(0);
        if(view == null){
            return;
        }
        view.clearAnimation();
        mShadowView.clearAnimation();
        Animation animation = getAnimation(enter);
        mShadowView.setVisibility(isTranslucentWork ? View.VISIBLE : View.GONE);
        if(animation != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    runnable.run();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);
            if(isTranslucentWork) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(enter ? 0.3f : 1, enter ? 1 : 0.3f);
                alphaAnimation.setInterpolator(enter ? new DecelerateInterpolator() : new AccelerateInterpolator());
                alphaAnimation.setDuration(animation.getDuration());
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mShadowView.setVisibility(enter ? VISIBLE : GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mShadowView.startAnimation(alphaAnimation);
            }
        }else{
            runnable.run();
        }
    }

    private void doAnimationByProgress(final boolean enter, float leftPercent){
        View view = getChildAt(0);
        if(view == null){
            return;
        }
        view.clearAnimation();
        mShadowView.clearAnimation();
        Animation animation = getAnimationByProgress(enter, leftPercent);
        mShadowView.setVisibility(isTranslucentWork ? View.VISIBLE : View.GONE);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mShadowView.setVisibility(enter ? VISIBLE : GONE);
                if (!enter) {
                    if(mListener != null){
                        mListener.exitOver();
                    }
//                    dismissImmediate(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    private Animation getAnimationByProgress(boolean enter, float leftPercent){
        float fromY, toY;
        TranslateAnimation animation;
        BaseInterpolator interpolator;
        if(enter){
            if(mGravity == Gravity.TOP){
                fromY = -1f * leftPercent;
                toY = 0f;
            } else {
                fromY = 1f * leftPercent;
                toY = 0f;
            }
            interpolator = new DecelerateInterpolator();
        }else{
            if(mGravity == Gravity.TOP){
                fromY = leftPercent - 1f;
                toY = -1f;
            } else {
                fromY = 1f - leftPercent;
                toY = 1f;
            }
            interpolator = new AccelerateInterpolator();
        }
        animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, fromY, Animation.RELATIVE_TO_PARENT, toY);
        animation.setInterpolator(interpolator);
        animation.setDuration((long) (Math.abs(toY - fromY) * DEFAULT_ANIMATION_DUATION));
        return animation;
    }

    private DisplayMetrics getScreenDisplay(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    private Animation getAnimation(boolean enter){
        if(enterAnimationRes != -1 || exitAnimationRes != -1){
            return AnimationUtils.loadAnimation(getContext(), enter ? enterAnimationRes : exitAnimationRes);
        }
        TranslateAnimation animation;
        if(enter){
            if(mGravity == Gravity.TOP){
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, -1f, Animation.RELATIVE_TO_PARENT, 0 );
            } else {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 0);
            }
            animation.setInterpolator(new DecelerateInterpolator());
        }else{
            if(mGravity == Gravity.TOP){
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, -1f);
            } else {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1f);
            }
            animation.setInterpolator(new AccelerateInterpolator());
        }
        animation.setDuration(DEFAULT_ANIMATION_DUATION);
        return animation;
    }
}
