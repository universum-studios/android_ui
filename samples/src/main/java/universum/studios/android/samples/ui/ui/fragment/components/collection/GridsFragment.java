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
package universum.studios.android.samples.ui.ui.fragment.components.collection;

import android.support.annotation.NonNull;

import universum.studios.android.support.examples.model.ExListItem;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.components_navigation_grids)
public final class GridsFragment extends BaseCollectionsFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "GridsFragment";

	@Override
	protected void onBindExamples(@NonNull SamplesAdapter adapter) {
		final ExListItem.Builder builder = new ExListItem.Builder(getResources());
		final List<ExListItem> items = new ArrayList<>();

		items.add(createItem(
				CollectionFragments.GRID_SIMPLE,
				R.string.components_navigation_grids_simple,
				builder
		));
		items.add(createItem(
				CollectionFragments.GRID_PULLABLE,
				R.string.components_navigation_grids_pullable,
				builder
		));

		adapter.changeItems(items);
	}
}
