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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

/**
 * Base class for time span selectors containing shared logic and attributes.
 */
public abstract class BaseTimeSpanSelector extends View implements TimeSpanSelector {

    private static final String KEY_SUPER_STATE = "super_state";
    private static final String KEY_START_MINUTES = "start_minutes";
    private static final String KEY_END_MINUTES = "end_minutes";

    protected OnRangeChangeListener listener;
    protected OnTimeChangeListener timeChangeListener;
    protected OnDragChangeListener dragChangeListener;

    protected boolean showTicks = true;
    protected boolean showTickLabels = true;
    protected boolean showAmPmLabels = false;
    protected boolean isRangeTextShown = true;
    protected boolean is24HourFormat = true;
    protected boolean isOvernightRangeAllowed = true;

    protected int currentStartMinutes = DEFAULT_START_MINUTES;
    protected int currentEndMinutes = DEFAULT_END_MINUTES;
    protected int thumbMinuteStep = DEFAULT_THUMB_MINUTE_STEP;

    protected int minDurationMinutes = 0;
    protected int maxDurationMinutes = MINUTES_IN_DAY;

    @ColorInt protected int trackColor;
    @ColorInt protected int rangeColor;
    @ColorInt protected int rangeTextColor;
    @ColorInt protected int thumbFillColor;
    @ColorInt protected int thumbStrokeColor;
    @ColorInt protected int thumbShadowColor;
    @ColorInt protected int startThumbDrawableTintColor;
    @ColorInt protected int endThumbDrawableTintColor;
    @ColorInt protected int hourTickColor;
    @ColorInt protected int minuteTickColor;
    @ColorInt protected int tickLabelColor;

    protected float trackWidth;
    protected float thumbRadius;
    protected float thumbTouchRadiusPadding;
    protected float thumbStrokeWidth;
    protected float thumbShadowDx;
    protected float thumbShadowDy;
    protected float thumbElevation;
    protected float hourTickHeight;
    protected float minuteTickHeight;
    protected float hourTickWidth;
    protected float minuteTickWidth;
    protected float tickLabelSize;
    protected float tickDistanceFromTrack;
    protected float tickLabelDistanceFromTick;
    protected float rangeTextSize;

    protected int tickLabelStyle = Typeface.NORMAL;
    protected int rangeTextStyle = Typeface.NORMAL;
    protected String rangeTextFormat = DEFAULT_RANGE_TEXT_FORMAT;
    protected RangeTextPosition rangeTextPosition = RangeTextPosition.BOTTOM;
    protected TickEdgeStyle tickEdgeStyle = TickEdgeStyle.BUTT;

    @Nullable protected Drawable startThumbDrawable;
    @Nullable protected Drawable endThumbDrawable;

    public BaseTimeSpanSelector(Context context) {
        super(context);
    }

    public BaseTimeSpanSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseTimeSpanSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Initializes default colors from resources and theme.
     */
    protected void initializeDefaultColors() {
        trackColor = MaterialColors.getColor(this, android.R.attr.textColorSecondaryNoDisable, Color.DKGRAY);
        rangeColor = MaterialColors.getColor(this, android.R.attr.colorPrimary, Color.YELLOW);
        rangeTextColor = MaterialColors.getColor(this, android.R.attr.textColorPrimary, Color.BLACK);
        thumbFillColor = rangeColor;
        thumbStrokeColor = MaterialColors.getColor(this, android.R.attr.strokeColor, Color.DKGRAY);
        thumbShadowColor = MaterialColors.getColor(this, android.R.attr.shadowColor, Color.BLACK);
        startThumbDrawableTintColor = MaterialColors.getColor(this, android.R.attr.thumbTint, Color.BLACK);
        endThumbDrawableTintColor = startThumbDrawableTintColor;
        hourTickColor = rangeTextColor;
        minuteTickColor = rangeTextColor;
        tickLabelColor = rangeTextColor;
    }

    /**
     * Converts DP (Density-independent Pixels) to physical pixels.
     *
     * @param value The value in DP.
     * @return The value in pixels.
     */
    protected float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /**
     * Converts SP (Scale-independent Pixels) to physical pixels.
     *
     * @param value The value in SP.
     * @return The value in pixels.
     */
    protected float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    /**
     * Set the listener to be notified when the time range changes.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnRangeChangeListener(@NonNull OnRangeChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Get the current range change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnRangeChangeListener getOnRangeChangeListener() {
        return listener;
    }

    /**
     * Set the listener for granular time and duration changes.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnTimeChangeListener(@Nullable OnTimeChangeListener listener) {
        this.timeChangeListener = listener;
    }

    /**
     * Get the current time change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnTimeChangeListener getOnTimeChangeListener() {
        return timeChangeListener;
    }

    /**
     * Set the listener for thumb drag interactions.
     *
     * @param listener The listener to set.
     */
    @Override
    public void setOnDragChangeListener(@Nullable OnDragChangeListener listener) {
        this.dragChangeListener = listener;
    }

    /**
     * Get the current drag change listener.
     *
     * @return The current listener or null if not set.
     */
    @Override
    @Nullable
    public OnDragChangeListener getOnDragChangeListener() {
        return dragChangeListener;
    }

    /**
     * Get the start time of the range in minutes from midnight (0-1439).
     *
     * @return Start time in minutes.
     */
    @Override
    public int getRangeStartInMinutes() {
        return currentStartMinutes;
    }

    /**
     * Set the start time of the range in minutes from midnight (0-1439).
     *
     * @param startMinutes Start time in minutes.
     */
    @Override
    public void setRangeStartInMinutes(int startMinutes) {
        updateRange(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), currentEndMinutes, true);
    }

    /**
     * Get the end time of the range in minutes from midnight (0-1439).
     *
     * @return End time in minutes.
     */
    @Override
    public int getRangeEndInMinutes() {
        return currentEndMinutes;
    }

    /**
     * Set the end time of the range in minutes from midnight (0-1439).
     *
     * @param endMinutes End time in minutes.
     */
    @Override
    public void setRangeEndInMinutes(int endMinutes) {
        updateRange(currentStartMinutes, TimeUtils.snapToStep(endMinutes, thumbMinuteStep), false);
    }

    /**
     * Set both start and end times of the range in minutes from midnight.
     *
     * @param startMinutes Start time in minutes.
     * @param endMinutes   End time in minutes.
     */
    @Override
    public void setRangeInMinutes(int startMinutes, int endMinutes) {
        updateRange(TimeUtils.snapToStep(startMinutes, thumbMinuteStep), TimeUtils.snapToStep(endMinutes, thumbMinuteStep), true);
    }

    /**
     * Updates the internal range state, enforcing duration constraints and optional overnight rules.
     *
     * @param newStart    Proposed start time in minutes.
     * @param newEnd      Proposed end time in minutes.
     * @param movingStart True if the start thumb was moved, false if the end thumb.
     */
    protected void updateRange(int newStart, int newEnd, boolean movingStart) {
        newStart = (newStart % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;
        newEnd = (newEnd % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;

        int duration = calculateDuration(newStart, newEnd);

        if (!isOvernightRangeAllowed && newEnd < newStart) {
            if (movingStart) {
                newStart = newEnd;
            } else {
                newEnd = newStart;
            }
            duration = 0;
        }

        if (duration < minDurationMinutes) {
            if (movingStart) {
                newStart = (newEnd - minDurationMinutes + MINUTES_IN_DAY) % MINUTES_IN_DAY;
            } else {
                newEnd = (newStart + minDurationMinutes) % MINUTES_IN_DAY;
            }
        } else if (duration > maxDurationMinutes) {
            if (movingStart) {
                newStart = (newEnd - maxDurationMinutes + MINUTES_IN_DAY) % MINUTES_IN_DAY;
            } else {
                newEnd = (newStart + maxDurationMinutes) % MINUTES_IN_DAY;
            }
        }

        if (this.currentStartMinutes != newStart || this.currentEndMinutes != newEnd) {
            boolean startChanged = this.currentStartMinutes != newStart;
            boolean endChanged = this.currentEndMinutes != newEnd;
            int oldDuration = getDurationInMinutes();

            this.currentStartMinutes = newStart;
            this.currentEndMinutes = newEnd;
            invalidate();
            notifyRangeChanged(false);

            if (timeChangeListener != null) {
                if (startChanged) {
                    timeChangeListener.onStartTimeChange(newStart);
                }
                if (endChanged) {
                    timeChangeListener.onEndTimeChange(newEnd);
                }
                if (getDurationInMinutes() != oldDuration) {
                    timeChangeListener.onDurationChange(getDurationInMinutes());
                }
            }
        }
    }

    /**
     * Calculates the duration between two time points, accounting for midnight wraps.
     *
     * @param start Start time in minutes.
     * @param end   End time in minutes.
     * @return Duration in minutes.
     */
    protected int calculateDuration(int start, int end) {
        if (end >= start) return end - start;
        return (MINUTES_IN_DAY - start) + end;
    }

    /**
     * Set the start time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    @Override
    public void setRangeStartTime(String timeString) throws IllegalArgumentException {
        setRangeStartInMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    /**
     * Set the end time using a string format (e.g., "HH:mm" or "h:mm a").
     *
     * @param timeString The time string to parse.
     * @throws IllegalArgumentException If the format is invalid.
     */
    @Override
    public void setRangeEndTime(String timeString) throws IllegalArgumentException {
        setRangeEndInMinutes(TimeUtils.convertTimeToMinutes(timeString));
    }

    /**
     * Set both start and end times using string formats.
     *
     * @param start The start time string.
     * @param end   The end time string.
     * @throws IllegalArgumentException If either format is invalid.
     */
    @Override
    public void setRangeTime(String start, String end) throws IllegalArgumentException {
        setRangeInMinutes(TimeUtils.convertTimeToMinutes(start), TimeUtils.convertTimeToMinutes(end));
    }

    /**
     * Get the total duration of the selected range in minutes.
     *
     * @return Duration in minutes.
     */
    @Override
    public int getDurationInMinutes() {
        return calculateDuration(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Get the minimum allowed duration in minutes.
     *
     * @return Minimum duration.
     */
    @Override
    public int getMinDurationMinutes() { return minDurationMinutes; }

    /**
     * Set the minimum allowed duration in minutes.
     *
     * @param minutes Minimum duration.
     */
    @Override
    public void setMinDurationMinutes(int minutes) {
        this.minDurationMinutes = Math.max(0, minutes);
        setRangeInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Get the maximum allowed duration in minutes.
     *
     * @return Maximum duration.
     */
    @Override
    public int getMaxDurationMinutes() { return maxDurationMinutes; }

    /**
     * Set the maximum allowed duration in minutes.
     *
     * @param minutes Maximum duration.
     */
    @Override
    public void setMaxDurationMinutes(int minutes) {
        this.maxDurationMinutes = Math.min(MINUTES_IN_DAY, minutes);
        setRangeInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Check if the current range spans across midnight (e.g., 10 PM to 2 AM).
     *
     * @return True if the range is overnight.
     */
    @Override
    public boolean isOvernight() {
        return currentEndMinutes < currentStartMinutes;
    }

    /**
     * Check if ranges spanning across midnight are allowed.
     *
     * @return True if allowed.
     */
    @Override
    public boolean isOvernightRangeAllowed() {
        return isOvernightRangeAllowed;
    }

    /**
     * Enable or disable ranges spanning across midnight.
     *
     * @param allowed True to allow overnight ranges.
     */
    @Override
    public void setOvernightRangeAllowed(boolean allowed) {
        this.isOvernightRangeAllowed = allowed;
        if (!allowed && isOvernight()) {
            currentEndMinutes = currentStartMinutes;
        }
        invalidate();
    }

    /**
     * Get the snap interval in minutes for the thumbs.
     *
     * @return The minute step.
     */
    @Override
    public int getThumbMinuteStep() {
        return thumbMinuteStep;
    }

    /**
     * Set the snap interval in minutes for the thumbs (e.g., 1, 5, 15, 30).
     *
     * @param thumbMinuteStep The minute step to set.
     */
    @Override
    public void setThumbMinuteStep(int thumbMinuteStep) {
        this.thumbMinuteStep = Math.max(1, thumbMinuteStep);
        setRangeInMinutes(currentStartMinutes, currentEndMinutes);
    }

    /**
     * Set the accent color, which typically updates both range and thumb colors.
     *
     * @param color The accent color.
     */
    @Override
    public void setAccentColor(@ColorInt int color) {
        this.rangeColor = color;
        this.thumbFillColor = color;
        invalidate();
    }

    /**
     * Get the current accent color.
     *
     * @return The accent color.
     */
    @Override
    @ColorInt
    public int getAccentColor() {
        return rangeColor;
    }

    /**
     * Set the color of the background track.
     *
     * @param color The track color.
     */
    @Override
    public void setTrackColor(@ColorInt int color) {
        this.trackColor = color;
        invalidate();
    }

    /**
     * Get the color of the background track.
     *
     * @return The track color.
     */
    @Override
    @ColorInt
    public int getTrackColor() {
        return trackColor;
    }

    /**
     * Sets the text color for both the range summary text and the tick labels.
     * Shortcut for calling {@link #setRangeTextColor(int)} and {@link #setTickLabelColor(int)}.
     *
     * @param color The color to set.
     */
    @Override
    public void setTextColor(@ColorInt int color) {
        setRangeTextColor(color);
        setTickLabelColor(color);
    }

    /**
     * Check if the range summary text is currently being displayed.
     *
     * @return True if shown.
     */
    @Override
    public boolean isRangeTextShown() {
        return isRangeTextShown;
    }

    /**
     * Toggle the visibility of the range summary text.
     *
     * @param showRangeText True to show, false to hide.
     */
    @Override
    public void showRangeText(boolean showRangeText) {
        this.isRangeTextShown = showRangeText;
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the range summary text.
     *
     * @param color The text color.
     */
    @Override
    public void setRangeTextColor(@ColorInt int color) {
        this.rangeTextColor = color;
        invalidate();
    }

    /**
     * Get the color of the range summary text.
     *
     * @return The text color.
     */
    @Override
    @ColorInt
    public int getRangeTextColor() {
        return rangeTextColor;
    }

    /**
     * Set the size of the range summary text.
     *
     * @param size Size in SP.
     */
    @Override
    public void setRangeTextSize(float size) {
        this.rangeTextSize = sp(size);
        requestLayout();
        invalidate();
    }

    /**
     * Get the size of the range summary text.
     *
     * @return Size in SP.
     */
    @Override
    public float getRangeTextSize() {
        return rangeTextSize;
    }

    /**
     * Set the typeface style for the range summary text (e.g., Typeface.BOLD).
     *
     * @param style The typeface style.
     */
    @Override
    public void setRangeTextStyle(int style) {
        this.rangeTextStyle = style;
        requestLayout();
        invalidate();
    }

    /**
     * Get the typeface style of the range summary text.
     *
     * @return The typeface style.
     */
    @Override
    public int getRangeTextStyle() {
        return rangeTextStyle;
    }

    /**
     * Set the format string for the range summary text (e.g., "%1$s to %2$s").
     *
     * @param format The format string.
     */
    @Override
    public void setRangeTextFormat(String format) {
        this.rangeTextFormat = format != null ? format : DEFAULT_RANGE_TEXT_FORMAT;
        invalidate();
    }

    /**
     * Get the current format string for the range summary text.
     *
     * @return The format string.
     */
    @Override
    public String getRangeTextFormat() {
        return rangeTextFormat;
    }

    /**
     * Set the relative position of the range summary text (TOP, BOTTOM, CENTER).
     *
     * @param position The position to set.
     */
    @Override
    public void setRangeTextPosition(@NonNull RangeTextPosition position) {
        this.rangeTextPosition = position;
        requestLayout();
        invalidate();
    }

    /**
     * Get the current relative position of the range summary text.
     *
     * @return The current position.
     */
    @Override
    @NonNull
    public RangeTextPosition getRangeTextPosition() {
        return rangeTextPosition;
    }

    /**
     * Set the color of the active range track.
     *
     * @param color The range color.
     */
    @Override
    public void setRangeColor(@ColorInt int color) {
        this.rangeColor = color;
        invalidate();
    }

    /**
     * Get the color of the active range track.
     *
     * @return The range color.
     */
    @Override
    @ColorInt
    public int getRangeColor() {
        return rangeColor;
    }

    /**
     * Set the color of the thumb shadow.
     *
     * @param color The shadow color.
     */
    @Override
    public void setThumbShadowColor(@ColorInt int color) {
        this.thumbShadowColor = color;
        invalidate();
    }

    /**
     * Get the color of the thumb shadow.
     *
     * @return The shadow color.
     */
    @Override
    @ColorInt
    public int getThumbShadowColor() {
        return thumbShadowColor;
    }

    /**
     * Set the horizontal offset of the thumb shadow.
     *
     * @param dx Offset in DP.
     */
    @Override
    public void setThumbShadowDx(float dx) {
        this.thumbShadowDx = dp(dx);
        invalidate();
    }

    /**
     * Get the horizontal offset of the thumb shadow in pixels.
     *
     * @return Offset in pixels.
     */
    @Override
    public float getThumbShadowDx() {
        return thumbShadowDx;
    }

    /**
     * Set the vertical offset of the thumb shadow.
     *
     * @param dy Offset in DP.
     */
    @Override
    public void setThumbShadowDy(float dy) {
        this.thumbShadowDy = dp(dy);
        invalidate();
    }

    /**
     * Get the vertical offset of the thumb shadow in pixels.
     *
     * @return Offset in pixels.
     */
    @Override
    public float getThumbShadowDy() {
        return thumbShadowDy;
    }

    /**
     * Set the elevation (blur radius) of the thumb shadow.
     *
     * @param thumbElevation Elevation in DP.
     */
    @Override
    public void setThumbElevation(float thumbElevation) {
        this.thumbElevation = dp(thumbElevation);
        invalidate();
    }

    /**
     * Get the elevation of the thumb shadow in pixels.
     *
     * @return Elevation in pixels.
     */
    @Override
    public float getThumbElevation() {
        return thumbElevation;
    }

    /**
     * Set all thumb shadow properties at once.
     *
     * @param dxDp           Horizontal offset in DP.
     * @param dyDp           Vertical offset in DP.
     * @param thumbElevation Elevation/Blur in DP.
     * @param color          Shadow color.
     */
    @Override
    public void setThumbShadow(float dxDp, float dyDp, float thumbElevation, @ColorInt int color) {
        this.thumbShadowDx = dp(dxDp);
        this.thumbShadowDy = dp(dyDp);
        this.thumbElevation = dp(thumbElevation);
        this.thumbShadowColor = color;
        invalidate();
    }

    /**
     * Check if a custom drawable is set for the start thumb.
     *
     * @return True if set.
     */
    @Override
    public boolean isStartThumbDrawableSet() {
        return startThumbDrawable != null;
    }

    /**
     * Set a custom drawable for the start thumb.
     *
     * @param drawable The drawable to set.
     */
    @Override
    public void setStartThumbDrawable(@Nullable Drawable drawable) {
        this.startThumbDrawable = drawable;
        invalidate();
    }

    /**
     * Set a custom drawable for the start thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    @Override
    public void setStartThumbDrawable(@DrawableRes int resId) {
        setStartThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Get the current custom drawable for the start thumb.
     *
     * @return The drawable or null.
     */
    @Override
    @Nullable
    public Drawable getStartThumbDrawable() {
        return startThumbDrawable;
    }

    /**
     * Check if a custom drawable is set for the end thumb.
     *
     * @return True if set.
     */
    @Override
    public boolean isEndThumbDrawableSet() {
        return endThumbDrawable != null;
    }

    /**
     * Set a custom drawable for the end thumb.
     *
     * @param drawable The drawable to set.
     */
    @Override
    public void setEndThumbDrawable(@Nullable Drawable drawable) {
        this.endThumbDrawable = drawable;
        invalidate();
    }

    /**
     * Set a custom drawable for the end thumb using a resource ID.
     *
     * @param resId The drawable resource ID.
     */
    @Override
    public void setEndThumbDrawable(@DrawableRes int resId) {
        setEndThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Get the current custom drawable for the end thumb.
     *
     * @return The drawable or null.
     */
    @Override
    @Nullable
    public Drawable getEndThumbDrawable() {
        return endThumbDrawable;
    }

    /**
     * Set custom drawables for both thumbs simultaneously.
     *
     * @param startDrawable The start thumb drawable.
     * @param endDrawable   The end thumb drawable.
     */
    @Override
    public void setThumbDrawables(@Nullable Drawable startDrawable, @Nullable Drawable endDrawable) {
        this.startThumbDrawable = startDrawable;
        this.endThumbDrawable = endDrawable;
        invalidate();
    }

    /**
     * Set custom drawables for both thumbs using resource IDs.
     *
     * @param startResId The start thumb resource ID.
     * @param endResId   The end thumb resource ID.
     */
    @Override
    public void setThumbDrawables(@DrawableRes int startResId, @DrawableRes int endResId) {
        setThumbDrawables(ContextCompat.getDrawable(getContext(), startResId), ContextCompat.getDrawable(getContext(), endResId));
    }

    /**
     * Set the tint color for the start thumb icon.
     *
     * @param color Tint color.
     */
    @Override
    public void setStartThumbDrawableTintColor(@ColorInt int color) {
        this.startThumbDrawableTintColor = color;
        invalidate();
    }

    /**
     * Get the tint color of the start thumb icon.
     *
     * @return Tint color.
     */
    @Override
    @ColorInt
    public int getStartThumbDrawableTintColor() {
        return startThumbDrawableTintColor;
    }

    /**
     * Set the tint color for the end thumb icon.
     *
     * @param color Tint color.
     */
    @Override
    public void setEndThumbDrawableTintColor(@ColorInt int color) {
        this.endThumbDrawableTintColor = color;
        invalidate();
    }

    /**
     * Get the tint color of the end thumb icon.
     *
     * @return Tint color.
     */
    @Override
    @ColorInt
    public int getEndThumbDrawableTintColor() {
        return endThumbDrawableTintColor;
    }

    /**
     * Set the radius of the thumb handles.
     *
     * @param radius Radius in DP.
     */
    @Override
    public void setThumbRadius(float radius) {
        this.thumbRadius = dp(radius);
        requestLayout();
        invalidate();
    }

    /**
     * Get the radius of the thumb handles in pixels.
     *
     * @return Radius in pixels.
     */
    @Override
    public float getThumbRadius() {
        return thumbRadius;
    }

    /**
     * Get the extra touch area padding around the thumbs in pixels.
     *
     * @return Padding in pixels.
     */
    @Override
    public float getThumbTouchRadiusPadding() {
        return thumbTouchRadiusPadding;
    }

    /**
     * Set the extra touch area padding around the thumbs.
     *
     * @param thumbTouchRadiusPadding Padding in DP.
     */
    @Override
    public void setThumbTouchRadiusPadding(float thumbTouchRadiusPadding) {
        this.thumbTouchRadiusPadding = dp(thumbTouchRadiusPadding);
        invalidate();
    }

    /**
     * Set the background fill color of the thumb handles.
     *
     * @param color Fill color.
     */
    @Override
    public void setThumbFillColor(@ColorInt int color) {
        this.thumbFillColor = color;
        invalidate();
    }

    /**
     * Get the background fill color of the thumb handles.
     *
     * @return Fill color.
     */
    @Override
    @ColorInt
    public int getThumbFillColor() {
        return thumbFillColor;
    }

    /**
     * Set the border thickness of the thumb handles.
     *
     * @param width Width in DP.
     */
    @Override
    public void setThumbStrokeWidth(float width) {
        this.thumbStrokeWidth = dp(width);
        invalidate();
    }

    /**
     * Get the border thickness of the thumb handles in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getThumbStrokeWidth() {
        return thumbStrokeWidth;
    }

    /**
     * Set the border color of the thumb handles.
     *
     * @param color Stroke color.
     */
    @Override
    public void setThumbStrokeColor(@ColorInt int color) {
        this.thumbStrokeColor = color;
        invalidate();
    }

    /**
     * Get the border color of the thumb handles.
     *
     * @return Stroke color.
     */
    @Override
    @ColorInt
    public int getThumbStrokeColor() {
        return thumbStrokeColor;
    }

    /**
     * Check if tick marks are currently enabled.
     *
     * @return True if ticks are shown.
     */
    @Override
    public boolean isShowTicks() {
        return showTicks;
    }

    /**
     * Toggle the visibility of all tick marks.
     *
     * @param showTicks True to show.
     */
    @Override
    public void setShowTicks(boolean showTicks) {
        this.showTicks = showTicks;
        requestLayout();
        invalidate();
    }

    /**
     * Check if hour labels are currently enabled.
     *
     * @return True if labels are shown.
     */
    @Override
    public boolean isShowTickLabels() {
        return showTickLabels;
    }

    /**
     * Toggle the visibility of the hour labels (e.g., 12, 14, 16).
     *
     * @param show True to show.
     */
    @Override
    public void setShowTickLabels(boolean show) {
        this.showTickLabels = show;
        requestLayout();
        invalidate();
    }

    /**
     * Get the distance between the track and the tick marks in pixels.
     *
     * @return Distance in pixels.
     */
    @Override
    public float getTickDistanceFromTrack() {
        return tickDistanceFromTrack;
    }

    /**
     * Set the distance between the track and the tick marks.
     *
     * @param distance Distance in DP.
     */
    @Override
    public void setTickDistanceFromTrack(float distance) {
        this.tickDistanceFromTrack = dp(distance);
        requestLayout();
        invalidate();
    }

    /**
     * Get the distance between tick marks and their respective labels in pixels.
     *
     * @return Distance in pixels.
     */
    @Override
    public float getTickLabelDistanceFromTick() {
        return tickLabelDistanceFromTick;
    }

    /**
     * Set the distance between tick marks and their respective labels.
     *
     * @param distance Distance in DP.
     */
    @Override
    public void setTickLabelDistanceFromTick(float distance) {
        this.tickLabelDistanceFromTick = dp(distance);
        requestLayout();
        invalidate();
    }

    /**
     * Set the typeface style for the tick labels (e.g., Typeface.ITALIC).
     *
     * @param style Typeface style.
     */
    @Override
    public void setTickLabelStyle(int style) {
        this.tickLabelStyle = style;
        requestLayout();
        invalidate();
    }

    /**
     * Get the typeface style for the tick labels.
     *
     * @return Typeface style.
     */
    @Override
    public int getTickLabelStyle() {
        return tickLabelStyle;
    }

    /**
     * Set the font size of the tick labels.
     *
     * @param size Size in SP.
     */
    @Override
    public void setTickLabelSize(float size) {
        this.tickLabelSize = sp(size);
        requestLayout();
        invalidate();
    }

    /**
     * Get the font size of the tick labels in pixels.
     *
     * @return Size in pixels.
     */
    @Override
    public float getTickLabelSize() {
        return tickLabelSize;
    }

    /**
     * Check if AM/PM labels are shown in 12-hour mode.
     *
     * @return True if shown.
     */
    @Override
    public boolean isAmPmLabelsShown() {
        return showAmPmLabels;
    }

    /**
     * Toggle the visibility of AM/PM labels (applicable only in 12-hour format).
     *
     * @param show True to show.
     */
    @Override
    public void showAmPmLabels(boolean show) {
        this.showAmPmLabels = show;
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the hour tick marks.
     *
     * @param color Tick color.
     */
    @Override
    public void setHourTickColor(@ColorInt int color) {
        this.hourTickColor = color;
        invalidate();
    }

    /**
     * Get the color of the hour tick marks.
     *
     * @return Tick color.
     */
    @Override
    @ColorInt
    public int getHourTickColor() {
        return hourTickColor;
    }

    /**
     * Set the color of the minute tick marks (e.g., 15-min intervals).
     *
     * @param color Tick color.
     */
    @Override
    public void setMinuteTickColor(@ColorInt int color) {
        this.minuteTickColor = color;
        invalidate();
    }

    /**
     * Get the color of the minute tick marks.
     *
     * @return Tick color.
     */
    @Override
    @ColorInt
    public int getMinuteTickColor() {
        return minuteTickColor;
    }

    /**
     * Set colors for both hour and minute ticks.
     *
     * @param hourColor   Color for hour ticks.
     * @param minuteColor Color for minute ticks.
     */
    @Override
    public void setTickColors(@ColorInt int hourColor, @ColorInt int minuteColor) {
        this.hourTickColor = hourColor;
        this.minuteTickColor = minuteColor;
        invalidate();
    }

    /**
     * Set the thickness of the hour tick marks.
     *
     * @param width Width in DP.
     */
    @Override
    public void setHourTickWidth(float width) {
        this.hourTickWidth = dp(width);
        invalidate();
    }

    /**
     * Get the thickness of the hour tick marks in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getHourTickWidth() {
        return hourTickWidth;
    }

    /**
     * Set the thickness of the minute tick marks.
     *
     * @param width Width in DP.
     */
    @Override
    public void setMinuteTickWidth(float width) {
        this.minuteTickWidth = dp(width);
        invalidate();
    }

    /**
     * Get the thickness of the minute tick marks in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getMinuteTickWidth() {
        return minuteTickWidth;
    }

    /**
     * Set thickness for both hour and minute ticks simultaneously.
     *
     * @param hourTickWidth   Width for hour ticks in DP.
     * @param minuteTickWidth Width for minute ticks in DP.
     */
    @Override
    public void setTicksWidth(float hourTickWidth, float minuteTickWidth) {
        this.hourTickWidth = dp(hourTickWidth);
        this.minuteTickWidth = dp(minuteTickWidth);
        invalidate();
    }

    /**
     * Set the length of the hour tick marks.
     *
     * @param height Length in DP.
     */
    @Override
    public void setHourTickHeight(float height) {
        this.hourTickHeight = dp(height);
        requestLayout();
        invalidate();
    }

    /**
     * Get the length of the hour tick marks in pixels.
     *
     * @return Length in pixels.
     */
    @Override
    public float getHourTickHeight() {
        return hourTickHeight;
    }

    /**
     * Set the length of the minute tick marks.
     *
     * @param height Length in DP.
     */
    @Override
    public void setMinuteTickHeight(float height) {
        this.minuteTickHeight = dp(height);
        requestLayout();
        invalidate();
    }

    /**
     * Get the length of the minute tick marks in pixels.
     *
     * @return Length in pixels.
     */
    @Override
    public float getMinuteTickHeight() {
        return minuteTickHeight;
    }

    /**
     * Set lengths for both hour and minute ticks simultaneously.
     *
     * @param hourTickHeight   Length for hour ticks in DP.
     * @param minuteTickHeight Length for minute ticks in DP.
     */
    @Override
    public void setTicksHeight(float hourTickHeight, float minuteTickHeight) {
        this.hourTickHeight = dp(hourTickHeight);
        this.minuteTickHeight = dp(minuteTickHeight);
        requestLayout();
        invalidate();
    }

    /**
     * Set the color of the hour labels.
     *
     * @param color Label color.
     */
    @Override
    public void setTickLabelColor(@ColorInt int color) {
        this.tickLabelColor = color;
        invalidate();
    }

    /**
     * Get the color of the hour labels.
     *
     * @return Label color.
     */
    @Override
    @ColorInt
    public int getTickLabelColor() {
        return tickLabelColor;
    }

    /**
     * Set the edge style (cap style) for the tick marks (ROUND or BUTT).
     *
     * @param style Edge style.
     */
    @Override
    public void setTickEdgeStyle(TickEdgeStyle style) {
        this.tickEdgeStyle = style;
        invalidate();
    }

    /**
     * Get the current edge style of the tick marks.
     *
     * @return Edge style.
     */
    @Override
    @NonNull
    public TickEdgeStyle getTickEdgeStyle() {
        return tickEdgeStyle;
    }

    /**
     * Set the thickness of the background track.
     *
     * @param width Width in DP.
     */
    @Override
    public void setTrackWidth(float width) {
        this.trackWidth = dp(width);
        requestLayout();
        invalidate();
    }

    /**
     * Get the thickness of the background track in pixels.
     *
     * @return Width in pixels.
     */
    @Override
    public float getTrackWidth() {
        return trackWidth;
    }

    /**
     * Check if the picker is currently using 24-hour format.
     *
     * @return True if 24-hour format.
     */
    @Override
    public boolean is24HourFormat() {
        return is24HourFormat;
    }

    /**
     * Toggle between 24-hour and 12-hour time formats.
     *
     * @param is24HourFormat True for 24-hour, false for 12-hour.
     */
    @Override
    public void set24HourFormat(boolean is24HourFormat) {
        this.is24HourFormat = is24HourFormat;
        requestLayout();
        invalidate();
    }

    /**
     * Notifies the listener about a range change event.
     *
     * @param finished True if the user has finished the interaction, false otherwise.
     */
    protected void notifyRangeChanged(boolean finished) {
        if (listener != null) {
            if (finished) {
                listener.onInteractionFinished(currentStartMinutes, currentEndMinutes, isOvernight());
            } else {
                listener.onRangeChanged(currentStartMinutes, currentEndMinutes, isOvernight());
            }
        }
    }

    /**
     * Returns the formatted string representation of the currently selected time range.
     *
     * @return Formatted range text.
     */
    protected String getFormattedRangeText() {
        String am = getContext().getString(R.string.tss_am);
        String pm = getContext().getString(R.string.tss_pm);
        String startStr = TimeUtils.formatDisplayTime(currentStartMinutes, is24HourFormat, am, pm);
        String endStr = TimeUtils.formatDisplayTime(currentEndMinutes, is24HourFormat, am, pm);
        return String.format(rangeTextFormat, startStr, endStr);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_START_MINUTES, currentStartMinutes);
        bundle.putInt(KEY_END_MINUTES, currentEndMinutes);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            currentStartMinutes = bundle.getInt(KEY_START_MINUTES);
            currentEndMinutes = bundle.getInt(KEY_END_MINUTES);
            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_STATE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }
}
