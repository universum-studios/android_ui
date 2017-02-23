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

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.PagerActivity;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;
import universum.studios.android.samples.ui.ui.fragment.BaseSamplesNavigationFragment;
import universum.studios.android.samples.ui.ui.fragment.NavigationFragments;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.samples.model.SampleItem;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.navigation_item_components)
public class ComponentsNavigationFragment extends BaseSamplesNavigationFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "ComponentsNavigationFragment";

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		final SampleItem item = getAdapter().getItem(position);
		if (item != null) {
			switch ((int) item.getId()) {
				case NavigationFragments.COMPONENTS_PAGERS:
					startActivity(new Intent(getActivity(), PagerActivity.class));
					break;
				default:
					super.onItemClick(parent, view, position, id);
			}
		}
	}

	@Override
	protected void onBindSamples(@NonNull SamplesAdapter adapter) {
		final SampleItem.Builder builder = new SampleItem.Builder(getResources());
		final List<SampleItem> items = new ArrayList<>();
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_BUTTONS,
				R.string.components_navigation_buttons
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_GRIDS,
				R.string.components_navigation_grids
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_LISTS,
				R.string.components_navigation_lists
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_PAGERS,
				R.string.components_navigation_pagers
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_MENUS,
				R.string.components_navigation_menus
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_PICKERS,
				R.string.components_navigation_pickers
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_PROGRESS_AND_ACTIVITY,
				R.string.components_navigation_progress_and_activity
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_SLIDERS,
				R.string.components_navigation_sliders
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_SWITCHES,
				R.string.components_navigation_switches
		));
		items.add(createItem(
				builder,
				NavigationFragments.COMPONENTS_TEXT_FIELDS,
				R.string.components_navigation_text_fields
		));
		adapter.changeItems(items);
	}
}
