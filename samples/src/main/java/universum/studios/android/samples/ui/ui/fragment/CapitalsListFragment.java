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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import universum.studios.android.samples.ui.R;
import universum.studios.android.samples.ui.data.model.CapitalItem;
import universum.studios.android.samples.ui.ui.adapter.CapitalsListAdapter;

/**
 * @author Martin Albedinsky
 */
public class CapitalsListFragment extends BaseSamplesListFragment<CapitalsListAdapter> {

	@SuppressWarnings("unused")
	private static final String TAG = "CapitalsListFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAdapter(new CapitalsListAdapter(getActivity()));
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(R.string.components_collection_empty);
	}

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		final CapitalItem capital = getAdapter().getItem(position);
		Toast.makeText(getActivity(), "Clicked: " + capital.name, Toast.LENGTH_SHORT).show();
	}
}
