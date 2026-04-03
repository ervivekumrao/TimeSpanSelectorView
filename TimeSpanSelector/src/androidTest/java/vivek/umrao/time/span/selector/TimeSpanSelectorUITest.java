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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instrumented tests for TimeSpanSelector library.
 * Provides full coverage of the public API and ensures correct behavior in a real Android environment.
 */
@RunWith(AndroidJUnit4.class)
public class TimeSpanSelectorUITest {

    private Context context;
    private float density;
    private float scaledDensity;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    }

    private float dp(float val) {
        return val * density;
    }

    private float sp(float val) {
        return val * scaledDensity;
    }

    @Test
    public void testLinearPickerInflation() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        assertNotNull("LinearTimeSpanSelector should be inflatable", picker);

        // Default values from TimeSpanSelector.java
        assertEquals("Default start time", 540, picker.getRangeStartInMinutes());
        assertEquals("Default end time", 1020, picker.getRangeEndInMinutes());
    }

    @Test
    public void testCircularPickerInflation() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        assertNotNull("CircularTimeSpanSelector should be inflatable", picker);

        assertEquals("Default start time", 540, picker.getRangeStartInMinutes());
        assertEquals("Default end time", 1020, picker.getRangeEndInMinutes());
    }

    @Test
    public void testProgrammaticSetters() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        picker.setThumbMinuteStep(1);

        picker.setRangeInMinutes(180, 300); // 03:00 to 05:00

        assertEquals(180, picker.getRangeStartInMinutes());
        assertEquals(300, picker.getRangeEndInMinutes());
        assertEquals(120, picker.getDurationInMinutes());
        assertFalse(picker.isOvernight());
    }

    @Test
    public void testStringAPI() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);

        picker.setRangeStartTime("10:00");
        picker.setRangeEndTime("14:30");

        assertEquals(600, picker.getRangeStartInMinutes());
        assertEquals(870, picker.getRangeEndInMinutes());

        picker.setRangeTime("08:00", "18:00");
        assertEquals(480, picker.getRangeStartInMinutes());
        assertEquals(1080, picker.getRangeEndInMinutes());
    }

    @Test
    public void testDurationConstraints() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        picker.setThumbMinuteStep(1);

        picker.setMinDurationMinutes(60);
        picker.setMaxDurationMinutes(120);

        assertEquals(60, picker.getMinDurationMinutes());
        assertEquals(120, picker.getMaxDurationMinutes());

        // Setting range less than min should snap to min
        picker.setRangeInMinutes(100, 110);
        assertEquals(50, picker.getRangeStartInMinutes());
        assertEquals(110, picker.getRangeEndInMinutes());
    }

    @Test
    public void testStateRestoration() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        picker.setThumbMinuteStep(1);
        picker.setRangeInMinutes(60, 180);

        Parcelable state = picker.onSaveInstanceState();
        assertNotNull("State should not be null", state);

        LinearTimeSpanSelector newPicker = new LinearTimeSpanSelector(context);
        newPicker.onRestoreInstanceState(state);

        assertEquals(60, newPicker.getRangeStartInMinutes());
        assertEquals(180, newPicker.getRangeEndInMinutes());
    }

    @Test
    public void testVisualAttributes() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);

        picker.setAccentColor(Color.RED);
        assertEquals(Color.RED, picker.getAccentColor());
        assertEquals(Color.RED, picker.getRangeColor());

        picker.setTrackColor(Color.GRAY);
        assertEquals(Color.GRAY, picker.getTrackColor());

        picker.setTrackWidth(20f);
        assertEquals(dp(20f), picker.getTrackWidth(), 0.01f);

        picker.setThumbRadius(15f);
        assertEquals(dp(15f), picker.getThumbRadius(), 0.01f);

        picker.setRangeTextColor(Color.BLUE);
        assertEquals(Color.BLUE, picker.getRangeTextColor());

        picker.setTextColor(Color.YELLOW);
        assertEquals(Color.YELLOW, picker.getRangeTextColor());
        assertEquals(Color.YELLOW, picker.getTickLabelColor());
    }

    @Test
    public void testThumbShadowAttributes() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        picker.setThumbShadowColor(Color.BLACK);
        assertEquals(Color.BLACK, picker.getThumbShadowColor());

        picker.setThumbShadowDx(5f);
        assertEquals(dp(5f), picker.getThumbShadowDx(), 0.01f);

        picker.setThumbShadowDy(5f);
        assertEquals(dp(5f), picker.getThumbShadowDy(), 0.01f);

        picker.setThumbElevation(10f);
        assertEquals(dp(10f), picker.getThumbElevation(), 0.01f);

        picker.setThumbShadow(2f, 2f, 8f, Color.DKGRAY);
        assertEquals(Color.DKGRAY, picker.getThumbShadowColor());
    }

    @Test
    public void testThumbDrawableAttributes() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        Drawable d1 = new ColorDrawable(Color.RED);
        Drawable d2 = new ColorDrawable(Color.BLUE);

        picker.setStartThumbDrawable(d1);
        assertTrue(picker.isStartThumbDrawableSet());
        assertEquals(d1, picker.getStartThumbDrawable());

        picker.setEndThumbDrawable(d2);
        assertTrue(picker.isEndThumbDrawableSet());
        assertEquals(d2, picker.getEndThumbDrawable());

        picker.setStartThumbDrawableTintColor(Color.GREEN);
        assertEquals(Color.GREEN, picker.getStartThumbDrawableTintColor());

        picker.setEndThumbDrawableTintColor(Color.CYAN);
        assertEquals(Color.CYAN, picker.getEndThumbDrawableTintColor());

        picker.setThumbDrawables(d2, d1);
        assertEquals(d2, picker.getStartThumbDrawable());
        assertEquals(d1, picker.getEndThumbDrawable());
    }

    @Test
    public void testThumbHandleAttributes() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        picker.setThumbFillColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, picker.getThumbFillColor());

        picker.setThumbStrokeWidth(2f);
        assertEquals(dp(2f), picker.getThumbStrokeWidth(), 0.01f);

        picker.setThumbStrokeColor(Color.WHITE);
        assertEquals(Color.WHITE, picker.getThumbStrokeColor());

        picker.setThumbTouchRadiusPadding(10f);
        assertEquals(dp(10f), picker.getThumbTouchRadiusPadding(), 0.01f);
    }

    @Test
    public void testTickAttributes() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);

        picker.setShowTicks(true);
        assertTrue(picker.isShowTicks());

        picker.setShowTickLabels(false);
        assertFalse(picker.isShowTickLabels());

        picker.setHourTickColor(Color.GREEN);
        assertEquals(Color.GREEN, picker.getHourTickColor());

        picker.setMinuteTickColor(Color.YELLOW);
        assertEquals(Color.YELLOW, picker.getMinuteTickColor());

        picker.setTickLabelSize(12f);
        assertEquals(sp(12f), picker.getTickLabelSize(), 0.01f);

        picker.setTickLabelStyle(Typeface.BOLD);
        assertEquals(Typeface.BOLD, picker.getTickLabelStyle());

        picker.setTickDistanceFromTrack(5f);
        assertEquals(dp(5f), picker.getTickDistanceFromTrack(), 0.01f);

        picker.setTickLabelDistanceFromTick(10f);
        assertEquals(dp(10f), picker.getTickLabelDistanceFromTick(), 0.01f);

        picker.setTickEdgeStyle(TimeSpanSelector.TickEdgeStyle.ROUND);
        assertEquals(TimeSpanSelector.TickEdgeStyle.ROUND, picker.getTickEdgeStyle());

        picker.setTickColors(Color.RED, Color.BLUE);
        assertEquals(Color.RED, picker.getHourTickColor());
        assertEquals(Color.BLUE, picker.getMinuteTickColor());
    }

    @Test
    public void testTickSizeAttributes() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        picker.setHourTickWidth(4f);
        assertEquals(dp(4f), picker.getHourTickWidth(), 0.01f);

        picker.setMinuteTickWidth(2f);
        assertEquals(dp(2f), picker.getMinuteTickWidth(), 0.01f);

        picker.setHourTickHeight(20f);
        assertEquals(dp(20f), picker.getHourTickHeight(), 0.01f);

        picker.setMinuteTickHeight(10f);
        assertEquals(dp(10f), picker.getMinuteTickHeight(), 0.01f);

        picker.setTicksWidth(6f, 3f);
        assertEquals(dp(6f), picker.getHourTickWidth(), 0.01f);
        assertEquals(dp(3f), picker.getMinuteTickWidth(), 0.01f);

        picker.setTicksHeight(30f, 15f);
        assertEquals(dp(30f), picker.getHourTickHeight(), 0.01f);
        assertEquals(dp(15f), picker.getMinuteTickHeight(), 0.01f);
    }

    @Test
    public void testFormatAndVisibility() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);

        picker.set24HourFormat(false);
        assertFalse(picker.is24HourFormat());

        picker.showRangeText(false);
        assertFalse(picker.isRangeTextShown());

        picker.showAmPmLabels(true);
        assertTrue(picker.isAmPmLabelsShown());

        picker.setRangeTextFormat("From %1$s To %2$s");
        assertEquals("From %1$s To %2$s", picker.getRangeTextFormat());

        picker.setRangeTextPosition(TimeSpanSelector.RangeTextPosition.TOP);
        assertEquals(TimeSpanSelector.RangeTextPosition.TOP, picker.getRangeTextPosition());

        picker.setRangeTextSize(18f);
        assertEquals(sp(18f), picker.getRangeTextSize(), 0.01f);

        picker.setRangeTextStyle(Typeface.ITALIC);
        assertEquals(Typeface.ITALIC, picker.getRangeTextStyle());
    }

    @Test
    public void testOvernightRangeLogic() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        picker.setOvernightRangeAllowed(true);
        assertTrue(picker.isOvernightRangeAllowed());

        // 10:00 PM (1320) to 02:00 AM (120)
        picker.setRangeInMinutes(1320, 120);
        assertTrue(picker.isOvernight());
        assertEquals(1320, picker.getRangeStartInMinutes());
        assertEquals(120, picker.getRangeEndInMinutes());
        assertEquals(240, picker.getDurationInMinutes());
    }

    @Test
    public void testListeners() {
        LinearTimeSpanSelector picker = new LinearTimeSpanSelector(context);
        picker.setThumbMinuteStep(1);

        AtomicInteger startValue = new AtomicInteger(-1);
        AtomicInteger endValue = new AtomicInteger(-1);

        picker.setOnRangeChangeListener(new TimeSpanSelector.OnRangeChangeListener() {
            @Override
            public void onRangeChanged(int startMinutes, int endMinutes, boolean isOvernight) {
                startValue.set(startMinutes);
                endValue.set(endMinutes);
            }

            @Override
            public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
            }
        });

        picker.setRangeInMinutes(100, 200);
        assertEquals(100, startValue.get());
        assertEquals(200, endValue.get());

        assertNotNull(picker.getOnRangeChangeListener());

        picker.setOnTimeChangeListener(new TimeSpanSelector.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(int startTimeMinutes) {
            }

            @Override
            public void onEndTimeChange(int endTimeMinutes) {
            }

            @Override
            public void onDurationChange(int durationMinutes) {
            }
        });
        assertNotNull(picker.getOnTimeChangeListener());

        picker.setOnDragChangeListener(new TimeSpanSelector.OnDragChangeListener() {
            @Override
            public boolean onDragStart(@NonNull TimeSpanSelector.Thumb thumb) {
                return true;
            }

            @Override
            public void onDragStop(@NonNull TimeSpanSelector.Thumb thumb) {
            }
        });
        assertNotNull(picker.getOnDragChangeListener());
    }

    @Test
    public void testBoundaryValues() {
        CircularTimeSpanSelector picker = new CircularTimeSpanSelector(context);
        picker.setThumbMinuteStep(1);

        picker.setRangeInMinutes(0, 1439);
        assertEquals(0, picker.getRangeStartInMinutes());
        assertEquals(1439, picker.getRangeEndInMinutes());
    }
}
