package com.example.dayplanner.statistics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CustomCircularProgressBar extends View {

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private float progress = 0f; // The current progress (0-100)
    private float maxProgress = 100f; // The max progress (100)
    private int progressColor = Color.GREEN;
    private int backgroundColor = Color.GRAY;
    private int progressWidth = 20; // The width of the progress bar
    private int backgroundWidth = 20; // The width of the background circle
    private String progressText = "0%"; // Text inside the circle
    private boolean isDayText = false; // boolean to check if I need to show day number or percentage

    /** This compomnent was made thanks to this video: https://www.youtube.com/watch?v=7BVg8_WR7h4 and AI **/

    public CustomCircularProgressBar(Context context) {
        super(context);
        init();
    }

    public CustomCircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(backgroundWidth);
        backgroundPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        setTextColorBasedOnTheme(this, getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - Math.max(progressWidth, backgroundWidth);

        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        RectF progressRect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(progressRect, -90, (360 * progress) / maxProgress, false, progressPaint);

        canvas.drawText(progressText, centerX, centerY + textPaint.getTextSize() / 3, textPaint);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        this.progressText = Math.round(progress) + "%";
        isDayText = false;
        invalidate();
    }

    public void setProgress(float progress, String customText) {
        this.progress = progress;
        this.progressText = customText;
        isDayText = false;
        invalidate();
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    public void setProgressWidth(int width) {
        this.progressWidth = width;
        progressPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setBackgroundWidth(int width) {
        this.backgroundWidth = width;
        backgroundPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setText(String text) {
        this.progressText = text;
        isDayText = true;
        invalidate();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize);
        invalidate();
    }

    public void setTextStyle(int style) {
        textPaint.setTypeface(Typeface.defaultFromStyle(style));
        invalidate();
    }
    public void setTextColorBasedOnTheme(CustomCircularProgressBar progressBar, Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        int textColor = a.getColor(0, Color.BLACK);
        a.recycle();

        progressBar.setTextColor(textColor);
    }
    public boolean isDayText() {
        return isDayText;
    }
}
