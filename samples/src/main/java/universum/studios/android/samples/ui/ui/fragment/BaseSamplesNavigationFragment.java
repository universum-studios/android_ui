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
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import universum.studios.android.samples.ui.ui.adapter.SamplesAdapter;
import universum.studios.android.support.fragment.manage.FragmentController;

/**
 * @author Martin Albedinsky
 */
public abstract class BaseSamplesNavigationFragment extends BaseSamplesListFragment<SamplesAdapter> {

	@SuppressWarnings("unused")
	private static final String TAG = "BaseNavigationFragment";

	protected FragmentController mFragmentController;

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
		final ExListItem item = getAdapter().getItem(position);
		if (item != null) mFragmentController.showFragment((int) item.getId());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up adapter.
		final SamplesAdapter adapter = new SamplesAdapter(getActivity());
		onBindExamples(adapter);
		setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mFragmentController = ((HomeActivity) getActivity()).getFragmentController();
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((HomeActivity) getActivity()).setNavigationAccessible(true);
	}

	protected abstract void onBindExamples(@NonNull SamplesAdapter adapter);

	protected ExListItem createItem(int id, int titleResId) {
		return createItem(id, titleResId, new ExListItem.Builder(getResources()));
	}

	protected ExListItem createItem(int id, int titleResId, ExListItem.Builder builder) {
		return builder.reset().id(id).text(titleResId).build();
	}
}
