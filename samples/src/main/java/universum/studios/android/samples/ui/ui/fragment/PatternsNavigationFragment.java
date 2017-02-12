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

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import universum.studios.android.support.examples.model.ExListItem;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.samples.ui.NavigationActivity;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.SearchActivity;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.navigation_item_patterns)
public final class PatternsNavigationFragment extends BaseSamplesNavigationFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "PatternsNavigationFragment";

	@Override
	protected void onBindExamples(@NonNull SamplesAdapter adapter) {
		final List<ExListItem> items = new ArrayList<>();
		items.add(createItem(
				NavigationFragments.PATTERNS_NAVIGATION_DRAWER,
				R.string.patterns_navigation_navigation_drawer
		));
		items.add(createItem(
				NavigationFragments.PATTERNS_SEARCH,
				R.string.patterns_navigation_search
		));
		items.add(createItem(
				NavigationFragments.PATTERNS_SWIPE_TO_REFRESH,
				R.string.patterns_navigation_swipe_to_refresh
		));
		adapter.changeItems(items);
	}

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		switch ((int) id) {
			case NavigationFragments.PATTERNS_NAVIGATION_DRAWER:
				NavigationActivity.launch(getActivity());
				break;
			case NavigationFragments.PATTERNS_SEARCH:
				SearchActivity.launch(getActivity());
				break;
			default:
				super.onItemClick(parent, view, position, id);
		}
	}
}
