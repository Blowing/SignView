package com.wujie.signview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.wujie.signview.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Troy on 2017-9-29.
 */

public class DrawSignView {


    /**
     * 用来写字的View
     */
    public class CanvasView extends View {

        public CanvasView(Context context) {
            super(context);
        }

        public CanvasView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
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

        public Background(Context context, Paint mPaintt, float lineHightt) {
            super(context);
            this.mPaint = mPaint;
            this.lineHight = lineHightt;
            this.mCursorPar = new CursorPar();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
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

}
