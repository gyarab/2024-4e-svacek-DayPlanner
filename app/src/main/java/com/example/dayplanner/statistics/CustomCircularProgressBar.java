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
    private boolean isDayText = false; // Flag to check if we need to show day number or percentage

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
        // Initialize the paint objects for progress, background, and text
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

        // Calculate center and radius for the progress circle
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - Math.max(progressWidth, backgroundWidth);

        // Draw the background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw the progress arc
        RectF progressRect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(progressRect, -90, (360 * progress) / maxProgress, false, progressPaint);

        // Draw the text (either percentage or day number)
        canvas.drawText(progressText, centerX, centerY + textPaint.getTextSize() / 3, textPaint);
    }

    // Set the progress value (0-100)
    public void setProgress(float progress) {
        this.progress = progress;
        this.progressText = Math.round(progress) + "%"; // Update the text to show the percentage
        isDayText = false; // Set flag to show percentage
        invalidate(); // Redraw the view with the updated progress
    }

    public void setProgress(float progress, String customText) {
        this.progress = progress;
        this.progressText = customText; // Update the text to show the percentage
        isDayText = false; // Set flag to show percentage
        invalidate(); // Redraw the view with the updated progress
    }

    // Set the maximum progress value
    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    // Set the progress color dynamically
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color); // Update the paint color for the progress
        invalidate(); // Redraw the view with the new color
    }

    // Set the background color dynamically
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color); // Update the background circle color
        invalidate(); // Redraw the view with the new background color
    }

    // Set the width of the progress circle
    public void setProgressWidth(int width) {
        this.progressWidth = width;
        progressPaint.setStrokeWidth(width); // Update the stroke width for the progress
        invalidate(); // Redraw the view with the new stroke width
    }

    // Set the width of the background circle
    public void setBackgroundWidth(int width) {
        this.backgroundWidth = width;
        backgroundPaint.setStrokeWidth(width); // Update the stroke width for the background circle
        invalidate(); // Redraw the view with the new stroke width
    }

    // Set custom text (for displaying the day number)
    public void setText(String text) {
        this.progressText = text; // Update the text to the given string
        isDayText = true; // Set flag to show day number
        invalidate(); // Redraw the view with the new text
    }

    // Method to set the text color dynamically
    public void setTextColor(int color) {
        textPaint.setColor(color);  // Update the text color
        invalidate();  // Redraw the view with the new text color
    }

    // Set the text size in the center of the progress bar
    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize); // Set the text size for the progress text
        invalidate(); // Redraw the view with the new text size
    }

    // Set the text style (e.g., bold, normal)
    public void setTextStyle(int style) {
        // Directly set the text style using the predefined constants
        textPaint.setTypeface(Typeface.defaultFromStyle(style)); // Use default style (bold, italic, etc.)
        invalidate(); // Redraw the view with the updated text style
    }
    public void setTextColorBasedOnTheme(CustomCircularProgressBar progressBar, Context context) {
        // Get the current theme's text color (light or dark)
        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        int textColor = a.getColor(0, Color.BLACK); // Default to black if not found
        a.recycle();  // Avoid memory leaks

        // Set the text color dynamically
        progressBar.setTextColor(textColor);
    }
    // Check if the text is in day number format
    public boolean isDayText() {
        return isDayText;
    }
}
