package com.mindmari.PaintApp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawingView extends View
{
    public static final int BRUSH = 1;
    public static final int LINE = 2;
    public static final int RECTANGLE = 3;
    public static final int SQUARE = 5;
    public static final int CIRCLE = 6;
    public static final int TRIANGLE = 7;

    public static final float TOUCH_TOLERANCE = 4;

    //Indicates if you are drawing
    private boolean isDrawing = false;
    //Ночная тема
    private boolean isNightTheme = false;

    private float mStartX;
    private float mStartY;

    private float mx;
    private float my;

    //Ширина DrawingView
    private int widthView;
    //Высота DrawingView
    private int heightView;
    //Текущая выбранная форма рисования
    private int currentShape;
    //Толщина карандаша
    private float strokeWidth = 25;
    //Текущий цвет
    private String currentColor;
    //----------------------------------------------------------------------------------------------
    //Путь для рисования
    private Path path;
    //Paint для рисования и для холста
    private Paint paint;
    //Холст
    private Canvas mCanvas;
    //Bitmap холста
    private Bitmap canvasBitmap;
    //----------------------------------------------------------------------------------------------
    public DrawingView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        setUpDrawing();
    }
    //----------------------------------------------------------------------------------------------
    //Установить цвет рисования
    public void setDrawPaintColor(int paintColor, String strColor)
    {
        currentColor = strColor;
        paint.setColor(paintColor);
    }
    //Получить цвет рисования
    public String getCurrentColor()
    {
        return currentColor;
    }
    //Установить форму рисования
    public void setDrawShape(int shape)
    {
        this.currentShape = shape;
    }
    //Установить картинку
    public void setBitmap(Bitmap bitmapPic)
    {
        canvasBitmap = bitmapPic.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(canvasBitmap);
        invalidate();
    }
    //Получить текущую картинку
    public Bitmap getCanvasBitmap()
    {
        return this.canvasBitmap;
    }
    //Залить всё белым или серым цветом
    public void clearAll()
    {
        canvasBitmap = Bitmap.createBitmap(widthView, heightView, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(canvasBitmap);
        mCanvas.drawColor(isNightTheme ? 0xFF414141 : 0xFFFFFFFF);
        invalidate();
    }
    //Вернуть ширину DrawingView
    public int getWidthView()
    {
        return widthView;
    }
    //Вернуть высоту DrawingView
    public int getHeightView()
    {
        return heightView;
    }
    //Установить толщину рисования
    public void setStrokeWidth(float strokeWidthDialog)
    {
        this.strokeWidth = strokeWidthDialog;
        paint.setStrokeWidth(strokeWidth);
    }
    //----------------------------------------------------------------------------------------------
    private void setUpDrawing()
    {
        //По умолчанию выбран карандаш
        currentShape = 1;
        //Запомнить выбранную тему
        isNightTheme = isNightTheme();

        path = new Path();
        paint = new Paint(Paint.DITHER_FLAG);
        currentColor = isNightTheme ? "#FFFFFFFF" : "#FF000000";
        paint.setColor(isNightTheme ? 0xFFFFFFFF : 0xFF000000);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }
    //----------------------------------------------------------------------------------------------
    //Проверить какая тема установлена в данный момент
    private boolean isNightTheme()
    {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = false;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) isDark = true;
        return isDark;
    }
    //----------------------------------------------------------------------------------------------
    //Задание размера View
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        //Запомнить ширину и высоту DrawingView
        widthView = w;
        heightView = h;
        //Создать Bitmap
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //Поместить Bitmap на холст
        mCanvas = new Canvas(canvasBitmap);
        //Закрасить Bitmap
        mCanvas.drawColor(isNightTheme ? 0xFF414141 : 0xFFFFFFFF);
    }
    //----------------------------------------------------------------------------------------------
    //Отображения рисунка, который нарисован пользователем
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(canvasBitmap, 0, 0, paint);

        if (isDrawing)
        {
            switch (currentShape) {
                case BRUSH:
                    onDrawPen();
                    break;
                case LINE:
                    onDrawLine(canvas);
                    break;
                case RECTANGLE:
                    onDrawRectangle(canvas);
                    break;
                case SQUARE:
                    onDrawSquare(canvas);
                    break;
                case CIRCLE:
                    onDrawCircle(canvas);
                    break;
                case TRIANGLE:
                    onDrawTriangle(canvas);
                    break;
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        mx = event.getX();
        my = event.getY();

        switch (currentShape)
        {
            case BRUSH:
                onTouchEventPen(event);
                break;
            case LINE:
                onTouchEventLine(event);
                break;
            case RECTANGLE:
                onTouchEventRectangle(event);
                break;
            case SQUARE:
                onTouchEventSquare(event);
                break;
            case CIRCLE:
                onTouchEventCircle(event);
                break;
            case TRIANGLE:
                onTouchEventTriangle(event);
                break;
        }
        return true;
    }
    //----------------------------------------------------------------------------------------------
    //Pen
    private void onDrawPen()
    {
        mCanvas.drawPath(path, paint);
    }

    private void onTouchEventPen(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                path.moveTo(mx, my);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(mx, my);
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                path.lineTo(mx, my);
                mCanvas.drawPath(path, paint);
                path.reset();
                break;
        }
        //Перерисовать
        invalidate();
    }
    //----------------------------------------------------------------------------------------------
    // Line
    private void onDrawLine(Canvas canvas)
    {
        float dx = Math.abs(mx - mStartX);
        float dy = Math.abs(my - mStartY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            canvas.drawLine(mStartX, mStartY, mx, my, paint);
        }
    }

    private void onTouchEventLine(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                mCanvas.drawLine(mStartX, mStartY, mx, my, paint);
                break;
        }
        //Перерисовать
        invalidate();
    }
    //----------------------------------------------------------------------------------------------
    // Triangle
    int countTouch = 0;
    float basexTriangle = 0;
    float baseyTriangle = 0;

    private void onDrawTriangle(Canvas canvas)
    {
        if (countTouch < 3)
        {
            canvas.drawLine(mStartX,mStartY,mx,my, paint);
        } else if (countTouch == 3)
        {
            canvas.drawLine(mx,my,mStartX,mStartY, paint);
            canvas.drawLine(mx,my,basexTriangle,baseyTriangle, paint);
        }
    }

    private void onTouchEventTriangle(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                countTouch++;
                if (countTouch == 1)
                {
                    isDrawing = true;
                    mStartX = mx;
                    mStartY = my;
                } else if (countTouch == 3)
                {
                    isDrawing = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                countTouch++;
                isDrawing = false;
                if (countTouch < 3)
                {
                    basexTriangle = mx;
                    baseyTriangle = my;
                    mCanvas.drawLine(mStartX, mStartY, mx, my, paint);
                } else
                {
                    mCanvas.drawLine(mx, my, mStartX, mStartY, paint);
                    mCanvas.drawLine(mx, my, basexTriangle, baseyTriangle, paint);
                    countTouch = 0;
                }
                break;
        }
        //Перерисовать
        invalidate();
    }
    //----------------------------------------------------------------------------------------------
    // Circle
    private void onDrawCircle(Canvas canvas)
    {
        canvas.drawCircle(mStartX, mStartY, calculateRadius(mStartX, mStartY, mx, my), paint);
    }

    private void onTouchEventCircle(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                mCanvas.drawCircle(mStartX, mStartY, calculateRadius(mStartX,mStartY,mx,my), paint);
                break;
        }
        //Перерисовать
        invalidate();
    }

    protected float calculateRadius(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
    //----------------------------------------------------------------------------------------------
    // Rectangle
    private void onDrawRectangle(Canvas canvas)
    {
        drawRectangle(canvas, paint);
    }

    private void onTouchEventRectangle(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                drawRectangle(mCanvas, paint);
                break;
        }
        //Перерисовать
        invalidate();
    }

    private void drawRectangle(Canvas canvas,Paint paint)
    {
        float right = Math.max(mStartX, mx);
        float left = Math.min(mStartX, mx);
        float bottom = Math.max(mStartY, my);
        float top = Math.min(mStartY, my);
        canvas.drawRect(left, top , right, bottom, paint);
    }
    //----------------------------------------------------------------------------------------------
    // Square
    private void onDrawSquare(Canvas canvas)
    {
        onDrawRectangle(canvas);
    }

    private void onTouchEventSquare(MotionEvent event)
    {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                mStartX = mx;
                mStartY = my;
                break;
            case MotionEvent.ACTION_MOVE:
                adjustSquare(mx, my);
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                adjustSquare(mx, my);
                drawRectangle(mCanvas, paint);
                break;
        }
        //Перерисовать
        invalidate();
    }

    private void adjustSquare(float x, float y)
    {
        float deltaX = Math.abs(mStartX - x);
        float deltaY = Math.abs(mStartY - y);

        float max = Math.max(deltaX, deltaY);

        mx = mStartX - x < 0 ? mStartX + max : mStartX - max;
        my = mStartY - y < 0 ? mStartY + max : mStartY - max;
    }
    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    private void addToLog(String msg)
    {
        Log.d("TAG", msg);
    }
    //----------------------------------------------------------------------------------------------
}
