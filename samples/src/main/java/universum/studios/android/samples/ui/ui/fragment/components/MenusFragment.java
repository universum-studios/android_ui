/*
 * =================================================================================================
 *                             Copyright (C) 2014 Martin Albedinsky
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
package universum.studios.android.samples.ui.ui.fragment.components;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.TimerAdapter;
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesFragment;
import universum.studios.android.support.fragment.annotation.ContentView;
import universum.studios.android.ui.widget.SpinnerLayout;

/**
 * @author Martin Albedinsky
 */
@ContentView(R.layout.fragment_components_menus)
public final class MenusFragment extends BaseSamplesFragment
		implements
		Spinner.OnItemSelectedListener,
		SpinnerLayout.OnItemSelectedListener {

	@SuppressWarnings("unused")
	private static final String TAG = "MenusFragment";

	private static final String[] WHEN = {
			"Every night", "Weeknights", "Never"
	};

	private static final String[] COLORS = {
			"Yellow", "Black", "White", "Red", "Blue", "Gray", "Magenta"
	};

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Spinner spinnerTimer = (Spinner) view.findViewById(R.id.fragment_components_menus_spinner_timer);
		final Spinner spinnerWhen = (Spinner) view.findViewById(R.id.fragment_components_menus_spinner_when);
		final Spinner spinnerColors = (Spinner) view.findViewById(R.id.fragment_components_menus_spinner_colors);
		final SpinnerLayout spinnerLayoutTimer = (SpinnerLayout) view.findViewById(R.id.fragment_components_menus_spinner_layout_timer);
		final SpinnerLayout sSpinnerLayoutWhen = (SpinnerLayout) view.findViewById(R.id.fragment_components_menus_spinner_layout_when);
		final SpinnerLayout spinnerLayoutColors = (SpinnerLayout) view.findViewById(R.id.fragment_components_menus_spinner_layout_colors);
		final SpinnerLayout spinnerLayoutColorsDisabled = (SpinnerLayout) view.findViewById(R.id.fragment_components_menus_spinner_layout_colors_disabled);

		final Context context = getActivity();
		spinnerTimer.setAdapter(new TimerAdapter(context));
		spinnerTimer.setSelection(2, false);
		spinnerTimer.setOnItemSelectedListener(this);

		spinnerWhen.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, WHEN));
		spinnerWhen.setSelection(0, false);
		spinnerWhen.setOnItemSelectedListener(this);

		spinnerColors.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, COLORS));
		spinnerColors.setSelection(0, false);
		spinnerColors.setOnItemSelectedListener(this);

		spinnerLayoutTimer.setAdapter(new TimerAdapter(context));
		spinnerLayoutTimer.setSelection(2, false);
		spinnerLayoutTimer.setOnItemSelectedListener(this);

		sSpinnerLayoutWhen.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, WHEN));
		sSpinnerLayoutWhen.setOnItemSelectedListener(this);

		spinnerLayoutColors.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, COLORS));
		spinnerLayoutColors.setOnItemSelectedListener(this);

		spinnerLayoutColorsDisabled.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, COLORS));
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Object item = "";
		switch (parent.getId()) {
			case R.id.fragment_components_menus_spinner_timer:
				item = ((TimerAdapter) parent.getAdapter()).getSelectedItem().value;
				break;
			case R.id.fragment_components_menus_spinner_when:
			case R.id.fragment_components_menus_spinner_colors:
				item = parent.getAdapter().getItem(position);
				break;
		}
		Toast.makeText(getActivity(), "Selected: " + item, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Ignored
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onItemSelected(@NonNull SpinnerLayout spinnerLayout, AdapterView<?> parent, View view, int position, long id) {
		Object item = "";
		switch (spinnerLayout.getId()) {
			case R.id.fragment_components_menus_spinner_layout_timer:
				item = ((TimerAdapter) spinnerLayout.getAdapter()).getSelectedItem().value;
				break;
			case R.id.fragment_components_menus_spinner_layout_when:
				item = spinnerLayout.getAdapter().getItem(position);
				if ("Never".contentEquals((String) item)) {
					spinnerLayout.setError("Never cannot be selected!");
				} else {
					spinnerLayout.clearError();
				}
				break;
			case R.id.fragment_components_menus_spinner_layout_colors:
				item = spinnerLayout.getAdapter().getItem(position);
				break;
		}
		Toast.makeText(getActivity(), "Selected: " + item, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(@NonNull SpinnerLayout spinnerLayout, AdapterView<?> parent) {
		// Ignored
	}
}
