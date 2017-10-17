package com.wujie.signview.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wujie.signview.R;
import com.wujie.signview.database.SignDataBase;
import com.wujie.signview.model.SignEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by Troy on 2017-9-29.
 */

public class DrawSignView {



    public static final int thight = 80;

    public View getView() {
        return view;
    }

    private View view = null;

    LinearLayout showColor;
    private Activity baseActivity;

    private long currUserID;
    private int canvasWidth = 0;
    private int canvasHeight = 0;

    SignDataBase signData;
    private SignView signView;
    String signName = null;

    private Bitmap sigleBitmap = null;

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
                switch (msg.what) {
                    case 1:
                        myPath.setMaxX(maxX / narrowNum);
                        myPath.setMaxY(maxY / narrowNum);
                        if(pathListener != null) {
                            pathListener.onPath(myPath);
                        }
                        // 设置 当前view不属于书写状态
                        isHandlerWriting = false;
                        mPath.reset();
                        mmPath = new Path();
                        setMaxOrMin();
                        invalidate();
                        isFirst = true;
                        break;
                }
                super.handleMessage(msg);
            }
        };

        public void clearCanvasView() {
            if (timer != null) {
                timer.cancel();
            }

            isHandlerWriting = false;
            mPath.reset();
            mmPath = new Path();
            setMaxOrMin();
        }

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

    /**
     * 显示签章的View
     */
    public class SignView extends View {

        List<CanvasPath> signPathList = new ArrayList<>();
        CanvasPath currTemp;

        public SignView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (currTemp != null) {
                canvas.drawBitmap(currTemp.getBitmap(), currTemp.getStartX(),
                        currTemp.getStartY(), null);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return super.onTouchEvent(event);
        }

        /**
         * 加入一个 签章数据
         */
        public void addSignPath(CanvasPath signPath) {
            if (signPathList != null) {
                for (CanvasPath tSignPath : signPathList) {
                    float stX = tSignPath.getStartX();
                    float stY = tSignPath.getStartY();
                    if ((stX + signPath.getBitmap().getWidth()) > getWidth()) {
                        stX = getWidth() - signPath.getBitmap().getWidth();
                    }
                    if ((stY + signPath.getBitmap().getHeight()) > getHeight()) {
                        stY = getHeight() - signPath.getBitmap().getHeight();
                    }
                    signPath.setStartX(stX);
                    signPath.setStartY(stY);
                    signPath.setEndX(signPath.getStartX() + signPath.getBitmap().getWidth());
                    signPath.setEndY(signPath.getStartY() + signPath.getBitmap().getHeight());
                }
            }
            signPathList.clear();
            signPathList.add(signPath);
            currTemp = signPath;
            invalidate();
        }

        public CanvasPath getSignPath() {
            if (signPathList != null && signPathList.size() > 0) {
                return  signPathList.get(0);
            }
            return  null;
        }

        private void clearAll() {
            signName = "";
            signPathList.clear();
            currTemp = null;
            invalidate();
        }

        /**
         * 移动CanvasPath dx dy
         */

        public void moveSignBitmap(CanvasPath canvasPath, float dx, float dy) {
            int width = getWidth();
            int height = getHeight();

            if (canvasPath.getStartX() + dx > 0 && canvasPath.getStartX() + dx <
                    width - canvasPath.getBitmap().getWidth()) {
                canvasPath.setStartX(canvasPath.getStartX() + dx);
            } else  if (canvasPath.getStartX() + dx < 0) {
                canvasPath.setStartX(0);
            } else {
                canvasPath.setStartX(width - canvasPath.getBitmap().getWidth());
            }

            if (canvasPath.getStartY() + dy > 0 && canvasPath.getStartY() + dy <
                    height - canvasPath.getBitmap().getHeight()) {
                canvasPath.setStartY(canvasPath.getStartY() + dy);
            } else if (canvasPath.getStartY() + dy < 0) {
                canvasPath.setStartY(0);
            } else {
                canvasPath.setStartY(height - canvasPath.getBitmap().getHeight());
            }

            canvasPath.setEndX(canvasPath.getStartX() + canvasPath.getBitmap().getWidth());
            canvasPath.setEndY(canvasPath.getStartY() + canvasPath.getBitmap().getHeight());

            currTemp = canvasPath;
            invalidate();
        }

    }


    public class WriteLineView extends View  {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mPaint;
        private float lineHeight;
        private float lineWidth;
        private Bitmap tempBitmap;
        private Canvas tempCanvas;
        private float paintWidth;
        private Context context;

        /**
         * 每个字之间的距离
         */
        private int fontSpace = 5;
        /**
         * 每一行距离顶点的基础距离
         */
        private int spaceY = 7;

        /**
         * 记录书写的字的信息 行列排列 数据字典中 外层list为行 里面list为列
         */
        private List<ArrayList<CanvasPath>> listPathLine = new ArrayList<>();

        /**
         * 当前的行数 从1开始计数
         */
        private int currLineNum = 1;

        /**
         * 当前的列数 从0开始
         */
        private int currIndexNum = 0;

        /**
         * 当前View最多可以显示的行数
         */
        private int maxLineNum = 0;

        public WriteLineView(Context context, Paint mPaint, float lineHeight) {
            super(context);
            this.context = context;
            this.mPaint = mPaint;
            this.lineHeight = lineHeight;
            this.paintWidth = mPaint.getStrokeWidth();
        }

        public float[] getMaxXY() {
            float maxY = 0;
            float maxX = 0;

            if(listPathLine != null) {
                for (ArrayList<CanvasPath> t:
                     listPathLine) {
                    if(t != null) {
                        for (CanvasPath path : t) {
                            if (path.getCanvasType() == CanvasPath.C_icanVasType_handleWriting) {
                                maxY = Math.max(maxY, path.getEndY());
                                maxX = Math.max(maxX, path.getEndX());
                            }
                        }
                    }
                }
            }
            float[] ss = new float[2];
            ss[0] = maxX;
            ss[1] = maxY;
            return  ss;
        }

        public  void write(CanvasPath mPath, WriteListener writeListener) {
            if (mBitmap == null || mCanvas == null) {
                this.mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config
                        .ARGB_4444);
                mCanvas = new Canvas(mBitmap);
            }

            if ((fontSpace + mPath.getMaxX()) > getWidth()) {
                Toast.makeText(context, "单个字超出了画布的长度", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mCanvas != null) {
                int tempCurrLineNum = currLineNum;
                int tempCurrIndexNum = currIndexNum;
                // 这里判断输入是否超出了边界===Y轴方向
                CanvasPath aa = getCanvasPath(currLineNum, currIndexNum -1);
                if (aa != null) {
                    if ((aa.getEndX() + mPath.getMaxX()) > getWidth()) {
                        if ((aa.getEndY() -spaceY + lineHeight) > getHeight()) {
                            Toast.makeText(context, "达到输入上限不能再输入了",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

            }
        }

        /**
         * 添加一个字到记录的路径中去
         * @param tlineNum
         * @param tlineIndexNum
         * @param path
         */
        private void addCanvasPath(int tlineNum, int tlineIndexNum, CanvasPath path) {
            /**
             * 中间跳过的行数加上
             */
            if (listPathLine.size() < tlineNum) {
                for (int i = listPathLine.size(); tlineNum > i; i++) {
                    ArrayList<CanvasPath> listLineItem = new ArrayList<>();

                    CanvasPath canvasT = new CanvasPath(CanvasPath.C_iCanVasType_enter);
                    canvasT.setStartX(fontSpace);
                    canvasT.setStartY(i * lineHeight + spaceY);
                    canvasT.setEndX(fontSpace);
                    canvasT.setEndY(canvasT.getStartY()+ lineHeight);
                    canvasT.setMaxX(0);
                    canvasT.setMaxY(lineHeight);
                    listLineItem.add(0, canvasT);

                    listPathLine.add(i, listLineItem);
                }
            }

            ArrayList<CanvasPath> listLineItem = listPathLine.get(tlineNum -1);

            if (listLineItem.size() >= tlineIndexNum) {
                listLineItem.add(tlineIndexNum, path);

                if (tlineIndexNum == 0) {
                    path.setStartX(fontSpace);
                    path.setStartY((tlineNum -1) * lineHeight + spaceY);
                }
                if (tlineIndexNum > 0) {
                    CanvasPath tPath = listLineItem.get(tlineIndexNum -1);
                    path.setStartX(tPath.getEndX());
                    path.setStartY(tPath.getStartY());
                }
            } else {
                float x = 0;
                if (listLineItem.size() == 0) {
                    x = fontSpace;
                } else {
                    x = listLineItem.get(listLineItem.size() -1).getEndX();
                }

                float y = (tlineNum -1) * lineHeight + spaceY;
                for (int i = listLineItem.size(); i < tlineIndexNum; i++) {
                    CanvasPath canvasT = new CanvasPath(CanvasPath.C_iCanVasType_space);
                    canvasT.setStartX(x);
                    canvasT.setStartY(y);
                    canvasT.setEndX(x + lineWidth);
                    canvasT.setEndY(y + lineHeight);
                    canvasT.setMaxX(lineWidth);
                    canvasT.setMaxY(lineHeight);
                    x = x + lineWidth;
                    listLineItem.add(i, canvasT);
                }
                path.setStartX(x);
                path.setStartY(y);
                listLineItem.add(tlineIndexNum, path);
            }
            path.setStartX(x);
            path.setStartY(y);
            listLineItem.add(tlineIndexNum, path);

        }

        /**
         * 重画 第lineNum行的 第lineIndexNum个以后的元素
         * @param lineNum
         * @param lineIndexNum
         */
        private void redrawLine(int lineNum, int lineIndexNum) {
            if (listPathLine != null && listPathLine.size() >= lineNum) {
                ArrayList<CanvasPath> listLineItem = listPathLine.get(lineNum - 1);
                if (listLineItem != null && listLineItem.size() > lineIndexNum) {
                    CanvasPath sPath = listLineItem.get(lineIndexNum);
                    float tempCurrX = sPath.getStartX();
                    float tempCurrY = sPath.getStartY();

                    int tempX = (int) Math.ceil(sPath.maxX + mPaint.getStrokeWidth());
                    int tempY = (int) Math.ceil(lineHeight);

                    tempBitmap = Bitmap.createBitmap(tempX, tempY, Bitmap.Config.ARGB_4444);
                    tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawPath(sPath.getPath(), mPaint);
                    sPath.setBitmap(tempBitmap);

                    for (int i = lineIndexNum; listPathLine.size() > i; i++) {
                        CanvasPath tPath = listLineItem.get(i);

                        if (tPath.getCanvasType() == CanvasPath.C_iCanVasType_space) {
                            listLineItem.remove(i);
                            i--;
                            if (tempCurrX < tPath.getEndX()) {
                                currIndexNum++;
                                return;
                            }
                        }

                        if (tempCurrX < tPath.getStartX()) {
                            currIndexNum++;
                            return;
                        }
                        if (tempCurrX + tPath.getMaxX() > getWidth()) {
                            refreshBackContent(tempCurrX, tempCurrY);
                            // 这里将余下的加入下一行
                            List<CanvasPath> listLineSubItem = new ArrayList<>();
                            for (int j = i; j < listLineItem.size(); j++) {
                                listLineSubItem.add(listLineItem.remove(j));
                            }
                            addToNextLine(lineNum + 1, listLineSubItem);
                            redrawLine(lineNum+1);
                            if (lineIndexNum == i) {
                                currIndexNum++;
                                currIndexNum = 1;
                            } else {
                                currIndexNum++;
                            }
                            return;
                        }

                        if (tPath.getCanvasType() == CanvasPath.C_icanVasType_handleWriting) {
                            tPath.setStartX(tempCurrX);
                            tPath.setStartY(tempCurrY);
                            tPath.setEndX(tempCurrX + tPath.getMaxX() + fontSpace);
                            tPath.setEndY(tempCurrY + lineHeight);
                            tempCurrX = tempCurrY + tPath.getMaxX() + fontSpace;
                            refreshBackContent(tPath);
                            if (tPath.getStartY() - spaceY > getHeight() - lineHeight) {
                                Toast.makeText(context, "达到输入上限。", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mCanvas.drawBitmap(tPath.getBitmap(), tPath.getStartX(), tPath
                                    .getStartY(), null);
                        }
                        currIndexNum++;

                    }
                }



            }
        }

        /**
         * 重画 参数行
         */
        private void redrawLine(int lineNum) {
            if (listPathLine != null && listPathLine.size() >= lineNum) {
                ArrayList<CanvasPath> listLineItem = listPathLine.get(lineNum - 1);
                if (listLineItem == null) {
                    listPathLine.remove(lineNum -1);
                    return;
                }
                float tempCurrX = fontSpace;
                float tempCurrY = lineHeight * (lineNum -1) + spaceY;
                for (int i = 0; listLineItem.size() > i; i++) {
                    CanvasPath tPath = listLineItem.get(i);
                    if (tPath.getCanvasType() == CanvasPath.C_iCanVasType_space) {
                        listLineItem.remove(i);
                        i--;
                        if (tempCurrX < tPath.getEndX()) {
                            return;
                        }
                    }

                    if (tempCurrX < tPath.startX) {
                        return;
                    }

                    if (tempCurrX + tPath.getMaxX() > getWidth()) {
                        refreshBackContent(tempCurrX, tempCurrY);
                        List<CanvasPath> listLineSubItem = new ArrayList<>();
                        for (int j = i; j < listLineItem.size(); j++) {
                            listLineSubItem.add(listLineItem.remove(j));
                        }
                        addToNextLine(lineNum + 1, listLineSubItem);
                        redrawLine(lineNum + 1);
                        return;
                    }

                    if (tPath.getCanvasType() == CanvasPath.C_icanVasType_handleWriting) {
                        tPath.setStartX(tempCurrX);
                        tPath.setStartY(tempCurrY);
                        tPath.setEndX(tempCurrX + tPath.getMaxX() + fontSpace);
                        tPath.setEndY(tempCurrY + lineHeight);
                        tempCurrX = tempCurrX + tPath.getMaxX() + fontSpace;
                        refreshBackContent(tPath);
                        if (tPath.getStartY() - spaceY > getHeight() - lineHeight) {
                            Toast.makeText(context, "达到输入框上限。", Toast.LENGTH_SHORT).show();
                        }
                        mCanvas.drawBitmap(tPath.getBitmap(), tPath.getStartX(),
                                tPath.getStartY(), null);
                    }
                }

            }
        }

        /**
         * 将listLineSubItem 加入到lineNum的行中
         * @param lineNum
         * @param listLineSubItem
         */
        private void addToNextLine(int lineNum, List<CanvasPath> listLineSubItem) {
            if (listLineSubItem == null || listLineSubItem.size() == 0) {
                return;
            }
            /**
             * 中间跳过的行数加上
             */
            if (listPathLine.size() < lineNum) {
                for (int i = listPathLine.size(); lineNum > i ; i++) {
                    ArrayList<CanvasPath> listLineItem = new ArrayList<>();
                    listPathLine.add(i, listLineItem);
                }
            }

            ArrayList<CanvasPath> listLineItem = listPathLine.get(lineNum -1);
            for (int i = 0; i < listLineSubItem.size(); i++) {
                CanvasPath tempPath = listLineSubItem.get(i);
                tempPath.clearSet();
                listLineItem.add(i, tempPath);
            }
        }

        /**
         * 清空 参数位置的内容
         * @param tPath
         */
        private void refreshBackContent(CanvasPath tPath) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mPaint.setStyle(Paint.Style.FILL);
            mCanvas.drawRect(tPath.getStartX() - paintWidth, tPath.getStartY() - paintWidth,
                    tPath.getEndX() + paintWidth, tPath.getEndY() + paintWidth, mPaint);
            mPaint.setXfermode(null);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        private void refreshBackContent(float currx, float currY) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mPaint.setStyle(Paint.Style.FILL);
            mCanvas.drawRect(currx - paintWidth, currY - paintWidth, getRight(),
                    currY + lineHeight, mPaint);
            mPaint.setXfermode(null);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        /**
         * 根据坐标来设置索引
         */
        public void setIndexByStation(float rx, float ry,
                                      onIndexChangeListener indexChangeListener) {
            int[] location = new int[2];
            // 获取view相对于屏幕的绝对位置
            this.getLocationOnScreen(location);
            int sx = location[0];
            int sy = location[1];
            // 这里根据屏幕点的位置 计算出相对于相对于写字（本子）view的位置
            float x = rx - sx;
            float y = ry - sy;

            int width = getWidth();
            int height = getHeight();

            //超出范围直接不予计算
            if (x > width || x < 0 || y < 0 || y > height) {
                Toast.makeText(getContext(), "点击输入区域移动光标.",Toast.LENGTH_SHORT).show();
                return;
            }
            calculateStation(x, y);

            addSpace(currLineNum, currIndexNum);
            CanvasPath c = getCanvasPath(currLineNum, currIndexNum -1);
            if (c == null) {
                Toast.makeText(getContext(), "光标移动到了顶部", Toast.LENGTH_SHORT);
                indexChangeListener.indexChanged(fontSpace, spaceY);
            } else {
                indexChangeListener.indexChanged(c.getEndX(), c.getStartY());
            }

        }

        private boolean calculateStation(float x, float y) {
            int temLine = (int) Math.ceil(y / lineHeight);
            int maxLine = (int) Math.floor(getHeight() / lineHeight);

            if (temLine > maxLine) {
                return false;
            }

            // 在点击区域内有输入的内容
            if (listPathLine.size() >= temLine) {
                ArrayList<CanvasPath> tListItem = listPathLine.get(temLine -1);
                float tx = 0;
                for (int i = 0; i < tListItem.size(); i++) {
                    CanvasPath t = tListItem.get(i);
                    if (x > t.getStartX() && x < t.getEndX()) {
                        currIndexNum = i;
                        currLineNum = temLine;
                        return  true;
                    }

                    // 点击两字之间的空白区域
                    if (tx > x && x < t.getStartX()) {
                        currIndexNum = i ;
                        currLineNum = temLine;
                        return true;
                    }

                    tx = t.getEndX();
                }
                // 这种情况是点击行的末尾， 后面没有字的情况
                if (x >= tx) {
                    float tempDx = x - tx;
                    int tempDIndex = (int) Math.floor(tempDx / lineWidth );
                    currIndexNum = tListItem.size() + tempDIndex;
                    currLineNum = temLine;
                    return  true;
                }
            } else {
                // 这种情况就是点击到没数据的区域了
                int temw = (int) Math.floor(x / lineWidth);
                currIndexNum = temw;
                currLineNum = temLine;
                return true;
            }
            Toast.makeText(context, "无效点击", Toast.LENGTH_SHORT).show();
            return false;
        }

        private void addSpace(int tLineNum, int tLineIndexNum) {
            /**
             * 中间跳过的行数加上
             */
            if (listPathLine.size() < tLineNum) {
                for (int i = listPathLine.size(); i < tLineNum; i++) {
                    ArrayList<CanvasPath> listLineItem = new ArrayList<>();
                    // 每一行 加上一个换行
                    CanvasPath canvasT = new CanvasPath(CanvasPath.C_iCanVasType_enter);
                    canvasT.setStartX(fontSpace);
                    canvasT.setStartY(i * lineHeight + spaceY);
                    canvasT.setEndX(fontSpace);
                    canvasT.setEndY(canvasT.getStartY() + lineHeight);
                    canvasT.setMaxX(0);
                    canvasT.setMaxY(lineHeight);

                    listLineItem.add(0, canvasT);

                    listPathLine.add(i, listLineItem);
                }
            }

            ArrayList<CanvasPath> listLineItem = listPathLine.get(tLineNum - 1);

            if (tLineIndexNum >= listLineItem.size()) {
                float x = 0;
                if (listLineItem.size() == 0) {
                    x = fontSpace;
                } else {
                    x = listLineItem.get(listLineItem.size() -1).getEndX();
                }
                float y = (tLineNum - 1) * lineHeight + spaceY;
                for (int i = listLineItem.size(); i < tLineIndexNum; i++) {
                    CanvasPath canvasT = new CanvasPath(CanvasPath.C_iCanVasType_space);
                    canvasT.setStartX(x);
                    canvasT.setStartY(y);
                    canvasT.setEndX(x + lineWidth);
                    canvasT.setEndY(y + lineHeight);
                    canvasT.setMaxX(lineWidth);
                    canvasT.setMaxY(lineHeight);
                    x = x +lineWidth;
                    listLineItem.add(i, canvasT);
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }

        public void cleanAll(RevokeListener revokeListener) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            this.mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
            mCanvas = new Canvas(mBitmap);
            listPathLine.clear();
            currLineNum= 1;
            currIndexNum = 0;
            if (revokeListener != null) {
                CanvasPath c = new CanvasPath(CanvasPath.C_icanVasType_handleWriting);
                c.setStartX(fontSpace);
                c.setStartY(spaceY);
                revokeListener.revoked(c);
            }
            invalidate();
        }
        /**
         * 撤销一个字
         */
        public void revoke(RevokeListener revokeListener) {
            // 在一行的后面 索引直接 减小索引
            if (currIndexNum > 0) {
                currIndexNum--;
            } else if (currIndexNum == 0) {
                // 已经在第一行了不能再移动了
                if (currLineNum == 1) {
                    return;
                }
                currLineNum--;
                currIndexNum = getLineSize(currLineNum) -1;
                if (currIndexNum < 0) {
                    currIndexNum = 0;
                }

            } else {
                return;
            }

        }

        /**
         *根据位置 得到 路径
         */
        private CanvasPath getCanvasPath(int lineNum, int indexNum) {
            if (lineNum < 1) {
                lineNum = 1;
            }
            if (indexNum < 0) {
                indexNum = 0;
            }
            if (listPathLine.size() < lineNum) {
                return null;
            }
            ArrayList<CanvasPath> tListPath = listPathLine.get(lineNum - 1);
            if (tListPath == null || tListPath.size() < (indexNum + 1)) {
                return null;
            }

            return tListPath.get(indexNum);

        }

        /**
         * 根据位置 得到路径 并且删除
         * @param lineNum
         * @param indexNum
         * @return
         */
        private CanvasPath getCanvasPathAndRemove(int lineNum, int indexNum) {
            if (lineNum < 1) {
                lineNum = 1;
                return null;
            }
            if (indexNum < 0) {
                indexNum = 0;
                return null;
            }

            if ( listPathLine.size() <lineNum) {
                return null;
            }

            ArrayList<CanvasPath> tListPath = listPathLine.get(lineNum -1);
            if (tListPath == null || tListPath.size() < (indexNum + 1)) {
                return  null;
            }
            return  tListPath.remove(indexNum);
        }

        /**
         * 得到某一行的字数
         * @param lineNum
         * @return
         */
        private int getLineSize(int lineNum) {
            if (lineNum < 1) {
                return  -1;
            }
            if (listPathLine.size() <lineNum) {
                return -1;
            }
            return listPathLine.get(lineNum - 1).size();
        }

        public Bitmap getBitmap() {
            return  mBitmap;
        }
    }

    public interface PaintParamsChangeListener {
        public void makeChange(BasePaintParameters basePaintParameters);
    }

    /**
     * 画笔的基础参数
     */
    public class BasePaintParameters {
        /**
         * 字和字之间的距离
          */
        private float fontSpacing = 5;

        private float baseSpacing = 20;

        /**
         * 字体缩小的倍数
         */
        private int fontSize = 10;

        /**
         * 笔记宽度
         */
        private float pointWeight = 15;

        public float getPointWeight() {
            return  pointWeight;
        }

        private int paintColor = 0xFFFF0000;

        private int getPaintColor() {
            return paintColor;
        }

        private int fontHeight = 50;

        public int getFontHeight() {
            return fontHeight;
        }

        public void setFontHeight(int fontHeight) {
            this.fontHeight = fontHeight;
        }

        private PaintParamsChangeListener changeListener;

        public BasePaintParameters() {
            baseSpacing = baseSpacing / fontSize;
            paintColor = baseActivity.getResources().getColor(R.color.sign_black);
        }
    }

    public interface RevokeListener{
        public void revoked(CanvasPath pp);
    }

    public interface onIndexChangeListener {
        public void indexChanged(float x, float y);
    }


    public interface WriteListener {
        public  void writed(CanvasPath pp);
    }


    public interface  PathListener{

        public void onPath(CanvasPath myPath);
    }

    public interface ColorChangeListener {
        public void colorChanged(int colorId);
    }

    public class ControlView extends RelativeLayout {

        private void startDragging(CanvasPath signPath, int x, int y) {
            stopDragging(0, 0);
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            mWindowParams.x = x;
            mWindowParams.y = y;

            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_DITHER;
            mWindowParams.format = PixelFormat.TRANSLUCENT;
            mWindowParams.windowAnimations = 0;

            ImageView v = new ImageView(getContext());
            v.setImageBitmap(signPath.getBitmap());

            Vibrator mVibrator = (Vibrator) baseActivity.getSystemService(Activity
                    .VIBRATOR_SERVICE);
            mVibrator.vibrate(100);
            mWindowManager = (WindowManager) getContext().getSystemService("window");
            mWindowManager.addView(v, mWindowParams);
            mDragView = v;
        }

        private void dragView(int dx, int dy) {
            float alpha = 1.0f;
            mWindowParams.alpha = alpha;
            mWindowParams.y = mWindowParams.y + dy;
            mWindowParams.x = mWindowParams.x + dx;

            mWindowManager.updateViewLayout(mDragView, mWindowParams);
        }

        ImageView mDragView;
        private void stopDragging(float dx, float dy) {
            if (mDragView != null) {
                WindowManager wm = (WindowManager) getContext().getSystemService("window");
                wm.removeView(mDragView);
                mDragView.setImageDrawable(null);
                mDragView = null;
            }
            if (dx != 0 || dy != 0) {
                signView.moveSignBitmap(moveSignPath, dx, dy);
            }
            moveSignView.setVisibility(View.VISIBLE);
        }


        private WindowManager mWindowManager;
        private WindowManager.LayoutParams mWindowParams;

        private Paint canvasPaint;
        private Paint gbPaint;
        private Paint writLinePaint;
        private Paint backgroundPaint;
        private Context context;

        private BasePaintParameters basePaintParameters;

        WriteLineView writeLineView ;
        CanvasView canvasView;
        Background background;

        float currX = 5;
        float currY = 7;

        private float lineHeight;

        private List<CanvasPath> historyCanvas;

        public ControlView(Context context) {
            super(context);
            this.context = context;
            basePaintParameters = new BasePaintParameters();
            initBackgroundPaint();// 初始化背景 画笔
            initCanvasPaint();// 初始化画板
            initCustomerPaint();//初始 光标 画笔
            initPaint();// 初始化 写字画笔
            addAllView();
            setWillNotDraw(false);

        }

        private void addAllView() {
            this.removeAllViews();
            lineHeight = thight + 10;

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();

            canvasView = new CanvasView(context, canvasPaint, width);
            RelativeLayout.LayoutParams l = new LayoutParams(width, width);
            l.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            l.addRule(RelativeLayout.CENTER_HORIZONTAL);
            canvasView.setLayoutParams(l);

            canvasView.setPathListener(new PathListener() {
                @Override
                public void onPath(CanvasPath myPath) {
                    writeLineView.write(myPath, new WriteListener() {
                        @Override
                        public void writed(CanvasPath pp) {
                            if (pp.getStartY() - 7 > writeLineView.getHeight() - lineHeight) {
                                Toast.makeText(context, "达到输入上限", Toast.LENGTH_SHORT);
                                return;
                            }
                            currX = pp.getEndX();
                            currY = pp.getStartY();
                            background.next(currX, currY); // 移动鼠标

                            if (historyCanvas != null && historyCanvas.size() > 0) {
                                String [] userID = { currUserID+ "", SignEntity
                                        .C_iSignPopType_Cursor +""};
                                SignEntity entity = signData.getSignEntityByUserIDAndPopType
                                        (userID);
                                if (entity == null) {
                                    entity = new SignEntity();
                                    entity.userId = currUserID +"";
                                    entity.popType = SignEntity.C_iSignPopType_Cursor;
                                    entity.hasState = 0;
                                    signData.insert(entity);

                                    Intent intent = new Intent();
                                    intent.setAction("com.seeyon.mobile.notification");
                                    intent.putExtra("content", "长按屏幕可以修改光标的起始位置");
                                    baseActivity.sendBroadcast(intent);
                                }
                                if (historyCanvas == null) {
                                    historyCanvas = new ArrayList<CanvasPath>();
                                }
                                historyCanvas.add(pp);
                            }
                        }
                    });
                }
            });

            setOnLongClickListener(null);
            if (canvasWidth == 0) {
                canvasWidth = LayoutParams.MATCH_PARENT;
            }

            if (canvasHeight == 0) {
                canvasHeight = LayoutParams.MATCH_PARENT;
            }

            LayoutParams ll = new LayoutParams(canvasWidth, canvasHeight);
            ll.addRule(RelativeLayout.CENTER_HORIZONTAL);
            writeLineView = new WriteLineView(context, writLinePaint, lineHeight);
            background = new Background(context, gbPaint, lineHeight);

            signView = new SignView(context);
            writeLineView.setLayoutParams(ll);
            background.setLayoutParams(ll);
            signView.setLayoutParams(ll);

            this.addView(writeLineView, 0);
            this.addView(background, 0);
            this.addView(canvasView, 0);
            this.addView(signView, 0);

            invalidate();

        }

        public void finish() {

        }

        public void revoke() {
            writeLineView.revoke(new RevokeListener() {
                @Override
                public void revoked(CanvasPath pp) {
                    currX = pp.getStartX();
                    currY = pp.getStartY();
                    background.next(currX, currY);
                }
            });
        }

        public void cleanAll() {
            writeLineView.cleanAll(new RevokeListener() {
                @Override
                public void revoked(CanvasPath pp) {
                    currX = pp.getStartX();
                    currY = pp.getStartY();
                    background.next(currX, currY);
                }
            });

            if (signView != null) {
                signView.clearAll();
            }
        }

        public void cleanSign() {
            if (signView != null) {
                signView.clearAll();
            }
        }

        public Bitmap getWriteBitMap() {
            Bitmap bb = writeLineView.getBitmap();
            float[] maxXY = writeLineView.getMaxXY();

            CanvasPath tsignPath = signView.getSignPath();
            if (tsignPath == null && maxXY[0] == 0 && maxXY[1] == 0) {
                return  null;
            }
            if (bb == null && tsignPath != null) {
                return tsignPath.getBitmap();
            }
            float signEndX = 0;
            float signEndY = 0;
            if (tsignPath != null) {
                signEndX = tsignPath.getStartX() + tsignPath.getBitmap().getWidth();
                signEndY = tsignPath.getStartY() + tsignPath.getBitmap().getHeight();
            }

            float maxX = maxXY[0];
            float maxY = maxXY[1];

            int amaxX = (int) Math.ceil(Math.max(signEndX, maxX));
            int amaxY = (int) Math.ceil(Math.max(signEndY, maxY));

            Bitmap mBitMap = Bitmap.createBitmap(amaxX, amaxY, Bitmap.Config.ARGB_4444);
            Canvas c = new Canvas(mBitMap);
            if (tsignPath != null) {
                c.drawBitmap(tsignPath.getBitmap(), tsignPath.getStartX(), tsignPath.getStartY(),
                        null);
            }
            if (bb != null) {
                c.drawBitmap(bb, 0, 0, null);
            }

            return mBitMap;
        }

        public void setColor(int color) {
             canvasPaint.setColor(color);
            writLinePaint.setColor(color);
        }


        /**
         * 初始化 光标的 画笔
         */
        private  void initCustomerPaint() {
            gbPaint = new Paint();
            gbPaint.setAntiAlias(true);
            gbPaint.setDither(true);
            gbPaint.setStyle(Paint.Style.STROKE);
            gbPaint.setStrokeJoin(Paint.Join.ROUND);
            gbPaint.setStrokeCap(Paint.Cap.ROUND);

            gbPaint.setStrokeWidth(2);
            gbPaint.setColor(Color.BLACK);
        }
        /**
         * 初始化写字的画笔
         */
        private void initPaint() {
            writLinePaint = new Paint();
            writLinePaint.setAntiAlias(true);
            writLinePaint.setDither(true);
            writLinePaint.setStyle(Paint.Style.STROKE);
            writLinePaint.setStrokeCap(Paint.Cap.ROUND);
            writLinePaint.setStrokeJoin(Paint.Join.ROUND);

            writLinePaint.setStrokeWidth(2);
            writLinePaint.setColor(basePaintParameters.getPaintColor());
        }

        /**
         * 初始化背景线条 画笔
         */
        private void initBackgroundPaint() {
            backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setDither(true); //设置是否抖动
            backgroundPaint.setStyle(Paint.Style.STROKE);
            backgroundPaint.setStrokeJoin(Paint.Join.ROUND);
            backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
            backgroundPaint.setStrokeWidth(1);
            backgroundPaint.setColor(Color.GREEN);
        }

        /**
         * 初始化画板的画笔
         */
        private void initCanvasPaint() {
            canvasPaint = new Paint();
            canvasPaint.setAntiAlias(true);
            canvasPaint.setDither(true);
            canvasPaint.setStyle(Paint.Style.STROKE);
            canvasPaint.setStrokeJoin(Paint.Join.ROUND);
            canvasPaint.setStrokeWidth(basePaintParameters.getPointWeight());
            canvasPaint.setColor(basePaintParameters.getPaintColor());
        }

        public void addSignView(Bitmap bitmap) {
            String[] userID = { currUserID + "",
                SignEntity.C_iSignPopType_Sign + ""};
            SignEntity entity = signData.getSignEntityByUserIDAndPopType(userID);
            if (entity == null) {
                entity = new SignEntity();
                entity.userId = currUserID+"";
                entity.popType = SignEntity.C_iSignPopType_Sign;
                entity.hasState = 0;
                signData.insert(entity);

                Intent intent = new Intent();
                intent.setAction("com.seeyon.mobile.notifacation");
                intent.putExtra("content", "按住印章，选择盖章位置");
                baseActivity.sendBroadcast(intent);
            }
            sigleBitmap = bitmap;
            CanvasPath signPath = new CanvasPath(CanvasPath.C_icanVasType_handleWriting);
            signPath.setBitmap(bitmap);

            float stX = currX;
            float stY = currY + lineHeight;
            if ((currX + bitmap.getWidth()) > writeLineView.getWidth()) {
                stX = writeLineView.getWidth() - bitmap.getWidth();
            }
            if ((currY + lineHeight + bitmap.getHeight()) > writeLineView.getHeight()) {
                stY = writeLineView.getHeight() - bitmap.getHeight();
            }

            signPath.setStartX(stX);
            signPath.setStartY(stY);
            signPath.setEndX(signPath.getStartX()+ bitmap.getWidth());
            signPath.setEndY(signPath.getStartY() + bitmap.getHeight());

            if (historyCanvas == null) {
                historyCanvas = new ArrayList<CanvasPath>();
            }
            historyCanvas.add(signPath);
            signView.addSignPath(signPath);
        }

        /**
         * 是否为移动光标
         */
        private boolean isMoveCuros = false;
        /**
         * 是否为移动签章
         */
        private boolean isMoveSi = false;

        private SignView moveSignView = null;
        private CanvasPath moveSignPath = null;
        /**
         * 记录光标移动的上一个点的X的坐标
         */
        float mx;
        /**
         * 记录光标移动的上一个点的Y的坐标
         */
        float my;

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (onTouchEvent(ev)) {
                canvasView.clearCanvasView();
                return  true;
            }
            boolean a = super.onInterceptTouchEvent(ev);
            return a;
        }

        @Override
        public void setOnLongClickListener(OnLongClickListener l) {
            super.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int tt = dip2px(context, 4);
                    if (Math.abs(maxRX - rx) < tt && Math.abs(maxRY - ry) < tt) {
                        if (!canvasView.isHandlerWriting() && !isMoveSi) {
                            canvasView.clearCanvasView();
                            writeLineView.setIndexByStation(rx, ry, new onIndexChangeListener() {
                                @Override
                                public void indexChanged(float x, float y) {
                                    currX = x;
                                    currY = y;
                                    background.next(currX, currY);
                                }
                            });
                            isMoveCuros = true;
                        }
                    }
                    return false;
                }
            });
        }


        int startDrowX;
        int startDrowY;
        boolean isCondition = false;

        float maxRX;
        float maxRY;

        float rx;
        float ry;
        float orx;
        float ory;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (showColor != null && showColor.getVisibility() == View.VISIBLE) {
                showColor.setVisibility(View.GONE);
            }
            float x = event.getX();
            float y = event.getY();
            rx = event.getRawX();
            ry = event.getRawY();

            int sx = 0;
            int sy = 0;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    maxRX = rx;
                    maxRY = ry;
                    orx = rx;
                    ory = ry;
                    int[] location = new int[2];
                    signView.getLocationOnScreen(location);
                    sx = location[0];
                    sy = location[1];
                    break;
            }
            if (maxRX < rx) {
                maxRX = rx;
            }
            if (maxRY < ry) {
                maxRY = ry;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isMoveCuros = false;
                    isCondition = false;
                    for (CanvasPath path : signView.signPathList) {
                        if (rx > (path.getStartX() + sx)
                                && rx < (path.getEndX() + sx)
                                && ry > (path.getStartY() + sy)
                                && ry < (path.getEndY() + sy)) {
                            moveSignPath = path;
                            startDrowX = (int) (path.getStartX() + sx);
                            startDrowY = (int) (path.getStartY() + sy);
                            isCondition = true;
                            moveSignView = signView;

                            isMoveSi = true;
                            startDragging(moveSignPath, startDrowX, startDrowY);
                            moveSignView.setVisibility(INVISIBLE);

                            isCondition = false;
                            mx = x;
                            my = y;
                            return true;
                        }
                    }

                    mx = x;
                    my = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isMoveSi) {
                        dragView((int)(x -mx), (int) (y - my));
                        mx = x;
                        my = y;
                        return true;
                    }
                    if (isMoveCuros) {
                        return  true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    maxRX = 0;
                    maxRY = 0;
                    mx = x;
                    my = y;
                    if (isMoveSi) {
                        canvasView.clearCanvasView();
                        float dx = rx - orx;
                        float dy = ry - ory;
                        stopDragging(dx, dy);
                        isMoveSi = false;
                        return true;
                    }
                    if (isMoveCuros) {
                        canvasView.clearCanvasView();
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    class SelectControl {
        LinearLayout selectColorBlack;
        LinearLayout selectColorBlue;
        LinearLayout selectColorRed;

        ColorChangeListener colorChangeListener;
        ImageView imgSelecColor;

        public SelectControl(ColorChangeListener colorChangeListener) {
            this.colorChangeListener = colorChangeListener;
            selectColorBlack = (LinearLayout) view.findViewById(R.id.ll_selectClore_black);
            selectColorBlue = (LinearLayout) view.findViewById(R.id.ll_selectClore_blue);
            selectColorRed = (LinearLayout) view.findViewById(R.id.ll_selectClore_red);

            imgSelecColor = (ImageView) view.findViewById(R.id.img_sign_operate_corle);
            initColorSelect();
        }

        public void initColorSelect(int type) {
            switch (type) {
                case SignEntity.C_iSignPiantColor_Black:
                    setSelectBg(selectColorBlack);
                    imgSelecColor.setImageResource(R.drawable.ic_pen_color_black);
                    break;
                case SignEntity.C_iSignPiantColor_Blue:
                    setSelectBg(selectColorBlue);
                    imgSelecColor.setImageResource(R.drawable.ic_pen_color_blue);
                    break;
                case SignEntity.C_iSignPiantColor_Red:
                    setSelectBg(selectColorRed);
                    imgSelecColor.setImageResource(R.drawable.ic_pen_color_red);
                    break;
                default:
                    setSelectBg(selectColorBlack);
                    imgSelecColor.setImageResource(R.drawable.ic_pen_color_black);
                    break;
            }
        }

        public int getColorByType(int type) {
            switch (type) {
                case SignEntity.C_iSignPiantColor_Red:
                    return baseActivity.getResources().getColor(R.color.sign_red);
                case SignEntity.C_iSignPiantColor_Black:
                    return baseActivity.getResources().getColor(R.color.sign_black);
                case SignEntity.C_iSignPiantColor_Blue:
                    return baseActivity.getResources().getColor(R.color.sign_blue);
                default:
                    return baseActivity.getResources().getColor(R.color.sign_black);
            }
        }

        private void setSelectBg(View v) {

        }
        private void callBack(int id) {
            if (showColor.getVisibility() == View.VISIBLE) {
                showColor.setVisibility(View.GONE);
            }
            if (id == R.id.ll_selectClore_black) {
                setSelectBg(selectColorBlack);
                imgSelecColor.setImageResource(R.drawable.ic_pen_color_black);
            } else if (id == R.id.ll_selectClore_blue) {
                setSelectBg(selectColorBlue);
                imgSelecColor.setImageResource(R.drawable.ic_pen_color_blue);
            } else if (id == R.id.ll_selectClore_red) {
                setSelectBg(selectColorRed);
                imgSelecColor.setImageResource(R.drawable.ic_pen_color_red);
            }

            if (colorChangeListener == null) {
                return;
            }

            if (id == R.id.ll_selectClore_black) {
                colorChangeListener.colorChanged(R.color.sign_black);
            } else if (id == R.id.ll_selectClore_blue) {
                colorChangeListener.colorChanged(R.color.sign_blue);
            } else if (id == R.id.ll_selectClore_red) {
                colorChangeListener.colorChanged(R.color.sign_red);
            }
        }


        private void initColorSelect() {
            selectColorBlack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack(R.id.ll_selectClore_black);
                }
            });

            selectColorBlue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack(R.id.ll_selectClore_blue);
                }
            });

            selectColorRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callBack(R.id.ll_selectClore_red);
                }
            });
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

        public void clearSet() {
            startX = 0;
            startY = 0;
            endX = 0;
            endY = 0;
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
