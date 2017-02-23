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

import java.util.ArrayList;
import java.util.List;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;
import universum.studios.android.support.fragment.annotation.ActionBarOptions;
import universum.studios.android.support.samples.model.SampleItem;

/**
 * @author Martin Albedinsky
 */
@ActionBarOptions(title = R.string.components_navigation_grids)
public final class GridsFragment extends BaseCollectionsFragment {

	@SuppressWarnings("unused")
	private static final String TAG = "GridsFragment";

	@Override
	protected void onBindSamples(@NonNull SamplesAdapter adapter) {
		final SampleItem.Builder builder = new SampleItem.Builder(getResources());
		final List<SampleItem> items = new ArrayList<>();
		items.add(createItem(
				builder,
				CollectionFragments.GRID_SIMPLE,
				R.string.components_navigation_grids_simple
		));
		adapter.changeItems(items);
	}
}
