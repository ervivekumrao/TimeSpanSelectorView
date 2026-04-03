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

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Utility class for time-related calculations and formatting.
 */
public class TimeUtils {

    /**
     * Total minutes in a single 24-hour day.
     */
    public static final int MINUTES_IN_DAY = 24 * 60;

    /**
     * Formats a total number of minutes into a human-readable string.
     *
     * @param totalMinutes   The minutes to format.
     * @param is24HourFormat True for 24-hour, false for 12-hour.
     * @param amString       Localized string for AM.
     * @param pmString       Localized string for PM.
     * @return A formatted time string (e.g., "14:30" or "2:30 PM").
     */
    public static String formatDisplayTime(int totalMinutes, boolean is24HourFormat, String amString, String pmString) {
        int minutesInDay = (totalMinutes % MINUTES_IN_DAY + MINUTES_IN_DAY) % MINUTES_IN_DAY;

        int hours = minutesInDay / 60;
        int minutes = minutesInDay % 60;

        if (is24HourFormat) {
            return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
        } else {
            String amPm = (hours < 12) ? amString : pmString;
            int displayHour = hours % 12;
            if (displayHour == 0) displayHour = 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minutes, amPm);
        }
    }

    /**
     * Converts a 24-hour time string ("HH:mm") into total minutes from midnight.
     *
     * @param timeString The time string to parse (e.g., "09:45").
     * @return Total minutes (0-1439).
     * @throws IllegalArgumentException If the format is invalid.
     */
    public static int convertTimeToMinutes(@NonNull String timeString) throws IllegalArgumentException {
        if (!timeString.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
            throw new IllegalArgumentException("Invalid time format. Expected hh:mm (24h). Received: " + timeString);
        }
        String[] parts = timeString.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    /**
     * Snaps the given minutes to the nearest step interval.
     *
     * @param minutes The minutes to snap.
     * @param step    The interval (e.g., 15 for 15-minute intervals).
     * @return The snapped minute value.
     */
    public static int snapToStep(int minutes, int step) {
        if (step <= 0) return minutes;
        return Math.round((float) minutes / step) * step;
    }
}
