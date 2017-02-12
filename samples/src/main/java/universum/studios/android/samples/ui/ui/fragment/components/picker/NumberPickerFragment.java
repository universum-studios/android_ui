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
import android.support.annotation.NonNull;
import android.view.View;

import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.fragment.BaseExamplesFragment;
import universum.studios.android.ui.widget.CircularNumberPicker;
import universum.studios.android.util.Logger;
import universum.studios.android.util.Toaster;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(
		title = R.string.components_navigation_pickers_number_picker
)
@ContentView(R.layout.fragment_components_pickers_number)
public final class NumberPickerFragment extends BaseExamplesFragment
		implements
		CircularNumberPicker.OnNumberChangeListener,
		CircularNumberPicker.OnNumberSelectionListener {

	@SuppressWarnings("unused")
	private static final String TAG = "NumberPickerFragment";
	private static final int[] NUMBERS = {
			12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final CircularNumberPicker numberPicker = (CircularNumberPicker) view.findViewById(R.id.picker);
		numberPicker.setNumbers(NUMBERS);
		numberPicker.setOnNumberSelectionListener(this);
		numberPicker.setOnNumberChangeListener(this);
	}

	@Override
	public void onNumberChanged(@NonNull CircularNumberPicker picker, int number) {
		Logger.d(TAG, "Changed to: " + Integer.toString(number));
	}

	@Override
	public void onNumberSelected(@NonNull CircularNumberPicker picker, int number) {
		Toaster.showToast(getActivity(), "Selected: " + Integer.toString(number));
	}
}
