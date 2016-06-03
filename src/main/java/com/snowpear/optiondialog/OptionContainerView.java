package com.snowpear.optiondialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
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

    private static int DEFAULT_ANIMATION_DUATION = 250;

    private boolean mDraggable;
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
    private static final int mSlideVelocity = 200;
    protected int mMaximumVelocity;
    private boolean isBeingDragged = false;

    private Scroller scroller;

    private static final int INVALID_POINTER = -1;
    /**
     * 我的活动的触摸点的id
     */
    private int mActivePointerId = -1;
    private boolean isScrolling = false;
    private boolean isScrollEnter = false;
    private static final boolean USE_CACHE = true;

    public void updateContainer(int gravity, boolean draggable, int animResEnter, int animResExit, View shadowView, boolean isTranslucentWork, OptionContainerChangeListener listener){
        mGravity = gravity;
        mDraggable = draggable;
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
        final float y = MotionEventCompat.getY(event, pointerIndex);
        final float deltaY = mDragDownY - y;
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
        OptionUtils.debug("computeScroll---finish--A");
        if (isScrolling) {
            OptionUtils.debug("computeScroll---finish--B");
            // Done with scroll, no longer want to cache view drawing.
            setScrollingCacheEnabled(false);
            scroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }

            if(mListener != null){
                if(isScrollEnter){
                    mListener.enterOver();
                    OptionUtils.debug("computeScroll---finish--C");
                }else{
                    mListener.exitOver();
                    OptionUtils.debug("computeScroll---finish--D");
                }
            }
        }
        isScrolling = false;
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished()) {
            OptionUtils.debug("computeScroll---A");
            if (scroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = scroller.getCurrX();
                int y = scroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                    OptionUtils.debug("computeScroll---B");
                }
                OptionUtils.debug("computeScroll---C");
                // Keep on drawing until the animation has finished.
                postInvalidate();
                return;
            }
        }
        OptionUtils.debug("computeScroll---finish");
        // Done with scroll, clean up state.
        finishScroll();
    }

    private void smoothScroll(boolean enter, int velocity){
        isScrollEnter = enter;
        final int totalHeight = getMeasuredHeight();
        int x = 0;
        int y = 0;
        if(enter){
            //回到最初
            x = 0;
            y = 0;
        }else{
            //结束此dialog
            x = 0;
            y =((mGravity == Gravity.TOP) ? 1 : -1) * totalHeight;
        }
        OptionUtils.debug("smoothScroll---ISSCROLLERENTER----:" + isScrollEnter);
        int sx = getScrollX();
        int sy = getScrollY();

        int dx = x - sx;
        int dy = y - sy;

        if (dx == 0 && dy == 0) {
            OptionUtils.debug("smoothScroll---dx and dy == 0 ");
            finishScroll();
            return;
        }

        setScrollingCacheEnabled(true);
        isScrolling = true;

        final int halfWidth = totalHeight / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dy) / totalHeight);
        final float distance = halfWidth + halfWidth * OptionUtils.distanceInfluenceForSnapDuration(distanceRatio);

        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(500 * Math.abs(distance / velocity));
        } else {
            duration = DEFAULT_ANIMATION_DUATION;
        }
        duration = Math.min(duration, DEFAULT_ANIMATION_DUATION);
        OptionUtils.debug("smoothScroll---sy:" + sy + "--dy:" + dy);
        scroller.startScroll(sx, sy, dx, dy, duration);
        postInvalidate();//此处一定要刷新
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (USE_CACHE) {
            final int size = getChildCount();
            for (int i = 0; i < size; ++i) {
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.setDrawingCacheEnabled(!enabled);
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!mDraggable){
            return false;
        }
        if(mGravity == Gravity.TOP && OptionUtils.canChildScrollDown(this)){
            return false;
        }
        if(mGravity == Gravity.BOTTOM && OptionUtils.canChildScrollUp(this)){
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
        if(!mDraggable){
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
                        OptionUtils.debug("onTouchEvent---action-up----A");
                    }else {
                        final boolean enter;
                        final VelocityTracker velocityTracker = mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);
                        OptionUtils.debug("onTouchEvent---action-up--initialVelocity:" + initialVelocity);
                        final int activePointerIndex = getPointerIndex(event, mActivePointerId);
                        if (mActivePointerId == INVALID_POINTER) {
                            //回到最初状态
                            enter = true;
                            OptionUtils.debug("onTouchEvent---action-up----B");
                        } else {
                            final float y = MotionEventCompat.getY(event, activePointerIndex);
                            final int totalDelta = (int) (y - mDragDownY);
                            //根据滑动的距离判断是否进入上一页或者下一页
                            if (initialVelocity > mSlideVelocity) {
                                //大于这个速率  继续完成动画
                                enter = (mGravity == Gravity.BOTTOM);
                                OptionUtils.debug("onTouchEvent---action-up----C");
                            } else if (initialVelocity < -mSlideVelocity) {
                                //回到最初状态
                                enter = (mGravity == Gravity.TOP);
                                OptionUtils.debug("onTouchEvent---action-up----D");
                            } else {
                                enter = (Math.abs(totalDelta) < getMeasuredHeight() / 3);
                                OptionUtils.debug("onTouchEvent---action-up----E");
                            }
                        }
                        smoothScroll(enter, initialVelocity);
                    }
                }
                break;
        }
        return true;
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
                alphaAnimation.setInterpolator(sInterpolator);
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
