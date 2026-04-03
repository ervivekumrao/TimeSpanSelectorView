package vivek.umrao.time.span.selector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import vivek.umrao.time.span.selector.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimeSpanSelector.OnRangeChangeListener listener = new TimeSpanSelector.OnRangeChangeListener() {
            @Override
            public void onRangeChanged(int startMinutes, int endMinutes, boolean isOvernight) {
                updateCallbackText(startMinutes, endMinutes, isOvernight);
            }

            @Override
            public void onInteractionFinished(int startMinutes, int endMinutes, boolean isOvernight) {
                updateCallbackText(startMinutes, endMinutes, isOvernight);
            }
        };

        binding.timeSpanDial.setOnRangeChangeListener(listener);
        binding.linearTimeSpanSelector.setOnRangeChangeListener(listener);

        // Demonstrating granular listeners
        binding.timeSpanDial.setOnTimeChangeListener(new TimeSpanSelector.OnTimeChangeListener() {
            @Override
            public void onStartTimeChange(int startTimeMinutes) {
                // Handle specific start change
            }

            @Override
            public void onEndTimeChange(int endTimeMinutes) {
                // Handle specific end change
            }

            @Override
            public void onDurationChange(int durationMinutes) {
                // Handle duration change
            }
        });

        binding.timeSpanDial.setOnDragChangeListener(new TimeSpanSelector.OnDragChangeListener() {
            @Override
            public boolean onDragStart(@NonNull TimeSpanSelector.Thumb thumb) {
                // Optional: show a toast or feedback
                return true;
            }

            @Override
            public void onDragStop(@NonNull TimeSpanSelector.Thumb thumb) {
                // Optional: cleanup
            }
        });

        // Initial update
        updateCallbackText(binding.timeSpanDial.getRangeStartInMinutes(), 
                binding.timeSpanDial.getRangeEndInMinutes(), 
                binding.timeSpanDial.isOvernight());

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );
    }

    private void updateCallbackText(int start, int end, boolean overnight) {
        String startTime = TimeUtils.formatDisplayTime(start, false, "AM", "PM");
        String endTime = TimeUtils.formatDisplayTime(end, false, "AM", "PM");
        int duration = (end >= start) ? (end - start) : (1440 - start + end);
        
        String text = String.format(Locale.getDefault(), 
            "Range: %s - %s\nDuration: %d mins | Overnight: %b", 
            startTime, endTime, duration, overnight);
        
        binding.callbackText.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}