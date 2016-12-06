package com.heihei.hehe.lockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 九宫格锁屏
 * @author zhujinlong@ichoice.com
 */
public class LockView extends View {

    private List<Float[]> points;
    private List<Float[]> selectPoints;
    private Float[] point;
    private Paint paint;
    private int column,ringColor,rightColor,errorColor;
    private boolean isRight = true;
    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public LockView(Context context) {
        this(context,null);
    }

    public LockView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockView);
        ringColor = a.getColor(R.styleable.LockView_ringColor,0xFF303F9F);
        rightColor = a.getColor(R.styleable.LockView_rightColor,0xFF3F51B5);
        errorColor = a.getColor(R.styleable.LockView_errorColor,0xFFFF4081);
        column = a.getInt(R.styleable.LockView_lockColumn,3);
        a.recycle();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int r = w >= h ? h : w;
        super.onMeasure(MeasureSpec.makeMeasureSpec(r,MeasureSpec.getMode(widthMeasureSpec)),
                MeasureSpec.makeMeasureSpec(r,MeasureSpec.getMode(heightMeasureSpec)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(points == null){
            points = new ArrayList<>();
            int cells = column * 2 + 1;
            float cellWidth = w * 1f / cells;
            for (int i = 0; i < cells; i++) {
                for (int j = 0; j < cells; j++) {
                    if(i % 2 != 0 && j % 2 != 0){
                        Float[] f= new Float[]{i * cellWidth + cellWidth * 0.5f,j * cellWidth + cellWidth * 0.5f,
                                cellWidth * 0.5f,i * cells * 1f + j};
                        points.add(f);
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isRight || points == null || points.isEmpty()){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                updatePoints(event,false);
                return true;
            case MotionEvent.ACTION_MOVE:
                updatePoints(event,true);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(selectPoints != null && !selectPoints.isEmpty()){
                    StringBuilder builder = new StringBuilder();
                    for (Float[] f : selectPoints) {
                        builder.append((int) ((float)f[3]));
                    }
                    if(callBack == null || callBack.isRight(builder.toString())){
                        selectPoints.clear();
                        point = null;
                        postInvalidate();
                    }else {
                        point = null;
                        isRight = false;
                        postInvalidate();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                selectPoints.clear();
                                isRight = true;
                                postInvalidate();
                            }
                        },500);
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updatePoints(MotionEvent event,boolean move){
        if(selectPoints == null){
            selectPoints = new ArrayList<>();
        }
        float x = event.getX();
        float y = event.getY();
        boolean invalidate = false;
        for (int i = 0; i < points.size(); i++) {
            Float[] f = points.get(i);
            if(!selectPoints.contains(f) && Math.pow(f[0] - x,2) + Math.pow(f[1] - y,2) <= 2 * Math.pow(f[2],2)){
                selectPoints.add(f);
                invalidate = true;
                break;
            }
        }
        if(!selectPoints.isEmpty() && move){
            point = new Float[]{x,y};
            invalidate = true;
        }
        if(invalidate){
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制点和线
        paint.setColor(isRight ? rightColor : errorColor);
        if(selectPoints != null && !selectPoints.isEmpty()){
            paint.setStyle(Paint.Style.FILL);
            Float[] preFloat = null;
            for (Float[] f : selectPoints) {
                if(preFloat != null){
                    canvas.drawLine(preFloat[0],preFloat[1],f[0],f[1],paint);
                }
                if(f.length > 2){
                    canvas.drawCircle(f[0],f[1],f[2] * 0.3f,paint);
                }
                preFloat = f;
            }
            if(preFloat != null && point != null){
                canvas.drawLine(preFloat[0],preFloat[1],point[0],point[1],paint);
            }
        }
        // 绘制圆环
        paint.setColor(isRight ? ringColor : errorColor);
        if(points != null && !points.isEmpty()){
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(points.get(0)[2] * 0.1f );
            for (int i = 0; i < points.size(); i++) {
                Float[] f = points.get(i);
                canvas.drawCircle(f[0],f[1],f[2] * 0.7f,paint);
            }
        }
    }

    public interface CallBack{
        boolean isRight(String passWord);
    }
}