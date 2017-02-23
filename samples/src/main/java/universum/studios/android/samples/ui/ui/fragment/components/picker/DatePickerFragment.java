/*
 * =================================================================================================
 *                             Copyright (C) 2016 Martin Albedinsky
 * =================================================================================================
 *         Licensed under the Apache License, Version 2.0 or later (further "License" only).
 * -------------------------------------------------------------------------------------------------
 * You may use this file only in compliance with the License. More details and copy of this License 
 * you may obtain at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * You can redistribute, modify or publish any part of the code written within this file but as it 
 * is described in the License, the software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES or CONDITIONS OF ANY KIND.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 * =================================================================================================
 */
package universum.studios.android.samples.ui.ui.fragment.components.picker;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.util.Calendar;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.ui.widget.CalendarView;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(
		title = R.string.components_navigation_pickers_date_picker
)
@ContentView(R.layout.fragment_components_pickers_date)
public final class DatePickerFragment extends BaseSamplesFragment
		implements
		CalendarView.OnDateSelectionListener,
		CalendarView.OnMonthChangeListener,
		CalendarView.OnYearChangeListener {

	@SuppressWarnings("unused")
	private static final String TAG = "DatePickerFragment";

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendar_view);
		calendarView.setOnDateSelectionListener(this);
		calendarView.setOnMonthChangeListener(this);
		calendarView.setOnYearChangeListener(this);
		calendarView.setVisibleDate(System.currentTimeMillis());
		view.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				calendarView.smoothScrollToPreviousMonth();
			}
		});
		view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				calendarView.smoothScrollToNextMonth();
			}
		});
	}

	@Override
	public void onDateSelected(@NonNull CalendarView calendarView, long dateInMillis) {
		Log.d(TAG, "Selected date in calendar: " + dateInMillis);
	}

	@Override
	public void onNoDateSelected(@NonNull CalendarView calendarView) {
		Log.d(TAG, "No date selected in calendar.");
	}

	@Override
	public void onMonthScrolled(@NonNull CalendarView calendarView, @IntRange(from = Calendar.JANUARY, to = Calendar.DECEMBER) int month, @FloatRange(from = 0.0f, to = 1.0f) float offset) {
		Log.d(TAG, "Month scrolled to: " + offset);
	}

	@Override
	public void onMonthChanged(@NonNull CalendarView calendarView, @IntRange(from = Calendar.JANUARY, to = Calendar.DECEMBER) int month) {
		Log.d(TAG, "Calendar scrolled to month: " + month);
	}

	@Override
	public void onYearChanged(@NonNull CalendarView calendarView, int year) {
		Log.d(TAG, "Calendar scrolled to year: " + year);
	}
}
