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
package universum.studios.android.samples.ui.ui.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import universum.studios.android.support.examples.model.ExListItem;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.samples.ui.PagerActivity;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;

import java.util.ArrayList;
import java.util.List;

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
		final ExListItem item = getAdapter().getItem(position);
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
	protected void onBindExamples(@NonNull SamplesAdapter adapter) {
		final ExListItem.Builder builder = new ExListItem.Builder(getResources());
		final List<ExListItem> items = new ArrayList<>();
		items.add(createItem(
				NavigationFragments.COMPONENTS_BUTTONS,
				R.string.components_navigation_buttons,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_GRIDS,
				R.string.components_navigation_grids,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_LISTS,
				R.string.components_navigation_lists,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_PAGERS,
				R.string.components_navigation_pagers,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_MENUS,
				R.string.components_navigation_menus,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_PICKERS,
				R.string.components_navigation_pickers,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_PROGRESS_AND_ACTIVITY,
				R.string.components_navigation_progress_and_activity,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_SLIDERS,
				R.string.components_navigation_sliders,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_SWITCHES,
				R.string.components_navigation_switches,
				builder
		));
		items.add(createItem(
				NavigationFragments.COMPONENTS_TEXT_FIELDS,
				R.string.components_navigation_text_fields,
				builder
		));
		adapter.changeItems(items);
	}
}
