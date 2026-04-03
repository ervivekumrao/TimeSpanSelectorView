# Time Span Selector (Library)

A highly customizable, elegant Time Span Selector library for Android, featuring both **Circular** (Radial) and **Linear** designs. Perfect for scheduling, alarm settings, and any time-duration selection needs.

## Features
- 🕒 **Two Layouts**: Circular and Linear selectors.
- 🌓 **Overnight Support**: Seamlessly handle ranges that cross midnight.
- 🎨 **Fully Customizable**: Colors, widths, tick marks, labels, and thumb drawables.
- 📏 **Step Intervals**: Snap to 1, 5, 15, or 30-minute intervals.
- 🕰️ **24/12 Hour Modes**: Supports both formats with optional AM/PM labels.
- 📱 **Interactive**: Smooth touch controls with customizable touch padding.
- 📝 **Range Text**: Display the selected range with custom formatting and positioning.

---

## Installation

Add the JitPack repository to your root `build.gradle` (or `settings.gradle`):

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.vivekumrao:time-span-selector:1.0.0'
}
```

---

## Usage

### XML Implementation

#### Circular Selector
```xml
<vivek.umrao.time.span.selector.CircularTimeSpanSelector
    android:id="@+id/circularSelector"
    android:layout_width="300dp"
    android:layout_height="300dp"
    app:tss_trackColor="#E0E0E0"
    app:tss_rangeColor="#FF4081"
    app:tss_is24HourFormat="false"
    app:tss_rangeTextPosition="center" />
```

#### Linear Selector
```xml
<vivek.umrao.time.span.selector.LinearTimeSpanSelector
    android:id="@+id/linearSelector"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:tss_rangeColor="#3F51B5"
    app:tss_rangeTextPosition="bottom" />
```

### Event Handling

#### Basic Range Listener
```java
selector.setOnRangeChangeListener(new TimeSpanSelector.OnRangeChangeListener() {
    @Override
    public void onRangeChanged(int startMinutes, int endMinutes, boolean isOvernight) {
        // Real-time updates (raw minutes from midnight: 0-1439)
    }

    @Override
    public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
        // Final selection on touch release
    }
});
```

#### Granular Time Listener
```java
selector.setOnTimeChangeListener(new TimeSpanSelector.OnTimeChangeListener() {
    @Override
    public void onStartTimeChange(int startTimeMinutes) {
        // Called when only start time changes
    }

    @Override
    public void onEndTimeChange(int endTimeMinutes) {
        // Called when only end time changes
    }

    @Override
    public void onDurationChange(int durationMinutes) {
        // Called when total duration changes
    }
});
```

#### Drag Interaction Listener
```java
selector.setOnDragChangeListener(new TimeSpanSelector.OnDragChangeListener() {
    @Override
    public boolean onDragStart(@NonNull TimeSpanSelector.Thumb thumb) {
        // Return true to allow drag, false to intercept/disable
        return true; 
    }

    @Override
    public void onDragStop(@NonNull TimeSpanSelector.Thumb thumb) {
        // Drag interaction finished
    }
});
```

---

## API Reference & Attributes

For a full list of XML attributes and programmatic APIs, please refer to the [Root README](../README.md).

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
Apache License 2.0. See `LICENSE` for details.
