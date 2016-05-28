package com.king.app.video.open.image;

import com.king.app.video.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;

/**
 * 起始大小由layout_width/height控制，位置也由布局控制
 * v5.9.3修改后可以通过setCropArea控制
 */
public class CropView extends View implements OnTouchListener {
    private final String TAG = "CropView";
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private static final int TOP = 0x15;
    private static final int LEFT = 0x16;
    private static final int BOTTOM = 0x17;
    private static final int RIGHT = 0x18;
    private static final int LEFT_TOP = 0x11;
    private static final int RIGHT_TOP = 0x12;
    private static final int LEFT_BOTTOM = 0x13;
    private static final int RIGHT_BOTTOM = 0x14;
    private static final int CENTER = 0x19;
    private int offset;
    private int minScale = 100;
    protected Paint paint = new Paint();
    private boolean isInTouch;

    private int SIZE_BOARDER;
    private int SIZE_BOARDER_POINT;
    private int SIZE_CROP_AREA;
    private int COLOR_BOARDER;
    private int COLOR_BOARDER_TOUCH;

    private OnCropAreaChangeListener onCropAreaChangeListener;

    public interface OnCropAreaChangeListener {
        public void onChange(int left, int top, int right, int bottom);
        public void onChange(int width, int height);
    }
    /**
     * 初始化获取屏幕宽高
     * screen orientation change的时候也要重新初始化
     */
    public void initParams() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        SIZE_BOARDER = getResources().getDimensionPixelSize(R.dimen.video_crop_boarder_size);
        SIZE_BOARDER_POINT = getResources().getDimensionPixelSize(R.dimen.video_crop_boarder_point_size);
        SIZE_CROP_AREA = getResources().getDimensionPixelSize(R.dimen.video_crop_side_area);
        COLOR_BOARDER = getResources().getColor(R.color.video_crop_stroke);
        COLOR_BOARDER_TOUCH = getResources().getColor(R.color.lightblue);
        offset = SIZE_BOARDER_POINT;
    }

    public void setOnCropAreaChangeListener(OnCropAreaChangeListener listener) {
        this.onCropAreaChangeListener = listener;
    }

    public CropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initParams();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initParams();
    }

    public CropView(Context context) {
        super(context);
        setOnTouchListener(this);
        initParams();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInTouch) {
            paint.setColor(COLOR_BOARDER_TOUCH);
        }
        else {
            paint.setColor(COLOR_BOARDER);
        }

        //crop frame
        paint.setStrokeWidth(SIZE_BOARDER);
        paint.setStyle(Style.STROKE);
        int left = offset, top = offset, right = getWidth() - offset, bottom = getHeight() - offset;
        canvas.drawRect(left, top, right, bottom, paint);

        //8 points area in 4 sides
        paint.setStyle(Style.FILL);
        canvas.drawRect(left -SIZE_BOARDER_POINT, top - SIZE_BOARDER_POINT, left + SIZE_BOARDER_POINT, top + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(right - SIZE_BOARDER_POINT, top - SIZE_BOARDER_POINT, right + SIZE_BOARDER_POINT, top + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(left -SIZE_BOARDER_POINT, bottom - SIZE_BOARDER_POINT, left + SIZE_BOARDER_POINT, bottom + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(right -SIZE_BOARDER_POINT, bottom - SIZE_BOARDER_POINT, right + SIZE_BOARDER_POINT, bottom + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(left + (right - left)/2 - SIZE_BOARDER_POINT, top - SIZE_BOARDER_POINT, left + (right - left)/2 + SIZE_BOARDER_POINT, top + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(left + (right - left)/2 - SIZE_BOARDER_POINT, bottom - SIZE_BOARDER_POINT, left + (right - left)/2 + SIZE_BOARDER_POINT, bottom + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(left -SIZE_BOARDER_POINT, top + (bottom - top)/2 - SIZE_BOARDER_POINT, left + SIZE_BOARDER_POINT, top + (bottom - top)/2 + SIZE_BOARDER_POINT, paint);
        canvas.drawRect(right -SIZE_BOARDER_POINT, top + (bottom - top)/2 - SIZE_BOARDER_POINT, right + SIZE_BOARDER_POINT, top + (bottom - top)/2 + SIZE_BOARDER_POINT, paint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            isInTouch = true;
            oriLeft = v.getLeft();
            oriRight = v.getRight();
            oriTop = v.getTop();
            oriBottom = v.getBottom();
            lastY = (int) event.getRawY();
            lastX = (int) event.getRawX();
            dragDirection = getDirection(v, (int) event.getX(),
                    (int) event.getY());
        }
        // 处理拖动事件
        delDrag(v, event, action);
        invalidate();
        return false;
    }

    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */
    protected void delDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                switch (dragDirection) {
                    case LEFT: // 左边缘
                        left(v, dx);
                        break;
                    case RIGHT: // 右边缘
                        right(v, dx);
                        break;
                    case BOTTOM: // 下边缘
                        bottom(v, dy);
                        break;
                    case TOP: // 上边缘
                        top(v, dy);
                        break;
                    case CENTER: // 点击中心-->>移动
                        center(v, dx, dy);
                        break;
                    case LEFT_BOTTOM: // 左下
                        left(v, dx);
                        bottom(v, dy);
                        break;
                    case LEFT_TOP: // 左上
                        left(v, dx);
                        top(v, dy);
                        break;
                    case RIGHT_BOTTOM: // 右下
                        right(v, dx);
                        bottom(v, dy);
                        break;
                    case RIGHT_TOP: // 右上
                        right(v, dx);
                        top(v, dy);
                        break;
                }
                if (dragDirection != CENTER) {
                    v.layout(oriLeft, oriTop, oriRight, oriBottom);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                if (onCropAreaChangeListener != null) {
                    onCropAreaChangeListener.onChange(oriLeft, oriTop, oriRight, oriBottom);
                }
                break;
            case MotionEvent.ACTION_UP:
                dragDirection = 0;
                isInTouch = false;
                break;
        }
    }

    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dx
     * @param dy
     */
    private void center(View v, int dx, int dy) {
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
        if (left < -offset) {
            left = -offset;
            right = left + v.getWidth();
        }
        if (right > screenWidth + offset) {
            right = screenWidth + offset;
            left = right - v.getWidth();
        }
        if (top < -offset) {
            top = -offset;
            bottom = top + v.getHeight();
        }
        if (bottom > screenHeight + offset) {
            bottom = screenHeight + offset;
            top = bottom - v.getHeight();
        }
        v.layout(left, top, right, bottom);
        oriLeft = left;
        oriTop = top;
        oriRight = right;
        oriBottom = bottom;
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;
        if (oriTop < -offset) {
            oriTop = -offset;
        }
        if (oriBottom - oriTop - 2 * offset < minScale) {
            oriTop = oriBottom - 2 * offset - minScale;
        }
    }

    /**
     * 触摸点为下边缘
     *
     * @param v
     * @param dy
     */
    private void bottom(View v, int dy) {
        oriBottom += dy;
        if (oriBottom > screenHeight + offset) {
            oriBottom = screenHeight + offset;
        }
        if (oriBottom - oriTop - 2 * offset < minScale) {
            oriBottom = minScale + oriTop + 2 * offset;
        }
    }

    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
        if (oriRight > screenWidth + offset) {
            oriRight = screenWidth + offset;
        }
        if (oriRight - oriLeft - 2 * offset < minScale) {
            oriRight = oriLeft + 2 * offset + minScale;
        }
    }

    /**
     * 触摸点为左边缘
     *
     * @param v
     * @param dx
     */
    private void left(View v, int dx) {
        oriLeft += dx;
        if (oriLeft < -offset) {
            oriLeft = -offset;
        }
        if (oriRight - oriLeft - 2 * offset < minScale) {
            oriLeft = oriRight - 2 * offset - minScale;
        }
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        if (x < SIZE_CROP_AREA && y < SIZE_CROP_AREA) {
            return LEFT_TOP;
        }
        if (y < SIZE_CROP_AREA && right - left - x < SIZE_CROP_AREA) {
            return RIGHT_TOP;
        }
        if (x < SIZE_CROP_AREA && bottom - top - y < SIZE_CROP_AREA) {
            return LEFT_BOTTOM;
        }
        if (right - left - x < SIZE_CROP_AREA && bottom - top - y < SIZE_CROP_AREA) {
            return RIGHT_BOTTOM;
        }
        if (x < SIZE_CROP_AREA) {
            return LEFT;
        }
        if (y < SIZE_CROP_AREA) {
            return TOP;
        }
        if (right - left - x < SIZE_CROP_AREA) {
            return RIGHT;
        }
        if (bottom - top - y < SIZE_CROP_AREA) {
            return BOTTOM;
        }
        return CENTER;
    }

    /**
     * 获取截取宽度
     *
     * @return
     */
    public int getCutWidth() {
        return getWidth() - 2 * offset;
    }

    /**
     * 获取截取高度
     *
     * @return
     */
    public int getCutHeight() {
        return getHeight() - 2 * offset;
    }

    public Rect getCutPosition() {
        oriLeft = getLeft();
        oriTop = getTop();
        oriRight = getRight();
        oriBottom = getBottom();
        Log.d(TAG, "getCutPosition " + oriLeft + "," + oriTop + "," + oriRight + "," + oriBottom);
        return new Rect(oriLeft + SIZE_BOARDER_POINT, oriTop + SIZE_BOARDER_POINT
                , oriRight - SIZE_BOARDER_POINT, oriBottom - SIZE_BOARDER_POINT);
    }

    /**
     * 设置初始裁剪框区域
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setCropArea(int left, int top, int right, int bottom) {
        /**
         * onTouch事件中，用layout和invalidate可以实时刷新view的位置大小，但是在UI主线程调用这种方法却又无法改变
         * 不知道具体原因，可能是跟UI线程有关
         //    	layout(left, top, right, bottom);
         //    	invalidate();
         */
        //采用这种方法可以
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = right - left;
        params.height = bottom - top;
        ((MarginLayoutParams) params).leftMargin = left;
        ((MarginLayoutParams) params).topMargin = top;
    }
    /**
     * 设置初始裁剪框区域
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setCropArea(int width, int height) {
        /**
         * onTouch事件中，用layout和invalidate可以实时刷新view的位置大小，但是在UI主线程调用这种方法却又无法改变
         * 不知道具体原因，可能是跟UI线程有关
         //    	layout(left, top, right, bottom);
         //    	invalidate();
         */
        //采用这种方法可以
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
    }

    public void setCropArea(Rect rect) {
        setCropArea(rect.left, rect.top, rect.right, rect.bottom);
    }

    public int getOffset() {
        return offset;
    }
}