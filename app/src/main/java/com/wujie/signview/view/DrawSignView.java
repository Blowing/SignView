package com.wujie.signview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.wujie.signview.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Troy on 2017-9-29.
 */

public class DrawSignView {



    public static final int thight = 80;
    /**
     * 用来写字的View
     */
    public class CanvasView extends View {

        private Path mPath; // 米字格中的路径

        private android.graphics.Path mmPath; // 横线上的路径按比例缩放

        private CanvasPath myPath;// 记录路径的常数

        private PathListener pathListener; //完成一次书写的回调

        private Paint mPaint;

        private int narrowNum = 10;// 缩小的倍数

        private int width;

        private Timer timer = null;

        private TimerTask task = null;



        private float TOUCH_TOLERANCE = 4; // 画的平滑程度 设置的越大越粗略

        private float mX, mY; //点击时坐标的及时位置

        private float maxX; //写字时 x方向的最大位移
        private float minX;

        private float maxY;
        private float minY;

        private float ddx;
        private float ddy;

        private float cavasViewSize;

        private Bitmap bitmap;
        private Canvas canvas;

        public PathListener getPathListener() {
            return pathListener;
        }

        public void setPathListener(PathListener pathListener) {
            this.pathListener = pathListener;
        }

        /**
         * 得到画板的画笔
         * @return
         */
        public Paint getmPaint() {
            return mPaint;
        }

        /**
         * 设置画板的画笔
         * @param mPaint
         */
        public void setmPaint(Paint mPaint) {
            this.mPaint = mPaint;
        }

        /**
         * 得到缩小倍数
         * @return
         */
        public int getNarrowNum() {
            return narrowNum;
        }

        /**
         * 设置缩小倍数
         * @param narrowNum
         */
        public void setNarrowNum(int narrowNum) {
            this.narrowNum = narrowNum;
        }

        private boolean isFirst = true;

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        public CanvasView(Context c , Paint paint, int width) {
            super(c);
            this.width = width;
            mPath = new Path();
            mmPath = new Path();
            this.mPaint = paint;
            TOUCH_TOLERANCE = dip2px(c, TOUCH_TOLERANCE);
            drawHandlerWritingBox();
            setBackgroundDrawable(new BitmapDrawable(bitmap));

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mPaint.setStrokeWidth(getWidth() / 53);
            narrowNum = h / thight;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if(mPaint != null) {
                canvas.drawPath(mPath, mPaint);
            }
        }

        private int onTouchCount = 0;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            onTouchCount++;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y, event);
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up(x, y);
                    break;
            }

            return true;
        }

        private void touch_start(float x, float y) {
            if(timer != null) {
                timer.cancel();
            }
            mPath.moveTo(x, y);
            mmPath.moveTo(x / narrowNum, y / narrowNum);

            myPath = new CanvasPath(mmPath);

            mX = x;
            mY = y;
            ddx = x;
            ddy = y;

            setMaxOrMin(x , y);

            int i = (int) Math.floor(x - mPaint.getStrokeWidth());
            int j = (int) Math.floor(y - mPaint.getStrokeWidth());
            int k = (int) Math.ceil(x + mPaint.getStrokeWidth());
            int l = (int) Math.ceil(y + mPaint.getStrokeWidth());
            invalidate(i, j, k , l);

        }

        private int left;
        private int right;
        private int bottom;
        private int top;

        private boolean isHandlerWriting = false;

        public boolean isHandlerWriting() {
            return  isHandlerWriting;
        }

        private void touch_move(float x, float y, MotionEvent event) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                isHandlerWriting = true;
                left = this.getLeft();
                top = this.getTop();
                bottom = this.getBottom();
                right = this.getRight();
                int[] location = new int[2];
                // 获取view相对于屏幕的绝对位置
                this.getLocationOnScreen(location);
                int sx = location[0];
                int sy = location[1];
                // 获取点击点相对于屏幕的绝对位置
                float rx = event.getRawX();
                float ry = event.getRawY();
                //左上角的点加上宽度和高度得到view的最大坐标
                int maxW = right - left + sx;
                int maxH = bottom - top + sy;
                if (rx < sx || rx > maxW || ry < sy || ry > maxH) {
                    return;
                }

                float tempx = (x + mX) / 2;
                float tempy = (y + mY) / 2;
                mPath.quadTo(mX, mY, tempx, tempy);
                mmPath.quadTo(mX /narrowNum, mY/narrowNum, tempx /2 , tempy/2);
                mX = x;
                mY = y;

                setMaxOrMin(x, y);

                float f17 = Math.min(x, Math.min(this.ddx, tempx));
                float f20 = Math.min(y, Math.min(this.ddy, tempy));
                float f23 = Math.max(x, Math.max(this.ddx, tempx));
                float f26 = Math.max(y, Math.max(this.ddy, tempy));

                int i = (int) Math.floor(f17 - mPaint.getStrokeWidth());
                int j = (int) Math.floor(f20 - mPaint.getStrokeWidth());
                int k = (int) Math.ceil(f23 + mPaint.getStrokeWidth());
                int l = (int) Math.ceil(f26 + mPaint.getStrokeWidth());
                invalidate(i, j, k, l);
                if(isFirst) {
                    invalidate();
                }
                isFirst = false;
                this.ddx = tempx;
                this.ddy = tempy;

            }
        }

        private void touch_up(float x, float y) {
            mPath.lineTo(mX, mY);
            mmPath.lineTo(mX / narrowNum, mY / narrowNum);

            float f17 = Math.min(this.ddx, x);
            float f20 = Math.min(this.ddy, y);
            float f23 = Math.max(this.ddx, x);
            float f26 = Math.max(this.ddy, y);

            int i = (int) Math.floor(f17) -20;
            int j = (int) Math.floor(f20) - 20;
            int k = (int) Math.ceil(f23) + 20;
            int l = (int) Math.ceil(f26) + 20;

            invalidate(i,j,k,l);
            if (isHandlerWriting) {
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                        onTouchCount = 0;
                    }
                };
                timer.schedule(task, 500);

            }
        }

        /**
         * 设置写的时候x，y方向的最大最小值
         * @param x
         * @param y
         */
        private void setMaxOrMin(float x, float y) {

            if (x > maxX) {
                maxX = x;
            }

            if ( x < minX) {
                minX = x;
            }

            if (y > maxY) {
                maxY = y;
            }

            if (y < minY) {
                minY = y;
            }
        }

        /**
         * 将最大最小值置为0
         */
        private void setMaxOrMin() {
            maxX = 0;
            minX = 0;

            maxY = 0;
            minY = 0;
        }

        private void drawHandlerWritingBox() {

            if (bitmap != null) {
                bitmap.recycle();
            }

            cavasViewSize = width;
            float t = mPaint.getStrokeWidth();

            bitmap = Bitmap.createBitmap((int) cavasViewSize,
                    (int) cavasViewSize, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);

            PathEffect effect = new DashPathEffect(new float[] {5,5,5,5}, 1);

            mPaint.setStrokeWidth(1);
            mPaint.setPathEffect(effect);
            canvas.drawLine(0, 0, cavasViewSize, cavasViewSize, mPaint);
            canvas.drawLine(0, cavasViewSize, cavasViewSize, cavasViewSize,
                    mPaint);
            canvas.drawLine(0, 0, cavasViewSize, 0, mPaint);
            canvas.drawLine(cavasViewSize, 0, 0, cavasViewSize, mPaint);
            canvas.drawLine(1, 0, 1, cavasViewSize, mPaint);
            canvas.drawLine(cavasViewSize - 1, 0, cavasViewSize - 1,
                    cavasViewSize, mPaint);
            canvas.drawLine(0, cavasViewSize / 2, cavasViewSize,
                    cavasViewSize / 2, mPaint);
            canvas.drawLine(cavasViewSize / 2, 0, cavasViewSize / 2,
                    cavasViewSize, mPaint);

            mPaint.setPathEffect(null);
            mPaint.setStrokeWidth(t);

        }

    }

    public interface  PathListener{

        public void onPath(CanvasPath myPath);
    }


    public class Background extends View {

        private Paint mPaint;

        private float lineHight;

        private Canvas mCanvas;

        private float mPaintWidth;

        private Bitmap mBitmap;

        private CursorPar mCursorPar;

        private Timer timer = null;
        private Handler mHandler = null;
        private TimerTask task = null;

        private boolean isS = false;

        private boolean a = true;

        public Background(Context context, final Paint mPaintt, float lineHightt) {
            super(context);
            this.mPaint = mPaintt;
            this.lineHight = lineHightt;
            this.mCursorPar = new CursorPar();
            mCursorPar.setCursocHeight(lineHightt -10);
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            if(mCanvas != null && a) {
                                mPaint.setStrokeWidth(2);
                                if (isS) {
                                    mPaint.setXfermode(null);
                                    isS = false;
                                } else {
                                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode
                                            .CLEAR));
                                    isS = true;
                                    mPaint.setStrokeWidth(4);
                                }

                                mCanvas.drawLine(mCursorPar.getCursorX(), mCursorPar.getCursorY(),
                                        mCursorPar.getCursorX(), mCursorPar.getCursorY() +
                                                mCursorPar.getCursocHeight(), mPaint);
                                mPaint.setXfermode(null);
                                invalidate((int) (mCursorPar.getCursorX() - mPaintWidth),
                                        (int) (mCursorPar.getCursorY() - mPaintWidth),
                                        (int) (mCursorPar.getCursorX() + mPaintWidth),
                                        (int) (mCursorPar.getCursorY() + mPaintWidth + mCursorPar.getCursocHeight()));
                            }
                            break;

                    }

                    super.handleMessage(msg);
                }
            };
            task = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            };
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 1000L, 500L);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            if(w > 0 && h > 0) {
                if(mPaint == null) {
                    mPaint = new Paint();
                    mPaint.setStrokeWidth(1);
                    mPaint.setColor(getResources().getColor(R.color.sign_line));
                }
            }
            mBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_4444);
            mCanvas = new Canvas(mBitmap);

            for (int i = 1; i < getHeight() / lineHight; i++) {
                mCanvas.drawLine(0, lineHight * i, getWidth(), lineHight *i, mPaint);
            }

        }

        public void next(float x, float y) {
            if (mCursorPar == null) {
                return;
            }
            if (timer == null || task == null) {
                wakeCustom();
            }
            a = false;
            clearCurrentGB();
            mCursorPar.setCursorX(x);
            mCursorPar.setCursorY(y);
            a = true;
        }

        private void clearCurrentGB() {
            isS = true;
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mPaint.setStyle(Paint.Style.FILL);
            mCanvas.drawRect(mCursorPar.getCursorX() - mPaintWidth, mCursorPar.getCursorY()
                    -mPaintWidth, mCursorPar.getCursorX() + mPaintWidth, mCursorPar.getCursorY() + mPaintWidth
                    + mCursorPar.getCursocHeight(), mPaint);
            mPaint.setXfermode(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1);
            invalidate((int) (mCursorPar.getCursorX() - mPaintWidth),
                    (int) (mCursorPar.getCursorY() - mPaintWidth),
                    (int) (mCursorPar.getCursorX() + mPaintWidth),
                    (int) (mCursorPar.getCursorY() + mPaintWidth + mCursorPar.getCursocHeight()));
        }

        public void wakeCustom() {
            task = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                }
            };
            timer = new Timer();
            timer.scheduleAtFixedRate(task, 1000L, 500L);
        }

        public void finish() {
            if(task != null) {
                task.cancel();
            }

            if(timer != null) {
                timer.cancel();
            }
            task = null;
            timer = null;
            clearCurrentGB();
        }
    }

    /**
     * 用来记录路径的实体类
     */
    public class CanvasPath {
        public static final int C_iCanVasType_space = 1;
        public static final int C_iCanVasType_enter = 2;
        public static final int C_icanVasType_handleWriting = 3;

        private Path path;
        private Bitmap bitmap;

        private float maxX;
        private float maxY;

        private float startX;
        private float startY;

        private float endX;
        private float endY;

        private int canvasType = C_icanVasType_handleWriting;

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public float getMaxX() {
            return maxX;
        }

        public void setMaxX(float maxX) {
            this.maxX = maxX;
        }

        public float getMaxY() {
            return maxY;
        }

        public void setMaxY(float maxY) {
            this.maxY = maxY;
        }

        public float getStartX() {
            return startX;
        }

        public void setStartX(float startX) {
            this.startX = startX;
        }

        public float getStartY() {
            return startY;
        }

        public void setStartY(float startY) {
            this.startY = startY;
        }

        public float getEndX() {
            return endX;
        }

        public void setEndX(float endX) {
            this.endX = endX;
        }

        public float getEndY() {
            return endY;
        }

        public void setEndY(float endY) {
            this.endY = endY;
        }

        public int getCanvasType() {
            return canvasType;
        }

        public void setCanvasType(int canvasType) {
            this.canvasType = canvasType;
        }

        public CanvasPath(Path path) {
            this.path = path;
        }

        public CanvasPath(int canvasType) {
            this.canvasType = canvasType;
        }
    }

    /**
     * 光标的参数类
     */
    public class CursorPar {
        private float cursocHeight;
        private float cursorX;
        private float cursorY;

        /**
         * 获取光标的高度
         * @return
         */
        public float getCursocHeight() {
            return cursocHeight;
        }

        /**
         * 设置光标的高度
         * @param cursocHeight
         */
        public void setCursocHeight(float cursocHeight) {
            this.cursocHeight = cursocHeight;
        }

        /**
         * 获取光标的X位置
         * @return
         */
        public float getCursorX() {
            return cursorX;
        }

        /**
         * 设置光标的X位置
         * @param cursorX
         */
        public void setCursorX(float cursorX) {
            this.cursorX = cursorX;
        }

        /**
         * 获取光标的Y位置
         * @return
         */
        public float getCursorY() {
            return cursorY;
        }

        /**
         * 设置光标的Y位置
         * @param cursorY
         */
        public void setCursorY(float cursorY) {
            this.cursorY = cursorY;
        }

        /**
         * 获取光标的颜色
         * @return
         */
        public int getCursorColor() {
            return cursorColor;
        }

        /**
         * 设置光标的颜色
         * @param cursorColor
         */
        public void setCursorColor(int cursorColor) {
            this.cursorColor = cursorColor;
        }

        private int cursorColor = Color.BLACK;

    }

    public int dip2px(Context c,float px) {
        float de = c.getResources().getDisplayMetrics().density;
        return (int) (px * de + 0.5f);
    }


}
