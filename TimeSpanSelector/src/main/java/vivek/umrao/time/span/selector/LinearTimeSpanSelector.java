/*
 * Copyright 2025 Vivek Umrao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vivek.umrao.time.span.selector;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Locale;

public class LinearTimeSpanSelector extends BaseTimeSpanSelector {

    private float lastTouchX;
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spanTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF trackRect = new RectF();
    private Thumb activeThumb = Thumb.NONE;
    private float spanTextY;
    private float tickY;

    public LinearTimeSpanSelector(Context context) {
        this(context, null);
    }

    public LinearTimeSpanSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearTimeSpanSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initializes the view with default values and custom attributes.
     */
    private void init(Context context, @Nullable AttributeSet attrs) {
        initializeDefaultValues();
        initializeDefaultColors();
        if (attrs != null) {
            initializeFromAttributes(context, attrs);
        }
        setupPaints();
        setFocusable(true);
        setClickable(true);
    }

    /**
     * Sets up default dimension and color values.
     */
    private void initializeDefaultValues() {
        hourTickHeight = dp(8f);
        minuteTickHeight = dp(4f);
        hourTickWidth = dp(1.5f);
        minuteTickWidth = dp(1f);
        tickLabelSize = sp(12f);
        trackWidth = dp(8f);
        thumbRadius = dp(12f);
        thumbTouchRadiusPadding = hourTickHeight + dp(2f);
        thumbStrokeWidth = dp(0f);
        tickDistanceFromTrack = dp(0);
        tickLabelDistanceFromTick = dp(0);
        spanTextSize = sp(14f);
        thumbShadowDx = dp(2f);
        thumbShadowDy = dp(2f);
        thumbElevation = dp(4f);
        isOvernightSpanAllowed = false; // Linear selector typically doesn't support overnight
    }

    /**
     * Loads view configuration from XML attributes.
     */
    private void initializeFromAttributes(Context context, @NonNull AttributeSet attrs) {
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearTimeSpanSelector)) {
            thumbMinuteStep = a.getInt(R.styleable.LinearTimeSpanSelector_tss_thumbMinuteStep, DEFAULT_THUMB_MINUTE_STEP);
            //isOvernightSpanAllowed = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_allowOvernightSpan, isOvernightSpanAllowed);

            currentStartMinutes = getStartTimeFromAttrs(a);
            currentEndMinutes = getEndTimeFromAttrs(a);
            minDurationMinutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_minDurationMinutes, minDurationMinutes);
            maxDurationMinutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_maxDurationMinutes, maxDurationMinutes);

            trackWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_trackWidth, trackWidth);
            trackColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_trackColor, trackColor);
            spanColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_spanColor, spanColor);
            thumbFillColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbFillColor, thumbFillColor);
            thumbStrokeColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbStrokeColor, thumbStrokeColor);
            thumbStrokeWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbStrokeWidth, thumbStrokeWidth);

            startThumbDrawable = a.getDrawable(R.styleable.LinearTimeSpanSelector_tss_startThumbDrawable);
            endThumbDrawable = a.getDrawable(R.styleable.LinearTimeSpanSelector_tss_endThumbDrawable);
            startThumbDrawableTintColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_startThumbDrawableTintColor, startThumbDrawableTintColor);
            endThumbDrawableTintColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_endThumbDrawableTintColor, endThumbDrawableTintColor);

            thumbRadius = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbRadius, thumbRadius);
            thumbShadowDx = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbShadowDx, thumbShadowDx);
            thumbShadowDy = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbShadowDy, thumbShadowDy);
            thumbElevation = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbElevation, thumbElevation);
            thumbShadowColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_thumbShadowColor, thumbShadowColor);

            showTicks = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showTicks, showTicks);
            hourTickColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_hourTickColor, hourTickColor);
            minuteTickColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_minuteTickColor, minuteTickColor);
            tickLabelColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            tickLabelSize = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickLabelSize, tickLabelSize);
            tickLabelStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_tickLabelStyle, tickLabelStyle);
            tickDistanceFromTrack = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickDistanceFromTrack, tickDistanceFromTrack);
            tickLabelDistanceFromTick = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_tickLabelDistanceFromTick, tickLabelDistanceFromTick);

            hourTickHeight = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_hourTickHeight, hourTickHeight);
            minuteTickHeight = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_minuteTickHeight, minuteTickHeight);
            hourTickWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_hourTickWidth, hourTickWidth);
            minuteTickWidth = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_minuteTickWidth, minuteTickWidth);
            thumbTouchRadiusPadding = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_thumbTouchRadiusPadding, thumbTouchRadiusPadding);

            is24HourFormat = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_is24HourFormat, is24HourFormat);
            showAmPmLabels = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showAmPmLabels, showAmPmLabels);
            showTickLabels = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showTickLabels, showTickLabels);
            isSpanTextShown = a.getBoolean(R.styleable.LinearTimeSpanSelector_tss_showSpanText, isSpanTextShown);

            if (a.hasValue(R.styleable.LinearTimeSpanSelector_tss_textColor)) {
                int commonColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_textColor, Color.BLACK);
                tickLabelColor = commonColor;
                spanTextColor = commonColor;
            }

            spanTextColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_spanTextColor, spanTextColor);
            tickLabelColor = a.getColor(R.styleable.LinearTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            spanTextSize = a.getDimension(R.styleable.LinearTimeSpanSelector_tss_spanTextSize, spanTextSize);
            spanTextStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanTextStyle, spanTextStyle);

            int pos = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanTextPosition, 1);
            if (pos == 0) {
                spanTextPosition = SpanTextPosition.TOP;
            } else if (pos == 2) {
                spanTextPosition = SpanTextPosition.CENTER;
            } else {
                spanTextPosition = SpanTextPosition.BOTTOM;
            }

            String format = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanTextFormat);
            if (format != null) spanTextFormat = format;

            int tickStyle = a.getInt(R.styleable.LinearTimeSpanSelector_tss_tickEdgeStyle, 1);
            tickEdgeStyle = (tickStyle == 0) ? TickEdgeStyle.ROUND : TickEdgeStyle.BUTT;
        }
    }

    private int getStartTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanStartInMinutes, currentStartMinutes);
        String timeStr = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanStartTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    private int getEndTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.LinearTimeSpanSelector_tss_spanEndInMinutes, currentEndMinutes);
        String timeStr = a.getString(R.styleable.LinearTimeSpanSelector_tss_spanEndTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    /**
     * Initializes and configures Paint objects used for drawing the linear slider.
     */
    private void setupPaints() {
        trackPaint.setStyle(Paint.Style.FILL);
        spanPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbFillColor);
        if (thumbElevation > 0) {
            thumbPaint.setShadowLayer(thumbElevation, thumbShadowDx, thumbShadowDy, thumbShadowColor);
        }
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setColor(thumbStrokeColor);
        thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(tickEdgeStyle == TickEdgeStyle.ROUND ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        tickLabelPaint.setColor(tickLabelColor);
        tickLabelPaint.setTextSize(tickLabelSize);
        tickLabelPaint.setTextAlign(Paint.Align.CENTER);
        tickLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, tickLabelStyle));
        spanTextPaint.setColor(spanTextColor);
        spanTextPaint.setTextSize(spanTextSize);
        spanTextPaint.setTextAlign(Paint.Align.CENTER);
        spanTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, spanTextStyle));
    }

    /**
     * Set the color of the span summary text for the linear selector.
     *
     * @param color The text color.
     */
    @Override
    public void setSpanTextColor(int color) {
        super.setSpanTextColor(color);
        spanTextPaint.setColor(spanTextColor);
        invalidate();
    }

    /**
     * Set the color of the tick labels for the linear selector.
     *
     * @param color The label color.
     */
    @Override
    public void setTickLabelColor(int color) {
        super.setTickLabelColor(color);
        tickLabelPaint.setColor(tickLabelColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = MeasureSpec.getSize(widthMeasureSpec);
        float totalHeight = calculateTotalHeight();
        int desiredHeight = (int) (totalHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    /**
     * Calculates the total height needed for the view based on enabled components (track, ticks, text).
     *
     * @return Total height in pixels.
     */
    private float calculateTotalHeight() {
        float h = Math.max(trackWidth, thumbRadius * 2);
        if (showTicks) h += calculateTickSpaceHeight();
        if (isSpanTextShown) h += calculateSpanTextHeight() + dp(8);
        return h;
    }

    /**
     * Calculates the vertical space occupied by the span summary text.
     *
     * @return Text height in pixels.
     */
    private float calculateSpanTextHeight() {
        if (!isSpanTextShown) return 0;
        Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        String text = getFormattedSpanText();
        int lines = text.split("\n").length;
        return lines * lineHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        updateLayout(w, h);
    }

    private void updateLayout(int w, int h) {
        float spanTextH = calculateSpanTextHeight();
        float tickSpaceH = calculateTickSpaceHeight();
        float coreH = Math.max(trackWidth, thumbRadius * 2);
        float margin = dp(8);

        float totalNeeded = calculateTotalHeight();
        float availableH = h - getPaddingTop() - getPaddingBottom();

        float currentY = getPaddingTop() + Math.max(0, (availableH - totalNeeded) / 2f);

        if (spanTextPosition == SpanTextPosition.TOP && isSpanTextShown) {
            spanTextY = currentY;
            currentY += spanTextH + margin;
        }

        float trackCenterY = currentY + coreH / 2f;
        float trackTop = trackCenterY - trackWidth / 2f;
        trackRect.set(getPaddingLeft() + thumbRadius, trackTop, w - getPaddingRight() - thumbRadius, trackTop + trackWidth);
        currentY += coreH;

        if (showTicks) {
            tickY = currentY + tickDistanceFromTrack + dp(2);
            currentY += tickSpaceH;
        } else {
            tickY = 0;
        }

        if (isSpanTextShown && (spanTextPosition == SpanTextPosition.BOTTOM || spanTextPosition == SpanTextPosition.CENTER)) {
            spanTextY = currentY + margin;
        }
    }

    private float calculateTickSpaceHeight() {
        if (!showTicks) return 0;
        float h = tickDistanceFromTrack + dp(2) + hourTickHeight;
        if (showTickLabels) {
            Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();
            float lineHeight = fm.descent - fm.ascent;
            int maxLines = (showAmPmLabels && !is24HourFormat) ? 2 : 1;
            h += tickLabelDistanceFromTick + dp(4) + maxLines * lineHeight;
        }
        return h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeThumb = getActiveThumb(x);
                if (activeThumb != Thumb.NONE) {
                    if (dragChangeListener != null) {
                        if (!dragChangeListener.onDragStart(activeThumb)) {
                            activeThumb = Thumb.NONE;
                            return false;
                        }
                    }
                    lastTouchX = x;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (activeThumb != Thumb.NONE) {
                    float dx = x - lastTouchX;
                    int newMin = positionToMinutes(minutesToPosition(activeThumb == Thumb.START ? currentStartMinutes : currentEndMinutes) + dx);
                    newMin = TimeUtils.snapToStep(newMin, thumbMinuteStep);
                    if (activeThumb == Thumb.START) {
                        updateSpan(newMin, currentEndMinutes, true);
                    } else {
                        updateSpan(currentStartMinutes, newMin, false);
                    }
                    lastTouchX = x;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (activeThumb != Thumb.NONE) {
                    if (dragChangeListener != null) {
                        dragChangeListener.onDragStop(activeThumb);
                    }
                    notifySpanChanged(true);
                    performClick();
                }
                activeThumb = Thumb.NONE;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Determines which thumb (start or end) is closest to the specified touch X coordinate.
     * Uses horizontal distance.
     *
     * @param x Touch X coordinate.
     * @return The active thumb or NONE.
     */
    private Thumb getActiveThumb(float x) {
        float sX = minutesToPosition(currentStartMinutes);
        float eX = minutesToPosition(currentEndMinutes);
        float touchR = thumbRadius + thumbTouchRadiusPadding;
        if (Math.abs(x - sX) <= touchR && Math.abs(x - sX) <= Math.abs(x - eX)) return Thumb.START;
        if (Math.abs(x - eX) <= touchR) return Thumb.END;
        return Thumb.NONE;
    }

    /**
     * Converts total minutes from midnight into a horizontal pixel position.
     *
     * @param minutes Minutes from midnight (0-1440).
     * @return X coordinate in pixels.
     */
    private float minutesToPosition(int minutes) {
        return trackRect.left + ((float) minutes / MINUTES_IN_DAY) * trackRect.width();
    }

    /**
     * Converts a horizontal pixel position into total minutes from midnight.
     *
     * @param pos X coordinate in pixels.
     * @return Minutes from midnight (0-1440).
     */
    private int positionToMinutes(float pos) {
        float rel = pos - trackRect.left;
        return Math.max(0, Math.min(Math.round((rel / trackRect.width()) * MINUTES_IN_DAY), MINUTES_IN_DAY));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (isSpanTextShown && spanTextPosition == SpanTextPosition.TOP) {
            drawSpanText(canvas, spanTextY);
        }
        trackPaint.setColor(trackColor);
        canvas.drawRoundRect(trackRect, trackRect.height() / 2, trackRect.height() / 2, trackPaint);
        if (showTicks) drawTicks(canvas);
        float sX = minutesToPosition(currentStartMinutes);
        float eX = minutesToPosition(currentEndMinutes);
        spanPaint.setColor(spanColor);
        canvas.drawRect(sX, trackRect.top, eX, trackRect.bottom, spanPaint);
        drawSingleThumb(canvas, sX, trackRect.centerY(), startThumbDrawable, startThumbDrawableTintColor);
        drawSingleThumb(canvas, eX, trackRect.centerY(), endThumbDrawable, endThumbDrawableTintColor);
        if (isSpanTextShown && (spanTextPosition == SpanTextPosition.BOTTOM || spanTextPosition == SpanTextPosition.CENTER)) {
            drawSpanText(canvas, spanTextY);
        }
    }

    /**
     * Draws the span summary text at the specified Y position.
     *
     * @param canvas The canvas to draw on.
     * @param y      The vertical baseline for the first line.
     */
    private void drawSpanText(Canvas canvas, float y) {
        String text = getFormattedSpanText();
        String[] lines = text.split("\n");
        Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float curY = y - fm.ascent;
        for (String line : lines) {
            canvas.drawText(line, getWidth() / 2f, curY, spanTextPaint);
            curY += lineHeight;
        }
    }

    /**
     * Draws a single thumb with its fill, icon, and stroke.
     *
     * @param canvas The canvas to draw on.
     * @param x      Horizontal center.
     * @param y      Vertical center.
     * @param d      Optional drawable icon.
     * @param tint   Icon tint color.
     */
    private void drawSingleThumb(Canvas canvas, float x, float y, Drawable d, int tint) {
        thumbPaint.setColor(thumbFillColor);
        canvas.drawCircle(x, y, thumbRadius, thumbPaint);
        if (d != null) {
            if (tint != Color.TRANSPARENT) DrawableCompat.setTint(d, tint);
            int size = (int) (thumbRadius * 1.2f);
            d.setBounds((int) (x - (float) size / 2), (int) (y - (float) size / 2), (int) (x + (float) size / 2), (int) (y + (float) size / 2));
            d.draw(canvas);
        }
        if (thumbStrokeWidth > 0) {
            thumbStrokePaint.setColor(thumbStrokeColor);
            thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
            canvas.drawCircle(x, y, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint);
        }
    }

    /**
     * Draws hour and minute tick marks along the linear track.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawTicks(Canvas canvas) {
        float y = tickY;
        tickPaint.setColor(hourTickColor);
        tickPaint.setStrokeWidth(hourTickWidth);
        Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;

        for (int m = 0; m <= MINUTES_IN_DAY; m += 60) {
            float x = minutesToPosition(m);
            canvas.drawLine(x, y, x, y + hourTickHeight, tickPaint);
            if (showTickLabels) {
                String label = formatTickLabel(m);
                if (!label.isEmpty()) {
                    String[] lines = label.split("\n");
                    float ly = y + hourTickHeight + tickLabelDistanceFromTick + dp(4) - fm.ascent;
                    for (String line : lines) {
                        canvas.drawText(line, x, ly, tickLabelPaint);
                        ly += lineHeight;
                    }
                }
            }
        }
        tickPaint.setColor(minuteTickColor);
        tickPaint.setStrokeWidth(minuteTickWidth);
        for (int m = 15; m < MINUTES_IN_DAY; m += 15) {
            if (m % 60 == 0) continue;
            float x = minutesToPosition(m);
            canvas.drawLine(x, y, x, y + minuteTickHeight, tickPaint);
        }
    }

    /**
     * Formats a minute value into a tick label string for the linear slider.
     *
     * @param minutes Minutes from midnight (0-1440).
     * @return Formatted label.
     */
    private String formatTickLabel(int minutes) {
        int h = (minutes / 60) % 24;
        if (h % 2 != 0) return "";
        if (is24HourFormat) return String.format(Locale.getDefault(), "%02d", h);
        int dh = h % 12;
        if (dh == 0) dh = 12;
        String ap = showAmPmLabels ? getContext().getString(h < 12 ? R.string.tss_am : R.string.tss_pm) : "";
        return ap.isEmpty() ? String.valueOf(dh) : dh + "\n" + ap;
    }
}
