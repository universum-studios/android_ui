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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;
import universum.studios.android.samples.ui.ui.fragment.BaseSectionNavigationFragment;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.fragment.manage.FragmentController;
import universum.studios.android.support.samples.model.SampleItem;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.components_navigation_pickers)
public final class PickersFragment extends BaseSectionNavigationFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "PickersFragment";

	@Override
	protected void onBindSamples(@NonNull SamplesAdapter adapter) {
		final SampleItem.Builder builder = new SampleItem.Builder(getResources());
		final List<SampleItem> items = new ArrayList<>();
		items.add(createItem(
				builder,
				PickersFragments.NUMBER_PICKER,
				R.string.components_navigation_pickers_number_picker
		));
		items.add(createItem(
				builder,
				PickersFragments.DATE_PICKER,
				R.string.components_navigation_pickers_date_picker
		));
		adapter.changeItems(items);
	}

	@Override
	protected boolean hasOwnFragmentsFactory() {
		return true;
	}

	@Override
	protected void onAttachFragmentsFactory(FragmentController controller) {
		controller.setFactory(new PickersFragments());
	}
}
