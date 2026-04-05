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
    private final Paint spanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spanTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        spanTextSize = sp(12);
        thumbShadowDx = dp(0.0f);
        thumbShadowDy = dp(1.0f);
        thumbElevation = dp(2);
        spanTextPosition = SpanTextPosition.CENTER;
    }

    /**
     * Loads view configuration from XML attributes.
     */
    private void initializeFromAttributes(Context context, @NonNull AttributeSet attrs) {
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularTimeSpanSelector)) {
            thumbMinuteStep = a.getInt(R.styleable.CircularTimeSpanSelector_tss_thumbMinuteStep, DEFAULT_THUMB_MINUTE_STEP);
            isOvernightSpanAllowed = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_allowOvernightSpan, isOvernightSpanAllowed);

            currentStartMinutes = getStartTimeFromAttrs(a);
            currentEndMinutes = getEndTimeFromAttrs(a);
            minDurationMinutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_minDurationMinutes, minDurationMinutes);
            maxDurationMinutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_maxDurationMinutes, maxDurationMinutes);

            trackWidth = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_trackWidth, trackWidth);
            trackColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_trackColor, trackColor);
            spanColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_spanColor, spanColor);
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
            isSpanTextShown = a.getBoolean(R.styleable.CircularTimeSpanSelector_tss_showSpanText, isSpanTextShown);

            if (a.hasValue(R.styleable.CircularTimeSpanSelector_tss_textColor)) {
                int commonColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_textColor, Color.BLACK);
                tickLabelColor = commonColor;
                spanTextColor = commonColor;
            }

            spanTextColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_spanTextColor, spanTextColor);
            tickLabelColor = a.getColor(R.styleable.CircularTimeSpanSelector_tss_tickLabelColor, tickLabelColor);
            spanTextSize = a.getDimension(R.styleable.CircularTimeSpanSelector_tss_spanTextSize, spanTextSize);
            spanTextStyle = a.getInt(R.styleable.CircularTimeSpanSelector_tss_spanTextStyle, spanTextStyle);

            int pos = a.getInt(R.styleable.CircularTimeSpanSelector_tss_spanTextPosition, 2);
            spanTextPosition = (pos == 0) ? SpanTextPosition.TOP : (pos == 1 ? SpanTextPosition.BOTTOM : SpanTextPosition.CENTER);

            String format = a.getString(R.styleable.CircularTimeSpanSelector_tss_spanTextFormat);
            if (format != null) spanTextFormat = format;

            int tickStyle = a.getInt(R.styleable.CircularTimeSpanSelector_tss_tickEdgeStyle, 1);
            tickEdgeStyle = (tickStyle == 0) ? TickEdgeStyle.ROUND : TickEdgeStyle.BUTT;
        }
    }

    private int getStartTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_spanStartInMinutes, currentStartMinutes);
        String timeStr = a.getString(R.styleable.CircularTimeSpanSelector_tss_spanStartTime);
        if (timeStr != null) {
            try {
                minutes = TimeUtils.convertTimeToMinutes(timeStr);
            } catch (Exception ignored) {
            }
        }
        return TimeUtils.snapToStep(minutes, thumbMinuteStep);
    }

    private int getEndTimeFromAttrs(TypedArray a) {
        int minutes = a.getInt(R.styleable.CircularTimeSpanSelector_tss_spanEndInMinutes, currentEndMinutes);
        String timeStr = a.getString(R.styleable.CircularTimeSpanSelector_tss_spanEndTime);
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

        spanPaint.setStyle(Paint.Style.STROKE);
        spanPaint.setStrokeWidth(trackWidth);
        spanPaint.setColor(spanColor);
        spanPaint.setStrokeCap(Paint.Cap.ROUND);

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

        spanTextPaint.setColor(spanTextColor);
        spanTextPaint.setTextSize(spanTextSize);
        spanTextPaint.setTextAlign(Paint.Align.CENTER);
        spanTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, spanTextStyle));
    }

    /**
     * Set the color of the span summary text for the circular selector.
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
     * Set the color of the tick labels for the circular selector.
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
        float textSpace = 0;
        if (isSpanTextShown && spanTextPosition != SpanTextPosition.CENTER) {
            String text = getFormattedSpanText();
            String[] lines = text.split("\n");
            Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
            float lineHeight = fm.descent - fm.ascent;
            float totalBlockHeight = lines.length * lineHeight;
            float margin = dp(12);
            textSpace = totalBlockHeight + margin;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            float safePadding = Math.max(thumbRadius + thumbElevation, trackWidth / 2f) + thumbStrokeWidth + dp(4);
            int minHeight = (int) (dp(200) + safePadding * 2 + textSpace); // reasonable default
            height = resolveSize(minHeight, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        float textSpaceTop = 0;
        float textSpaceBottom = 0;
        float margin = dp(12);

        if (isSpanTextShown) {
            String text = getFormattedSpanText();
            String[] lines = text.split("\n");
            Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
            float lineHeight = fm.descent - fm.ascent;
            float totalBlockHeight = lines.length * lineHeight;

            if (spanTextPosition == SpanTextPosition.TOP) {
                textSpaceTop = totalBlockHeight + margin;
            } else if (spanTextPosition == SpanTextPosition.BOTTOM) {
                textSpaceBottom = totalBlockHeight + margin;
            }
        }

        float safePadding = Math.max(thumbRadius + thumbElevation, trackWidth / 2f) + thumbStrokeWidth + dp(4);
        cx = w / 2f;

        float availableHeight = h - getPaddingTop() - getPaddingBottom() - textSpaceTop - textSpaceBottom;
        float availableWidth = w - getPaddingLeft() - getPaddingRight();

        viewRadius = Math.min(availableWidth, availableHeight) / 2f - safePadding;

        // Center the ring in the available space after accounting for text
        cy = getPaddingTop() + textSpaceTop + (availableHeight / 2f);

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
        drawSpanArc(canvas);
        drawThumb(canvas);
        drawSpanText(canvas);
    }

    /**
     * Draws the span summary text in the center of the dial (or top/bottom as configured).
     * TOP and BOTTOM positions are placed outside the ring with a margin.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawSpanText(Canvas canvas) {
        if (!isSpanTextShown) return;
        String text = getFormattedSpanText();
        float centerX = cx;

        String[] lines = text.split("\n");
        Paint.FontMetrics fm = spanTextPaint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float totalBlockHeight = lines.length * lineHeight;

        float y;
        float margin = dp(12); // Margin to avoid overlap with the ring/thumbs
        float outerExtent = viewRadius + Math.max(trackWidth / 2f, thumbRadius);

        switch (spanTextPosition) {
            case TOP:
                // Position above the ring
                y = cy - outerExtent - margin - (lines.length - 1) * lineHeight - fm.descent;
                break;
            case BOTTOM:
                // Position below the ring
                y = cy + outerExtent + margin - fm.ascent;
                break;
            case CENTER:
            default:
                // Position in the exact center of the ring
                y = cy - (totalBlockHeight / 2f) - fm.ascent;
                break;
        }

        for (String line : lines) {
            canvas.drawText(line, centerX, y, spanTextPaint);
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
     * Draws the active span arc between start and end times.
     *
     * @param canvas The canvas to draw on.
     */
    private void drawSpanArc(@NonNull Canvas canvas) {
        float startAngle = minutesToAngle(currentStartMinutes);
        float endAngle = minutesToAngle(currentEndMinutes);
        float sweep = calculateSweepAngle(startAngle, endAngle);
        canvas.drawArc(drawingOval, startAngle - 90, sweep, false, spanPaint);
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
                    announceSpan();
                    notifySpanChanged(true);
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
            updateSpan(minutes, currentEndMinutes, true);
        } else if (activeThumb == Thumb.END) {
            updateSpan(currentStartMinutes, minutes, false);
        }
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    /**
     * Announces the currently selected span for accessibility users.
     */
    private void announceSpan() {
        if (accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled()) {
            String start = TimeUtils.formatDisplayTime(currentStartMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            String end = TimeUtils.formatDisplayTime(currentEndMinutes, is24HourFormat, getContext().getString(R.string.tss_am), getContext().getString(R.string.tss_pm));
            announceForAccessibility(getContext().getString(isOvernight() ? R.string.tss_accessibility_span_overnight_selected : R.string.tss_accessibility_span_selected, start, end));
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
