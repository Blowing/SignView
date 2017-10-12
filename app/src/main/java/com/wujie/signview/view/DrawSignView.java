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
import android.widget.Toast;

import com.wujie.signview.R;

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
