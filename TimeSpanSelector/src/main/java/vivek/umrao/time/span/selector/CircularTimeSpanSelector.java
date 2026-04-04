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
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Locale;

/**
 * A circular time span selector view.
 */
public class CircularTimeSpanSelector extends BaseTimeSpanSelector {
    private static final float DEGREES_IN_CIRCLE = 360f;
    private static final float HOURS_IN_DAY = 24f;
    private static final float DEGREES_PER_HOUR = DEGREES_IN_CIRCLE / HOURS_IN_DAY;

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rangeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float cx, cy, viewRadius;
    private final RectF drawingOval = new RectF();

    private Thumb activeThumb = Thumb.NONE;
    private AccessibilityManager accessibilityManager;

    public CircularTimeSpanSelector(Context context) {
        this(context, null);
    }

    public CircularTimeSpanSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularTimeSpanSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Initializes the view with default values and custom attributes.
     */
    private void init(Context context, @Nullable AttributeSet attrs) {
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        initializeDefaultValues();
        initializeDefaultColors();
        if (attrs != null) {
            initializeFromAttributes(context, attrs);
        }
        setupPaints();
        setFocusable(true);
        setClickable(true);
        setContentDescription(getContext().getString(R.string.tss_content_description_time_span_selector));
    }

    /**
     * Sets up default dimension and color values.
     */
    private void initializeDefaultValues() {
        trackWidth = dp(12);
        thumbRadius = dp(15);
        thumbStrokeWidth = dp(0f);
        tickLabelSize = sp(12);
        tickDistanceFromTrack = dp(0);
        tickLabelDistanceFromTick = dp(20);
        hourTickHeight = dp(8);
        minuteTickHeight = dp(4);
        hourTickWidth = dp(2.0f);
        minuteTickWidth = dp(1.0f);
        thumbTouchRadiusPadding = hourTickHeight + dp(2f);
        rangeTextSize = sp(12);
        thumbShadowDx = dp(0.0f);
        thumbShadowDy = dp(1.0f);
        thumbElevation = dp(2);
        rangeTextPosition = RangeTextPosition.CENTER;
    }

    /**
     * Loads view configuration from XML attributes.
     */
    private void initializeFromAttributes(Context context, @NonNull AttributeSet attrs) {
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularTimeSpanSelector)) {
            thumbMinuteStep = a.getInt(R.styleable.CircularTimeSpanSelector_tss_thumbMinuteStep, DEFAULT_THUMB_MINUTE_STEP);
            isOvernightRangeAllowed = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_allowOvernightRange, isOvernightRangeAllowed);

            currentStartMinutes = getStartTimeFromAttrs(a);
            currentEndMinutes = getEndTimeFromAttrs(a);
            minDurationMinutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_minDurationMinutes, minDurationMinutes);
            maxDurationMinutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_maxDurationMinutes, maxDurationMinutes);

            trackWidth = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_trackWidth, trackWidth);
            trackColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_trackColor, trackColor);
            rangeColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_rangeColor, rangeColor);
            thumbFillColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_thumbFillColor, thumbFillColor);
            thumbStrokeColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_thumbStrokeColor, thumbStrokeColor);

            startThumbDrawable = a.getDrawable(R.styleable.CircularTimeSpanSelector_tss_startThumbDrawable);
            endThumbDrawable = a.getDrawable(R.styleable.CircularTimeSpanSelector_tss_endThumbDrawable);
            startThumbDrawableTintColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_startThumbDrawableTintColor, startThumbDrawableTintColor);
            endThumbDrawableTintColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_endThumbDrawableTintColor, endThumbDrawableTintColor);

            thumbRadius = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbRadius, thumbRadius);
            thumbStrokeWidth = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbStrokeWidth, thumbStrokeWidth);
            thumbShadowDx = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbShadowDx, thumbShadowDx);
            thumbShadowDy = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbShadowDy, thumbShadowDy);
            thumbElevation = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbElevation, thumbElevation);
            thumbShadowColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_thumbShadowColor, thumbShadowColor);

            showTicks = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_showTicks, showTicks);
            hourTickColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_hourTickColor, hourTickColor);
            minuteTickColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_minuteTickColor, minuteTickColor);
            tickLabelColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            tickLabelSize = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_tickLabelSize, tickLabelSize);
            tickLabelStyle = a.getInt(R.styleable.CircularTimeSpanSelector_tss_tickLabelStyle, tickLabelStyle);
            tickDistanceFromTrack = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_tickDistanceFromTrack, tickDistanceFromTrack);
            tickLabelDistanceFromTick = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_tickLabelDistanceFromTick, tickLabelDistanceFromTick);

            hourTickHeight = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_hourTickHeight, hourTickHeight);
            minuteTickHeight = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_minuteTickHeight, minuteTickHeight);
            hourTickWidth = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_hourTickWidth, hourTickWidth);
            minuteTickWidth = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_minuteTickWidth, minuteTickWidth);
            thumbTouchRadiusPadding = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_thumbTouchRadiusPadding, thumbTouchRadiusPadding);

            is24HourFormat = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_is24HourFormat, is24HourFormat);
            showAmPmLabels = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_showAmPmLabels, showAmPmLabels);
            showTickLabels = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_showTickLabels, showTickLabels);
            isRangeTextShown = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_showRangeText, isRangeTextShown);

            if (a.hasValue(R.styleable.CircularTimeSpanSelector_tss_textColor)) {
                int commonColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_textColor, Color.BLACK);
                tickLabelColor = commonColor;
                rangeTextColor = commonColor;
            }

            rangeTextColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_rangeTextColor, rangeTextColor);
            tickLabelColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            rangeTextSize = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_rangeTextSize, rangeTextSize);
            rangeTextStyle = a.getInt(R.styleable.CircularTimeSpanSelector_tss_rangeTextStyle, rangeTextStyle);

            int pos = a.getInt(R.styleable.CircularTimeSpanSelector_tss_rangeTextPosition, 2);
            rangeTextPosition = (pos == 0) ? RangeTextPosition.TOP : (pos == 1 ? RangeTextPosition.BOTTOM : RangeTextPosition.CENTER);

            String format = a.getString(R.styleable.CircularTimeSpanSelector_tss_rangeTextFormat);
            if (format != null) rangeTextFormat = format;

            int tickStyle = a.getInt(R.styleable.CircularTimeSpanSelector_tss_tickEdgeStyle, 1);
            tickEdgeStyle = (tickStyle == 0) ? TickEdgeStyle.ROUND : TickEdgeStyle.BUTT;
        }
    }

    private int getStartTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_rangeStartInMinutes, currentStartMinutes);
        String timeStr = a.getString(R.styleable.CircularTimeSpanSelector_tss_rangeStartTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    private int getEndTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_rangeEndInMinutes, currentEndMinutes);
        String timeStr = a.getString(R.styleable.CircularTimeSpanSelector_tss_rangeEndTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    /**
     * Initializes and configures Paint objects used for drawing the circular dial.
     */
    private void setupPaints() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(trackWidth);
        ringPaint.setColor(trackColor);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);

        rangePaint.setStyle(Paint.Style.STROKE);
        rangePaint.setStrokeWidth(trackWidth);
        rangePaint.setColor(rangeColor);
        rangePaint.setStrokeCap(Paint.Cap.ROUND);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(tickEdgeStyle == TickEdgeStyle.ROUND ? Paint.Cap.ROUND : Paint.Cap.BUTT);

        tickLabelPaint.setColor(tickLabelColor);
        tickLabelPaint.setTextSize(tickLabelSize);
        tickLabelPaint.setTextAlign(Paint.Align.CENTER);
        tickLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, tickLabelStyle));

        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(thumbFillColor);

        thumbShadowPaint.setColor(thumbFillColor);
        thumbShadowPaint.setStyle(Paint.Style.FILL);
        thumbShadowPaint.setShadowLayer(thumbElevation, thumbShadowDx, thumbShadowDy, thumbShadowColor);

        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
        thumbStrokePaint.setColor(thumbStrokeColor);

        rangeTextPaint.setColor(rangeTextColor);
        rangeTextPaint.setTextSize(rangeTextSize);
        rangeTextPaint.setTextAlign(Paint.Align.CENTER);
        rangeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, rangeTextStyle));
    }

    /**
     * Set the color of the range summary text for the circular picker.
     *
     * @param color The text color.
     */
    @Override
    public void setRangeTextColor(int color) {
        super.setRangeTextColor(color);
        rangeTextPaint.setColor(rangeTextColor);
        invalidate();
    }

    /**
     * Set the color of the tick labels for the circular picker.
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
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        float safePadding = Math.max(thumbRadius + thumbElevation, trackWidth / 2f) + thumbStrokeWidth + dp(4);
        cx = w / 2f;
        cy = h / 2f;
        viewRadius = Math.min(w, h) / 2f - safePadding;
        if (viewRadius > 0) {
            drawingOval.set(cx - viewRadius, cy - viewRadius, cx + viewRadius, cy + viewRadius);
        } else {
            drawingOval.setEmpty();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (viewRadius <= 0 || drawingOval.isEmpty()) return;

        canvas.drawCircle(cx, cy, viewRadius, ringPaint);
        drawTicksAndLabels(canvas);
        drawRangeArc(canvas);
        drawThumb(canvas);
        drawRangeText(canvas);
    }

    /**
     * Draws the range summary text in the center of the dial (or top/bottom as configured).
     *
     * @param canvas The canvas to draw on.
     */
    private void drawRangeText(Canvas canvas) {
        if (!isRangeTextShown) return;
        String text = getFormattedRangeText();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        String[] lines = text.split("\n");
        Paint.FontMetrics fm = rangeTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float totalBlockHeight = (lines.length - 1) * lineHeight + (fm.descent - fm.ascent);

        float y;
        float offset = Math.min(getWidth(), getHeight()) / 5f;
        switch (rangeTextPosition) {
            case TOP:
                y = centerY - offset - totalBlockHeight / 2f - fm.ascent;
                break;
            case BOTTOM:
                y = centerY + offset - totalBlockHeight / 2f - fm.ascent;
                break;
            default:
                y = centerY - totalBlockHeight / 2f - fm.ascent;
                break;
        }

        for (String line : lines) {
            canvas.drawText(line, centerX, y, rangeTextPaint);
            y += lineHeight;
        }
    }

    /**
     * Draws both the start and end thumbs on the circular track.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawThumb(@NonNull Canvas canvas) {
        drawSingleThumb(canvas, pointForMinutes(currentStartMinutes), startThumbDrawable, startThumbDrawableTintColor);
        drawSingleThumb(canvas, pointForMinutes(currentEndMinutes), endThumbDrawable, endThumbDrawableTintColor);
    }

    /**
     * Draws a single thumb with its shadow, fill, icon, and stroke.
     *
     * @param canvas The canvas to draw on.
     * @param pos    Coordinates on the circle.
     * @param d      Optional drawable icon.
     * @param tint   Icon tint color.
     */
    private void drawSingleThumb(Canvas canvas, PointF pos, Drawable d, int tint) {
        thumbPaint.setColor(thumbFillColor);
        if (thumbElevation > 0) canvas.drawCircle(pos.x, pos.y, thumbRadius, thumbShadowPaint);
        canvas.drawCircle(pos.x, pos.y, thumbRadius, thumbPaint);
        if (d != null) {
            if (tint != Color.TRANSPARENT) DrawableCompat.setTint(d, tint);
            float size = thumbRadius * 1.2f;
            d.setBounds((int) (pos.x - size / 2f), (int) (pos.y - size / 2f), (int) (pos.x + size / 2f), (int) (pos.y + size / 2f));
            d.draw(canvas);
        }
        if (thumbStrokeWidth > 0) {
            thumbStrokePaint.setColor(thumbStrokeColor);
            thumbStrokePaint.setStrokeWidth(thumbStrokeWidth);
            canvas.drawCircle(pos.x, pos.y, thumbRadius - thumbStrokeWidth / 2f, thumbStrokePaint);
        }
    }

    /**
     * Draws the active range arc between start and end times.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawRangeArc(@NonNull Canvas canvas) {
        float startAngle = minutesToAngle(currentStartMinutes);
        float endAngle = minutesToAngle(currentEndMinutes);
        float sweep = calculateSweepAngle(startAngle, endAngle);
        canvas.drawArc(drawingOval, startAngle - 90, sweep, false, rangePaint);
    }

    /**
     * Draws hour/minute ticks and labels around the dial.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawTicksAndLabels(@NonNull Canvas canvas) {
        float tickBaseRadius = viewRadius - trackWidth / 2f - tickDistanceFromTrack;
        Paint.FontMetrics fm = tickLabelPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;

        for (int h = 0; h < HOURS_IN_DAY; h++) {
            float angle = h * DEGREES_PER_HOUR;
            if (showTicks) {
                PointF p1 = polar(angle, tickBaseRadius - hourTickHeight);
                PointF p2 = polar(angle, tickBaseRadius);
                tickPaint.setColor(hourTickColor);
                tickPaint.setStrokeWidth(hourTickWidth);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, tickPaint);
            }
            if (showTickLabels && h % 2 == 0) {
                String label = formatTickLabel(h);
                PointF lp = polar(angle, viewRadius - trackWidth / 2f - tickLabelDistanceFromTick);

                String[] lines = label.split("\n");
                float totalBlockHeight = (lines.length - 1) * lineHeight + (fm.descent - fm.ascent);
                float y = lp.y - totalBlockHeight / 2f - fm.ascent;

                for (String line : lines) {
                    canvas.drawText(line, lp.x, y, tickLabelPaint);
                    y += lineHeight;
                }
            }
        }
        if (showTicks) {
            tickPaint.setColor(minuteTickColor);
            tickPaint.setStrokeWidth(minuteTickWidth);
            for (int m = 15; m < TimeUtils.MINUTES_IN_DAY; m += 15) {
                if (m % 60 == 0) continue;
                PointF p1 = polar(minutesToAngle(m), tickBaseRadius - minuteTickHeight);
                PointF p2 = polar(minutesToAngle(m), tickBaseRadius);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, tickPaint);
            }
        }
    }

    /**
     * Formats an hour value into a tick label string based on 12/24h format.
     *
     * @param h Hour (0-23).
     * @return Formatted label.
     */
    private String formatTickLabel(int h) {
        if (is24HourFormat) return String.format(Locale.getDefault(), "%02d", h);
        int dh = h % 12;
        if (dh == 0) dh = 12;
        String ap = showAmPmLabels ? getContext().getString(h < 12 ? R.string.tss_am : R.string.tss_pm) : "";
        if (ap.isEmpty()) return String.valueOf(dh);
        return dh + " " + ap;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled()) return false;
        float x = event.getX(), y = event.getY();
        int minutes = angleToMinutes(calculateAngleFromPoint(x, y));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                activeThumb = getActiveThumb(x, y);
                if (activeThumb != Thumb.NONE) {
                    if (dragChangeListener != null) {
                        if (!dragChangeListener.onDragStart(activeThumb)) {
                            activeThumb = Thumb.NONE;
                            return false;
                        }
                    }
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    updatePosition(minutes);
                    playSoundEffect(SoundEffectConstants.CLICK);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (activeThumb != Thumb.NONE) {
                    updatePosition(minutes);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (activeThumb != Thumb.NONE) {
                    if (dragChangeListener != null) {
                        dragChangeListener.onDragStop(activeThumb);
                    }
                    activeThumb = Thumb.NONE;
                    announceRange();
                    notifyRangeChanged(true);
                    performClick();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    /**
     * Determines which thumb (start or end) is closest to the specified touch coordinates.
     * Uses Euclidean distance in polar space.
     *
     * @param x Touch X coordinate.
     * @param y Touch Y coordinate.
     * @return The active thumb or NONE if too far from both.
     */
    private Thumb getActiveThumb(float x, float y) {
        PointF ps = pointForMinutes(currentStartMinutes), pe = pointForMinutes(currentEndMinutes);
        float r = thumbRadius + thumbTouchRadiusPadding;
        float d1 = (float) Math.hypot(x - ps.x, y - ps.y), d2 = (float) Math.hypot(x - pe.x, y - pe.y);
        if (d1 <= r && d2 <= r) return d1 < d2 ? Thumb.START : Thumb.END;
        if (d1 <= r) return Thumb.START;
        if (d2 <= r) return Thumb.END;
        return Thumb.NONE;
    }

    /**
     * Updates the position of the active thumb based on the touch angle.
     *
     * @param minutes The minute value corresponding to the touch point.
     */
    private void updatePosition(int minutes) {
        minutes = TimeUtils.snapToStep(minutes, thumbMinuteStep);
        if (activeThumb == Thumb.START) {
            updateRange(minutes, currentEndMinutes, true);
        } else if (activeThumb == Thumb.END) {
            updateRange(currentStartMinutes, minutes, false);
        }
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    /**
     * Announces the currently selected range for accessibility users.
     */
    private void announceRange() {
        if (accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled()) {
            String start = TimeUtils.formatDisplayTime(currentStartMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            String end = TimeUtils.formatDisplayTime(currentEndMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            announceForAccessibility(getContext().getString(isOvernight() ? R.string.tss_accessibility_range_overnight_selected : R.string.tss_accessibility_range_selected, start, end));
        }
    }

    /**
     * Converts total minutes from midnight into a rotation angle (0-360).
     */
    private float minutesToAngle(int minutes) {
        return ((float) minutes / TimeUtils.MINUTES_IN_DAY) * DEGREES_IN_CIRCLE;
    }

    /**
     * Converts a rotation angle (0-360) into total minutes from midnight.
     */
    private int angleToMinutes(float angle) {
        return (int) (((angle % DEGREES_IN_CIRCLE + DEGREES_IN_CIRCLE) % DEGREES_IN_CIRCLE) / DEGREES_IN_CIRCLE * TimeUtils.MINUTES_IN_DAY);
    }

    /**
     * Calculates the point on the track corresponding to the given minutes.
     */
    private PointF pointForMinutes(int m) {
        return polar(minutesToAngle(m), viewRadius);
    }

    /**
     * Converts polar coordinates (angle, radius) to Cartesian coordinates (x, y).
     */
    private PointF polar(float angle, float r) {
        double rad = Math.toRadians(angle - 90);
        return new PointF(cx + (float) (r * Math.cos(rad)), cy + (float) (r * Math.sin(rad)));
    }

    /**
     * Calculates the rotation angle relative to the center for a given touch point.
     */
    private float calculateAngleFromPoint(float x, float y) {
        float angle = (float) Math.toDegrees(Math.atan2(y - cy, x - cx)) + 90;
        return angle < 0 ? angle + DEGREES_IN_CIRCLE : angle;
    }

    /**
     * Calculates the sweep angle (positive) between start and end angles, accounting for overnight wraps.
     */
    private float calculateSweepAngle(float start, float end) {
        float sweep = end - start;
        return isOvernight() ? (DEGREES_IN_CIRCLE + sweep) : (sweep < 0 ? sweep + DEGREES_IN_CIRCLE : sweep);
    }
}
