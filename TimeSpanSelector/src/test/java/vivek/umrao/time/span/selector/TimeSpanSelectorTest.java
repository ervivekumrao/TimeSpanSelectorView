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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class TimeSpanSelectorTest {

    private CircularTimeSpanSelector circularPicker;
    private LinearTimeSpanSelector linearPicker;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        circularPicker = new CircularTimeSpanSelector(context);
        linearPicker = new LinearTimeSpanSelector(context);
    }

    @Test
    public void testDefaultValues() {
        assertEquals(TimeSpanSelector.DEFAULT_START_MINUTES, circularPicker.getRangeStartInMinutes());
        assertEquals(TimeSpanSelector.DEFAULT_END_MINUTES, circularPicker.getRangeEndInMinutes());
        assertTrue(circularPicker.is24HourFormat());
        assertTrue(circularPicker.isShowTicks());
        assertTrue(circularPicker.isShowTickLabels());
        assertTrue(circularPicker.isRangeTextShown());
        assertTrue(circularPicker.isOvernightRangeAllowed());
        assertEquals(TimeSpanSelector.DEFAULT_THUMB_MINUTE_STEP, circularPicker.getThumbMinuteStep());
    }

    @Test
    public void testSetRangeInMinutes() {
        circularPicker.setRangeInMinutes(600, 1200);
        assertEquals(600, circularPicker.getRangeStartInMinutes());
        assertEquals(1200, circularPicker.getRangeEndInMinutes());
        assertFalse(circularPicker.isOvernight());
    }

    @Test
    public void testOvernightRange() {
        circularPicker.setRangeInMinutes(1320, 120); // 10 PM to 2 AM
        assertTrue(circularPicker.isOvernight());
        assertEquals(240, circularPicker.getDurationInMinutes());
    }

    @Test
    public void testSetRangeStartTime() {
        circularPicker.setRangeStartTime("08:30");
        assertEquals(8 * 60 + 30, circularPicker.getRangeStartInMinutes());
    }

    @Test
    public void testSetRangeEndTime() {
        circularPicker.setRangeEndTime("18:45");
        assertEquals(18 * 60 + 45, circularPicker.getRangeEndInMinutes());
    }

    @Test
    public void testSetRangeTime() {
        circularPicker.setRangeTime("10:00", "20:00");
        assertEquals(10 * 60, circularPicker.getRangeStartInMinutes());
        assertEquals(20 * 60, circularPicker.getRangeEndInMinutes());
    }

    @Test
    public void testOnRangeChangeListener() {
        TimeSpanSelector.OnRangeChangeListener listener = mock(TimeSpanSelector.OnRangeChangeListener.class);
        circularPicker.setOnRangeChangeListener(listener);
        assertEquals(listener, circularPicker.getOnRangeChangeListener());

        circularPicker.setRangeInMinutes(500, 600);
        verify(listener, atLeastOnce()).onRangeChanged(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    public void testOnTimeChangeListener() {
        TimeSpanSelector.OnTimeChangeListener listener = mock(TimeSpanSelector.OnTimeChangeListener.class);
        circularPicker.setThumbMinuteStep(1); // Disable snapping for this test
        circularPicker.setOnTimeChangeListener(listener);
        assertEquals(listener, circularPicker.getOnTimeChangeListener());

        // Default: Start=540, End=1020, Duration=480
        // Setting both separately to control the order and calls
        circularPicker.setRangeStartInMinutes(100);
        verify(listener, atLeastOnce()).onStartTimeChange(100);
        
        circularPicker.setRangeEndInMinutes(200);
        verify(listener, atLeastOnce()).onEndTimeChange(200);
        
        // Duration was 480, now it's 100
        verify(listener, atLeastOnce()).onDurationChange(100);
    }

    @Test
    public void testOnDragChangeListener() {
        TimeSpanSelector.OnDragChangeListener listener = mock(TimeSpanSelector.OnDragChangeListener.class);
        circularPicker.setOnDragChangeListener(listener);
        assertEquals(listener, circularPicker.getOnDragChangeListener());
    }

    @Test
    public void testOnDragChangeEvents() {
        TimeSpanSelector.OnDragChangeListener listener = mock(TimeSpanSelector.OnDragChangeListener.class);
        org.mockito.Mockito.when(listener.onDragStart(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        circularPicker.setOnDragChangeListener(listener);

        // Layout the view to calculate dimensions
        circularPicker.measure(View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
                             View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
        circularPicker.layout(0, 0, 1000, 1000);

        // Set start thumb to 0 minutes (12 o'clock)
        circularPicker.setThumbMinuteStep(1);
        circularPicker.setRangeStartInMinutes(0);

        // 12 o'clock in the picker corresponds to angle 0, which is (cx, cy - viewRadius)
        // From CircularTimeSpanSelector.onSizeChanged: cx=500, cy=500
        // viewRadius is approx 500 - 21 = 479
        float touchX = 500f;
        float touchY = 21f;

        // Simulate touch down
        android.view.MotionEvent downEvent = android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_DOWN, touchX, touchY, 0);
        circularPicker.onTouchEvent(downEvent);
        verify(listener).onDragStart(TimeSpanSelector.Thumb.START);

        // Simulate touch up
        android.view.MotionEvent upEvent = android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_UP, touchX, touchY, 0);
        circularPicker.onTouchEvent(upEvent);
        verify(listener).onDragStop(TimeSpanSelector.Thumb.START);
        
        downEvent.recycle();
        upEvent.recycle();
    }

    @Test
    public void testMinMaxDuration() {
        circularPicker.setMinDurationMinutes(60);
        assertEquals(60, circularPicker.getMinDurationMinutes());

        circularPicker.setMaxDurationMinutes(120);
        assertEquals(120, circularPicker.getMaxDurationMinutes());
    }

    @Test
    public void testOvernightAllowed() {
        circularPicker.setOvernightRangeAllowed(false);
        assertFalse(circularPicker.isOvernightRangeAllowed());
    }

    @Test
    public void testThumbMinuteStep() {
        circularPicker.setThumbMinuteStep(30);
        assertEquals(30, circularPicker.getThumbMinuteStep());
    }

    @Test
    public void testColors() {
        circularPicker.setAccentColor(Color.RED);
        assertEquals(Color.RED, circularPicker.getAccentColor());

        circularPicker.setTrackColor(Color.GRAY);
        assertEquals(Color.GRAY, circularPicker.getTrackColor());

        circularPicker.setRangeColor(Color.GREEN);
        assertEquals(Color.GREEN, circularPicker.getRangeColor());

        circularPicker.setRangeTextColor(Color.YELLOW);
        assertEquals(Color.YELLOW, circularPicker.getRangeTextColor());

        circularPicker.setThumbFillColor(Color.WHITE);
        assertEquals(Color.WHITE, circularPicker.getThumbFillColor());

        circularPicker.setThumbStrokeColor(Color.BLACK);
        assertEquals(Color.BLACK, circularPicker.getThumbStrokeColor());

        circularPicker.setThumbShadowColor(Color.DKGRAY);
        assertEquals(Color.DKGRAY, circularPicker.getThumbShadowColor());

        circularPicker.setHourTickColor(Color.RED);
        assertEquals(Color.RED, circularPicker.getHourTickColor());

        circularPicker.setMinuteTickColor(Color.BLUE);
        assertEquals(Color.BLUE, circularPicker.getMinuteTickColor());

        circularPicker.setTickLabelColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, circularPicker.getTickLabelColor());
    }

    @Test
    public void testTextSettings() {
        circularPicker.showRangeText(false);
        assertFalse(circularPicker.isRangeTextShown());

        circularPicker.setRangeTextSize(20f);
        assertEquals(20f, circularPicker.getRangeTextSize(), 0.1f);

        circularPicker.setRangeTextStyle(Typeface.BOLD);
        assertEquals(Typeface.BOLD, circularPicker.getRangeTextStyle());

        circularPicker.setRangeTextFormat("%s to %s");
        assertEquals("%s to %s", circularPicker.getRangeTextFormat());

        circularPicker.setRangeTextPosition(TimeSpanSelector.RangeTextPosition.CENTER);
        assertEquals(TimeSpanSelector.RangeTextPosition.CENTER, circularPicker.getRangeTextPosition());
    }

    @Test
    public void testThumbSettings() {
        circularPicker.setThumbRadius(15f);
        assertEquals(15f, circularPicker.getThumbRadius(), 0.1f);

        circularPicker.setThumbTouchRadiusPadding(10f);
        assertEquals(10f, circularPicker.getThumbTouchRadiusPadding(), 0.1f);

        circularPicker.setThumbStrokeWidth(2f);
        assertEquals(2f, circularPicker.getThumbStrokeWidth(), 0.1f);

        circularPicker.setThumbElevation(5f);
        assertEquals(5f, circularPicker.getThumbElevation(), 0.1f);

        circularPicker.setThumbShadowDx(1f);
        assertEquals(1f, circularPicker.getThumbShadowDx(), 0.1f);

        circularPicker.setThumbShadowDy(1f);
        assertEquals(1f, circularPicker.getThumbShadowDy(), 0.1f);
    }

    @Test
    public void testTickSettings() {
        circularPicker.setShowTicks(false);
        assertFalse(circularPicker.isShowTicks());

        circularPicker.setShowTickLabels(false);
        assertFalse(circularPicker.isShowTickLabels());

        circularPicker.setTickDistanceFromTrack(10f);
        assertEquals(10f, circularPicker.getTickDistanceFromTrack(), 0.1f);

        circularPicker.setTickLabelDistanceFromTick(5f);
        assertEquals(5f, circularPicker.getTickLabelDistanceFromTick(), 0.1f);

        circularPicker.setTickLabelSize(12f);
        assertEquals(12f, circularPicker.getTickLabelSize(), 0.1f);

        circularPicker.setTickLabelStyle(Typeface.ITALIC);
        assertEquals(Typeface.ITALIC, circularPicker.getTickLabelStyle());

        circularPicker.showAmPmLabels(true);
        assertTrue(circularPicker.isAmPmLabelsShown());

        circularPicker.setHourTickWidth(4f);
        assertEquals(4f, circularPicker.getHourTickWidth(), 0.1f);

        circularPicker.setMinuteTickWidth(2f);
        assertEquals(2f, circularPicker.getMinuteTickWidth(), 0.1f);

        circularPicker.setHourTickHeight(20f);
        assertEquals(20f, circularPicker.getHourTickHeight(), 0.1f);

        circularPicker.setMinuteTickHeight(10f);
        assertEquals(10f, circularPicker.getMinuteTickHeight(), 0.1f);

        circularPicker.setTickEdgeStyle(TimeSpanSelector.TickEdgeStyle.ROUND);
        assertEquals(TimeSpanSelector.TickEdgeStyle.ROUND, circularPicker.getTickEdgeStyle());
    }

    @Test
    public void testTrackSettings() {
        circularPicker.setTrackWidth(10f);
        assertEquals(10f, circularPicker.getTrackWidth(), 0.1f);
    }

    @Test
    public void test24HourFormat() {
        circularPicker.set24HourFormat(false);
        assertFalse(circularPicker.is24HourFormat());
    }

    @Test
    public void testThumbDrawables() {
        circularPicker.setStartThumbDrawable(android.R.drawable.ic_menu_add);
        assertTrue(circularPicker.isStartThumbDrawableSet());
        assertNotNull(circularPicker.getStartThumbDrawable());

        circularPicker.setEndThumbDrawable(android.R.drawable.ic_menu_delete);
        assertTrue(circularPicker.isEndThumbDrawableSet());
        assertNotNull(circularPicker.getEndThumbDrawable());

        circularPicker.setStartThumbDrawableTintColor(Color.RED);
        assertEquals(Color.RED, circularPicker.getStartThumbDrawableTintColor());

        circularPicker.setEndThumbDrawableTintColor(Color.BLUE);
        assertEquals(Color.BLUE, circularPicker.getEndThumbDrawableTintColor());
    }

    @Test
    public void testLinearPickerApis() {
        // Since both inherit from BaseTimeSpanSelector, testing some specific ones on Linear
        linearPicker.setThumbMinuteStep(1);
        linearPicker.setRangeInMinutes(100, 200);
        assertEquals(100, linearPicker.getRangeStartInMinutes());
        assertEquals(200, linearPicker.getRangeEndInMinutes());

        linearPicker.setAccentColor(Color.CYAN);
        assertEquals(Color.CYAN, linearPicker.getAccentColor());
    }

    @Test
    public void testAttributes() {
        Context context = ApplicationProvider.getApplicationContext();
        // Inflate from layout file to test attribute parsing
        View layout = LayoutInflater.from(context).inflate(R.layout.test_attributes, null);
        CircularTimeSpanSelector picker = layout.findViewById(R.id.circular_selector_attrs);

        assertEquals(120, picker.getRangeStartInMinutes());
        assertEquals(240, picker.getRangeEndInMinutes());
        assertEquals(5, picker.getThumbMinuteStep());
        assertFalse(picker.is24HourFormat());
        assertFalse(picker.isOvernightRangeAllowed());
        assertEquals(30, picker.getMinDurationMinutes());
        assertEquals(600, picker.getMaxDurationMinutes());
        assertEquals(Color.RED, picker.getTrackColor());
        assertEquals(Color.GREEN, picker.getRangeColor());
        assertEquals(context.getResources().getDisplayMetrics().density * 10, picker.getTrackWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 15, picker.getThumbRadius(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 12, picker.getThumbTouchRadiusPadding(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 2, picker.getThumbStrokeWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 20, picker.getHourTickHeight(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 10, picker.getMinuteTickHeight(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 4, picker.getHourTickWidth(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 2, picker.getMinuteTickWidth(), 0.1f);
        assertEquals(Color.BLUE, picker.getHourTickColor());
        assertEquals(Color.YELLOW, picker.getMinuteTickColor());
        assertTrue(picker.isShowTicks());
        assertTrue(picker.isShowTickLabels());
        assertEquals(context.getResources().getDisplayMetrics().density * 8, picker.getTickDistanceFromTrack(), 0.1f);
        assertEquals(TimeSpanSelector.TickEdgeStyle.ROUND, picker.getTickEdgeStyle());
        assertTrue(picker.isAmPmLabelsShown());
        // Using 14sp -> density * 14
        assertEquals(context.getResources().getDisplayMetrics().scaledDensity * 14, picker.getTickLabelSize(), 0.1f);
        assertEquals(Color.MAGENTA, picker.getTickLabelColor());
        assertEquals(Typeface.BOLD, picker.getTickLabelStyle());
        assertEquals(context.getResources().getDisplayMetrics().density * 6, picker.getTickLabelDistanceFromTick(), 0.1f);
        assertEquals(Color.WHITE, picker.getThumbFillColor());
        assertEquals(Color.BLACK, picker.getThumbStrokeColor());
        assertEquals(context.getResources().getDisplayMetrics().density * 3, picker.getThumbShadowDx(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 3, picker.getThumbShadowDy(), 0.1f);
        assertEquals(context.getResources().getDisplayMetrics().density * 4, picker.getThumbElevation(), 0.1f);
        assertEquals(0xFF888888, picker.getThumbShadowColor());

        assertTrue(picker.isRangeTextShown());
        assertEquals(Color.CYAN, picker.getRangeTextColor());
        assertEquals(context.getResources().getDisplayMetrics().scaledDensity * 18, picker.getRangeTextSize(), 0.1f);
        assertEquals("Selected: %1$s to %2$s", picker.getRangeTextFormat());
        assertEquals(Typeface.ITALIC, picker.getRangeTextStyle());
        assertEquals(TimeSpanSelector.RangeTextPosition.TOP, picker.getRangeTextPosition());

        // Linear Picker
        LinearTimeSpanSelector linearPicker = layout.findViewById(R.id.linear_selector_attrs);
        assertEquals(8 * 60, linearPicker.getRangeStartInMinutes());
        assertEquals(20 * 60, linearPicker.getRangeEndInMinutes());
        assertEquals(0xFF333333, linearPicker.getRangeTextColor());
        assertEquals(0xFF333333, linearPicker.getTickLabelColor());
    }
}
